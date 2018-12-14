<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags"%>

<sec:authorize access="hasAuthority('BOSS')">
	<c:set var="IS_BOSS" value="${true}" />
</sec:authorize>
<sec:authorize access="hasAuthority('DEPARTMENT_HEAD')">
	<c:set var="IS_DEPARTMENT_HEAD" value="${true}" />
</sec:authorize>
<sec:authorize access="hasAuthority('OFFICE')">
	<c:set var="IS_OFFICE" value="${true}" />
</sec:authorize>
<c:set var="IS_ALLOWED" value="${IS_BOSS || IS_DEPARTMENT_HEAD || IS_OFFICE }" />
<html>

<head>
<uv:head />
<%@include file="include/absence_overview_js.jsp"%>
</head>

<body style="-webkit-print-color-adjust:exact;">
	<spring:url var="URL_PREFIX" value="/web" />

	<sec:authorize access="hasAuthority('OFFICE')">
		<c:set var="IS_OFFICE" value="true" />
	</sec:authorize>

	<uv:menu />

    <div class="print-info--only-landscape">
        <h4><spring:message code="print.info.landscape" /></h4>
    </div>

	<div class="content print--only-landscape">
		<div id="absenceOverview-Container" class="container">

			<c:if test="${IS_ALLOWED}">
				<div class="row">
					<div class="col-xs-12">
						<legend id="vacation">
							<spring:message code="overview.vacationOverview.title" />
                            <uv:print/>
						</legend>
					</div>
				</div>

                <div class="col-md-8">
                    <div class="form-group">
                    <div class="row">
                        <label class="control-label col-md-3" for="yearSelect">
								Jahr:
                        </label>
						<div class="col-md-6">
							<select id="yearSelect" name="yearSelect" size="1" path="" class="form-control">
								<c:forEach var="i" begin="1" end="9">
									<option value="${currentYear - 10 + i}">
										<c:out value="${currentYear - 10 + i}" />
									</option>
								</c:forEach>
								<option value="${currentYear}" selected="${currentYear}">
									<c:out value="${currentYear}" />
								</option>
								<option value="${currentYear +1}">
									<c:out value="${currentYear +1}" />
								</option>
							</select>
						</div>
						</div>
					</div>
                    <div class="form-group">
                    <div class="row">
                        <label class="control-label col-md-3" for="personSelect">
								Mitarbeiter:
                        </label>
                        <div class="col-md-6">
							<select id="personSelect" name="personSelect" size="1" path="" class="form-control">
								<c:forEach items="${persons}" var="person">
								    <c:choose>
								    <c:when test="${person.id == selectedPerson}">
								        <option value="${person.id}" selected>
                                        	<c:out value="${person.niceName}" />
                                        </option>
                                    </c:when>
                                    <c:otherwise>
									    <option value="${person.id}">
										    <c:out value="${person.niceName}" />
									    </option>
									</c:otherwise>
									</c:choose>
								</c:forEach>
							</select>
						</div>
					</div>
					</div>
				</div>

				 <div class="row">
           			<div class="col-xs-12">
                		<hr/>
						<div id="vacationOverview"></div>
					</div>
				</div>

                <div id="vacationOverviewLegend" class="row">
					<label class="col-md-1">
                         <spring:message code="overview.vacationOverview.legendTitle" />
                     </label>
                     <div class="col-md-3">
                         <table>
                             <tr>
                                 <td class='vacationOverview-legend-colorbox vacationOverview-day-weekend'></td>
                                 <td class='vacationOverview-legend-text'><spring:message code="overview.vacationOverview.weekend" /></td>
                             </tr>
                             <tr>
                                 <td class='vacationOverview-legend-colorbox vacationOverview-day-personal-holiday-status-ALLOWED'></td>
                                 <td class='vacationOverview-legend-text'><spring:message code="overview.vacationOverview.allowed" /></td>
                             </tr>
                             <tr>
                                 <td class='vacationOverview-legend-colorbox vacationOverview-day-personal-other-holiday-status-ALLOWED'></td>
                                 <td class='vacationOverview-legend-text'><spring:message code="overview.vacationOverview.other.allowed" /></td>
                             </tr>
                             <tr>
                                 <td class='vacationOverview-legend-colorbox vacationOverview-day-personal-holiday-status-WAITING'></td>
                                 <td class='vacationOverview-legend-text'><spring:message code="overview.vacationOverview.vacation" /></td>
                             </tr>
                             <tr>
                                 <td class='vacationOverview-legend-colorbox vacationOverview-day-sick-note'></td>
                                 <td class='vacationOverview-legend-text'><spring:message code="overview.vacationOverview.sick" /></td>
                             </tr>
                         </table>
                     </div>
                </div>

			</c:if>
		</div>

	</div>

</body>