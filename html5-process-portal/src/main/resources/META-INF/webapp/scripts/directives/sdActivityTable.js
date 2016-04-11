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

(function() {
	'use strict';

	angular.module('bpm-common').directive(
			'sdActivityTable',
			[ '$parse', '$q', 'sdEnvConfigService', 'sdUtilService', 'sdViewUtilService', 'sdLoggerService', 'sdPreferenceService',
			  'sdWorklistService', 'sdActivityInstanceService', 'sdProcessInstanceService', 'sdProcessDefinitionService',
			  'sdCriticalityService', 'sdStatusService', 'sdPriorityService', '$filter', 'sgI18nService',
			  '$timeout', 'sdLoggedInUserService', 'sdDialogService', 'sdCommonViewUtilService','sdWorklistConstants', '$sce',
			  ActivityTableDirective ]);

	/*
	 *
	 */
	function ActivityTableDirective($parse, $q, sdEnvConfigService, sdUtilService, sdViewUtilService, sdLoggerService, sdPreferenceService,
			sdWorklistService, sdActivityInstanceService, sdProcessInstanceService, sdProcessDefinitionService, sdCriticalityService,
			sdStatusService, sdPriorityService, $filter, sgI18nService, $timeout, sdLoggedInUserService,
			sdDialogService, sdCommonViewUtilService, sdWorklistConstants, $sce) {

		var trace = sdLoggerService.getLogger('bpm-common.sdActivityTable');

		var directiveDefObject = {
				restrict : 'AE',
				require : '^?sdData',
				transclude : true,
				replace : true,
				scope : true, // Creates a new sub scope
				templateUrl : sdUtilService.getBaseUrl() + 'plugins/html5-process-portal/scripts/directives/partials/activityTable.html',
				compile : function(elem, attr, transclude) {
					processRawMarkup(elem, attr, transclude);

					return {
						post : function(scope, element, attr, ctrl) {
							var activityTableCompiler = new ActivityTableCompiler(scope, element, attr, ctrl);
						}
					};
				}
		};

		// Defaults
		var DEFAULT_VALUES = {
				MODE : 'worklist',
				WORKLIST : {
					NAME : 'worklist',
					VISIBLE_COLUMNS : [ 'activityName', 'activityOID', 'criticality', 'priority','descriptors',
					                    'startTime', 'lastModified', 'duration', 'lastPerformer', 'data' ],
					                    PREFERENCE_MODULE : 'ipp-workflow-perspective',
					                    SHOW_TRIVIAL_DATA_COLUMNS : true,
					                    COLUMN_NAME_MAP : {
					                    	"Overview" : "ActivityName",
					                    	"OID" : "ActivityOID",
					                    	"Started" : "StartTime",
					                    	"ProcessDefinition" : "ProcessName"
					                    }
				},
				ACITIVITY_INSTANCE_VIEW : {
					NAME : 'activityTable',
					VISIBLE_COLUMNS : [ 'activityName', 'activityOID', 'assignedTo', 'priority', 'criticality',
					                    'descriptors', 'startTime', 'LastModified', 'duration', 'assignedTo' ],
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
		function processRawMarkup(elem, attr, transclude) {
			try{
				processCustomTemplate(elem, attr, transclude);
				processTrivialDataColumn(elem, attr);
				processDescriptorColumns(elem, attr);
				processToolbar(elem, attr);
			}catch(e) {
				showError(e, elem);
				throw e;
			}
		}

		/**
		 * 
		 */
		function processCustomTemplate(elem, attr, transclude) {

			var customTemplate = {};

			transclude({}, function(clone) {
				for(var index = 0 ; index < clone.length; index++ ){
					if(jQuery(clone[index]).is("thead") ) {
						customTemplate.head = jQuery(clone[index])[0];
					} else if(jQuery(clone[index]).is("tbody")){
						customTemplate.body = jQuery(clone[index])[0];
					}
				}
			});


			if(customTemplate.head && customTemplate.body) {
				//Remove all the contents of the table
				jQuery(elem.find('table[sd-data-table]')[0]).empty();

				//Add contents from custom template
				jQuery(customTemplate.body).appendTo(elem.find('table[sd-data-table]')[0]);
				jQuery(customTemplate.head).appendTo(elem.find('table[sd-data-table]')[0]);

			}
		}

		/*
		 *
		 */
		function processTrivialDataColumn(elem, attr) {

			// Process Trivial Data Column
			var showTrivialDataColumn = DEFAULT_VALUES.WORKLIST.SHOW_TRIVIAL_DATA_COLUMNS;

			if (attr.sdaMode === DEFAULT_VALUES.ACITIVITY_INSTANCE_VIEW.NAME) {
				showTrivialDataColumn = DEFAULT_VALUES.ACITIVITY_INSTANCE_VIEW.SHOW_TRIVIAL_DATA_COLUMNS;
			}

			if (attr.sdaTrivialDataColumn && attr.sdaTrivialDataColumn === 'false') {
				showTrivialDataColumn = false;
			}
			// If not required remove the column
			if (!showTrivialDataColumn) {
				var cols = elem.find('[sda-column-type="TRIVIAL_DATA"]');
				cols.remove();
				// Toolbar
				var toolbar = elem.prev();
				var items = toolbar.find('[sda-column-type="TRIVIAL_DATA"]');
				items.remove();
			}
		}

		/*
		 *
		 */
		function processDescriptorColumns(elem, attr) {
			// Process Descriptor columns
			var showDescriptorCoulmns = true; // Default
			if (attr.sdaDescriptorColumns && attr.sdaDescriptorColumns === 'false') {
				showDescriptorCoulmns = false;
			}
			// If not required remove the column
			if (!showDescriptorCoulmns) {
				trace.debug("Removing descriptor columns.");
				var cols = elem.find('[sda-column-type="DESCRIPTOR_COLUMNS"]');
				cols.remove();
			}
		}


		/*
		 *
		 */
		function processToolbar(elem, attr) {

			var toolbarAttr = attr.sdaToolbar;

			if( angular.isUndefined(toolbarAttr) ) {
				return;
			}

			var toolbar = $parse(toolbarAttr)();

			if ( toolbar === false ) {
				elem.find('[sda-toolbar]').remove();
			} else if( toolbar === true ) {
				return true;
			} else if ( toolbar ) {
				var toolBarButtons = elem.find('[sda-toolbar] > div > button');
				var allAvailableButtons = ['columnSelector','export','saveFilters'];

				angular.forEach(toolBarButtons, function(button) {
					var buttonType = button.attributes['sda-toolbar-type'].value;
					if(toolbar.indexOf(buttonType) === -1) {
						jQuery(button).remove();
					}
					allAvailableButtons.push(buttonType);
				});

				//Check if passed toolbar names are correct.
				var invalidValues = [];
				angular.forEach(toolbar,function(button) {
					if(allAvailableButtons.indexOf(button) === -1) {
						invalidValues.push(button);
					}
				});
				if(invalidValues.length >  0) {
					throw 'Invalid value in sda-toolbar :' + invalidValues;
				}
			}
		}


		/*
		 *
		 */
		function ActivityTableCompiler(scope, element, attr, ctrl) {
			try {
				this.initialize(attr, scope, $filter, element);
			} catch (e) {
				showError(e);
			}

			/*
			 * Defined here as access required to scope
			 */
			if (angular.isDefined(ctrl) && ctrl !== null) {
				trace.debug("sdData is defined.Activity table will use provided custom source for data.");
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
		}


		/**
		 *
		 */
		ActivityTableCompiler.prototype.trustAsHtml = function(htmlStr) {
			return $sce.trustAsHtml(htmlStr);
		};


	

		/*
		 *
		 */
		ActivityTableCompiler.prototype.initialize = function(attr, scope, $filter, element) {
			var scopeToUse = scope.$parent;
			var self = this;
			this.scope = scope;

			// Define data
			this.activities = {};
			this.dataTable = null; // Handle to data table instance, to be set later

			// Abort Activity Data
			this.showAbortActivityDialog = false;
			this.activitiesToAbort = [];
			this.dirtyDataForms = [];

			// All processes with activities
			this.allAccessibleProcesses = null;
			this.allAvailableCriticalities = null;
			this.availableStates = [];
			this.availablePriorities = [];
			this.preferenceModule = "";
			this.preferenceId = "";

			this.registerMethods(attr, scope, $filter, element);

			this.actionsPopoverTemplateUrl = 
				this.prependBaseUrl('plugins/html5-process-portal/scripts/directives/partials/activityActionsPopover.html');

			this.openDocumentTemplateUrl =
				this.prependBaseUrl('plugins/html5-process-portal/scripts/directives/partials/documentPopoverActivityTable.html');

			this.abortMenuTemplateUrl =
				this.prependBaseUrl('plugins/html5-process-portal/scripts/directives/partials/abortActivityMenuPopover.html');

			this.abortMenuPopover = {
					toolbar : false
			};
			// Process Query
			if (!attr.sdaQuery && !attr.sdData) {
				throw 'Query attribute has to be specified if sdData is not specified.';
			}

			if (!attr.sdData) {
				var queryGetter = $parse(attr.sdaQuery);
				var query = queryGetter(scope);
				if (query === undefined) {
					throw 'Query evaluated to "nothing" for activity table.';
				}

				this.query = query;
			}

			// Mode Selector
			if (attr.sdaMode) {
				this.mode = attr.sdaMode;
			} else {
				this.mode = DEFAULT_VALUES.MODE;
			}

			if (this.mode === DEFAULT_VALUES.WORKLIST.NAME) {
				this.initializeWorklistMode(attr, scope);
			} else if (this.mode === DEFAULT_VALUES.ACITIVITY_INSTANCE_VIEW.NAME) {
				this.initializeActivityInstanceMode(attr, scope);
			} else {
				throw 'Not a valid value for sdaMode.Valid modes are : ' + DEFAULT_VALUES.WORKLIST.NAME + ' & '
				+ DEFAULT_VALUES.ACITIVITY_INSTANCE_VIEW.NAME;
			}

			this.customizeWithAttributeValues(attr, scope, scopeToUse);

			if(this.isToolBarVisible("columnSelector")) {
				this.columnSelector = sdLoggedInUserService.getUserInfo().isAdministrator ?  'admin' : true;
			}

			if(attr.sdaExtraColumns) {
				this.extraColumns = JSON.parse(attr.sdaExtraColumns);
			}
			if((query!=null && query.id == 'allResubmissionInstances')) {
				if(null == this.extraColumns) {
					this.extraColumns = [];
				}
				if(!this.showResubmissionTime()) {
					this.extraColumns.push("resubmissionTime");
				}
			}

			if (attr.sdaPageSize) {
				this.sdaPageSize = attr.sdaPageSize;
			}

			if (attr.sdaInitialSelection) {
				this.initialSelection = attr.sdaInitialSelection;
			}

			this.fetchDescriptorCols(element, attr);
			this.fetchAvailableStates();
			this.fetchAvailablePriorities();

			//Refreshing when Item is activated //remove on completion of server push
			this.refreshHandler = $parse(attr.sdaAutoRefresh);
			ActivityTableCompiler.prototype.registerRefresh = function(){
				this.refreshHandler(scopeToUse);
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
							//For Extanal Usage
							var exposedApi = {
									activate : self.activate
							} 
							
							exposedApi =  angular.merge( exposedApi, self.dataTable)
							assignable(scopeToUse, exposedApi);
						}
					}
					unregister();
				}
			});

			if (attr.sdaReady) {
				trace.log('Table defines sda-ready attribute, so deferring initialization...');
				var unregisterReady = scopeToUse.$watch(attr.sdaReady, function(newVal, oldVal) {
					if (newVal === true) {
						trace.log('sda-ready flag is triggered...');
						// Initialize after current digest cycle
						$timeout(function() {
							self.ready = true;
						});
						unregisterReady();
					}
				});
			}
		};
		
		
		/**
		 */
		ActivityTableCompiler.prototype.registerMethods = function(attr, scope, $filter, element) { 
			
			var self = this;

			this.preferenceDelegate = function(prefInfo) {
				trace.log('Fetching column preference for scope :',prefInfo.scope ,",preferenceId :",self.preferenceId,", preferenceName:",self.preferenceName);

				var preferenceStore = sdPreferenceService.getStore(prefInfo.scope, self.preferenceModule,
						self.preferenceId);
				preferenceStore.super_getValue = preferenceStore.getValue;
				// Override
				preferenceStore.getValue = function(name, fromParent) {
					var value = this.super_getValue(name, fromParent);

					if(name && name.indexOf(".columnFilterAndSortOrder") > -1){
						return value;
					}
					value = self.getColumnNamesByMode(value);
					return value;
				};

				// Override
				preferenceStore.marshalName = function(scope, name) {
					trace.debug("marshalName - scope:",scope,", name :", name);

					if(name) {
						return name;
					}

					var name = self.preferenceName;
					if (scope == 'PARTITION') {
						if (self.isWorklistMode() && this.parentStore && !this.parentStore[name]) {
							name = 'Default';
						}else if(self.isActivityTableMode()) {
							name = 'Default';
						}
					}
					return name;
				}
				return preferenceStore;
			};


			/**
			 *
			 */
			this.changeFormStatus = function(rowId) {
				var self = this;
				if (this.dirtyDataForms.indexOf(rowId) == -1) {
					this.dirtyDataForms.push(rowId);
				}

				// Auto select dirty rows
				var selectedRows = self.dataTable.getSelection();
				var matchArray = $filter('filter')(selectedRows, {
					activityOID : rowId
				}, true);
				var isRowSelected = matchArray.length > 0;

				if (!isRowSelected) {
					var rows = $filter('filter')(self.activities.list, {
						activityOID : rowId
					}, true);

					if (rows && rows.length === 1) {
						selectedRows.push({
							activityOID : rows[0].activityOID
						});
					}
					self.dataTable.setSelection(selectedRows);
				}
			};


			/**
			 *
			 */
			this.showResubmissionConfirmation = function(rowItem) {
				var self = this;
				trace.log('Worklist Item submitted for resubmission :',rowItem.activityOID);

				var title = sgI18nService.translate('views-common-messages.common-confirm', 'Confirm');

				var options = {
						title : title,
						dialogActionType : 'YES_NO'
				};

				var defer = sdDialogService.confirm(scope, sgI18nService.translate('processportal.views-worklistPanel-resubmit-confirm'), options);

				defer.then(function() {
					self.reactivateItem(rowItem, scope, self);
				});

			}

			/*
			 *
			 */
			self.openAbortPopover = function(rowItem) {
				var selectedItems = [];
				if (angular.isDefined(rowItem) && rowItem !=null) {
					selectedItems = [ rowItem ];
				} else {
					self.abortMenuPopover.toolbar = true;
					selectedItems = self.dataTable.getSelection();
				}

				var processesToAbort = [];
				angular.forEach(selectedItems, function(item) {
					processesToAbort.push(item.processInstance);
				});
				self.processesToAbort = processesToAbort;

			};

			/*
			 *
			 */
			self.joinCompleted = function(result) {
				self.refresh();
				if (angular.isDefined(result)) {
					if (angular.isDefined(result)) {
						sdCommonViewUtilService.openProcessInstanceDetailsView(result,true);
					}
				}
			};

			/*
			 *
			 */
			self.openJoinDialog = function() {
				self.showJoinProcessDialog = true;
				self.abortMenu.popover = false;
			};

			/*
			 *
			 */
			self.switchCompleted = function(result) {
				self.refresh();
				if (angular.isDefined(result)) {
					var name  =  sgI18nService.translate('views-common-messages.views-switchProcessDialog-worklist-title');
					var params = {
							pInstanceOids :  result.join(','),
							name : name,
							type : "processInstances"
					}
					sdViewUtilService.openView('worklistPanel', 'id='+new Date().getTime(), params, true);
				}
			};

			/*
			 *
			 */
			self.openSwitchDialog = function(pauseParentProcess) {
				self.showSwitchProcessDialog = true;
				self.abortMenuPopover.toolbar = false;
				self.pauseParentProcess = pauseParentProcess;
			};

			/**
			 *
			 * @param rowItems
			 */
			self.openDefaultDelegationDialog = function(rowItems) {
				var self = this;

				var options = {
						title : sgI18nService.translate('views-common-messages.common-confirm', 'Confirm'),
						dialogActionType : 'YES_NO'
				};

				var defer = sdDialogService.confirm
				(scope, sgI18nService.translate(
						'views-common-messages.views-strandedActivities-confirmDefaultDelegate'),
						options);

				defer.then(function() {
					self.performDefaultDelegate(scope, sdActivityInstanceService, sdDialogService, sgI18nService,
							rowItems);
				});
			};

			/**
			 * 
			 */
			self.getQuery = function() {
				var queryGetter = $parse(attr.sdaQuery);
				var query = queryGetter(scope);
				if (query === undefined) {
					throw 'Query evaluated to "nothing" for activity table.';
				}
				var queryValue = {};
				angular.copy(query,queryValue);
				return queryValue;
			}
			/**
			 *
			 * @param rowItem
			 */
			this.activateAndOpenView = function( rowItem ) {
				var interactionAware = sdEnvConfigService.getEventInterceptor() ? true : false;

				this.activate( rowItem.activityOID, interactionAware).then(
						function(result) {
							sdCommonViewUtilService.openActivityView(rowItem.activityOID, null, (rowItem.trivial ? self.cachedQuery : undefined), result);
							self.refresh();
						},
						function(result) {
							trace.error("Error in activating worklist item : ",rowItem.activityOID,".Error : ",  result.failure[0].message);
							var options = {
									title : sgI18nService.translate('views-common-messages.common-error', 'Error')
							};
							var message = result.failure[0].message;
							sdDialogService.error(scope, message, options);
						}
				);
				
			};
			
		};
		
		/**
		 *
		 */
		ActivityTableCompiler.prototype.activate = function(activityOID, isInteractionAware) {
			return sdActivityInstanceService.activate( activityOID, isInteractionAware);
		};

		/**
		 *
		 */
		ActivityTableCompiler.prototype.initializeWorklistMode = function(attr, scope) {
			this.priorityEditable = false;
			this.visibleColumns = DEFAULT_VALUES.WORKLIST.VISIBLE_COLUMNS;
			this.preferenceModule = DEFAULT_VALUES.WORKLIST.PREFERENCE_MODULE;
			this.exportFileName = "Worklist";
			this.initialSort = {
					name : 'startTime',
					dir : 'desc'
			};
		};

		/**
		 *
		 */
		ActivityTableCompiler.prototype.reactivateItem = function(rowItem, scope, methodScope) {
			var interpolate = $filter('interpolate');
			sdActivityInstanceService.reactivate(rowItem.activityOID).then(
					function(result) {
						if (result.failure.length > 0) {
							trace.error("Error in reactivating worklist item : ",rowItem.activityOID,".Error : ", result.failure[0].message);
							var options = {
									title : sgI18nService.translate('views-common-messages.common-error', 'Error')
							};
							var message = interpolate(sgI18nService.translate(
									'processportal.views-worklistPanel-resubmit-error', 'Error'),
									[ rowItem.activityOID ]);
							sdDialogService.error(scope, message, options)
						} else {
							sdCommonViewUtilService.openActivityView(rowItem.activityOID);
							methodScope.refresh();
						}
					});
		};

		/**
		 *
		 */
		ActivityTableCompiler.prototype.initializeActivityInstanceMode = function(attr, scope) {
			this.priorityEditable = true;
			this.originalPriorities = {};
			this.changedPriorities = {};
			this.defaultDelegateEnabled = false;
			this.initialSort = {
					name : 'startTime',
					dir : 'desc'
			};
			this.updatePriorityNotification = {
					error : false,
					result : {}
			};
			this.visibleColumns = DEFAULT_VALUES.ACITIVITY_INSTANCE_VIEW.VISIBLE_COLUMNS;
			this.exportFileName = "Activity_Table";

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
		ActivityTableCompiler.prototype.customizeWithAttributeValues = function(attr, scope, scopeToUse) {
			var self = this;
			var titleExpr = "";
			if (attr.sdaTitle) {
				titleExpr = attr.sdaTitle;
			}
			var titleGetter = $parse(titleExpr);
			this.title = titleGetter(scopeToUse);

			this.preferenceId = 'worklist-participant-columns';

			if (this.query) {

				if (this.query.type) {
					this.preferenceName = this.query.type;
				} else if (this.query.processQId) {
					this.preferenceName = this.query.processQId;
					this.preferenceId = 'worklist-process-columns';
				} else if (this.query.participantQId) {
					this.preferenceName = "{ipp-participant}" + this.query.participantQId;
				} else if (this.query.pInstanceOids) {
					this.preferenceName = "processInstances"
				}

				if(!this.preferenceName) {
					this.preferenceName = this.query.userId;
				}

				if(this.query.name) {
					this.exportFileName = this.exportFileName + " (" + this.query.name +")";
				}
			}

			if(!this.preferenceName) {
				this.preferenceName = sdLoggedInUserService.getUserInfo().id;
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

			if (attr.sdaIntialSort) {
				var sortGetter = $parse(attr.sdaInitialSort);
				this.intialSort = sortGetter(scopeToUse);
			}

			if (attr.sdaToolbar) {
				this.toolBarConfig =  $parse(attr.sdaToolbar)();
			}
			
			if (angular.isDefined(attr.sdaActions)) {
				this.actionsConfig =  $parse(attr.sdaActions)();
			}
		};

		
		
		/*
		 *
		 */
		ActivityTableCompiler.prototype.prependBaseUrl = function(url) {
			return sdUtilService.getBaseUrl() + url;
		};


		/*
		 *
		 */
		ActivityTableCompiler.prototype.refresh = function() {
			this.dataTable.refresh(true);
		};

		ActivityTableCompiler.prototype.showNotificationAndRefresh = function(notifications) {
			this.notification = notifications;
			this.showNotificationDialog = true;
			if(!sdUtilService.isEmpty(this.notification.result)){
				this.dataTable.refresh(true);
			}
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

			options.descriptorColumns = self.descriptorCols;

			if(this.query) {
				//Worklist mode
				self.cachedQuery = angular.extend({}, self.getQuery());
			} else {
				//Activity mode
				self.cachedQuery = {};
			}

			self.cachedQuery.options = options;

			var showResubmitLink = false;
			if(self.cachedQuery.id == 'allResubmissionInstances' || self.cachedQuery.type == 'resubmission'){
				showResubmitLink  = true;
			}
			options.extraColumns = self.extraColumns;

			if (angular.isDefined(this.sdDataCtrl)) { //If sdData is provided

				var dataResult = self.sdDataCtrl.retrieveData(self.cachedQuery);
				dataResult.then(function(data) {
					self.activities.list = data.list;
					self.activities.totalCount = data.totalCount;
					deferred.resolve(self.activities);
					self.safeApply();
					self.storePriorities(self.activities.list);
					sdViewUtilService.syncLaunchPanels();
				}, function(error) {
					deferred.reject(error);
				});

			}else { 									//If sdData is not provided

				if (this.mode != 'worklist') {
					throw 'sdData is not defined for sdActivityTable';
				}

				sdWorklistService.getWorklist(self.cachedQuery).then(function(data) {

					angular.forEach(data.list,function(item){
						item.showResubmitLink = showResubmitLink;
					});

					self.activities.list = data.list;
					self.activities.totalCount = data.totalCount;
					self.storePriorities(self.activities.list);

					var activityOIDs = [];
					angular.forEach(self.activities.list, function(workItem, index) {
						if ((workItem.trivial == undefined || workItem.trivial) && workItem.status.value != 2) {
							activityOIDs.push(workItem.activityOID);
						}
					});

					deferred.resolve(self.activities);
					sdViewUtilService.syncLaunchPanels();
				}, function(error) {
					deferred.reject(error);
				});
			}
			return deferred.promise;
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.fetchDescriptorCols = function(elem, attr) {
			var self = this;

			sdProcessDefinitionService.getDescriptorColumns().then(function(descriptors) {
				self.descriptorCols = [];
				angular.forEach(descriptors, function(descriptor) {
					self.descriptorCols.push({
						id : descriptor.id,
						field : "descriptorValues['" + descriptor.id + "'].value",
						title : descriptor.title,
						dataType : descriptor.type,
						sortable : descriptor.sortable,
						filterable : descriptor.filterable
					});
				});

				if (attr.sdaReady) {
					self.descriptorsReady = true;
				} else {
					$timeout(function(){
						//Handle Columns
						try {
							self.processTableColumns(elem, attr);
							self.ready = true;
						}catch(e) {
							showError(e, elem);
						}
					});
				}
				self.safeApply();
			});
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.fetchAllProcesses = function() {
			var self = this;
			var deferred = $q.defer();

			if (!self.allAccessibleProcesses) {
				sdProcessDefinitionService.getAllProcesses(false).then(function(processes) {
					self.allAccessibleProcesses = processes;
					deferred.resolve(self.allAccessibleProcesses);
				});
			} else {
				deferred.resolve(self.allAccessibleProcesses);
			}

			return deferred.promise;
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.fetchAllAvailableCriticalities = function() {
			var self = this;
			var deferred = $q.defer();

			if (!self.allAvailableCriticalities) {
				sdCriticalityService.getAllCriticalities().then(function(criticalities) {
					self.allAvailableCriticalities = criticalities;
					deferred.resolve(self.allAvailableCriticalities);
				});
			} else {
				deferred.resolve(self.allAvailableCriticalities);
			}

			return deferred.promise;
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.fetchAvailableStates = function() {
			var self = this;
			sdStatusService.getAllActivityStates().then(function(value) {
				self.availableStates = value;
			});
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.fetchAvailablePriorities = function() {
			var self = this;
			sdPriorityService.getAllPriorities().then(function(data) {
				self.availablePriorities = data;
			});
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.activateItem = function(rowItem) {

			if(rowItem.showResubmitLink){
				trace.debug("Openinig resubmission confirmation for ",rowItem.activityOID);
				this.showResubmissionConfirmation(rowItem);
			}else{
				trace.debug("Activate :",rowItem.activityOID);
				this.activateAndOpenView(rowItem);
			}
			this.registerRefresh();
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.openNotes = function(rowItem) {
			//sdCommonViewUtilService.openNotesView(rowItem.processInstance.oid, true);
			sdCommonViewUtilService.openNotesViewHTML5(rowItem.processInstance.oid, rowItem.processInstance.processName, true); // do not remove this line
		};

		/**
		 *
		 * @param rowItem
		 */
		ActivityTableCompiler.prototype.openRelocationDialog = function(rowItem) {
			var self = this;
			sdActivityInstanceService.getRelocationTargets(rowItem.activityOID).then(function(targets) {
				rowItem.relocationTargets = [];
				if (targets) {
					jQuery.each(targets, function(_, target) {
						rowItem.relocationTargets.push({
							name: target.activity.name,
							id: target.activity.id
						})
					});
				}
				if (rowItem.relocationTargets.length > 0) {
					rowItem.showRelocationDialog = true;
				} else {
					rowItem.showNoRelocationTargetsDialog = true;
				}
			});

		};

		/**
		 *
		 * @param rowItem
		 */
		ActivityTableCompiler.prototype.relocateActivity = function(rowItem) {
			var self = this;
			sdActivityInstanceService.relocate(rowItem.activityOID, rowItem.selectedTarget).then(function() {
				self.refresh();
			}, function(errorMessage) {
				trace.error("Error in relocating worklist item : " , rowItem.activityOID , ".Error : " , errorMessage);
				var options = {
						title : sgI18nService.translate('views-common-messages.common-error', 'Error')
				};
				var message = errorMessage ? sgI18nService.translate(errorMessage) : sgI18nService.translate('processportal.toolbars-workflowActions-relocation-dialog-notAuthorized');
				sdDialogService.error(self.scope, message, options)
			});
			rowItem.showRelocationDialog = false;
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.openProcessDocumentsPopover = function(rowItem) {
			var self = this;

			self.processPopover = {
					data: rowItem
			}
			rowItem.contentLoaded = false;

			var promise1 = sdProcessInstanceService.getProcessInstanceDocuments(rowItem.processInstance.oid).then(
					function(dataPathValues) {
						rowItem.supportsProcessAttachments = rowItem.processInstance.supportsProcessAttachments;
						rowItem.specificDocuments = [];
						jQuery.each(dataPathValues, function(_, dataPathValue) {
							if (dataPathValue.dataPath.id === 'PROCESS_ATTACHMENTS') {
								rowItem.processAttachments = dataPathValue;
							} else {
								rowItem.specificDocuments.push(dataPathValue);
							}
						});
					});

			var promise2 = sdProcessInstanceService.getCorrespondenceFolder(rowItem.processInstance.oid).then(
					function(correspondenceFolder) {
						rowItem.correspondences = correspondenceFolder.folders;
					}, function(correspondenceFolder){
						rowItem.correspondences = [];
					});

			$q.all([promise1, promise2]).finally(function() {
				rowItem.contentLoaded = true;
				self.processPopover.data = rowItem;
				self.processPopover.showDocumentPopover = true;
			});
		}


		/*
		 *
		 */
		ActivityTableCompiler.prototype.openDocumentsView = function(docId) {
			sdCommonViewUtilService.openDocumentView(docId);
		};

		/**
		 *
		 */
		ActivityTableCompiler.prototype.openCorrespondenceView = function(folder) {
			sdCommonViewUtilService.openCorrespondenceView(folder);
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.openAllProcessDocumentViews = function(rowItem) {
			var self = this;
			if (rowItem.processAttachments) {
				jQuery.each(rowItem.processAttachments.documents, function(_, doc) {
					self.openDocumentsView(doc.uuid);
				});
			}
			if (rowItem.specificDocuments) {
				jQuery.each(rowItem.specificDocuments, function(_, specificDataPath) {
					jQuery.each(specificDataPath.documents, function(_, doc) {
						self.openDocumentsView(doc.uuid);
					});
				});
			}
			if (rowItem.correspondences) {
				jQuery.each(rowItem.correspondences, function(_, folder) {
					sdCommonViewUtilService.openCorrespondenceView(folder);
				});
			}
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.openProcessDetails = function(rowItem) {
			if (this.isWorklistMode()) {
				rowItem.defaultCaseActivity ? sdCommonViewUtilService.openCaseDetailsView(rowItem.processInstance.oid,
						true) : sdCommonViewUtilService.openProcessInstanceDetailsView(rowItem.processInstance.oid,
								true);
			} else if (this.isActivityTableMode()) {
				rowItem.isCaseInstance ? sdCommonViewUtilService.openCaseDetailsView(rowItem.processInstance.oid, true)
						: sdCommonViewUtilService.openProcessInstanceDetailsView(rowItem.processInstance.oid, true);
			}
		};

		/**
		 *
		 */
		ActivityTableCompiler.prototype.containsAllTrivialManualActivities = function() {
			var self = this;
			var selectedItems = [];
			var dataTable = self.dataTable;
			if (dataTable != null) {
				selectedItems = dataTable.getSelection();
			}

			if (selectedItems.length < 1) {
				return false;
			}
			var activitiesData = [];
			angular.forEach(selectedItems, function(item) {
				if (item.dataMappings) {
					activitiesData.push(item.activityOID);
				}
			});

			if (selectedItems.length == activitiesData.length) {
				return true;
			}

			return false;
		};

		/**
		 *
		 */
		ActivityTableCompiler.prototype.isSelectionHomogenous = function(selectedRows) {
			var firstItem = selectedRows[0];
			var matchArray = [];

			angular.forEach(selectedRows,
					function(row) {
				if (row.activity.qualifiedId === firstItem.activity.qualifiedId
						&& row.modelOID === firstItem.modelOID) {
					matchArray.push(row);
				}
			});

			if (matchArray.length === selectedRows.length) {
				return true;
			}

			return false;
		};

		/**
		 *
		 */
		ActivityTableCompiler.prototype.isSelectionDirty = function(activities) {
			var self = this;
			var activitiesWithDirtyForms = [];
			angular.forEach(activities, function(activity) {

				if (self.dirtyDataForms.indexOf(activity.activityOID) > -1) {
					activitiesWithDirtyForms.push(activity);
				}
			});

			if (activitiesWithDirtyForms.length > 0) {
				return true;
			}

			return false;
		}
		
		ActivityTableCompiler.prototype.completeAll = function() {
			var self = this;
			var STATUS_PARTIAL_SUCCESS = 'partialSuccess';
			var STATUS_SUCCESS = 'success';
			var STATUS__FAILURE = 'failure';

			self.completeActivityResult = {
					status : 'success', // success failure partialSuccess
					notifications : [],
					nameIdMap : {}
			};

			var selectedItems = self.selectedActivity;
			
			angular.forEach(selectedItems, function(item) {
				self.completeActivityResult.nameIdMap[item.activityOID] = item.activity.name;
			});

			if (selectedItems.length > 0) {
				var activitiesData = [];

				if (self.completeDialog.confirmationType === 'dataMapping') {
					// When data fields are filled in a dialog
					angular.forEach(selectedItems, function(item, index) {
						var outData = self.completeDialog.outData;
						var dataMappings = {};
						angular.forEach(self.completeDialog.dataMappings, function(mapping) {
							dataMappings[mapping.id] = mapping.typeName;
						});
						activitiesData.push({
							oid : item.activityOID,
							outData : outData,
							dataMappings : dataMappings
						});
					});

				} else {
					// When data fields are filled inline in the
					// table
					angular.forEach(selectedItems,
							function(item, index) {
						if (item.dataMappings) {
							var outData = item.inOutData;
							var dataMappings = {};
							angular.forEach(item.dataMappings, function(
									mapping) {
								dataMappings[mapping.id] = mapping.typeName;
							});

							activitiesData.push({
								oid : item.activityOID,
								outData : outData,
								dataMappings : dataMappings
							});
						}
					});
				}

				if (activitiesData.length > 0) {
					trace.debug("Complete activity called for ",activitiesData.length ," activities.");
					sdActivityInstanceService
					.completeAll(activitiesData)
					.then(
							function(result) {

								self.showCompleteNotificationDialog = true;
								self.completeActivityResult.notifications = result;
								self.refresh();


								if (result.failure.length > 0 && result.success.length > 0) {
									// partial Success
									self.completeActivityResult.status = STATUS_PARTIAL_SUCCESS;
									self.completeActivityResult.title = sgI18nService
									.translate(
											'processportal.views-completeActivityDialog-notification-title-error',
									'ERROR');
									trace.debug("Complete activity finished with partial success.");
									trace.debug("Failed  activites - ",result.failure);
								} else if (result.success.length === activitiesData.length) {
									// Success
									self.completeActivityResult.status = STATUS_SUCCESS;
									self.completeActivityResult.title = sgI18nService
									.translate(
											'processportal.views-completeActivityDialog-notification-title-success',
									'SUCCESS');
									trace.debug("Complete activity finished with no failures.");
								} else {
									self.completeActivityResult.status = STATUS__FAILURE;
									self.completeActivityResult.title = sgI18nService
									.translate(
											'processportal.views-completeActivityDialog-notification-title-error',
									'ERROR');
									trace.debug("Complete activity failed.");
									trace.debug("Failed  activites - ",result.failure);
								}

							});
				} else {
					self.dataTable.setSelection([]);
				}
			}
		};

		/**
		 *
		 * @param rowItem
		 */
		ActivityTableCompiler.prototype.openCompleteDialog = function(rowItem) {

			var self = this;
			var CONFIRMATION_TYPE_SINGLE = 'single'
				var CONFIRMATION_TYPE_GENERIC = 'generic'
					var CONFIRMATION_TYPE_DATAMAPPING = 'dataMapping'

						self.completeAllDialog = {
					confirmLabel : sgI18nService.translate('portal-common-messages.common-yes', 'Yes'),
					cancelLabel : sgI18nService.translate('portal-common-messages.common-no', 'No'),
					title : sgI18nService.translate('processportal.views-completeActivityDialog-title', 'Confirm')
			};

			self.selectedActivity = [];
			self.completeDialog = {
					confirmationType : CONFIRMATION_TYPE_SINGLE, // single /
					// generic /
					// dataMapping
					dataMappings : {},
					outData : {}
			};

			self.showCompleteDialog = true;

			if (angular.isDefined(rowItem)) {

				self.selectedActivity = [ rowItem ];
				self.completeDialog.confirmationType = CONFIRMATION_TYPE_SINGLE;
			} else {
				var selectedItems = this.dataTable.getSelection();
				if (selectedItems.length > 0) {
					// Add rows having dirty field to selected activity
					this.selectedActivity = selectedItems;

					if (this.isSelectionHomogenous(selectedItems) && !this.isSelectionDirty(selectedItems)) {

						self.completeDialog.confirmationType = CONFIRMATION_TYPE_DATAMAPPING;
						var firstItem = selectedItems[0];
						self.completeDialog.dataMappings = angular
						.copy(firstItem.dataMappings);
						self.completeDialog.outData = angular
						.copy(firstItem.inOutData);

						self.completeDialog.description = firstItem.activity.description;

						self.completeAllDialog.confirmLabel = sgI18nService.translate(
								'processportal.views-completeActivityDialog-homogeneous-complete', 'Complete');
						self.completeAllDialog.cancelLabel = sgI18nService.translate(
								'views-common-messages.common-cancel', 'Cancel');
						self.completeAllDialog.title = firstItem.activity.name;
					} else {

						self.completeDialog.confirmationType = CONFIRMATION_TYPE_GENERIC;
					}
				}
			}
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.openDelegateDialog = function(rowItem) {
			this.showDelegateDialog = true;
			if (angular.isDefined(rowItem)) {
				this.selectedActivity = [ rowItem ];
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
		ActivityTableCompiler.prototype.openAbortDialog = function(value) {
			var self = this;
			this.activitiesToAbort = [];

			if (Array.isArray(value)) {
				var selectedItems = value;
				if (selectedItems.length < 1) {
					return;
				}
				angular.forEach(selectedItems, function(item) {
					self.activitiesToAbort.push(item.activityOID);
				});
			} else {
				var item = value;
				this.activitiesToAbort.push(item.activityOID);
			}

			this.showAbortActivityDialog = true;
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.getDescriptorExportText = function(descriptors) {
			var descriptorsToExport = [];

			angular.forEach(descriptors, function(descriptor) {
				if (!descriptor.isDocument)
					descriptorsToExport.push(descriptor.key + " : " + descriptor.value);
			});
			return descriptorsToExport.join(',');
		};

		/**
		 *
		 */
		ActivityTableCompiler.prototype.getDescriptorValueForExport = function(descriptorData) {
			var exportValue;
			if (angular.isUndefined(descriptorData)) {
				return;
			}
			if (descriptorData.isDocument) {

				var documentNames = [];
				angular.forEach(descriptorData.documents, function(document) {
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
			if (this.priorityEditable) {
				self.originalPriorities = {};
				self.changedPriorities = {};
				angular.forEach(data, function(row) {
					self.originalPriorities[row.activityOID] = row.priority.value;
				});
			}
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.isPriorityChanged = function() {
			for (name in this.changedPriorities) {
				return true;
			}
			return false;
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.isPriorityChangedForRow = function(id) {
			for (name in this.changedPriorities) {
				if (name == id) {
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

			if (self.originalPriorities[activityOID] != value) {
				self.changedPriorities[activityOID] = value;
			} else if (angular.isDefined(self.changedPriorities[activityOID])) {
				delete self.changedPriorities[activityOID];
			}
		};

		/*
		 *
		 */
		ActivityTableCompiler.prototype.savePriorityChanges = function() {
			var self = this;

			// Process instance oid to activity map for result
			var processActivityMap = {};
			// process oid to priority map to send to service
			var requestData = {};
			angular.forEach(self.activities.list, function(rowData) {
				if (angular.isDefined(self.changedPriorities[rowData.activityOID])) {
					processActivityMap[rowData.processInstance.oid] = rowData.activity.name + ' (#'
					+ rowData.activityOID + ')';
					requestData[rowData.processInstance.oid] = self.changedPriorities[rowData.activityOID];
				}
			});

			sdPriorityService.savePriorityChanges(requestData).then(function(successResult) {
				angular.forEach(successResult.success, function(data) {
					data['item'] = processActivityMap[data.OID];
				});
				angular.forEach(successResult.failure, function(data) {
					data['item'] = processActivityMap[data.OID];
				});
				self.updatePriorityNotification.visible = true;
				self.updatePriorityNotification.result = successResult;
				self.refresh();

			}, function(failureResult) {
				trace.error("Error occured in updating the priorities : ", failureResult);
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
		ActivityTableCompiler.prototype.showResubmissionTime = function() {
			if(this.extraColumns && $.inArray('resubmissionTime', this.extraColumns) > -1) {
				return true;
			}
			return false;
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
		ActivityTableCompiler.prototype.performDefaultDelegate = function(scope, sdActivityInstanceService,
				sdDialogService, sgI18nService, rowItems) {
			var self = this;
			var containsCaseInstance = false;

			angular.forEach(rowItems, function(activity) {
				if (activity.isCaseInstance) {
					containsCaseInstance = true;
				}
			});

			if (containsCaseInstance) {
				var options = {
						title : sgI18nService.translate('views-common-messages.common-error', 'Error')
				};
				var message = sgI18nService.translate(
						'views-common-messages.views-switchProcessDialog-caseAbort-message',
				'Operation not suppored for case instances');
				sdDialogService.error(scope, message, options)
			}

			rowItems.every(function(activity) {
				return !(activity.isCaseInstance);
			});

			var data = {};
			angular.forEach(rowItems, function(item) {
				data[item.activityOID] = item.status.value;
			});
			sdActivityInstanceService.performDefaultDelegate(data).then(function(result) {
				if (result.failure.length > 0) {
					var options = {
							title : sgI18nService.translate('views-common-messages.common-error', 'Error')
					};
					sdDialogService.error(scope, result.failure[0].message, options)
				}
				self.refresh();
			}, function(error) {
				trace.error("Error in performing default delegate :",error);
			});
		};
		
		
		/*
		 *
		 */
		ActivityTableCompiler.prototype.isActionButtonVisible = function(columnName) {
			var actions = this.actionsConfig;
			
			if ( actions === false ) {
				return false;
			} else if( actions === true ) {
				return true;
			} else if ( actions ) {
				return contains( actions, columnName);
			}
			return true;
		}

		/**
		 *
		 */
		ActivityTableCompiler.prototype.processTableColumns = function(elem, attr) {
			var self = this;
			var scopeToUse = this.scope.$parent;
			// Dont Consider sdaVisibleColumns if sdaColumns is set
			if (attr.sdaVisibleColumns && !attr.sdaColumns) {
				self.visibleColumns  = $parse(attr.sdaVisibleColumns)(scopeToUse);
			}

			if (!attr.sdaColumns) {
				return;
			}
			var columns = $parse(attr.sdaColumns)(scopeToUse);
			var requiredColumns = [];
			var currentOrder = [];

			angular.forEach(columns, function(column){
				requiredColumns.push(column.name);
			});

			var headColumns = elem.find('table[sd-data-table] > thead > tr > th');
			var bodyColumns = elem.find('table[sd-data-table] > tbody > tr > td');

			// Retaining required columns
			for(var col = 0 ; col < headColumns.length ; col++ ) {
				var columnElement = angular.element(headColumns[col]);

				if(requiredColumns.indexOf(columnElement.attr('sda-name')) === -1) {
					var bodyElement = angular.element(bodyColumns[col]);
					columnElement.remove();
					bodyElement.remove();
				}else {
					currentOrder.push(columnElement.attr('sda-name'));
				}
			}

			//Check validity of passed columns
			if(currentOrder.length !== requiredColumns.length){
				var incorrectColumns = [];
				angular.forEach(requiredColumns, function(column){
					if (currentOrder.indexOf(column) === -1) {
						incorrectColumns.push(column);
					}
				})
				throw "Invalid value in sda-columns :" +incorrectColumns;
			}

			// Reordering columnns
			var table =  elem.find('table[sd-data-table]');
			for(var colIndex = requiredColumns.length -1; colIndex >=  0; colIndex-- ) {
				var presentIndex = currentOrder.indexOf(requiredColumns[colIndex]);

				if(colIndex === presentIndex) {
					continue;
				}
				if(colIndex != presentIndex) {
					moveColumn(table, presentIndex, colIndex);
				}
			}

			this.definedColumnAtrributes = {};
			angular.forEach(columns, function(column) {
				var columnDef = angular.copy(column);
				delete columnDef.name;
				self.definedColumnAtrributes[column.name] = columnDef;
			});
		};



		/**
		 *
		 */
		ActivityTableCompiler.prototype.isToolBarVisible = function( name ) {
			var toolBarConfig = this.toolBarConfig;

			if(angular.isUndefined(toolBarConfig) || (toolBarConfig === true)) {
				return true;
			}

			if(toolBarConfig === false) {
				return false;
			}

			return this.toolBarConfig.indexOf(name) !== -1;
		};

		/**
		 *
		 */
		ActivityTableCompiler.prototype.isColumnVisible = function(columnName) {
			if(!angular.isDefined(this.definedColumnAtrributes)) {
				return contains(this.visibleColumns, columnName);
			}
			var attributes = this.definedColumnAtrributes;
			if(attributes && attributes[columnName] && angular.isDefined(attributes[columnName].visible) ) {
				return this.definedColumnAtrributes[columnName].visible;
			}
			return true;
		};

		/**
		 *
		 */
		ActivityTableCompiler.prototype.isDefinedSortable = function(columnName, defaultValue) {
			var attributes = this.definedColumnAtrributes;
			if(attributes && attributes[columnName] && angular.isDefined(attributes[columnName].sort) ) {
				return (self.definedColumnAtrributes[columnName].sort === true) ? defaultValue : false;
			}
			return defaultValue;
		};

		/**
		 *
		 */
		ActivityTableCompiler.prototype.isDefinedFilterable = function(columnName, defaultValue) {
			var attributes = this.definedColumnAtrributes;
			if(attributes && attributes[columnName] && angular.isDefined(attributes[columnName].filter) ) {
				return (this.definedColumnAtrributes[columnName].filter === true) ? defaultValue : false;
			}
			return defaultValue;
		};

		/**
		 *
		 */
		ActivityTableCompiler.prototype.isDefinedFixed = function(columnName, defaultValue) {
			var attributes = this.definedColumnAtrributes;
			if(attributes && attributes[columnName] && angular.isDefined(attributes[columnName].fixed) ) {
				return this.definedColumnAtrributes[columnName].fixed;
			}
			return defaultValue;
		};
		
		/**
		 *
		 */
		ActivityTableCompiler.prototype.getColumnNamesByMode = function getColumnNamesByMode(value) {
			if (angular.isUndefined(value)) {
				return value;
			}

			if (this.mode === DEFAULT_VALUES.WORKLIST.NAME) {
				var prefValue = JSON.parse(value);
				prefValue.selectedColumns = replaceColumnNames(prefValue.selectedColumns,
						DEFAULT_VALUES.WORKLIST.COLUMN_NAME_MAP);
				value = JSON.stringify(prefValue);

			} else {
				try {
					var prefValue = JSON.parse(value);
					// Do nothing
				} catch (e) {
					var prefColumns = value.split('$#$');
					value = replaceColumnNames(prefColumns, DEFAULT_VALUES.ACITIVITY_INSTANCE_VIEW.COLUMN_NAME_MAP)
					.join('$#$');
				}
			}
			return value;
		};

		/**
		 *
		 */
		function contains( list, value) {
			var found = $filter('filter')(list, value);
			if (found && found.length > 0) {
				return true;
			}
			return false;
		}


		/*
		 *
		 */
		function showError (e, element) {
			trace.error('Error on activity table:', e);
			trace.printStackTrace();
			var errorToShow = 'Unknown Error';
			if (angular.isString(e)) {
				errorToShow = e;
			} else if (e.status != undefined && e.statusText != undefined) {
				errorToShow = e.status + ' - ' + e.statusText;
			}
			var errorMessage = 'sd-activity-table is unable to process table. Pls. refer browser console for details. Reason: '
				+ errorToShow;
			jQuery('<pre class="tbl-error">' + errorMessage + '</pre>').insertBefore(element);
		};

		return directiveDefObject;
	}

	/**
	 *
	 */
	function replaceColumnNames(originalColumns, columnNameMap) {
		var newColumns = [];
		angular.forEach(originalColumns, function(columnName) {

			if (columnNameMap[columnName]) {
				newColumns.push(columnNameMap[columnName])
			} else {
				newColumns.push(columnName)
			}
		});
		return newColumns;
	}

	/**
	 *
	 */
	function moveColumn (table, from, to) {
		var rows = jQuery('tr', table);
		var cols;
		rows.each(function() {
			cols = jQuery(this).children('th, td');
			cols.eq(from).detach().insertBefore(cols.eq(to));
		});
	}

})();
