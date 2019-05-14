package org.synyx.urlaubsverwaltung.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_DEPARTMENTS;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;


/**
 * Provides functionality to get the correct mail recipients for different use cases.
 */
@Service
class RecipientService {

    private final PersonService personService;
    private final DepartmentService departmentService;

    @Autowired
    RecipientService(PersonService personService, DepartmentService departmentService) {

        this.personService = personService;
        this.departmentService = departmentService;
    }

    /**
     * Get all persons with the given notification type.
     *
     * @param notification to get all persons for
     * @return list of recipients with the given notification type
     */
    List<Person> getRecipientsWithNotificationType(MailNotification notification) {

        return personService.getPersonsWithNotificationType(notification);
    }

    /**
     * Get all second stage authorities that must be notified about the given temporary allowed application.
     *
     * @param application that has been allowed temporary
     * @return list of recipients for the given temporary allowed application
     */
    List<Person> getRecipientsForTemporaryAllow(Application application) {
        return getResponsibleSecondStageAuthorities(application.getPerson());
    }


    /**
     * Depending on application issuer role the recipients for allow/remind mail are generated.
     *
     * <p>USER -> DEPARTMENT_HEAD DEPARTMENT_HEAD -> SECOND_STAGE_AUTHORITY, BOSS SECOND_STAGE_AUTHORITY -> BOSS</p>
     *
     * @param application to find out recipients for
     * @return list of recipients for the given application allow/remind request
     */
    List<Person> getRecipientsForAllowAndRemind(Application application) {

        /*
         * NOTE:
         *
         * It's not possible that someone has both roles,
         * {@link Role.BOSS} and ({@link Role.DEPARTMENT_HEAD} or {@link Role.SECOND_STAGE_AUTHORITY})
         *
         * Thus no need to use a {@link java.util.Set} to avoid person duplicates within the returned list.
         */

        Person applicationPerson = application.getPerson();

        List<Person> bosses = personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_ALL);

        List<Person> relevantBosses =
            personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_DEPARTMENTS).stream()
                .filter(bossesForDepartmentOf(applicationPerson))
                .collect(toList());

        if (applicationPerson.hasRole(SECOND_STAGE_AUTHORITY)) {
            return concat(bosses, relevantBosses);
        }

        if (applicationPerson.hasRole(DEPARTMENT_HEAD)) {
            List<Person> secondStageAuthorities = getResponsibleSecondStageAuthorities(applicationPerson);
            List<Person> responsibleDepartmentHeads = getResponsibleDepartmentHeads(applicationPerson);
            return concat(bosses, relevantBosses, secondStageAuthorities, responsibleDepartmentHeads);
        }

        //boss and user
        List<Person> responsibleDepartmentHeads = getResponsibleDepartmentHeads(applicationPerson);
        return concat(bosses, relevantBosses, responsibleDepartmentHeads);
    }

    /**
     * Get recipients for the cancelation request of a given application.
     *
     * @param  application  to find out recipients for
     *
     * @return  list of recipients for the given application allow/remind request
     */
    List<Person> getRecipientsForCancelationRequest(Application application) {

        Person applicationPerson = application.getPerson();

        List<Person> secondStageAuthorities = getResponsibleSecondStageAuthorities(applicationPerson);
        List<Person> responsibleDepartmentHeads = getResponsibleDepartmentHeads(applicationPerson);
        List<Person> office = getRecipientsWithNotificationType(MailNotification.NOTIFICATION_OFFICE);

        return concat(office, secondStageAuthorities, responsibleDepartmentHeads);
    }

    private Predicate<Person> bossesForDepartmentOf(Person applicationPerson) {
        return boss ->
            departmentService.getAssignedDepartmentsOfMember(applicationPerson).stream()
                .anyMatch(depOfAssignedMember -> departmentService.getAssignedDepartmentsOfMember(boss).contains(depOfAssignedMember));
    }

    private static List<Person> concat(List<Person> list1, List<Person> list2) {
        return Stream.concat(list1.stream(), list2.stream()).collect(toList());
    }

    private static List<Person> concat(List<Person> list1, List<Person> list2, List<Person> list3) {
        return concat(concat(list1, list2), list3);
    }

    private static List<Person> concat(List<Person> list1, List<Person> list2, List<Person> list3, List<Person> list4) {
        return concat(concat(list1, list2), concat(list3, list4));
    }

    private List<Person> getResponsibleSecondStageAuthorities(Person applicationPerson) {
        Predicate<Person> responsibleSecondStageAuthority = secondStageAuthority ->
            departmentService.isSecondStageAuthorityOfPerson(secondStageAuthority, applicationPerson);

        return personService.getPersonsWithNotificationType(NOTIFICATION_SECOND_STAGE_AUTHORITY)
            .stream()
            .filter(responsibleSecondStageAuthority)
            .filter(without(applicationPerson))
            .collect(toList());
    }

    private static Predicate<Person> without(Person applicationPerson) {
        return person -> !person.equals(applicationPerson);
    }

    private List<Person> getResponsibleDepartmentHeads(Person applicationPerson) {
        Predicate<Person> responsibleDepartmentHeads = departmentHead ->
            departmentService.isDepartmentHeadOfPerson(departmentHead, applicationPerson);

        return personService.getPersonsWithNotificationType(NOTIFICATION_DEPARTMENT_HEAD)
            .stream()
            .filter(responsibleDepartmentHeads)
            .filter(without(applicationPerson))
            .collect(toList());
    }

}
