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

					// TODO For testing simulates already selected benchmarks

					/*
					 * this.displayedBenchmarks = [ { name :
					 * this.benchmarks[0].name, categories :
					 * this.benchmarks[0].categories, trafficLights : [
					 * this.benchmarks[0].trafficLights[0],
					 * this.benchmarks[0].trafficLights[1] ] }, { name :
					 * this.benchmarks[1].name, categories :
					 * this.benchmarks[1].categories, trafficLights : [
					 * this.benchmarks[1].trafficLights[0] ] } ];
					 * 
					 * this.displayedTrafficLights = {};
					 * this.displayedTrafficLights[this.benchmarks[0].name] = {
					 * trafficLights : this.displayedBenchmarks[0].trafficLights };
					 * this.displayedTrafficLights[this.benchmarks[0].name][this.benchmarks[0].trafficLights[0].path] =
					 * this.benchmarks[0].trafficLights[0];
					 * this.displayedTrafficLights[this.benchmarks[0].name][this.benchmarks[0].trafficLights[1].path] =
					 * this.benchmarks[0].trafficLights[1];
					 * this.displayedTrafficLights[this.benchmarks[1].name] = {
					 * trafficLights : this.displayedBenchmarks[1].trafficLights };
					 * this.displayedTrafficLights[this.benchmarks[1].name][this.benchmarks[1].trafficLights[0].path] =
					 * this.benchmarks[1].trafficLights[1];
					 */

					var self = this;

					BenchmarkService
							.instance()
							.getBenchmarks()
							.done(
									function(benchmarks) {
										self.benchmarks = benchmarks;
										self.benchmark = self.benchmarks[0];
										self.enhanceInformation();

										BusinessObjectManagementService
												.instance()
												.getBusinessObjects()
												.done(
														function(
																businessObjectModels) {
															self.businessObjectModels = businessObjectModels;

															console
																	.log(self.businessObjectModels);

															self
																	.refreshBusinessObjects();

															if (self.queryParameters.benchmark
																	&& self.queryParameters.drilldown) {
																self.preconfigured = true;
																self.drilldown = self.queryParameters.drilldown;

																// Evaluate
																// preconfiguration
																// of the
																// View

																// TODO
																// Incomplete/Hack

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
															} else {
																self
																		.onDrillDownChange();
															}

															self.safeApply();
														}).fail(function() {
													// TODO Error message
												});
									}).fail(function() {
								// TODO Error message
							});
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.onDrillDownChange = function() {
					this.trafficLights = [];
					this.expandedRows = {};

					var self = this;

					if (!this.drilldown) {
						// TODO The following call may be executed entirely on
						// the server

						BenchmarkService.instance().getActivityInstances()
								.done(function(activityInstances) {
									self.activityInstances = activityInstances;

									self.initializeTrafficLightsNoDrilldown();
									self.calculateCounts();
									self.cumulateLeaveTrafficLights();

									self.safeApply();
								}).fail(function() {
									// TODO Error handling
								});
					} else if (this.drilldown == "PROCESS") {
						// TODO The following call may be executed entirely on
						// the server

						BenchmarkService
								.instance()
								.getModels()
								.done(
										function(models) {
											self.models = models;

											BenchmarkService
													.instance()
													.getActivityInstances()
													.done(
															function(
																	activityInstances) {
																self.activityInstances = activityInstances;

																self
																		.initializeTrafficLightsFromModels();
																self
																		.calculateCounts();
																self
																		.cumulateLeaveTrafficLights();

																self
																		.safeApply();
															}).fail(function() {
														// TODO Error handling
													});
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
						var currentTrafficLight;

						if (this.drilldown == "PROCESS") {
							console.log(this.activityInstances[n]);

							currentTrafficLight = this.leafTrafficLightsMap[this.activityInstances[n].activity.id]; // TODO
							// Use
							// ID
							// path
							// for
							// uniqueness
						} else {
							// TODO Dummy

							for (key in this.leafTrafficLightsMap) {
								currentTrafficLight = this.leafTrafficLightsMap[key];

								break;
							}
						}

						// Activity does not contribute to traffic light

						if (!currentTrafficLight) {
							continue;
						}

						// TODO Filter whether AI belongs to this Leaf TLV

						for (var m = 0; m < this.benchmark.categories.length; ++m) {
							if (this.activityInstances[n].state == "Completed"
									|| this.activityInstances[n].state == "Aborted") {
								continue;
							}

							if (this.activityInstances[n].criticality * 1000 >= this.benchmark.categories[m].low
									&& this.activityInstances[n].criticality * 1000 <= this.benchmark.categories[m].high) {
								if (!currentTrafficLight.categories[this.benchmark.categories[m].name]) {
									currentTrafficLight.categories[this.benchmark.categories[m].name] = [];
								}

								currentTrafficLight.categories[this.benchmark.categories[m].name]
										.push(this.activityInstances[n]);

								break;
							}
						}

						if (this.activityInstances[n].state == "Completed") {
							currentTrafficLight.completed
									.push(this.activityInstances[n]);
						} else if (this.activityInstances[n].state == "Aborted") {
							currentTrafficLight.aborted
									.push(this.activityInstances[n]);
						}

						currentTrafficLight.sum.push(this.activityInstances[n]);
					}
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.initializeTrafficLightsNoDrilldown = function() {
					this.trafficLights = [];
					this.trafficLightsMap = {};
					this.leafTrafficLightsMap = {};

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
					this.leafTrafficLightsMap["/"] = rootRow;
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.initializeTrafficLightsFromModels = function() {
					this.trafficLights = [];
					this.trafficLightsMap = {};
					this.leafTrafficLightsMap = {};

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

						for (var m = 0; m < model.processDefinitions.length; ++m) {
							var process = model.processDefinitions[m];
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
								this.leafTrafficLightsMap[activity.id] = activityRow; // TODO
								// Needs
								// to
								// be
								// the
								// full
								// path
							}
						}
					}
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.initializeTrafficLightsFromBusinessObjectInstances = function() {
					this.trafficLights = [];
					this.trafficLightsMap = {};
					this.leafTrafficLightsMap = {};

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

					// Cache Group By Instances in map

					this.groupByBusinessObjectInstancesRowMap = {};

					if (this.groupByBusinessObjectInstances) {
						for (var n = 0; n < this.groupByBusinessObjectInstances.length; ++n) {
							var groupByBusinessObjectInstance = this.groupByBusinessObjectInstances[n];
							var groupRow;

							this.trafficLights
									.push(groupRow = {
										level : 1,
										path : "/"
												+ groupByBusinessObjectInstance[this.groupByBusinessObject.primaryKeyField.id],
										type : this.groupByRelationship.otherBusinessObject.id,
										name : this.groupByRelationship.otherRole
												+ " "
												+ groupByBusinessObjectInstance[this.groupByBusinessObject.primaryKeyField.id], // TODO
										// Replace
										// by
										// name
										parent : rootRow,
										categories : {},
										completed : [],
										aborted : [],
										sum : []
									});

							this.groupByBusinessObjectInstancesRowMap[groupByBusinessObjectInstance[this.groupByBusinessObject.primaryKeyField.id]] = groupRow;
							this.trafficLightsMap[groupRow.path] = groupRow;
						}
					}

					for (var n = 0; n < this.businessObjectInstances.length; ++n) {
						var businessObjectInstance = this.businessObjectInstances[n];

						var parentRow = rootRow;
						var level = 1;

						if (this.groupByBusinessObjectInstances.length) {
							if (businessObjectInstance[this.groupByRelationship.otherForeignKeyField]) {
								if (this.groupByRelationship.otherCardinality == "TO_MANY") {
									for (var l = 0; l < businessObjectInstance[this.groupByRelationship.otherForeignKeyField].length; ++l) {
										if (parentRow = this.groupByBusinessObjectInstancesRowMap[businessObjectInstance[this.groupByRelationship.otherForeignKeyField][l]]) {
											level = 2;
											break;
										}
									}
								} else {
									parentRow = this.groupByBusinessObjectInstancesRowMap[businessObjectInstance[this.groupByRelationship.otherForeignKeyField]];
									level = 2;
								}
							}
						}

						var objectRow;

						this.trafficLights
								.push(objectRow = {
									level : level,
									path : "/"
											+ businessObjectInstance.FundGroup
											+ "/"
											+ this.businessObject.name
											+ businessObjectInstance[this.businessObject.primaryKeyField.id],
									type : this.businessObject.name,
									name : this.businessObject.name
											+ " "
											+ businessObjectInstance[this.businessObject.primaryKeyField.id], // TODO
									// Replace
									// by
									// name
									parent : parentRow,
									categories : {},
									completed : [],
									aborted : [],
									sum : []
								});

						this.trafficLightsMap[objectRow.path] = objectRow;
						this.leafTrafficLightsMap[businessObjectInstance[this.businessObject.primaryKeyField.id]] = objectRow;
					}
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.cumulateLeaveTrafficLights = function() {
					for ( var n in this.leafTrafficLightsMap) {
						var leafTrafficLight = this.leafTrafficLightsMap[n];
						var currentTrafficLight = leafTrafficLight;

						while (currentTrafficLight.parent) {
							for (var m = 0; m < this.benchmark.categories.length; ++m) {
								if (leafTrafficLight.categories[this.benchmark.categories[m].name]) {
									if (!currentTrafficLight.parent.categories[this.benchmark.categories[m].name]) {
										currentTrafficLight.parent.categories[this.benchmark.categories[m].name] = [];
									}

									currentTrafficLight.parent.categories[this.benchmark.categories[m].name].push
											.apply(
													currentTrafficLight.parent.categories[this.benchmark.categories[m].name],
													leafTrafficLight.categories[this.benchmark.categories[m].name])
								}
							}

							currentTrafficLight.parent.completed.push.apply(
									currentTrafficLight.parent.completed,
									leafTrafficLight.completed);
							currentTrafficLight.parent.aborted.push.apply(
									currentTrafficLight.parent.aborted,
									leafTrafficLight.aborted);
							currentTrafficLight.parent.sum.push.apply(
									currentTrafficLight.parent.sum,
									leafTrafficLight.sum);

							currentTrafficLight = currentTrafficLight.parent;
						}
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
					if (!this.drilldown) {
						return false;
					} else if (this.drilldown == "BUSINESS_OBJECT") {
						return !this.expandedRows[row.path]
								&& row.type != this.businessObject.name;
					} else {
						return !this.expandedRows[row.path]
								&& row.type != "ACTIVITY";
					}
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
					this.businessObjectsMap = {};

					for (var n = 0; n < this.businessObjectModels.length; ++n) {
						for (var m = 0; m < this.businessObjectModels[n].businessObjects.length; ++m) {
							// Cache in a map to lookup business objects later
							// for group by changes

							this.businessObjectsMap[this.businessObjectModels[n].id
									+ ":"
									+ this.businessObjectModels[n].businessObjects[m].id] = this.businessObjectModels[n].businessObjects[m];

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
				 * TODO Make part of BO Service
				 */
				TrafficLightViewController.prototype.addPrimaryKeyField = function(
						businessObject) {
					for (var n = 0; n < businessObject.fields.length; ++n) {
						if (businessObject.fields[n].primaryKey) {
							businessObject.primaryKeyField = businessObject.fields[n];

							break;
						}
					}
				}

				/**
				 * 
				 */
				TrafficLightViewController.prototype.onBusinessObjectChange = function() {
					this.groupByRelationship = null;
					this.groupByBusinessObjectInstances = [];

					// TODO Make part of BO Service

					this.addPrimaryKeyField(this.businessObject);

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

										// TODO The following call may be
										// executed entirely on the server

										BenchmarkService
												.instance()
												.getActivityInstances()
												.done(
														function(
																activityInstances) {
															self.activityInstances = activityInstances;

															self
																	.initializeTrafficLightsFromBusinessObjectInstances();
															self
																	.calculateCounts();
															self
																	.cumulateLeaveTrafficLights();
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
				TrafficLightViewController.prototype.onGroupByRelationshipChange = function() {
					if (!this.groupByRelationship) {
						this.groupByBusinessInstances = [];
					} else {
						var self = this;

						this.groupByBusinessObject = this.businessObjectsMap[this.groupByRelationship.otherBusinessObject.modelId
								+ ":"
								+ this.groupByRelationship.otherBusinessObject.id];

						// TODO Make part of BO Service

						this.addPrimaryKeyField(this.groupByBusinessObject);

						console.log("Group By Field");
						console.log(this.groupByBusinessObject);

						BusinessObjectManagementService
								.instance()
								.getBusinessObjectInstances(
										this.groupByBusinessObject)
								.done(
										function(businessObjectInstances) {
											console.log("Result");
											console
													.log(businessObjectInstances);

											self.groupByBusinessObjectInstances = businessObjectInstances;

											self.trafficLights = [];
											self.expandedRows = {};

											// TODO The following call may be
											// executed entirely on the server

											BenchmarkService
													.instance()
													.getActivityInstances()
													.done(
															function(
																	activityInstances) {
																self.activityInstances = activityInstances;

																self
																		.initializeTrafficLightsFromBusinessObjectInstances();
																self
																		.calculateCounts();
																self
																		.cumulateLeaveTrafficLights();
																self
																		.safeApply();
															}).fail(function() {
														// TODO Error handling
													});
										}).fail(function() {
									// TODO Error handling
								});
					}
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
					var style = "";
					var size = count == 0 ? 0 : count / sumCount;

					if (category) {
						style += "color: " + category.color + ";";
					} else {
						style += "color: #AAAAAA;";
					}

					style += "font-size:" + Math.ceil(30 * size) + "px;";
					style += "-webkit-animation: fadeIn 5s;";
					style += "-moz-animation: fadeIn 5s;"
					style += "-o-animation: fadeIn 5s;"
					style += "animation: fadeIn 5s;"

					return style;
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
					this.openView("ganttChartViewNew", "oid="
							+ activity.rootProcessInstance.oid, {
						oid : "" + activity.rootProcessInstance.oid
					});
				};

				/**
				 * 
				 */
				TrafficLightViewController.prototype.openView = function(
						viewId, viewIdentity, viewParams) {
					var command = {
						type : "OpenView",
						data : {
							viewId : viewId,
							viewKey : viewIdentity,
							params : viewParams
						}
					};

					parent.postMessage(JSON.stringify(command), "*");
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
