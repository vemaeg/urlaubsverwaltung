<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<sec:authorize access="hasAuthority('USER')">
    <c:set var="IS_USER" value="${true}"/>
</sec:authorize>

<sec:authorize access="hasAuthority('BOSS')">
  <c:set var="IS_BOSS" value="${true}"/>
</sec:authorize>

<sec:authorize access="hasAuthority('DEPARTMENT_HEAD')">
  <c:set var="IS_DEPARTMENT_HEAD" value="${true}"/>
</sec:authorize>

<sec:authorize access="hasAuthority('SECOND_STAGE_AUTHORITY')">
  <c:set var="IS_SECOND_STAGE_AUTHORITY" value="${true}"/>
</sec:authorize>

<sec:authorize access="hasAuthority('OFFICE')">
    <c:set var="IS_OFFICE" value="${true}"/>
</sec:authorize>

<c:set var="CAN_CANCEL" value="${IS_BOSS || IS_DEPARTMENT_HEAD || IS_SECOND_STAGE_AUTHORITY}"/>

<c:if test="${application.status == 'WAITING' || application.status == 'ALLOWED' || application.status == 'TEMPORARY_ALLOWED' }">

    <c:if test="${application.status == 'WAITING'}">
        <sec:authorize access="hasAuthority('USER')">
            <jsp:include page="actions/remind_form.jsp"/>
        </sec:authorize>
        <sec:authorize access="hasAnyAuthority('DEPARTMENT_HEAD', 'SECOND_STAGE_AUTHORITY', 'BOSS')">
            <jsp:include page="actions/allow_form.jsp"/>
            <jsp:include page="actions/reject_form.jsp"/>
            <jsp:include page="actions/refer_form.jsp"/>
        </sec:authorize>
        <sec:authorize access="hasAuthority('USER')">
            <jsp:include page="actions/cancel_form.jsp"/>
        </sec:authorize>
    </c:if>

    <c:if test="${application.status == 'TEMPORARY_ALLOWED'}">
        <sec:authorize access="hasAnyAuthority('DEPARTMENT_HEAD', 'SECOND_STAGE_AUTHORITY', 'BOSS')">
            <jsp:include page="actions/allow_form.jsp"/>
            <jsp:include page="actions/reject_form.jsp"/>
            <jsp:include page="actions/refer_form.jsp"/>
        </sec:authorize>
    </c:if>

    <c:if test="${application.status == 'ALLOWED' || application.status == 'TEMPORARY_ALLOWED'}">
        <c:if test="${CAN_CANCEL || (IS_USER && application.person.id == signedInUser.id)}">
            <jsp:include page="actions/cancel_form.jsp"/>
        </c:if>
    </c:if>
</c:if>
