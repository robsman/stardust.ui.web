/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * @author Subodh.Godbole
 * @author johnson.quadras
 */

(function(){
	'use strict';

	angular.module('bpm-common').directive('sdActivityTable',
			['$parse', '$q', 'sdUtilService', 'sdViewUtilService', 'sdLoggerService', 'sdPreferenceService', 'sdWorklistService',
			 'sdActivityInstanceService', 'sdProcessDefinitionService', 'sdCriticalityService', 'sdStatusService', 'sdPriorityService', 
			 '$filter','sgI18nService','$timeout','sdLoggedInUserService','sdDialogService', ActivityTableDirective]);

	/*
	 *
	 */
	function ActivityTableDirective($parse, $q, sdUtilService, sdViewUtilService, sdLoggerService, sdPreferenceService, sdWorklistService,
			sdActivityInstanceService, sdProcessDefinitionService, sdCriticalityService, sdStatusService, sdPriorityService, $filter, sgI18nService, $timeout, sdLoggedInUserService, sdDialogService ) {

		var trace = sdLoggerService.getLogger('bpm-common.sdActivityTable');

		var directiveDefObject = {
				restrict : 'AE',
				require : '^?sdData',
				scope: true, // Creates a new sub scope
				templateUrl: 'plugins/html5-process-portal/scripts/directives/partials/worklist.html',
				compile: function(elem, attr, transclude) {
					processRawMarkup(elem, attr);

					return {
						post: function(scope, element, attr, ctrl) {
							var activityTableCompiler = new ActivityTableCompiler(scope, element, attr, ctrl);
						}
					};
				}
		};

		//Defaults
		var DEFAULT_VALUES = {
				MODE:'worklist',
				WORKLIST: {
					NAME : 'worklist',
					VISIBLE_COLUMNS : ['activityName', 'activityOID', 'criticality', 'priority', 'descriptors', 'startTime', 'lastModified', 'duration', 'lastPerformer', 'data'],
					PREFERENCE_MODULE : 'ipp-workflow-perspective',
					SHOW_TRIVIAL_DATA_COLUMNS : true,
					COLUMN_NAME_MAP  : {
						"Overview" : "ActivityName",
						"OID" : "ActivityOID",
						"Started" : "StartTime",
						"ProcessDefinition" : "ProcessName"
					}
				},
				ACITIVITY_INSTANCE_VIEW : {
					NAME:'activityTable',
					VISIBLE_COLUMNS : ['activityName', 'activityOID', 'assignedTo', 'priority', 'criticality', 'descriptors', 'startTime', 'LastModified', 'duration', 'assignedTo'],
					SHOW_TRIVIAL_DATA_COLUMNS : false,
					COLUMN_NAME_MAP : {
						"EndTime" : "LastModified",
						"ProcessId" : "ProcessName"
					}
				}
		};

		/*
		 *
		 */
		function processRawMarkup(elem, attr) {
			// Process Trivial Data Column
			var showTrivialDataColumn =  DEFAULT_VALUES.WORKLIST.SHOW_TRIVIAL_DATA_COLUMNS;

			if( attr.sdaMode === DEFAULT_VALUES.ACITIVITY_INSTANCE_VIEW.NAME ){
				showTrivialDataColumn = DEFAULT_VALUES.ACITIVITY_INSTANCE_VIEW.SHOW_TRIVIAL_DATA_COLUMNS;
			}

			if (attr.sdaTrivialDataColumn && attr.sdaTrivialDataColumn === 'false') {
				showTrivialDataColumn = false;
			}
			// If not required remove the column
			if (!showTrivialDataColumn) {
				var cols = elem.find('[sda-column="TRIVIAL_DATA"]');
				cols.remove();
			}
			// Toolbar
			var toolbar = elem.prev();
			var items = toolbar.find('[sda-column="TRIVIAL_DATA"]');
			items.remove();

			// Process Descriptor columns
			var showDescriptorCoulmns = true; // Default
			if (attr.sdaDescriptorColumns && attr.sdaDescriptorColumns === 'false') {
				showDescriptorCoulmns = false;
			}
			// If not required remove the column
			if (!showDescriptorCoulmns) {
				var cols = elem.find('[sda-column="DESCRIPTOR_COLUMNS"]');
				cols.remove();
			}
		}

		/*
		 *
		 */
		function ActivityTableCompiler(scope, element, attr, ctrl) {
			try{
				this.initialize(attr, scope, $filter);
				this.showError = false;
			}catch (e) {
				this.showError(e);
			}

			/*
			 * Defined here as access required to scope
			 */
			if(angular.isDefined(ctrl)){
				this.sdDataCtrl = ctrl;
			}

			/*
			 * This needs to be defined here as it requires access to scope
			 */
			ActivityTableCompiler.prototype.safeApply = function() {
				sdUtilService.safeApply(scope);
			};

			// Expose controller as a whole on to scope
			scope.activityTableCtrl = this;
			sdUtilService.addFunctionProxies(scope.activityTableCtrl);
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.initialize = function(attr, scope, $filter) {
			var scopeToUse = scope.$parent;
			var self = this;

			// Define data
			this.activities = {};
			this.dataTable = null; // Handle to data table instance, to be set later

			//Abort Activity Data
			this.showAbortActivityDialog = false;
			this.activitiesToAbort = [];
			this.dirtyDataForms =[];

			//All processes with activities
			this.allAccessibleProcesses = [];
			this.allAvailableCriticalities = [];
			this.availableStates = [];
			this.availablePriorities = [];
			this.preferenceModule = "";
			this.preferenceId ="";
			this.columnSelector = 'admin';

			// Process Query
			if (!attr.sdaQuery && !attr.sdData) {
				throw 'Query attribute has to be specified if sdData is not specified.';
			}

			if(!attr.sdData) {
				var queryGetter = $parse(attr.sdaQuery);
				var query = queryGetter(scopeToUse);
				if (query == undefined ) {
					throw 'Query evaluated to "nothing" for activity table.';
				}
				this.query = query;
			}

			//Mode Selector
			if(attr.sdaMode){
				this.mode= attr.sdaMode;
			}else{
				this.mode = DEFAULT_VALUES.MODE;  
			}

			if (this.mode === DEFAULT_VALUES.WORKLIST.NAME) {
				this.initializeWorklistMode(attr, scope);
			} else if(this.mode === DEFAULT_VALUES.ACITIVITY_INSTANCE_VIEW.NAME){
				this.initializeActivityInstanceMode(attr, scope);
			}else{
				throw 'Not a valid value for sdaMode.Valid modes are : '+ DEFAULT_VALUES.WORKLIST.NAME +' & '+DEFAULT_VALUES.ACITIVITY_INSTANCE_VIEW.NAME;
			}

			this.customizeWithAttributeValues(attr, scope, scopeToUse);

			if (attr.sdaPageSize) {
				this.sdaPageSize = attr.sdaPageSize;
			}

			if (attr.sdaInitialSelection) {
				this.initialSelection = attr.sdaInitialSelection;
			}

			if (attr.sdaSelection) {
				var assignable = $parse(attr.sdaSelection).assign;
				if (assignable) {
					this.selection = null;
					this.selectionExpr = 'activityTableCtrl.selection';

					// Update parent for change in sdDataTable
					scope.$watch(this.selectionExpr, function(newVal, oldVal) {
						if (newVal != undefined && newVal != null && newVal != oldVal) {
							assignable(scopeToUse, self.selection);
						}
					});

					// Update for sdDataTable for change in parent
					scopeToUse.$watch(attr.sdaSelection, function(newVal, oldVal) {
						if (newVal != undefined && newVal != null && newVal != self.selection) {
							self.selection = newVal;
						}
					});
				}
			}

			// Process TableHandle and then set data table instance
			this.tableHandleExpr = 'activityTableCtrl.dataTable';

			var unregister = scope.$watch(this.tableHandleExpr, function(newVal, oldVal) {
				if (newVal != undefined && newVal != null && newVal != oldVal) {
					if (attr.sdActivityTable) {
						var assignable = $parse(attr.sdActivityTable).assign;
						if (assignable) {
							assignable(scopeToUse, self.dataTable);
						}
					}
					unregister();
				}
			});
			
			if (attr.sdaReady) {
				trace.log( 'Table defines sda-ready attribute, so deferring initialization...');
				var unregisterReady = scopeToUse.$watch(attr.sdaReady, function(newVal, oldVal) {
					if(newVal === true) {
						trace.log('sda-ready flag is triggered...');
						// Initialize after current digest cycle
						$timeout(function(){self.ready = true;});
						unregisterReady();
					}
				});
			} 

			/**
			 * 
			 */
			this.preferenceDelegate = function(prefInfo) {
				var preferenceStore = sdPreferenceService.getStore(prefInfo.scope, self.preferenceModule, self.preferenceId);
				preferenceStore.super_getValue = preferenceStore.getValue;
				// Override
				preferenceStore.getValue = function(name, fromParent) {
					var value = this.super_getValue(name, fromParent);
					value = self.getColumnNamesByMode(value);
					return value;
				};
				// Override
				preferenceStore.marshalName = function(scope) {
					var name = self.preferenceName;
					if (scope == 'PARTITION') {
						name = 'Default';
					}
					return name;
				}
				return preferenceStore;
			};
			
			/**
			 * 
			 */
			ActivityTableCompiler.prototype.getColumnNamesByMode = function getColumnNamesByMode(value) {
				
				if(angular.isUndefined(value)){
					return value;
				}
				
				if(this.mode === DEFAULT_VALUES.WORKLIST.NAME){
					var prefValue = JSON.parse(value);
					prefValue.selectedColumns = replaceColumnNames(prefValue.selectedColumns, DEFAULT_VALUES.WORKLIST.COLUMN_NAME_MAP);
					value = JSON.stringify(prefValue);
					
				} else {
					try{
						var prefValue = JSON.parse(value);
						//Do nothing
					}catch (e){
						var prefColumns = value.split('$#$');
						value = replaceColumnNames(prefColumns, DEFAULT_VALUES.ACITIVITY_INSTANCE_VIEW.COLUMN_NAME_MAP).join('$#$');
					}
				}
				return value;
			};

			/**
			 * 
			 */
			ActivityTableCompiler.prototype.isColumnVisible = function(columnName) {
				var found = $filter('filter')(self.visbleColumns, columnName);
				if(found && found.length === 1){
					return true;
				}
				return false;
			};


			/**
			 * 
			 */
			ActivityTableCompiler.prototype.changeFormStatus = function(rowId) {
				var self = this;

				if (this.dirtyDataForms.indexOf(rowId) == -1) {
					this.dirtyDataForms.push(rowId);
				}

				//Auto select dirty rows
				var selectedRows = self.dataTable.getSelection();
				var matchArray   = $filter('filter')(selectedRows, { activityOID : rowId }, true);
				var isRowSelected = matchArray.length > 0;

				if(!isRowSelected) {
					var rows = $filter('filter')(self.activities.list, {
						activityOID : rowId
					}, true);

					if (rows && rows.length === 1) {
						selectedRows.push({activityOID:rows[0].activityOID});
					}

					self.dataTable.setSelection(selectedRows);
				}
			};

			this.fetchDescriptorCols(attr);
			this.fetchAllProcesses();
			this.fetchAllAvailableCriticalities();
			this.fetchAvailableStates();
			this.fetchAvailablePriorities();

			/*
			 * 
			 */
			self.openAbortPopover = function(event, rowItem) {
				var selectedItems = [];
				if (angular.isDefined(rowItem)) {
					selectedItems = [rowItem];
				}else {
					selectedItems = self.dataTable.getSelection();
				}
				
				var processesToAbort = [];
				angular.forEach(selectedItems, function( item ) {
					processesToAbort.push(item.processInstance);
				});
				self.processesToAbort = processesToAbort;
				
				self.popoverDirective.show(event);
			};

			/*
			 *
			 */
			self.joinCompleted = function(result) {
				self.refresh();
				sdViewUtilService.syncLaunchPanels();
				if (angular.isDefined(result)) {
					// TODO pass result as an argument to below view
					sdViewUtilService.openView('processDefinitionView', true);
				}
			};

/*
			 *
			 */
			self.openJoinDialog = function() {
				self.showJoinProcessDialog = true;
			};
			
			/*
			 *
			 */
			self.switchCompleted = function(result) {
				self.refresh();
				sdViewUtilService.syncLaunchPanels();
				if (angular.isDefined(result)) {
					// TODO pass result as an argument to below view
					sdViewUtilService.openView('worklistViewHtml5', true);
				}
			};
			
			/*
			 *
			 */
			self.openSwitchDialog = function() {
				self.showSwitchProcessDialog = true;
			};

			/**
			 * 
			 * @param rowItems
			 */
			self.openDefaultDelegationDialog = function(rowItems) {
				var self = this;
				
				var title = sgI18nService.translate('views-common-messages.common-confirm','Confirm');
				var html  = sgI18nService.translate('views-common-messages.views-strandedActivities-confirmDefaultDelegate','Confirm');
				var options = {
						title: title,
						type: 'confirm',
						onConfirm: function() {
							self.performDefaultDelegate(scope,sdActivityInstanceService, sdDialogService, sgI18nService, rowItems);
						},
						confirmActionLabel: sgI18nService.translate('views-common-messages.common-yes','Yes'),
						cancelActionLabel:sgI18nService.translate('views-common-messages.common-no','No')
					};
				
				
				sdDialogService.dialog(scope, options, html)
				
			};
		};

		/**
		 * 
		 */
		ActivityTableCompiler.prototype.initializeWorklistMode = function(attr, scope){
			this.priorityEditable = false;
			this.visbleColumns = DEFAULT_VALUES.WORKLIST.VISIBLE_COLUMNS;
			this.preferenceModule = DEFAULT_VALUES.WORKLIST.PREFERENCE_MODULE;
			this.exportFileName= "Worklist";
			this.initialSort = {name : 'startTime', dir : 'desc'};

		};

		/**
		 * 
		 */
		ActivityTableCompiler.prototype.initializeActivityInstanceMode = function(attr, scope){

			this.priorityEditable = true;
			this.originalPriorities = {};
			this.changedPriorities = {};
			this.defaultDelegateEnabled = false;
			this.initialSort = {name : 'activityOID', dir : 'desc'};
			this.updatePriorityNotification = {
					error : false,
					result : {}
			};
			this.visbleColumns = DEFAULT_VALUES.ACITIVITY_INSTANCE_VIEW.VISIBLE_COLUMNS;
			this.exportFileName= "Activity_Table";

			if (!attr.sdaPreferenceModule) {
				throw "sdaPreferenceModule is not defined."
			}
			if (!attr.sdaPreferenceId) {
				throw "sdaPreferenceId is not defined."
			}
			if (!attr.sdaPreferenceName) {
				throw "sdaPreferenceName is not defined."
			}
			if (attr.sdaDefaultDelegateEnabled) {
				this.defaultDelegateEnabled = attr.sdaDefaultDelegateEnabled === 'true' ? true : false;
			}
		};

		/**
		 * 
		 */
		ActivityTableCompiler.prototype.customizeWithAttributeValues = function(attr, scope, scopeToUse){
			// Process Title
			var titleExpr = "";
			if (attr.sdaTitle) {
				titleExpr = attr.sdaTitle;
			}
			var titleGetter = $parse(titleExpr);
			this.title = titleGetter(scopeToUse);

			if(this.query){
				
				var idFromQuery = this.query.userId || this.query.participantQId || this.query.processQId;
				
				if(this.query.processQId) {
					this.preferenceName = idFromQuery;
					this.preferenceId = 'worklist-process-columns';
				}else if(this.query.userId) {
					this.preferenceName = idFromQuery;
					this.preferenceId = 'worklist-participant-columns';
				}else if(this.query.participantQId) {
					this.preferenceName = "{ipp-participant}" + idFromQuery;
					this.preferenceId = 'worklist-participant-columns';
				}else{
					this.preferenceName = sdLoggedInUserService.getUserId();
					this.preferenceId = 'worklist-participant-columns';
				}

				this.exportFileName = this.exportFileName +"_"+idFromQuery; 
			}


			if (attr.sdaPreferenceModule) {
				this.preferenceModule = attr.sdaPreferenceModule;
			}

			if (attr.sdaPreferenceId) {
				this.preferenceId = attr.sdaPreferenceId;
			}

			if (attr.sdaPreferenceName) {
				this.preferenceName = attr.sdaPreferenceName;
			}

			if (attr.sdaExportName) {
				this.exportFileName = attr.sdaExportName;
			}

			if(attr.sdaIntialSort){
				var sortGetter = $parse(attr.sdaInitialSort);
				this.intialSort = sortGetter(scopeToUse);
			}
			
			if(attr.sdaVisibleColumns){
				var visibleColumnGetter = $parse(attr.sdaVisibleColumns);
				this.visbleColumns =visibleColumnGetter(scopeToUse);
			}
		};


		/*
		 * 
		 */
		ActivityTableCompiler.prototype.refresh = function() {
			this.dataTable.refresh(true);
		};

		/*
		 * 
		 */
		ActivityTableCompiler.prototype.cleanLocals = function() {
			this.dirtyDataForms = [];
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.fetchPage = function(options) {
			var self = this;
			var deferred = $q.defer();
			self.cleanLocals();

			var query = angular.extend({}, this.query);
			query.options = options;

			if( angular.isDefined(this.sdDataCtrl) ) {
				trace.debug("sdData is defined fetching custom data. ");

				var dataResult = self.sdDataCtrl.retrieveData(query);

				dataResult.then(function(data){
					self.activities = data;
					deferred.resolve( self.activities );
					self.safeApply( self.activities.list );
					self.storePriorities( self.activities.list );
				},function(error){
					deferred.reject(error);
				});

			} else {

				if(this.mode != 'worklist'){
					throw 'sdData is not defined for sdActivityTable';
				}
				trace.debug("sdData not defined fetching default data. ");

				sdWorklistService.getWorklist(query).then(function(data) {
					self.activities.list = data.list;
					self.activities.totalCount = data.totalCount;
					self.storePriorities(self.activities.list);

					var activityOIDs = [];
					angular.forEach(self.activities.list, function(workItem, index){
						if (workItem.trivial == undefined || workItem.trivial) {
							activityOIDs.push(workItem.activityOID);
						}
					});

					sdActivityInstanceService.getTrivialManualActivitiesDetails(activityOIDs).then(function(data) {
						self.activities.trivialManualActivities = data;
						deferred.resolve(self.activities);
						self.safeApply();
					}, function(error) {
						deferred.reject(error);
					});
				}, function(error) {
					deferred.reject(error);
				});

			}

			return deferred.promise;
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.onSelect = function(info) {
			// NOP
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.onPagination = function(info) {
			// NOP
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.onColumnReorder = function(info) {
			// NOP
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.onSorting = function(info) {
			// NOP
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.fetchDescriptorCols = function(attr) {
			var self = this;

			sdProcessDefinitionService.getDescriptorColumns().then(function(descriptors) {
				self.descritorCols = [];
				angular.forEach(descriptors, function(descriptor){
					self.descritorCols.push({
						id: descriptor.id,
						field: "descriptorValues['" + descriptor.title + "'].value",
						title: descriptor.title,
						dataType: descriptor.type,
						sortable: descriptor.sortable,
						filterable : descriptor.filterable,
						key : descriptor.title.replace(/ /g, '')
					});
				});

				if(attr.sdaReady){
					self.descriptorsReady = true;
				}else{
					self.ready = true;
				}
				
				self.safeApply();
			});
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.fetchAllProcesses = function() {
			var self = this;

			sdProcessDefinitionService.getAllProcesses(false).then(function(processes) {
				self.allAccessibleProcesses = processes;
			});
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.fetchAllAvailableCriticalities = function()
		{
			var self = this;
			sdCriticalityService.getAllCriticalities()
			.then(function(criticalities){ 
				self.allAvailableCriticalities = criticalities;
			});
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.fetchAvailableStates = function()
		{
			var self = this;
			sdStatusService.getAllActivityStates()
			.then(function(value){
				self.availableStates = value;
			});
		};


		/*
		 *
		 */
		ActivityTableCompiler.prototype.fetchAvailablePriorities = function()
		{
			var self = this;
			sdPriorityService.getAllPriorities().then(function(data){
				self.availablePriorities = data;
			});
		};


		/*
		 *
		 */
		ActivityTableCompiler.prototype.activateItem = function( rowItem ) {
			sdViewUtilService.openView("activityPanel", "OID=" + rowItem.activityOID, {"oid" : "" + rowItem.activityOID});
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.openNotes = function( rowItem ) {
			sdViewUtilService.openView("notesPanel", "oid=" + rowItem.processInstance.oid,
					{"oid": "" + rowItem.processInstance.oid}, true);
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.openProcessDetails = function( rowItem ) {
			var viewId = "";
			if(this.isWorklistMode()) {
				viewId = rowItem.defaultCaseActivity ? 'caseDetailsView' : 'processInstanceDetailsView';
			}else if(this.isActivityTableMode()) {
				viewId = rowItem.isCaseInstance ? 'caseDetailsView' : 'processInstanceDetailsView';
			}
			
			sdViewUtilService.openView( viewId,
					"processInstanceOID=" + rowItem.processInstance.oid,
					{
				"oid": "" + rowItem.activityOID,
				"processInstanceOID": "" + rowItem.processInstance.oid
					}, true
			);
		};


		/**
		 *
		 */
		ActivityTableCompiler.prototype.containsAllTrivialManualActivities = function() {
			var self = this;
			var selectedtems = [];
			var dataTable = self.dataTable;
			if(dataTable != null){
				selectedtems = dataTable.getSelection();
			}

			if(selectedtems.length < 1){
				return false;
			}
			var activitiesData = [];
			angular.forEach(selectedtems,function( item ){
				var trivialActivityInfo = self.activities.trivialManualActivities[item.activityOID];
				if(trivialActivityInfo){
					activitiesData.push(item.activityOID);
				}
			});

			if(selectedtems.length == activitiesData.length){
				return true;
			}
			return false;
		};

		/**
		 * 
		 */
		ActivityTableCompiler.prototype.isSelectionHomogenous = function( selectedRows ) {
			var firstItem = selectedRows[0];
			var matchArray = [];

			angular.forEach( selectedRows, function(row) {
				if (row.activity.qualifiedId === firstItem.activity.qualifiedId && row.modelOID === firstItem.modelOID) {
					matchArray.push(row);
				}
			});

			if(matchArray.length === selectedRows.length ){
				return true;
			}

			return false;
		};

		/**
		 * 
		 */
		ActivityTableCompiler.prototype.isSelectionDirty = function( activities ) { 
			var self = this;
			var activitiesWithDirtyForms =[]; 
			angular.forEach(activities,function(activity){

				if(self.dirtyDataForms.indexOf(activity.activityOID) > -1){
					activitiesWithDirtyForms.push(activity);
				}
			});

			if(activitiesWithDirtyForms.length > 0){
				return true;
			}

			return false;
		}


		/*
		 * 
		 */
		ActivityTableCompiler.prototype.completeAll = function(res) {
			var self = this;
			var STATUS_PARTIAL_SUCCESS = 'partialSuccess';
			var STATUS_SUCCESS = 'success';
			var STATUS__FAILURE = 'failure';

			self.completeActivityResult = {
					status : 'success',  //success failure partialSuccess
					notifications : [],
					nameIdMap : {}
			};

			var promise = res.promise;
			var selectedItems = self.selectedActivity;

			promise.then(function() {

				angular.forEach(selectedItems,function(item){
					self.completeActivityResult.nameIdMap[item.activityOID] = item.activity.name;
				});

				if (selectedItems.length > 0) {
					var activitiesData = [];

					if(self.completeDialog.confirmationType === 'dataMapping') {
						//When data fields are filled in a dialog
						angular.forEach(selectedItems, function(item, index) {
							var outData = self.completeDialog.outData;
							var dataMappings = {};
							angular.forEach( self.completeDialog.dataMappings,function(mapping){
								dataMappings[mapping.id] = mapping.typeName; 
							});
							activitiesData.push({
								oid : item.activityOID,
								outData : outData,
								dataMappings : dataMappings
							});
						});

					}else {
						//When data fields are filled inline in the table
						angular.forEach(selectedItems, function(item, index) {
							var trivialActivityInfo = self.activities.trivialManualActivities[item.activityOID];
							if(trivialActivityInfo) {
								var outData = trivialActivityInfo.inOutData;
								var dataMappings = {};
								angular.forEach( trivialActivityInfo.dataMappings,function(mapping){
									dataMappings[mapping.id] = mapping.typeName; 
								});

								activitiesData.push({oid: item.activityOID, outData: outData , dataMappings : dataMappings});
							}
						});
					}

					if (activitiesData.length > 0 ) {
						sdActivityInstanceService.completeAll(activitiesData).then(function(result) {

							self.showCompleteNotificationDialog = true;
							self.completeActivityResult.notifications = result;
							self.refresh();
							sdViewUtilService.syncLaunchPanels();

							if (result.failure.length > 0
									&& result.success.length > 0) {
								// partial Success
								self.completeActivityResult.status = STATUS_PARTIAL_SUCCESS;
								self.completeActivityResult.title = sgI18nService.translate('processportal.views-completeActivityDialog-notification-title-error','ERROR');
							} else if (result.success.length === activitiesData.length) {
								// Success
								self.completeActivityResult.status = STATUS_SUCCESS;
								self.completeActivityResult.title = sgI18nService.translate('processportal.views-completeActivityDialog-notification-title-success','SUCCESS');
							} else {
								self.completeActivityResult.status = STATUS__FAILURE;
								self.completeActivityResult.title = sgI18nService.translate('processportal.views-completeActivityDialog-notification-title-error','ERROR');
							}

						});
					} else {
						self.dataTable.setSelection([]);
					}
				}
			});
		};



		/**
		 * 
		 * @param rowItem
		 */
		ActivityTableCompiler.prototype.openCompleteDialog = function( rowItem) {

			var self = this;
			var CONFIRMATION_TYPE_SINGLE = 'single'
			var CONFIRMATION_TYPE_GENERIC = 'generic'
			var CONFIRMATION_TYPE_DATAMAPPING = 'dataMapping'
			
			self.completeAllDialog = {
				confirmLabel : sgI18nService.translate('processportal.views-common-messages.common-Yes','yes'),
				cancelLabel : sgI18nService.translate('processportal.views-common-messages.common-No','no'),
				title : sgI18nService.translate('processportal.views-completeActivityDialog-title','Confirm')
			};
				

			self.selectedActivity = [];
			self.completeDialog = {
					confirmationType : CONFIRMATION_TYPE_SINGLE,  //single / generic / dataMapping
					dataMappings : {},
					outData : {}
			};
			
			self.showCompleteDialog = true;

			if (angular.isDefined( rowItem )) {

				self.selectedActivity = [ rowItem ];
				self.completeDialog.confirmationType = CONFIRMATION_TYPE_SINGLE;
			}
			else {
				var selectedItems = this.dataTable.getSelection();
				if ( selectedItems.length > 0) {
					// Add rows having dirty field to selected activity
					this.selectedActivity = selectedItems;

					if ( this.isSelectionHomogenous( selectedItems ) && !this.isSelectionDirty( selectedItems ) ) {

						self.completeDialog.confirmationType = CONFIRMATION_TYPE_DATAMAPPING;
						var firstItem = selectedItems[0];
						self.completeDialog.dataMappings = angular
						.copy(self.activities.trivialManualActivities[firstItem.activityOID].dataMappings);
						self.completeDialog.outData =angular
						.copy(self.activities.trivialManualActivities[firstItem.activityOID].inOutData);
						
						self.completeAllDialog.confirmLabel = sgI18nService.translate('views-common-messages.common-confirm','Confirm');
						self.completeAllDialog.cancelLabel = sgI18nService.translate('views-common-messages.common-cancel','Cancel');
						self.completeAllDialog.title = sgI18nService.translate('processportal.views-completeActivityDialog-form-title','Complete Activities')
					} else {

						self.completeDialog.confirmationType = CONFIRMATION_TYPE_GENERIC;
					}
				}
			}
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.openDelegateDialog = function( rowItem ) {
			this.showDelegateDialog = true;
			if (angular.isDefined(rowItem)) {
				this.selectedActivity = [rowItem];
			} else {
				var selectedItems = this.dataTable.getSelection();
				if (selectedItems.length > 0) {
					this.selectedActivity = selectedItems;
				}
			}
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.onDelegateConfirm = function() {
			this.refresh();
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.openAbortDialog = function(value) {
			var self = this;
			this.activitiesToAbort = [];

			if (Array.isArray(value)) {
				var selectedItems = value;
				if (selectedItems.length < 1) {
					trace.log("No Rows selected");
					return;
				}

				angular.forEach(selectedItems, function( item ) {
					self.activitiesToAbort.push(item.activityOID);
				});
			} else {
				var item = value;
				this.activitiesToAbort.push(item.activityOID);
			}

			this.showAbortActivityDialog = true;
		}

		/*
		 *
		 */
		ActivityTableCompiler.prototype.abortCompleted = function( ) {
			this.refresh();
			sdViewUtilService.syncLaunchPanels();
			this.activitiesToAbort = [];
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.getDescriptorExportText = function(descriptors) {
			var descriptorsToExport  = [];

			angular.forEach(descriptors,function( descriptor){
				if( !descriptor.isDocument )
					descriptorsToExport.push(descriptor.key +" : "+descriptor.value);
			});
			return descriptorsToExport.join(',');
		};


		/**
		 * 
		 */
		ActivityTableCompiler.prototype.getDescriptorValueForExport = function( descriptorData ) {
			var exportValue;
			if( angular.isUndefined(descriptorData)){
				return;
			}
			if( descriptorData.isDocument) {

				var documentNames = [];
				angular.forEach(descriptorData.documents,function(document){
					documentNames.push(document.name)
				});
				exportValue = documentNames.join(',');
			} else {
				exportValue = descriptorData.value;
			}
			return exportValue;
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.storePriorities = function(data) {
			var self = this;
			if(this.priorityEditable){
				self.originalPriorities = {};
				self.changedPriorities = {};
				angular.forEach(data,function( row ) {
					self.originalPriorities[row.activityOID] = row.priority.value;
				});
			}
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.isPriorityChanged = function() {
			for ( name in this.changedPriorities ) {
				return true;
			}
			return false;
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.isPriorityChangedForRow = function( id ) {
			for ( name in this.changedPriorities ) {
				if (name == id){
					return true;
				}
			}
			return false;
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.registerNewPriority = function(activityOID, value) {
			var self = this;

			if(self.originalPriorities[activityOID] != value){
				self.changedPriorities[activityOID] = value;
			}else if(angular.isDefined(self.changedPriorities[activityOID])){
				delete self.changedPriorities[activityOID];
			}
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.savePriorityChanges = function() {
			var self = this;

			//Process instance oid to activity map for result
			var processActivityMap = {};
			//process oid to priority map to send to service
			var requestData = {};
			angular.forEach(self.activities.list,function( rowData ){
				if(angular.isDefined(self.changedPriorities[ rowData.activityOID ])){
					processActivityMap[rowData.processInstance.oid] =  	rowData.activity.name +' (#'+rowData.activityOID+')';
					requestData[rowData.processInstance.oid] = self.changedPriorities[ rowData.activityOID ];
				}
			});

			sdPriorityService.savePriorityChanges(requestData).then(
					function(successResult) {
						angular.forEach(successResult.success,function(data){
							data['item'] = processActivityMap[data.OID];
						});
						angular.forEach(successResult.failure,function(data){
							data['item'] = processActivityMap[data.OID];
						});
						self.updatePriorityNotification.visible = true;
						self.updatePriorityNotification.result = successResult;
						self.refresh();
						sdViewUtilService.syncLaunchPanels();

					}, function(failureResult) {
						trace.error("Error occured in updating the priorities : ",failureResult);
					});
		};


		/**
		 * 
		 */
		ActivityTableCompiler.prototype.isWorklistMode = function() {
			return this.mode === DEFAULT_VALUES.WORKLIST.NAME;
		};

		/**
		 * 
		 */
		ActivityTableCompiler.prototype.isActivityTableMode = function() {
			return this.mode === DEFAULT_VALUES.ACITIVITY_INSTANCE_VIEW.NAME;
		};

		/*
		 * 
		 */
		ActivityTableCompiler.prototype.showError = function(e) {
			trace.error('Error on activity table:', e);
			trace.printStackTrace();
			this.showError = "true";
			var errorToShow = 'Unknown Error';
			if (angular.isString(e)) {
				errorToShow = e;
			} else if (e.status != undefined && e.statusText != undefined) {
				errorToShow =  e.status + ' - ' + e.statusText;
			}
			this.errorMessage = 'sd-activity-table is unable to process table. Pls. refer browser console for details. Reason: ' + errorToShow;
		};
		

		/*
		 * 
		 */
		ActivityTableCompiler.prototype.performDefaultDelegate = function(scope, sdActivityInstanceService, sdDialogService, sgI18nService, rowItems) {
			var self = this;
			
			var containsCaseInstance = false;
			angular.forEach(rowItems,function(activity){
				if(activity.isCaseInstance){
					containsCaseInstance = true;
				}
			});
			
			if(containsCaseInstance){
				var title = sgI18nService.translate('views-common-messages.common-error','Error');
				var message  = sgI18nService.translate('views-common-messages.views-switchProcessDialog-caseAbort-message','Operation not suppored for case instances');
				sdDialogService.error(scope, message, title)
			}
			
			rowItems.every(function(activity) {
			    return !(activity.isCaseInstance);
			});
			
			var data = {};
			angular.forEach(rowItems, function(item){
				data[item.activityOID] = item.status.value;
			});
			sdActivityInstanceService.performDefaultDelegate(data).then(
					function(result) {
						if( result.failure.length > 0){
							var title = sgI18nService.translate('views-common-messages.common-error','Error');
							sdDialogService.error(scope, result.failure[0].message, title)
						}
						self.refresh();
					}, function(error) {
						trace.error("Error in performing default delegate :  "+error);
					});
		};
		

		return directiveDefObject;
	};
	
	/**
	 * 
	 */
	function replaceColumnNames(originalColumns , columnNameMap){
		var newColumns = [];
		angular.forEach(originalColumns,function(columnName){

			if(columnNameMap[columnName]){
				newColumns.push(columnNameMap[columnName])
			}else{
				newColumns.push(columnName)
			}
		});
		return newColumns;
	};
})();
