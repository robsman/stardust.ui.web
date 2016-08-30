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
		'sdGanttChartCtrl', ['$scope', '$parse', '$q', '$filter',
							 'sdProcessInstanceService', 'sdLoggerService',
							 'sdActivityInstanceService', 'sdCommonViewUtilService', 'sgI18nService',
							 'sdLocalizationService', 'sdGanttChartService', 
							 Controller
		]);

	var _filter = null;
	var _sdActivityInstanceService = null;
	var TOTAL_WIDTH = 1000;
	var _sgI18nService = null;
	var _q = null;
	var _sdProcessInstanceService = null;
	var _sdCommonViewUtilService = null;
	var _sdLocalizationService = null;
	var _sdGanttChartService = null;
	var trace = null;
	var _parse = null;

	var FINISHED_STATUSES = [2, 6];
	var ONE_DAY_IN_MIILS = 86400000;
	var ONE_HOUR_IN_MIILS = 3600000;
	var MINUTES_LABEL_IN_HOUR = ["00", "15", "30", "45"];

	var FACTORS = {
		days: {
			majorFactorWidth: TOTAL_WIDTH / 30, // 30 days in  a month
			minorFactorWidth: TOTAL_WIDTH / (30 * 24), // 24 hours in a day
			minorFactor: (1000 * 60 * 60), // 1 hour
			majorFactor: (1000 * 60 * 60 * 24) // 1 Day
		},
		hours: {
			majorFactorWidth: TOTAL_WIDTH / 24, // 24 hours in a day
			minorFactorWidth: TOTAL_WIDTH / (24 * 60), // 60 mins in a hour
			minorFactor: (1000 * 60), // 1 Min
			majorFactor: (1000 * 60 * 60) // 1 Hour
		},
		minutes: {
			majorFactorWidth: TOTAL_WIDTH / 15,
			minorFactorWidth: TOTAL_WIDTH / (15 * 15), // 15 mins
			minorFactor: (1000 * 60), // 1 Min
			majorFactor: (1000 * 60 * 15) // 15 mins
		}
	};

	/**
	 *
	 */
	function Controller($scope, $parse, $q, $filter, sdProcessInstanceService, sdLoggerService,
		sdActivityInstanceService, sdCommonViewUtilService, sgI18nService,
		sdLocalizationService, sdGanttChartService) {
		_filter = $filter;
		_sdProcessInstanceService = sdProcessInstanceService;
		_sdActivityInstanceService = sdActivityInstanceService;
		_sdCommonViewUtilService = sdCommonViewUtilService;
		_sgI18nService = sgI18nService;
		_q = $q;
		_sdLocalizationService = sdLocalizationService;
		trace = sdLoggerService.getLogger('html5-bcc-ui.sdGanttChartCtrl');
		_parse = $parse;
		_sdGanttChartService = sdGanttChartService;
		this.intialize($scope);
	}

	/**
	 *
	 */
	Controller.prototype.intialize = function($scope) {
		var self = this;
		self.selected = {
			process: "",
			legend: "status",
			timeFrame: "days",
			hideAuxilary: true
		};
		self.data = {
			list: []
		};
		self.process = {};
		self.legends = [];
		self.benchmarkCategories = [];
		self.majorTimeFrames = null;
		self.minorTimeFrames = null;
		self.timeFormat = _sdLocalizationService.getInfo().dateTimeFormat;
		self.showAuxiliary = false;
		self.timeFrames = self.getTimeFrames();
		self.dataTable = null;
		self.tableReady = false;

		//Get the process oid from the params
		var queryGetter = _parse("panel.params.custom");
		var params = queryGetter($scope);
		self.selected.process = params.processInstanceOId;
		self.tableReady = true;

		self.currentTimeLine = {};
		self.estimatedEndTimeLine = {};
		self.elapsedEstimatedLine = {};
		self.showProcessDescriptor = false;
		self.dataExpanded = false;
	};

	/**
	 *
	 */
	Controller.prototype.populateLegendCategories = function(benchmarkPresent, benchmarkName) {
		var self = this;
		self.legendCategories = [];
		var status = {
			id: "status",
			label: _sgI18nService.translate('business-control-center-messages.views-common-column-status'),
		};

		self.legendCategories.push(status);

		if (benchmarkPresent) {
			var benchmark = {
				id: "benchmark",
				label: benchmarkName
			};
			self.legendCategories.push(benchmark);
			self.selected.legend = "benchmark";
			self.legends = self.benchmarkCategories;

		} else {
			self.selected.legend = "status";
			self.legends = self.getAvailableStatuses();
		}
	};

	/**
	 *
	 */
	Controller.prototype.getBenchmarkCategories = function(data) {
		var deferred = _q.defer();
		var self = this;

		var benchmarkPresent = data.benchmark ? data.benchmark.value > 0 : false;
		if (benchmarkPresent) {

			_sdProcessInstanceService.getBenchmarkDetailsByBenchmarkOid(data.benchmark.oid).then(function(data) {
				self.extractExpectedDurations(data.processDefinitions);
				angular.forEach(data.categories, function(cat) {
					var d = {
						label: cat.name,
						color: cat.color
					};
					self.benchmarkCategories.push(d);
				});

				self.populateLegendCategories(true, data.name);
				deferred.resolve();
			});
		} else {
			self.populateLegendCategories(false, null);
			deferred.resolve();
		}

		return deferred.promise;
	};

	/**
	 *
	 */
	Controller.prototype.extractExpectedDurations = function(processDefinitions) {
		var self = this;
		var durations = {};
		angular.forEach(processDefinitions, function(process) {
			durations[process.id] = { expectedDuration: process.expectedDuration, activities: {} };

			angular.forEach(process.activities, function(activity) {
				durations[process.id].activities[activity.id] = {
						expectedDuration : activity.expectedDuration
				};
			});
		});
		self.expectedDurations = durations;
	};

	/**
	 *
	 */
	Controller.prototype.getExpectedDurationForProcess = function(pQid) {
		var self = this;
		if (self.expectedDurations) {

			if (self.expectedDurations[pQid]) {
				return self.expectedDurations[pQid].expectedDuration;
			}

		}
		return undefined;
	};

	/**
	 *
	 */
	Controller.prototype.getExpectedDurationForActivity = function(pQid, aid) {
		var self = this;
		if (self.expectedDurations && self.expectedDurations[pQid] && self.expectedDurations[pQid].activities[aid]) {
			return self.expectedDurations[pQid].activities[aid].expectedDuration;
		}
		return 0;
	};

	/**
	 *
	 */
	Controller.prototype.getBarColor = function(value, type, hasBenchmark) {
		var self = this;
		var color = "";
		var found = "";
		if (!hasBenchmark) {
			var statuses = self.getAvailableStatuses();

			if (type == "process") {
				found = _filter("filter")(statuses, { piValue: value }, true);
			} else {
				found = _filter("filter")(statuses, { aiValue: value }, true);
			}

			if (found && found[0] !== undefined) {
				color = found[0].color;
			}

		} else {
			if(value.benchmark && value.benchmark.color) {
				color = value.benchmark.color;
			} else {
				color = self.getBarColor(value.status.value, type, false);
			}
		}
		return color;
	};

	/**
	 *
	 */
	Controller.prototype.getTimeFrames = function() {

		return [{
			id: "minutes",
			label: _sgI18nService.translate('portal-common-messages.portalFramework-humanDate-MINUTES', "Minutes")
		}, {
			id: "hours",
			label: _sgI18nService.translate('portal-common-messages.portalFramework-humanDate-HOURS', "Hours")
		}, {
			id: "days",
			label: _sgI18nService.translate('portal-common-messages.portalFramework-humanDate-DAYS', "Days")
		}];
	};

	/**
	 *
	 */
	Controller.prototype.getAvailableStatuses = function() {

		return [{
			label: _sgI18nService
				.translate('views-common-messages.views-activityTable-statusFilter-completed'),
			color: '#AAAAAA',
			aiValue: 2,
			piValue: 2
		}, {
			label: _sgI18nService
				.translate('views-common-messages.views-processTable-statusFilter-active'),
			color: '#00CD66',
			aiValue: 0,
			piValue: 0
		}, {
			label: _sgI18nService
				.translate('views-common-messages.views-activityTable-statusFilter-aborted'),
			color: 'red',
			aiValue: 6,
			piValue: 1
		}, {
			label: _sgI18nService
				.translate('views-common-messages.views-activityTable-statusFilter-interrupted'),
			color: '#CD661D',
			aiValue: 4,
			piValue: 3
		}, {
			label: _sgI18nService
				.translate('views-common-messages.views-activityTable-statusFilter-suspended'),
			color: '#1E90FF',
			aiValue: 5,
			piValue: ''
		}, {
			label: _sgI18nService
				.translate('views-common-messages.views-activityTable-statusFilter-hibernated'),
			color: '#EEEE00',
			aiValue: 7,
			piValue: ''
		}, {
			label: _sgI18nService
				.translate('views-common-messages.views-activityTable-statusFilter-application'),
			color: '#8A2BE2',
			aiValue: 1,
			piValue: ''
		}, {
			label: _sgI18nService
				.translate('views-common-messages.views-activityTable-statusFilter-halted'),
			color: '#EFAE39',
			aiValue: 10,
			piValue: 6
		}];
	};

	/**
	 */
	Controller.prototype.drawTimeFrameDays = function(startTime, endTime) {
		var factor = FACTORS.days;
		var self = this;
		var first = startTime;
		var second = endTime;
		var oneMonth = new Date(first);
		oneMonth.setMonth(first.getMonth() + 1);
		if (second < oneMonth) {
			second = oneMonth;
		} else {
			second.setDate(second.getDate() + 2);
		}
		var daysInMonth = 0;
		var dayWidth = factor.majorFactorWidth;
		var current = new Date(first);
		self.minorTimeFrameWidth = dayWidth;
		var temptableHolder = '';

		while (second > first) {

			if (isNextMonth(current, first)) {
				var record = new Date(first);
				record.setMonth(first.getMonth() - 1);
				self.majorTimeFrames.push({
					width: (daysInMonth * dayWidth) - (self.majorTimeFrames > 0 ? 1 :0),
					value: record
				});
				daysInMonth = 0;
				current = new Date(first);
			}
			temptableHolder = temptableHolder + '<span class="minorTimeLine" style="width :' + self.minorTimeFrameWidth + 'px;">' + new Date(first).getDate() + '</span>';
			self.minorTimeFrames.push({
				value: new Date(first)
			});
			daysInMonth = daysInMonth + 1;
			first.setDate(first.getDate() + 1);
		}
		document.getElementById("minorTimeLine").innerHTML = temptableHolder;

		//If the first has been incremented to the 1st of next month .Show the major tool bar with the previous month
		if (first.getDate() == 1) {
			first.setDate(first.getDate() - 1);
		}
		self.majorTimeFrames.push({
			width: (daysInMonth * dayWidth),
			value: new Date(first)
		});
	};

	/**
	 *
	 */
	Controller.prototype.drawTimeFrameHours = function(startTime, endTime) {

		var factor = FACTORS.hours;
		var self = this;
		var first = startTime;
		var second = endTime;
		var nextDay = new Date(first);
		nextDay.setDate(first.getDate() + 1);
		if (second < nextDay) {
			second = nextDay;
			second.setHours(second.getHours() + 1);
		} else {
			second.setHours(second.getHours() + 3);
		}
		var hoursInDay = 0;
		var hourWidth = factor.majorFactorWidth;
		var currentDay = new Date(first);
		self.minorTimeFrameWidth = hourWidth;
		var temptableHolder = '';

		while (second > first) {
			if (isNextDay(currentDay, first)) {
				var record = new Date(first);
				record.setDate(first.getDate() - 1);
				self.majorTimeFrames.push({
					width: (hoursInDay * hourWidth) - (self.majorTimeFrames > 0 ? 1 :0),
					value: record
				});
				hoursInDay = 0;
				currentDay = new Date(first);
			}

			self.minorTimeFrames.push({
				value: first.getHours()
			});
			temptableHolder = temptableHolder + '<span class="minorTimeLine" style="width :' + self.minorTimeFrameWidth + 'px;">' + first.getHours() + '</span>';
			hoursInDay = hoursInDay + 1;
			first.setHours(first.getHours() + 1);
		}

		document.getElementById("minorTimeLine").innerHTML = temptableHolder;
		self.majorTimeFrames.push({
			width: (hoursInDay * hourWidth),
			value: first
		});

	};

	/**
	 *
	 */
	Controller.prototype.drawTimeFrameMinutes = function(startTime, endTime) {
		var self = this;
		var first = startTime;
		var second = endTime;
		var factor = FACTORS.minutes;
		var temptableHolder = '';


		var minDuration = first.getTime() + 4 * ONE_HOUR_IN_MIILS;
		if (second.getTime() < minDuration) {
			second.setTime(minDuration);
		} else {
			second.setTime(second.getTime() + (ONE_HOUR_IN_MIILS * 2));
		}
		var quaterHoursInADay = 0;
		var quaterHourWidth = factor.majorFactorWidth;
		var currentDay = new Date(first);
		self.minorTimeFrameWidth = quaterHourWidth;

		while (second > first) {
			if (isNextDay(currentDay, first)) {
				var record = new Date(first);
				record.setDate(first.getDate() - 1);
				self.majorTimeFrames.push({
					width: (quaterHoursInADay * quaterHourWidth) - (self.majorTimeFrames > 0 ? 1 :0),
					value: record
				});
				quaterHoursInADay = 0;
				currentDay = new Date(first);
			}

			for (var index = 0; index < MINUTES_LABEL_IN_HOUR.length; index++) {
				self.minorTimeFrames.push({
					value: first.getHours() + ":" + MINUTES_LABEL_IN_HOUR[index]
				});
				quaterHoursInADay = quaterHoursInADay + 1;
				temptableHolder = temptableHolder + '<span class="minorTimeLine" style="width :' + self.minorTimeFrameWidth + 'px;">' + first.getHours() + ":" + MINUTES_LABEL_IN_HOUR[index] + '</span>';
			}

			first.setTime(first.getTime() + ONE_HOUR_IN_MIILS);
		}

		document.getElementById("minorTimeLine").innerHTML = temptableHolder;
		self.majorTimeFrames.push({
			width: (quaterHoursInADay * quaterHourWidth),
			value: first
		});
	};

	/**
	 *
	 */
	Controller.prototype.computeDifference = function(startTime,
		endTime, factor) {
		var differenceInFactor = (endTime - startTime) / factor.minorFactor;
		var correctionForPadding = parseInt((endTime - startTime) / factor.majorFactor);
		return { difference: differenceInFactor, padding: correctionForPadding };
	};
	/**
	 *
	 */
	Controller.prototype.mouseEnter = function(event, xOffset) {
		var self = this;
		if (!xOffset) {
			xOffset = 0;
		}
		self.position = event.offsetX + xOffset - 100 - $("#ganttChart").scrollLeft();
	};


	/**
	 *
	 */
	Controller.prototype.onLegendChange = function() {
		var self = this;
		switch (self.selected.legend) {
			case "status":
				self.legends = self.getAvailableStatuses();
				break;
			case "benchmark":
				self.legends = self.benchmarkCategories;
				break;
		}
	};

	/**
	 *
	 */
	Controller.prototype.activate = function(col) {
		_sdActivityInstanceService.activate(col.oid).then(
			function() {
				_sdCommonViewUtilService.openActivityView(col.oid);
			});
	};

	/**
	 *
	 */
	Controller.prototype.onTimeFrameChange = function() {
		var self = this;
		self.drawTimeFrames();
	};


	/**
	 *
	 */
	Controller.prototype.drawTimeFrames = function() {
		var self = this;
		self.majorTimeFrames = [];
		self.minorTimeFrames = [];

		var start = self.getStartTime();
		var end = new Date();

		if (self.process.endTime) {
			end = new Date(self.process.endTime);
		} else if (self.process.expectedEndTime) {
			if (new Date(self.process.expectedEndTime) > end) {
				end = new Date(self.process.expectedEndTime);
			}
		}

		switch (self.selected.timeFrame) {
			case "minutes":
				self.drawTimeFrameMinutes(start, end);
				break;
			case "hours":
				self.drawTimeFrameHours(start, end);
				break;
			case "days":
				self.drawTimeFrameDays(start, end);
				break;
		}
	};

	/**
	 *
	 */
	function isNextDay(timeOne, timeTwo) {

		if (timeOne.getYear() < timeTwo.getYear()) {
			return true;
		}
		if (timeOne.getYear() == timeTwo.getYear()) {
			if (timeOne.getMonth() < timeTwo.getMonth()) {
				return true;
			} else if (timeOne.getMonth() == timeTwo.getMonth()) {
				return timeOne.getDate() < timeTwo.getDate();
			}
		}
		return false;
	}
	/**
	 *
	 */
	function isNextMonth(timeOne, timeTwo) {
		if (timeOne.getYear() < timeTwo.getYear()) {
			return true;
		}
		if (timeOne.getYear() == timeTwo.getYear()) {
			if (timeOne.getMonth() < timeTwo.getMonth())
				return true;
		}
		return false;
	}

	/**
	 *
	 */
	Controller.prototype.expandAll = function () {
		var self = this;
		var dataCallRequired = false;  // true if expand all already called.

		//Check if indiviual nodes are loaded
		if(!this.dataExpanded) {
			angular.forEach(this.dataTable.getData(),function(node){
				if(node.type =="process" && node.$$treeInfo && node.$$treeInfo.loaded == false) {
					dataCallRequired = true;
				}
			});
		}

		if (dataCallRequired) {   //Fetching all the data
			this.fetchAll = true;
			this.dataTable.refresh();
		} else { 	// UI Expand all
			this.dataTable.expandAll();
		}
		this.dataExpanded = true;
	};


	/**
	 *
	 */
	Controller.prototype.collapseAll = function() {
		this.dataTable.collapseAll();
	};

	/**
	 *
	 */
	Controller.prototype.toggleAuxiliary = function() {
		this.showAuxiliary = !this.showAuxiliary;
		this.dataTable.refreshUi();
	};

	/**
	 *
	 */
	Controller.prototype.getAuxTitle = function() {
		if (this.showAuxiliary) {
			return "Hide auxiliary";
		}
		return "Show auxiliary";
	};

	/**
	 *
	 */
	Controller.prototype.auxFilter = function(rowData) {
		if (this.showAuxiliary) {
			return true;
		} else {
			return !rowData.auxiliary;
		}

	};
	
	/**
	 * 
	 */
	Controller.prototype.populateGraphData = function(process, expandAll) {
		var self = this;
		var expectedDuration = self.getExpectedDurationForProcess(process.qualifiedId);
		var statusColor = self.getBarColor(process.status.value, "process", false);
		var bColor = self.getBarColor(process, "process", true);
		
		process.expectedEndTime = expectedDuration ? (process.startTime + expectedDuration * ONE_HOUR_IN_MIILS) : 0;
		process.sColor = statusColor;
		process.bColor = bColor;
		process.$leaf = false;
		process.$expanded = expandAll ? true : false;
		
		this.addTimeFrameData(process);
		this.addChildGraphData(process, expandAll);
		this.plotGridLines(process);
	}

	/**
	 * 
	 */
	Controller.prototype.addChildGraphData = function (process, expandAll) {
		var self = this;
	
		if (process.children && process.children.length > 0) {
			angular.forEach(process.children, function (activity) {
				var activityId = activity.name.replace(/\s/g, "");
				var expectedDuraion = self.getExpectedDurationForActivity(process.qualifiedId, activityId);
				activity.sColor = self.getBarColor(activity.status.value, "Activity", false);
				activity.bColor = self.getBarColor(activity, "Activity", true);
				activity.expectedEndTime = expectedDuraion ? (activity.startTime + expectedDuraion * ONE_HOUR_IN_MIILS) : 0;
				self.addTimeFrameData(activity);

				if (activity.type == "process") {
					self.populateGraphData(activity, expandAll);
				}
			});
		}
	}
	
	/**
	 * 
	 */
	Controller.prototype.addTimeFrameData = function(data) {
		var timeData = {
				days :this.getGraphData(data, 'days'),
				hours : this.getGraphData(data, 'hours'),
				minutes : this.getGraphData(data, 'minutes')
		};
		
		data = angular.extend(data,timeData);
	}
	
	/**
	 * 
	 */
	Controller.prototype.plotGridLines = function(data) {
		this.calculateGridLines('days', data.days);
		this.calculateGridLines('hours', data.hours);
		this.calculateGridLines('minutes', data.minutes);
		this.determineAppropriateTimeFrame(data);
	}

	/**
	 */
	Controller.prototype.fetchData = function(params) {
		var self = this;
		var deferred = _q.defer();
		var data = {};
		var processOid = self.selected.process;
		
		if (this.fetchAll) {
			 trace.debug("Fetching the complete process tree for oid =>", processOid);
			_sdGanttChartService.getByProcess(processOid, true, true).then(function (ganttChartInfo) {
				self.dataExpanded = true;
				self.populateGraphData(ganttChartInfo, true);
				data.list = [ganttChartInfo];
				data.totalCount = data.list.length;
				deferred.resolve(data);
			});
		}
		else if (!params) {

			self.currentTime = new Date(); //Current Time Load once intially

			_sdGanttChartService.getByProcess(processOid, false, true).then(function (ganttChartInfo) {
				self.benchmarkCategories = [];
				var process = angular.extend({}, ganttChartInfo);
				process.children = [];  //Removing children as it is not needed here
				self.process = process;
				self.getBenchmarkCategories(process).then(function () {
					self.process.expectedEndTime = self.getExpectedDurationForProcess(process.qualifiedId)
												  * ONE_HOUR_IN_MIILS 
												  + process.startTime;
					self.drawTimeFrames();
					self.populateGraphData(ganttChartInfo, false);
					data.list = [ganttChartInfo];
					data.totalCount = data.list.length;
					ganttChartInfo.$leaf = false;
					ganttChartInfo.$expanded = true;
					deferred.resolve(data);
				});
			});

		}  else {
			trace.debug("Lazy loading children for parent =>", params.parent.oid);
			_sdGanttChartService.getByProcess(params.parent.oid, false, false).then(function (subProcessGanttChartInfo) {
				self.addChildGraphData(subProcessGanttChartInfo, false);
				data.list = subProcessGanttChartInfo.children;
				data.totalCount = data.list.length;
				deferred.resolve(data);
			});
		}

		self.fetchAll = false;
		return deferred.promise;
	};

	/**
	 *
	 */
	Controller.prototype.determineAppropriateTimeFrame = function(data) {

		if ((data.endTime - data.startTime) < ONE_HOUR_IN_MIILS) {
			this.selected.timeFrame = "minutes";
		} else if ((data.endTime - data.startTime) < ONE_DAY_IN_MIILS) {
			this.selected.timeFrame = "hours";
		} else {
			this.selected.timeFrame = "days";
		}
		this.onTimeFrameChange();
	};

	/**
	 *
	 */
	Controller.prototype.calculateGridLines = function(timeFrame, data) {
		var offset = 234;
		this.currentTimeLine[timeFrame] = data.delay + data.completed + offset;
		if (data.inflight> 0) {
			this.estimatedEndTimeLine[timeFrame] = data.delay + data.completed + data.inflight + offset;
			if(this.estimatedEndTimeLine[timeFrame] ==this.currentTimeLine[timeFrame] ) {
				this.estimatedEndTimeLine[timeFrame] = 0;
			}
		} else if(data.elapsed > 0 ) {
			this.elapsedEstimatedLine[timeFrame] = data.delay + data.elapsed  + offset;
		}
	};

	/**
	 *
	 */
	Controller.prototype.getClassForCompletedBar = function(length) {
		if (length < 1) {
			return "img-rounded";
		}
	};

	/**
	 *
	 */
	function getFactor(timeframe) {
		var factor = null;
		switch (timeframe) {
			case "minutes":
				factor = FACTORS.minutes;
				break;
			case "hours":
				factor = FACTORS.hours;
				break;
			case "days":
				factor = FACTORS.days;
				break;
		}
		return factor;
	}

	/**
	 *
	 */
	Controller.prototype.getStartTime = function(timeFrame) {
		var self = this;

		var start = new Date(self.process.startTime);
		switch (timeFrame) {
			case "minutes":
				start.setMinutes(0, 0, 0);
				break;
			case "hours":
				start.setMinutes(0, 0, 0);
				break;
			case "days":
				start.setHours(0, 0, 0, 0);
				break;
		}
		return start;
	};

	/**
	 *
	 */
	Controller.prototype.getChartData = function(rowData) {
		var self = this;
		switch (self.selected.timeFrame) {
			case "minutes":
				return rowData.minutes;
			case "hours":
				return rowData.hours;
			case "days":
				return rowData.days;
		}

		return rowData.days;
	};
	/**
	 *
	 */
	Controller.prototype.getRowColor = function(rowData) {
		var self = this;
		switch (self.selected.legend) {
			case "benchmark":
				return rowData.bColor;
			case "status":
				return rowData.sColor;
		}
		return rowData.sColor;
	};
	
	/**
	 *
	 */
	Controller.prototype.getLabel = function(rowData) {
		var self = this;
		if (self.selected.legend == 'benchmark') {
			if (rowData.benchmark.label) {
				return rowData.benchmark.label;
			}
		}
		return rowData.status.label;
	};
	/**
	 *
	 */
	Controller.prototype.getGraphData = function(item, timeFrame) {

		var self = this;

		self.columnData = [];
		var factor = getFactor(timeFrame);
		var startTime = self.getStartTime(timeFrame);
		var minorFactorWidth = factor.minorFactorWidth;

		var delay = self.computeDifference(startTime.getTime(),
			item.startTime, factor);
		var inflightLength = null;
		var elapsedLength = null;

		var status = item.status;
		if (self.selected.legend == "benchmark" && item.benchmark.label) {
			status = item.benchmark;
		}

		//If the item doesnt have a end time or a activity for a process which has stil not completed.
		if (!item.endTime || FINISHED_STATUSES.indexOf(item.status.value) < 0) {
			item.endTime = self.currentTime.getTime();
		}

		var completedLength = self.computeDifference(item.startTime,
			item.endTime, factor);
		
		if(item.expectedEndTime ) {
			if ( FINISHED_STATUSES.indexOf(item.status.value) < 0 && item.endTime < item.expectedEndTime) {
				inflightLength = self.computeDifference(item.endTime, item.expectedEndTime, factor);
			} else {
				elapsedLength =  self.computeDifference(item.startTime, item.expectedEndTime, factor);
			}
		}
		
		delay = (delay.difference * minorFactorWidth) - 1;
		var completed = completedLength.difference * minorFactorWidth;
		if (completedLength.difference < 2) {
			completed = 2;
		}

		var inflight = 0;
		var elapsed = 0;
		if (inflightLength) {
			inflight = (inflightLength.difference * minorFactorWidth) + inflightLength.padding;
			if (inflightLength.difference < 2) {
				inflight = 2;
			}
		}
		
		if(elapsedLength) {
			elapsed =  (elapsedLength.difference * minorFactorWidth) + elapsedLength.padding;
			if(elapsedLength.difference < 2) {
				elapsed = 2;
			}
		}
		 
		var graphData = {
			delay: delay,
			completed: completed,
			inflight: inflight,
			elapsed : elapsed
		};
		return graphData;
	};
	
	/**
	 * 
	 */
	Controller.prototype.setShowProcessDescriptors = function(){
		this.showProcessDescriptor = !this.showProcessDescriptor; 
	};

})();
