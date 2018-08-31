package org.synyx.urlaubsverwaltung.web.statistics;

import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.core.department.Department;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ApplicationForLeaveDepartmentStatistics {

    private final Department department;

    private BigDecimal leftVacationDays = BigDecimal.ZERO;

    private final Map<VacationType, BigDecimal> waitingVacationDays = new HashMap<>();
    private final Map<VacationType, BigDecimal> allowedVacationDays = new HashMap<>();

    public ApplicationForLeaveDepartmentStatistics (Department department, VacationTypeService vacationTypeService) {

        Assert.notNull(department, "Department must be given.");

        this.department = department;

        for (VacationType vacationType : vacationTypeService.getVacationTypes()) {
            waitingVacationDays.put(vacationType, BigDecimal.ZERO);
            allowedVacationDays.put(vacationType, BigDecimal.ZERO);
        }
    }

    public Department getDepartment() {

        return department;
    }



    public BigDecimal getTotalWaitingVacationDays() {

        BigDecimal total = BigDecimal.ZERO;

        for (BigDecimal days : getWaitingVacationDays().values()) {
            total = total.add(days);
        }

        return total;
    }


    public BigDecimal getTotalAllowedVacationDays() {

        BigDecimal total = BigDecimal.ZERO;

        for (BigDecimal days : getAllowedVacationDays().values()) {
            total = total.add(days);
        }

        return total;
    }


    public BigDecimal getLeftVacationDays() {

        return leftVacationDays;
    }


    public Map<VacationType, BigDecimal> getWaitingVacationDays() {

        return waitingVacationDays;
    }


    public Map<VacationType, BigDecimal> getAllowedVacationDays() {

        return allowedVacationDays;
    }


    public void setLeftVacationDays(BigDecimal leftVacationDays) {

        Assert.notNull(leftVacationDays, "Days must be given.");

        this.leftVacationDays = leftVacationDays;
    }


    public void addWaitingVacationDays(VacationType vacationType, BigDecimal waitingVacationDays) {

        Assert.notNull(vacationType, "Vacation type must be given.");
        Assert.notNull(waitingVacationDays, "Days must be given.");

        BigDecimal currentWaitingVacationDays = getWaitingVacationDays().get(vacationType);

        getWaitingVacationDays().put(vacationType, currentWaitingVacationDays.add(waitingVacationDays));
    }


    public void addAllowedVacationDays(VacationType vacationType, BigDecimal allowedVacationDays) {

        Assert.notNull(vacationType, "Vacation type must be given.");
        Assert.notNull(allowedVacationDays, "Days must be given.");

        BigDecimal currentAllowedVacationDays = getAllowedVacationDays().get(vacationType);

        getAllowedVacationDays().put(vacationType, currentAllowedVacationDays.add(allowedVacationDays));
    }
}
