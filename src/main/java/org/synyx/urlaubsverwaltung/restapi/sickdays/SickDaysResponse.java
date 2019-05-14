package org.synyx.urlaubsverwaltung.restapi.sickdays;

import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;

public class SickDaysResponse {
    private BigDecimal childSickDays;
    private BigDecimal childSickDaysWithAub;

    private BigDecimal sickDays;
    private BigDecimal sickDaysWithAub;

    private Person person;

    public SickDaysResponse(BigDecimal childSickDays, BigDecimal childSickDaysWithAub, BigDecimal sickDays, BigDecimal sickDaysWithAub, Person person) {
        this.childSickDays = childSickDays;
        this.childSickDaysWithAub = childSickDaysWithAub;
        this.sickDays = sickDays;
        this.sickDaysWithAub = sickDaysWithAub;
        this.person = person;
    }
    public BigDecimal getChildSickDays() {
        return childSickDays;
    }

    public void setChildSickDays(BigDecimal childSickDays) {
        this.childSickDays = childSickDays;
    }

    public BigDecimal getChildSickDaysWithAub() {
        return childSickDaysWithAub;
    }

    public void setChildSickDaysWithAub(BigDecimal childSickDaysWithAub) {
        this.childSickDaysWithAub = childSickDaysWithAub;
    }

    public BigDecimal getSickDays() {
        return sickDays;
    }

    public void setSickDays(BigDecimal sickDays) {
        this.sickDays = sickDays;
    }

    public BigDecimal getSickDaysWithAub() {
        return sickDaysWithAub;
    }

    public void setSickDaysWithAub(BigDecimal sickDaysWithAub) {
        this.sickDaysWithAub = sickDaysWithAub;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
