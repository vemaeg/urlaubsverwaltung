package org.synyx.urlaubsverwaltung.restapi.vacation;

import com.google.common.collect.Lists;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.account.service.AccountService;
import org.synyx.urlaubsverwaltung.core.account.service.VacationDaysService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.holiday.VacationDays;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkDaysService;
import org.synyx.urlaubsverwaltung.restapi.ResponseWrapper;
import org.synyx.urlaubsverwaltung.restapi.RestApiDateFormat;
import org.synyx.urlaubsverwaltung.restapi.absence.AbsenceResponse;
import org.synyx.urlaubsverwaltung.web.person.UnknownPersonException;

import java.math.BigDecimal;
import java.util.*;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Api(value = "Vacations", description = "Get all vacations for a certain period")
@RestController("restApiVacationController")
@RequestMapping("/api")
public class VacationController {

    private final PersonService personService;
    private final ApplicationService applicationService;
    private final DepartmentService departmentService;
    private final AccountService accountService;
    private final VacationDaysService vacationDaysService;
    private final WorkDaysService calendarService;

    @Autowired
    VacationController(PersonService personService, ApplicationService applicationService,
        DepartmentService departmentService, AccountService accountService, VacationDaysService vacationDaysService, WorkDaysService calendarService) {

        this.personService = personService;
        this.applicationService = applicationService;
        this.departmentService = departmentService;
        this.accountService = accountService;
        this.vacationDaysService = vacationDaysService;
        this.calendarService = calendarService;
    }

    @ApiOperation(
        value = "Get all allowed vacations for a certain period",
        notes = "Get all allowed vacations for a certain period. "
            + "If a person is specified, only the allowed vacations of the person are fetched. "
            + "If a person and the department members flag is specified, "
            + "then all the waiting and allowed vacations of the departments the person is assigned to, are fetched. "
            + "Information only reachable for users with role office."
    )
    @RequestMapping(value = "/vacations", method = RequestMethod.GET)
    public ResponseWrapper<VacationListResponse> vacations(
        @ApiParam(value = "Get vacations for department members of person")
        @RequestParam(value = "departmentMembers", required = false)
        Boolean departmentMembers,
        @ApiParam(value = "Start date with pattern yyyy-MM-dd", defaultValue = "2016-01-01")
        @RequestParam(value = "from")
        String from,
        @ApiParam(value = "End date with pattern yyyy-MM-dd", defaultValue = "2016-12-31")
        @RequestParam(value = "to")
        String to,
        @ApiParam(value = "ID of the person")
        @RequestParam(value = "person", required = false)
        Integer personId) {

        DateTimeFormatter formatter = DateTimeFormat.forPattern(RestApiDateFormat.DATE_PATTERN);
        DateMidnight startDate = formatter.parseDateTime(from).toDateMidnight();
        DateMidnight endDate = formatter.parseDateTime(to).toDateMidnight();

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Parameter 'from' must be before or equals to 'to' parameter");
        }

        List<Application> applications = new ArrayList<>();

        if (personId == null && departmentMembers == null) {
            applications = applicationService.getApplicationsForACertainPeriodAndState(startDate, endDate,
                    ApplicationStatus.ALLOWED);
        }

        if (personId != null) {
            Optional<Person> person = personService.getPersonByID(personId);

            if (person.isPresent()) {
                if (departmentMembers == null || !departmentMembers) {
                    applications = applicationService.getApplicationsForACertainPeriodAndPersonAndState(startDate,
                            endDate, person.get(), ApplicationStatus.ALLOWED);
                } else {
                    applications = departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person.get(),
                            startDate, endDate);
                }
            }
        }

        List<AbsenceResponse> vacationResponses = Lists.transform(applications, AbsenceResponse::new);

        return new ResponseWrapper<>(new VacationListResponse(vacationResponses));
    }

    @ApiOperation(
            value = "Get all vacation length for a certain year and person", notes = "Get vacation length for a certain year and person. "
    )
    @RequestMapping(value = "/vacations/length", method = RequestMethod.GET)
    public ResponseWrapper<VacationLengthResponse> vacationLength(
            @ApiParam(value = "Year", defaultValue = "2016")
            @RequestParam(value = "year")
                    Integer year,
            @ApiParam(value = "ID of the person")
            @RequestParam(value = "person")
                    Integer personId) throws UnknownPersonException {

        Map<Integer, VacationDays> vacationDays = new HashMap<>();

        Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));

        Optional<Account> account = accountService.getHolidaysAccount(year, person);

        BigDecimal vacationDaysLeft = BigDecimal.ZERO;
        BigDecimal previousYearRestDays = BigDecimal.ZERO;
        BigDecimal vacationDaysAllowed = BigDecimal.ZERO;


        if (account.isPresent()) {
            vacationDaysLeft = vacationDaysService.calculateTotalLeftVacationDays(account.get());
            previousYearRestDays = account.get().getRemainingVacationDays();
            vacationDaysAllowed = account.get().getVacationDays();
        }

        for (int month = 1; month <= 12; month++) {
            DateMidnight startDate = DateUtil.getFirstDayOfMonth(year, month);
            DateMidnight endDate = DateUtil.getLastDayOfMonth(year, month);

            BigDecimal waitingVacationDays = BigDecimal.ZERO;
            BigDecimal allowedVacationDays = BigDecimal.ZERO;

            List<Application> applications = applicationService.getApplicationsForACertainPeriodAndPerson(startDate, endDate, person);

            for (Application application : applications) {
                if (application.getVacationType().isOfCategory(VacationCategory.HOLIDAY)) {
                    if (application.hasStatus(ApplicationStatus.WAITING)
                            || application.hasStatus(ApplicationStatus.TEMPORARY_ALLOWED)) {
                        waitingVacationDays = waitingVacationDays.add(getVacationDays(application, year, month));
                    } else if (application.hasStatus(ApplicationStatus.ALLOWED)) {
                        allowedVacationDays = allowedVacationDays.add(getVacationDays(application, year, month));
                    }
                }
            }
            vacationDays.put(month, new VacationDays(waitingVacationDays, allowedVacationDays));
        }
        return new ResponseWrapper<>(new VacationLengthResponse(vacationDaysLeft, previousYearRestDays, vacationDaysAllowed, vacationDays, person));
    }

    private BigDecimal getVacationDays(Application application, Integer relevantYear, Integer relevantMonth) {

        int yearOfStartDate = application.getStartDate().getYear();
        int yearOfEndDate = application.getEndDate().getYear();

        int monthOfStartDate = application.getStartDate().getMonthOfYear();
        int monthOfEndDate = application.getEndDate().getMonthOfYear();

        DayLength dayLength = application.getDayLength();
        Person person = application.getPerson();

        if (yearOfStartDate != yearOfEndDate || monthOfStartDate != monthOfEndDate) {
            DateMidnight startDate = getStartDateForCalculation(application, relevantYear, relevantMonth);
            DateMidnight endDate = getEndDateForCalculation(application, relevantYear, relevantMonth);

            return calendarService.getWorkDays(dayLength, startDate, endDate, person);
        }

        return calendarService.getWorkDays(dayLength, application.getStartDate(), application.getEndDate(), person);
    }


    private DateMidnight getStartDateForCalculation(Application application, int relevantYear, int relevantMonth) {

        if (application.getStartDate().getYear() != relevantYear) {
            return DateUtil.getFirstDayOfYear(application.getEndDate().getYear());
        } else if (application.getStartDate().getMonthOfYear() != relevantMonth) {
            return DateUtil.getFirstDayOfMonth(relevantYear, relevantMonth);
        }

        return application.getStartDate();
    }


    private DateMidnight getEndDateForCalculation(Application application, int relevantYear, int relevantMonth) {

        if (application.getEndDate().getYear() != relevantYear) {
            return DateUtil.getLastDayOfYear(application.getStartDate().getYear());
        } else if (application.getEndDate().getMonthOfYear() != relevantMonth) {
            return DateUtil.getLastDayOfMonth(relevantYear, relevantMonth);
        }

        return application.getEndDate();
    }
}
