/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

define([ "document-triage/js/Utils" ], function(Utils) {
	return {
		instance : function() {
			return new DocumentAssignmentService();
		}
	};

	/**
	 * 
	 */
	function DocumentAssignmentService() {
		this.nase = "Propase";
		/**
		 * 
		 */
		DocumentAssignmentService.prototype.initialize = function() {
		};
	}
});
