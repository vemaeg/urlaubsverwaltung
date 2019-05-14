package org.synyx.urlaubsverwaltung.restapi.vacation;

import org.synyx.urlaubsverwaltung.core.holiday.VacationDays;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.util.Map;

public class VacationLengthResponse {
    private BigDecimal vacationDaysLeft;
    private BigDecimal previousYearRemainingDays;
    private BigDecimal vacationDaysAllowed;
    private Map<Integer, VacationDays> vacationDays;
    private Person person;

    public VacationLengthResponse(BigDecimal vacationDaysLeft, BigDecimal previousYearRemainingDays, BigDecimal vacationDaysAllowed, Map<Integer, VacationDays> vacationDays, Person person) {
        this.vacationDaysLeft = vacationDaysLeft;
        this.previousYearRemainingDays = previousYearRemainingDays;
        this.vacationDaysAllowed = vacationDaysAllowed;
        this.vacationDays = vacationDays;
        this.person = person;
    }

    public BigDecimal getVacationDaysLeft() {
        return vacationDaysLeft;
    }

    public void setVacationDaysLeft(BigDecimal vacationDaysLeft) {
        this.vacationDaysLeft = vacationDaysLeft;
    }

    public BigDecimal getPreviousYearRemainingDays() {
        return previousYearRemainingDays;
    }

    public void setPreviousYearRemainingDays(BigDecimal previousYearRemainingDays) {
        this.previousYearRemainingDays = previousYearRemainingDays;
    }

    public Map<Integer, VacationDays> getVacationDays() {
        return vacationDays;
    }

    public void setVacationDays(Map<Integer, VacationDays> vacationDays) {
        this.vacationDays = vacationDays;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public BigDecimal getVacationDaysAllowed() {
        return vacationDaysAllowed;
    }

    public void setVacationDaysAllowed(BigDecimal vacationDaysAllowed) {
        this.vacationDaysAllowed = vacationDaysAllowed;
    }
}
