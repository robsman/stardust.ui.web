/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

define(
		[
				"simple-modeler/js/Utils",
				"benchmark/js/BenchmarkService",
				"business-object-management/js/BusinessObjectManagementService",
				"business-object-management/js/BusinessObjectManagementPanelController",
				"benchmark/js/ColorGenerator" ],
		function(Utils, BenchmarkService, BusinessObjectManagementService,
				BusinessObjectManagementPanelController, ColorGenerator) {
			return {
				create : function() {
					var controller = new TrafficLightViewController();

					return controller;
				}
			};

			/**
			 * 
			 */
			function TrafficLightViewController() {
				/**
				 * 
				 */
				TrafficLightViewController.prototype.initialize = function() {
					this.queryParameters = Utils.getQueryParameters();

					console.log("Query Parameters");
					console.log(this.queryParameters);

					this.messages = [];
					this.trafficLightSelectionDialog = {};
					this.businessObjectManagementPanelController = BusinessObjectManagementPanelController
							.create();

					this.businessObjectManagementPanelController
							.initialize(this);

					this.models = [ {
						id : "DailyFundsProcessing",
						name : "Daily Funds Processing",
						processes : [ {
							id : "ProcessingEurope",
							name : "Processing Europe",
							activities : [ {
								id : "RetrievePrices",
								name : "Retrieve Prices"
							}, {
								id : "SweepAndTranslate",
								name : "Sweep and Translate"
							} ]
						} ]
					} ];

					this.trafficLights = [];
					this.trafficLightsMap = {};

					this.benchmarks = [ {
						name : "Criticality",
						categories : [ {
							name : "Normal",
							color : "#00FF00"
						}, {
							name : "At Risk",
							color : "#FFFB00"
						}, {
							name : "Critical",
							color : "#FF0000"
						} ],
						trafficLights : [ {
							process : {
								name : "Daily Fund Processing Europe"
							},
							model : {
								name : "Daily Fund Processing"
							}
						}, {
							process : {
								name : "Daily Fund Processing Europe"
							},
							activity : {
								name : "Sweep and Translate"
							},
							model : {
								name : "Daily Fund Processing"
							}
						}, {
							process : {
								name : "Daily Fund Processing US"
							},
							model : {
								name : "Daily Fund Processing"
							}
						} ]
					}, {
						name : "Standard Funds Processing",
						categories : [ {
							name : "Normal",
							color : "#FF0000"
						}, {
							name : "At Risk",
							color : "#00FFFF"
						}, {
							name : "Critical",
							color : "#0000FF"
						} ],
						trafficLights : [ {
							process : {
								name : "Monthly Tax Report"
							},
							model : {
								name : "Report Dissimination"
							}
						}, {
							process : {
								name : "Monthly Tax Report"
							},
							activity : {
								name : "Retrieve Report Data"
							},
							model : {
								name : "Report Dissimination"
							}
						} ]
					} ];
					this.benchmark = this.benchmarks[0];

					this.enhanceInformation();

					// TODO For testing simulates already selected benchmarks

					this.displayedBenchmarks = [
							{
								name : this.benchmarks[0].name,
								categories : this.benchmarks[0].categories,
								trafficLights : [
										this.benchmarks[0].trafficLights[0],
										this.benchmarks[0].trafficLights[1] ]
							},
							{
								name : this.benchmarks[1].name,
								categories : this.benchmarks[1].categories,
								trafficLights : [ this.benchmarks[1].trafficLights[0] ]
							} ];

					this.displayedTrafficLights = {};
					this.displayedTrafficLights[this.benchmarks[0].name] = {
						trafficLights : this.displayedBenchmarks[0].trafficLights
					};
					this.displayedTrafficLights[this.benchmarks[0].name][this.benchmarks[0].trafficLights[0].path] = this.benchmarks[0].trafficLights[0];
					this.displayedTrafficLights[this.benchmarks[0].name][this.benchmarks[0].trafficLights[1].path] = this.benchmarks[0].trafficLights[1];
					this.displayedTrafficLights[this.benchmarks[1].name] = {
						trafficLights : this.displayedBenchmarks[1].trafficLights
					};
					this.displayedTrafficLights[this.benchmarks[1].name][this.benchmarks[1].trafficLights[0].path] = this.benchmarks[1].trafficLights[1];

					var self = this;

					BusinessObjectManagementService
							.instance()
							.getBusinessObjects()
							.done(
									function(businessObjectModels) {
										self.businessObjectModels = businessObjectModels;

										console.log(self.businessObjectModels);

										self.refreshBusinessObjects();

										if (self.queryParameters.benchmark
												&& self.queryParameters.drilldown) {
											self.preconfigured = true;
											self.drilldown = self.queryParameters.drilldown;

											// Evaluate preconfiguration of the
											// View

											// TODO Incomplete/Hack

											if (self.queryParameters.businessDate == "TODAY") {
												self.businessDate = "30/04/1966";
											}

											for (var n = 0; n < self.benchmarks.length; ++n) {
												if (self.benchmarks[n].name == self.queryParameters.benchmark) {
													self.benchmark = self.benchmarks[n];

													break;
												}
											}

											if (self.queryParameters.drilldown == "BUSINESS_OBJECT") {
												for (var n = 0; n < self.businessObjectModels.length; ++n) {
													for (var m = 0; m < self.businessObjectModels[n].businessObjects.length; ++m) {
														if (self.businessObjectModels[n].businessObjects[m].name == self.queryParameters.businessObject) {
															self.businessObject = self.businessObjectModels[n].businessObjects[m];

															self
																	.onBusinessObjectChange();

															break;
														}
													}
												}
											}
										}

										self.safeApply();
									}).fail(function() {
							});
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.onDrillDownChange = function(
						row) {
					this.trafficLights = [];
					this.expandedRows = {};

					var self = this;

					if (this.drilldown == 'PROCESS') {
						this.initializeTrafficLightsFromModels();
						BenchmarkService.instance().getActivityInstances(
								this.businessObject).done(
								function(activityInstances) {
									self.activityInstances = activityInstances;

									self.calculateCounts();
									self.safeApply();
								}).fail(function() {
							// TODO Error handling
						});
					} else if (this.drilldown == 'BUSINESS_OBJECT') {
						// Wait for BO to be selected
					}
				};

				/**
				 * Should be executed on the server.
				 */
				TrafficLightViewController.prototype.calculateCounts = function() {
					for (var n = 0; n < this.activityInstances.length; ++n) {
						for (var m = 0; m < this.benchmark.categories.length; ++m) {
							if (this.activityInstances[n].state != "Completed"
									&& this.activityInstances[n].state != "Aborted") {
								// TODO Filter by benchmark boundaries

								if (!this.trafficLightsMap["/"].categories[this.benchmark.categories[m].name]) {
									this.trafficLightsMap["/"].categories[this.benchmark.categories[m].name] = [];
								}

								this.trafficLightsMap["/"].categories[this.benchmark.categories[m].name]
										.push(this.activityInstances[n]);
							}

							break;
						}

						if (this.activityInstances[n].state == "Completed") {
							this.trafficLightsMap["/"].completed
									.push(this.activityInstances[n]);
						} else if (this.activityInstances[n].state == "Aborted") {
							this.trafficLightsMap["/"].aborted
									.push(this.activityInstances[n]);
						}

						this.trafficLightsMap["/"].sum
								.push(this.activityInstances[n]);
					}
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.initializeTrafficLightsFromModels = function() {
					var rootRow;

					this.trafficLights.push(rootRow = {
						level : 0,
						path : "/",
						type : "ROOT",
						name : "Total",
						categories : {},
						completed : [],
						aborted : [],
						sum : []
					});

					this.trafficLightsMap["/"] = rootRow;

					for (var n = 0; n < this.models.length; ++n) {
						var model = this.models[n];
						var modelRow;

						this.trafficLights.push(modelRow = {
							level : 1,
							path : "/" + model.id,
							type : "MODEL",
							name : model.name,
							parent : rootRow,
							categories : {},
							completed : [],
							aborted : [],
							sum : []
						});

						this.trafficLightsMap[modelRow.path] = modelRow;

						for (var m = 0; m < model.processes.length; ++m) {
							var process = model.processes[m];
							var processRow;

							this.trafficLights.push(processRow = {
								level : 2,
								path : "/" + model.id + "/" + process.id,
								parent : modelRow,
								type : "PROCESS",
								name : process.name,
								categories : {},
								completed : [],
								aborted : [],
								sum : []
							});

							this.trafficLightsMap[processRow.path] = processRow;

							for (var l = 0; l < process.activities.length; ++l) {
								var activity = process.activities[l];
								var activityRow;

								this.trafficLights.push(activityRow = {
									level : 3,
									path : "/" + model.id + "/" + process.id
											+ "/" + activity.id,
									parent : processRow,
									type : "ACTIVITY",
									name : activity.name,
									categories : {},
									completed : [],
									aborted : [],
									sum : []
								});

								this.trafficLightsMap[activityRow.path] = activityRow;
							}
						}
					}
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.initializeTrafficLightsFromBusinessObjectInstances = function() {
					var rootRow;

					this.trafficLights.push(rootRow = {
						level : 0,
						path : "/",
						type : "ROOT",
						name : "Total",
						categories : {},
						completed : [],
						aborted : [],
						sum : []
					});

					this.trafficLightsMap[rootRow.path] = rootRow;
					var groups = {};

					for (var n = 0; n < this.businessObjectInstances.length; ++n) {
						var businessObjectInstance = this.businessObjectInstances[n];

						if (!businessObjectInstance.FundGroup
								|| businessObjectInstance.FundGroup == "") {
							continue;
						}

						var groupRow = groups[businessObjectInstance.FundGroup];

						if (!groupRow) {
							this.trafficLights.push(groupRow = {
								level : 1,
								path : "/" + businessObjectInstance.FundGroup,
								type : "Fund Group",
								name : "Fund Group "
										+ businessObjectInstance.FundGroup,
								parent : rootRow,
								categories : {},
								completed : [],
								aborted : [],
								sum : []
							});

							groups[businessObjectInstance.FundGroup] = groupRow;

							this.trafficLightsMap[groupRow.path] = groupRow;
						}

						var objectRow;

						this.trafficLights
								.push(objectRow = {
									level : 2,
									path : "/"
											+ businessObjectInstance.FundGroup
											+ "/"
											+ this.businessObject.name
											+ businessObjectInstance[this.businessObject.primaryKeyField.id],
									type : this.businessObject.name,
									name : this.businessObject.name
											+ " "
											+ businessObjectInstance[this.businessObject.primaryKeyField.id],
									parent : groupRow,
									categories : {},
									completed : [],
									aborted : [],
									sum : []
								});

						this.trafficLightsMap[objectRow.path] = objectRow;
					}
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.expandRow = function(row) {
					this.expandedRows[row.path] = row;
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.collapseRow = function(row) {
					delete this.expandedRows[row.path];

					// if (activityInstance.subProcessInstance) {
					// for (var n = 0; n <
					// activityInstance.subProcessInstance.activityInstances.length;
					// ++n) {
					// this
					// .collapseActivityInstance(activityInstance.subProcessInstance.activityInstances[n]);
					// }
					// }
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.isExpandable = function(
						row) {
					return !this.expandedRows[row.path]
							&& row.type != "ACTIVITY";
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.isCollapsable = function(
						row) {
					return this.expandedRows[row.path];
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.refreshBusinessObjects = function() {
					this.businessObjects = [];

					for (var n = 0; n < this.businessObjectModels.length; ++n) {
						for (var m = 0; m < this.businessObjectModels[n].businessObjects.length; ++m) {
							if (!this.businessObjectModels[n].businessObjects[m].types) {
								this.businessObjectModels[n].businessObjects[m].types = {};
							}

							this.businessObjectModels[n].businessObjects[m].modelOid = this.businessObjectModels[n].oid;
							this.businessObjectModels[n].businessObjects[m].label = this.businessObjectModels[n].name
									+ "/"
									+ this.businessObjectModels[n].businessObjects[m].name;
							this.businessObjects
									.push(this.businessObjectModels[n].businessObjects[m]);
						}
					}
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.enhanceInformation = function() {
					for (var n = 0; n < this.benchmarks.length; ++n) {
						var benchmark = this.benchmarks[n];

						for (var m = 0; m < benchmark.trafficLights.length; ++m) {
							var trafficLight = benchmark.trafficLights[m];
							var path = trafficLight.process.name;

							if (trafficLight.activity) {
								path += "/";
								path += trafficLight.activity.name;
							}

							trafficLight.path = path;
						}
					}
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.onBusinessObjectChange = function() {
					for (var n = 0; n < this.businessObject.fields.length; ++n) {
						if (this.businessObject.fields[n].primaryKey) {
							this.businessObject.primaryKeyField = this.businessObject.fields[n];

							break;
						}
					}

					var self = this;

					BusinessObjectManagementService
							.instance()
							.getBusinessObjectInstances(this.businessObject)
							.done(
									function(businessObjectInstances) {
										console.log("Result");
										console.log(businessObjectInstances);

										self.businessObjectInstances = businessObjectInstances;

										self.trafficLights = [];
										self.expandedRows = {};

										self
												.initializeTrafficLightsFromBusinessObjectInstances();

										BenchmarkService
												.instance()
												.getActivityInstances(
														this.businessObject)
												.done(
														function(
																activityInstances) {
															self.activityInstances = activityInstances;

															self
																	.calculateCounts();
															self.safeApply();
														}).fail(function() {
													// TODO Error handling
												});
									}).fail(function() {
								// TODO Error handling
							});
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.getCategoryActivityInstances = function(
						trafficLight, category) {
					return this.trafficLightsMap[trafficLight.path].categories[category.name];
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.getCategoryCount = function(
						trafficLight, category) {
					if (this.trafficLightsMap[trafficLight.path].categories[category.name]) {
						return this.trafficLightsMap[trafficLight.path].categories[category.name].length;
					} else {
						return 0;
					}
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.getCompletedActivityInstances = function(
						trafficLight) {
					return this.trafficLightsMap[trafficLight.path].completed;
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.getCompletedCount = function(
						trafficLight) {
					return this.trafficLightsMap[trafficLight.path].completed.length;
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.getAbortedActivityInstances = function(
						trafficLight) {
					return this.trafficLightsMap[trafficLight.path].aborted;
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.getAbortedCount = function(
						trafficLight) {
					return this.trafficLightsMap[trafficLight.path].aborted.length;
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.getSumActivityInstances = function(
						trafficLight) {
					return this.trafficLightsMap[trafficLight.path].sum;
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.getSumCount = function(
						trafficLight) {
					return this.trafficLightsMap[trafficLight.path].sum.length;
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.getCountStyle = function(
						count, sumCount, category) {
					
					var size = count == 0 ? 0 : count / sumCount;
					
					if (category) {
						return "color: " + category.color + "; font-size:"
								+ Math.ceil(30 * size) + "px;";
					} else {
						return "color: #AAAAAA; font-size:"
								+ Math.ceil(30 * size) + "px;";
					}
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.openTrafficLightSelectionDialog = function(
						benchmark) {
					this.trafficLightSelectionDialog.benchmark = benchmark;

					this
							.onTrafficLightSelectionDialogBenchmarkSelectionChange();
					this.trafficLightSelectionDialog.dialog("open");
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.onTrafficLightSelectionDialogBenchmarkSelectionChange = function() {
					this.trafficLightSelectionDialog.trafficLights = [];

					if (this.trafficLightSelectionDialog.benchmark) {
						for (var n = 0; n < this.trafficLightSelectionDialog.benchmark.trafficLights.length; ++n) {
							if (!this.displayedTrafficLights[this.trafficLightSelectionDialog.benchmark.name][this.trafficLightSelectionDialog.benchmark.trafficLights[n].path]) {
								this.trafficLightSelectionDialog.trafficLights
										.push(this.trafficLightSelectionDialog.benchmark.trafficLights[n]);
							}
						}
					}
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.addTrafficLight = function() {
					this.trafficLightSelectionDialog.benchmark.trafficLights
							.push(this.trafficLightSelectionDialog.trafficLight);
					this.displayedTrafficLights[this.trafficLightSelectionDialog.benchmark.name].trafficLights
							.push(this.trafficLightSelectionDialog.trafficLight);
					this.displayedTrafficLights[this.trafficLightSelectionDialog.benchmark.name][this.trafficLightSelectionDialog.trafficLight.label] = this.trafficLightSelectionDialog.trafficLight;

					this.closeTrafficLightSelectionDialog();
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.closeTrafficLightSelectionDialog = function() {
					this.trafficLightSelectionDialog.dialog("close");
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.openGanttChartView = function(
						activity) {
					this
							.openView("ganttChartView",
									"viewId=ganttChartView&oid="
											+ activity.rootProcessInstance.oid,
									window.btoa("viewId=ganttChartView&oid="
											+ activity.rootProcessInstance.oid));
				};

				/**
				 * TODO - re-use a Util from web-modeler
				 */
				TrafficLightViewController.prototype.openView = function(
						viewId, viewParams, viewIdentity) {
					var portalWinDoc = this.getOutlineWindowAndDocument();
					var link = jQuery("a[id $= 'view_management_link']",
							portalWinDoc.doc);
					var linkId = link.attr('id');
					var form = link.parents('form:first');
					var formId = form.attr('id');

					link = portalWinDoc.doc.getElementById(linkId);

					var linkForm = portalWinDoc.win.formOf(link);

					linkForm[formId + ':_idcl'].value = linkId;
					linkForm['viewParams'].value = viewParams;
					linkForm['viewId'].value = viewId;
					linkForm['viewIdentity'].value = viewIdentity;

					portalWinDoc.win.iceSubmit(linkForm, link);
				};

				/*
				 * 
				 */
				TrafficLightViewController.prototype.getOutlineWindowAndDocument = function() {
					return {
						win : parent.document
								.getElementById("portalLaunchPanels").contentWindow,
						doc : parent.document
								.getElementById("portalLaunchPanels").contentDocument
					};
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.safeApply = function(fn) {
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
				TrafficLightViewController.prototype.formatTimeStamp = function(
						timeStamp) {
					return moment(timeStamp).format("M/D/YYYY h:mm a");
				};
			}
		});
