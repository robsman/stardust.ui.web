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
			'sdGanttChartCtrl',
			['sdProcessInstanceService', 'sdLoggerService', '$filter',
			 'sgI18nService','sdActivityInstanceService','sdCommonViewUtilService',Controller]);

	var _filter = null;
	var _sdActivityInstanceService = null;
	var totalWidth = 900;

	
	

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
	
	
	var statuses = [{
		label : "Complete",
		color : '#AAAAAA',
		aiValue : 2,
		piValue : 2
	},
	{
		label : "Alive",
		color : '#00CD66',
		aiValue : 0,
		piValue : 0
	},
	{
		label : "Aborted",
		color : 'red',
		aiValue : 6,
		piValue : 1
	},
	{
		label : "Interupted",
		color : '#CD661D',
		aiValue : 4,
		piValue : 3
	},
	{
		label : "Suspended",
		color : '#1E90FF',
		aiValue : 5,
		piValue : ''
	},
	{
		label : "Hibernated",
		color : '#EEEE00',
		aiValue : 7,
		piValue : ''
	},
	{
		label : "Application",
		color : '#8A2BE2',
		aiValue : 1,
		piValue : ''
	}]
	
	
	var _sdProcessInstanceService = null;
	var _sdCommonViewUtilService = null;
	
	/**
	 * 
	 */
	function Controller(sdProcessInstanceService, sdLoggerService, $filter,
			sdPreferenceService, sdActivityInstanceService, sdCommonViewUtilService ) {
		_filter = $filter;
		_sdProcessInstanceService = sdProcessInstanceService;
		_sdActivityInstanceService = sdActivityInstanceService;
		_sdCommonViewUtilService  = sdCommonViewUtilService ;
		this.intialize();

	};


	Controller.prototype.getTimeFrames = function() {
		return timeFrames;
	}
	
	
	Controller.prototype.populateLegendCategories = function(benchmarkPresent,  benchmarkName) {
		var self = this;
		self.legendCategories = [];
		var status = 	{
				id : "status",
				label : "Status"
		};

		self.legendCategories.push(status)

		if(benchmarkPresent) {
			var benchmark = 	{
					id : "benchmark",
					label : benchmarkName
			}	
		self.legendCategories.push(benchmark)	
		self.selected.legend = "benchmark";
		}else {
			self.selected.legend = "status";
		}
	}
	
	
	
	
	
	Controller.prototype.getProcessAndDrawChart = function() {
		var self = this;
		self.data.list = [];
		_sdProcessInstanceService.getProcessByOid(self.selected.process).then(function(data){
			self.process = data;
			self.addProcessToChartData(self.process);
			self.benchmarkCategories = [];
			
			if(data.benchmark.value > 0 ){
				_sdProcessInstanceService.getBenchmarkCategories(data.benchmark.oid).then(function(data){ 
				
					var complete = {
						label : "Complete",
						color: "#AAAAAA"
					}
					self.benchmarkCategories.push(complete);
					
					angular.forEach(data.categories,function(cat){
						var d = {
								label : cat.name,
								color: cat.color
						}
						self.benchmarkCategories.push(d);
					});
					
					self.populateLegendCategories(true, data.name);
				});
			}else{
				self.populateLegendCategories(false, null)
			}
		
			_sdActivityInstanceService.getByProcessOid(self.process.oid).then(function(activityList){ 
				self.addActivitiesToChartData(activityList);
				self.onLegendChange();
				self.onTimeFrameChange();
			});

		});
	}
	
	
	
	Controller.prototype.addActivitiesToChartData = function(activityList) { 
		var self = this;
		angular.forEach(activityList, function(activity){
			var statusColor = self.getBarColor(activity.status.value,"Activity", false);
			var bColor = self.getBarColor(activity,"Activity", true);
			var data = {
					name : activity.activity.name,
					startTime :  activity.startTime,
					endTime :  activity.lastModification,
					sColor : statusColor,
					bColor : bColor,
					activatable : activity.activatable,
					status : activity.status,
					oid : activity.activityOID,
					benchmarkCategory : activity.benchmark
			}
			self.data.list.push(data)
		});
	};
	
	Controller.prototype.addProcessToChartData = function(piData) { 
		var self = this;
		var statusColor = self.getBarColor(piData.status.value,"Process",false);
		var bColor = self.getBarColor(piData,"Process", true);
		
		var data = {
				name : piData.processName,
				startTime :  piData.startTime,
				endTime :  piData.endTime,
				predictedEndTime :   piData.predictedEndTime,
				sColor : statusColor,
				bColor : bColor,
				activatable : piData.activatable,
				status : piData.status,
				benchmarkCategory : piData.benchmark
		}
		self.data.list.push(data)
	};
	
	
	Controller.prototype.getBarColor = function(value,type,hasBenchmark) {
		var self = this;
		var color = "";
		var found = ""
		if (!hasBenchmark) {
			if(type == "Process") {
			  found = _filter("filter")(self.legends,{piValue : value} , true)
			}else {
			  found = _filter("filter")(self.legends,{aiValue : value} , true)
			}
			
			if(found){
				color = found[0].color;
			}
			
		}else {
			if(value.benchmark.color){
				color = value.benchmark.color;
			} else{
				
				color =  self.getBarColor(value.status.value,type, false)
			}
		}
		return color;
	};
	
	/**
	 * 
	 */
	Controller.prototype.intialize = function() {
		var self = this;
		
		self.selected = {
				process : "",
				legend : "status",
				timeFrame : "days"
		}
		self.data = {
				list : []
		};

		self.process = {};
		self.legends = [];
		self.benchmarkCategories = [];
		self.onLegendChange();
		self.timeFrames = self.getTimeFrames();
		self.selectedTimeFrame = "days";
		self.selectedCategory = "benchmark";

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

	Controller.prototype.drawTimeFrameDays = function(startTime, endTime) {
		var factor = FACTORS.days;
		var self = this;
		var first = startTime;
		var second = endTime;
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
		var current = new Date(first);
		self.minorTimeFrameWidth = dayWidth;
		while (second > first) {

			if (isNextMonth(current,first)) {
				var record = new Date(first);
				record.setMonth(first.getMonth() - 1);
				self.majorTimeFrames.push({
					width : (daysInMonth * dayWidth) + (daysInMonth - 1),
					value : record
				});
				daysInMonth = 0;
				current =new Date( first);
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
	};


	Controller.prototype.drawTimeFrameHours = function(startTime, endTime) {

		var factor = FACTORS.hours;
		var self = this;
		var first = startTime;
		var second = endTime;
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
		var currentDay = new Date(first);
		self.minorTimeFrameWidth = hourWidth;
		while (second > first) {
			if (isNextDay(currentDay,first)) {
				var record = new Date(first);
				record.setDate(first.getDate() - 1);
				self.majorTimeFrames.push({
					width : (hoursInDay * hourWidth) + (hoursInDay -1 ),
					value : record
				});
				hoursInDay = 0;
				currentDay = new Date(first);
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

	Controller.prototype.drawTimeFrameMinutes = function(startTime, endTime) {
		var self = this;
		var first = startTime;
		var second = endTime;
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
		var currentDay = new Date(first);
		self.minorTimeFrameWidth = quaterHourWidth;
		while (second > first) {
			if (isNextDay(currentDay,first)) {
				var record = new Date(first);
				record.setDate(first.getDate() - 1);
				self.majorTimeFrames.push({
					width : (quaterHoursInADay * quaterHourWidth) + (quaterHoursInADay -1 ),
					value : record
				});
				quaterHoursInADay = 0;
				currentDay = new Date(first);
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
		
		angular.forEach(self.data.list , function(item) {

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

			
			var delay = (delay.difference * minorFactorWidth)  + delay.padding;
			var completed =  completedLength.difference * minorFactorWidth + completedLength.padding;
			if( completedLength.difference < 1){
				completed = 2;
			}
			
			var inflight = 0;
			if(inflightLength) {
					 inflight = (inflightLength.difference * minorFactorWidth) + inflightLength.padding;
					if(inflightLength.difference < 1){
						inflight = 2;
					}
			}
			
			var status = item.status;
			if(self.selected.legend == "benchmark" && item.benchmarkCategory.label) {
				status = item.benchmarkCategory;
			}
			
			
			self.columnData.push({
				name : item.name,
				type : item.type,
				startTime : item.startTime,
				endTime : item.endTime,
				predictedEndTime : item.predictedEndTime,
				delay : delay,
				completed : completed,
				inflight : inflight,
				color : (self.selected.legend == "status") ? item.sColor : item.bColor,
				status :  status,
				activatable : item.activatable,
				oid : item.oid
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
	
	
	
	Controller.prototype.onLegendChange = function() {
		var self = this;
			switch (self.selected.legend ) {
				case "status":
					self.legends = statuses;
					break;
				case "benchmark":
					self.legends = self.benchmarkCategories;
					break;
			}
			self.onTimeFrameChange();
	};
	
	Controller.prototype.activate = function(col) {
		_sdActivityInstanceService.activate(col.oid).then(
				function(result) {
					_sdCommonViewUtilService.openActivityView(col.oid);
				});
	};
	
	Controller.prototype.onTimeFrameChange = function() {
		var self = this;
		self.columnData = [];
		self.majorTimeFrames = [];
		self.minorTimeFrames = [];
		var start = new Date(self.process.startTime);
		
		var end = new Date();
		if(self.process.endTime) {
			end = new Date(self.process.endTime);
		}
		else if(self.process.predictedEndTime) {
			end = new Date(self.process.predictedEndTime);
		}

		switch (self.selected.timeFrame) {
			case "minutes":
				start.setMinutes(0, 0, 0);
				self.computeChart(FACTORS.minutes, start);
				self.drawTimeFrameMinutes(start, end);
				break;
			case "hours":
				start.setMinutes(0, 0, 0);
				self.computeChart(FACTORS.hours, start);
				self.drawTimeFrameHours(start, end);
				break;
			case "days":
				start.setHours(0, 0, 0, 0);
				self.computeChart(FACTORS.days, start);
				self.drawTimeFrameDays(start, end);
				break;
		}
	};
	
	
	function isNextDay(timeOne, timeTwo){
		
		if(timeOne.getYear() < timeTwo.getYear()) {
			return true;
		}
		if(timeOne.getYear() == timeTwo.getYear()) {
			if(timeOne.getMonth() < timeTwo.getMonth()) {
				return true;
			}
			else if(timeOne.getMonth() == timeTwo.getMonth()) {
				return timeOne.getDate() < timeTwo.getDate()
			}
		}
		return false;
	}
	
	function isNextMonth(timeOne, timeTwo){
		if(timeOne.getYear() < timeTwo.getYear()) {
			return true;
		}
		if(timeOne.getYear() == timeTwo.getYear()) {
			if(timeOne.getMonth() < timeTwo.getMonth()) 
				return true;
		}
		return false;
	}
	

})();