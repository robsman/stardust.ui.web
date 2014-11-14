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

					this.millisPerMinute = 60 * 1000;
					this.millisPerHour = 60 * 60 * 1000;
					this.processInstancesStack = [];

					var self = this;

					var self = this;

					BenchmarkService
							.instance()
							.getBenchmark("XYZ")
							.done(
									function(benchmark) {
										self.benchmark = benchmark;

										if (self.queryParameters.oid) {
											self
													.loadProcessInstance(self.queryParameters.oid);
										}

										self.safeApply();
									}).fail(function() {
								// TODO Error Messages
							});

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

					// Calculate total interval

					for (var n = 0; n < this.flatActivityInstanceList.length; ++n) {
						this.start = Math.min(this.start,
								this.flatActivityInstanceList[n].start);

						if (this.flatActivityInstanceList[n].end) {
							this.end = Math.max(this.end,
									this.flatActivityInstanceList[n].end);
						}

						console
								.log("Activity "
										+ this.flatActivityInstanceList[n].activity.name
										+ " "
										+ moment(
												this.flatActivityInstanceList[n].start)
												.format("M/D/YYYY h:mm a"));
						if (this.flatActivityInstanceList[n].end) {
							console
									.log("Activity "
											+ this.flatActivityInstanceList[n].activity.name
											+ " "
											+ moment(
													this.flatActivityInstanceList[n].end)
													.format("M/D/YYYY h:mm a"));
						}
					}

					// TODO Workaround, server needs to calculate

					for (var n = 0; n < this.flatActivityInstanceList.length; ++n) {
						if (!this.flatActivityInstanceList[n].end) {
							this.flatActivityInstanceList[n].end = this.end;
						}
					}

					this.duration = this.end - this.start;
					this.currentTimeDivision = null;

					var barCellWidth;

					for (var n = 0; n < this.flatActivityInstanceList.length; ++n) {
						var activityInstance = this.flatActivityInstanceList[n];
						var barDivision = jQuery("#" + activityInstance.activity.id);

						barCell = jQuery(barDivision.parent());

						if (!this.currentTimeDivision) {
							this.currentTimeDivision = jQuery("#currentTimeDivision");

							barCellWidth = barCell.width();

							this.currentTimeDivision.css(
									{
										left : barCell.offset().left
												+ (this.now - this.start)
												/ this.duration * barCellWidth,
										top : jQuery("#ganttChartTable")
												.position().top,
										width : 0,
										height : jQuery("#ganttChartTable")
												.height(),
										visibility : "visible"
									}, 2000);
						}

						barDivision.css({
							left : (activityInstance.start - this.start)
									/ this.duration * barCellWidth,
							top : 0,
							visibility : "visible"
						});

						barDivision
								.css(
										{
											width : Math
													.ceil((activityInstance.end - activityInstance.start)
															/ this.duration
															* barCellWidth)
										}, 2000);

						var color = "#BBBBBB";
						var opacity = "1";

						for (var l = 0; l < this.benchmark.categories.length; ++l) {
							if (activityInstance.criticality >= this.benchmark.categories[l].low
									&& activityInstance.criticality <= this.benchmark.categories[l].high) {
								color = this.benchmark.categories[l].color;

								break;
							}
						}

						// Make planned semi-translucent
						// TODO Ugly to filter on a state string

						if (activityInstance.state == "Planned") {
							opacity = "0.3";
						}

						jQuery(barDivision.children("table")).css({
							"opacity" : opacity,
							"background-color" : color
						});

						var self = this;

						barDivision
								.hover(
										function(event) {
											self.tooltipActivityInstance = self.flatActivityInstanceList[jQuery(
													this).parent().parent()
													.index() - 1];

											jQuery("#activityInstanceTooltip")
													.css({
														'top' : event.clientY,
														'left' : event.clientX
													});
											jQuery("#activityInstanceTooltip")
													.show();
											self.safeApply();
										}, function() {
											jQuery("#activityInstanceTooltip")
													.hide();
										})
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
							format : "M/D"
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

					console.log("Units:");
					console.log(units);

					jQuery("#timeAxisDivision").empty();

					var startTime = moment(this.start);
					var endTime = moment(this.end);
					var startTickTime = startTime.clone().endOf(
							units.startOffset); // Last unit before
					var tickTime = startTickTime.clone();

					while (!tickTime.isAfter(endTime)) {
						console.log(tickTime.format("M/D/YYYY h:mm a"));

						var tick = jQuery("<table class='tickTable layoutTable'><tr><td colspan='2'>"
								+ tickTime.format(units.format)
								+ "</td></tr><tr><td class='tickCell' style='width: 50%;'></td><td style='width: 50%;'></td></tr></table>");

						jQuery("#timeAxisDivision").append(tick);

						console.log(tickTime.diff(startTickTime)
								/ this.millisPerHour);
						console.log(tickTime.diff(startTickTime)
								/ this.duration);

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
