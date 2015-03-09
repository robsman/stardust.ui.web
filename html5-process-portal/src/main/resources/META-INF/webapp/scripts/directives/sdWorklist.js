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
 */

(function(){
	'use strict';

	angular.module('bpm-common').directive('sdWorklist',
			['$parse', '$q', 'sdUtilService', 'sdViewUtilService', 'sdLoggerService', 'sdPreferenceService', 'sdWorklistService',
			 'sdActivityInstanceService', 'sdProcessDefinitionService', 'sdCriticalityService', 'sdStatusService','$filter','sgI18nService', 'sdViewUtilService', WorklistDirective]);

	/*
	 *
	 */
	function WorklistDirective($parse, $q, sdUtilService, sdViewUtilService, sdLoggerService, sdPreferenceService, sdWorklistService,
			sdActivityInstanceService, sdProcessDefinitionService, sdCriticalityService, sdStatusService, $filter, sgI18nService) {

		var trace = sdLoggerService.getLogger('bpm-common.sdWorklist');

		var directiveDefObject = {
			restrict : 'AE',
			scope: true, // Creates a new sub scope
			templateUrl: 'plugins/html5-process-portal/scripts/directives/partials/worklist.html',
			compile: function(elem, attr, transclude) {
				processRawMarkup(elem, attr);

				return {
					post: function(scope, element, attr, ctrl) {
						var worklistCompiler = new WorklistCompiler(scope, element, attr, ctrl);
					}
				};
			}
		};

		/*
		 *
		 */
		function processRawMarkup(elem, attr) {
			// Process Trivial Data Column
			var showTrivialDataColumn = true; // Default
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
		}

		/*
		 *
		 */
		function WorklistCompiler(scope, element, attr, ctrl) {
			var self = this;

			this.initialize(attr, scope, $filter);

			/*
			 * This needs to be defined here as it requires access to scope
			 */
			WorklistCompiler.prototype.safeApply = function() {
				sdUtilService.safeApply(scope);
			};

			// Expose controller as a whole on to scope
			scope.worklistCtrl = this;
			sdUtilService.addFunctionProxies(scope.worklistCtrl);
		}

		/*
		 *
		 */
		WorklistCompiler.prototype.initialize = function(attr, scope, $filter) {
			var scopeToUse = scope.$parent;
			var self = this;

			// Define data
			this.worklist = {};
			this.dataTable = null; // Handle to data table instance, to be set later

			//Abort Activity Data
			this.showAbortActivityDialog = false;
			this.activitiesToAbort = [];

			//All processes with activities
			this.allAccessibleProcesses = [];
			this.allAvailableCriticalities = [];
			this.availableStates = [];

			// Process Query
			if (!attr.sdaQuery) {
				throw 'Query attribute is not specified for worklist.';
			}
			var queryGetter = $parse(attr.sdaQuery);
			var query = queryGetter(scopeToUse);
			if (query == undefined) {
				throw 'Query evaluated to "nothing" for worklist.';
			}
			this.query = query;

			// Process Title
			var titleExpr = "";
			if (attr.sdaTitle) {
				titleExpr = attr.sdaTitle;
			}
			var titleGetter = $parse(titleExpr);
			this.title = titleGetter(scopeToUse);

			// Process TableHandle and then set data table instance
			this.tableHandleExpr = 'worklistCtrl.dataTable';

			var unregister = scope.$watch(this.tableHandleExpr, function(newVal, oldVal) {
				if (newVal != undefined && newVal != null && newVal != oldVal) {
					if (attr.sdWorklist) {
						var assignable = $parse(attr.sdWorklist).assign;
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

			if (attr.sdaSelection) {
				var assignable = $parse(attr.sdaSelection).assign;
				if (assignable) {
					this.selection = null;
					this.selectionExpr = 'worklistCtrl.selection';

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

			if (attr.sdaPageSize) {
				this.sdaPageSize = attr.sdaPageSize;
			}

			this.columnSelector = 'admin'; //TODO

			this.exportFileName = this.query.userId || this.query.participantQId; //TODO

			this.worklistPrefModule = 'ipp-workflow-perspective';
			this.worklistPrefId = 'worklist-participant-columns' || 'worklist-process-columns'; //TODO
			this.worklistPrefName = this.query.userId || this.query.participantQId; //TODO

			this.preferenceDelegate = function(prefInfo) {
				var preferenceStore = sdPreferenceService.getStore(prefInfo.scope, self.worklistPrefModule, self.worklistPrefId);

				// Override
				preferenceStore.marshalName = function(scope) {
					if (scope == 'PARTITION') {
						return 'Default';
					}
					return self.worklistPrefName;
				}

				return preferenceStore;
			}
			
			
			this.dirtyDataForms =[];

			/**
			 * 
			 */
			this.changeFormStatus = function(rowId) {
			   var self = this;

			   if (this.dirtyDataForms.indexOf(rowId) == -1) {
			      this.dirtyDataForms.push(rowId);
			   }

			   //Auto select dirty rows
			   var selectedRows = self.dataTable.getSelection();
			   var matchArray   = $filter('filter')(selectedRows, { oid : rowId }, true);
			   var isRowSelected = matchArray.length > 0;

			   if(!isRowSelected){
			      var rows = $filter('filter')(self.worklist.list, {
			         oid : rowId
			      }, true);

			      if (rows && rows.length === 1) {
			         selectedRows.push({oid:rows[0].oid});
			      }

			      self.dataTable.setSelection(selectedRows);
			   }
			}

			
			this.fetchDescriptorCols();
			this.fetchAllProcesses();
			this.fetchAllAvailableCriticalities();
			this.fetchAvailableStates();
		};

		/*
		 *
		 */
		WorklistCompiler.prototype.refresh = function() {
			this.dataTable.refresh(true);
		};
		
		/*
      *
      */
     WorklistCompiler.prototype.cleanLocals = function() {
         this.dirtyDataForms = [];
     };

		/*
		 *
		 */
		WorklistCompiler.prototype.fetchPage = function(options) {
			var self = this;
			var deferred = $q.defer();
			self.cleanLocals();

			var query = angular.extend({}, this.query);
			query.options = options;

			sdWorklistService.getWorklist(query).then(function(data) {
				self.worklist.list = data.list;
				self.worklist.totalCount = data.totalCount;

				var oids = [];
				angular.forEach(self.worklist.list, function(workItem, index){
					if (workItem.trivial == undefined || workItem.trivial) {
						oids.push(workItem.oid);
					}
				});

				sdActivityInstanceService.getTrivialManualActivitiesDetails(oids).then(function(data) {
					self.worklist.trivialManualActivities = data;

					deferred.resolve(self.worklist);

					self.safeApply();
				}, function(error) {
					deferred.reject(error);
				});
			}, function(error) {
				deferred.reject(error);
			});

			return deferred.promise;
		};

		/*
		 *
		 */
		WorklistCompiler.prototype.onSelect = function(info) {
			// NOP
		};

		/*
		 *
		 */
		WorklistCompiler.prototype.onPagination = function(info) {
			// NOP
		};

		/*
		 *
		 */
		WorklistCompiler.prototype.onColumnReorder = function(info) {
			// NOP
		};

		/*
		 *
		 */
		WorklistCompiler.prototype.onSorting = function(info) {
			// NOP
		};

		/*
		 *
		 */
		WorklistCompiler.prototype.fetchDescriptorCols = function() {
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
						filterable : descriptor.filterable
					});
				});

				self.ready = true;
				self.safeApply();
			});
		};

		/*
		 *
		 */
		WorklistCompiler.prototype.fetchAllProcesses = function() {
			var self = this;

			sdProcessDefinitionService.getAllProcesses(false).then(function(processes) {
				self.allAccessibleProcesses = processes;
			});
		};

		/*
		 *
		 */
      WorklistCompiler.prototype.fetchAllAvailableCriticalities = function()
      {
         var self = this;
         sdCriticalityService.getAllCriticalities().then(function(criticalities)
         {
            self.allAvailableCriticalities = criticalities;
         });
      };

      /*
       *
       */

      WorklistCompiler.prototype.fetchAvailableStates = function()
      {
         var self = this;
         sdStatusService.getAllActivityStates().then(function(value)
         {
            self.availableStates = value;
         });
      };



		/*
       *
       */
		WorklistCompiler.prototype.activateWorkItem = function(workItem) {
			sdViewUtilService.openView("activityPanel", "OID=" + workItem.oid, {"oid" : "" + workItem.oid});
		};

		/*
		 *
		 */
		WorklistCompiler.prototype.openNotes = function(workItem) {
			sdViewUtilService.openView("notesPanel", "oid=" + workItem.processInstance.oid,
					{"oid": "" + workItem.processInstance.oid}, true);
		};

		/*
		 *
		 */
		WorklistCompiler.prototype.openProcessHistory = function(workItem) {
			sdViewUtilService.openView("processInstanceDetailsView",
					"processInstanceOID=" + workItem.processInstance.oid,
					{
						"oid": "" + workItem.oid,
						"processInstanceOID": "" + workItem.processInstance.oid
					}, true
			);
		};


		/**
		 *
		 */
		 WorklistCompiler.prototype.containsAllTrivialManualActivities = function() {
	         var self = this;
	         var selectedWorkItems = [];

	         var dataTable = self.dataTable;

	         if(dataTable != null){
	         	selectedWorkItems = dataTable.getSelection();
	         }

	         if(selectedWorkItems.length < 1){
	            return false;
	         }
	         var activitiesData = [];
	         angular.forEach(selectedWorkItems,function(workItem){
	            var trivialActivityInfo = self.worklist.trivialManualActivities[workItem.oid];
	            if(trivialActivityInfo){
	               activitiesData.push(workItem.oid);
	            }
	         });

	         if(selectedWorkItems.length == activitiesData.length){
	            return true;
	         }
	         return false;
	      };

      /**
       * 
       */
      WorklistCompiler.prototype.isSelectionHomogenous = function(rows) {

         var firstItem = rows[0];

         var matchArray = [];

         angular.forEach(rows, function(row) {
            if (row.activity.qualifiedId === firstItem.activity.qualifiedId && row.modelOID === firstItem.modelOID) {
               matchArray.push(row);
            }
         });
         
         if(matchArray.length ===rows.length ){
            return true;
         }
         
         return false;
      };
      
      /**
       * 
       */
      WorklistCompiler.prototype.isSelectionDirty = function(activities) { 
         var self = this;
         var activitiesWithDirtyForms =[]; 
         angular.forEach(activities,function(activity){
    
            if(self.dirtyDataForms.indexOf(activity.oid) > -1){
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
	    WorklistCompiler.prototype.completeAll = function(res) {
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
	         var selectedWorkItems = self.selectedActivity;
            
	         promise.then(function() {
	            
	            angular.forEach(selectedWorkItems,function(item){
	               self.completeActivityResult.nameIdMap[item.oid] = item.activity.name;
	            });
	            
	            if (selectedWorkItems.length > 0) {
	               var activitiesData = [];
	               
	               if(self.completeDialog.confirmationType === 'dataMapping') {
	                  //When data fields are filled in a dialog
	                  angular.forEach(selectedWorkItems, function(workItem, index) {
	                     var outData = self.completeDialog.outData;
	                     var dataMappings = {};
	                     angular.forEach( self.completeDialog.dataMappings,function(mapping){
	                        dataMappings[mapping.id] = mapping.typeName; 
	                     });
	                     activitiesData.push({
	                        oid : workItem.oid,
	                        outData : outData,
	                        dataMappings : dataMappings
	                     });
	                  });

	               }else {
	                  //When data fields are filled inline in worklist
	                  angular.forEach(selectedWorkItems, function(workItem, index) {
	                     var trivialActivityInfo = self.worklist.trivialManualActivities[workItem.oid];
	                     if(trivialActivityInfo) {
	                        var outData = trivialActivityInfo.inOutData;
	                        var dataMappings = {};
	                        angular.forEach( trivialActivityInfo.dataMappings,function(mapping){
	                           dataMappings[mapping.id] = mapping.typeName; 
	                        });
	         
	                        activitiesData.push({oid: workItem.oid, outData: outData , dataMappings : dataMappings});
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
	       * @param workItem
	       */
      WorklistCompiler.prototype.openCompleteDialog = function( workItem) {
    	  
         var self = this;
         var CONFIRMATION_TYPE_SINGLE=  'single'
         var CONFIRMATION_TYPE_GENERIC=  'generic'
         var CONFIRMATION_TYPE_DATAMAPPING= 'dataMapping'
         
         self.selectedActivity = [];
         self.completeDialog = {
            confirmationType : CONFIRMATION_TYPE_SINGLE,  //single / generic / dataMapping
            dataMappings : {},
            outData : {}
         }
         self.showCompleteDialog = true;

         if (angular.isDefined( workItem)) {

            self.selectedActivity = [ workItem ];
            self.completeDialog.confirmationType = CONFIRMATION_TYPE_SINGLE;
         }
         else {
            var selectedWorkItems = this.dataTable.getSelection();
            if ( selectedWorkItems.length > 0) {
               // Add rows having dirty field to selected activity
               this.selectedActivity = selectedWorkItems;

               if ( this.isSelectionHomogenous( selectedWorkItems) && !this.isSelectionDirty( selectedWorkItems) ) {
                 
            	  self.completeDialog.confirmationType = CONFIRMATION_TYPE_DATAMAPPING;
                  var firstItem = selectedWorkItems[0];
                  self.completeDialog.dataMappings = angular
                           .copy(self.worklist.trivialManualActivities[firstItem.oid].dataMappings);
                  self.completeDialog.outData =angular
                  .copy(self.worklist.trivialManualActivities[firstItem.oid].inOutData);
               } else {
            	   
                  self.completeDialog.confirmationType = CONFIRMATION_TYPE_GENERIC;
               }
            }
         }
      };

		/*
		 *
		 */
		WorklistCompiler.prototype.openDelegateDialog = function(workItem) {
			this.showDelegateDialog = true;
			if (angular.isDefined(workItem)) {
				this.selectedActivity = [workItem];
			} else {
				var selectedWorkItems = this.dataTable.getSelection();
				if (selectedWorkItems.length > 0) {
					// TODO
					this.selectedActivity = selectedWorkItems;
				}
			}
		};

		/*
		 *
		 */
		WorklistCompiler.prototype.onDelegateConfirm = function() {
			this.refresh();
		};

		/*
		 *
		 */
		WorklistCompiler.prototype.onAbortPopoverConfirm = function() {
			this.refresh();
		};

		/*
		 *
		 */
		WorklistCompiler.prototype.openAbortDialog = function(value) {
			var self = this;
			this.activitiesToAbort = [];

			if (Array.isArray(value)) {
				var selectedWorkItems = value;
				if (selectedWorkItems.length < 1) {
					trace.log("No Rows selected");
					return;
				}

				angular.forEach(selectedWorkItems, function(workItem) {
					self.activitiesToAbort.push(workItem.oid);
				});
			} else {
				var workItem = value;
				this.activitiesToAbort.push(workItem.oid);
			}

			this.showAbortActivityDialog = true;
		}

		/*
		 *
		 */
		 WorklistCompiler.prototype.abortCompleted = function(workItem) {
			this.refresh();
			sdViewUtilService.syncLaunchPanels();
			this.activitiesToAbort = [];
		};
		
		/*
      *
      */
     WorklistCompiler.prototype.getDescriptorExportText = function(descriptors) {
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
     WorklistCompiler.prototype.getDescriptorValueForExport = function( descriptorData ) {
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
    
		return directiveDefObject;
	}
})();
