package org.synyx.urlaubsverwaltung.calendar.web;

import com.google.gson.Gson;

import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.util.StringUtils;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.synyx.urlaubsverwaltung.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.calendar.GoogleCalendarService;
import org.synyx.urlaubsverwaltung.calendar.JollydayCalendar;
import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.io.IOException;

import java.math.BigDecimal;

import java.util.List;


/**
 * Controller for calendar relevant stuff.
 *
 * @author  Aljona Murygina
 */
@Controller
public class CalendarController {

    private static final String JSP_FOLDER = "calendar/";
    private GoogleCalendarService googleCalendarService;
    private OwnCalendarService ownCalendarService;
    private JollydayCalendar jollydayCalendar;
    private PersonService personService;

    public CalendarController(GoogleCalendarService googleCalendarService, OwnCalendarService ownCalendarService,
        JollydayCalendar jollydayCalendar, PersonService personService) {

        this.googleCalendarService = googleCalendarService;
        this.ownCalendarService = ownCalendarService;
        this.jollydayCalendar = jollydayCalendar;
        this.personService = personService;
    }

    /**
     * Is used in application form: ajax call to calculate vacation days for dates given by datepicker.
     *
     * @param  start  start date as String (e.g. 2013-3-21)
     * @param  end  end date as String (e.g. 2013-3-21)
     * @param  length  day length as String (FULL, MORNING or NOON)
     * @param  personId  id of the person to calculate used days for
     *
     * @return  number of days as String for the given parameters or "N/A" if parameters are not valid in any way
     */
    @RequestMapping(value = "/calendar/vacation", method = RequestMethod.GET)
    @ResponseBody
    public String getNumberOfDays(@RequestParam("start") String start,
        @RequestParam("end") String end,
        @RequestParam("length") String length,
        @RequestParam("person") Integer personId) {

        if (StringUtils.hasText(start) && StringUtils.hasText(end) && StringUtils.hasText(length)) {
            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd"); // please do not change, because is used in custom.js
            DateMidnight startDate = DateMidnight.parse(start, fmt);
            DateMidnight endDate = DateMidnight.parse(end, fmt);

            if (startDate.isBefore(endDate) || startDate.isEqual(endDate)) {
                DayLength howLong = DayLength.valueOf(length);
                Person person = personService.getPersonByID(personId);
                BigDecimal days = ownCalendarService.getWorkDays(howLong, startDate, endDate, person);

                return days.toString();
            }
        }

        return "N/A";
    }


    /**
     * Is used in jquery datepicker to mark public holidays: ajax call to check if date is a public holiday.
     *
     * @param  month
     * @param  year
     *
     * @return  "1" if date is a public holiday, "0" if not, "N/A" if parameter not valid
     */
    @RequestMapping(value = "/calendar/public-holiday", method = RequestMethod.GET)
    @ResponseBody
    public String getPublicHolidays(@RequestParam("year") String year,
        @RequestParam("month") String month) {

        if (StringUtils.hasText(year) && StringUtils.hasText(month)) {
            try {
                List<String> holidays = jollydayCalendar.getPublicHolidays(Integer.parseInt(year),
                        Integer.parseInt(month));

                String json = new Gson().toJson(holidays);

                return json;
            } catch (NumberFormatException ex) {
                return "N/A";
            }
        }

        return "N/A";
    }


    /**
     * TODO: google calendar stuff in development Following methods are only for developing...
     */

    @RequestMapping(value = "/calendar", method = RequestMethod.GET)
    public String getCalendarSite(Model model) {

        return JSP_FOLDER + "calendar";
    }


    @RequestMapping(value = "/calendar/setup", method = RequestMethod.GET)
    public String setupGoogleCalendar(Model model) throws IOException {

        googleCalendarService.setUp();

        return JSP_FOLDER + "setup";
    }


    @RequestMapping(value = "/calendar/event", method = RequestMethod.GET)
    public String addEvent(Model model) throws IOException {

        googleCalendarService.addEvent();

        return JSP_FOLDER + "event";
    }
}
