/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

define(
		[ "simple-modeler/js/Utils",
				"simple-modeler/js/SimpleModelDefinitionService",
				"benchmark/js/BenchmarkService" ],
		function(Utils, ChecklistDefinitionService, BenchmarkService) {
			return {
				create : function() {
					var controller = new GanttChartViewController();

					return controller;
				}
			};

			function GanttChartViewController() {
				/**
				 * 
				 */
				GanttChartViewController.prototype.initialize = function() {
					this.queryParameters = Utils.getQueryParameters();

					console.log("Query Parameters");
					console.log(this.queryParameters);

					this.expandedCumulantsInStatusTable = {};

					this.benchmarks = [ {
						name : "Criticality Formula",
						zones : [ {
							name : "High",
							color : "#CA3013"
						}, {
							name : "Medium",
							color : "#FFE75D"
						}, {
							name : "Low",
							color : "#A6C9B2"
						} ]
					}, {
						name : "Standard Europe",
						zones : [ {
							name : "SLA Breach",
							color : "#CA3013"
						}, {
							name : "Delayed",
							color : "#7D7B9B"
						}, {
							name : "At Risk",
							color : "#FFE75D"
						}, {
							name : "On Time",
							color : "#A6C9B2"
						} ]
					} ];

					this.millisPerMinute = 60 * 1000;
					this.millisPerHour = 60 * 60 * 1000;
					this.processInstancesStack = [];

					var self = this;

					if (this.queryParameters.oid) {
						this.loadProcessInstance(this.queryParameters.oid);
					}

					return this;
				};

				/**
				 * TODO Will be removed
				 */
				GanttChartViewController.prototype.filterProcessDefinitions = function(
						models) {
					this.processDefinitions = [];

					for ( var modelId in models) {
						var model = models[modelId];

						for ( var processId in model.processes) {
							var process = model.processes[processId];

							if (process.name == model.name) {
								this.processDefinitions.push(process);
							}
						}
					}
				};

				/**
				 * 
				 */
				GanttChartViewController.prototype.loadProcessInstance = function(
						oid) {
					jQuery("body").css("cursor", "wait");

					var self = this;

					BenchmarkService
							.instance()
							.getProcessInstance(oid)
							.done(
									function(processInstance) {
										console.log("===> Process Instance");
										console.log(processInstance);

										self.processInstance = processInstance;
										self.flatActivityInstanceList = [];
										self.expandedActivityInstances = {};
										self
												.createFlatActivityInstanceListFromChecklists();
										self.safeApply();

										window.setTimeout(function() {
											self.refreshActivityInstances();
											self.safeApply();
											jQuery("body").css("cursor",
													"default");
										}, 2000);
									}).fail(function() {
								jQuery("body").css("cursor", "default");
							});
				};

				/**
				 * 
				 */
				GanttChartViewController.prototype.expandInStatusTable = function(
						cumulant) {
					this.expandedCumulantsInStatusTable[cumulant.fullId] = cumulant;
				};

				/**
				 * 
				 */
				GanttChartViewController.prototype.expandActivityInstance = function(
						activityInstance) {
					this.expandedActivityInstances["" + activityInstance.oid] = activityInstance;

					var self = this;

					window.setTimeout(function() {
						self.currentTimeDivision.css({
							top : jQuery("#ganttChartTable").position().top,
							height : jQuery("#ganttChartTable").height(),
							visibility : "visible"
						});
					}, 500);
				};

				/**
				 * 
				 */
				GanttChartViewController.prototype.collapseActivityInstance = function(
						activityInstance) {
					delete this.expandedActivityInstances[""
							+ activityInstance.oid];

					if (activityInstance.subProcessInstance) {
						for (var n = 0; n < activityInstance.subProcessInstance.activityInstances.length; ++n) {
							this
									.collapseActivityInstance(activityInstance.subProcessInstance.activityInstances[n]);
						}
					}

					var self = this;

					window.setTimeout(function() {
						self.currentTimeDivision.css({
							top : jQuery("#ganttChartTable").position().top,
							height : jQuery("#ganttChartTable").height(),
							visibility : "visible"
						});
					}, 500);
				};

				/**
				 * 
				 */
				GanttChartViewController.prototype.isExpandable = function(
						activityInstance) {
					return !this.expandedActivityInstances[''
							+ activityInstance.oid]
							&& activityInstance.subProcessInstance;
				};

				/**
				 * 
				 */
				GanttChartViewController.prototype.isCollapsable = function(
						activityInstance) {
					return this.expandedActivityInstances[''
							+ activityInstance.oid];
				};

				/**
				 * 
				 */
				GanttChartViewController.prototype.refreshActivityInstances = function() {
					this.now = moment().valueOf();
					this.start = this.now;
					this.end = this.now;

					// this.traversed = {};
					// this.activityInstancesMap = {};
					// this.startActivity = null;
					//
					// for (var n = 0; n < this.flatActivityInstanceList.length;
					// ++n) {
					// this.start
					// var activityInstance = this.flatActivityInstanceList[n];
					//
					// this.activityInstancesMap[activityInstance.oid] =
					// activityInstance;
					//
					// if (!this.startActivity || activityInstance.start <
					// this.startActivity) {
					// this.startActivity = activityInstance;
					// }
					// }

					// this.traverse(this.startActivity, this.start);

					// Calculate total interval

					for (var n = 0; n < this.flatActivityInstanceList.length; ++n) {
						if (!this.flatActivityInstanceList[n].assumedStart) {
							if (this.flatActivityInstanceList[n].start) {
								this.flatActivityInstanceList[n].assumedStart = this.flatActivityInstanceList[n].start;
							} else {
								this.flatActivityInstanceList[n].assumedStart = this.now;
							}
						}

						if (!this.flatActivityInstanceList[n].assumedEnd) {
							if (this.flatActivityInstanceList[n].state == 2
									|| this.flatActivityInstanceList[n].state == 4) {
								this.flatActivityInstanceList[n].assumedEnd = this.flatActivityInstanceList[n].lastModification;
							} else {
								this.flatActivityInstanceList[n].assumedEnd = this.now
										+ 35 * 60 * 60 * 1000;
								this.flatActivityInstanceList[n].requiredEnd = this.now
										- new Date().getTime()
										% 2
										* 60
										* 60
										* 1000;
							}
						}

						this.start = Math.min(this.start,
								this.flatActivityInstanceList[n].assumedStart);
						this.end = Math.max(this.start,
								this.flatActivityInstanceList[n].assumedEnd);
					}

					this.duration = this.end - this.start;

					console.log("Start:    "
							+ moment(this.start).format("M/D/YYYY h:mm a"));
					console.log("Now:      "
							+ moment(this.now).format("M/D/YYYY h:mm a"));
					console.log("End:      "
							+ moment(this.end).format("M/D/YYYY h:mm a"));
					console.log("Duration: " + this.duration
							/ this.millisPerHour);

					this.currentTimeDivision = null;

					var barCellWidth;

					for (var n = 0; n < this.flatActivityInstanceList.length; ++n) {
						var activityInstance = this.flatActivityInstanceList[n];
						var barDivision = jQuery("#" + activityInstance.oid);

						console.log(activityInstance.oid);
						console.log(activityInstance.state);
						console.log(barDivision.parent());

						barCell = jQuery(barDivision.parent());

						if (!this.currentTimeDivision) {
							this.currentTimeDivision = jQuery("#currentTimeDivision");

							barCellWidth = barCell.width();

							console.log("Bar Cell");
							console.log(barCell);
							console.log(barCellWidth);

							// this.currentTimeDivision.css(
							// {
							// left : barCell.offset().left
							// + (this.now - this.start)
							// / this.duration * barCellWidth,
							// top : jQuery("#ganttChartTable")
							// .position().top,
							// width : 0,
							// height : jQuery("#ganttChartTable")
							// .height(),
							// visibility : "visible"
							// }, 2000);
						}

						console.log("Assumed Start: "
								+ moment(activityInstance.assumedStart).format(
										"M/D/YYYY h:mm a"));
						console.log("Assumed End: "
								+ moment(activityInstance.assumedEnd).format(
										"M/D/YYYY h:mm a"));

						barDivision.css({
							left : (activityInstance.assumedStart - this.start)
									/ this.duration * barCellWidth,
							top : 0,
							visibility : "visible"
						});

						barDivision
								.css(
										{
											width : Math
													.ceil((activityInstance.assumedEnd - activityInstance.assumedStart)
															/ this.duration
															* barCellWidth)
										}, 2000);						

						// var barCompletionDivision = jQuery("<div
						// class='pendingState atRiskState
						// barDivision'></div>");
						//
						// barCell.append(barCompletionDivision);
						// barCompletionDivision.css("left", "" +
						// (activityInstance.start -
						// this.start) / this.duration *
						// barCell.width() + "px");
						// barCompletionDivision.css("width", "" +
						// (activityInstance.end
						// -
						// activityInstance.start) * 0.7 /
						// this.duration * barCell.width() + "px");
						// barCompletionDivision.css("height", "" +
						// activityRow.height() + "px");
					}

					this.calculateSuggestedTimeUnit();
					this.refreshTimeAxis();
				};

				/**
				 * 
				 */
				GanttChartViewController.prototype.calculateSuggestedTimeUnit = function() {
					if (this.duration / this.millisPerHour < 20) {
						this.timeUnit = "h";
					} else if (this.duration / this.millisPerHour < 100) {
						this.timeUnit = "d";
					} else if (this.duration / this.millisPerHour < 1000) {
						this.timeUnit = "w";
					} else {
						this.timeUnit = "M";
					}
				};

				/**
				 * 
				 */
				GanttChartViewController.prototype.refreshTimeAxis = function() {
					var units = null;

					if (this.timeUnit == 'h') {
						units = {
							startOffset : "hour",
							addUnit : "h",
							format : "HH:MM"
						};
					} else if (this.timeUnit == 'd') {
						units = {
							startOffset : "day",
							addUnit : "d",
							format : "M/D/YYYY h:mm a"
						};
					} else if (this.timeUnit == 'w') {
						units = {
							startOffset : "week",
							addUnit : "w",
							format : "M/D/YYYY"
						};
					} else if (this.timeUnit == 'M') {
						units = {
							startOffset : "month",
							addUnit : "M",
							format : "M/YYYY"
						};
					}

					jQuery("#timeAxisDivision").empty();

					var startTime = moment(this.start);
					var endTime = moment(this.end);
					var startTickTime = startTime.clone().startOf(
							units.startOffset); // Last hour
					// before
					var tickTime = startTickTime.clone();

					while (!tickTime.isAfter(endTime)) {
						var tick = jQuery("<table class='tickTable layoutTable'><tr><td colspan='2'>"
								+ tickTime.format(units.format)
								+ "</td></tr><tr><td class='tickCell' style='width: 50%;'></td><td style='width: 50%;'></td></tr></table>");

						jQuery("#timeAxisDivision").append(tick);

						tick.css({
							left : jQuery("#timeAxisDivision").position().left
									+ tickTime.diff(startTickTime)
									/ this.duration
									* jQuery("#timeAxisDivision").width(),
							top : jQuery("#timeAxisDivision").position().top
						});

						tickTime.add(units.addUnit, 1);
					}
				};

				/**
				 * 
				 */
				GanttChartViewController.prototype.createFlatActivityInstanceListFromChecklists = function() {
					var auxiliaryActivityInstance = {
						oid : "p" + this.processInstance.oid,
						start : this.processInstance.start,
						activity : {
							id : this.processInstance.processDefinition.id,
							name : this.processInstance.processDefinition.name,
							description : this.processInstance.processDefinition.description,
							descriptors : this.processInstance.descriptors,
							type : "Subprocess"
						},
						subProcessInstance : this.processInstance,
						depth : 0
					};

					this.flatActivityInstanceList
							.push(auxiliaryActivityInstance);
					this.createFlatActivityInstanceList(this.processInstance,
							auxiliaryActivityInstance, 1);

					console.log("Flat List");
					console.log(this.flatActivityInstanceList);
				};

				/**
				 * 
				 */
				GanttChartViewController.prototype.createFlatActivityInstanceList = function(
						processInstance, superActivityInstance, depth) {
					for (var n = 0; n < processInstance.activityInstances.length; ++n) {
						if (superActivityInstance) {
							processInstance.activityInstances[n].superActivityInstanceOid = superActivityInstance.oid;
						}

						processInstance.activityInstances[n].depth = depth;

						this.flatActivityInstanceList
								.push(processInstance.activityInstances[n]);

						if (processInstance.activityInstances[n].subProcessInstance) {
							this
									.createFlatActivityInstanceList(
											processInstance.activityInstances[n].subProcessInstance,
											processInstance.activityInstances[n],
											depth + 1);
						}
					}
				};

				/**
				 * 
				 */
				GanttChartViewController.prototype.traverse = function(
						activityInstance, start) {
					if (this.traversed[activityInstance.oid]) {
						if (activityInstance.start >= start) {
							return;
						} else {
							// Current branch took longer, recalculation needed

							activityInstance.start = null;
							activityInstance.end = null;
						}
					} else {
						// Convert benchmark properties for easier handling

						// TODO Fake

						if (expectedDuration = 0) {
							activityInstance.expectedDuration = 30 * this.millisPerMinute;
							activityInstance.expectedCompletion = moment()
									.valueOf();
						}

						// Mark traversed

						this.traversed[activityInstance.oid] = activityInstance.oid;
					}

					if (activityInstance.start == null) {
						// If potential start was in the past use current time
						activityInstance.start = Math.max(start, moment()
								.valueOf());
					}

					if (activityInstance.end == null) {
						activityInstance.end = activityInstance.start
								+ activityInstance.expectedDuration;
					}

					if (activityInstance.end > this.end) {
						this.end = activityInstance.end;
					}

					if (activityInstance.successors) {
						for (var n = 0; n < activityInstance.successors.length; ++n) {
							this
									.traverse(
											this.activityInstancesMap[activityInstance.successors[n]],
											activityInstance.end);
						}
					}
				};

				/**
				 * 
				 */
				GanttChartViewController.prototype.calculateDelayState = function(
						activityInstance) {
					if (activityInstance.requiredEnd < this.now) {
						return "delayedState";
					} else if (activityInstance.assumedEnd > activityInstance.requiredEnd) {
						return "atRiskState";
					}

					return "normalState";
				};

				/**
				 * 
				 */
				GanttChartViewController.prototype.getTestActivityInstances = function() {
					var stamp = moment().add("m", -200);

					console.log(stamp.format());

					return [
							{
								oid : 1,
								activity : {
									name : "Activity 1"
								},
								start : stamp.valueOf(),
								end : stamp.clone().add("m", 50).valueOf(),
								expectedCompletion : stamp.clone().add("m", 45)
										.valueOf(),
								successors : [ 2, 3 ]
							},
							{
								oid : 2,
								activity : {
									name : "Activity 2"
								},
								start : stamp.add("m", 60).valueOf(),
								end : stamp.clone().add("m", 60).valueOf(),
								expectedCompletion : stamp.clone()
										.add("m", 110).valueOf(),
								successors : [ 4 ]
							},
							{
								oid : 3,
								activity : {
									name : "Activity 3"
								},
								start : stamp.add("m", 60).valueOf(),
								end : stamp.clone().add("m", 200).valueOf(),
								expectedCompletion : stamp.clone()
										.add("m", 190).valueOf(),
								successors : [ 4 ]
							},
							{
								oid : 4,
								activity : {
									name : "Activity 4"
								},
								start : stamp.add("m", 80).valueOf(),
								expectedDuration : 60 * this.millisPerMinute,
								expectedCompletion : stamp.clone()
										.add("m", 320).valueOf(),
								successors : [ 5 ]
							},
							{
								oid : 5,
								activity : {
									name : "Activity 5"
								},
								expectedDuration : 200 * this.millisPerMinute,
								expectedCompletion : stamp.clone()
										.add("m", 400).valueOf(),
								successors : [ 6, 7 ]
							},
							{
								oid : 6,
								activity : {
									name : "Activity 6"
								},
								expectedDuration : 20 * this.millisPerMinute,
								expectedCompletion : stamp.clone()
										.add("m", 450).valueOf(),
								successors : [ 8 ]
							},
							{
								oid : 7,
								activity : {
									name : "Activity 7"
								},
								expectedDuration : 30 * this.millisPerMinute,
								expectedCompletion : stamp.clone()
										.add("m", 480).valueOf(),
								successors : [ 8 ]
							},
							{
								oid : 8,
								activity : {
									name : "Activity 8"
								},
								expectedDuration : 50 * this.millisPerMinute,
								expectedCompletion : stamp.clone()
										.add("h", 500).valueOf(),
								successors : []
							} ];
				};

				/**
				 * 
				 */
				GanttChartViewController.prototype.safeApply = function(fn) {
					var phase = this.$root.$$phase;

					if (phase == '$apply' || phase == '$digest') {
						if (fn && (typeof (fn) === 'function')) {
							fn();
						}
					} else {
						this.$apply(fn);
					}
				};

				/**
				 * 
				 */
				GanttChartViewController.prototype.activateActivityInstance = function(
						activityInstance) {
					var message = {
						"type" : "OpenView",
						"data" : {
							"viewId" : "activityPanel",
							"viewKey" : "OID=" + activityInstance.oid,
							"params" : {
								"oid" : "" + activityInstance.oid
							}
						}
					};

					parent.postMessage(JSON.stringify(message), "*");
				};

				/**
				 * 
				 */
				GanttChartViewController.prototype.getDescriptorValue = function(
						instance, descriptorName) {
					if (instance.descriptors) {
						for (var n = 0; n < instance.descriptors.length; ++n) {
							if (instance.descriptors[n].id == descriptorName) {
								return instance.descriptors[n].value;
							}
						}
					}

					return null;
				};

				/**
				 * 
				 */
				GanttChartViewController.prototype.formatTimeStamp = function(
						timeStamp) {
					return moment(timeStamp).format("M/D/YYYY h:mm a");

				};
			}
		});
