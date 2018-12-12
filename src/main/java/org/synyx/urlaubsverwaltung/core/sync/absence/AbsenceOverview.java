package org.synyx.urlaubsverwaltung.core.sync.absence;

import org.synyx.urlaubsverwaltung.core.holiday.DayOfMonth;
import org.synyx.urlaubsverwaltung.restapi.person.PersonResponse;

import java.util.List;
import java.util.Map;

public class AbsenceOverview {
    private PersonResponse person;
    private Integer personID;
    private Map<Integer, List<DayOfMonth>> months;

    public PersonResponse getPerson() {
        return person;
    }

    public void setPerson(PersonResponse person) {
        this.person = person;
    }

    public Integer getPersonID() {
        return personID;
    }

    public void setPersonID(Integer personID) {
        this.personID = personID;
    }

    public Map<Integer, List<DayOfMonth>> getMonths() {
        return months;
    }

    public void setMonths(Map<Integer, List<DayOfMonth>> months) {
        this.months = months;
    }
}
