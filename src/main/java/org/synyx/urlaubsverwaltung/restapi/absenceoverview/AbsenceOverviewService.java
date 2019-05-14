package org.synyx.urlaubsverwaltung.restapi.absenceoverview;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.holiday.DayOfMonth;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceOverview;
import org.synyx.urlaubsverwaltung.util.DateUtil;
import org.synyx.urlaubsverwaltung.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.person.api.PersonResponse;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import static java.time.ZoneOffset.UTC;

@Component
public class AbsenceOverviewService {

    private PersonService personService;
    private WorkingTimeService workingTimeService;
    private PublicHolidaysService publicHolidayService;
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Autowired
    AbsenceOverviewService(PersonService personService,
                           WorkingTimeService workingTimeService,
                           PublicHolidaysService publicHolidayService) {

        this.personService = personService;
        this.workingTimeService = workingTimeService;
        this.publicHolidayService = publicHolidayService;
    }

    public AbsenceOverview getAbsenceOverview(Integer personId, Integer selectedYear) throws UnknownPersonException {
        LocalDate date = LocalDate.now(UTC);

        Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));

        int year = selectedYear != null ? selectedYear : date.getYear();

        AbsenceOverview absenceOverview = new AbsenceOverview();
        absenceOverview.setMonths(new LinkedHashMap<>());
        absenceOverview.setPerson(new PersonResponse(person));
        absenceOverview.setPersonID(personId);

        for(int monthCounter = 1; monthCounter <= 12; monthCounter++) {
            LocalDate lastDay = DateUtil.getLastDayOfMonth(year, monthCounter);
            Integer month = monthCounter;
            ArrayList<DayOfMonth> days = new ArrayList<>();
            Integer dayCounter = 1;

            while (dayCounter <= lastDay.lengthOfMonth()) {
                DayOfMonth dayOfMonth = new DayOfMonth();
                LocalDate currentDay = LocalDate.of(year, month, dayCounter);
                dayOfMonth.setDayText(currentDay.format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
                dayOfMonth.setDayNumber(dayCounter);

                dayOfMonth.setTypeOfDay(getTypeOfDay(person, currentDay));
                days.add(dayOfMonth);
                dayCounter++;
            }

            while (dayCounter <= 31) {
                DayOfMonth dayOfMonth = new DayOfMonth();
                dayOfMonth.setDayNumber(monthCounter);
                days.add(dayOfMonth);
                dayCounter++;
            }

            absenceOverview.getMonths().put(monthCounter, days);
        }
        return absenceOverview;
    }

    private DayOfMonth.TypeOfDay getTypeOfDay(Person person, LocalDate currentDay) {
        DayOfMonth.TypeOfDay typeOfDay;

        FederalState state = workingTimeService.getFederalStateForPerson(person, currentDay);
        if (DateUtil.isWorkDay(currentDay)
                && (publicHolidayService.getWorkingDurationOfDate(currentDay, state).longValue() > 0)) {

            typeOfDay = DayOfMonth.TypeOfDay.WORKDAY;
        } else {
            typeOfDay = DayOfMonth.TypeOfDay.WEEKEND;
        }
        return typeOfDay;
    }
}
