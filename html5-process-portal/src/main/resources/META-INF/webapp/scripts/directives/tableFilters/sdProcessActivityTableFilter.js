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
 * @author Johnson.Quadras
 */
 (function() {
   'use strict';

   angular.module('workflow-ui').directive('sdProcessActivityTableFilter',[ '$filter', '$parse', 'sdUtilService','sdLoggerService', ActivityFilter]);

   var trace = null;
   /*
    *
    */
   function ActivityFilter( $filter, $parse, sdUtilService, sdLoggerService) {
       trace = sdLoggerService.getLogger('bpm-common.sdProcessActivityTableFilter');

       return {
         restrict : 'A',
         templateUrl : sdUtilService.getBaseUrl() + 'plugins/html5-process-portal/scripts/directives/tableFilters/processActivityTableFilter.html',
         controller : [ '$scope', '$attrs', '$filter', '$parse', FilterController ],
         link : function(scope, element, attr, ctrl) {

            /*
             */
            scope.handlers.applyFilter = function() {
               var displayText = [];

               if (ctrl.isActivityFilter()) {

                   if (scope.filterData.activities.length === 0) {
                       return false;
                   }
                   angular.forEach(scope.filterData.activities, function(value) {
                       var text = (value === "-1") ? DEFAULT_ACTIVITY.name : ctrl.idToName[value];
                       displayText.push(text);

                   });
               } else {

                   if (scope.filterData.processes.length === 0) {
                       return false;
                   }
                   angular.forEach(scope.filterData.processes, function(value) {
                       var text = (value === "-1") ? DEFAULT_PROCESS.name : ctrl.idToName[value];
                       displayText.push(text);
                   });
               }
               var title = displayText.join(',');
               scope.setFilterTitle(sdUtilService.truncateTitle(title));
               return true;
            };
         }
      };
   }

   var UNIQUE_ID_SEPRATOR = "#|#";

   var DEFAULT_ACTIVITY = {
      "id" : "-1",
      "qualifiedId" : "-1",
      "order" : 0,
      "name" : "All Activites",
      "description" : "All Activites.",
      "implementationTypeId" : "Manual",
      "implementationTypeName" : "Manual",
      "auxillary" : false,
      "process" : "All processes",
      "uniqueId" : "-1"
   };

   var DEFAULT_PROCESS = {
      "id" : "-1",
      "order" : 0,
      "name" : "All Processes",
      "description" : "All Processes",
      "modelOid" : -1,
      "modelName" : "PredefinedModel",
      "auxillary" : false,
      "activities" : [],
      "model" : ""
   };

   var FILTER_TYPE_ACTIVITY = "activity";

   /**
    *
    */
   var FilterController = function($scope, $attrs, $filter, $parse) {

      var self = this;

      this.filterType = $attrs.sdaFilterType;

      var allProcessBinding = $parse($attrs.sdaProcesses);

      allProcessBinding($scope).then(function(result) {
    	  self.allAccessibleProcesses = result;

    	  trace.debug("Intialized with Filter type : ", self.filterType);
          trace.debug("Intialized with Processes : ", self.allAccessibleProcesses);

          self.intialize($scope);
      });

      /**
       *
       */
      this.loadAllActivities = function() {
         this.getAllActivities($filter);
      };
      /**
       *
       */
      this.isActivityFilter = function() {
         return self.filterType == FILTER_TYPE_ACTIVITY;
      };

      /***
       *
       */
      this.updateProcess = function() {
         if (self.isActivityFilter()) {
            this.getActivitiesForSelectedProcesses($filter, $scope.filterData.processes);
         }
      };

      /**
       *
       */
      this.loadValues = function() {
         angular.forEach(self.allAccessibleProcesses, function(data) {

            var duplicate =  $filter('filter')(self.processes, {
               id : data.id
            }, true);

            if (duplicate.length < 1) {
            	data['order'] = 1;
            	self.processes.push(data)
            }


         });

         if (this.isActivityFilter()) {
            this.loadAllActivities();
         }
      };

      $scope.filterCtrl = this;
   };

   /*
    *
    */
   FilterController.prototype.getActivitiesForSelectedProcesses = function( $filter, selectedProcesses) {
      var self = this;
      self.activities = [ DEFAULT_ACTIVITY ];

      if (selectedProcesses.indexOf('-1') > -1) {

         self.loadAllActivities();
      }
      else {

         angular.forEach(selectedProcesses, function( selectedProcess) {

            angular.forEach(self.allAccessibleProcesses, function( process) {
               if (process.id === selectedProcess) {
                  var activities = process.activities;
                  angular.forEach(activities, function(activity) {
                     activity['order'] = 1;
                     var found = $filter('filter')(self.activities, {
                        uniqueId : activity.uniqueId
                     }, true);
                     if (found.length < 1) {
                        activity['order'] = 1;
                        self.activities.push(activity)
                     }
                  })
               }
            });
         });
      }
   };

   /*
    *
    */
   FilterController.prototype.intialize = function( $scope) {
      this.showAuxillaryProcess = false;
      this.showAuxillaryActivity = false;

      this.idToName = {};
      this.createIdNamePairs( $scope);
      if (angular.isUndefined( $scope.filterData.processes)) {
         this.processes = [ DEFAULT_PROCESS ];
         this.activities = [ DEFAULT_ACTIVITY ];
         this.loadValues();
      }
      else {
         this.processes = [ DEFAULT_PROCESS ];
         this.activities = [ DEFAULT_ACTIVITY ];
         this.loadValues();
         this.updateProcess();
      }

   };

   /**
    *
    */
   FilterController.prototype.createIdNamePairs = function() {
      var self = this;

      angular.forEach(self.allAccessibleProcesses, function( process) {
         self.idToName[process.id] = process.name;
         angular.forEach(process.activities, function(activity) {
         //Using a seprator to avoid send a array inside another array which the DTOBuilder is not able to handle.
            activity.uniqueId = process.id + UNIQUE_ID_SEPRATOR + activity.qualifiedId;
            self.idToName[activity.uniqueId] = activity.name;
         });
      });

   };

   /*
    *
    */
   FilterController.prototype.auxComparator = function(auxValue, showAux) {
	   if (showAux) {
		   return true;
	   }
	   else {
		   return !auxValue;
	   }
   };

   /*
    *
    */
   FilterController.prototype.getAllActivities = function($filter) {
	   var self = this;
	   self.activities = [];
	   self.activities.push(DEFAULT_ACTIVITY);

	   angular.forEach(self.allAccessibleProcesses, function(process) {

		   if (!angular.isUndefined(process.activities)) {
			   angular.forEach(process.activities, function(activity) {

				  angular.forEach(process.activities, function(activity) {
					   var duplicate =  $filter('filter')(self.activities, {
						   uniqueId : activity.uniqueId
					   }, true);

					   if (duplicate.length < 1) {
						   activity['order'] = 1;
						   self.activities.push(activity)
					   }
				   });

			   });
		   }
	   });
   };

   /*
    *
    */
   FilterController.prototype.showHideAuxillaryProcess = function() {
      this.showAuxillaryProcess = !this.showAuxillaryProcess;
   };

   /*
    *
    */
   FilterController.prototype.showHideAuxillaryActivity = function() {
      this.showAuxillaryActivity = !this.showAuxillaryActivity;
   };

})();
