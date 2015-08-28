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
			[ '$q', 'benchmarkService', 'sdProcessDefinitionService', 'sdTrafficLightViewService',
					'sdProcessInstanceService', 'sdActivityInstanceService', 'sdLoggerService', '$injector', TrafficLightViewController ]);

	var _q;
	var trace;
	var _benchmarkService;
	var _sdProcessDefinitionService;
	var _sdTrafficLightViewService;
	var _sdProcessInstanceService;
	var _sdBusinessObjectManagementService;
	var _sdActivityInstanceService;

	/**
	 * 
	 */
	function TrafficLightViewController($q, benchmarkService, sdProcessDefinitionService, sdTrafficLightViewService,
			sdProcessInstanceService, sdActivityInstanceService, sdLoggerService, $injector) {
		trace = sdLoggerService.getLogger('benchmark-app.sdTrafficLightViewController');
		_q = $q;
		_benchmarkService = benchmarkService;
		_sdProcessDefinitionService = sdProcessDefinitionService;
		_sdTrafficLightViewService = sdTrafficLightViewService;
		_sdProcessInstanceService = sdProcessInstanceService;
		_sdActivityInstanceService = sdActivityInstanceService;
		// dynamically injecting the sdBusinessObjectManagementService from
		// ipp-business-object-management
		_sdBusinessObjectManagementService = $injector.get('sdBusinessObjectManagementService');

		this.processes = [ {
			'id' : 'ALL_PROCESSES',
			'name' : 'All Processes'
		} ];

		this.selectedProcesses = [ this.processes[0] ];
		this.benchmarkDefinitions = [ {
			'oid' : 'ALL_BENCHMARKS',
			'name' : 'All Benchmarks'
		} ];
		this.selectedBenchmarks = [ this.benchmarkDefinitions[0].oid ];
		this.getRuntimeBenchmarkDefinitions();
		this.getAllProcesses();

		this.dataTable = null;
		this.processDataTable = null;
		this.activityDataTable = null;

		this.showTLVCriteria = true;
		this.selectedDrillDown = "PROCESS_WORKITEM";
		this.dayOffset = 0;
		this.selectedDateType = "BUSINESS_DATE";

		this.bOInstanceMap = {};
		this.bOPrimaryKeyMap = {};

		this.relatedBOMap = {};
		this.selectedBusinessObjectInstances = [];
		this.selectedRelatedBusinessObjectInstances = [];
	}

	/**
	 * 
	 * @returns
	 */
	TrafficLightViewController.prototype.getRuntimeBenchmarkDefinitions = function() {
		var self = this;
		_benchmarkService.getBenchmarkDefinitions('Published').then(function(data) {
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
	/**
	 * 
	 */
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
	/**
	 * 
	 */
	TrafficLightViewController.prototype.showTrafficLightView = function() {
		var self = this;
		self.showTLVStatastics = false;
		self.showProcessTable = false;
		self.showActivityTable = false;
		self.processDataTable = null;
		self.activityDataTable = null;
		var queryData = {
			'isAllProcessess' : false,
			'isAllBenchmarks' : false
		};
		var processes = [];
		var bOids = [];
		angular.forEach(self.selectedProcesses, function(process) {
			if (process.id == 'ALL_PROCESSES') {
				queryData.isAllProcessess = true;
			} else {
				processes.push(process);
			}
		});

		angular.forEach(self.selectedBenchmarks, function(bOid) {
			if (bOid == 'ALL_BENCHMARKS') {
				queryData.isAllBenchmarks = true;
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

		_sdTrafficLightViewService.getRuntimeBenchmarkCategories(bOids).then(function(data) {
			self.categories = data;
			queryData.processes = processes;
			queryData.bOids = bOids;
			queryData.dateType = self.selectedDateType;
			queryData.dayOffset = self.dayOffset;
			queryData.categories = self.categories;
			self.queryData = queryData;
			self.tlvCriteriaForm.$error.benchmarksNotIdentical = false;
			_sdTrafficLightViewService.getTLVStatastic(queryData).then(function(data) {
				self.tlvStatsData = {};
				self.tlvStatsData.list = data.list;
				self.tlvStatsData.totalCount = data.totalCount;
				self.showTLVStatastics = true;
			}, function(error) {
				trace.log(error);
			});
		}, function(error) {
			self.errorMsg = error.data.message;
			self.tlvCriteriaForm.$error.benchmarksNotIdentical = true;
			trace.log(error);
		});

	};
	
	
	/**
	 * 
	 */
	
	TrafficLightViewController.prototype.getTlvStatsData = function(params){
		var self = this;
		var deferred = _q.defer();
		var tlvData = {};
		if(!params) {
			tlvData.list = self.tlvStatsData.list;
			tlvData.totalCount = self.tlvStatsData.totalCount;
			angular.forEach(tlvData.list, function(object){
				object.$leaf = false;
			});
			deferred.resolve(tlvData);
		}else{
			self.parentId = params.parent.id;
			self.getActivitySatastic(params.parent.id).then(function(data){
				tlvData.list = data.list;
				tlvData.totalCount = data.totalCount;
				deferred.resolve(tlvData);
			},function(error) {
				trace.log(error);
				deferred.reject(error);
			});
		}
		
		return deferred.promise;
	}
	/**
	 * 
	 * @param processId
	 */
	TrafficLightViewController.prototype.getActivitySatastic = function(processId) {
		var self = this;
		var deferred = _q.defer();
		var queryData = {};
		queryData = self.queryData;
		delete queryData.isAllProcessess;
		delete queryData.isAllBenchmarks;
		delete queryData.processes;
		queryData.processId = processId;
		_sdTrafficLightViewService.getTLVActivityStatastic(queryData).then(function(data) {
			deferred.resolve(data);
		}, function(error) {
			trace.log(error);
			deferred.reject(error);
		});
		
		return deferred.promise;
	};

	/**
	 * 
	 */
	TrafficLightViewController.prototype.setShowTLVCriteria = function() {
		var self = this;
		self.showTLVCriteria = !self.showTLVCriteria;
	};
	/**
	 * \
	 * 
	 * @param processId
	 * @param state
	 * @param benchmarkIndex
	 */
	TrafficLightViewController.prototype.setDataForProcessTable = function(id, state, isActivity, benchmarkIndex) {
		var self = this;
		self.selectedBenchmarkCategory = benchmarkIndex;
		self.state = state;
		
		if(isActivity){
			self.processDataTable = null;
			self.selectedProcessId = self.parentId;
			self.selectedActivityId = id;
			if (self.activityDataTable != undefined) {
				self.activityDataTable.refresh();
			} else {
				self.showActivityTable = true;
				self.showProcessTable = false;
				
			}
		}else{
			self.activityDataTable = null;
			self.selectedProcessId = id;
			if (self.processDataTable != undefined) {
				self.processDataTable.refresh();
			} else {
				self.showProcessTable = true;
				self.showActivityTable = false;
			}
		}
		
	};

	/**
	 * 
	 * @param params
	 * @returns
	 */
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
	/**
	 * 
	 * @param params
	 * @returns
	 */
	TrafficLightViewController.prototype.getActivityListForTLV = function(params) {
		var self = this;
		var query = {
			'options' : params.options,
			'bOids' : self.queryData.bOids,
			'dateType' : self.queryData.dateType,
			'dayOffset' : self.queryData.dayOffset,
			'benchmarkCategory' : self.selectedBenchmarkCategory,
			'processId' : self.selectedProcessId,
			'activityId' : self.selectedActivityId,
			'state' : self.state
		};

		var deferred = _q.defer();
		self.processList = {};
		_sdActivityInstanceService.getActivitylistForTLV(query).then(function(data) {
			self.processList.list = data.list;
			self.processList.totalCount = data.totalCount;

			deferred.resolve(self.processList);
		}, function(error) {
			deferred.reject(error);
		});

		return deferred.promise;
	};

	TrafficLightViewController.prototype.drillDownChange = function() {
		var self = this;
		if (self.selectedDrillDown == 'BUSINESS_OBJECT') {
			_sdBusinessObjectManagementService.getBusinessObjects().then(function(data) {
				self.businessObjectModels = data.models;
				self.refreshBusinessObjects();
				self.showBusinessObjects = true;
			}, function(error) {
				trace.log(error);
			});
		} else {
			self.showBusinessObjects = false;
			self.showGroupByObjects = false;
		}
	};

	/**
	 * 
	 */
	TrafficLightViewController.prototype.getBusinessObjectInstances = function() {
		var self = this;
		self.selectedBusinessObjectInstances = [];
		self.selectedRelatedBusinessObject = {};
		self.selectedRelatedBusinessObjectInstances = [];
		if (self.bOInstanceMap[self.selectedBusinessObject.businessObjectQualifiedId] == undefined) {
			_sdBusinessObjectManagementService
					.getBusinessObjectInstances(self.selectedBusinessObject)
					.then(
							function(data) {
								angular.forEach(self.selectedBusinessObject.fields, function(field) {
									if (field.primaryKey) {
										self.primaryKeyForBO = field.id;
									}
								});
								self.bOPrimaryKeyMap[self.selectedBusinessObject.businessObjectQualifiedId] = self.primaryKeyForBO;
								self.bOInstanceMap[self.selectedBusinessObject.businessObjectQualifiedId] = data;
								if (self.relatedBOMap[self.selectedBusinessObject.businessObjectQualifiedId] == undefined) {
									_sdBusinessObjectManagementService
											.getRelatedBusinessObject(self.selectedBusinessObject)
											.then(
													function(data) {
														self.refreshRelatedBusinessObjects(data);
														self.relatedBOMap[self.selectedBusinessObject.businessObjectQualifiedId] = self.relatedBusinessObjects;
														self.showGroupByObjects = true;
													}, function(error) {
														trace.log(error);
													});
								} else {
									self.relatedBusinessObjects = self.relatedBOMap[self.selectedBusinessObject.businessObjectQualifiedId];
									self.showGroupByObjects = true;
								}
							}, function(error) {
								trace.log(error);
							});
		} else {
			self.primaryKeyForBO = self.bOPrimaryKeyMap[self.selectedBusinessObject.businessObjectQualifiedId];
			self.relatedBusinessObjects = self.relatedBOMap[self.selectedBusinessObject.businessObjectQualifiedId];
			self.showGroupByObjects = true;
		}
	};

	/**
	 * 
	 */
	TrafficLightViewController.prototype.refreshBusinessObjects = function() {
		var self = this;
		self.businessObjects = [];

		for (var n = 0; n < self.businessObjectModels.length; ++n) {
			for (var m = 0; m < self.businessObjectModels[n].businessObjects.length; ++m) {
				if (!self.businessObjectModels[n].businessObjects[m].types) {
					self.businessObjectModels[n].businessObjects[m].types = {};
				}

				self.businessObjectModels[n].businessObjects[m].modelOid = self.businessObjectModels[n].oid;
				self.businessObjectModels[n].businessObjects[m].label = self.businessObjectModels[n].name + "/"
						+ self.businessObjectModels[n].businessObjects[m].name;
				self.businessObjects.push(self.businessObjectModels[n].businessObjects[m]);
			}
		}
	};

	/**
	 * 
	 */
	TrafficLightViewController.prototype.refreshRelatedBusinessObjects = function(relationshipData) {
		var self = this;
		self.relatedBusinessObjects = [];

		angular.forEach(relationshipData, function(object) {
			var relatedObject = {};
			relatedObject.businessObjectQualifiedId = '{' + object.relationship.otherBusinessObject.modelId + '}'
					+ object.relationship.otherBusinessObject.id;
			relatedObject.label = object.relationship.otherBusinessObject.modelId + '/'
					+ object.relationship.otherBusinessObject.id;
			relatedObject.otherForeignKeyField = object.relationship.otherForeignKeyField;
			self.relatedBusinessObjects.push(relatedObject);
		});
	};
	/**
	 * 
	 * @param matchVal
	 */
	TrafficLightViewController.prototype.getBOInstanceMatches = function(matchVal) {
		var self = this;
		var results = [];
		var boInstancedata = self.bOInstanceMap[self.selectedBusinessObject.businessObjectQualifiedId];

		boInstancedata.forEach(function(v) {
			if (v[self.primaryKeyForBO] == matchVal) {
				results.push(v);
			}
		});

		self.businessObjectInstancesData = results;
	};
	/**
	 * 
	 */
	TrafficLightViewController.prototype.getRelatedBOInstances = function() {
		var self = this;
		self.selectedRelatedBusinessObjectInstances = [];
		var primaryKeys = [];
		angular.forEach(self.selectedBusinessObjectInstances, function(selBOInstance) {
			angular.forEach(selBOInstance[self.selectedRelatedBusinessObject.otherForeignKeyField],function(id){
				primaryKeys.push(id);
			});	
		});
		_sdBusinessObjectManagementService.getRelatedBusinessObjectInstances(
				self.selectedRelatedBusinessObject.businessObjectQualifiedId, primaryKeys).then(function(data) {
			self.realatedBusinessObjectInstances = data;
		}, function(error) {
			trace.log(error);
		});
	};
	
	TrafficLightViewController.prototype.getRelatedBOInstanceMatches = function(relatedMatchVal){
		var self = this;
		var results = [];
		var boInstancedata = self.realatedBusinessObjectInstances;

		boInstancedata.forEach(function(v) {
			if (v[self.selectedRelatedBusinessObject.otherForeignKeyField] == relatedMatchVal) {
				results.push(v);
			}
		});

		self.relatedBusinessObjectInstancesData = results;
	};
})();