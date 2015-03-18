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

	angular.module('bpm-common').directive('sdActivityTable',
			['$parse', '$q', 'sdUtilService', 'sdViewUtilService', 'sdLoggerService', 'sdPreferenceService', 'sdWorklistService',
			 'sdActivityInstanceService', 'sdProcessDefinitionService', 'sdCriticalityService', 'sdStatusService', 'sdPriorityService', '$filter','sgI18nService', ActivityTableDirective]);

	/*
	 *
	 */
	function ActivityTableDirective($parse, $q, sdUtilService, sdViewUtilService, sdLoggerService, sdPreferenceService, sdWorklistService,
			sdActivityInstanceService, sdProcessDefinitionService, sdCriticalityService, sdStatusService, sdPriorityService, $filter, sgI18nService) {

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

		var defaultValues = {
				mode:'worklist',
				worklist: {
					visibleColumns : ['overview', 'oid', 'criticality', 'priority', 'descriptors', 'started', 'lastModified', 'duration', 'lastPerformer', 'data'],
					preferenceModule : 'ipp-workflow-perspective'

				},
				activityInstanceView : {
					visibleColumns : ['overview', 'oid', 'criticality', 'priority', 'descriptors', 'started', 'lastModified', 'duration', 'lastPerformer', 'data']
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

			// Process Descriptor columns
			var showDescriptorCoulmns = true; // Default
			if (attr.sdaDescriptorColumn && attr.sdaDescriptorColumn === 'false') {
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
			var self = this;

			this.initialize(attr, scope, $filter);
			
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
		}

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

			this.worklistPrefModule = "";
			this.worklistPrefId ="";

			this.columnSelector = 'admin';

			// Process Query
			if (!attr.sdaQuery) {
				throw 'Query attribute has to be specified if sdData is not specified.';
			}
			var queryGetter = $parse(attr.sdaQuery);
			var query = queryGetter(scopeToUse);
			if (query == undefined) {
				throw 'Query evaluated to "nothing" for activity table.';
			}
			this.query = query;

			
			if(attr.sdaMode){
				this.mode= attr.sdaMode;
			}else{
				this.mode = defaultValues.mode;  
			}

			if (this.mode === 'worklist') {
				this.initializeWorklistMode(attr, scope);
			} else if(this.mode === 'activityInstanceView'){
				this.initializeActivityInstanceMode(attr, scope);
			}else{
				throw 'Not a valid value for sdaMode';
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

		
			this.exportFileName = this.query.userId || this.query.participantQId; 
			console.log(self.worklistPrefModule);
			console.log(self.worklistPrefId);

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
			   var matchArray   = $filter('filter')(selectedRows, { oid : rowId }, true);
			   var isRowSelected = matchArray.length > 0;

				if(!isRowSelected) {
			      var rows = $filter('filter')(self.activities.list, {
			         oid : rowId
			      }, true);

			      if (rows && rows.length === 1) {
			         selectedRows.push({oid:rows[0].oid});
			      }

			      self.dataTable.setSelection(selectedRows);
			   }
			};


			this.fetchDescriptorCols();
			this.fetchAllProcesses();
			this.fetchAllAvailableCriticalities();
			this.fetchAvailableStates();
			this.fetchAvailablePriorities();
		};


		/**
		 * 
		 */
		ActivityTableCompiler.prototype.initializeWorklistMode = function(attr, scope){
			this.priorityEditable = false;
			this.visbleColumns = defaultValues.worklist.visibleColumns;
			this.worklistPrefModule = defaultValues.worklist.preferenceModule;

		};

		/**
		 * 
		 */
		ActivityTableCompiler.prototype.initializeActivityInstanceMode = function(attr, scope){

			this.priorityEditable = true;
			this.originalPriorities = {};
			this.changedPriorities = {};
			this.updatePriorityNotification = {
					error : false,
					result : {}
			};
			this.visbleColumns = defaultValues.activityInstance.visibleColumns;
			
			if (!attr.sdaPreferenceModule) {
				throw "sdaPreferenceModule is not defined."
			}
			
			if (!attr.sdaPreferenceId) {
				throw "sdaPreferenceModule is not defined."
			}
			
			if (!attr.sdaPreferenceName) {
				throw "sdaPreferenceName is not defined."
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


			var idFromQuery = this.query.userId || this.query.participantQId;

			if(idFromQuery){
				this.worklistPrefId = 'worklist-participant-columns';
			}else{
				this.worklistPrefId = 'worklist-process-columns';
			}

			if (attr.sdaPreferenceModule) {
				this.worklistPrefModule = attr.sdaPreferenceModule;
			}

			if (attr.sdaPreferenceId) {
				this.worklistPrefId = attr.sdaPreferenceId;
			}

			this.worklistPrefName = this.query.userId || this.query.participantQId; 
			if (attr.sdaPreferenceName) {
				this.worklistPrefName = attr.sdaPreferenceName;
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

			if( angular.isDefined(this.sdDataCtrl) ) {
				trace.debug("sdData is defined fetching custom data. ");

				self.sdDataCtrl.retrieveData().then(function(data){
					self.activities = data;
					deferred.resolve(self.activities);
					self.safeApply(self.activities.list);
					self.storePriorities();
				});

			} else {
				trace.debug("sdData not defined fetching default data. ");

				var query = angular.extend({}, this.query);
				query.options = options;

				sdWorklistService.getWorklist(query).then(function(data) {
					self.activities.list = data.list;
					self.activities.totalCount = data.totalCount;
					self.storePriorities(self.activities.list);

					var oids = [];
					angular.forEach(self.activities.list, function(workItem, index){
						if (workItem.trivial == undefined || workItem.trivial) {
							oids.push(workItem.oid);
						}
					});

					sdActivityInstanceService.getTrivialManualActivitiesDetails(oids).then(function(data) {
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
		ActivityTableCompiler.prototype.fetchDescriptorCols = function() {
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
         sdCriticalityService.getAllCriticalities().then(function(criticalities)
         {
            self.allAvailableCriticalities = criticalities;
         });
      };

      /*
       *
       */

      ActivityTableCompiler.prototype.fetchAvailableStates = function()
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
			sdViewUtilService.openView("activityPanel", "OID=" + rowItem.oid, {"oid" : "" + rowItem.oid});
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
		ActivityTableCompiler.prototype.openProcessHistory = function( rowItem ) {
			sdViewUtilService.openView("processInstanceDetailsView",
					"processInstanceOID=" + rowItem.processInstance.oid,
					{
						"oid": "" + rowItem.oid,
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
	            var trivialActivityInfo = self.activities.trivialManualActivities[item.oid];
	            if(trivialActivityInfo){
	               activitiesData.push(item.oid);
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
	               self.completeActivityResult.nameIdMap[item.oid] = item.activity.name;
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
	                        oid : item.oid,
	                        outData : outData,
	                        dataMappings : dataMappings
	                     });
	                  });

	               }else {
	                  //When data fields are filled inline in the table
	                  angular.forEach(selectedItems, function(item, index) {
	                     var trivialActivityInfo = self.activities.trivialManualActivities[item.oid];
	                     if(trivialActivityInfo) {
	                        var outData = trivialActivityInfo.inOutData;
	                        var dataMappings = {};
	                        angular.forEach( trivialActivityInfo.dataMappings,function(mapping){
	                           dataMappings[mapping.id] = mapping.typeName; 
	                        });

	                        activitiesData.push({oid: item.oid, outData: outData , dataMappings : dataMappings});
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
                           .copy(self.activities.trivialManualActivities[firstItem.oid].dataMappings);
                  self.completeDialog.outData =angular
                  .copy(self.activities.trivialManualActivities[firstItem.oid].inOutData);
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
		ActivityTableCompiler.prototype.onAbortPopoverConfirm = function() {
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
					self.activitiesToAbort.push(item.oid);
				});
			} else {
				var item = value;
				this.activitiesToAbort.push(item.oid);
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
    			 self.originalPriorities[row.oid] = row.priority.value;
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
     ActivityTableCompiler.prototype.registerNewPriority = function(oid, value) {
    	 var self = this;

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
     ActivityTableCompiler.prototype.savePriorityChanges = function() {
    	 var self = this;

    	 //Process instance oid to activity map for result
    	 var processActivityMap = {};
    	 //process oid to priority map to send to service
    	 var requestData = {};


    	 angular.forEach(self.activities.list,function( rowData ){
    		 if(angular.isDefined(self.changedPriorities[ rowData.oid ])){
    			 processActivityMap[rowData.processInstance.oid] =  	rowData.activity.name +' (#'+rowData.oid+')';
    			 requestData[rowData.processInstance.oid] = self.changedPriorities[ rowData.oid ];
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
 	ActivityTableCompiler.prototype.openProcessDetails = function(oid) {
 		sdViewUtilService.openView("processInstanceDetailsView",
 				"processInstanceOID=" + oid, {
 					"processInstanceOID" : "" + oid
 				}, true);
 	};



     return directiveDefObject;
	};


})();
