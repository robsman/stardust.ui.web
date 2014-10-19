/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

define([ "benchmark/js/Utils", "benchmark/js/BenchmarkService",
		"benchmark/js/ColorGenerator" ], function(Utils, BenchmarkService,
		ColorGenerator) {
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

			var hue = 90;

			for (var n = 0; n < 10; n++) {
				this.colorPalette.push(new ColorGenerator.create(
						[ hue, 100, 50 ]).getHex());

				hue += 60;
			}
			
			console.log("Color Palette");
			console.log(this.colorPalette);
			
			var self = this;

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

			this.modelTree = [];

			this.initializeModelTree();
			this.expandedRows = {};
			this.categories = [];

			// For testing

			this.addCategory();

			this.categories[0] = {
				name : "Normal",
				color : this.colorPalette[0],
				threshold : 999,
				high : 300
			};

			// BenchmarkService.instance().getBusinessObjects().done(
			// function(businessObjectModels) {
			// }).fail(function() {
			// });
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
					name : model.name
				});

				for (var m = 0; m < model.processes.length; ++m) {
					var process = model.processes[m];
					var processRow;

					this.modelTree.push(processRow = {
						level : 1,
						path : model.id + "/" + process.id,
						parent : modelRow,
						type : "PROCESS",
						name : process.name,
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
			return !this.expandedRows[row.path] && row.type != "ACTIVITY";
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
				threshold : 999
			});

			for (var n = 0; n < this.modelTree.length; ++n) {
				if (this.modelTree[n].type != "MODEL") {
					this.modelTree[n].conditions.push({});
				}
			}
		};

		/**
		 * 
		 */
		BenchmarkViewController.prototype.deleteCategory = function(index) {
			this.categories.splice(index, 1);

			for (var n = 0; n < this.modelTree.length; ++n) {
				if (this.modelTree[n].type != "MODEL") {
					this.modelTree[n].conditions.splice(index, 1);
				}
			}
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
