package org.synyx.urlaubsverwaltung.restapi.absenceoverview;

import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.core.holiday.DayOfMonth;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.settings.FederalState;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceOverview;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.core.workingtime.PublicHolidaysService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.restapi.person.PersonResponse;
import org.synyx.urlaubsverwaltung.web.person.UnknownPersonException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.synyx.urlaubsverwaltung.core.holiday.DayOfMonth.TypeOfDay.WEEKEND;
import static org.synyx.urlaubsverwaltung.core.holiday.DayOfMonth.TypeOfDay.WORKDAY;

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
        DateMidnight date = new DateMidnight();

        Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));

        int year = selectedYear != null ? selectedYear : date.getYear();

        AbsenceOverview absenceOverview = new AbsenceOverview();
        absenceOverview.setMonths(new LinkedHashMap<>());
        absenceOverview.setPerson(new PersonResponse(person));
        absenceOverview.setPersonID(personId);

        for(int monthCounter = 1; monthCounter <= 12; monthCounter++) {
            DateMidnight lastDay = DateUtil.getLastDayOfMonth(year, monthCounter);
            Integer month = monthCounter;
            ArrayList<DayOfMonth> days = new ArrayList<>();
            Integer dayCounter = 1;

            while (dayCounter <= lastDay.toDate().getDate()) {
                DayOfMonth dayOfMonth = new DayOfMonth();
                DateMidnight currentDay = new DateMidnight(year, month, dayCounter);
                dayOfMonth.setDayText(currentDay.toString(DATE_FORMAT));
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

    private DayOfMonth.TypeOfDay getTypeOfDay(Person person, DateMidnight currentDay) {
        DayOfMonth.TypeOfDay typeOfDay;

        FederalState state = workingTimeService.getFederalStateForPerson(person, currentDay);
        if (DateUtil.isWorkDay(currentDay)
                && (publicHolidayService.getWorkingDurationOfDate(currentDay, state).longValue() > 0)) {

            typeOfDay = WORKDAY;
        } else {
            typeOfDay = WEEKEND;
        }
        return typeOfDay;
    }
}
