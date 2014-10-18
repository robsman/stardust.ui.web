/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

define([ "benchmark/js/Utils", "benchmark/js/BenchmarkService" ], function(
		Utils, BenchmarkService) {
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

			var self = this;

			// BenchmarkService.instance().getBusinessObjects().done(
			// function(businessObjectModels) {
			// }).fail(function() {
			// });
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
