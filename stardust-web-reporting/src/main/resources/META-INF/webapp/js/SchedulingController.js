/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

define([], function() {
	return {
		create : function() {
			var controller = new SchedulingController();

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
		SchedulingController.prototype.createDefaultSettings = function() {
			return {
				recurrenceInterval : this.WEEKLY,
				delivery : {
					mode : "personalFolder"
				},
				recurrenceRange : {
					startDate : new Date().toISOString(),
					endMode : "noEnd",
					occurences : 10
				},
				dailyRecurrenceOptions : {
					daysRecurrence : "interval",
					daysIntervalCount : 1
				},
				weeklyRecurrenceOptions : {
					recurrenceWeekCount : 1,
					mondays : false,
					tuesdays : false,
					wednesdays : false,
					thursdays : false,
					fridays : false,
					saturdays : false,
					sundays : true
				},
				monthlyRecurrenceOptions : {},
				yearlyRecurrenceOptions : {},
				recurrenceRange : {
					startDate : new Date().toISOString()
				}
			};
		};

		/**
		 * 
		 */
		SchedulingController.prototype.initialize = function(scheduling) {
			this.scheduling = scheduling;
		};
	}
});