
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>



<!DOCTYPE html>
<html>

<head>
    <uv:head />
    <script type="text/javascript">
        $(document).ready(function() {

            $("table.sortable").tablesorter({
                sortList: [[1,0]]
            });

        });

    </script>
</head>

<body>

<spring:url var="URL_PREFIX" value="/web/companyoverview" />
<c:set var="linkPrefix" value="${URL_PREFIX}/sicknotes"/>

<uv:menu />

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
                           <spring:message code="action.sicknotes.statistics" /><span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu" role="menu" aria-labelledby="active-state">
                           <li>
                                <a href="${URL_PREFIX}/statistics?from=${filterFrom}&to=${filterTo}">
                                    <spring:message code="applications.statistics" />
                                </a>
                           </li>
                        </ul>
                    </div>
                    <div class="legend-dropdown dropdown">
                        <a id="active-state" data-target="#" href="#" data-toggle="dropdown"
                           aria-haspopup="true" role="button" aria-expanded="false">
                           <c:choose>
                                <c:when test="${param.department != null}">
                                    <c:out value="${selectedDepartment.name}" /><span class="caret"></span>
                                </c:when>
                                <c:otherwise>
                                    <spring:message code="persons.all" /><span class="caret"></span>
                                </c:otherwise>
                           </c:choose>
                        </a>
                        <ul class="dropdown-menu" role="menu" aria-labelledby="active-state">
                            <li>
                                <a href="${linkPrefix}/statistics?from=${filterFrom}&to=${filterTo}">
                                    <spring:message code="persons.all" />
                                </a>
                            </li>

                            <sec:authorize access="hasAuthority('OFFICE')">
                                <li>
                                    <a href="${linkPrefix}/statistics/departments?from=${filterFrom}&to=${filterTo}">
                                        <spring:message code="departments.all" />
                                    </a>
                                </li>
                            </sec:authorize>

                            <c:if test="${departments.size() > 0}">
                                <c:forEach items="${departments}" var="department">
                                    <li>
                                        <a href='${linkPrefix}/statistics?department=${department.id}&from=${filterFrom}&to=${filterTo}'>
                                            <i class="fa fa-fw"></i>
                                            <c:out value="${department.name}" />
                                        </a>
                                    </li>
                                </c:forEach>
                            </c:if>
                        </ul>
                    </div>
                    <uv:print/>
                </legend>

                <c:choose>
                    <c:when test="${param.department != null}">
                        <uv:filter-modal id="filterModal" actionUrl="${linkPrefix}/statistics?department=${selectedDepartment.id}"/>
                    </c:when>
                    <c:otherwise>
                        <uv:filter-modal id="filterModal" actionUrl="${linkPrefix}/statistics"/>
                    </c:otherwise>
                </c:choose>

                <p class="is-inline-block">
                    <a href="#filterModal" data-toggle="modal">
                        <spring:message code="filter.period"/>:&nbsp;<uv:date date="${from}"/> - <uv:date date="${to}"/>
                    </a>
                </p>
                <p class="pull-right visible-print">
                    <spring:message code="filter.validity"/> <uv:date date="${today}" />
                </p>

                <table class="list-table selectable-table sortable tablesorter" cellspacing="0">
                    <thead class="hidden-xs hidden-sm">
                    <tr>
                        <th class="sortable-field"><spring:message code="person.data.firstName"/></th>
                        <th class="sortable-field"><spring:message code="person.data.lastName"/></th>
                        <th class="sortable-field"><spring:message code="sicknotes.daysOverview.sickDays.title"/></th>
                        <th class="sortable-field"><spring:message code="sicknotes.daysOverview.sickDays.child.title"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${persons}" var="person">
                    <tr>
                        <td class="hidden-xs">
                            <c:out value="${person.firstName}"/>
                        </td>
                        <td class="hidden-xs">
                            <c:out value="${person.lastName}"/>
                        </td>
                        <td class="hidden-xs">
                            <i class="fa fa-medkit hidden-print"></i>
                            <uv:number number="${sickDays[person].days['TOTAL']}"/>
                            <spring:message code="sicknotes.daysOverview.sickDays.number"/>
                            <c:if test="${sickDays[person].days['WITH_AUB'] > 0}">
                                <p class="list-table--second-row">
                                    <i class="fa fa-check positive"></i> <spring:message
                                        code="overview.sicknotes.sickdays.aub" arguments="${sickDays[person].days['WITH_AUB']}"/>
                                </p>
                            </c:if>
                        </td>
                        <td class="hidden-xs">
                            <i class="fa fa-child hidden-print"></i>
                            <uv:number number="${childSickDays[person].days['TOTAL']}"/>
                            <spring:message code="sicknotes.daysOverview.sickDays.child.number"/>
                            <c:if test="${childSickDays[person].days['WITH_AUB'] > 0}">
                                <p class="list-table--second-row">
                                    <i class="fa fa-check positive"></i> <spring:message
                                            code="overview.sicknotes.sickdays.aub"
                                            arguments="${childSickDays[person].days['WITH_AUB']}"/>
                                </p>
                            </c:if>
                        </td>
                        <td class="visible-xs">
                            <i class="fa fa-medkit hidden-print"></i> <uv:number number="${sickDays[person].days['TOTAL']}"/>
                            <i class="fa fa-child hidden-print"></i> <uv:number number="${childSickDays[person].days['TOTAL']}"/>
                        </td>
                        </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

</body>

</html>
