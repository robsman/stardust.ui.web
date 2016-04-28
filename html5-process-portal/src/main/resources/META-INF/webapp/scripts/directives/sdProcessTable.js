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
 * @author Nikhil.Gahlot
 */

(function() {
	'use strict';

	angular.module('bpm-common').directive(
			'sdProcessTable',
			[ '$parse', '$q', '$timeout', '$filter', 'sdUtilService', 'sdViewUtilService', 'sdLoggerService',
					'sgI18nService', 'sdPreferenceService', 'sdProcessInstanceService', 'sdProcessDefinitionService',
					'sdStatusService', 'sdPriorityService', 'sdDialogService', 'sdCommonViewUtilService','sdLoggedInUserService',
					ProcessTableDirective ]);

	/*
	 *
	 */
	function ProcessTableDirective($parse, $q, $timeout, $filter, sdUtilService, sdViewUtilService, sdLoggerService,
			sgI18nService, sdPreferenceService, sdProcessInstanceService, sdProcessDefinitionService, sdStatusService,
			sdPriorityService, sdDialogService, sdCommonViewUtilService, sdLoggedInUserService) {

		//Defaults
		var DEFAULT_VALUES = {
			VISIBLE_COLUMNS : [ 'processName', 'processOID', 'priority', 'descriptors', 'startingUser', 'startTime',
					'duration' ],
			PREFERENCE_MODULE : 'ipp-views-common'
		};

		var trace = sdLoggerService.getLogger('bpm-common.sdProcessTable');

		var directiveDefObject = {
			restrict : 'AE',
			require : '^?sdData',
			scope : true, // Creates a new sub scope
			templateUrl : sdUtilService.getBaseUrl() + 'plugins/html5-process-portal/scripts/directives/partials/processTable.html',
			compile : function(elem, attr, transclude) {
				return {
					post : function(scope, element, attr, ctrl) {
						new ProcessTableCompiler(scope, element, attr, ctrl);
					}
				};
			}
		};

		/*
		 *
		 */
		function ProcessTableCompiler(scope, element, attr, ctrl) {
			var self = this;




			/*
			 *
			 */
			ProcessTableCompiler.prototype.safeApply = function() {
				sdUtilService.safeApply(scope);
			};

			/*
			 *
			 */
			ProcessTableCompiler.prototype.initialize = function(attr, scope) {
				var self = this;
				var scopeToUse = scope.$parent;

				// Define data
				this.processList = {};
				this.dataTable = null; // Handle to data table instance, to be set later

				//Abort Data
				this.showAbortProcessDialog = false;
				this.processesToAbort = [];
				this.processesToJoin = [];
				this.allAccessibleProcesses = null;
				this.availableStates = [];

				// Set Default values
				this.visbleColumns = DEFAULT_VALUES.VISIBLE_COLUMNS;
				this.processTablePrefModule = DEFAULT_VALUES.PREFERENCE_MODULE;
				this.exportFileName = "Processes";
				this.columnSelector = sdLoggedInUserService.getUserInfo().isAdministrator ?  'admin' : true;
				this.initialSort = {
					name : 'startTime',
					dir : 'desc'
				};

				// Set custom values
				this.customizeWithAttributeValues(attr, scope, scopeToUse);
				// Process TableHandle and then set data table instance
				this.tableHandleExpr = 'processTableCtrl.dataTable';

				// Priority
				this.priorityEditable = true;
				this.originalPriorities = {};
				this.changedPriorities = {};
				this.updatePriorityNotification = {
					error : false,
					result : {}
				};

				this.actionsPopover = {
							url : sdUtilService.getBaseUrl()  +'plugins/html5-process-portal/scripts/directives/partials/processActionsPopover.html'
				};

				this.caseMenuPopover = {
						url : sdUtilService.getBaseUrl()  + 'plugins/html5-process-portal/scripts/directives/partials/caseMenuPopover.html',
						visible : false
				}

				this.processDocumentsPopover = {
						url : sdUtilService.getBaseUrl()  + 'plugins/html5-process-portal/scripts/directives/partials/documentPopoverProcessTable.html'
				}

				this.abortProcessPopover = {
						url : sdUtilService.getBaseUrl()  + 'plugins/html5-process-portal/scripts/directives/partials/abortProcessMenuPopover.html',
						visible : false
				}

				var unregister = scope.$watch(self.tableHandleExpr, function(newVal, oldVal) {
					if (newVal != undefined && newVal != null && newVal != oldVal) {
						if (attr.sdProcessTable) {
							var assignable = $parse(attr.sdProcessTable).assign;
							if (assignable) {
								assignable(scopeToUse, self.dataTable);
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

				if (attr.sdaSelection) {
					var assignable = $parse(attr.sdaSelection).assign;
					if (assignable) {
						this.selection = null;
						this.selectionExpr = 'processTableCtrl.selection';

						// Update parent for change in sdDataTable
						scope.$watch(self.selectionExpr, function(newVal, oldVal) {
							if (newVal != undefined && newVal != null && newVal != oldVal) {
								assignable(scopeToUse, self.selection);
							}
						});

						// Update for sdDataTable for change in parent
						scopeToUse.$watch(attr.sdaSelection, function(newVal, oldVal) {
							if (newVal != undefined && newVal != null && newVal != self.selection) {
								this.selection = newVal;
							}
						});
					}
				}

				this.descritorCols = [];

				this.fetchDescriptorCols();
				this.fetchAvailableStates();
				this.fetchAvailablePriorities();
			};

			/**
			 *
			 */
			ProcessTableCompiler.prototype.customizeWithAttributeValues = function(attr, scope, scopeToUse) {
				// Process Title
				var self = this;
				var titleExpr = "";
				if (attr.sdaTitle) {
					titleExpr = attr.sdaTitle;
				}
				var titleGetter = $parse(titleExpr);
				self.title = titleGetter(scopeToUse);

				// Preference attributes
				if (attr.sdaPreferenceModule) {
					self.processTablePrefModule = attr.sdaPreferenceModule;
				}

				if (attr.sdaPreferenceId) {
					self.processTablePrefId = attr.sdaPreferenceId;
				}

				if (attr.sdaPreferenceName) {
					self.processTablePrefName = attr.sdaPreferenceName;
				}

				if (attr.sdaInitialSelection) {
					self.initialSelection = attr.sdaInitialSelection;
				}
				if (attr.sdaInitialSort) {
					var sortGetter = $parse(attr.sdaInitialSort);
					self.initialSort = sortGetter(scopeToUse);
				}

				if (attr.sdaPageSize) {
					self.sdaPageSize = attr.sdaPageSize;
				}

				if (attr.sdaExportName) {
					self.exportFileName = attr.sdaExportName;
				}

				if (attr.sdaVisibleColumns) {
					var visibleColumnGetter = $parse(attr.sdaVisibleColumns);
					self.visbleColumns = visibleColumnGetter(scopeToUse);
				}

			};

			/**
			 *
			 */
			ProcessTableCompiler.prototype.isColumnVisible = function(columnName) {
				var found = $filter('filter')(self.visbleColumns, columnName);
				if (found && found.length === 1) {
					return true;
				}
				return false;
			};

			/**
			 *
			 */
			ProcessTableCompiler.prototype.refresh = function() {
				this.dataTable.refresh(true);
			};

			/**
			 *
			 */
			ProcessTableCompiler.prototype.showNotificationAndRefresh = function(notifications){
				this.notification = notifications;
				this.showNotificationDialog = true;
				if(!sdUtilService.isEmpty(this.notification.result)){
					this.dataTable.refresh(true);
				}

			}

			/**
			 *
			 */
			ProcessTableCompiler.prototype.fetchPage = function(options) {
				var deferred = $q.defer();
				var self = this;

				var query = angular.extend({}, self.query);
				options.descriptorColumns = self.descritorCols;
				query.options = options;
				if (angular.isDefined(self.sdDataCtrl)) {
					trace.debug("sdData is defined fetching custom data. ");

					var dataResult = self.sdDataCtrl.retrieveData(query);

					dataResult.then(function(data) {
						self.processList = data;
						deferred.resolve(self.processList);
						self.safeApply(self.processList.list);
						self.storePriorities(self.processList.list);
					}, function(error) {
						deferred.reject(error);
					});
				} else {
					trace.debug("sdData not defined fetching default data. ");

					sdProcessInstanceService.getProcesslist(query).then(function(data) {
						self.processList.list = data.list;
						self.processList.totalCount = data.totalCount;

						deferred.resolve(self.processList);

						self.safeApply();
					}, function(error) {
						deferred.reject(error);
					});
				}

				return deferred.promise;
			};

			/**
			 *
			 */
			ProcessTableCompiler.prototype.fetchDescriptorCols = function() {
				var self = this;
				sdProcessDefinitionService.getDescriptorColumns().then(function(descriptors) {
					self.descritorCols = [];
					angular.forEach(descriptors, function(descriptor) {
						self.descritorCols.push({
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
						self.ready = true;
					}

					self.safeApply();
				});
			};

			/**
			 *
			 */
			ProcessTableCompiler.prototype.fetchAvailableStates = function() {
				var self = this;
				sdStatusService.getAllProcessStates().then(function(value) {
					self.availableStates = value;
				});
			};

			/**
			 *
			 */
			ProcessTableCompiler.prototype.getDescriptorExportText = function(descriptors) {
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
			ProcessTableCompiler.prototype.getDescriptorValueForExport = function(descriptorData) {
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
			ProcessTableCompiler.prototype.fetchAvailablePriorities = function() {
				var self = this;
				sdPriorityService.getAllPriorities().then(function(data) {
					self.availablePriorities = data;
				});
			};

			/*
			 *
			 */
			ProcessTableCompiler.prototype.isPriorityChangedForRow = function(id) {
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
			ProcessTableCompiler.prototype.registerNewPriority = function(oid, value) {
				var self = this;
				if (self.originalPriorities[oid] != value) {
					self.changedPriorities[oid] = value;
				} else {
					if (angular.isDefined(self.changedPriorities[oid])) {
						delete self.changedPriorities[oid];
					}
				}
			};

			/*
			 *
			 */
			ProcessTableCompiler.prototype.storePriorities = function(data) {
				var self = this;
				if (self.priorityEditable) {
					self.originalPriorities = {};
					self.changedPriorities = {};
					angular.forEach(data, function(row) {
						self.originalPriorities[row.oid] = row.priority.value;
					});
				}
			};

			/*
			 *
			 */
			ProcessTableCompiler.prototype.savePriorityChanges = function() {
				var self = this;
				// Process instance oid to Process map for result
				var processMap = {};
				// process oid to priority map to send to service
				var requestData = {};

				angular.forEach(self.processList.list, function(rowData) {
					if (angular.isDefined(self.changedPriorities[rowData.oid])) {
						processMap[rowData.oid] = rowData.processName + ' (#' + rowData.oid + ')';
						requestData[rowData.oid] = self.changedPriorities[rowData.oid];
					}

				});

				sdPriorityService.savePriorityChanges(requestData).then(

				function(successResult) {
					angular.forEach(successResult.success, function(data) {
						data['item'] = processMap[data.OID];
					});
					angular.forEach(successResult.failure, function(data) {
						data['item'] = processMap[data.OID];
					});
					self.updatePriorityNotification.visible = true;
					self.updatePriorityNotification.result = successResult;
					self.refresh();
					sdViewUtilService.syncLaunchPanels();

				}, function(failureResult) {
					trace.error("Error occured in updating the priorities : ", failureResult);
				});
			};

			/*
			 *
			 */
			ProcessTableCompiler.prototype.isPriorityChanged = function() {
				for (name in this.changedPriorities) {
					return true;
				}
				return false;
			};

			/*
			 *
			 */
			ProcessTableCompiler.prototype.openNotes = function(rowItem) {
				sdCommonViewUtilService.openNotesViewHTML5(rowItem.oid, rowItem.processName, true);
			};

			/*
			 *
			 */
			ProcessTableCompiler.prototype.openProcessHistory = function(rowItem) {
				if (rowItem.caseInstance) {
					sdCommonViewUtilService.openCaseDetailsView(rowItem.oid,true);
				}else{
					sdCommonViewUtilService.openProcessInstanceDetailsView(rowItem.oid,true);
				}
			};

			/**
			 *
			 */
			ProcessTableCompiler.prototype.openChart = function(rowItem) {
				sdCommonViewUtilService.openGanttChartView(rowItem.oid,true);
			};

			/*
			 *
			 */
			ProcessTableCompiler.prototype.recoverProcess = function(scope, value) {
				var processes = [];
				if (Array.isArray(value)) {
					var selectedItems = value;
					if (selectedItems.length < 1) {
						trace.log("No Rows selected");
						return;
					}

					angular.forEach(selectedItems, function(item) {
						processes.push(item.oid);
					});
				} else {
					var item = value;
					processes.push(item.oid);
				}

				sdProcessInstanceService.recoverProcesses(processes).then(
						function(result) {
							var options = {};
							var message = result.message;
							if (result.success == 'true') {
								options.title = sgI18nService.translate('portal-common-messages.common-info', 'Information');
								sdDialogService.info(scope, message, options);
							} else {
								options.title = sgI18nService.translate('portal-common-messages.common-error', 'ERROR');
								sdDialogService.error(scope, message, options);
							}

						},
						function() {
							var options = { title : sgI18nService.translate('portal-common-messages.common-error', 'ERROR') };
							sdDialogService.error(scope, sgI18nService.translate(
									'portal-common-messages.internalServerError', 'Error Occurred'), options );
						});
			};

			/*
			 *
			 */
			self.onAbortPopoverConfirm = function() {
				this.refresh();
			};

			/*
			 *
			 */
			ProcessTableCompiler.prototype.openAbortDialog = function(value) {
				this.processesToAbort = [];

				if (Array.isArray(value)) {
					var selectedItems = value;
					if (selectedItems.length < 1) {
						trace.log("No Rows selected");
						return;
					}

					angular.forEach(selectedItems, function(item) {
						self.processesToAbort.push(item.oid);
					});
				} else {
					var item = value;
					this.processesToAbort.push(item.oid);
				}

				this.showAbortProcessDialog = true;
			}

			/*
			 *
			 */
			self.abortCompleted = function() {
				self.refresh();
				sdViewUtilService.syncLaunchPanels();
				this.processesToAbort = [];
			};

			/*
			 *
			 */
			self.joinCompleted = function(result) {
				self.refresh();
				sdViewUtilService.syncLaunchPanels();
				if (angular.isDefined(result)) {
					sdCommonViewUtilService.openProcessInstanceDetailsView(result,true);
				}
			};

			/*
			 *
			 */
			ProcessTableCompiler.prototype.openJoinDialog = function() {
				this.showJoinProcessDialog = true;
				this.abortProcessPopover.visible = false;
			}

			/*
			 *
			 */
			self.switchCompleted = function(result) {
				self.refresh();
				sdViewUtilService.syncLaunchPanels();
				if (angular.isDefined(result)) {
					var viewKey = new Date().getTime();
					var params = {
							name : sgI18nService.translate('views-common-messages.views-switchProcessDialog-worklist-title', 'Abort and Join Process'),
							pInstanceOids : result.join(","),
							type : "processInstances"
					};
					sdViewUtilService.openView('worklistPanel', 'id='+viewKey, params, true);
				}
			};

			/*
			 *
			 */
			ProcessTableCompiler.prototype.openSwitchDialog = function(pauseParentProcess) {
				this.showSwitchProcessDialog = true;
				this.abortProcessPopover.visible = false;
				this.pauseParentProcess = pauseParentProcess;
			}

			/*
			 *
			 */
			ProcessTableCompiler.prototype.openAttachToCaseDialog = function(value) {
				var self = this;
				this.processesToAttachCase = [];

				if (Array.isArray(value)) {
					var selectedItems = value;
					if (selectedItems.length < 1) {
						trace.log("No Rows selected");
						return;
					}

					self.processesToAttachCase = selectedItems;
				} else {
					var item = value;
					self.processesToAttachCase.push(item);
				}
				self.caseMenuPopover.visible = false
				self.showAttachToCaseDialog = true;
			}

			/*
			 *
			 */
			self.attachToCaseCompleted = function(success, result) {
				if (success) {
					self.refresh();
					sdViewUtilService.syncLaunchPanels();
					if (angular.isDefined(result)) {
						sdViewUtilService.openView('caseDetailsView', 'processInstanceOID=' + result, {
							'oid' : '' + result,
							'processInstanceOID' : '' + result
						}, true);
					}
				}
			};

			/*
			 *
			 */
			ProcessTableCompiler.prototype.openCreateCaseDialog = function(value) {
				var self = this;
				self.processesToCreateCase = [];

				if (Array.isArray(value)) {
					var selectedItems = value;
					if (selectedItems.length < 1) {
						trace.log("No Rows selected");
						return;
					}

					self.processesToCreateCase = selectedItems;
				} else {
					var item = value;
					self.processesToCreateCase.push(item);
				}

				this.caseMenuPopover.visible = false;
				self.showCreateCaseDialog = true;
			}

			/*
			 *
			 */
			self.createCaseCompleted = function(caseOid, openCaseDetail) {
				self.refresh();
				sdViewUtilService.syncLaunchPanels();
				if (openCaseDetail && angular.isDefined(caseOid)) {
					sdViewUtilService.openView('caseDetailsView', 'processInstanceOID=' + caseOid, {
						'oid' : '' + caseOid,
						'processInstanceOID' : '' + caseOid
					}, true);
				}
			};

			/**
			 *
			 */
			ProcessTableCompiler.prototype.openAbortPopover = function(rowItem) {
				var self = this;
				if (angular.isDefined(rowItem)) {
					self.processesToAbort = [ rowItem ];
				} else {
					var selectedItems = self.dataTable.getSelection();
					if (selectedItems.length > 0) {
						self.processesToAbort = selectedItems;
					}
				}
				self.processDocumentsPopover.visible = true;
			}
			
			// this method is just added for as popover required it.
			ProcessTableCompiler.prototype.openCasePopover = function(){
				return true;
			}

			// Also added to force a digest so that our popover opens correctly
			// otherwise we can get caught with an empty popover that is waiting
			// for a digest to display its template.
			ProcessTableCompiler.prototype.openActionsPopover = function(){
				return true;
			}
			
			/*
			 *
			 */
			ProcessTableCompiler.prototype.fetchAllProcesses = function() {
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

			self.initialize(attr, scope);
			/*
			 * Defined here as access required to scope
			 */
			if (angular.isDefined(ctrl)) {
				self.sdDataCtrl = ctrl;
			}
			// Expose controller as a whole on to scope
			scope.processTableCtrl = self;
		}

		/**
		 * TODO - check if duplication in ActivityTableCompiler can be avoided
		 */
		ProcessTableCompiler.prototype.openProcessDocumentsPopover = function(rowItem, $event) {
			var self = this;

			self.processPopover = {
					data : rowItem
			}

			rowItem.contentLoaded = false;
			sdProcessInstanceService.getProcessInstanceDocuments(rowItem.oid).then(function (dataPathValues) {
				rowItem.supportsProcessAttachments = rowItem.supportsProcessAttachments;
				rowItem.specificDocuments = [];
				jQuery.each(dataPathValues, function(_, dataPathValue) {
					if (dataPathValue.dataPath.id === 'PROCESS_ATTACHMENTS') {
						rowItem.processAttachments = dataPathValue;
					} else {
						rowItem.specificDocuments.push(dataPathValue);
					}
				});

				rowItem.contentLoaded = true;
				self.processPopover.data = rowItem;
				self.processDocumentsPopover.visible = true;

			});
		};

		/**
		 * TODO - check if duplication in ActivityTableCompiler can be avoided
		 */
		ProcessTableCompiler.prototype.openDocumentsView = function(docId) {
		   sdCommonViewUtilService.openDocumentView(docId);
		   this.processDocumentsPopover.visible = false;
		};

		/**
		 * TODO - check if duplication in ActivityTableCompiler can be avoided
		 */
		ProcessTableCompiler.prototype.openAllProcessDocumentViews = function(rowItem) {
			var self = this;
			this.processDocumentsPopover.visible = false;
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
		};

		return directiveDefObject;
	}
})();
