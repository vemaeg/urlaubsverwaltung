package org.synyx.urlaubsverwaltung.web.person;

import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.security.SecurityRules;
import org.synyx.urlaubsverwaltung.web.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor;
import org.synyx.urlaubsverwaltung.web.department.DepartmentConstants;

import java.math.BigDecimal;
import java.util.Locale;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Controller
@RequestMapping("/web")
public class PersonManagementController {

    private final PersonService personService;
    private final DepartmentService departmentService;
    private final PersonValidator validator;

    @Autowired
    public PersonManagementController(PersonService personService, DepartmentService departmentService, PersonValidator validator) {
        this.personService = personService;
        this.departmentService = departmentService;
        this.validator = validator;
    }

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor());
        binder.registerCustomEditor(BigDecimal.class, new DecimalNumberPropertyEditor(locale));
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping("/staff/new")
    public String newPersonForm(Model model) {

        model.addAttribute(PersonConstants.PERSON_ATTRIBUTE, new Person());

        return PersonConstants.PERSON_FORM_JSP;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @PostMapping("/staff")
    public String newPerson(@ModelAttribute(PersonConstants.PERSON_ATTRIBUTE) Person person,
                            Errors errors,
                            RedirectAttributes redirectAttributes) {

        validator.validate(person, errors);

        if (errors.hasErrors()) {
            return PersonConstants.PERSON_FORM_JSP;
        }

        Person createdPerson = personService.create(person);

        redirectAttributes.addFlashAttribute("createSuccess", true);

        return "redirect:/web/staff/" + createdPerson.getId();
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @GetMapping("/staff/{personId}/edit")
    public String editPersonForm(@PathVariable("personId") Integer personId,
                                 Model model)
        throws UnknownPersonException {

        Person person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));

        model.addAttribute(PersonConstants.PERSON_ATTRIBUTE, person);
        model.addAttribute(DepartmentConstants.DEPARTMENTS_ATTRIBUTE,
                                departmentService.getManagedDepartmentsOfDepartmentHead(person));
        model.addAttribute(DepartmentConstants.SECOND_STAGE_DEPARTMENTS_ATTRIBUTE,
                departmentService.getManagedDepartmentsOfSecondStageAuthority(person));

        return PersonConstants.PERSON_FORM_JSP;
    }


    @PreAuthorize(SecurityRules.IS_OFFICE)
    @PostMapping("/staff/{personId}/edit")
    public String editPerson(@PathVariable("personId") Integer personId,
        @ModelAttribute(PersonConstants.PERSON_ATTRIBUTE) Person person, Errors errors,
        RedirectAttributes redirectAttributes) {

        validator.validate(person, errors);

        if (errors.hasErrors()) {
            return PersonConstants.PERSON_FORM_JSP;
        }

        personService.update(person);

        redirectAttributes.addFlashAttribute("updateSuccess", true);

        return "redirect:/web/staff/" + personId;
    }
}
