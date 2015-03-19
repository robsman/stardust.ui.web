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
			 'sdProcessInstanceService', 'sdProcessDefinitionService', 'sdStatusService', ProcessTableDirective]);

	/*
	 *
	 */
	function ProcessTableDirective($parse, $q, sdUtilService, sdViewUtilService, sdLoggerService, sgI18nService, sdPreferenceService,
			sdProcessInstanceService, sdProcessDefinitionService, sdStatusService) {

		var trace = sdLoggerService.getLogger('bpm-common.sdProcessTable');

		var directiveDefObject = {
			restrict : 'AE',
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
				this.processList = {};
				this.dataTable = null; // Handle to data table instance, to be set later

				//Abort Activity Data
				this.showAbortActivityDialog = false;
				this.activitiesToAbort = [];

				//All processes with activities
//				this.allAccessibleProcesses = [];
//				this.allAvailableCriticalities = [];
				this.availableStates = [];

				// Process Query
//				if (!attr.sdaQuery) {
//					throw 'Query attribute is not specified for processTable.';
//				}
//				var queryGetter = $parse(attr.sdaQuery);
//				var query = queryGetter(scopeToUse);
//				if (query == undefined) {
//					throw 'Query evaluated to "nothing" for processTable.';
//				}
//				this.query = query;

				// Process Title
				var titleExpr = "";
				if (attr.sdaTitle) {
					titleExpr = attr.sdaTitle;
				}
				var titleGetter = $parse(titleExpr);
				this.title = titleGetter(scopeToUse);

				// Process TableHandle and then set data table instance
				this.tableHandleExpr = 'processTableCtrl.dataTable';
				
				var unregister = scope.$watch(this.tableHandleExpr, function(newVal, oldVal) {
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
					this.initialSelection = attr.sdaInitialSelection;
				}
				if (attr.sdaPageSize) {
					this.sdaPageSize = attr.sdaPageSize;
				}
				this.columnSelector = 'admin'; //TODO
//				this.exportFileName = this.query.userId || this.query.participantQId; //TODO
				
				this.processTablePrefModule = 'ipp-workflow-perspective';
				this.processTablePrefId = 'processTable-participant-columns' || 'processTable-process-columns'; //TODO
				//this.processTablePrefName = this.query.userId || this.query.participantQId; //TODO
				
				if (attr.sdaSelection) {
					var assignable = $parse(attr.sdaSelection).assign;
					if (assignable) {
						this.selection = null;
						this.selectionExpr = 'processTableCtrl.selection';

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
				
				// TODO remove
//				self.ready = true;
				
				self.descritorCols = [];
				
				this.fetchDescriptorCols();
				this.fetchAvailableStates();
			};
			
			this.preferenceDelegate = function(prefInfo) {
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
			
			this.fetchPage = function(options) {
				var deferred = $q.defer();
//				self.cleanLocals();

				var query = angular.extend({}, this.query);
				query.options = options;

				sdProcessInstanceService.getProcesslist(query).then(function(data) {
					self.processList.list = data.list;
					self.processList.totalCount = data.totalCount;

					deferred.resolve(self.processList);

					self.safeApply();
				}, function(error) {
					deferred.reject(error);
				});

				return deferred.promise;
			};
			
			this.fetchDescriptorCols = function() {
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
			
			this.fetchAvailableStates = function() {
				sdStatusService.getAllActivityStates().then(function(value) {
					self.availableStates = value;
				});
			};
			
			this.getDescriptorExportText = function(descriptors) {
		        var descriptorsToExport  = [];
		        
		        angular.forEach(descriptors,function( descriptor){
		        	if( !descriptor.isDocument )
		           descriptorsToExport.push(descriptor.key +" : "+descriptor.value);
		        });
		        return descriptorsToExport.join(',');
		     };
		     
			this.getDescriptorValueForExport = function(descriptorData) {
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
			this.openNotes = function( rowItem ) {
				sdViewUtilService.openView("notesPanel", "oid=" + rowItem.oid,
						{"oid": "" + rowItem.oid}, true);
			};

			/*
			 *
			 */
			this.openProcessHistory = function( rowItem ) {
				sdViewUtilService.openView("processInstanceDetailsView",
						"processInstanceOID=" + rowItem.oid,
						{
							"oid": "" + rowItem.oid,
							"processInstanceOID": "" + rowItem.oid
						}, true
				);
			};
			
			/*
			 *
			 */
			this.onAbortPopoverConfirm = function() {
				this.refresh();
			};

			/*
			 *
			 */
			this.openAbortDialog = function(value) {
				
				//TODO abort process
			}

			/*
			 *
			 */
			 this.abortCompleted = function( ) {
				this.refresh();
				sdViewUtilService.syncLaunchPanels();
				this.processesToAbort = [];
			};
			
			this.onSelect = function(info) {
				// NOP
			};

			/*
			 *
			 */
			this.onPagination = function(info) {
				// NOP
			};

			/*
			 *
			 */
			this.onColumnReorder = function(info) {
				// NOP
			};

			/*
			 *
			 */
			this.onSorting = function(info) {
				// NOP
			};
			
			
			this.initialize(attr, scope);
			// Expose controller as a whole on to scope
			scope.processTableCtrl = this;
		}

		return directiveDefObject;
	}
})();
