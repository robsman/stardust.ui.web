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
			this.trafficLightSelectionDialog = {};
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
					type : "PROCESS",
					model : {
						name : "Daily Fund Processing"
					},
					name: "Receive Prices"
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
				trafficLights : []
			} ];
		};

		/**
		 * 
		 */
		BenchmarkViewController.prototype.openTrafficLightSelectionDialog = function(fn) {
			this.trafficLightSelectionDialog.dialog("open");		
		};
		
		/**
		 * 
		 */
		BenchmarkViewController.prototype.closeTrafficLightSelectionDialog = function(fn) {
			this.trafficLightSelectionDialog.dialog("close");		
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
