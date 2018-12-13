package org.synyx.urlaubsverwaltung.restapi.sickdays;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteCategory;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkDaysService;
import org.synyx.urlaubsverwaltung.restapi.ResponseWrapper;
import org.synyx.urlaubsverwaltung.restapi.RestApiDateFormat;
import org.synyx.urlaubsverwaltung.web.person.UnknownPersonException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Api(value = "Sick Days", description = "Get all sick days for a certain period and person.")
@RestController("restApiSickDaysController")
@RequestMapping("/api")
public class SickDaysController {

    private final SickNoteService sickNoteService;
    private final PersonService personService;
    private final WorkDaysService calendarService;

    @Autowired
    SickDaysController(SickNoteService sickNoteService, PersonService personService, WorkDaysService calendarService) {

        this.sickNoteService = sickNoteService;
        this.personService = personService;
        this.calendarService = calendarService;
    }

    @ApiOperation(
            value = "Get all sick days for a certain period and person", notes = "Get all sick days for a certain period and person. "
    )
    @RequestMapping(value = "/sickdays", method = RequestMethod.GET)
    public ResponseWrapper<SickDaysResponse> sickDays(
            @ApiParam(value = "Year", defaultValue = "2016")
            @RequestParam(value = "year")
                    Integer year,
            @ApiParam(value = "Month")
            @RequestParam(value = "month", required = false)
                    Integer month,
            @ApiParam(value = "ID of the person")
            @RequestParam(value = "person")
                    Integer personId) throws UnknownPersonException {

        DateMidnight startDate;
        DateMidnight endDate;

        if (month != null) {
            startDate = DateUtil.getFirstDayOfMonth(year, month);
            endDate = DateUtil.getLastDayOfMonth(year, month);
        } else {
            startDate = DateUtil.getFirstDayOfYear(year);
            endDate = DateUtil.getLastDayOfYear(year);
        }

        BigDecimal childSickDays = BigDecimal.ZERO;
        BigDecimal childSickDaysWithAub = BigDecimal.ZERO;
        BigDecimal sickDays = BigDecimal.ZERO;
        BigDecimal sickDaysWithAub = BigDecimal.ZERO;

        Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));

        List<SickNote> sickNotes = sickNoteService.getByPersonAndPeriod(person, startDate, endDate);

        for(SickNote sickNote: sickNotes) {
            DateMidnight relevantAubStartDate;
            DateMidnight relevantAubEndDate;

            DateMidnight relevantStartDate = sickNote.getStartDate();
            DateMidnight relevantEndDate = sickNote.getEndDate();

            if (sickNote.getStartDate().isBefore(startDate)) {
                relevantStartDate = startDate;
            }
            if (sickNote.getEndDate().isAfter(endDate)) {
                relevantEndDate = endDate;
            }

            BigDecimal workDays = calendarService.getWorkDays(sickNote.getDayLength(), relevantStartDate,
                    relevantEndDate, person);

            if (sickNote.getSickNoteType().isOfCategory(SickNoteCategory.SICK_NOTE_CHILD)) {
                childSickDays = childSickDays.add(workDays);

                if (sickNote.isAubPresent()) {
                     relevantAubStartDate = sickNote.getAubStartDate();
                     relevantAubEndDate = sickNote.getAubEndDate();

                    if (sickNote.getAubStartDate().isBefore(startDate)) {
                        relevantAubStartDate = startDate;
                    }

                    if (sickNote.getAubEndDate().isAfter(endDate)) {
                        relevantAubEndDate = endDate;
                    }
                    BigDecimal workDaysWithAUB = calendarService.getWorkDays(sickNote.getDayLength(),
                            relevantAubStartDate, relevantAubEndDate, person);

                    childSickDaysWithAub = childSickDaysWithAub.add(workDaysWithAUB);
                }
            } else {
                sickDays = sickDays.add(workDays);

                if (sickNote.isAubPresent()) {
                    relevantAubStartDate = sickNote.getAubStartDate();
                    relevantAubEndDate = sickNote.getAubEndDate();

                    if (sickNote.getAubStartDate().isBefore(startDate)) {
                        relevantAubStartDate = startDate;
                    }

                    if (sickNote.getAubEndDate().isAfter(endDate)) {
                        relevantAubEndDate = endDate;
                    }

                    BigDecimal workDaysWithAUB = calendarService.getWorkDays(sickNote.getDayLength(),
                            relevantAubStartDate, relevantAubEndDate, person);

                    sickDaysWithAub = sickDaysWithAub.add(workDaysWithAUB);
                }
            }
        }

        return new ResponseWrapper<>(new SickDaysResponse(childSickDays, childSickDaysWithAub, sickDays, sickDaysWithAub, person));
    }
}