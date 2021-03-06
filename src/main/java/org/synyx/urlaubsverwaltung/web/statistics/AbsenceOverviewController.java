package org.synyx.urlaubsverwaltung.web.statistics;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.security.SecurityRules;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/web")
@Controller
public class AbsenceOverviewController {
    @Autowired
    private PersonService personService;

    @Autowired
    private DepartmentService departmentService;

    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @PostMapping("/absenceoverview")
    public String absenceOverview() {

        return "redirect:/web/absenceoverview";
    }

    @PreAuthorize(SecurityRules.IS_PRIVILEGED_USER)
    @GetMapping("/absenceoverview")
    public String absenceOverview(Model model,
                                  @RequestParam(value = "person", required = false) Integer personId) {
        Person signedInUser = personService.getSignedInUser();

        preparePersons(signedInUser, model);

        model.addAttribute("selectedPerson", personId);
        model.addAttribute("currentYear", LocalDate.now().getYear());
        return "overview/absence_overview";
    }

    private void preparePersons(Person person, Model model) {
        if (person.hasRole(Role.BOSS) || person.hasRole(Role.OFFICE)) {
            model.addAttribute("persons", personService.getActivePersons());
        } else if (person.hasRole(Role.SECOND_STAGE_AUTHORITY)) {
            List<Department> departments = departmentService.getManagedDepartmentsOfSecondStageAuthority(person);
            List<Person> persons = new ArrayList<>();
            for (Department department : departments) {
                persons.addAll(department.getMembers());
            }
            model.addAttribute("persons", persons);
        } else if (person.hasRole(Role.DEPARTMENT_HEAD)) {
            List<Department> departments = departmentService.getManagedDepartmentsOfDepartmentHead(person);
            List<Person> persons = new ArrayList<>();
            for (Department department : departments) {
                persons.addAll(department.getMembers());
            }
            model.addAttribute("persons", persons);
        }
    }
}
