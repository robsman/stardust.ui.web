/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Aditya.Gaikwad
 */

(function() {
	'use strict';

	angular.module('workflow-ui').controller(
			'sdProcessSearchViewCtrl',
			[ '$scope', 'sdUtilService', 'sdViewUtilService',
					'sdProcessSearchService', '$q', '$filter','sdLoggerService',
					ProcessSearchViewCtrl ]);

	var _sdUtilService;
	var _sdViewUtilService;
	var _sdProcessSearchService;
	var _q;
	var _filter;
	var trace;

	/*
	 *
	 */
	function ProcessSearchViewCtrl($scope, sdUtilService, sdViewUtilService,
			sdProcessSearchService, $q, $filter, sdLoggerService) {
		// Register for View Events
		sdViewUtilService.registerForViewEvents($scope, this.handleViewEvents,
				this);

		// Preserve to use later in life-cycle
		_sdUtilService = sdUtilService;
		_sdViewUtilService = sdViewUtilService;
		_sdProcessSearchService = sdProcessSearchService;
		_q = $q;
		_filter = $filter;
		trace = sdLoggerService.getLogger('bpm-processportal.ProcessSearchViewCtrl');

		this.initialize(_sdViewUtilService.getViewParams($scope));

		/*
		 * This needs to be defined here as it requires access to $scope
		 */
		ProcessSearchViewCtrl.prototype.safeApply = function() {
			sdUtilService.safeApply($scope);
		};
	}

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.handleViewEvents = function(event) {
		if (event.type == "ACTIVATED") {
			this.refresh();
		} else if (event.type == "DEACTIVATED") {
			// TODO
		}
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.initialize = function(params) {

		this.defaultActivity = {
			value : "All",
			label : _sdProcessSearchService
					.getI18MessageString('business-control-center-messages.views-processSearchView-allActivities'),
			name : _sdProcessSearchService
					.getI18MessageString('business-control-center-messages.views-processSearchView-allActivities'),
			order : 0,
		};

		this.defaultProcess = {
			value : "All",
			label : _sdProcessSearchService
					.getI18MessageString('business-control-center-messages.messages-common-allProcesses'),
			name : _sdProcessSearchService
					.getI18MessageString('business-control-center-messages.messages-common-allProcesses'),
			order : 0,
		};

		OPTIONS.AM = _sdProcessSearchService.getI18MessageString('html5-common.date-picker-meridian-am', 'am');
		OPTIONS.PM = _sdProcessSearchService.getI18MessageString('html5-common.date-picker-meridian-pm', 'pm');

		this.processDataTable = null;
		this.activityDataTable = null;
		this.showSearchCriteria = true;
		this.priorities = [];
		this.descritorCols = [];
		this.activitySrchState = [];
		this.activitySrchCriticality = [];
		this.procSrchProcess = [];
		this.allBusinessProcesses = [];
		this.procSrchAuxProcess = [];
		this.procSrchActivities = [];
		this.procSrchAuxActivities = [];
		this.defineData();

		this.activitiesQIDMap = [];
		this.activitiesQIDProcessMap = [];

		this.HIERARCHY_PROCESS = "PROCESS";
		this.HIERARCHY_PROCESS_AND_CASE = "PROCESS_AND_CASE";
		this.HIERARCHY_CASE = "CASE";
		this.HIERARCHY_ROOT_PROCESS = "ROOT_PROCESS";
		this.CASE_PROCESS_ID = "{PredefinedModel}CaseProcess";
		this.caseProcDef = {};
		this.descBoolOptions = {};
		this.selected = {};
		this.archiveAuditTrailURL = '';
		this.oldestAuditTrailEntry = '';

		this.SEARCH_OPT = "searchOption";
		this.SEARCH_OPT_PROCESS = "PROC";
		this.SEARCH_OPT_ACTIVITY = "ACT";
		this.STARTED_FROM = "startedFrom";
		this.STARTED_TO = "startedTo";
		this.END_TIME_FROM = "endTimeFrom";
		this.END_TIME_TO = "endTimeTo";
		this.STATE = "state";
		this.PRIORITY = "priority";
		this.OID = "oid";
		this.ROOT_OID = "rootOid";
		this.HIERARCHY = "hierarchy";
		this.CASE_OWNER = "caseOwner";
		this.PROCESS_FILTERS = "processFilters";
		this.PROCESSES = "processes";
		this.DESCRIPTORS = "descriptors";
		this.MODIFY_TIME_FROM = "modifyTimeFrom";
		this.MODIFY_TIME_TO = "modifyTimeTo";
		this.ACTIVITY_FILTERS = "activityFilters";
		this.ACTIVITIES = "activities";
		this.PERFORMER = "performer";
		this.CRITICALITY = "criticality";

		this.INTERACTIVE_ACTIVITIES = "InteractiveActivities";
		this.NONINTERACT_ACTIVITIES = "NonInteractiveActivities";
		this.AUXILIARY_ACTIVITIES = "AuxiliaryActivities";
		this.AUXILIARY_PROCESSES = "AuxiliaryProcesses";

		this.dateFormat = "mm/dd/y";

		var self = this;
		var promises = [];

		var busProcDeferred = _q.defer();
		promises.push(busProcDeferred.promise);

		_sdProcessSearchService
				.getAllBusinessProcesses(false)
				.then(
						function(processes) {
							setOrder(processes);
							self.allBusinessProcesses
									.push(self.defaultProcess);
							self.allBusinessProcesses = self.allBusinessProcesses
									.concat(processes);
							self.procSrchProcessSelected = [ self.allBusinessProcesses[0] ];
							self.procSrchProcess = self.allBusinessProcesses;

							// Extract and store Case Process Definition
							var casePresentPos = self.searchCaseProcess();
							if (casePresentPos > -1) {
								self.caseProcDef = self.procSrchProcess[casePresentPos];
							}
							// Remove Case process as defualt Hiearchy is excluding Case Process
							self.filterCaseProcess();

							//Create Processes and Activities lookup map Object
							self.createProcessesActivitiesMap();
							busProcDeferred.resolve();
						});

		var procPriorityDeferred = _q.defer();
		promises.push(procPriorityDeferred.promise);

		_sdProcessSearchService
				.getAllPriorities()
				.then(
						function(priorities) {
							self.priorities
									.push({
										value : "-9999",
										label : _sdProcessSearchService
												.getI18MessageString('business-control-center-messages.views-processSearchView-chooseProcess-options-all-label'),
										name : "all",
										order: 0,

									});
							self.priorities = self.priorities
									.concat(priorities);
							self.query.processSearchCriteria.processSrchPrioritySelected = self.priorities[0].value;
							procPriorityDeferred.resolve();
						});

		var procStatesDeferred = _q.defer();
		promises.push(procStatesDeferred.promise);
		_sdProcessSearchService
				.getProcessStates()
				.then(
						function(states) {
							self.procSrchState = states;
							self.query.processSearchCriteria.procSrchStateSelected = self.procSrchState[0].value;
							procStatesDeferred.resolve();
						});

		var procHierarchyDeferred = _q.defer();
		promises.push(procHierarchyDeferred.promise);
		_sdProcessSearchService
				.getProcessHierarchy()
				.then(
						function(hierarchyTypes) {
							self.procSrchHierarchy = hierarchyTypes;
							self.query.processSearchCriteria.procSearchHierarchySelected = self.procSrchHierarchy[0].value;
							procHierarchyDeferred.resolve();
						});

		var procCriticalitiesDeferred = _q.defer();
		promises.push(procCriticalitiesDeferred.promise);
		_sdProcessSearchService
				.getAllCriticalities()
				.then(
						function(criticalities) {
							self.activitySrchCriticality
									.push({
										value : "10",
										label : _sdProcessSearchService
												.getI18MessageString('business-control-center-messages.views-processSearchView-chooseProcess-options-all-label'),
										name : "all",
									});

							self.activitySrchCriticality = self.activitySrchCriticality
									.concat(criticalities);
							self.query.processSearchCriteria.activitySrchCriticalitySelected = self.activitySrchCriticality[0].label;
							procCriticalitiesDeferred.resolve();
						});

		var procSearchOptionsDeferred = _q.defer();
		promises.push(procSearchOptionsDeferred.promise);
		_sdProcessSearchService
				.searchAttributes()
				.then(
						function(searchFor) {
							self.searchOptions = searchFor;
							self.query.processSearchCriteria.filterObject = self.searchOptions[0].value;
							procSearchOptionsDeferred.resolve();
						}, function(error) {
							trace.log(error);
						});

		var actStatesDeferred = _q.defer();
		promises.push(actStatesDeferred.promise);
		_sdProcessSearchService
				.getAllActivityStates()
				.then(
						function(states) {
							self.activitySrchState = [ {
								value : 3,
								label : _sdProcessSearchService
										.getI18MessageString('business-control-center-messages.views-processSearchView-chooseProcess-options-alive-label'),
								name : "alive",
							} ];

							self.activitySrchState = self.activitySrchState
									.concat(states);

							var default_state = [ {
								value : -9999,
								label : _sdProcessSearchService
										.getI18MessageString('business-control-center-messages.views-processSearchView-chooseProcess-options-all-label'),
								name : "all",
							} ];
							self.activitySrchState = self.activitySrchState
									.concat(default_state);
							self.query.processSearchCriteria.activitySrchStateSelected = self.activitySrchState[0].value;
							actStatesDeferred.resolve();
						});

		var descBoolOptionsDeferred = _q.defer();
		promises.push(descBoolOptionsDeferred.promise);
		_sdProcessSearchService.getDescBoolOptions().then(
				function(descBoolOptions) {
					self.descBoolOptions = descBoolOptions;
					descBoolOptionsDeferred.resolve();
				});

		var archiveURLDeferred = _q.defer();
		promises.push(archiveURLDeferred.promise);
		_sdProcessSearchService.getArchiveAuditTrailURL().then(
				function(archiveAuditTrail) {
					self.archiveAuditTrailURL = archiveAuditTrail.archiveAuditTrailURL;
					archiveURLDeferred.resolve();
				});

		var oldestAuditTrailDeferred = _q.defer();
		promises.push(oldestAuditTrailDeferred.promise);
		_sdProcessSearchService.getOldestAuditTrailEntry().then(
				function(oldestAuditTrail) {
					self.oldestAuditTrailEntry = oldestAuditTrail.oldestAuditTrailEntry;
					oldestAuditTrailDeferred.resolve();
				});

		this.showProcSearchResult = false;

		_q.all(promises).then(function() {
			if (Object.keys(params).length != 0) {
				self.prePopulateCriteria(params).then(function() {
					self.search();
				});
			}
		});




	};

	/**
	 *
	 */
	ProcessSearchViewCtrl.prototype.defineData = function() {
		var self = this;
		self.query = {
			processSearchCriteria : {
				filterObject : '',
				procStartFrom : '',
				procStartTo : '',
				procEndFrom : '',
				procEndTo : '',
				procSearchHierarchySelected : '',
				showAuxiliaryProcess : false,
				showInteractiveActivities : true,
				showNonInteractiveActivities : false,
				showAuxiliaryActivities : false,
				procSrchProcessSelected : [],
				procSrchStateSelected : '',
				processSrchPrioritySelected : '',
				processSrchCaseOwner : '',
				processSrchRootProcessOID : '',
				processSrchProcessOID : '',
				actStartFrom : '',
				actStartTo : '',
				actModifyFrom : '',
				actModifyTo : '',
				activitySrchStateSelected : '',
				activitySrchCriticalitySelected : '',
				activitySrchActivityOID : '',
				activitySrchPerformer : '',
				descriptors : {
					formatted : {}
				},
			}
		};

		self.partialAuthor = "";

	}

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.performSearch = function(options) {
		var self = this;
		var deferred = _q.defer();
		self.processCaseOwner();
		self.processActPerformer();

		this.query.options = options.options;
		_sdProcessSearchService.performSearch(this.query).then(function(data) {
			self.data = {};
			self.data.totalCount = data.totalCount;
			self.data.list = data.list;

			deferred.resolve(self.data);
		}, function(error) {
			deferred.reject(error);
		});

		return deferred.promise;
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.processChange = function() {
		var deferred = _q.defer();
		this.showProcSearchResult = false;
		this.descritorCols = [];
		if (this.query.processSearchCriteria.filterObject == 1) {
			this.procSrchActivities = this.calculateSelectedProcessActivities();

			this.applyActivityFilters();

			this.procSrchActivities.splice(0, 0, this.defaultActivity);
			this.activitySrchSelected = [ this.procSrchActivities[0] ];

			this.query.processSearchCriteria.activitySrchStateSelected = this.activitySrchState[0].value;
			this.query.processSearchCriteria.activitySrchCriticalitySelected = this.activitySrchCriticality[0].label;
		}

		if (this.procSrchProcessSelected[0]
				&& !(this.procSrchProcessSelected[0].value && this.procSrchProcessSelected[0].value == "All")) {
			var selectedProcDefIds = getSelectedProcDefIds(this.procSrchProcessSelected);
			var self = this;
			_sdProcessSearchService
					.getCommonDescriptors(selectedProcDefIds, false)
					.then(
							function(commonDescriptors) {
								angular
										.forEach(
												commonDescriptors,
												function(descriptor) {
													self.descritorCols
															.push({
																id : descriptor.id,
																field : "descriptorValues['"
																		+ descriptor.id
																		+ "'].value",
																title : descriptor.title,
																dataType : descriptor.type,
																detailedType : descriptor.detailedType
															});
												});
								deferred.resolve();
							});
		} else {
			deferred.resolve();
		}
		return deferred.promise;
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.refresh = function() {
		if (this.query.processSearchCriteria.filterObject == 0) {
			if (this.processDataTable != null) {
				this.processDataTable.refresh(true);
			}
		} else {
			if (this.activityDataTable != null) {
				this.activityDataTable.refresh(true);
			}
		}
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.search = function() {
		if(!this.validateData()) {
			return;
		}

		this.processSearchData();
		this.showSearchCriteria = false;
		this.showProcSearchResult = true;
		this.refresh();
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.processSearchData = function() {

		this.processSelectedProcesses();

		if (this.query.processSearchCriteria.filterObject == 1) {
			var selectedActivities = this.activitySrchSelected;
			var selectedActRuntimeElementOids = '';
			var selectedActRuntimeElementOidsArr = [];
			for ( var act in selectedActivities) {
				if (selectedActivities[act].id) {
					selectedActRuntimeElementOids += ","
							+ selectedActivities[act].runtimeElementOid;
					selectedActRuntimeElementOidsArr
							.push(selectedActivities[act].runtimeElementOid);
				}
			}
			selectedActRuntimeElementOids = selectedActRuntimeElementOids
					.substr(1);
			if (selectedActRuntimeElementOidsArr.length == 0) {
				this.query.processSearchCriteria.activitySrchSelected = [ this.procSrchActivities[0].value ];
			} else {
				this.query.processSearchCriteria.activitySrchSelected = selectedActRuntimeElementOidsArr
			}
		}


		if (this.selected) {
			for ( var item in this.selected) {
				for ( var index in this.descritorCols) {
					if (this.descritorCols[index].id == item) {
						var obj = {};
						if (this.descritorCols[index].dataType == "STRING") {
							if(!_sdUtilService.isEmpty(this.selected[this.descritorCols[index].id])) {
								obj['textSearch'] = this.selected[this.descritorCols[index].id];
							}
						} else if (this.descritorCols[index].dataType == "BOOLEAN") {
							obj['equals'] = (this.selected[this.descritorCols[index].id] == 0)? 'true' : 'false';
						} else if (this.descritorCols[index].dataType == "NUMBER" ) {
							if(!_sdUtilService.isEmpty(this.selected[this.descritorCols[index].id])) {
								obj['from'] = this.selected[this.descritorCols[index].id];
								obj['to'] = this.selected[this.descritorCols[index].id];
							}
						}else if (this.descritorCols[index].dataType == "DATE") {
							obj['from'] = this.selected[this.descritorCols[index].id].from;
							obj['to'] = this.selected[this.descritorCols[index].id].to;
						}else if (this.descritorCols[index].dataType == "LIST") {
							if(!_sdUtilService.isEmpty(this.selected[this.descritorCols[index].id])) {
								obj['textSearch'] = this.selected[this.descritorCols[index].id];
							}
						}

						this.query.processSearchCriteria.descriptors.formatted[item] = obj;
						break;
					}
				}
			}
		}
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.reset = function() {
		this.removeFormErrors();
		this.defineData();
		this.query.processSearchCriteria.filterObject = this.searchOptions[0].value;
		this.procSrchProcessSelected = [ this.procSrchProcess[0] ];
		this.query.processSearchCriteria.procSearchHierarchySelected = this.procSrchHierarchy[0].value;
		this.query.processSearchCriteria.procSrchStateSelected = this.procSrchState[0].value;
		this.query.processSearchCriteria.processSrchPrioritySelected = this.priorities[0].value;
		this.processSrchCaseOwner = "";
		this.activitySrchPerformer = "";

		this.descritorCols = [];
		this.filterProcessDefinitionList();

		this.selected = {};
		this.query.processSearchCriteria.descriptors.formatted = {};
		this.showProcSearchResult = false;
	}

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.toggleAuxiliaryProcess = function() {
		var deferred = _q.defer();
		if (this.query.processSearchCriteria.showAuxiliaryProcess) {
			this.query.processSearchCriteria.showAuxiliaryProcess = false;
			this.procSrchProcess = this.allBusinessProcesses;
			deferred.resolve();
		} else {
			this.query.processSearchCriteria.showAuxiliaryProcess = true;
			var self = this;
			if (self.procSrchAuxProcess.length == 0) {
				this.getAllUniqueProcesses(false).then(function(processes) {
					self.procSrchProcess = processes;
					self.initializeCriteria();
					//Create Processes and Activities lookup map Object
					self.createProcessesActivitiesMap();
					deferred.resolve();
					return;
				});
			} else {
				self.procSrchProcess = self.procSrchAuxProcess;
				self.initializeCriteria();
				deferred.resolve();
				return;
			}
		}
		this.initializeCriteria();
		return deferred.promise;
	}


	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.initializeCriteria = function() {
		this.procSrchProcessSelected = [ this.procSrchProcess[0] ];
		this.descritorCols = [];
		this.filterProcessDefinitionList();
	}

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.toggleInteractiveActivities = function() {
		if (this.query.processSearchCriteria.showInteractiveActivities) {
			this.query.processSearchCriteria.showInteractiveActivities = false;
		} else {
			this.query.processSearchCriteria.showInteractiveActivities = true;
		}
		this.postToggleActivities();
	}

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.toggleNonInteractiveActivities = function() {
		if (this.query.processSearchCriteria.showNonInteractiveActivities) {
			this.query.processSearchCriteria.showNonInteractiveActivities = false;
		} else {
			this.query.processSearchCriteria.showNonInteractiveActivities = true;
		}
		this.postToggleActivities();
	}

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.toggleAuxiliaryActivities = function() {
		if (this.query.processSearchCriteria.showAuxiliaryActivities) {
			this.query.processSearchCriteria.showAuxiliaryActivities = false;
		} else {
			this.query.processSearchCriteria.showAuxiliaryActivities = true;
		}
		this.postToggleActivities();
	}

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.processHierarchyChange = function() {
		this.procSrchProcessSelected = [ this.procSrchProcess[0] ];
		this.filterProcessDefinitionList();
		this.processChange();
		this.processSrchCaseOwner = "";
	}

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.filterProcessDefinitionList = function() {
		if (this.query.processSearchCriteria.procSearchHierarchySelected == this.HIERARCHY_PROCESS
				|| this.query.processSearchCriteria.procSearchHierarchySelected == this.HIERARCHY_ROOT_PROCESS) {
			// If ProcessDefinition list contains Case PD, remove the case PD
			// and populate Processes select box.
			this.filterCaseProcess();
		} else if (this.query.processSearchCriteria.procSearchHierarchySelected == this.HIERARCHY_CASE
				|| this.query.processSearchCriteria.procSearchHierarchySelected == this.HIERARCHY_PROCESS_AND_CASE) {
			// If ProcessDefinition list does not contain Case PD, add the case
			// PD and populate Processes select box.
			this.addCaseProcess();
		}
	}

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.searchCaseProcess = function() {
		var casePresentPos = -1;
		for ( var procDef in this.procSrchProcess) {
			if (this.procSrchProcess[procDef].id
					&& this.procSrchProcess[procDef].id == this.CASE_PROCESS_ID) {
				casePresentPos = procDef;
				break;
			}
		}
		return casePresentPos;
	}

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.filterCaseProcess = function() {
		var casePresentPos = this.searchCaseProcess();
		if (casePresentPos > -1) {
			this.procSrchProcess.splice(casePresentPos, 1);
		}
	}

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.addCaseProcess = function() {
		var casePresentPos = this.searchCaseProcess();
		if (casePresentPos == -1) {
			this.procSrchProcess.splice(1, 0, this.caseProcDef);
		}
	}

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.getAllUniqueProcesses = function() {
		var self = this;
		var deferred = _q.defer();

		_sdProcessSearchService
				.getAllUniqueProcesses()
				.then(
						function(processes) {
							setOrder(processes);
							self.procSrchAuxProcess
									.push({
										value : "All",
										label : _sdProcessSearchService
												.getI18MessageString('business-control-center-messages.messages-common-allProcesses'),
										name : _sdProcessSearchService
												.getI18MessageString('business-control-center-messages.messages-common-allProcesses'),
										order : 0,
									});
							self.procSrchAuxProcess = self.procSrchAuxProcess
									.concat(processes);
							self.createProcessesActivitiesMap();
							deferred.resolve(self.procSrchAuxProcess);
						});
		return deferred.promise;
	}

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.applyActivityFilters = function() {
		// For Interactive Activities
		if (!this.query.processSearchCriteria.showInteractiveActivities) {
			for (var i = this.procSrchActivities.length - 1; i >= 0; i--) {
				if (this.procSrchActivities[i].interactive) {
					this.procSrchActivities.splice(i, 1);
				}
			}
		}

		// For Non-Interactive Activities
		if (!this.query.processSearchCriteria.showNonInteractiveActivities) {
			for (var i = this.procSrchActivities.length - 1; i >= 0; i--) {
				if (!this.procSrchActivities[i].interactive) {
					this.procSrchActivities.splice(i, 1);
				}
			}
		}

		// For Auxiliary Activities
		if (!this.query.processSearchCriteria.showAuxiliaryActivities) {
			for (var i = this.procSrchActivities.length - 1; i >= 0; i--) {
				if (this.procSrchActivities[i].auxillary) {
					this.procSrchActivities.splice(i, 1);
				}
			}
		}
	}

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.performArchiveSearch = function(options) {
		var criteria = this.buildSearchCriteria();

	    openArchiveSearch(this.archiveAuditTrailURL, criteria);
	};

	function openArchiveSearch(url, criteria) {
		var message = '{"type": "OpenView", "data": {"viewId": "processSearchView", "params": ' + JSON.stringify(criteria) + '}}';

		// url will always end with "/"
		url += "main.html?uicommand=" + message;
		if (window.console) {
			console.log('Archive Search URL: ', url);
		}

		parent.BridgeUtils.openWindow(url, 'ArchivePortal');
	}


	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.buildSearchCriteria = function() {
		var criteria = {};
		var selectedSearchOption = this.query.processSearchCriteria.filterObject;
		 if (selectedSearchOption == "0")
         {
			 criteria[this.SEARCH_OPT] = this.SEARCH_OPT_PROCESS;
			 this.buildProcessSearchCriteria(criteria);
         }
         else if (selectedSearchOption == "1")
         {
        	criteria[this.SEARCH_OPT] = this.SEARCH_OPT_ACTIVITY;
        	this.buildActivitySearchCriteria(criteria);
         }
		 return criteria;
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.buildProcessSearchCriteria = function(criteria) {

	      if (this.query.processSearchCriteria.procStartFrom) {
	         criteria[this.STARTED_FROM] = this.processDate(this.query.processSearchCriteria.procStartFrom);
	      }

	      if (this.query.processSearchCriteria.procStartTo) {
	    	  criteria[this.STARTED_TO] = this.processDate(this.query.processSearchCriteria.procStartTo);
	      }

	      if (this.query.processSearchCriteria.procEndFrom) {
	    	  criteria[this.END_TIME_FROM] = this.processDate(this.query.processSearchCriteria.procEndFrom);
	      }

	      if (this.query.processSearchCriteria.procEndTo) {
	    	  criteria[this.END_TIME_TO] = this.processDate(this.query.processSearchCriteria.procEndTo);
	      }

	      criteria[this.STATE] = findValueById(this.procSrchState, this.query.processSearchCriteria.procSrchStateSelected);

	      criteria[this.PRIORITY] = findValueById(this.priorities, this.query.processSearchCriteria.processSrchPrioritySelected);

	      if ('' != this.query.processSearchCriteria.processSrchRootProcessOID) {
		         criteria[this.ROOT_OID] = this.query.processSearchCriteria.processSrchRootProcessOID;
	      }

	      if ('' != this.query.processSearchCriteria.processSrchProcessOID) {
	         criteria[this.OID] = this.query.processSearchCriteria.processSrchProcessOID;
	      }

	      if ('' != this.query.processSearchCriteria.procSearchHierarchySelected) {
	         criteria[this.HIERARCHY] = this.query.processSearchCriteria.procSearchHierarchySelected;
	      }

	      var caseOwner = this.processCaseOwner();
	      if ('' != caseOwner) {
	    	  criteria[this.CASE_OWNER] = caseOwner;
	      }

	      if (this.query.processSearchCriteria.showAuxiliaryProcess) {
	    	  criteria[this.PROCESS_FILTERS] = [this.AUXILIARY_PROCESSES];
	      }

	      criteria[this.PROCESSES] = this.processSelectedProcesses(this.query.processSearchCriteria.procSrchProcessSelected);

	      var descObject = this.processDescriptors();
	      if (Object.keys(descObject).length !== 0) {
	    	  criteria[this.DESCRIPTORS] = descObject;
	      }
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.processCaseOwner = function() {
		if (this.processSrchCaseOwner != undefined
				&& this.processSrchCaseOwner.length == 1) {
			this.query.processSearchCriteria.processSrchCaseOwner = this.processSrchCaseOwner[0].id;
		} else {
			this.query.processSearchCriteria.processSrchCaseOwner = "";
		}
		return this.query.processSearchCriteria.processSrchCaseOwner;
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.processDescriptors = function() {
		var descObject = {};
		if (this.selected) {
			for ( var item in this.selected) {
				for ( var index in this.descritorCols) {
					if (this.descritorCols[index].id == item) {
						var obj = {};
						if (this.descritorCols[index].dataType == "STRING") {
							descObject[item] = this.selected[item];
						} else if (this.descritorCols[index].dataType == "BOOLEAN") {
							descObject[item] = (this.selected[this.descritorCols[index].id] == 0)? true : false;
						} else if (this.descritorCols[index].dataType == "NUMBER") {
							descObject[item] = Number(this.selected[item]);
						} else if (this.descritorCols[index].dataType == "DATE") {
							//TODO handle Date
						}
						break;
					}
				}
			}
		}
		return descObject;
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.processSelectedProcesses = function() {
		var selectedProcDefIdsArr = getSelectedProcDefIds(this.procSrchProcessSelected, true);

		if (selectedProcDefIdsArr.length == 0) {
			this.query.processSearchCriteria.procSrchProcessSelected = [ this.procSrchProcess[0].value ];
		} else {
			this.query.processSearchCriteria.procSrchProcessSelected = selectedProcDefIdsArr;
		}
		return this.query.processSearchCriteria.procSrchProcessSelected;
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.prePopulateCriteria = function(params) {
		var deferred = _q.defer();
		this.processDefaults(params);
		if (this.SEARCH_OPT_PROCESS == params[this.SEARCH_OPT]) {
			this.query.processSearchCriteria.filterObject = "0";
			this.prePopulateProcessCriteria(params).then(function() {
				deferred.resolve();
			});
		} else if (this.SEARCH_OPT_ACTIVITY == params[this.SEARCH_OPT]) {
			this.query.processSearchCriteria.filterObject = "1";
			this.prePopulateActivityCriteria(params).then(function() {
				deferred.resolve();
			});
		}
		return deferred.promise;
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.prePopulateProcessCriteria = function(params) {
		var deferred = _q.defer();
		if (params[this.STARTED_FROM]) {
			this.query.processSearchCriteria.procStartFrom = this.prePopulateDate(params[this.STARTED_FROM]);
	      }

	      if (params[this.STARTED_TO]) {
	    	  this.query.processSearchCriteria.procStartTo = this.prePopulateDate(params[this.STARTED_TO]);
	      }

	      if (params[this.END_TIME_FROM]) {
	    	  this.query.processSearchCriteria.procEndFrom = this.prePopulateDate(params[this.END_TIME_FROM]);
	      }

	      if (params[this.END_TIME_TO]) {
	    	  this.query.processSearchCriteria.procEndTo = this.prePopulateDate(params[this.END_TIME_TO]);
	      }

	      this.query.processSearchCriteria.procSrchStateSelected = findIdByValue(this.procSrchState, params[this.STATE]);

	      this.query.processSearchCriteria.processSrchPrioritySelected = findIdByValue(this.priorities, params[this.PRIORITY]);

	      if ('' != params[this.ROOT_OID]) {
	    	  this.query.processSearchCriteria.processSrchRootProcessOID = params[this.ROOT_OID];
	      }

	      if ('' != params[this.OID]) {
	    	  this.query.processSearchCriteria.processSrchProcessOID = params[this.OID];
	      }

	      if ('' != params[this.HIERARCHY]) {
	    	  this.query.processSearchCriteria.procSearchHierarchySelected = params[this.HIERARCHY];
	    	  this.filterProcessDefinitionList();
	      }

	      if ('' != params[this.CASE_OWNER]) {
//	    	  this.query.processSearchCriteria.processSrchCaseOwner = params[this.CASE_OWNER];
	    	  this.processSrchCaseOwner = params[this.CASE_OWNER];
	      }

	      var self = this;

	      if (params[this.PROCESS_FILTERS] && params[this.PROCESS_FILTERS][0] && params[this.PROCESS_FILTERS][0] === this.AUXILIARY_PROCESSES) {
	    	  this.toggleAuxiliaryProcess().then(function() {
	    		  self.prePopulateSelectedProcess(params[self.PROCESSES]).then(function() {
	    	    	  self.query.processSearchCriteria.procSrchProcessSelected = params[self.PROCESSES];

	    	    	  //For Descriptors
	    		      self.prePopulateDescriptors(params[self.DESCRIPTORS]);
	    		      deferred.resolve();
	    	      });
	    	  });
	      } else {
	    	  self.prePopulateSelectedProcess(params[self.PROCESSES]).then(function() {
    	    	  self.query.processSearchCriteria.procSrchProcessSelected = params[self.PROCESSES];

    	    	  //For Descriptors
    		      self.prePopulateDescriptors(params[self.DESCRIPTORS]);
    		      deferred.resolve();
    	      });
	      }

	      return deferred.promise;
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.prePopulateSelectedProcess = function(selectedProcesses) {
		var deferred = _q.defer();

		if (selectedProcesses.length == 1 && selectedProcesses[0] == this.defaultProcess.value) {
			this.query.processSearchCriteria.procSrchProcessSelected.push(this.defaultProcess);
		} else {
			for (var int = 0; int < selectedProcesses.length; int++) {
				var selProcess = selectedProcesses[int];
				for ( var procDef in this.procSrchProcess) {
					if (this.procSrchProcess[procDef].id
							&& this.procSrchProcess[procDef].id == selProcess) {
						this.query.processSearchCriteria.procSrchProcessSelected.push(this.procSrchProcess[procDef]);
						break;
					}
				}
			}
		}

		this.procSrchProcessSelected = this.query.processSearchCriteria.procSrchProcessSelected;
		this.processChange().then(function() {
			deferred.resolve();
		});
		return deferred.promise;
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.buildActivitySearchCriteria = function(criteria) {

	      if (this.query.processSearchCriteria.actStartFrom) {
	         criteria[this.STARTED_FROM] = this.processDate(this.query.processSearchCriteria.actStartFrom);
	      }

	      if (this.query.processSearchCriteria.actStartTo) {
	    	  criteria[this.STARTED_TO] = this.processDate(this.query.processSearchCriteria.actStartTo);
	      }

	      if (this.query.processSearchCriteria.actModifyFrom) {
	    	  criteria[this.MODIFY_TIME_FROM] = this.processDate(this.query.processSearchCriteria.actModifyFrom);
	      }

	      if (this.query.processSearchCriteria.actModifyTo) {
	    	  criteria[this.MODIFY_TIME_TO] = this.processDate(this.query.processSearchCriteria.actModifyTo);
	      }

	      criteria[this.STATE] = findValueById(this.activitySrchState, this.query.processSearchCriteria.activitySrchStateSelected);

	      criteria[this.PRIORITY] = findValueById(this.priorities, this.query.processSearchCriteria.processSrchPrioritySelected);

	      criteria[this.CRITICALITY] = this.query.processSearchCriteria.activitySrchCriticalitySelected;

	      if ('' != this.query.processSearchCriteria.activitySrchActivityOID) {
	         criteria[this.OID] = this.query.processSearchCriteria.activitySrchActivityOID;
	      }

	      var actPerformer = this.processActPerformer();
	      if ('' != actPerformer) {
	    	  criteria[this.PERFORMER] = actPerformer;
	      }

	      if (this.query.processSearchCriteria.showAuxiliaryProcess) {
	    	  criteria[this.PROCESS_FILTERS] = [this.AUXILIARY_PROCESSES];
	      }

	      criteria[this.PROCESSES] = this.processSelectedProcesses(this.query.processSearchCriteria.procSrchProcessSelected);

	      criteria[this.ACTIVITY_FILTERS] = this.getActivityFilters();

	      criteria[this.ACTIVITIES] = this.processSelectedActivities(this.query.processSearchCriteria.activitySrchSelected);

	      var descObject = this.processDescriptors();
	      if (Object.keys(descObject).length !== 0) {
	    	  criteria[this.DESCRIPTORS] = descObject;
	      }
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.processActPerformer = function() {
		if (this.activitySrchPerformer != undefined
				&& this.activitySrchPerformer.length == 1) {
			this.query.processSearchCriteria.activitySrchPerformer = this.activitySrchPerformer[0].id;
		} else {
			this.query.processSearchCriteria.activitySrchPerformer = "";
		}
		return this.query.processSearchCriteria.activitySrchPerformer;
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.getActivityFilters = function() {
		var activityFilters = [];
		if (this.query.processSearchCriteria.showInteractiveActivities) {
			activityFilters.push(this.INTERACTIVE_ACTIVITIES);
		}
		if (this.query.processSearchCriteria.showNonInteractiveActivities) {
			activityFilters.push(this.NONINTERACT_ACTIVITIES);
		}
		if (this.query.processSearchCriteria.showAuxiliaryActivities) {
			activityFilters.push(this.AUXILIARY_ACTIVITIES);
		}
		return activityFilters;
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.processSelectedActivities = function() {
		var processedSelectedActivities = [];

		var selectedActivities = this.activitySrchSelected;
		for (var index = 0; index < selectedActivities.length; index++) {
			if (selectedActivities[index].value == this.defaultActivity.value) {
				processedSelectedActivities = [this.defaultActivity.value];
				break;
			}
			var selActivityQid = selectedActivities[index].qualifiedId;
			var selProcess = this.activitiesQIDProcessMap[selActivityQid];
			processedSelectedActivities.push(filterModelIdFromQID(selProcess.id) + selActivityQid);
		}
		return processedSelectedActivities;
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.prePopulateActivityCriteria = function(params) {
		var deferred = _q.defer();
		if (params[this.STARTED_FROM]) {
			this.query.processSearchCriteria.actStartFrom = this.prePopulateDate(params[this.STARTED_FROM]);
	      }

	      if (params[this.STARTED_TO]) {
	    	  this.query.processSearchCriteria.actStartTo = this.prePopulateDate(params[this.STARTED_TO]);
	      }

	      if (params[this.END_TIME_FROM]) {
	    	  this.query.processSearchCriteria.actModifyFrom = this.prePopulateDate(params[this.END_TIME_FROM]);
	      }

	      if (params[this.END_TIME_TO]) {
	    	  this.query.processSearchCriteria.actModifyTo = this.prePopulateDate(params[this.END_TIME_TO]);
	      }

	      this.query.processSearchCriteria.processSrchPrioritySelected = findIdByValue(this.priorities, params[this.PRIORITY]);

	      if (params[this.OID]) {
	    	  this.query.processSearchCriteria.activitySrchActivityOID = params[this.OID];
	      }

	      //TODO
	      if (params[this.PERFORMER]) {
//	    	  this.query.processSearchCriteria.activitySrchPerformer = params[this.PERFORMER];
			  this.activitySrchPerformer = [{ "id" : params[this.PERFORMER] }];
	      }


	      var self = this;
	      if (params[this.PROCESS_FILTERS] && params[this.PROCESS_FILTERS][0] && params[this.PROCESS_FILTERS][0] === this.AUXILIARY_PROCESSES) {
	    	  this.toggleAuxiliaryProcess().then(function() {
	    		  self.prePopulateSelectedProcess(params[self.PROCESSES]).then(function() {
	    	    	  self.query.processSearchCriteria.procSrchProcessSelected = params[self.PROCESSES];

	    	    	  this.preSelectFilters(params);

	    	    	  //For Descriptors
	    		      self.prePopulateDescriptors(params[self.DESCRIPTORS]);


	    		      self.prePopulateSelectedActivities(params[self.ACTIVITIES]).then(function() {
	    			      //Set Activity State and Criticality after processChange as it resets it.
	    			      self.query.processSearchCriteria.activitySrchStateSelected = findIdByValue(self.activitySrchState, params[self.STATE]);
	    		    	  self.query.processSearchCriteria.activitySrchCriticalitySelected = params[self.CRITICALITY];
	    		    	  deferred.resolve();
	    		      });

	    		      deferred.resolve();
	    	      });
	    	  });
	      } else {
	    	  self.prePopulateSelectedProcess(params[self.PROCESSES]).then(function() {
    	    	  self.query.processSearchCriteria.procSrchProcessSelected = params[self.PROCESSES];

    	    	  self.preSelectFilters(params);

    	    	  //For Descriptors
    		      self.prePopulateDescriptors(params[self.DESCRIPTORS]);

    		      self.prePopulateSelectedActivities(params[self.ACTIVITIES]).then(function() {
    			      //Set Activity State and Criticality after processChange as it resets it.
    			      self.query.processSearchCriteria.activitySrchStateSelected = findIdByValue(self.activitySrchState, params[self.STATE]);
    		    	  self.query.processSearchCriteria.activitySrchCriticalitySelected = params[self.CRITICALITY];
    		    	  deferred.resolve();
    		      });

    		      deferred.resolve();
    	      });
	      }

	      return deferred.promise;
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.preSelectFilters = function(params) {
		var isInteractiveActivitiesFilterSet = false;

		var activityFilters = params[this.ACTIVITY_FILTERS];
		if (activityFilters != null) {
			for (var int = 0; int < activityFilters.length; int++) {
				if (params[this.ACTIVITY_FILTERS][int] == this.INTERACTIVE_ACTIVITIES) {
					isInteractiveActivitiesFilterSet = true;
					break;
				}
			}
		}

		if (isInteractiveActivitiesFilterSet) {
			this.query.processSearchCriteria.showInteractiveActivities = false;
			this.toggleInteractiveActivities();
		} else {
			this.toggleInteractiveActivities();
		}

		if (activityFilters != null) {
			for (var int = 0; int < activityFilters.length; int++) {
				if (params[this.ACTIVITY_FILTERS][int] == this.NONINTERACT_ACTIVITIES) {
					this.toggleNonInteractiveActivities();
				} else if (params[this.ACTIVITY_FILTERS][int] == this.AUXILIARY_ACTIVITIES) {
					this.toggleAuxiliaryActivities();
				}
			}
		}
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.prePopulateSelectedActivities = function(selectedActivities) {
		var deferred = _q.defer();

		this.activitySrchSelected = [];
		var tempSelectedActivities = [];
		for (var index = 0; index < selectedActivities.length; index++) {
			if (selectedActivities[index] == this.defaultActivity.value) {
				tempSelectedActivities = [this.defaultActivity];
				break;
			}
			var selActivity = selectedActivities[index];
			var selActivityQID = unmarshalActivityID(selActivity);
			tempSelectedActivities.push(this.activitiesQIDMap[selActivityQID]);
		}
		var self = this;
		this.processChange().then(function() {
			//processChange() would reset the selectedActivities to ALL
			self.activitySrchSelected = tempSelectedActivities;
			deferred.resolve();
		});

		return deferred.promise;
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.createProcessesActivitiesMap = function() {
		for ( var procDefIndex in this.procSrchProcess) {
			var procDef = this.procSrchProcess[procDefIndex];
			if (procDef.id) {
				//For Activities
				var allActivities = procDef.activities;
				for (var int = 0; int < allActivities.length; int++) {
					if (allActivities[int].id) {
						//Stores ActivityQID and ProcDefinition
						this.activitiesQIDProcessMap[allActivities[int].qualifiedId] = procDef;
						//Stores ActivityQID and Activity
						this.activitiesQIDMap[allActivities[int].qualifiedId] = allActivities[int];
					}
				}
			}
		}
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.prePopulateDescriptors = function(paramDescripters) {
		this.selected = {};
			for ( var item in paramDescripters) {
				for ( var index in this.descritorCols) {
					if (this.descritorCols[index].id == item) {
						if (this.descritorCols[index].dataType == "STRING") {
							this.selected[item] = paramDescripters[item];
						} else if (this.descritorCols[index].dataType == "BOOLEAN") {
							this.selected[item] = (paramDescripters[item] == true)? "0" : "1";
						} else if (this.descritorCols[index].dataType == "NUMBER") {
							this.selected[item] = Number(paramDescripters[item]);
						} else if (this.descritorCols[index].dataType == "DATE") {
							//TODO handle Date
						}
						break;
					}
				}
			}
			return this.selected;
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.processDate = function(dateInMilleseconds) {
		var date = new Date(dateInMilleseconds);
		var TimeInfo = getDateTimeObj(date, true, false);
		return jQuery.datepicker.formatDate(this.dateFormat, date) + " " + TimeInfo.hours + ":" + TimeInfo.mins + " " + TimeInfo.meridian;
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.prePopulateDate = function(dateStr) {
		var datePartSeperator = dateStr.indexOf(" ");
		var datePart = dateStr.substr(0, datePartSeperator);
		var date = jQuery.datepicker.parseDate(this.dateFormat, datePart).getTime();
		var dateObj = {};
		dateObj["date"] = date;
		dateObj["hours"] = dateStr.substr(datePartSeperator + 1, 2);
		dateObj["mins"] = dateStr.substr(datePartSeperator + 4, 3);
		dateObj["meridian"] = dateStr.substr(datePartSeperator + 7, 3);
		return getDate(dateObj, false);
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.calculateSelectedProcessActivities = function() {
		this.procSrchActivities = [];
		for (var i = 0; i < this.procSrchProcessSelected.length; i++) {
			if (this.procSrchProcessSelected[i].value
					&& this.procSrchProcessSelected[i].value == "All") {
				var allActivites = addAllActivites(this.procSrchProcess);
				this.procSrchActivities = this.procSrchActivities
						.concat(allActivites);

				break;
			} else {
				this.procSrchActivities = this.procSrchActivities
						.concat(this.procSrchProcessSelected[i].activities);
			}
		}
		return this.procSrchActivities;
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.postToggleActivities = function() {
		this.filterProcessDefinitionList();
		this.calculateSelectedProcessActivities();
		this.applyActivityFilters();
		this.procSrchActivities.splice(0, 0, this.defaultActivity);
		this.activitySrchSelected = [ this.procSrchActivities[0] ];
	};

	/*
	 *
	 */
	ProcessSearchViewCtrl.prototype.processDefaults = function(params) {
		//Activity Search Defaults
		if (params[this.SEARCH_OPT] == null) {
			params[this.SEARCH_OPT] = this.SEARCH_OPT_PROCESS;

		}
		if (params[this.STATE] == null && params[this.SEARCH_OPT] === this.SEARCH_OPT_PROCESS) {
			params[this.STATE] = this.procSrchState[0].value;
		}
		if (params[this.PRIORITY] == null) {
			params[this.PRIORITY] = this.priorities[0].value;
		}
		if (params[this.HIERARCHY] == null) {
			params[this.HIERARCHY] = this.HIERARCHY_PROCESS;
		}
		if (params[this.PROCESSES] == null) {
			params[this.PROCESSES] = [ this.procSrchProcess[0].value ];
		}

		//Activity Search Defaults
		if (params[this.CRITICALITY] == null) {
			params[this.CRITICALITY] = this.activitySrchCriticality[0].name;
		}
		if (this.SEARCH_OPT_ACTIVITY === params[this.SEARCH_OPT]) {
			if (params[this.STATE] == null) {
				params[this.STATE] = this.activitySrchState[0].name;
			}
		}
		if (params[this.ACTIVITIES] == null) {
			params[this.ACTIVITIES] = [ this.defaultActivity.value ];
		}

	};

	/**
	 *
	 */
	ProcessSearchViewCtrl.prototype.setShowSearchCriteria = function() {
		this.showSearchCriteria = !this.showSearchCriteria;
	};

	/**
	 *
	 */
	ProcessSearchViewCtrl.prototype.validateData = function() {
		this.removeFormErrors();

		if (!(this.processSearchForm.$valid)) {
			return false;
		}

		if (this.query.processSearchCriteria.filterObject == 0) {
			if (!this.checkDateRangeValidity(this.query.processSearchCriteria.procStartFrom, this.query.processSearchCriteria.procStartTo)) {
				this.processSearchForm.$error.procStartTimeRange = true;
				return false;
			}
			if (!this.checkDateRangeValidity(this.query.processSearchCriteria.procEndFrom, this.query.processSearchCriteria.procEndTo)) {
				this.processSearchForm.$error.procEndTimeRange = true;
				return false;
			}
			if (this.query.processSearchCriteria.processSrchRootProcessOID && isNaN(this.query.processSearchCriteria.processSrchRootProcessOID)) {
				this.processSearchForm.$error.invalidRootProcessOID = true;
				return false;
			}
			if (this.query.processSearchCriteria.processSrchProcessOID && isNaN(this.query.processSearchCriteria.processSrchProcessOID)) {
				this.processSearchForm.$error.invalidProcessOID = true;
				return false;
			}
		} else {
			if (!this.checkDateRangeValidity(this.query.processSearchCriteria.actStartFrom, this.query.processSearchCriteria.actStartTo)) {
				this.processSearchForm.$error.actStartTimeRange = true;
				return false;
			}
			if (!this.checkDateRangeValidity(this.query.processSearchCriteria.actModifyFrom, this.query.processSearchCriteria.actModifyTo)) {
				this.processSearchForm.$error.actModifyTimeRange = true;
				return false;
			}
			if (this.query.processSearchCriteria.activitySrchActivityOID && isNaN(this.query.processSearchCriteria.activitySrchActivityOID)) {
				this.processSearchForm.$error.invalidActivityOID = true;
				return false;
			}
		}

		if(!this.descriptorsValid()) {
			return false;
		}

		return true;
	};

/**
 *
 */
ProcessSearchViewCtrl.prototype.checkDateRangeValidity = function(from, to) {
	return _sdUtilService.validateDateRange(from, to);
}


/**
	*
	 */
	ProcessSearchViewCtrl.prototype.descriptorsValid = function() {
				//Descriptor date
			if (this.selected) {
			    for (var item in this.selected) {
			        for (var index in this.descritorCols) {
			            if (this.descritorCols[index].id == item) {
			                if (this.descritorCols[index].dataType == "DATE") {
			                    if (this.selected[this.descritorCols[index].id].from && this.selected[this.descritorCols[index].id].to) {
			                        if (this.selected[this.descritorCols[index].id].from > this.selected[this.descritorCols[index].id].to) {
			                           	trace.debug("To Date is less than from Date. Date Validation failed for descriptor - ",this.descritorCols[index].id);
			                            return false;
			                        }
			                    }
			                }

			            }
			        }
			    }
			}

			return true;
	}


	/**
	 *
	 */
	ProcessSearchViewCtrl.prototype.removeFormErrors = function() {
		_sdUtilService.removeFormErrors(this.processSearchForm, [
				'procStartTimeRange', 'procEndTimeRange', 'actStartTimeRange',
				'actModifyTimeRange', 'invalidRootProcessOID',
				'invalidProcessOID', 'invalidActivityOID' ]);
	};

	/*
	 *
	 */
	function getSelectedProcDefIds(procSrchProcessSelected, getArray) {
		var selectedProcDefs = procSrchProcessSelected;
		var selectedProcDefIds = '';
		var selectedProcDefIdsArr = [];
		for ( var procDef in selectedProcDefs) {
			if (selectedProcDefs[procDef].id) {
				selectedProcDefIds += "," + selectedProcDefs[procDef].id;
				selectedProcDefIdsArr.push(selectedProcDefs[procDef].id);
			}
		}
		selectedProcDefIds = selectedProcDefIds.substr(1);
		return (getArray) ? selectedProcDefIdsArr : selectedProcDefIds
	}

	/*
	 *
	 */
	function addAllActivites(allProcesses) {
		var allActivities = [];
		for (var i = 0; i < allProcesses.length; i++) {
			if (allProcesses[i].value && allProcesses[i].value == "All") {
			} else {
				allActivities = allActivities
						.concat(allProcesses[i].activities);
			}
		}
		return allActivities;
	}

	/*
	 *
	 */
	function setOrder(processes) {
		angular.forEach(processes, function(proc) {
			proc['order'] = 1;
			var activities = proc.activities;
			angular.forEach(activities, function(activity) {
				activity['order'] = 1;
			});
		});
	}

	/*
	 *
	 */
	function findValueById(procSrchStates, processStateId) {
		for (var i = 0; i < procSrchStates.length; i++) {
			if (isNaN(processStateId) || isNaN(procSrchStates[i].value)) {
				if(procSrchStates[i].value == processStateId) {
					return procSrchStates[i].name;;
				}
			} else if (procSrchStates[i].value.toUpperCase() == processStateId.toUpperCase()) {
				return procSrchStates[i].name;
			}
		}
	}

	/*
	 *
	 */
	function findIdByValue(procSrchStates, processStateValue) {
		for (var i = 0; i < procSrchStates.length; i++) {
			if (isNaN(processStateValue) || isNaN(procSrchStates[i].name)) {
				if (procSrchStates[i].name == processStateValue) {
					return procSrchStates[i].value;
				}
			}
			if (procSrchStates[i].name.toUpperCase() == processStateValue.toUpperCase()) {
				return procSrchStates[i].value;
			}
		}
	}

	/*
	 *
	 */
	function filterModelIdFromQID(entityQID) {
		// Logic to handle special characters like '{' in QID
		// e.g. {ReportingModel}AbortProcess, So Getting the last word i.e. AbortProcess
		var lastIndex = entityQID.lastIndexOf("}");
		if (lastIndex != -1) {
			return entityQID.substr(lastIndex + 1, entityQID.length);
		}
	}

	/*
	 *
	 */
	function unmarshalActivityID(activityId) {
		//
		var index = activityId.indexOf("{");
		if (index != -1) {
			var activityQID = activityId.substr(index, activityId.length);
			return activityQID;
		}
	}



	//Date Time Helper Funciotns
	var OPTIONS = {
			clock24 : {
				hours : getArrayWithNumber(0, 23)
			},
			clock12 : {
				hours : getArrayWithNumber(1, 12)
			},
			minutes : getArrayWithNumber(0, 59),
			AM : '',
			PM : ''
		}

  /**
   *
   */
	function parseString(number){
		if(number < 10) {
			return "0"+number;
		}
		return ""+number;
	}

	/**
	 *
	 */
	function getDateTimeObj(date, isDateSelected, is24HourClock) {

		var dateTime = {
			mins : parseString(date.getMinutes())
		}

		if (is24HourClock) {
			dateTime.hours = date.getHours()
		} else {
			var postHour = date.getHours();
			if (postHour == 12) { // At 00 hours we need to show 12 AM
				dateTime.meridian = OPTIONS.PM;
				dateTime.hours = postHour;
			} else if (postHour > 12) {
				dateTime.meridian = OPTIONS.PM;
				dateTime.hours = postHour - 12;
			} else if (postHour == 0) {
				dateTime.hours = 12;
				dateTime.meridian = OPTIONS.AM;
			} else {
				dateTime.hours = postHour;
				dateTime.meridian = OPTIONS.AM;
			}
		}
		dateTime.hours = parseString(dateTime.hours);

		if (isDateSelected) {
			dateTime.date = date.getTime();
		} else {
			dateTime.date = '';
		}
		return dateTime;
	}

	/**
	 *
	 */
	function getArrayWithNumber(from, to) {
		var array = [];
		for (var i = from; i <= to; i++) {
			array.push(parseString(i));
		}
		return array;
	}

	/**
	 *
	 */
	function getDate(input, is24HourClock) {
		if (!input) {
			return undefined;
		}

		var mins = parseInt(input.mins);
		var hours = parseInt(input.hours);

		var date = new Date(input.date);
		date.setMinutes(mins);
		if (!is24HourClock) {
			if (input.meridian == OPTIONS.PM && parseInt(input.hours) < 12) {
				date.setHours(hours + 12);
			} else if (input.meridian == OPTIONS.AM && hours == 12) {
				date.setHours(hours);
			} else {
				date.setHours(hours);
			}
		} else {
			date.setHours(hours);
		}
		return date.getTime();
	}


})();
