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

(function(){
	'use strict';

	angular.module('bpm-common').directive('sdProcessTable',
			['$parse', '$q', 'sdUtilService', 'sdViewUtilService', 'sdLoggerService', 'sgI18nService', 'sdPreferenceService',
			 'sdProcessInstanceService', 'sdProcessDefinitionService', 'sdStatusService', 'sdPriorityService', 'sdDialogService', ProcessTableDirective]);

	/*
	 *
	 */
	function ProcessTableDirective($parse, $q, sdUtilService, sdViewUtilService, sdLoggerService, sgI18nService, sdPreferenceService,
			sdProcessInstanceService, sdProcessDefinitionService, sdStatusService, sdPriorityService, sdDialogService) {

		var trace = sdLoggerService.getLogger('bpm-common.sdProcessTable');

		var directiveDefObject = {
			restrict : 'AE',
			require : '^?sdData',
			scope: true, // Creates a new sub scope
			templateUrl: 'plugins/html5-process-portal/scripts/directives/partials/processTable.html',
			compile: function(elem, attr, transclude) {
				return {
					post: function(scope, element, attr, ctrl) {
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

				self.availableStates = [];

				// Process Title
				var titleExpr = "";
				if (attr.sdaTitle) {
					titleExpr = attr.sdaTitle;
				}
				var titleGetter = $parse(titleExpr);
				self.title = titleGetter(scopeToUse);

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
				
				if (attr.sdaInitialSelection) {
					self.initialSelection = attr.sdaInitialSelection;
				}
				if (attr.sdaPageSize) {
					self.sdaPageSize = attr.sdaPageSize;
				}
				self.columnSelector = 'admin'; //TODO
//				self.exportFileName = self.query.userId || self.query.participantQId; //TODO
//				self.processTablePrefModule = 'ipp-workflow-perspective';
//				self.processTablePrefId = 'processTable-participant-columns' || 'processTable-process-columns'; //TODO
				
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
			
			self.refresh = function() {
				self.dataTable.refresh(true);
			};
			
			self.preferenceDelegate = function(prefInfo) {
				var preferenceStore = sdPreferenceService.getStore(prefInfo.scope, self.processTablePrefModule, self.processTablePrefId);

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
					angular.forEach(descriptors, function(descriptor){
						self.descritorCols.push({
							id: descriptor.id,
							field: "descriptorValues['" + descriptor.title + "'].value",
							title: descriptor.title,
							dataType: descriptor.type,
							sortable: descriptor.sortable,
							filterable : descriptor.filterable
						});
					});

					self.ready = true;
					self.safeApply();
				});
			};
			
			self.fetchAvailableStates = function() {
				sdStatusService.getAllProcessStates().then(function(value) {
					self.availableStates = value;
				});
			};
			
			self.getDescriptorExportText = function(descriptors) {
		        var descriptorsToExport  = [];
		        
		        angular.forEach(descriptors,function( descriptor){
		        	if( !descriptor.isDocument )
		           descriptorsToExport.push(descriptor.key +" : "+descriptor.value);
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
			self.registerNewPriority = function(oid, value) {
				if(self.originalPriorities[oid] != value){
					self.changedPriorities[oid] = value;
				}else{
					if(angular.isDefined(self.changedPriorities[oid])){
						delete self.changedPriorities[oid];
					}
				}
			};
			
			/*
			 *
			 */
			self.storePriorities = function(data) {
				if(self.priorityEditable){
					self.originalPriorities = {};
					self.changedPriorities = {};
					angular.forEach(data,function( row ) {
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
						processMap[rowData.oid] = rowData.processName + ' (#' + rowData.oid
								+ ')';
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
				for ( name in self.changedPriorities ) {
					return true;
				}
				return false;
			};
			
			/*
			 *
			 */
			self.openNotes = function( rowItem ) {
				sdViewUtilService.openView("notesPanel", "oid=" + rowItem.oid,
						{"oid": "" + rowItem.oid}, true);
			};

			/*
			 *
			 */
			self.openProcessHistory = function(rowItem) {
				var view = 'processInstanceDetailsView';
				if (rowItem.caseInstance) {
					view = 'caseDetailsView';
				}
				sdViewUtilService.openView(view, 'processInstanceOID=' + rowItem.oid, {
					'oid' : '' + rowItem.oid,
					'processInstanceOID' : '' + rowItem.oid
				}, true);
			};
			
			self.openChart = function(rowItem) {
				sdViewUtilService.openView('ganttChartView', 'processInstanceOId=' + rowItem.oid, {
					'oid' : '' + rowItem.oid,
					'processInstanceOId' : '' + rowItem.oid
				}, true);
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

					angular.forEach(selectedItems, function( item ) {
						processes.push(item.oid);
					});
				} else {
					var item = value;
					processes.push(item.oid);
				}
				
				sdProcessInstanceService.recoverProcesses(processes).then(function(result) {
					var title = '';
					var message = result.message;
					if (result.success == 'true') {
						title = sgI18nService.translate('portal-common-messages.common-info','Information');
					} else {
						title = sgI18nService.translate('portal-common-messages.common-error','ERROR');
					}
					sdDialogService.alert(scope, message, title);
				}, function() {
					sdDialogService.alert(scope, 
							sgI18nService.translate('portal-common-messages.internalServerError','Error Occurred'), 
							sgI18nService.translate('portal-common-messages.common-error','ERROR'));
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

					angular.forEach(selectedItems, function( item ) {
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
					sdViewUtilService.openView('processDefinitionView', true);
				}
			};
			
			/*
			 *
			 */
			self.openJoinDialog = function(value) {
				self.processesToJoin = [];

				if (Array.isArray(value)) {
					var selectedItems = value;
					if (selectedItems.length < 1) {
						trace.log("No Rows selected");
						return;
					}

					self.processesToJoin = selectedItems;
				} else {
					var item = value;
					self.processesToJoin.push(item);
				}

				self.showJoinProcessDialog = true;
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
			
			self.onAbortPopoverConfirm = function(type, result) {
				self.refresh();
				sdViewUtilService.syncLaunchPanels();
				
				if (angular.isDefined(type) && angular.isDefined(result)) {
					if ('abortandstart' === type) {
						// TODO open spawned processes
						
						sdViewUtilService.openView('worklistViewHtml5', true);
					} else if ('abortandjoin' === type) {
						// TODO open joined process
						
						sdViewUtilService.openView('processDefinitionView', true);
					}						
				}
			}
			
			self.openAbortPopover = function(event, rowItem) {
				if (angular.isDefined(rowItem)) {
					self.processesToAbort = [rowItem];
				} else {
					var selectedItems = self.dataTable.getSelection();
					if (selectedItems.length > 0) {
						self.processesToAbort = selectedItems;
					}
				}
				self.popoverDirective.show(event);
			}
			
			self.onSelect = function(info) {
				// NOP
			};

			/*
			 *
			 */
			self.onPagination = function(info) {
				// NOP
			};

			/*
			 *
			 */
			self.onColumnReorder = function(info) {
				// NOP
			};

			/*
			 *
			 */
			self.onSorting = function(info) {
				// NOP
			};
			
			
			self.initialize(attr, scope);
			/*
			 * Defined here as access required to scope
			 */
			if(angular.isDefined(ctrl)){
				self.sdDataCtrl = ctrl;
			}
			// Expose controller as a whole on to scope
			scope.processTableCtrl = self;
		}

		return directiveDefObject;
	}
})();
