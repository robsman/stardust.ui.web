/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

define(
		[ "benchmark/js/Utils", "benchmark/js/BenchmarkService",
				"benchmark/js/ColorGenerator" ],
		function(Utils, BenchmarkService, ColorGenerator) {
			return {
				create : function() {
					var controller = new BenchmarkViewController();

					return controller;
				}
			};

			/**
			 * 
			 */
			function BenchmarkViewController() {
				/**
				 * 
				 */
				BenchmarkViewController.prototype.initialize = function() {
					this.messages = [];
					this.colorPalette = [];
					this.benchmark = {}

					var hue = 90;

					for (var n = 0; n < 10; n++) {
						this.colorPalette.push(new ColorGenerator.create([ hue,
								100, 50 ]).getHex());

						hue += 60;
					}

					this.categories = [];

					var self = this;

					BenchmarkService.instance().getModels().done(
							function(models) {
								self.models = models;
								self.modelTree = [];

								self.initializeModelTree();
								
								self.expandedRows = {};
								
								// For testing

								self.addCategory();

								self.categories[0] = {
									name : "Normal",
									color : self.colorPalette[0],
									threshold : 999,
									high : 300,
									conditions : {}
								};
								
								self.safeApply();
							}).fail(function() {
					});
				};

				/**
				 * 
				 */
				BenchmarkViewController.prototype.initializeModelTree = function() {
					for (var n = 0; n < this.models.length; ++n) {
						var model = this.models[n];
						var modelRow;

						this.modelTree.push(modelRow = {
							level : 0,
							path : model.id,
							type : "MODEL",
							name : model.name,
							mode : "BOOLEAN"
						});

						for (var m = 0; m < model.processDefinitions.length; ++m) {
							var process = model.processDefinitions[m];
							var processRow;

							this.modelTree.push(processRow = {
								level : 1,
								path : model.id + "/" + process.id,
								parent : modelRow,
								type : "PROCESS",
								name : process.name,
								mode : "BOOLEAN",
								conditions : []
							});

							for (var l = 0; l < process.activities.length; ++l) {
								var activity = process.activities[l];

								this.modelTree.push({
									level : 2,
									path : model.id + "/" + process.id + "/"
											+ activity.id,
									parent : processRow,
									type : "ACTIVITY",
									name : activity.name,
									mode : "BOOLEAN",
									conditions : []
								});
							}
						}
					}
				};

				/**
				 * 
				 */
				BenchmarkViewController.prototype.expandRow = function(row) {
					this.expandedRows[row.path] = row;
				};

				/**
				 * 
				 */
				BenchmarkViewController.prototype.collapseRow = function(row) {
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
				BenchmarkViewController.prototype.isExpandable = function(row) {
					return !this.expandedRows[row.path]
							&& row.type != "ACTIVITY";
				};

				/**
				 * 
				 */
				BenchmarkViewController.prototype.isCollapsable = function(row) {
					return this.expandedRows[row.path];
				};

				/**
				 * 
				 */
				BenchmarkViewController.prototype.addCategory = function() {
					this.categories.push({
						color : this.colorPalette[this.categories.length],
						threshold : 999,
						conditions : {}
					});

					for (var n = 0; n < this.modelTree.length; ++n) {
						if (this.modelTree[n].type != "MODEL") {
							this.modelTree[n].conditions.push({});
						}
					}

					this.submitChanges();
				};

				/**
				 * 
				 */
				BenchmarkViewController.prototype.deleteCategory = function(
						index) {
					this.categories.splice(index, 1);

					for (var n = 0; n < this.modelTree.length; ++n) {
						if (this.modelTree[n].type != "MODEL") {
							this.modelTree[n].conditions.splice(index, 1);
						}
					}

					this.submitChanges();
				};

				/**
				 * 
				 */
				BenchmarkViewController.prototype.getLow = function(index) {
					if (!index) {
						return 0;
					}

					return this.categories[index - 1].threshold;
				}

				/**
				 * 
				 */
				BenchmarkViewController.prototype.getConditionCellClass = function(
						condition) {
					if (condition && condition.type) {
						return "conditionCell";
					}

					return "";
				};

				/**
				 * 
				 */
				BenchmarkViewController.prototype.submitChanges = function() {
					// Transfer conditions to benchmark Categories

					for (var n = 0; n < this.modelTree.length; ++n) {
						var row = this.modelTree[n];

						if (row.conditions) {
							for (var m = 0; m < row.conditions.length; ++m) {
								this.categories[m].conditions[row.path] = row.conditions[m];
							}
						}
					}

					this.benchmark.categories = this.categories;
					console.log("Send content to server: " , this.benchmark);
				}

				/**
				 * 
				 */
				BenchmarkViewController.prototype.safeApply = function(fn) {
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
