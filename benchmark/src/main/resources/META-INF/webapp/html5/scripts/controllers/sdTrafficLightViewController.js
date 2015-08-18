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
 * @author Abhay.Thappan
 */

(function() {
	'use strict';

	angular.module("benchmark-app").controller(
			'sdTrafficLightViewController',
			[ '$q', '$filter', '$timeout', 'benchmarkService', 'sdProcessDefinitionService',
					'sdTrafficLightViewService', 'sdProcessInstanceService', 'sdLoggerService',
					TrafficLightViewController ]);

	var _q;
	var trace;
	var _timeout;
	var _filter;
	var _benchmarkService;
	var _sdProcessDefinitionService;
	var _sdTrafficLightViewService;
	var _sdProcessInstanceService;

	/**
	 * 
	 */
	function TrafficLightViewController($q, $filter, $timeout, benchmarkService, sdProcessDefinitionService,
			sdTrafficLightViewService, sdProcessInstanceService, sdLoggerService) {
		trace = sdLoggerService.getLogger('benchmark-app.sdTrafficLightViewController');
		_q = $q;
		_timeout = $timeout;
		_filter = $filter;
		_benchmarkService = benchmarkService;
		_sdProcessDefinitionService = sdProcessDefinitionService;
		_sdTrafficLightViewService = sdTrafficLightViewService;
		_sdProcessInstanceService = sdProcessInstanceService;
		this.processes = [ {
			'id' : 'ALL_PROCESSES',
			'name' : 'All Processes'
		} ];
		this.benchmarkDefinitions = [ {
			'oid' : 'ALL_BENCHMARKS',
			'name' : 'All Benchmarks'
		} ];
		this.getRuntimeBenchmarkDefinitions();
		this.getAllProcesses();

		this.dataTable = null;
		this.processDataTable = null;

		this.showTLVCriteria = true;
		this.selectedDrillDown = "PROCESS_WORKITEM";
		this.dayOffset = 0;
		this.selectedDateType = "BUSINESS_DATE";
	}

	/**
	 * 
	 * @returns
	 */
	TrafficLightViewController.prototype.getRuntimeBenchmarkDefinitions = function() {
		var self = this;
		_benchmarkService.getBenchmarkDefinitions('').then(function(data) {
			angular.forEach(data.benchmarkDefinitions, function(benchmarkDef) {
				var benchmarkDefintion = {
					'oid' : benchmarkDef.metadata.runtimeOid,
					'name' : benchmarkDef.content.name,
					'categories' : benchmarkDef.content.categories
				};
				self.benchmarkDefinitions.push(benchmarkDefintion);
			});
		}, function(error) {
			trace.log(error);
		});
	};

	TrafficLightViewController.prototype.getAllProcesses = function() {
		var self = this;
		_sdProcessDefinitionService.getAllUniqueProcesses(true).then(function(data) {
			angular.forEach(data, function(processDef) {
				var processDefintion = {
					'id' : processDef.id,
					'name' : processDef.name
				};
				self.processes.push(processDefintion);
			});
		}, function(error) {
			trace.log(error);
		});
	};

	TrafficLightViewController.prototype.showTrafficLightView = function() {
		var self = this;
		self.showTLVStatastics = false;
		self.showProcessTable = false;
		self.processDataTable = null;
		var queryData = {
			'isAllProcessess' : false,
			'isAllBenchmarks' : false
		};
		var processes = [];
		var bOids = [];
		angular.forEach(self.selectedProcesses, function(process) {
			if (process.id == 'ALL_PROCESSES') {
				queryData.isAllProcessess = true;
				// break;
			} else {
				processes.push(process);
			}
		});

		angular.forEach(self.selectedBenchmarks, function(bOid) {
			if (bOid == 'ALL_BENCHMARKS') {
				queryData.isAllBenchmarks = true;
				// break;
			} else {
				bOids.push(bOid);
			}
		});

		if (queryData.isAllProcessess) {
			processes = [];
			angular.forEach(self.processes, function(process) {
				if (process.id != 'ALL_PROCESSES') {
					processes.push(process);
				}

			});
		}

		if (queryData.isAllBenchmarks) {
			bOids = [];
			angular.forEach(self.benchmarkDefinitions, function(benchmarkDef) {
				if (benchmarkDef.oid != 'ALL_BENCHMARKS') {
					bOids.push(benchmarkDef.oid);
				}
			});
		}

		angular.forEach(self.benchmarkDefinitions, function(benchmarkDef) {
			if (bOids[0] == benchmarkDef.oid) {
				self.categories = benchmarkDef.categories;
			}
		});

		queryData.processes = processes;
		queryData.bOids = bOids;
		queryData.dateType = self.selectedDateType;
		queryData.dayOffset = self.dayOffset;
		queryData.categories = self.categories;
		self.queryData = queryData;
		_sdTrafficLightViewService.getTLVStatastic(queryData).then(function(data) {
			self.tlvStatsData = {};
			self.tlvStatsData.list = data.list;
			self.tlvStatsData.totalCount = data.totalCount;
			self.showTLVStatastics = true;
		}, function(error) {
			trace.log(error);
		});
	};
	
	TrafficLightViewController.prototype.getActivitySatastic = function(processId){
		var self = this;
		var queryData = {};
		queryData = self.queryData;
		delete queryData.isAllProcessess;
		delete queryData.isAllBenchmarks;
		delete queryData.processes;
		queryData.processId = processId;
		_sdTrafficLightViewService.getTLVActivityStatastic(queryData).then(function(data) {
			console.log(data);
		}, function(error) {
			trace.log(error);
		});
	};

	/**
	 * 
	 */
	TrafficLightViewController.prototype.setShowTLVCriteria = function() {
		var self = this;
		self.showTLVCriteria = !self.showTLVCriteria;
	};

	TrafficLightViewController.prototype.setDataForProcessTable = function(processId, state, benchmarkIndex) {
		var self = this;
		self.selectedBenchmarkCategory = benchmarkIndex;
		self.selectedProcessId = processId;
		self.state = state;
		if(self.processDataTable != undefined){
			self.processDataTable.refresh();
		}else{
			self.showProcessTable = true;
		}		
	};
	

	TrafficLightViewController.prototype.getProcesslistForTLV = function(params) {
        var self = this;
		var query = {
			'options' : params.options,
			'bOids' : self.queryData.bOids,
			'dateType' : self.queryData.dateType,
			'dayOffset' : self.queryData.dayOffset,
			'benchmarkCategory' : self.selectedBenchmarkCategory,
			'processId' : self.selectedProcessId,
			'state' : self.state
		};

		var deferred = _q.defer();
		self.processList = {};
		_sdProcessInstanceService.getProcesslistForTLV(query).then(function(data) {
			self.processList.list = data.list;
			self.processList.totalCount = data.totalCount;

			deferred.resolve(self.processList);
		}, function(error) {
			deferred.reject(error);
		});

		return deferred.promise;
	};
	
	TrafficLightViewController.prototype.getActivitylistForTLV = function(params) {
        var self = this;
		var query = {
			'options' : params.options,
			'bOids' : self.queryData.bOids,
			'dateType' : self.queryData.dateType,
			'dayOffset' : self.queryData.dayOffset,
			'benchmarkCategory' : self.selectedBenchmarkCategory,
			'processId' : self.selectedProcessId,
			'activtyId' : self.selectedActivityId,
			'state' : self.state
		};

		var deferred = _q.defer();
		self.processList = {};
		_sdProcessInstanceService.getProcesslistForTLV(query).then(function(data) {
			self.processList.list = data.list;
			self.processList.totalCount = data.totalCount;

			deferred.resolve(self.processList);
		}, function(error) {
			deferred.reject(error);
		});

		return deferred.promise;
	};
	
	

})();