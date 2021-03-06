package org.synyx.urlaubsverwaltung.availability.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Service
class HolidayAbsenceProvider extends AbstractTimedAbsenceProvider {

    private final PublicHolidaysService publicHolidaysService;
    private final WorkingTimeService workingTimeService;

    @Autowired
    HolidayAbsenceProvider(SickDayAbsenceProvider nextPriorityProvider, PublicHolidaysService publicHolidaysService,
        WorkingTimeService workingTimeService) {

        super(nextPriorityProvider);

        this.publicHolidaysService = publicHolidaysService;
        this.workingTimeService = workingTimeService;
    }

    @Override
    TimedAbsenceSpans addAbsence(TimedAbsenceSpans knownAbsences, Person person, LocalDate date) {

        Optional<TimedAbsence> holidayAbsence = checkForHolidays(date, person);

        if (holidayAbsence.isPresent()) {
            List<TimedAbsence> knownAbsencesList = knownAbsences.getAbsencesList();
            knownAbsencesList.add(holidayAbsence.get());

            return new TimedAbsenceSpans(knownAbsencesList);
        }

        return knownAbsences;
    }


    @Override
    boolean isLastPriorityProvider() {

        return false;
    }


    private Optional<TimedAbsence> checkForHolidays(LocalDate currentDay, Person person) {

        BigDecimal expectedWorkingDuration = publicHolidaysService.getWorkingDurationOfDate(currentDay,
                getFederalState(currentDay, person));

        boolean fullDayHoliday = expectedWorkingDuration.compareTo(DayLength.ZERO.getDuration()) == 0;
        boolean halfDayHoliday = expectedWorkingDuration.compareTo(DayLength.NOON.getDuration()) == 0;

        TimedAbsence absence = null;

        if (fullDayHoliday) {
            absence = new TimedAbsence(DayLength.FULL, TimedAbsence.Type.HOLIDAY);
        } else if (halfDayHoliday) {
            absence = new TimedAbsence(DayLength.NOON, TimedAbsence.Type.HOLIDAY);
        }

        return Optional.ofNullable(absence);
    }


    private FederalState getFederalState(LocalDate date, Person person) {

        return workingTimeService.getFederalStateForPerson(person, date);
    }
}
