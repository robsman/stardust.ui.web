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
			['$scope','sdProcessInstanceService', 'sdLoggerService', '$filter',
			 'sgI18nService','sdActivityInstanceService','sdCommonViewUtilService','sgI18nService','$q','sdLocalizationService','$parse',Controller]);

	var _filter = null;
	var _sdActivityInstanceService = null;
	var TOTAL_WIDTH = 900;
	var _sgI18nService = null;
	var _q = null;
	var _sdProcessInstanceService = null;
	var _sdCommonViewUtilService = null;
	var _sdLocalizationService = null;
	var _sdLoggerService = null;
	var _parse = null;

	var FINISHED_STATUSES = [2,6];
	var ONE_DAY_IN_MIILS = 86400000
	var ONE_HOUR_IN_MIILS = 3600000
	var MINUTES_LABEL_IN_HOUR = ["00","15","30","45"];

	var FACTORS = {
			days : {
				majorFactorWidth : TOTAL_WIDTH / 30,  // 30 days in  a month
				minorFactorWidth : TOTAL_WIDTH / (30 * 24), // 24 hours in a day
				minorFactor : (1000 * 60 * 60 ),  // 1 hour
				majorFactor : (1000 * 60 * 60 * 24) // 1 Day
			},
			hours : {
				majorFactorWidth : TOTAL_WIDTH / 24, // 24 hours in a day
				minorFactorWidth : TOTAL_WIDTH / (24 * 60), // 60 mins in a hour
				minorFactor : (1000 * 60 ), // 1 Min
				majorFactor : (1000 * 60 * 60) // 1 Hour
			},
			minutes : {
				majorFactorWidth : TOTAL_WIDTH / 15,  
				minorFactorWidth : TOTAL_WIDTH / (15 * 15), // 15 mins
				minorFactor : (1000 * 60 ), // 1 Min
				majorFactor : (1000 * 60  * 15)// 15 mins
			}
	}

	/**
	 * 
	 */
	function Controller($scope, sdProcessInstanceService, sdLoggerService, $filter,
			sdPreferenceService, sdActivityInstanceService, sdCommonViewUtilService, sgI18nService, $q, sdLocalizationService, $parse) {
		_filter = $filter;
		_sdProcessInstanceService = sdProcessInstanceService;
		_sdActivityInstanceService = sdActivityInstanceService;
		_sdCommonViewUtilService  = sdCommonViewUtilService ;
		_sgI18nService = sgI18nService
		_q = $q;
		_sdLocalizationService = sdLocalizationService;
		_sdLoggerService = sdLoggerService;
		_parse = $parse;
		this.intialize($scope);
	};

	/**
	 * 
	 */
	Controller.prototype.intialize = function($scope) {
		var self = this;
		self.selected = {
				process : "",
				legend : "status",
				timeFrame : "days",
				hideAuxilary : true
		}
		self.data = {
				list : []
		};
		self.process = {};
		self.legends = [];
		self.benchmarkCategories = [];
		self.majorTimeFrames = null;
		self.minorTimeFrames = null;
		self.timeFormat =_sdLocalizationService.getInfo().dateTimeFormat;
		self.showAuxillary = false;
		self.timeFrames = self.getTimeFrames();
		self.dataTable = null;
		self.tableReady = false;
		
		//Get the process oid from the params
		var queryGetter = _parse("panel.params.custom");
		var params = queryGetter($scope);
		self.selected.process = params.processInstanceOId;
		self.tableReady = true;
	};

	/**
	 * 
	 */
	Controller.prototype.populateLegendCategories = function(benchmarkPresent,  benchmarkName) {
		var self = this;
		self.legendCategories = [];
		var status = 	{
				id : "status",
				label :_sgI18nService.translate('business-control-center-messages.views-common-column-status'),
		};

		self.legendCategories.push(status)

		if(benchmarkPresent) {
			var benchmark = 	{
					id : "benchmark",
					label : benchmarkName
			}	
			self.legendCategories.push(benchmark)	
			self.selected.legend = "benchmark";
			
			self.legends = self.benchmarkCategories;
		}else {
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
		
		var benchmarkPresent = data.benchmark.value > 0 ;
		if(benchmarkPresent){

			_sdProcessInstanceService.getBenchmarkCategories(data.benchmark.oid).then(function(data){ 

				angular.forEach(data.categories,function(cat){
					var d = {
							label : cat.name,
							color: cat.color
					}
					self.benchmarkCategories.push(d);
				});

				self.populateLegendCategories(true, data.name);
				deferred.resolve();
			});
		}else {
			self.populateLegendCategories(false, null)
			deferred.resolve();
		}
		
		return deferred.promise;
	}


	//TODO visit this after the tree table has been implemented.
	/**
	 * 
	 */
	Controller.prototype.expandSubProcess = function(row){
		var self = this;
	
		var found = _filter("filter")(self.data.list, { oid : row.oid}, true);
		
		if( found && found.length != 1 ){
			return;
		}
		
		if(!found[0].dataFetched){
			console.log("Fetching sub process data");
			self.getSubprocessData(row).then(function(activityList){
				found[0].dataFetched= true;
				found[0].expanded = true;
				self.addActivitiesToChartData(activityList);
				self.onTimeFrameChange();
			});
			
		}else{
			//Show data
			found[0].expanded = true;
			console.log("Data already fetched");
			self.onTimeFrameChange();
		}
	
	};
	

	/**
	 * 
	 */
	Controller.prototype.getBarColor = function( value, type, hasBenchmark) {
		var self = this;
		var color = "";
		var found = ""
			if (!hasBenchmark) {
				var statuses = self.getAvailableStatuses();
				
				if(type == "Process") {
					found = _filter("filter")(statuses,{piValue : value} , true)
				}else {
					found = _filter("filter")(statuses,{aiValue : value} , true)
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
	Controller.prototype.getTimeFrames = function(){

		return [{
			id : "minutes",
			label :_filter("uppercase")( _sgI18nService.translate('portal-common-messages.portalFramework-humanDate-MINUTES',"Minutes"))
		}, {
			id : "hours",
			label : _filter("uppercase")(_sgI18nService.translate('portal-common-messages.portalFramework-humanDate-HOURS',"Hours"))
		}, {
			id : "days",
			label :_filter("uppercase")( _sgI18nService.translate('portal-common-messages.portalFramework-humanDate-DAYS',"Days"))
		}];
	}

	/**
	 * 
	 */
	Controller.prototype.getAvailableStatuses = function(){

		return [{
			label : _sgI18nService
			.translate('views-common-messages.views-activityTable-statusFilter-completed'),
			color : '#AAAAAA',
			aiValue : 2,
			piValue : 2
		},
		{
			label : _sgI18nService
			.translate('views-common-messages.views-processTable-statusFilter-active'),
			color : '#00CD66',
			aiValue : 0,
			piValue : 0
		},
		{
			label : _sgI18nService
			.translate('views-common-messages.views-activityTable-statusFilter-aborted'),
			color : 'red',
			aiValue : 6,
			piValue : 1
		},
		{
			label : _sgI18nService
			.translate('views-common-messages.views-activityTable-statusFilter-interrupted'),
			color : '#CD661D',
			aiValue : 4,
			piValue : 3
		},
		{
			label :  _sgI18nService
			.translate('views-common-messages.views-activityTable-statusFilter-suspended'),
			color : '#1E90FF',
			aiValue : 5,
			piValue : ''
		},
		{
			label : _sgI18nService
			.translate('views-common-messages.views-activityTable-statusFilter-hibernated'),
			color : '#EEEE00',
			aiValue : 7,
			piValue : ''
		},
		{
			label : _sgI18nService
			.translate('views-common-messages.views-activityTable-statusFilter-application'),
			color : '#8A2BE2',
			aiValue : 1,
			piValue : ''
		}];
	}

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
		}else{
			second.setDate(second.getDate() + 2);
		}
		var months = [];
		var days = [];
		var daysInMonth = 0;
		var dayWidth = factor.majorFactorWidth;
		var current = new Date(first);
		self.minorTimeFrameWidth = dayWidth;
		var temptableHolder =''
		//self.drawCurrentTimeLine(first, second, factor);

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
			temptableHolder = temptableHolder + '<span class="minorTimeLine" style="width :'+ self.minorTimeFrameWidth+'px;">'+new Date(first).getDate()+'</span>'
			self.minorTimeFrames.push({
				value : new Date(first)
			});
			daysInMonth = daysInMonth + 1;
			first.setDate(first.getDate() + 1);
		}
		document.getElementById("minorTimeLine").innerHTML = temptableHolder;
		self.majorTimeFrames.push({
			width : (daysInMonth * dayWidth) + (daysInMonth - 1),
			value : new Date(first)
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
		}else{
			second.setHours(second.getHours() + 3);
		}
		var hours = [];
		var days = [];
		var hoursInDay = 0;
		var hourWidth = factor.majorFactorWidth;
		var currentDay = new Date(first);
		self.minorTimeFrameWidth = hourWidth;
		var temptableHolder = '';
		//self.drawCurrentTimeLine(first, second, factor);

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
			temptableHolder = temptableHolder + '<span class="minorTimeLine" style="width :'+ self.minorTimeFrameWidth+'px;">'+first.getHours()+'</span>'
			hoursInDay = hoursInDay + 1;
			first.setHours(first.getHours() + 1);
		}
		
		document.getElementById("minorTimeLine").innerHTML = temptableHolder;
		self.majorTimeFrames.push({
			width : (hoursInDay * hourWidth) + (hoursInDay - 1),
			value : first
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
		}else{
			second.setTime(second.getTime() + (ONE_HOUR_IN_MIILS/2));
		}
		var quaterHours = [];
		var days = [];
		var quaterHoursInADay = 0;
		var quaterHourWidth = factor.majorFactorWidth;
		var currentDay = new Date(first);
		self.minorTimeFrameWidth = quaterHourWidth;

		//self.drawCurrentTimeLine(first, second, factor);
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

			for (var index = 0; index < MINUTES_LABEL_IN_HOUR.length; index++) {
				self.minorTimeFrames.push({
					value : first.getHours() + ":"+MINUTES_LABEL_IN_HOUR[index]
				});
				quaterHoursInADay = quaterHoursInADay + 1;
				temptableHolder = temptableHolder + '<span class="minorTimeLine" style="width :'+ self.minorTimeFrameWidth+'px;">'+first.getHours() + ":"+MINUTES_LABEL_IN_HOUR[index]+'</span>'
			}

			first.setTime(first.getTime() + ONE_HOUR_IN_MIILS);
		}
		
		document.getElementById("minorTimeLine").innerHTML = temptableHolder;
		self.majorTimeFrames.push({
			width : (quaterHoursInADay * quaterHourWidth) + (quaterHoursInADay - 1),
			value : first
		});
	};


	/**
	 * 
	 */
	Controller.prototype.drawCurrentTimeLine = function(start,end, factor){
		var self = this;	

		self.currentTimeLine = {
				height : self.data.list.length * 32
		}
	}

	/**
	 * 
	 */
	Controller.prototype.computeDifference = function(startTime,
			endTime, factor) {
		var differenceInFactor = (endTime - startTime) / factor.minorFactor;
		var correctionForPadding = parseInt((endTime - startTime)
				/ factor.majorFactor);
		return {difference : differenceInFactor, padding : correctionForPadding} ;
	};
	/**
	 * 
	 */
	Controller.prototype.mouseEnter = function(event, xOffset) {
		var self = this;
		if(!xOffset) {
			xOffset = 0;
		}
		self.position = event.offsetX +xOffset - 100 - $("#ganttChart").scrollLeft();
	};
	
	/**
	 * 
	 */
	Controller.prototype.auxComparator = function(auxValue, showAux) {
	   if (showAux) {
		   return true;
	   }
	   else {
		   return !auxValue;
	   }
   };
	
	/**
	 * 
	 */
	Controller.prototype.onLegendChange = function() {
		var self = this;
		switch (self.selected.legend ) {
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
				function(result) {
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
		
		if(self.process.endTime) {
			end = new Date(self.process.endTime);
		}
		else if(self.process.predictedEndTime) {
			end = new Date(self.process.predictedEndTime);
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
	/**
	 * 
	 */
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
	
	/**
	 * 
	 */
	Controller.prototype.expandAll = function() {
		this.dataTable.expandAll();
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
	Controller.prototype.toggleAuxillary = function() {
		this.showAuxillary = !this.showAuxillary;
	};
	
	/**
	 * 
	 */
	Controller.prototype.getAuxTitle = function() {
		if(this.showAuxillary){
			return "Hide Auxillary"
		}
		return "Show Auxillary";
	};
	
	
	
	/**
	 * After tree table
	 */
	
	Controller.prototype.fetchData = function(params){
		var self = this;
		var deferred = _q.defer();
		var data = {};
		
		if(!params) {
			
			self.currentTime = new Date(); //Current Time Load once intially
			
			_sdProcessInstanceService.getProcessByOid(self.selected.process).then(function(process){
				self.benchmarkCategories = [];
				self.process = process;
			
				//Root Oid present then the process is a subprocess. Draw chart with root process.
				if(process.processInstanceRootOID){
					_sdProcessInstanceService.getProcessByOid(process.processInstanceRootOID).then(function(rootProcess){
						self.process = rootProcess;
						self.drawTimeFrames();
						
						var parent = self.constructDataForProcess(rootProcess);
						parent.children = [];
						parent.$leaf =  false
						parent.$expanded =  true
						self.getBenchmarkCategories(rootProcess).then(function() {
							self.getChildren(rootProcess.oid).then(function(childrens){
								parent.children = childrens;
								
								data.list   = [parent];
								data.totalCount =	data.list.length;
								console.log("Data from table")
								console.log(data)
								deferred.resolve(data);
							});
						});
					});
				

				} else {
					var parent = self.constructDataForProcess(process);
					parent.children = [];
					parent.$leaf =  false
					parent.$expanded =  true

					
					self.drawTimeFrames();
					self.getBenchmarkCategories(process).then(function() {
						self.getChildren(process.oid).then(function(childrens){
							parent.children = childrens;
							
							data.list   = [parent];
							data.totalCount =	data.list.length;
							console.log("Data from table")
							console.log(data)
							deferred.resolve(data);
						});
					});	
					
				}
			

			});

		} else {
			
			self.getChildren(params.parent.piOid).then(function(childrens) {
				data.list = childrens;
				data.totalCount =	data.list.length;
				deferred.resolve(data);
			})
			
		}
		return deferred.promise;
	};
	
	
	Controller.prototype.getChildren = function(oid) {
		var self = this;
		var defered = _q.defer();
		var children = []
		_sdActivityInstanceService.getByProcessOid(oid).then(function(activityList){
			angular.forEach(activityList, function(activity){
				var activityGraphData  = self.constructDataForActivity(activity);
				children.push(activityGraphData)
			});
			
			defered.resolve(children);
			
		});
		return defered.promise;
	}
	
	/**
	 * 
	 */
	Controller.prototype.constructDataForActivity = function(data) {
		var self = this;
		var normalizedData = this.normalizeActivityData(data);
		
		if(normalizedData.piOid){
			normalizedData.$leaf = false;
		}
		
		normalizedData.days = self.getGraphData(normalizedData,'days');
		normalizedData.hours = self.getGraphData(normalizedData,'hours');
		normalizedData.minutes = self.getGraphData(normalizedData,'minutes');
		console.log("*****Data *****")
		console.log(normalizedData)
		return normalizedData;
	}
	
	/**
	 * 
	 */
	Controller.prototype.constructDataForProcess = function(data){
		var self = this;
		var normalizedData = self.normalizeProcessData(data)
		
		normalizedData.days = self.getGraphData(normalizedData,'days');
		normalizedData.hours = self.getGraphData(normalizedData,'hours');
		normalizedData.minutes = self.getGraphData(normalizedData,'minutes');
		console.log("*****Data *****")
		console.log(normalizedData)
		return normalizedData;
	}
	
	/**
	 * 
	 */
	Controller.prototype.normalizeActivityData = function(activity) {
		var self = this;
		var statusColor = self.getBarColor(activity.status.value,"Activity", false);
		var bColor = self.getBarColor(activity,"Activity", true);
		
		var piOid = null;
		var auxillary = activity.auxillary;
		if(activity.activity.implementationTypeId == 'Subprocess'){
			var process =  _sdProcessInstanceService.getProcessByStartingActivityOid( activity.activityOID, true);
			piOid = process.oid
			auxillary = process.auxillary;
		}
		
		var data = {
				name : activity.activity.name,
				startTime :  activity.startTime,
				endTime :  activity.lastModification,
				sColor : statusColor,
				bColor : bColor,
				activatable : activity.activatable,
				status : activity.status,
				oid : activity.activityOID,
				benchmarkCategory : activity.benchmark,
				type : activity.activity.implementationTypeId,
				subProcessId : (activity.processInstance) ? activity.processInstance.oid : null,
				auxillary : auxillary,
				piOid : piOid
		}
		
		return data;
		
	};
	
	/**
	 * 
	 */
	Controller.prototype.normalizeProcessData = function(piData) { 
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
				benchmarkCategory : piData.benchmark,
				type : "process",
				auxillary : piData.auxillary
		}
		return data;
	};
	
	
	/**
	 * 
	 */
	 function getFactor (timeframe){
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
	Controller.prototype.getStartTime = function(timeFrame){
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
	}
	
	
	/**
	 * 
	 */
	Controller.prototype.getChartData = function(rowData)
	{
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
	}
	
	/**
	 * 
	 */
	Controller.prototype.getRowColor = function(rowData)
	{
		var self = this;
		switch (self.selected.legend ) {
			case "benchmark":
				return rowData.bColor;
			case "status":
				return rowData.sColor;
		}
		return rowData.sColor;
	}
	
	/**
	 * 
	 */
	Controller.prototype.getLabel = function(rowData)
	{
		console.log("Data")
		var self = this;
		if(self.selected.legend  == 'benchmark') {
			if(rowData.benchmarkCategory.label){
				return rowData.benchmarkCategory.label;
			}
		}
		return rowData.status.label;  
	}
	
	
	
	/**
	 * 
	 */
	Controller.prototype.getGraphData = function(item, timeFrame){

		var self = this;

		self.columnData = [];
		var factor =  getFactor(timeFrame);
		var startTime = self.getStartTime(timeFrame)
		var minorFactorWidth =factor.minorFactorWidth;
	

		var delay = self.computeDifference(startTime.getTime(),
				item.startTime, factor);
		var inflightLength = null;


		var status = item.status;
		if(self.selected.legend == "benchmark" && item.benchmarkCategory.label) {
			status = item.benchmarkCategory;
		}

		//If the item doesnt have a end time or a activity for a process which has stil not completed.

		
		if(!item.endTime ||  FINISHED_STATUSES.indexOf(item.status.value) < 0) {
			item.endTime = self.currentTime.getTime();
		}

		var completedLength = self.computeDifference(item.startTime,
				item.endTime, factor)

				if(item.predictedEndTime) {
					inflightLength  = self.computeDifference(item.endTime,
							item.predictedEndTime, factor);
				}	


		var delay = (delay.difference * minorFactorWidth)  + delay.padding -1;
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

		 var graphData = {
			delay : delay,
			completed : completed,
			inflight : inflight
		};
		 
		 return graphData;
	}

	
	

})();