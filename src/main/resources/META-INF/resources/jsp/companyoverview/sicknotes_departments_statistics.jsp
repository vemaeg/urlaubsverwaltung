<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>


<!DOCTYPE html>
<html>

<head>
    <uv:head/>
    <script type="text/javascript">
        $(document).ready(function() {

            $("table.sortable").tablesorter({
                sortList: [[0, 0]],
                headers: {
                    1: {sorter: false},
                    3: {sorter: 'commaNumber'}
                },
            });

        });
    </script>
</head>

<body>

<spring:url var="URL_PREFIX" value="/web/companyoverview"/>
<c:set var="linkPrefix" value="${URL_PREFIX}/sicknotes/statistics"/>

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
                           <spring:message code="action.sicknotes.statistics" /><span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu" role="menu" aria-labelledby="active-state">
                            <li>
                                <a href="${URL_PREFIX}/statistics/departments?from=${filterFrom}&to=${filterTo}">
                                    <spring:message code="applications.statistics" />
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
                                <th class="sortable-field"><spring:message code="sicknotes.daysOverview.sickDays.title"/></th>
                                <th class="sortable-field"><spring:message code="sicknotes.daysOverview.sickDays.child.title"/></th>
                                <th class="sortable-field"><spring:message code="sicknotes.statistics.averageSickTime"/></th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${departments}" var="department">
                                <tr>
                                    <td class="hidden-xs"><c:out value="${department.name}"/></td>
                                    <td class="hidden-xs">
                                        <i class="fa fa-medkit hidden-print"></i>
                                        <uv:number number="${departmentSickDays[department].days['TOTAL']}"/>
                                        <spring:message code="sicknotes.daysOverview.sickDays.number"/>
                                        <c:if test="${departmentSickDays[department].days['WITH_AUB'] > 0}">
                                            <p class="list-table--second-row">
                                            <i class="fa fa-check positive"></i> <spring:message
                                                code="overview.sicknotes.sickdays.aub" arguments="${departmentSickDays[department].days['WITH_AUB']}"/>
                                            </p>
                                        </c:if>
                                    </td>
                                    <td class="hidden-xs">
                                        <i class="fa fa-child hidden-print"></i>
                                        <uv:number number="${departmentChildSickDays[department].days['TOTAL']}"/>
                                        <spring:message code="sicknotes.daysOverview.sickDays.child.number"/>
                                        <c:if test="${departmentChildSickDays[department].days['WITH_AUB'] > 0}">
                                            <p class="list-table--second-row">
                                                <i class="fa fa-check positive"></i> <spring:message
                                                    code="overview.sicknotes.sickdays.aub"
                                                    arguments="${departmentChildSickDays[department].days['WITH_AUB']}"/>
                                            </p>
                                        </c:if>
                                    </td>
                                    <td class"hidden-xs">
                                        <uv:number number="${averageSickDays[department]}"/>
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