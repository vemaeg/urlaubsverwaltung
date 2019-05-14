package org.synyx.urlaubsverwaltung.restapi.absenceoverview;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceOverview;
import org.synyx.urlaubsverwaltung.api.ResponseWrapper;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;

@Api(value = "AbsenceOverview", description = "Get Absence-Overview Metadata")
@RestController("restApiAbsenceOverview")
@RequestMapping("/api")
public class AbsenceOverviewController {

    private AbsenceOverviewService absenceOverviewService;

    @Autowired
    AbsenceOverviewController(AbsenceOverviewService absenceOverviewService) {
        this.absenceOverviewService = absenceOverviewService;
    }

    @ApiOperation(
            value = "Get Absence-Overview Metadata",
            notes = "Get Absence-Overview metadata for a person")
    @RequestMapping(value = "/absenceoverview", method = RequestMethod.GET)
    public ResponseWrapper<AbsenceOverviewResponse> getAbsenceOverview(
            @RequestParam("selectedPerson") Integer selectedPersonId,
            @RequestParam("selectedYear") Integer selectedYear) throws UnknownPersonException {

        AbsenceOverview absenceOverview =
                absenceOverviewService.getAbsenceOverview(selectedPersonId, selectedYear);

        return new ResponseWrapper<>(new AbsenceOverviewResponse(absenceOverview));
    }
}
