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
		_sdViewUtilService = sdViewUtilService;
		_sdProcessSearchService = sdProcessSearchService;
		_q = $q;
		_filter = $filter;
		trace = sdLoggerService.getLogger('bpm-processportal.ProcessSearchViewCtrl');

		this.initialize();
		
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
	ProcessSearchViewCtrl.prototype.initialize = function() {

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

		this.HIERARCHY_PROCESS = "PROCESS";
		this.HIERARCHY_PROCESS_AND_CASE = "PROCESS_AND_CASE";
		this.HIERARCHY_CASE = "CASE";
		this.HIERARCHY_ROOT_PROCESS = "ROOT_PROCESS";
		this.CASE_PROCESS_ID = "{PredefinedModel}CaseProcess";
		this.caseProcDef = {};
		this.descBoolOptions = {};
		this.selected = {};

		var self = this;

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
						});

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
						});

		_sdProcessSearchService
				.getProcessStates()
				.then(
						function(states) {
							self.procSrchState = states;
							self.query.processSearchCriteria.procSrchStateSelected = self.procSrchState[0].value;
						});

		_sdProcessSearchService
				.getProcessHierarchy()
				.then(
						function(hierarchyTypes) {
							self.procSrchHierarchy = hierarchyTypes;
							self.query.processSearchCriteria.procSearchHierarchySelected = self.procSrchHierarchy[0].value;
						});

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
						});

		_sdProcessSearchService
				.searchAttributes()
				.then(
						function(searchFor) {
							self.searchOptions = searchFor;
							self.query.processSearchCriteria.filterObject = self.searchOptions[0].value;
						}, function(error) {
							trace.log(error);
						});

		_sdProcessSearchService
				.getAllActivityStates()
				.then(
						function(states) {
							self.activitySrchState = [ {
								value : "3",
								label : _sdProcessSearchService
										.getI18MessageString('business-control-center-messages.views-processSearchView-chooseProcess-options-alive-label'),
								name : "alive",
							} ];

							self.activitySrchState = self.activitySrchState
									.concat(states);

							var default_state = [ {
								value : "10",
								label : _sdProcessSearchService
										.getI18MessageString('business-control-center-messages.views-processSearchView-chooseProcess-options-all-label'),
								name : "all",
							} ];
							self.activitySrchState = self.activitySrchState
									.concat(default_state);
							self.query.processSearchCriteria.activitySrchStateSelected = self.activitySrchState[0].value;
						});

		_sdProcessSearchService.getDescBoolOptions().then(
				function(descBoolOptions) {
					self.descBoolOptions = descBoolOptions;
				});
		this.showProcSearchResult = false;

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
		if (self.processSrchCaseOwner != undefined
				&& self.processSrchCaseOwner.length == 1) {
			self.query.processSearchCriteria.processSrchCaseOwner = self.processSrchCaseOwner[0].id;
		} else {
			self.query.processSearchCriteria.processSrchCaseOwner = "";
		}

		if (self.activitySrchPerformer != undefined
				&& self.activitySrchPerformer.length == 1) {
			self.query.processSearchCriteria.activitySrchPerformer = self.activitySrchPerformer[0].id;
		} else {
			self.query.processSearchCriteria.activitySrchPerformer = "";
		}

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
		this.descritorCols = [];
		if (this.query.processSearchCriteria.filterObject == 1) {
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
																sortable : descriptor.sortable,
																filterable : descriptor.filterable
															});
												});
							});
		}
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
		this.processSearchData();
		this.showSearchCriteria = false;
		this.showProcSearchResult = true;
		this.refresh();
	};

	/*
	 * 
	 */
	ProcessSearchViewCtrl.prototype.processSearchData = function() {

		var selectedProcDefIdsArr = getSelectedProcDefIds(
				this.procSrchProcessSelected, true);

		if (selectedProcDefIdsArr.length == 0) {
			this.query.processSearchCriteria.procSrchProcessSelected = [ this.procSrchProcess[0].value ];
		} else {
			this.query.processSearchCriteria.procSrchProcessSelected = selectedProcDefIdsArr;
		}

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
							obj['textSearch'] = this.selected[this.descritorCols[index].id];
						} else if (this.descritorCols[index].dataType == "BOOLEAN") {
							obj['equals'] = (this.selected[this.descritorCols[index].id] == 0)? 'true' : 'false';
						} else if (this.descritorCols[index].dataType == "NUMBER" || this.descritorCols[index].dataType == "DATE") {
							obj['from'] = this.selected[this.descritorCols[index].id];
							obj['to'] = this.selected[this.descritorCols[index].title];
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
		if (this.query.processSearchCriteria.showAuxiliaryProcess) {
			this.query.processSearchCriteria.showAuxiliaryProcess = false;
			this.procSrchProcess = this.allBusinessProcesses;
		} else {
			this.query.processSearchCriteria.showAuxiliaryProcess = true;
			var self = this;
			if (self.procSrchAuxProcess.length == 0) {
				this.getAllUniqueProcesses(false).then(function(processes) {
					self.procSrchProcess = processes;
				});
			} else {
				self.procSrchProcess = self.procSrchAuxProcess;
			}
		}
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
		this.filterProcessDefinitionList();
		this.processChange();
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
		this.filterProcessDefinitionList();
		this.processChange();
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
		this.filterProcessDefinitionList();
		this.processChange();
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
									});
							self.procSrchAuxProcess = self.procSrchAuxProcess
									.concat(processes);
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

})();