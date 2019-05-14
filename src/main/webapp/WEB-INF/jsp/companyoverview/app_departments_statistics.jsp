<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>


<!DOCTYPE html>
<html>

<head>
    <uv:head/>
    <script defer type="text/javascript" src="<spring:url value='/assets/sick_notes.min.js' />"></script>
    <script defer type="text/javascript" src="<spring:url value='/assets/npm.tablesorter.min.js' />"></script>
</head>

<body>

<spring:url var="URL_PREFIX" value="/web/companyoverview"/>
<c:set var="linkPrefix" value="${URL_PREFIX}/statistics"/>

<uv:menu/>

<div class="print-info--only-landscape">
    <h4><spring:message code="print.info.landscape" /></h4>
</div>

<div class="content print--only-landscape">

    <div class="container">

        <div class="row">

            <div class="col-xs-12">

                <legend class="is-sticky">
                    <div class="legend-dropdown dropdown">
                        <a id="active-state" data-target="#" href="#" data-toggle="dropdown"
                           aria-haspopup="true" role="button" aria-expanded="false">
                           <spring:message code="applications.statistics" /><span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu" role="menu" aria-labelledby="active-state">
                            <li>
                                <a href="${URL_PREFIX}/sicknotes/statistics/departments?from=${filterFrom}&to=${filterTo}">
                                    <spring:message code="action.sicknotes.statistics" />
                                </a>
                            </li>
                        </ul>
                    </div>
                    <div class="legend-dropdown dropdown">
                        <a id="active-state" data-target="#" href="#" data-toggle="dropdown"
                           aria-haspopup="true" role="button" aria-expanded="false">
                            <spring:message code="departments.title" /><span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu" role="menu" aria-labelledby="active-state">
                            <li>
                                <a href="${linkPrefix}?from=${filterFrom}&to=${filterTo}">
                                    <spring:message code="persons.all" />
                                </a>
                            </li>

                            <li>
                                <a href="${linkPrefix}/departments?from=${filterFrom}&to=${filterTo}">
                                    <spring:message code="departments.all" />
                                </a>
                            </li>

                            <c:forEach items="${departments}" var="department">
                                <li>
                                    <a href='${linkPrefix}?department=${department.id}&from=${filterFrom}&to=${filterTo}'>
                                        <i class="fa fa-fw"></i>
                                        <c:out value="${department.name}" />
                                    </a>
                                </li>
                            </c:forEach>
                        </ul>
                    </div>
                    <uv:print/>
                </legend>

                <p class="is-inline-block">
                    <c:choose>
                        <c:when test="${not empty errors}">
                            <a href="#filterModal" data-toggle="modal">
                                <spring:message code="applications.statistics.error.period" />
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a href="#filterModal" data-toggle="modal">
                                <spring:message code="filter.period"/>:&nbsp;<uv:date date="${from}"/> - <uv:date date="${to}"/>
                            </a>
                        </c:otherwise>
                    </c:choose>
                </p>

                <uv:filter-modal id="filterModal" actionUrl="${linkPrefix}/departments"/>

                <c:choose>
                    <c:when test="${not empty errors}">
                        <div class="alert alert-danger">
                            <spring:message code='applications.statistics.error'/>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <table cellspacing="0" class="list-table sortable tablesorter">
                            <thead class="hidden-xs hidden-sm">
                            <tr>
                                <th class="sortable-field"><spring:message code="departments.title"/></th>
                                <th><%-- placeholder to ensure correct number of th --%></th>
                                <th class="sortable-field"><spring:message code="applications.statistics.allowed"/></th>
                                <th class="sortable-field"><spring:message code="applications.statistics.waiting"/></th>
                                <th class="sortable-field"><spring:message code="applications.statistics.left"/> (<c:out value="${from.year}" />)</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${statistics}" var="statistic">
                                <tr>
                                    <td class="hidden-xs"><c:out value="${statistic.department.name}"/></td>
                                    <td class="hidden-xs hidden-sm">
                                        <spring:message code="applications.statistics.total"/>:
                                        <c:forEach items="${vacationTypes}" var="type">
                                            <br/>
                                            <small><c:out value="${type.displayName}"/>:</small>
                                        </c:forEach>
                                    </td>
                                    <td class="hidden-xs hidden-sm number">
                                        <b class="sortable"><uv:number number="${statistic.totalAllowedVacationDays}"/></b>
                                        <spring:message code="duration.days"/>
                                        <c:forEach items="${vacationTypes}" var="type">
                                            <br/>
                                            <small>
                                                <uv:number number="${statistic.allowedVacationDays[type]}"/>
                                            </small>
                                        </c:forEach>
                                    </td>
                                    <td class="hidden-xs hidden-sm number">
                                        <b class="sortable"><uv:number number="${statistic.totalWaitingVacationDays}"/></b>
                                        <spring:message code="duration.days"/>
                                        <c:forEach items="${vacationTypes}" var="type">
                                            <br/>
                                            <small>
                                                <uv:number number="${statistic.waitingVacationDays[type]}"/>
                                            </small>
                                        </c:forEach>
                                    </td>
                                    <td class="hidden-xs">
                                        <b class="sortable"><uv:number number="${statistic.leftVacationDays}"/></b>
                                        <spring:message code="duration.vacationDays"/>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>

            </div>

        </div>
        <%-- end of row --%>

    </div>
    <%-- end of container --%>

</div>
<%-- end of content --%>

</body>

</html>
