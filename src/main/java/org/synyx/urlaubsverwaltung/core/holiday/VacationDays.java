package org.synyx.urlaubsverwaltung.core.holiday;

import java.math.BigDecimal;

public class VacationDays {
    private BigDecimal waitingVacationDays;
    private BigDecimal allowedVacationDays;

    public VacationDays(BigDecimal waitingVacationDays, BigDecimal allowedVacationDays) {
        this.waitingVacationDays = waitingVacationDays;
        this.allowedVacationDays = allowedVacationDays;
    }

    public BigDecimal getWaitingVacationDays() {
        return waitingVacationDays;
    }

    public void setWaitingVacationDays(BigDecimal waitingVacationDays) {
        this.waitingVacationDays = waitingVacationDays;
    }

    public BigDecimal getAllowedVacationDays() {
        return allowedVacationDays;
    }

    public void setAllowedVacationDays(BigDecimal allowedVacationDays) {
        this.allowedVacationDays = allowedVacationDays;
    }
}
