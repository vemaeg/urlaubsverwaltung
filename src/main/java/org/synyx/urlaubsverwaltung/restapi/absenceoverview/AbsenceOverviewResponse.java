package org.synyx.urlaubsverwaltung.restapi.absenceoverview;

import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceOverview;

public class AbsenceOverviewResponse {

    private AbsenceOverview absenceOverview;

    public AbsenceOverviewResponse(AbsenceOverview absenceOverview) {
        this.absenceOverview = absenceOverview;
    }

    public AbsenceOverview getAbsenceOverview() {
        return absenceOverview;
    }

    public void setAbsenceOverview(AbsenceOverview absenceOverview) {
        this.absenceOverview = absenceOverview;
    }
}
