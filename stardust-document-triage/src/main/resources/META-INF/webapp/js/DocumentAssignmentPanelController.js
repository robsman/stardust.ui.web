/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

define([ "document-triage/js/Utils",
		"document-triage/js/DocumentAssignmentService" ], function(Utils,
		DocumentAssignmentService) {
	return {
		create : function() {
			var controller = new DocumentAssignmentPanelController();

			return controller;
		}
	};

	/**
	 * 
	 */
	function DocumentAssignmentPanelController() {
		/**
		 * 
		 */
		DocumentAssignmentPanelController.prototype.initialize = function() {
			this.nase = "Propase";
		};

		/**
		 * 
		 */
		DocumentAssignmentPanelController.prototype.safeApply = function(fn) {
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
