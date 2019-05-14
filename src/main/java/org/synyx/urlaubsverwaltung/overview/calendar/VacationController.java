package org.synyx.urlaubsverwaltung.overview.calendar;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.synyx.urlaubsverwaltung.api.ResponseWrapper;
import org.synyx.urlaubsverwaltung.api.RestApiDateFormat;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.account.service.AccountService;
import org.synyx.urlaubsverwaltung.account.service.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.holiday.VacationDays;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;
import org.synyx.urlaubsverwaltung.api.ResponseWrapper;
import org.synyx.urlaubsverwaltung.api.RestApiDateFormat;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;
import org.synyx.urlaubsverwaltung.restapi.vacation.VacationLengthResponse;

import java.math.BigDecimal;
import java.util.*;

import static java.util.stream.Collectors.toList;


@Api("Vacations: Get all vacations for a certain period")
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
    @GetMapping("/vacations")
    public ResponseWrapper<VacationListResponse> vacations(
        @ApiParam(value = "Get vacations for department members of person")
        @RequestParam(value = "departmentMembers", required = false)
        Boolean departmentMembers,
        @ApiParam(value = "Start date with pattern yyyy-MM-dd", defaultValue = RestApiDateFormat.EXAMPLE_FIRST_DAY_OF_YEAR)
        @RequestParam(value = "from")
        String from,
        @ApiParam(value = "End date with pattern yyyy-MM-dd", defaultValue = RestApiDateFormat.EXAMPLE_LAST_DAY_OF_YEAR)
        @RequestParam(value = "to")
        String to,
        @ApiParam(value = "ID of the person")
        @RequestParam(value = "person", required = false)
        Integer personId) {

        LocalDate startDate;
        LocalDate endDate;
        try{
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern(RestApiDateFormat.DATE_PATTERN);
            startDate = LocalDate.parse(from, fmt);
            endDate = LocalDate.parse(to, fmt);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(exception.getMessage());
        }

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

        List<VacationResponse> vacationResponses = applications.stream().map(VacationResponse::new).collect(toList());

        return new ResponseWrapper<>(new VacationListResponse(vacationResponses));
    }

    @ApiOperation(
            value = "Get all vacation length for a certain year and person", notes = "Get vacation length for a certain year and person. "
    )
    @RequestMapping(value = "/vacations/length")
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
            LocalDate startDate = DateUtil.getFirstDayOfMonth(year, month);
            LocalDate endDate = DateUtil.getLastDayOfMonth(year, month);

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

        int monthOfStartDate = application.getStartDate().getMonthValue();
        int monthOfEndDate = application.getEndDate().getMonthValue();

        DayLength dayLength = application.getDayLength();
        Person person = application.getPerson();

        if (yearOfStartDate != yearOfEndDate || monthOfStartDate != monthOfEndDate) {
            LocalDate startDate = getStartDateForCalculation(application, relevantYear, relevantMonth);
            LocalDate endDate = getEndDateForCalculation(application, relevantYear, relevantMonth);

            return calendarService.getWorkDays(dayLength, startDate, endDate, person);
        }

        return calendarService.getWorkDays(dayLength, application.getStartDate(), application.getEndDate(), person);
    }


    private LocalDate getStartDateForCalculation(Application application, int relevantYear, int relevantMonth) {

        if (application.getStartDate().getYear() != relevantYear) {
            return DateUtil.getFirstDayOfYear(application.getEndDate().getYear());
        } else if (application.getStartDate().getMonthValue() != relevantMonth) {
            return DateUtil.getFirstDayOfMonth(relevantYear, relevantMonth);
        }

        return application.getStartDate();
    }


    private LocalDate getEndDateForCalculation(Application application, int relevantYear, int relevantMonth) {

        if (application.getEndDate().getYear() != relevantYear) {
            return DateUtil.getLastDayOfYear(application.getStartDate().getYear());
        } else if (application.getEndDate().getMonthValue()!= relevantMonth) {
            return DateUtil.getLastDayOfMonth(relevantYear, relevantMonth);
        }

        return application.getEndDate();
    }
}
