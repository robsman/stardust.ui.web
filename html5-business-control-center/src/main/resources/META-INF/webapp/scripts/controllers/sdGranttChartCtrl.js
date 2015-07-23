/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Johnson.Quadras
 */

(function() {
	'use strict';

	angular.module("bcc-ui").controller(
			'sdGranttChartCtrl',
			['sdProcessInstanceService', 'sdLoggerService', '$filter',
			 'sgI18nService', Controller]);

	var _filter = null;

	var totalWidth = 900;

	var days = {
			process : {
				startTime : new Date(2015, 6, 6, 10, 10, 0).getTime(),
				endTime : new Date(2015, 7, 22, 16, 0, 0).getTime(),
				oid : '723',
				name : 'Fund Processing',
				status : 'Completed'
			},
			list : [{
				name : "Process",
				startTime : new Date(2015, 6, 6, 10, 10, 0).getTime(),
				endTime : new Date(2015, 7, 22, 16, 0, 0).getTime(),
				type : 'process',
				color : '#808080 ',
				status : 'Completed'
			}, {
				name : "Activity1",
				startTime : new Date(2015, 6, 16, 14, 10, 0).getTime(),
				predictedEndTime : new Date(2015, 6,27, 15, 0, 0).getTime(),
				type : 'activity',
				color : '#FF0000',
				status : 'Completed'
			}, {
				name : "Activity2",
				startTime : new Date(2015, 6, 17, 14, 0, 0).getTime(),
				predictedEndTime : new Date(2015, 6, 25, 15, 0, 0).getTime(),
				type : 'activity',
				color : '#ffff00',
				status : 'Pending'
			}, {
				name : "Activity3",
				startTime : new Date(2015, 6, 20, 15, 1, 0).getTime(),
				endTime : new Date(2015, 7, 8, 16, 0, 0).getTime(),
				type : 'activity',
				color : '#0000ff',
				status : 'Late'
			}

			]
	};


	var hours = {
			process : {
				startTime : new Date(2015, 6, 6, 10, 10, 0).getTime(),
				endTime : new Date(2015, 6,7, 18, 10, 0).getTime(),
				oid : '723',
				name : 'Fund Processing',
				status : 'Completed'
			},
			list : [{
				name : "Process",
				startTime :  new Date(2015, 6, 6, 10, 10, 0).getTime(),
				endTime :  new Date(2015, 6,7, 18, 10, 0).getTime(),
				type : 'process',
				color : '#808080 ',
				status : 'Completed'
			}, {
				name : "Activity1",
				startTime :  new Date(2015, 6, 6, 15, 12, 0).getTime(),
				endTime :  new Date(2015, 6, 6, 18, 20, 0).getTime(),
				type : 'activity',
				color : '#FF0000',
				status : 'Late'
			}
			]
	};


	var mins = {
			process : {
				startTime : new Date(2015, 6, 6, 10, 15, 0).getTime(),
				endTime : new Date(2015, 6, 6, 14,30, 0).getTime(),
				oid : '723',
				name : 'Fund Processing',
				status : 'Completed'
			},
			list : [{
				name : "Process",
				startTime :  new Date(2015, 6, 6, 10, 15, 0).getTime(),
				endTime :  new Date(2015, 6,6,14,30, 0).getTime(),
				type : 'process',
				color : '#808080 ',
				status : 'Completed'
			}, {
				name : "Activity1",
				startTime :  new Date(2015, 6, 6, 11, 30, 0).getTime(),
				endTime : new Date(2015, 6, 6, 14, 15, 0).getTime(),
				type : 'activity',
				color : '#FF0000',
				status : 'Late'
			}
			]
	};


	var data = days;

	var legendCategories = [{
		id : "benchmark",
		label : "Benchmark Name"
	}, {
		id : "status",
		label : "Status"
	}];

	var legends = [{
		label : "Complete",
		color : 'gray'
	}, {
		label : "On Time",
		color : '#00FF00'
	}, {
		label : "Almost Late",
		color : '#0000FF'
	}, {
		label : "Late",
		color : '#FF0000'
	}];

	var timeFrames = [{
		id : "minutes",
		label : "Minutes"
	}, {
		id : "hours",
		label : "Hours"
	}, {
		id : "days",
		label : "Days"
	}];
	/**
	 * 
	 */
	function Controller(sdProcessInstanceService, sdLoggerService, $filter,
			sdPreferenceService) {
		_filter = $filter;
		this.intialize();

	};

	Controller.prototype.getProcess = function() {
		return data.process;
	}

	Controller.prototype.getLegendCategories = function() {
		return legendCategories;
	}

	Controller.prototype.getLegends = function() {
		return legends;
	}

	Controller.prototype.getTimeFrames = function() {
		return timeFrames;
	}

	/**
	 * 
	 */
	Controller.prototype.intialize = function() {
		var self = this;

		self.data = {
				list : []
		};

		self.data.list = self.getProcessData();
		self.process = self.getProcess();

		self.legendCategories = self.getLegendCategories();

		self.legends = self.getLegends();

		self.timeFrames = self.getTimeFrames();

		self.selectedTimeFrame = "days";
		self.selectedCategory = "benchmark";
		
		self.onTimeFrameChange();

	};

	Controller.prototype.getProcessData = function() {
		return data.list;
	};

	var FACTORS = {
			days : {
				majorFactorWidth : totalWidth / 30,  // 30 days in  a month
				minorFactorWidth : totalWidth / (30 * 24), // 24 hours in a day
				minorFactor : (1000 * 60 * 60 ),  // 1 hour
				majorFactor : (1000 * 60 * 60 * 24) // 1 Day
			},
			hours : {
				majorFactorWidth : totalWidth / 24, // 24 hours in a day
				minorFactorWidth : totalWidth / (24 * 60), // 60 mins in a hour
				minorFactor : (1000 * 60 ), // 1 Min
				majorFactor : (1000 * 60 * 60) // 1 Hour
			},
			minutes : {
				majorFactorWidth : totalWidth / 15,  
				minorFactorWidth : totalWidth / (15 * 15), // 15 mins
				minorFactor : (1000 * 60 ), // 1 Min
				majorFactor : (1000 * 60  * 15)// 15 mins
			}
	}

	Controller.prototype.drawTimeFrameDays = function() {

		var factor = FACTORS.days;
		var self = this;
		var startTime = new Date(self.process.startTime);
		startTime.setHours(0, 0, 0, 0);
		var first = startTime;
		var second = new Date(self.process.endTime);
		var oneMonth = new Date(first);
		oneMonth.setMonth(first.getMonth() + 1);
		if (second < oneMonth) {
			second = oneMonth;
		}else{
			second.setDate(second.getDate() + 2);
		}
		var months = [];
		var days = [];
		var daysInMonth = 0;
		var dayWidth = factor.majorFactorWidth;
		var currentMonth = first.getMonth();
		self.minorTimeFrameWidth = dayWidth;
		while (second > first) {


			if (currentMonth < first.getMonth()) {
				var record = new Date(first);
				record.setMonth(first.getMonth() - 1);
				self.majorTimeFrames.push({
					width : (daysInMonth * dayWidth) + (daysInMonth - 1),
					value : record
				});
				daysInMonth = 0;
				currentMonth = first.getMonth();
			}

			self.minorTimeFrames.push({
				value : new Date(first)
			});
			daysInMonth = daysInMonth + 1;
			first.setDate(first.getDate() + 1);
		}
		self.majorTimeFrames.push({
			width : (daysInMonth * dayWidth) + (daysInMonth - 1),
			value : new Date(first)
		});
		//self.majorTimeFrames = months;
		//self.minorTimeFrames = days;
	};


	Controller.prototype.drawTimeFrameHours = function() {

		var factor = FACTORS.hours;
		var self = this;
		var startTime = new Date(self.process.startTime);
		startTime.setMinutes(0, 0, 0);
		var first = startTime;
		var second = new Date(self.process.endTime);
		var nextDay = new Date(first);
		nextDay.setDate(first.getDate() + 1);
		if (second < nextDay) {
			second = nextDay;
		}else{
			second.setHours(second.getHours() + 3);
		}
		
		
		var hours = [];
		var days = [];
		var hoursInDay = 0;
		var hourWidth = factor.majorFactorWidth;
		var currentDay = first.getDate();
		self.minorTimeFrameWidth = hourWidth;
		while (second > first) {
			if (currentDay < first.getDate()) {
				var record = new Date(first);
				record.setDate(first.getDate() - 1);
				self.majorTimeFrames.push({
					width : (hoursInDay * hourWidth) + (hoursInDay -1 ),
					value : record
				});
				hoursInDay = 0;
				currentDay = first.getDate();
			}

			self.minorTimeFrames.push({
				value : first.getHours()
			});
			hoursInDay = hoursInDay + 1;
			first.setHours(first.getHours() + 1);
		}
		self.majorTimeFrames.push({
			width : (hoursInDay * hourWidth) + (hoursInDay - 1),
			value : first
		});

	};

	var one_day = 86400000
	var one_hour = 3600000


	var minsArray = ["00","15","30","45"];

	Controller.prototype.drawTimeFrameMinutes = function(startTime) {
		var self = this;
		var first = startTime;
		var second = new Date(self.process.endTime);
		
		var factor = FACTORS.minutes;
		
		var minDuration = first.getTime() + 4 * one_hour;
		if (second.getTime() < minDuration) {
			second.setTime(minDuration);
		}else{
			second.setTime(second.getTime() + (one_hour/2));
		}
		var quaterHours = [];
		var days = [];
		var quaterHoursInADay = 0;
		var quaterHourWidth = factor.majorFactorWidth;
		var currentDay = first.getDate();
		self.minorTimeFrameWidth = quaterHourWidth;
		while (second > first) {
			
			if (currentDay < first.getDate()) {
				var record = new Date(first);
				record.setDate(first.getDate() - 1);
				self.majorTimeFrames.push({
					width : (quaterHoursInADay * quaterHourWidth) + (quaterHoursInADay -1 ),
					value : record
				});
				quaterHoursInADay = 0;
				currentDay = first.getDate();
			}

			for (var index = 0; index < minsArray.length; index++) {
				self.minorTimeFrames.push({
					value : first.getHours() + ":"+minsArray[index]
				});
				quaterHoursInADay = quaterHoursInADay + 1;
			}
			
			first.setTime(first.getTime() + one_hour);
		}
		self.majorTimeFrames.push({
			width : (quaterHoursInADay * quaterHourWidth) + (quaterHoursInADay - 1),
			value : first
		});
	};


	Controller.prototype.computeChart = function(factor, startTime) {
		var self = this;

		self.columnData = [];
		var minorFactorWidth = factor.minorFactorWidth;
		angular.forEach(self.getProcessData(), function(item) {

			var delay = self.computeDifferenceForDays(startTime.getTime(),
					item.startTime, factor);
			var inflightLength = null;

			if(!item.endTime) {
				item.endTime = new Date().getTime();
			}
			var completedLength = self.computeDifferenceForDays(item.startTime,
					item.endTime, factor)
					if(item.predictedEndTime) {
						inflightLength  = self.computeDifferenceForDays(item.endTime,
								item.predictedEndTime, factor);
					}		

			self.columnData.push({
				name : item.name,
				type : item.type,
				startTime : item.startTime,
				endTime : item.endTime,
				predictedEndTime : item.predictedEndTime,
				delay : (delay.difference * minorFactorWidth)  + delay.padding,
				completed : completedLength.difference * minorFactorWidth + completedLength.padding,
				inflight : (inflightLength)
				? (inflightLength.difference * minorFactorWidth) + inflightLength.padding
						: 0,
						color : item.color,
						status : item.status
			});

		});
	};

	Controller.prototype.computeDifferenceForDays = function(startTime,
			endTime, factor) {
		var differenceInFactor = (endTime - startTime) / factor.minorFactor;
		var correctionForPadding = parseInt((endTime - startTime)
				/ factor.majorFactor);
		return {difference : differenceInFactor, padding : correctionForPadding} ;
	};
	
	Controller.prototype.mouseEnter = function(event, xOffset) {
		var self = this;
		if(!xOffset) {
			xOffset = 0;
		}
		self.position = event.offsetX +xOffset - 100 - $("#granttChart").scrollLeft();
	};

	Controller.prototype.onTimeFrameChange = function() {
		var self = this;
		self.columnData = [];
		self.majorTimeFrames = [];
		self.minorTimeFrames = [];
		var start = new Date(self.process.startTime);
		var time = new Date()
		if (self.selectedTimeFrame == "minutes") {
			start.setMinutes(0, 0, 0);
			self.computeChart(FACTORS.minutes, start);
			console.log((new Date().getTime() - time.getTime()))
			self.drawTimeFrameMinutes(start);
		}
		if (self.selectedTimeFrame == "hours") {
			start.setMinutes(0, 0, 0);
			self.computeChart(FACTORS.hours, start);
			console.log((new Date().getTime() - time.getTime()))
			self.drawTimeFrameHours();

		} else if (self.selectedTimeFrame == "days") {
			start.setHours(0, 0, 0, 0);
			self.computeChart(FACTORS.days, start);
			console.log((new Date().getTime() - time.getTime()))
			self.drawTimeFrameDays();
		}

		console.log((new Date().getTime() - time.getTime()))
	}

})();