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
			[ '$scope', '$q', 'benchmarkService', 'sdProcessDefinitionService', 'sdTrafficLightViewService',
					'sdProcessInstanceService', 'sdActivityInstanceService', 'sdLoggerService', '$injector',
					'sdUtilService', 'sdFavoriteViewService', '$parse','sdViewUtilService', TrafficLightViewController ]);

	var _q;
	var trace;
	var _benchmarkService;
	var _sdProcessDefinitionService;
	var _sdTrafficLightViewService;
	var _sdProcessInstanceService;
	var _sdBusinessObjectManagementService;
	var _sdActivityInstanceService;
	var _sdUtilService;
	var _sdFavoriteViewService;
	var _sdViewUtilService;

	var _parse;

	/**
	 *
	 */
	function TrafficLightViewController($scope, $q, benchmarkService, sdProcessDefinitionService,
			sdTrafficLightViewService, sdProcessInstanceService, sdActivityInstanceService, sdLoggerService, $injector,
			sdUtilService, sdFavoriteViewService, $parse, sdViewUtilService) {
		trace = sdLoggerService.getLogger('benchmark-app.sdTrafficLightViewController');
		_q = $q;
		_benchmarkService = benchmarkService;
		_sdProcessDefinitionService = sdProcessDefinitionService;
		_sdTrafficLightViewService = sdTrafficLightViewService;
		_sdProcessInstanceService = sdProcessInstanceService;
		_sdActivityInstanceService = sdActivityInstanceService;
		_sdUtilService = sdUtilService;
		_sdFavoriteViewService = sdFavoriteViewService;
		_parse = $parse;
		_sdViewUtilService = sdViewUtilService;
		// dynamically injecting the sdBusinessObjectManagementService from
		// ipp-business-object-management
		_sdBusinessObjectManagementService - null;
		
		this.boAvailable = false;
		//Check if Business object management is present.
		if($injector.has('sdBusinessObjectManagementService')) {
					_sdBusinessObjectManagementService = $injector.get('sdBusinessObjectManagementService');
					this.boAvailable = true;
					trace.log('sdBusinessObjectManagementService found!');
		} else {
					trace.log('sdBusinessObjectManagementService not found.Operating in non BO mode.');
		}

		// Getting the custom view params if view is getting opened from
		// favorite instance
		var queryGetter = _parse("panel.params.custom");
		var params = queryGetter($scope);

		this.processes = [ {
			'id' : 'ALL_PROCESSES',
			'name' : 'All Processes',
			 order : 0
		} ];

		this.selectedProcesses = [ this.processes[0] ];
		this.selProcesses = [JSON.stringify(this.processes[0])];
		this.benchmarkDefinitions = [ {
			'oid' : 'ALL_BENCHMARKS',
			'name' : 'All Benchmarks',
			 order : 0
		} ];
		this.selectedBenchmarks = [ this.benchmarkDefinitions[0].oid ];
		this.getRuntimeBenchmarkDefinitions();
		// passing custom view params here to call REST API for getting
		// favorite.
		this.getAllProcesses(params, $scope);

		this.dataTable = null;
		this.dataTableForBO = null;
		this.processDataTable = null;
		this.activityDataTable = null;

		this.showTLVCriteria = true;
		this.selectedDrillDown = "PROCESS_WORKITEM";
		this.dayOffset = 0;
		this.selectedDateType = "BUSINESS_DATE";
		this.selectedBOType = "PROCESSES";

		this.bOInstanceMap = {};
		this.bOPrimaryKeyMap = {};
		this.bOPrimaryKeyTypeMap = {};

		this.relatedBOMap = {};
		this.selectedBusinessObjectInstances = [];
		this.selectedRelatedBusinessObjectInstances = [];

		// Saving the name of the favorite for future update.
		if (params.preferenceName != undefined) {
			this.tlvReportName = params.preferenceName;
			this.favoriteName = params.preferenceName;
		}else{
			this.tlvReportName = 'New TLV';
			this.favoriteName = 'New TLV';
		}
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
					'categories' : benchmarkDef.content.categories,
					'models' : benchmarkDef.content.models
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
	TrafficLightViewController.prototype.getAllProcesses = function(params, $scope) {
		var self = this;
		_sdProcessDefinitionService.getAllUniqueProcesses(true).then(function(data) {
			angular.forEach(data, function(processDef) {
				var processDefintion = {
					'id' : processDef.id,
					'name' : processDef.name
				};
				self.processes.push(processDefintion);
			});
			if (params.preferenceId != undefined && params.preferenceName != undefined) {
				self.getFavoriteByName(params, $scope);
			}

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
		self.showTLVStatasticsByBO = false;
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

		_sdTrafficLightViewService
				.getRuntimeBenchmarkCategories(bOids)
				.then(
						function(data) {
							self.categories = data;
							queryData.processes = processes;
							queryData.bOids = bOids;
							queryData.dateType = self.selectedDateType;
							queryData.dayOffset = self.dayOffset;
							queryData.categories = self.categories;
							queryData.processActivityMap = self.createProcessActivityArray(queryData.bOids,
									queryData.processes);
							self.queryData = queryData;
							self.tlvCriteriaForm.$error.benchmarksNotIdentical = false;
							if (self.selectedDrillDown == "PROCESS_WORKITEM") {
								_sdTrafficLightViewService.getTLVStatastic(queryData).then(function(data) {
									self.tlvStatsData = {};
									self.tlvStatsData.list = data.benchmarkTLVProcessStas;
									self.tlvStatsData.totalCount = data.benchmarkTLVProcessStas.length;
									self.bATLVStatsMap = data.bATLVStatsMap;
									self.showTLVStatastics = true;
								}, function(error) {
									trace.log(error);
								});
							} else if (self.selectedDrillDown == "BUSINESS_OBJECT"
									&& self.selectedBusinessObject != undefined) {
								queryData.businessObjectQualifiedId = self.selectedBusinessObject.businessObjectQualifiedId;
								queryData.businessObjectType = self.bOPrimaryKeyTypeMap[self.selectedBusinessObject.businessObjectQualifiedId];
								if (!_sdUtilService.isEmpty(self.selectedBusinessObjectInstances)) {
									var boInstances = [];
									angular.forEach(self.selectedBusinessObjectInstances, function(boInstance) {
										boInstances.push(boInstance[self.primaryKeyForBO]);
									});

									queryData.selectedBusinessObjectInstances = boInstances;
								}

								if (self.selectedRelatedBusinessObject != null && self.selectedRelatedBusinessObject.businessObjectQualifiedId != undefined) {
									queryData.groupBybusinessQualifiedId = self.selectedRelatedBusinessObject.businessObjectQualifiedId;
									queryData.groupBybusinessObjectType = self.bOPrimaryKeyTypeMap[self.selectedRelatedBusinessObject.businessObjectQualifiedId];
									if (!_sdUtilService.isEmpty(self.selectedRelatedBusinessObjectInstances)) {
										var groupbyBOInstances = [];
										angular
												.forEach(
														self.selectedRelatedBusinessObjectInstances,
														function(groupbyBOInstance) {
															groupbyBOInstances
																	.push(groupbyBOInstance[self.selectedRelatedBusinessObject.otherForeignKeyField]);
														});

										queryData.selectedRelatedBusinessObjectInstances = groupbyBOInstances;
									}
								}
								queryData.selectedBOType = self.selectedBOType;
								self.queryData = queryData;

								_sdTrafficLightViewService.getTLVStatasticByBusinessObject(queryData).then(
										function(data) {
											self.tlvBOStatsTotal = {};
											self.tlvBOStatsData = {};
											self.tlvBOStatsDrillDownData = {};
											self.tlvBOStatsTotal.list = [ data.totalBusinessObjectStatistic ];
											self.tlvBOStatsTotal.totalCount = self.tlvBOStatsTotal.list.length;
											self.tlvBOStatsData.list = data.businessObjectsResultList;
											self.tlvBOStatsData.totalCount = data.businessObjectsResultList.length;
											if (data.businessObjectsForGroupByMap != undefined) {
												self.tlvBOStatsDrillDownData = data.businessObjectsForGroupByMap;
											}
											self.showTLVStatasticsByBO = true;
										}, function(error) {
											trace.log(error);
										});
							}

						}, function(error) {
							self.errorMsg = error.data.message;
							self.tlvCriteriaForm.$error.benchmarksNotIdentical = true;
							trace.log(error);
						});

	};

	/**
	 *
	 */
	TrafficLightViewController.prototype.createProcessActivityArray = function(bOids, processes) {
		var benchmarkProcessActivityMap = {};
		var self = this;
		processes.forEach(function(process) {
			bOids.forEach(function(bOid) {
				self.benchmarkDefinitions.forEach(function(benchmarkDefinition) {
					if (bOid == benchmarkDefinition.oid) {
						benchmarkDefinition.models.forEach(function(model) {
							model.processDefinitions.forEach(function(procDef) {
								if (procDef.id == process.id) {
									var benchmarkProcessActivityArray = [];
									procDef.activities.forEach(function(activity) {
										if (activity.enableBenchmark) {
											var qualifiedActivityId = '{' + model.id + '}' + activity.id;
											benchmarkProcessActivityArray.push(qualifiedActivityId);
										}
									});
									benchmarkProcessActivityMap[process.id] = benchmarkProcessActivityArray;
								}
							});
						});
					}
				});
			});

		});

		return benchmarkProcessActivityMap;
	};

	/**
	 *
	 */

	TrafficLightViewController.prototype.getTlvStatsData = function(params) {
		var self = this;
		var deferred = _q.defer();
		var tlvData = {};
		if (!params) {
			tlvData.list = self.tlvStatsData.list;
			tlvData.totalCount = self.tlvStatsData.totalCount;
			angular.forEach(tlvData.list, function(object) {
				if (!(object.name == "Total Process" || object.name == "Total Activity")) {
					object.$leaf = false;
				}
			});
			deferred.resolve(tlvData);
		} else {
			var activityList = self.bATLVStatsMap[params.parent.id];
			if (activityList != undefined) {
				tlvData.list = activityList;
				tlvData.totalCount = activityList.length;
			} else {
				tlvData.list = [];
				tlvData.totalCount = 0;
			}

			deferred.resolve(tlvData);
		}

		return deferred.promise;
	}
	/**
	 *
	 * @param params
	 * @returns
	 */
	TrafficLightViewController.prototype.getTlvStatsDataByBO = function(params) {
		var self = this;
		var deferred = _q.defer();
		var tlvData = {};
		if (!params) {
			tlvData.list = self.tlvBOStatsTotal.list;
			tlvData.totalCount = self.tlvBOStatsTotal.totalCount;
			angular.forEach(tlvData.list, function(object) {
				object.$leaf = false;
			});

			deferred.resolve(tlvData);
		} else {
			if (params.parent.name == "Total") {
				tlvData.list = self.tlvBOStatsData.list;
				tlvData.totalCount = self.tlvBOStatsData.totalCount;
				angular.forEach(tlvData.list, function(object) {
					if (object.isGroup) {
						object.$leaf = false;
					}

				});
			} else {
				var drillDownList = self.tlvBOStatsDrillDownData[params.parent.name];
				if (drillDownList != undefined) {
					tlvData.list = drillDownList;
					tlvData.totalCount = drillDownList.length;
				} else {
					tlvData.list = [];
					tlvData.totalCount = 0;
				}
			}

			deferred.resolve(tlvData);
		}

		return deferred.promise;
	}

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
	TrafficLightViewController.prototype.setDataForProcessTable = function(id, state, isActivity, parentId,
			benchmarkIndex) {
		var self = this;
		self.selectedBenchmarkCategory = benchmarkIndex;
		self.state = state;

		if (isActivity) {
			self.processDataTable = null;
			self.processActivitiesMap = {};

			if (id != undefined) {
				self.processActivitiesMap[parentId] = [ id ];
			} else {
				self.processActivitiesMap = self.queryData.processActivityMap;
			}
			if (self.activityDataTable != undefined) {
				self.activityDataTable.refresh();
			} else {
				self.showActivityTable = true;
				self.showProcessTable = false;

			}
		} else {
			self.activityDataTable = null;
			self.selectedProcessIds = [];
			if (id != undefined) {
				self.selectedProcessIds.push(id);
			} else {
				self.queryData.processes.forEach(function(process) {
					self.selectedProcessIds.push(process.id)
				});
			}

			if (self.processDataTable != undefined) {
				self.processDataTable.refresh();
			} else {
				self.showProcessTable = true;
				self.showActivityTable = false;
			}
		}

	};
	/**
	 * This methos will be used for showing process or activity table for
	 * business objects.
	 */
	TrafficLightViewController.prototype.setDataForBOProcessActivityTable = function(instanceOids) {
		var self = this;
		self.instanceOids = instanceOids;
		if (self.selectedBOType == "ACTIVITIES") {
			if (self.activityDataTable != undefined) {
				self.activityDataTable.refresh();
			} else {
				self.showActivityTable = true;
				self.showProcessTable = false;

			}
		} else {
			if (self.processDataTable != undefined) {
				self.processDataTable.refresh();
			} else {
				self.showProcessTable = true;
				self.showActivityTable = false;
			}
		}
	}

	/**
	 *
	 * @param params
	 * @returns
	 */
	TrafficLightViewController.prototype.getProcesslistForTLV = function(params) {
		var self = this;
		var query = {};
		if (self.selectedDrillDown == "PROCESS_WORKITEM") {
			query = {
				'options' : params.options,
				'bOids' : self.queryData.bOids,
				'dateType' : self.queryData.dateType,
				'dayOffset' : self.queryData.dayOffset,
				'benchmarkCategory' : self.selectedBenchmarkCategory,
				'processIds' : self.selectedProcessIds,
				'state' : self.state,
				'drillDownType' : self.selectedDrillDown
			};
		} else {
			query = {
				'options' : params.options,
				'oids' : self.instanceOids,
				'drillDownType' : self.selectedDrillDown
			};
		}

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
		var query = {};
		if (self.selectedDrillDown == "PROCESS_WORKITEM") {
			query = {
				'options' : params.options,
				'bOids' : self.queryData.bOids,
				'dateType' : self.queryData.dateType,
				'dayOffset' : self.queryData.dayOffset,
				'benchmarkCategory' : self.selectedBenchmarkCategory,
				'processActivitiesMap' : self.processActivitiesMap,
				'state' : self.state,
				'drillDownType' : self.selectedDrillDown
			};
		} else {
			query = {
				'options' : params.options,
				'oids' : self.instanceOids,
				'drillDownType' : self.selectedDrillDown
			};
		}

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
	/**
	 *
	 */
	TrafficLightViewController.prototype.drillDownChange = function(favSelectedBusinessObject,
			favSelectedBusinessObjectInstances, favSelectedRelatedBusinessObject,
			favSelectedRelatedBusinessObjectInstances) {
		var self = this;
		if (self.selectedDrillDown == 'BUSINESS_OBJECT') {
			_sdBusinessObjectManagementService.getBusinessObjects().then(
					function(data) {
						self.businessObjectModels = data.models;
						self.refreshBusinessObjects();
						self.showBusinessObjects = true;

						// Setting the selectedBusinessObject from
						// favSelectedBusinessObject
						if (favSelectedBusinessObject != undefined) {
							angular.forEach(self.businessObjects, function(businessObject) {
								if (favSelectedBusinessObject.id == businessObject.id) {
									self.selectedBusinessObject = businessObject;
								}

							});
						}

						// calling method getBusinessObjectInstances to
						// synchronize view with favorite instance
						if (self.selectedBusinessObject != undefined) {
							self.getBusinessObjectInstances(favSelectedBusinessObjectInstances,
									favSelectedRelatedBusinessObject, favSelectedRelatedBusinessObjectInstances);
						}

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
	TrafficLightViewController.prototype.getBusinessObjectInstances = function(favSelectedBusinessObjectInstances,
			favSelectedRelatedBusinessObject, favSelectedRelatedBusinessObjectInstances) {
		var self = this;
		self.selectedBusinessObjectInstances = [];
		self.selectedRelatedBusinessObject = {};
		self.selectedRelatedBusinessObjectInstances = [];
		// check if the pre-existing boInstance map has data for selected
		// business object
		if (self.bOInstanceMap[self.selectedBusinessObject.businessObjectQualifiedId] == undefined) {
			// calling the service to get the business object instances for
			// selected BO.
			_sdBusinessObjectManagementService
					.getBusinessObjectInstances(self.selectedBusinessObject)
					.then(
							function(data) {

								angular.forEach(self.selectedBusinessObject.fields, function(field) {
									if (field.primaryKey) {
										self.primaryKeyForBO = field.id;
									}
								});
								// creating map of businessObjectQualifiedId and primarykey of Business Object
								self.bOPrimaryKeyMap[self.selectedBusinessObject.businessObjectQualifiedId] = self.primaryKeyForBO;
								// Creating map for Business Object instances.
								self.bOInstanceMap[self.selectedBusinessObject.businessObjectQualifiedId] = data;
								// Checking for related BO map using the selected BO qualified id.
								if (self.relatedBOMap[self.selectedBusinessObject.businessObjectQualifiedId] == undefined) {
									// getting related Business Objects for selected BO
									_sdBusinessObjectManagementService
											.getRelatedBusinessObject(self.selectedBusinessObject)
											.then(
													function(data) {
														self.refreshRelatedBusinessObjects(data);
														self.relatedBOMap[self.selectedBusinessObject.businessObjectQualifiedId] = self.relatedBusinessObjects;
														self.showGroupByObjects = true;
                                                        //Setting the selected related BO from favSelectedRelatedBusinessObject
														if (favSelectedRelatedBusinessObject != undefined) {
															angular.forEach(self.relatedBusinessObjects,function(relatedBusinessObject) {
																if (favSelectedRelatedBusinessObject.businessObjectQualifiedId == relatedBusinessObject.businessObjectQualifiedId) {
																	self.selectedRelatedBusinessObject = relatedBusinessObject;
																	//Setting the selected related BO instances from favorite SelectedRelatedBusinessObjectInstances
																    if (favSelectedRelatedBusinessObjectInstances != undefined) {
																	     self.getRelatedBOInstances(favSelectedRelatedBusinessObjectInstances);
																	}
																}

															});

														}
													}, function(error) {
														trace.log(error);
													});
								} else {
									self.relatedBusinessObjects = self.relatedBOMap[self.selectedBusinessObject.businessObjectQualifiedId];
									self.showGroupByObjects = true;

									if (favSelectedRelatedBusinessObject != undefined) {
										angular.forEach(self.relatedBusinessObjects,function(relatedBusinessObject) {
											if (favSelectedRelatedBusinessObject.businessObjectQualifiedId == relatedBusinessObject.businessObjectQualifiedId) {
												self.selectedRelatedBusinessObject = relatedBusinessObject;
												if (favSelectedRelatedBusinessObjectInstances != undefined) {
													self.getRelatedBOInstances(favSelectedRelatedBusinessObjectInstances);
												}
											}

										});

									}
								}

								if (favSelectedBusinessObjectInstances != undefined) {
									self.selectedBusinessObjectInstances = favSelectedBusinessObjectInstances;
								}

							}, function(error) {
								trace.log(error);
							});
		} else {
			self.primaryKeyForBO = self.bOPrimaryKeyMap[self.selectedBusinessObject.businessObjectQualifiedId];
			self.relatedBusinessObjects = self.relatedBOMap[self.selectedBusinessObject.businessObjectQualifiedId];
			self.showGroupByObjects = true;

			if (favSelectedRelatedBusinessObject != undefined) {
				angular.forEach(self.relatedBusinessObjects,function(relatedBusinessObject) {
					if (favSelectedRelatedBusinessObject.businessObjectQualifiedId == relatedBusinessObject.businessObjectQualifiedId) {
					   self.selectedRelatedBusinessObject = relatedBusinessObject;
					}
				});

			}

			if (favSelectedBusinessObjectInstances != undefined) {
				self.selectedBusinessObjectInstances = favSelectedBusinessObjectInstances;
			}

			if (favSelectedRelatedBusinessObjectInstances != undefined) {
				self.getRelatedBOInstances(favSelectedRelatedBusinessObjectInstances);
			}
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
				// creating map of businessObjectQualifiedId and PrimaryKeyType
				angular
						.forEach(
								self.businessObjectModels[n].businessObjects[m].fields,
								function(object) {
									if (object.primaryKey) {
										self.bOPrimaryKeyTypeMap[self.businessObjectModels[n].businessObjects[m].businessObjectQualifiedId] = object.type;
									}
								});

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
			if (v[self.primaryKeyForBO].toString().indexOf(matchVal) > -1) {
				results.push(v);
			}
		});

		self.businessObjectInstancesData = results;
	};
	/**
	 *
	 */
	TrafficLightViewController.prototype.getRelatedBOInstances = function(favSelectedRelatedBusinessObjectInstances) {
		var self = this;
		self.selectedRelatedBusinessObjectInstances = [];
		var primaryKeys = [];
		if (self.selectedRelatedBusinessObject != null) {
			angular.forEach(self.selectedBusinessObjectInstances, function(selBOInstance) {

				angular.forEach(selBOInstance[self.selectedRelatedBusinessObject.otherForeignKeyField], function(id) {
					if (id != null) {
						primaryKeys.push(id);
					}

				});

			});

			_sdBusinessObjectManagementService.getRelatedBusinessObjectInstances(
					self.selectedRelatedBusinessObject.businessObjectQualifiedId, primaryKeys).then(function(data) {
				self.realatedBusinessObjectInstances = data;
				if (favSelectedRelatedBusinessObjectInstances != undefined) {
					self.selectedRelatedBusinessObjectInstances = favSelectedRelatedBusinessObjectInstances;
				}

			}, function(error) {
				trace.log(error);
			});

		}
	};
	/**
	 *
	 * @param relatedMatchVal
	 */
	TrafficLightViewController.prototype.getRelatedBOInstanceMatches = function(relatedMatchVal) {
		var self = this;
		var results = [];
		var boInstancedata = self.realatedBusinessObjectInstances;

		boInstancedata.forEach(function(v) {
			if (v[self.selectedRelatedBusinessObject.otherForeignKeyField].toString().indexOf(relatedMatchVal) > -1) {
				results.push(v);
			}
		});

		self.relatedBusinessObjectInstancesData = results;
	};
	/**
	 *
	 */
	TrafficLightViewController.prototype.getfontSize = function(count) {
		var fontSize = 10;
		if (count != 0) {
			fontSize = fontSize + count + 2;
		}
		return fontSize;
	};


	/**
	 *
	 * @param res
	 */
	TrafficLightViewController.prototype.updateFavorite = function(res) {
		var favoriteData = {};
		var self = this;
		favoriteData.selectedProcesses = self.selectedProcesses;
		favoriteData.selectedBenchmarks = self.selectedBenchmarks;
		favoriteData.selectedDrillDown = self.selectedDrillDown;
		favoriteData.showBusinessObjects = self.showBusinessObjects;
		favoriteData.showGroupByObjects = self.showGroupByObjects;
		if (self.showBusinessObjects) {
			favoriteData.selectedBusinessObject = self.selectedBusinessObject;
			favoriteData.selectedBusinessObjectInstances = self.selectedBusinessObjectInstances;
			favoriteData.selectedBOType = self.selectedBOType;
		}

		if (self.showGroupByObjects) {
			favoriteData.selectedRelatedBusinessObject = self.selectedRelatedBusinessObject;
			favoriteData.selectedRelatedBusinessObjectInstances = self.selectedRelatedBusinessObjectInstances;
		}

		favoriteData.selectedDateType = self.selectedDateType;
		favoriteData.dayOffset = self.dayOffset;

		_sdFavoriteViewService.updateFavorite("trafficLightViewNew", self.tlvReportName, favoriteData).then(
				function(data) {

					if (self.favoriteName == "New TLV") {
						self.favoriteName = self.tlvReportName;
						_sdViewUtilService.updateViewInfo("trafficLightViewNew", "", {
							"preferenceName" : self.favoriteName
						});
					}else{
						self.tlvReportName = self.favoriteName;
					}
					trace.log(data);
					self.showDeleteButton = true;
				}, function(error) {
					trace.log(error);
				});
	};

	/**
	 *
	 * @param params
	 * @param $scope
	 */
	TrafficLightViewController.prototype.getFavoriteByName = function(params, $scope) {

		var self = this;
		// Calling REST service to get the favorite by name.
		_sdFavoriteViewService.getFavoriteByName(params.preferenceId, params.preferenceName).then(
				function(data) {
					self.showDeleteButton = true;
					self.favoriteName = params.preferenceName;
					trace.log(data);
					var values = _parse(data.preferenceValue);
					var favoriteData = values($scope);
                    trace.log("FavoriteData:-", favoriteData);
					self.selectedProcesses = [];
					self.selProcesses = [];
					// Setting selected processes from favorite
					angular.forEach(favoriteData.selectedProcesses, function(favProcess) {
						angular.forEach(self.processes, function(process) {
							if (favProcess.id == process.id) {
								self.selectedProcesses.push(favProcess);
								self.selProcesses.push(JSON.stringify(favProcess));
							}

						});
					});

					// setting all TLV criteria from favorite
					self.selectedBenchmarks = favoriteData.selectedBenchmarks;
					self.selectedDrillDown = favoriteData.selectedDrillDown;
					self.drillDownChange(favoriteData.selectedBusinessObject,
							favoriteData.selectedBusinessObjectInstances, favoriteData.selectedRelatedBusinessObject,
							favoriteData.selectedRelatedBusinessObjectInstances);
					self.showBusinessObjects = favoriteData.showBusinessObjects;
					self.showGroupByObjects = favoriteData.showGroupByObjects;
					self.selectedBOType = favoriteData.selectedBOType;

					self.selectedDateType = favoriteData.selectedDateType;
					self.dayOffset = favoriteData.dayOffset;
				}, function(error) {
					trace.log(error);
				});
	};


	/**
	 *
	 * @param params
	 * @param $scope
	 */
	TrafficLightViewController.prototype.deleteFavorite = function() {

		var self = this;
		// Calling REST service to get the favorite by name.
		_sdFavoriteViewService.deleteFavorite("trafficLightViewNew", self.favoriteName).then(
				function(data) {
					_sdViewUtilService.closeView("trafficLightViewNew", 'id='+ self.favoriteName);
					self.showDeleteButton = false;
					trace.log(data);
				}, function(error) {
					trace.log(error);
				});
	};
	/**
	 *
	 */
	TrafficLightViewController.prototype.saveFavoritePopupClose = function(){
		var self = this;
		self.tlvReportName = self.favoriteName;
		self.saveFavoritePopup.close();
	};
	/**
	 *
	 */
	TrafficLightViewController.prototype.processChange = function(){
		var self = this;
		var selectedProcesses = [];
		angular.forEach(self.selProcesses, function(process){
			selectedProcesses.push(JSON.parse(process));
		});

		self.selectedProcesses = selectedProcesses;
	};
})();
