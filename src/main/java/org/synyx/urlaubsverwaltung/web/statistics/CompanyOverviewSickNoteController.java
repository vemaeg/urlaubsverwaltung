package org.synyx.urlaubsverwaltung.web.statistics;

import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.*;
import org.synyx.urlaubsverwaltung.core.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.core.department.Department;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteCategory;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.core.sync.CalendarService;
import org.synyx.urlaubsverwaltung.core.workingtime.WorkDaysService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.web.department.UnknownDepartmentException;
import org.synyx.urlaubsverwaltung.web.person.PersonConstants;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/web/companyoverview/sicknotes")
public class CompanyOverviewSickNoteController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private PersonService personService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private SickNoteService sickNoteService;

    @Autowired
    private WorkDaysService calendarService;


    @InitBinder
    public void initBinder(DataBinder binder) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor());
    }

    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @RequestMapping(value = "/statistics", method = RequestMethod.POST)
    public String companySickNotesStatistics(@ModelAttribute("period") FilterPeriod period,
                                             @RequestParam(value = ControllerConstants.DEPARTMENT_ATTRIBUTE, required = false) Integer requestedDepartmentId) {

        String departmentParameter = (requestedDepartmentId != null) ? "&" + ControllerConstants.DEPARTMENT_ATTRIBUTE + "="
                + requestedDepartmentId : "";

        return "redirect:/web/companyoverview/sicknotes/statistics?from=" + period.getStartDateAsString() + "&to="
                + period.getEndDateAsString() + departmentParameter;
    }

    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @RequestMapping(value = "/statistics", method = RequestMethod.GET)
    public String companySickNotesStatistics(@RequestParam(value = "from", required = false) String from,
                                             @RequestParam(value = "to", required = false) String to,
                                             @RequestParam(value = ControllerConstants.DEPARTMENT_ATTRIBUTE, required = false) Optional<Integer> requestedDepartmentId,
                                             Model model) throws UnknownDepartmentException {

        FilterPeriod period = new FilterPeriod(Optional.ofNullable(from), Optional.ofNullable(to));

        List<SickNote> sickNotes = sickNoteService.getByPeriod(period.getStartDate(), period.getEndDate());

        List<Person> persons;

        if (requestedDepartmentId.isPresent()) {
            Department department = departmentService.getDepartmentById(requestedDepartmentId.get()).orElseThrow(() ->
                    new UnknownDepartmentException(requestedDepartmentId.get()));
            persons = getRelevantPersons(department);
            model.addAttribute("selectedDepartment", department);
        } else {
            persons = getRelevantPersons();
        }

        List<SickNote> sickNotesOfPersons = sickNotes.stream().filter(sickNote ->
                persons.contains(sickNote.getPerson()) && sickNote.isActive()).collect(Collectors.toList());

        Map<Person, SickDays> sickDays = new HashMap<>();
        Map<Person, SickDays> childSickDays = new HashMap<>();

        for (Person person : persons) {
            sickDays.put(person, new SickDays());
            childSickDays.put(person, new SickDays());
        }

        for (SickNote sickNote : sickNotesOfPersons) {
            Person person = sickNote.getPerson();
            BigDecimal workDays = calendarService.getWorkDays(sickNote.getDayLength(), sickNote.getStartDate(),
                    sickNote.getEndDate(), person);

            if (sickNote.getSickNoteType().isOfCategory(SickNoteCategory.SICK_NOTE_CHILD)) {
                childSickDays.get(person).addDays(SickDays.SickDayType.TOTAL, workDays);

                if (sickNote.isAubPresent()) {
                    BigDecimal workDaysWithAUB = calendarService.getWorkDays(sickNote.getDayLength(),
                            sickNote.getAubStartDate(), sickNote.getAubEndDate(), person);

                    childSickDays.get(person).addDays(SickDays.SickDayType.WITH_AUB, workDaysWithAUB);
                }
            } else {
                sickDays.get(person).addDays(SickDays.SickDayType.TOTAL, workDays);

                if (sickNote.isAubPresent()) {
                    BigDecimal workDaysWithAUB = calendarService.getWorkDays(sickNote.getDayLength(),
                            sickNote.getAubStartDate(), sickNote.getAubEndDate(), person);

                    sickDays.get(person).addDays(SickDays.SickDayType.WITH_AUB, workDaysWithAUB);
                }
            }
        }

        List<Department> departments = getRelevantDepartments();

        model.addAttribute("departments", departments);
        model.addAttribute("sickDays", sickDays);
        model.addAttribute("childSickDays", childSickDays);
        model.addAttribute("today", DateMidnight.now());
        model.addAttribute("from", period.getStartDate());
        model.addAttribute("to", period.getEndDate());
        model.addAttribute("filterFrom", period.getStartDateAsString());
        model.addAttribute("filterTo", period.getEndDateAsString());
        model.addAttribute("period", period);


        model.addAttribute(PersonConstants.PERSONS_ATTRIBUTE, persons);

        return "companyoverview/sicknotes_statistics";
    }

    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/statistics/departments", method = RequestMethod.POST)
    public String departmentSickNotesStatistics(@ModelAttribute("period") FilterPeriod period) {

        return "redirect:/web/companyoverview/statistics/departments?from=" + period.getStartDateAsString() + "&to="
                + period.getEndDateAsString();
    }

    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/statistics/departments", method = RequestMethod.GET)
    public String departmentSickNotesStatistics(@RequestParam(value = "from", required = false) String from,
                                                @RequestParam(value = "to", required = false) String to,
                                                Model model) {

        FilterPeriod period = new FilterPeriod(Optional.ofNullable(from), Optional.ofNullable(to));
        List<Department> departments = departmentService.getAllDepartments();

        Map<Department, SickDays> departmentSickDays = new HashMap<>();
        Map<Department, SickDays> departmentChildSickDays = new HashMap<>();
        Map<Department, BigDecimal> averageSickDays = new HashMap<>();

        for (Department department: departments) {
            departmentSickDays.put(department, new SickDays());
            departmentChildSickDays.put(department, new SickDays());
        }

        for (Department department : departments) {
            BigDecimal totalSickDays = BigDecimal.ZERO;
            for (Person person : department.getMembers()) {
                List<SickNote> sickNotes = sickNoteService.getByPersonAndPeriod(person,  period.getStartDate(), period.getEndDate());

                for (SickNote sickNote : sickNotes) {
                    BigDecimal workDays = calendarService.getWorkDays(sickNote.getDayLength(), sickNote.getStartDate(),
                            sickNote.getEndDate(), person);
                    totalSickDays = totalSickDays.add(workDays);

                    if (sickNote.getSickNoteType().isOfCategory(SickNoteCategory.SICK_NOTE_CHILD)) {
                        departmentChildSickDays.get(department).addDays(SickDays.SickDayType.TOTAL, workDays);

                        if (sickNote.isAubPresent()) {
                            BigDecimal workDaysWithAUB = calendarService.getWorkDays(sickNote.getDayLength(),
                                    sickNote.getAubStartDate(), sickNote.getAubEndDate(), person);

                            departmentChildSickDays.get(department).addDays(SickDays.SickDayType.WITH_AUB, workDaysWithAUB);
                        }
                    } else {
                        departmentSickDays.get(department).addDays(SickDays.SickDayType.TOTAL, workDays);

                        if (sickNote.isAubPresent()) {
                            BigDecimal workDaysWithAUB = calendarService.getWorkDays(sickNote.getDayLength(),
                                    sickNote.getAubStartDate(), sickNote.getAubEndDate(), person);

                            departmentSickDays.get(department).addDays(SickDays.SickDayType.WITH_AUB, workDaysWithAUB);
                        }
                    }
                }
            }
            averageSickDays.put(department, totalSickDays.divide(new BigDecimal(department.getMembers().size()), 2, BigDecimal.ROUND_HALF_UP));
        }

        model.addAttribute("departments", departments);
        model.addAttribute("departmentSickDays", departmentSickDays);
        model.addAttribute("departmentChildSickDays", departmentChildSickDays);
        model.addAttribute("today", DateMidnight.now());
        model.addAttribute("from", period.getStartDate());
        model.addAttribute("to", period.getEndDate());
        model.addAttribute("filterFrom", period.getStartDateAsString());
        model.addAttribute("filterTo", period.getEndDateAsString());
        model.addAttribute("period", period);
        model.addAttribute("averageSickDays", averageSickDays);

        return "companyoverview/sicknotes_departments_statistics";
    }

    private List<Person> getRelevantPersons() {

        Person signedInUser = sessionService.getSignedInUser();

        if (signedInUser.hasRole(Role.DEPARTMENT_HEAD)) {
            return departmentService.getManagedMembersOfDepartmentHead(signedInUser);
        }

        return personService.getActivePersons();
    }

    private List<Person> getRelevantPersons(Department department) {

        Person signedInUser = sessionService.getSignedInUser();

        if (signedInUser.hasRole(Role.OFFICE) || department.isPersonDepartmentHead(signedInUser) || department.isPersonSecondStageAuthority(signedInUser)) {
            return department.getMembers();
        }
        return Collections.emptyList();
    }

    private List<Department> getRelevantDepartments() {

        Person signedInUser = sessionService.getSignedInUser();

        if (signedInUser.hasRole(Role.BOSS) || signedInUser.hasRole(Role.OFFICE)) {
            return departmentService.getAllDepartments();
        }

        if (signedInUser.hasRole(Role.DEPARTMENT_HEAD)) {
            return departmentService.getManagedDepartmentsOfDepartmentHead(signedInUser);
        }

        if (signedInUser.hasRole(Role.SECOND_STAGE_AUTHORITY)) {
            return departmentService.getManagedDepartmentsOfSecondStageAuthority(signedInUser);
        }

        return departmentService.getAssignedDepartmentsOfMember(signedInUser);
    }
}
