package org.synyx.urlaubsverwaltung.account.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.account.domain.VacationDaysLeft;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.period.NowService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Provides calculation of used / left vacation days.
 */
@Service
public class VacationDaysService {

    private final WorkDaysService calendarService;
    private final NowService nowService;
    private final ApplicationService applicationService;

    @Autowired
    public VacationDaysService(WorkDaysService calendarService, NowService nowService,
        ApplicationService applicationService) {

        this.calendarService = calendarService;
        this.nowService = nowService;
        this.applicationService = applicationService;
    }

    /**
     * Calculates the total number of days that are left to be used for applying for leave.
     *
     * <p>NOTE: The calculation depends on the current date. If it's before April, the left remaining vacation days are
     * relevant for calculation and if it's after April, only the not expiring remaining vacation days are relevant for
     * calculation.</p>
     *
     * @param  account  {@link org.synyx.urlaubsverwaltung.account.domain.Account}
     *
     * @return  total number of left vacation days
     */
    public BigDecimal calculateTotalLeftVacationDays(Account account) {

        VacationDaysLeft vacationDaysLeft = getVacationDaysLeft(account, Optional.empty());

        // it's before April - the left remaining vacation days must be used
        if (nowService.currentYear() == account.getYear() && DateUtil.isBeforeApril(nowService.now(), account.getYear())) {
            return vacationDaysLeft.getVacationDays().add(vacationDaysLeft.getRemainingVacationDays());
        } else {
            // it's after April - only the left not expiring remaining vacation days must be used
            return vacationDaysLeft.getVacationDays().add(vacationDaysLeft.getRemainingVacationDaysNotExpiring());
        }
    }

    /**
     * This version of the method also considers the account for next year,
     * so that it can adjust for vacation days carried over from this year to the next and then used there
     * (reducing the amount available in this year accordingly)
     *
     * @param account the account for the year to calculate the vacation days for
     *
     * @param nextYear the account for following year, if available
     *
     * @return information about the vacation days left for that year
     */
    public VacationDaysLeft getVacationDaysLeft(Account account, Optional<Account> nextYear) {

        BigDecimal vacationDays = account.getVacationDays();
        BigDecimal remainingVacationDays = account.getRemainingVacationDays();
        BigDecimal remainingVacationDaysNotExpiring = account.getRemainingVacationDaysNotExpiring();

        BigDecimal daysBeforeApril = getUsedDaysBeforeApril(account);
        BigDecimal daysAfterApril = getUsedDaysAfterApril(account);
        BigDecimal daysUsedNextYear = getRemainingVacationDaysAlreadyUsed(nextYear);

        return VacationDaysLeft.builder()
                .withAnnualVacation(vacationDays)
                .withRemainingVacation(remainingVacationDays)
                .notExpiring(remainingVacationDaysNotExpiring)
                .forUsedDaysBeforeApril(daysBeforeApril)
                .forUsedDaysAfterApril(daysAfterApril)
                .withVacationDaysUsedNextYear(daysUsedNextYear)
                .get();
    }


    /**
     * Returns the already used vacations from last year of the given account.
     *
     * @param account the account for the year to calculate the vacation days that are used from the year before
     *
     * @return total number of used vacations
     */
    public BigDecimal getRemainingVacationDaysAlreadyUsed(Optional<Account> account) {
        if (account.isPresent() && account.get().getRemainingVacationDays().signum() > 0) {

            VacationDaysLeft left = getVacationDaysLeft(account.get(), Optional.empty());

            BigDecimal totalUsed = account.get().getVacationDays()
                    .add(account.get().getRemainingVacationDays())
                    .subtract(left.getVacationDays())
                    .subtract(left.getRemainingVacationDays());

            BigDecimal remainingUsed = totalUsed.subtract(account.get().getVacationDays());

            if (remainingUsed.signum() > 0) {
                return remainingUsed;
            }
        }
        return BigDecimal.ZERO;
    }


    BigDecimal getUsedDaysBeforeApril(Account account) {

        LocalDate firstOfJanuary = DateUtil.getFirstDayOfMonth(account.getYear(), Month.JANUARY.getValue());
        LocalDate lastOfMarch = DateUtil.getLastDayOfMonth(account.getYear(), Month.MARCH.getValue());

        return getUsedDaysBetweenTwoMilestones(account.getPerson(), firstOfJanuary, lastOfMarch);
    }


    BigDecimal getUsedDaysAfterApril(Account account) {

        LocalDate firstOfApril = DateUtil.getFirstDayOfMonth(account.getYear(), Month.APRIL.getValue());
        LocalDate lastOfDecember = DateUtil.getLastDayOfMonth(account.getYear(), Month.DECEMBER.getValue());

        return getUsedDaysBetweenTwoMilestones(account.getPerson(), firstOfApril, lastOfDecember);
    }


    BigDecimal getUsedDaysBetweenTwoMilestones(Person person, LocalDate firstMilestone, LocalDate lastMilestone) {

        // get all applications for leave
        List<Application> allApplicationsForLeave = applicationService.getApplicationsForACertainPeriodAndPerson(
                firstMilestone, lastMilestone, person);

        // filter them since only waiting and allowed applications for leave of type holiday are relevant
        List<Application> applicationsForLeave = allApplicationsForLeave.stream()
                .filter(input ->
                            VacationCategory.HOLIDAY.equals(input.getVacationType().getCategory())
                            && (input.hasStatus(ApplicationStatus.WAITING)
                                || input.hasStatus(ApplicationStatus.ALLOWED)))
                .collect(Collectors.toList());

        BigDecimal usedDays = BigDecimal.ZERO;

        for (Application applicationForLeave : applicationsForLeave) {
            LocalDate startDate = applicationForLeave.getStartDate();
            LocalDate endDate = applicationForLeave.getEndDate();

            if (startDate.isBefore(firstMilestone)) {
                startDate = firstMilestone;
            }

            if (endDate.isAfter(lastMilestone)) {
                endDate = lastMilestone;
            }

            usedDays = usedDays.add(calendarService.getWorkDays(applicationForLeave.getDayLength(), startDate, endDate,
                        person));
        }

        return usedDays;
    }
}
