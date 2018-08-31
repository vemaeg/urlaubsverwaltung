package org.synyx.urlaubsverwaltung.web.statistics;

import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.*;
import org.synyx.urlaubsverwaltung.core.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.core.department.Department;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.springframework.ui.Model;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.web.department.UnknownDepartmentException;

import javax.security.sasl.AuthenticationException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/web/companyoverview")
public class CompanyOverviewController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private PersonService personService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private VacationTypeService vacationTypeService;

    @Autowired
    private ApplicationForLeaveStatisticsBuilder applicationForLeaveStatisticsBuilder;

    @Autowired
    private ApplicationForLeaveDepartmentStatisticsBuilder applicationForLeaveDepartmentStatisticsBuilder;

    @InitBinder
    public void initBinder(DataBinder binder) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor());
    }

    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @RequestMapping(value = "/statistics", method = RequestMethod.POST)
    public String companyApplicationForLeaveStatistics(@ModelAttribute("period") FilterPeriod period,
                                                       @RequestParam(value = ControllerConstants.DEPARTMENT_ATTRIBUTE, required = false) Integer requestedDepartmentId) {

        String departmentParameter = (requestedDepartmentId != null) ? "&" + ControllerConstants.DEPARTMENT_ATTRIBUTE + "="
                + requestedDepartmentId : "";

        return "redirect:/web/companyoverview/statistics?from=" + period.getStartDateAsString() + "&to="
                + period.getEndDateAsString() + departmentParameter;
    }

    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @RequestMapping(value = "/statistics", method = RequestMethod.GET)
    public String companyApplicationForLeaveStatistics(@RequestParam(value = "from", required = false) String from,
                                                       @RequestParam(value = "to", required = false) String to,
                                                       @RequestParam(value = ControllerConstants.DEPARTMENT_ATTRIBUTE, required = false) Optional<Integer> requestedDepartmentId,
                                                       Model model) throws UnknownDepartmentException {

        FilterPeriod period = new FilterPeriod(Optional.ofNullable(from), Optional.ofNullable(to));

        DateMidnight fromDate = period.getStartDate();
        DateMidnight toDate = period.getEndDate();

        List<Person> persons;

        // NOTE: Not supported at the moment
        if (fromDate.getYear() != toDate.getYear()) {
            model.addAttribute("period", period);
            model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, "INVALID_PERIOD");

            return "companyoverview/app_statistics";
        }

        if (requestedDepartmentId.isPresent()) {
            Department department = departmentService.getDepartmentById(requestedDepartmentId.get()).orElseThrow(() ->
                    new UnknownDepartmentException(requestedDepartmentId.get()));
            persons = getRelevantPersons(department);
            model.addAttribute("selectedDepartment", department);
        } else {
            persons = getRelevantPersons();
        }

        List<ApplicationForLeaveStatistics> statistics = persons.stream().map(person ->
                applicationForLeaveStatisticsBuilder.build(person, fromDate, toDate)).collect(Collectors.toList());

        List<Department> departments = getRelevantDepartments();

        model.addAttribute("departments", departments);
        model.addAttribute("from", fromDate);
        model.addAttribute("to", toDate);
        model.addAttribute("filterFrom", period.getStartDateAsString());
        model.addAttribute("filterTo", period.getEndDateAsString());
        model.addAttribute("statistics", statistics);
        model.addAttribute("period", period);
        model.addAttribute("vacationTypes", vacationTypeService.getVacationTypes());

        return "companyoverview/app_statistics";
    }

    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/statistics/departments", method = RequestMethod.POST)
    public String departmentApplicationForLeaveStatistics(@ModelAttribute("period") FilterPeriod period) {

        return "redirect:/web/companyoverview/statistics/departments?from=" + period.getStartDateAsString() + "&to="
                + period.getEndDateAsString();
    }

    @PreAuthorize(SecurityRules.IS_OFFICE)
    @RequestMapping(value = "/statistics/departments", method = RequestMethod.GET)
    public String departmentApplicationForLeaveStatistics(@RequestParam(value = "from", required = false) String from,
                                                          @RequestParam(value = "to", required = false) String to,
                                                          Model model) {

        FilterPeriod period = new FilterPeriod(Optional.ofNullable(from), Optional.ofNullable(to));

        DateMidnight fromDate = period.getStartDate();
        DateMidnight toDate = period.getEndDate();

        // NOTE: Not supported at the moment
        if (fromDate.getYear() != toDate.getYear()) {
            model.addAttribute("period", period);
            model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, "INVALID_PERIOD");

            return "companyoverview/app_departments_statistics";
        }

        List<Department> departments = departmentService.getAllDepartments();
        List<ApplicationForLeaveDepartmentStatistics> statistics = departments.stream().map(department ->
                applicationForLeaveDepartmentStatisticsBuilder.build(department, fromDate, toDate)).collect(Collectors.toList());

        model.addAttribute("from", fromDate);
        model.addAttribute("to", toDate);
        model.addAttribute("filterFrom", period.getStartDateAsString());
        model.addAttribute("filterTo", period.getEndDateAsString());
        model.addAttribute("statistics", statistics);
        model.addAttribute("period", period);
        model.addAttribute("departments", departments);
        model.addAttribute("vacationTypes", vacationTypeService.getVacationTypes());

        return "companyoverview/app_departments_statistics";
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
