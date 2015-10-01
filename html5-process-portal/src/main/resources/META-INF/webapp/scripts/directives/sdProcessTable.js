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
					'sdStatusService', 'sdPriorityService', 'sdDialogService', 'sdCommonViewUtilService',
					ProcessTableDirective ]);

	/*
	 *
	 */
	function ProcessTableDirective($parse, $q, $timeout, $filter, sdUtilService, sdViewUtilService, sdLoggerService,
			sgI18nService, sdPreferenceService, sdProcessInstanceService, sdProcessDefinitionService, sdStatusService,
			sdPriorityService, sdDialogService, sdCommonViewUtilService) {

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
				var scopeToUse = scope.$parent;

				// Define data
				self.processList = {};
				self.dataTable = null; // Handle to data table instance, to be set later

				//Abort Data
				self.showAbortProcessDialog = false;
				self.processesToAbort = [];
				self.processesToJoin = [];
				self.allAccessibleProcesses = null;
				self.availableStates = [];

				// Set Default values
				self.visbleColumns = DEFAULT_VALUES.VISIBLE_COLUMNS;
				self.processTablePrefModule = DEFAULT_VALUES.PREFERENCE_MODULE;
				self.exportFileName = "Processes";
				self.columnSelector = 'admin'; //TODO
				self.initialSort = {
					name : 'startTime',
					dir : 'desc'
				};

				// Set custom values
				self.customizeWithAttributeValues(attr, scope, scopeToUse);
				self.documentPopoverHandle = null;
				// Process TableHandle and then set data table instance
				self.tableHandleExpr = 'processTableCtrl.dataTable';

				// Priority
				self.priorityEditable = true;
				self.originalPriorities = {};
				self.changedPriorities = {};
				self.updatePriorityNotification = {
					error : false,
					result : {}
				};

				
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
						self.selection = null;
						self.selectionExpr = 'processTableCtrl.selection';

						// Update parent for change in sdDataTable
						scope.$watch(self.selectionExpr, function(newVal, oldVal) {
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

				self.descritorCols = [];

				self.fetchDescriptorCols();
				self.fetchAvailableStates();
				self.fetchAvailablePriorities();
			};

			/**
			 * 
			 */
			ProcessTableCompiler.prototype.customizeWithAttributeValues = function(attr, scope, scopeToUse) {
				// Process Title
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
			self.isColumnVisible = function(columnName) {
				var found = $filter('filter')(self.visbleColumns, columnName);
				if (found && found.length === 1) {
					return true;
				}
				return false;
			};

			self.refresh = function() {
				self.dataTable.refresh(true);
			};

			self.preferenceDelegate = function(prefInfo) {
				var preferenceStore = sdPreferenceService.getStore(prefInfo.scope, self.processTablePrefModule,
						self.processTablePrefId);

				// Override
				preferenceStore.marshalName = function(scope) {
					if (scope == 'PARTITION') {
						return 'Default';
					}
					return self.processTablePrefName;
				}

				return preferenceStore;
			}

			self.fetchPage = function(options) {
				var deferred = $q.defer();

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

			self.fetchDescriptorCols = function() {
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

			self.fetchAvailableStates = function() {
				sdStatusService.getAllProcessStates().then(function(value) {
					self.availableStates = value;
				});
			};

			self.getDescriptorExportText = function(descriptors) {
				var descriptorsToExport = [];

				angular.forEach(descriptors, function(descriptor) {
					if (!descriptor.isDocument)
						descriptorsToExport.push(descriptor.key + " : " + descriptor.value);
				});
				return descriptorsToExport.join(',');
			};

			self.getDescriptorValueForExport = function(descriptorData) {
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
			self.fetchAvailablePriorities = function() {
				sdPriorityService.getAllPriorities().then(function(data) {
					self.availablePriorities = data;
				});
			};
			
			/*
			 * 
			 */
			self.isPriorityChangedForRow = function(id) {
			    for (name in self.changedPriorities) {
				if (name == id) {
				    return true;
				}
			    }
			    return false;
			};

			/*
			 *
			 */
			self.registerNewPriority = function(oid, value) {
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
			self.storePriorities = function(data) {
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
			self.savePriorityChanges = function() {

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
			self.isPriorityChanged = function() {
				for (name in self.changedPriorities) {
					return true;
				}
				return false;
			};

			/*
			 *
			 */
			self.openNotes = function(rowItem) {
				sdCommonViewUtilService.openNotesView(rowItem.oid, true);
			};

			/*
			 *
			 */
			self.openProcessHistory = function(rowItem) {
				if (rowItem.caseInstance) {
					sdCommonViewUtilService.openCaseDetailsView(rowItem.oid,true);
				}else{
					sdCommonViewUtilService.openProcessInstanceDetailsView(rowItem.oid,true);
				}
			};

			self.openChart = function(rowItem) {
				sdCommonViewUtilService.openGanttChartView(rowItem.oid,true);
			};
			
			/*
			 *
			 */
			self.recoverProcess = function(scope, value) {
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
							} else {
								options.title = sgI18nService.translate('portal-common-messages.common-error', 'ERROR');
							}
							
							sdDialogService.alert(scope, message, options);
						},
						function() {
							var options = { title : sgI18nService.translate('portal-common-messages.common-error', 'ERROR') };
							sdDialogService.alert(scope, sgI18nService.translate(
									'portal-common-messages.internalServerError', 'Error Occurred'), options );
						});
			};

			/*
			 * 
			 */
			self.onAbortPopoverConfirm = function() {
				self.refresh();
			};

			/*
			 *
			 */
			self.openAbortDialog = function(value) {
				self.processesToAbort = [];

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
					self.processesToAbort.push(item.oid);
				}

				self.showAbortProcessDialog = true;
			}

			/*
			 *
			 */
			self.abortCompleted = function() {
				self.refresh();
				sdViewUtilService.syncLaunchPanels();
				self.processesToAbort = [];
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
			self.openJoinDialog = function() {
				self.showJoinProcessDialog = true;
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
							pInstanceOids : result.join(",")
					};
					sdViewUtilService.openView('worklistPanel', 'id='+viewKey, params, true);
				}
			};

			/*
			 *
			 */
			self.openSwitchDialog = function() {
				self.showSwitchProcessDialog = true;
			}

			/*
			 *
			 */
			self.openAttachToCaseDialog = function(value) {
				self.processesToAttachCase = [];

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
			self.openCreateCaseDialog = function(value) {
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

			self.openAbortPopover = function(event, rowItem) {
				if (angular.isDefined(rowItem)) {
					self.processesToAbort = [ rowItem ];
				} else {
					var selectedItems = self.dataTable.getSelection();
					if (selectedItems.length > 0) {
						self.processesToAbort = selectedItems;
					}
				}
				self.popoverDirective.show(event);
			}

			/*
			 *
			 */
			self.fetchAllProcesses = function() {
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
				self.documentPopoverHandle.show($event);
			});
		};
		
		/**
		 * TODO - check if duplication in ActivityTableCompiler can be avoided
		 */
		ProcessTableCompiler.prototype.openDocumentsView = function(docId) {
		   sdCommonViewUtilService.openDocumentView(docId);
		};

		/**
		 * TODO - check if duplication in ActivityTableCompiler can be avoided
		 */
		ProcessTableCompiler.prototype.openAllProcessDocumentViews = function(rowItem) {
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
		};

		return directiveDefObject;
	}
})();
