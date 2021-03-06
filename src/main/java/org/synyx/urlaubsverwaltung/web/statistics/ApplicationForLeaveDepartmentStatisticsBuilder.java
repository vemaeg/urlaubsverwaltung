package org.synyx.urlaubsverwaltung.web.statistics;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.account.service.AccountService;
import org.synyx.urlaubsverwaltung.account.service.VacationDaysService;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
public class ApplicationForLeaveDepartmentStatisticsBuilder {

    private final AccountService accountService;
    private final ApplicationService applicationService;
    private final WorkDaysService calendarService;
    private final VacationDaysService vacationDaysService;
    private final VacationTypeService vacationTypeService;

    @Autowired
    public ApplicationForLeaveDepartmentStatisticsBuilder(AccountService accountService, ApplicationService applicationService,
                                                          WorkDaysService calendarService, VacationDaysService vacationDaysService,
                                                          VacationTypeService vacationTypeService) {

        this.accountService = accountService;
        this.applicationService = applicationService;
        this.calendarService = calendarService;
        this.vacationDaysService = vacationDaysService;
        this.vacationTypeService = vacationTypeService;
    }

    public ApplicationForLeaveDepartmentStatistics build(Department department, LocalDate from, LocalDate to) {
        Assert.notNull(department, "Department must be given.");
        Assert.notNull(from, "From must be given.");
        Assert.notNull(to, "To must be given.");

        Assert.isTrue(from.getYear() == to.getYear(), "From and to must be in the same year");

        BigDecimal vacationDaysLeft = BigDecimal.ZERO;

        ApplicationForLeaveDepartmentStatistics statistics = new ApplicationForLeaveDepartmentStatistics(department, vacationTypeService);

        List<Person>persons = department.getMembers();

        for (Person person: persons) {
            Optional<Account> account = accountService.getHolidaysAccount(from.getYear(), person);

            if (account.isPresent()) {
                vacationDaysLeft = vacationDaysLeft.add(vacationDaysService.calculateTotalLeftVacationDays(account.get()));
            }

            List<Application> applications = applicationService.getApplicationsForACertainPeriodAndPerson(from, to, person);

            for (Application application : applications) {
                if (application.hasStatus(ApplicationStatus.WAITING)
                        || application.hasStatus(ApplicationStatus.TEMPORARY_ALLOWED)) {
                    statistics.addWaitingVacationDays(application.getVacationType(),
                            getVacationDays(application, from.getYear()));
                } else if (application.hasStatus(ApplicationStatus.ALLOWED)) {
                    statistics.addAllowedVacationDays(application.getVacationType(),
                            getVacationDays(application, from.getYear()));
                }
            }
        }

        statistics.setLeftVacationDays(vacationDaysLeft);
        return statistics;
    }

    private BigDecimal getVacationDays(Application application, int relevantYear) {

        int yearOfStartDate = application.getStartDate().getYear();
        int yearOfEndDate = application.getEndDate().getYear();

        DayLength dayLength = application.getDayLength();
        Person person = application.getPerson();

        if (yearOfStartDate != yearOfEndDate) {
            LocalDate startDate = getStartDateForCalculation(application, relevantYear);
            LocalDate endDate = getEndDateForCalculation(application, relevantYear);

            return calendarService.getWorkDays(dayLength, startDate, endDate, person);
        }

        return calendarService.getWorkDays(dayLength, application.getStartDate(), application.getEndDate(), person);
    }


    private LocalDate getStartDateForCalculation(Application application, int relevantYear) {

        if (application.getStartDate().getYear() != relevantYear) {
            return DateUtil.getFirstDayOfYear(application.getEndDate().getYear());
        }

        return application.getStartDate();
    }


    private LocalDate getEndDateForCalculation(Application application, int relevantYear) {

        if (application.getEndDate().getYear() != relevantYear) {
            return DateUtil.getLastDayOfYear(application.getStartDate().getYear());
        }

        return application.getEndDate();
    }
}
