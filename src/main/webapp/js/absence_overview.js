	//https://tc39.github.io/ecma262/#sec-array.prototype.find
	if (!Array.prototype.find) {
	  Object.defineProperty(Array.prototype, 'find', {
	    value: function(predicate) {
	     // 1. Let O be ? ToObject(this value).
	      if (this == null) {
	        throw new TypeError('"this" is null or not defined');
	      }

	      var o = Object(this);

	      // 2. Let len be ? ToLength(? Get(O, "length")).
	      var len = o.length >>> 0;

	      // 3. If IsCallable(predicate) is false, throw a TypeError exception.
	      if (typeof predicate !== 'function') {
	        throw new TypeError('predicate must be a function');
	      }

	      // 4. If thisArg was supplied, let T be thisArg; else let T be undefined.
	      var thisArg = arguments[1];

	      // 5. Let k be 0.
	      var k = 0;

	      // 6. Repeat, while k < len
	      while (k < len) {
	        // a. Let Pk be ! ToString(k).
	        // b. Let kValue be ? Get(O, Pk).
	        // c. Let testResult be ToBoolean(? Call(predicate, T, � kValue, k, O �)).
	        // d. If testResult is true, return kValue.
	        var kValue = o[k];
	        if (predicate.call(thisArg, kValue, k, o)) {
	          return kValue;
	        }
	        // e. Increase k by 1.
	        k++;
	      }

	      // 7. Return undefined.
	      return undefined;
	    }
	  });
	}

	$(function() {

		function selectedItemChange() {
			var selectedYear = document.getElementById('yearSelect');
			var selectedPerson = document
					.getElementById('personSelect');
			var selectedPersonValue = selectedPerson.value;
			var selectedYearValue = selectedYear.options[selectedYear.selectedIndex].value;

			if (selectedYearValue != null && selectedPersonValue != null) {
				var url = location.protocol + "//" + location.host
						+ "/api/absenceoverview?selectedYear="
						+ selectedYearValue + "&selectedPerson="
						+ selectedPersonValue;

				var xhttp = new XMLHttpRequest();
				xhttp.open("GET", url, false);
				xhttp.setRequestHeader("Content-type", "application/json");
				xhttp.send();
				var holyDayOverviewResponse = JSON.parse(xhttp.responseText);

				var url = location.protocol + "//" + location.host
                						+ "/api/vacations/length?year="
                						+ selectedYearValue + "&person="
                						+ selectedPersonValue;

				var xhttp = new XMLHttpRequest();
                				xhttp.open("GET", url, false);
                				xhttp.setRequestHeader("Content-type", "application/json");
                				xhttp.send();

                var vacationLengthResponse = JSON.parse(xhttp.responseText);
                var totalVacationDays = 0;

				if (holyDayOverviewResponse != null
						&& holyDayOverviewResponse != undefined
						&& holyDayOverviewResponse.response != null
						&& holyDayOverviewResponse.response != undefined) {

					var months = holyDayOverviewResponse.response.absenceOverview.months;
					var personId = holyDayOverviewResponse.response.absenceOverview.personID;
					var previousYear = parseInt(selectedYearValue) - 1;
					var thisYearTotalAllowedDays = parseFloat(vacationLengthResponse.response.previousYearRemainingDays) + parseFloat(vacationLengthResponse.response.vacationDaysAllowed);

					var outputTable = "<table id ='absenceOverviewTable' cellspacing='0' class='list-table sortable tablesorter absenceOverview-table'>";
                                                    	outputTable += "<tr><th class='absenceOverview-title'>Tag/Monat</th>";
                                                    	for(i = 1; i <= 31; i++) {
                                                    	            outputTable += "<th class='vacationOverview-day-item'>"
                                                    									+ i + "</th>";
                                                    					}
                                                    	outputTable += "<th class = 'absenceOverview-title'>Resturlaub " + previousYear + "</th>";
                                                    	outputTable += "<th class = 'absenceOverview-value'>" + vacationLengthResponse.response.previousYearRemainingDays + "</th>";
                                                    	outputTable += "<th class = 'absenceOverview-title'>Kranktage</th>";
                                                    	outputTable += "<th class = 'absenceOverview-title'>Kranktage mit AUB</th>";
                                                    	outputTable += "<th class = 'absenceOverview-title'>Kind krank</th>";
                                                    	outputTable += "</tr><tbody class='vacationOverview-tbody'>";

                    outputTable += "<tr>"
                    for (i = 0; i <= 31; i++) {
                        outputTable += "<td></td>";
                    }

                    outputTable += "<td class = 'absenceOverview-label'>Urlaub " + selectedYearValue + "</td>";
                    outputTable += "<td>" + vacationLengthResponse.response.vacationDaysAllowed + "</td><td></td><td></td><td></td></tr>"

                    outputTable += "<tr>"
                    for (i = 0; i <= 31; i++) {
                        outputTable += "<td></td>";
                    }

                    outputTable += "<td class = 'absenceOverview-label'>Gesamt</td>";
                    outputTable += "<td>" + thisYearTotalAllowedDays + "</td><td></td><td></td><td></td></tr>"

					for (var month in months) {
					            outputTable += "<tr><td>" + month + "</td>"
					            var days = months[month];
								var url = location.protocol + "//"
										+ location.host + "/api/absences?year="
										+ selectedYearValue + "&month="
										+ month + "&person="
										+ personId;
								var xhttp = new XMLHttpRequest();
								xhttp.open("GET", url, false);
								xhttp.setRequestHeader("Content-type",
										"application/json");
								xhttp.send();
								var response = JSON.parse(xhttp.responseText);
								if (response != null && response != undefined) {
                                    var monthlyVacationDays = 0;

                                    var url = location.protocol + "//" + location.host
                                    						+ "/api/sickdays?year="
                                    						+ selectedYearValue + "&month=" + month + "&person="
                                    						+ selectedPersonValue;

                                    var xhttp = new XMLHttpRequest();
                                    xhttp.open("GET", url, false);
                                    xhttp.setRequestHeader("Content-type",
                                    		"application/json");
                                    xhttp.send();

                                    var sickDaysResponse = JSON.parse(xhttp.responseText);
                                    var monthlySickDays = sickDaysResponse.response.sickDays;
                                    var monthlySickDaysWithAub = sickDaysResponse.response.sickDaysWithAub;
                                    var monthlyChildSickDays = sickDaysResponse.response.childSickDays;

									days
											.forEach(
													function(currentDay, index,
															array) {
														if (response.response.absences
																.find(
																		function(
																				currentValue,
																				index,
																				array) {
																			if (this.toString() == currentValue.date
																					&& currentValue.status === "WAITING"
																					&& currentValue.type === "VACATION"
																					&& currentValue.dayLength === 1) {
																				return true;
																			}
																		},
																		currentDay.dayText)) {
															currentDay.cssClass = 'vacationOverview-day-personal-holiday-status-WAITING vacationOverview-day-item ';
														}
														if (response.response.absences
																.find(
																		function(
																				currentValue,
																				index,
																				array) {
																			if (this.toString() == currentValue.date
																					&& currentValue.status === "WAITING"
																					&& currentValue.type === "VACATION"
																					&& currentValue.dayLength < 1) {
																				return true;
																			}
																		},
																		currentDay.dayText)) {
															currentDay.cssClass = 'vacationOverview-day-personal-holiday-status-WAITING vacationOverview-day-item ';
															currentDay.html = '1/2';
														}

														if (response.response.absences
																.find(
																		function(
																				currentValue,
																				index,
																				array) {
																			if (this.toString() == currentValue.date
																					&& currentValue.status === "ALLOWED"
																					&& currentValue.dayLength < 1
																					&& currentValue.type === "VACATION") {
																				monthlyVacationDays += 0.5;
																				return true;
																			}
																		},
																		currentDay.dayText)) {
															currentDay.cssClass = ' vacationOverview-day-personal-holiday-status-ALLOWED vacationOverview-day-item ';
															currentDay.html = '1/2';
														}
														if (response.response.absences
																.find(
																		function(
																				currentValue,
																				index,
																				array) {
																			if (this.toString() == currentValue.date
																					&& currentValue.status === "ALLOWED"
																					&& currentValue.dayLength === 1
																					&& currentValue.type === "VACATION") {
																				monthlyVacationDays += 1;
																				return true;
																			}
																		},
																		currentDay.dayText)) {
															currentDay.cssClass = ' vacationOverview-day-personal-holiday-status-ALLOWED vacationOverview-day-item ';
														}
														if (response.response.absences
                                                        		.find(
                                                        				function(
                                                        						currentValue,
                                                        						index,
                                                        						array) {
                                                        					if (this.toString() == currentValue.date
                                                        							&& currentValue.status === "ALLOWED"
                                                        							&& currentValue.dayLength === 1
                                                        							&& currentValue.type === "OTHER_VACATION") {
                                                        						return true;
                                                        					}
                                                        				},
                                                        				currentDay.dayText)) {
                                                        	currentDay.cssClass = ' vacationOverview-day-personal-other-holiday-status-ALLOWED vacationOverview-day-item ';
                                                        }

                                                        if (response.response.absences
                                                        		.find(
                                                        				function(
                                                        						currentValue,
                                                        						index,
                                                        						array) {
                                                        					if (this.toString() == currentValue.date
                                                        							&& currentValue.status === "ALLOWED"
                                                        							&& currentValue.dayLength < 1
                                                        							&& currentValue.type === "OTHER_VACATION") {
                                                        						return true;
                                                        					}
                                                        				},
                                                        				currentDay.dayText)) {
                                                        	currentDay.cssClass = ' vacationOverview-day-personal-other-holiday-status-ALLOWED vacationOverview-day-item ';
                                                        	currentDay.html = '1/2';
                                                        }
														if (response.response.absences
																.find(
																		function(
																				currentValue,
																				index,
																				array) {
																			if (this.toString() == currentValue.date
																					&& currentValue.type === 'SICK_NOTE'
																					&& currentValue.dayLength === 1) {
																				return true;
																			}
																		},
																		currentDay.dayText)) {
															currentDay.cssClass = ' vacationOverview-day-sick-note vacationOverview-day-item ';
														}
														if (response.response.absences
																.find(
																		function(
																				currentValue,
																				index,
																				array) {
																			if (this.toString() == currentValue.date
																					&& currentValue.type === 'SICK_NOTE'
																					&& currentValue.dayLength < 1) {
																				return true;
																			}
																		},
																		currentDay.dayText)) {
															currentDay.cssClass = ' vacationOverview-day-sick-note vacationOverview-day-item ';
															currentDay.html = '1/2';
														}
													}, this);
									days
                                            .forEach(
                                            		function(dayItem,
                                            				dayIndex,
                                            				dayArray) {
                                            			if (dayItem.typeOfDay === null) {
                                            			    dayItem.cssClass = ' vacationOverview-day-not-in-month vacationOverview-day-item';
                                            			}

                                            			else if (dayItem.typeOfDay === "WEEKEND") {
                                            				dayItem.cssClass = ' vacationOverview-day-weekend vacationOverview-day-item';
                                            			} else {
                                            				if (!dayItem.cssClass)
                                            				{
                                            					dayItem.cssClass = ' vacationOverview-day vacationOverview-day-item ';
                                            				};
                                            			};
                                            			if (dayItem.html === undefined) {
                                            			    dayItem.html = '';
                                            			}
                                            			outputTable += "<td class='" + dayItem.cssClass + "'>" + dayItem.html + "</td>";
                                            		}, outputTable);
                                    totalVacationDays += vacationLengthResponse.response.vacationDays[month].allowedVacationDays;
                                    outputTable += "<td></td>";
                                    outputTable += "<td>" + vacationLengthResponse.response.vacationDays[month].allowedVacationDays + "</td>";
                                    outputTable += "<td>" + monthlySickDays + "</td>";
                                    outputTable += "<td>" + monthlySickDaysWithAub + "</td>";
                                    outputTable += "<td>" + monthlyChildSickDays + "</td>";

								}
							};

/*					overViewList
							.forEach(
									function(item, index, array) {
										outputTable += "<tr><td>"
												+ item.person.niceName
												+ "</td>";
										item.days
												.forEach(
														function(dayItem,
																dayIndex,
																dayArray) {
															if (dayItem.typeOfDay === "WEEKEND") {
																dayItem.cssClass = ' vacationOverview-day-weekend vacationOverview-day-item';
															} else {
																if (!dayItem.cssClass)
																{
																	dayItem.cssClass = ' vacationOverview-day vacationOverview-day-item ';
																};
															};
															outputTable += "<td class='" + dayItem.cssClass + "'></td>";
														}, outputTable);
										outputTable += "</tr>";
									}, outputTable); */

                    outputTable += "<tr><td></td>"
                    for(i = 1; i <= 31; i++) {
                        outputTable += "<td></td>";
                    }

                    var url = location.protocol + "//" + location.host
                    						+ "/api/sickdays?year="
                    						+ selectedYearValue + "&person="
                    						+ selectedPersonValue;

                    var xhttp = new XMLHttpRequest();
                    xhttp.open("GET", url, false);
                    xhttp.setRequestHeader("Content-type",
                    		"application/json");
                    xhttp.send();

                    var sickDaysResponse = JSON.parse(xhttp.responseText);

                    outputTable += "<td></td>";
                    outputTable += "<td>" + totalVacationDays + "</td>";
                    outputTable += "<td>" + sickDaysResponse.response.sickDays + "</td>";
                    outputTable += "<td>" + sickDaysResponse.response.sickDaysWithAub + "</td>";
                    outputTable += "<td>" + sickDaysResponse.response.childSickDays + "</td></tr>";



					outputTable += "<tr><td></td>"
                    for(i = 1; i <= 31; i++) {
                        outputTable += "<td></td>";
                    }

                    var remainingDays = thisYearTotalAllowedDays - totalVacationDays;
                    outputTable += "<td class = 'absenceOverview-label'>Rest " + selectedYearValue + "</td>";
                    outputTable += "<td class = 'absenceOverview-value'>" + remainingDays + "</td>";
                    outputTable += "<td></td><td></td><td></td>";
                    outputTable += "</tbody></table>";

                    var element = document.getElementById("vacationOverview");
                    element.innerHTML = outputTable;
				}
			}
		}
		var selectedYear = document.getElementById('yearSelect');
		var selectedPerson = document.getElementById('personSelect');
		selectedYear.addEventListener("change", function() {
			selectedItemChange();
		});
		selectedPerson.addEventListener("change", function() {
			selectedItemChange();
		});
		if (typeof(Event) === "'function") {
			var event = new Event("change");
	    } else {
	        var event = document.createEvent("Event");
	        event.initEvent("change", true, true);
	    }
		selectedYear.dispatchEvent(event);
	}

	);
