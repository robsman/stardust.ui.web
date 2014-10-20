/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

define(
		[
				"benchmark/js/Utils",
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
					this.messages = [];
					this.trafficLightSelectionDialog = {};
					this.businessObjectManagementPanelController = BusinessObjectManagementPanelController
							.create();

					this.businessObjectManagementPanelController
							.initialize(this);

					this.benchmarks = [ {
						name : "Criticality",
						categories : [ {
							name : "Normal",
							color : "#FF0000"
						}, {
							name : "At Risk",
							color : "#FFFB00"
						}, {
							name : "Critical",
							color : "#00FF00"
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

					this.processInstances = [];

					var self = this;

					BusinessObjectManagementService
							.instance()
							.getBusinessObjects()
							.done(
									function(businessObjectModels) {
										self.businessObjectModels = businessObjectModels;

										console.log(self.businessObjectModels);

										self.refreshBusinessObjects();
										self.safeApply();
									}).fail(function() {
							});
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
				TrafficLightViewController.prototype.loadProcessInstances = function() {
					this.processInstances.push({
						bla : ""
					});

					this.processInstances = this.processInstances.slice(0);
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
			}
		});
