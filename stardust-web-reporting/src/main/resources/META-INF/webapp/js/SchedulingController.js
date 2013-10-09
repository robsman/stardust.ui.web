/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

define([], function() {
	return {
		create : function(scheduling) {
			var controller = new SchedulingController();

			controller.initialize(scheduling);
			
			return controller;
		}
	};

	/**
	 * 
	 */
	function SchedulingController() {
		this.DAILY = "daily";
		this.WEEKLY = "weekly";
		this.MONTHLY = "monthly";
		this.YEARLY = "yearly";

		/**
		 * 
		 */
		SchedulingController.prototype.initialize = function(scheduling) {
			this.scheduling = scheduling;
			this.scheduling.recurrenceInterval = this.WEEKLY;
			this.scheduling.dailyRecurrenceOptions = {
					daysRecurrence: "interval",
					daysIntervalCount : 1
			};
			this.scheduling.weeklyRecurrenceOptions = {
				recurrenceWeekCount : 1,
				mondays : false,
				tuesdays : false,
				wednesdays : false,
				thursdays : false,
				fridays : false,
				saturdays : false,
				sundays : true
			};

			this.dataSetPanel = jQuery("#dataSetPanel");
		};
	}
});