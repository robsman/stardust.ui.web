/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
/**
 * @author Johnson.Quadras
 */
 (function()
 {
   'use strict';

   angular.module('bpm-common').directive('sdProcessActivityFilter',
      [ '$filter', ActivityFilter ]);

   /*
    *
    */
    function ActivityFilter()
    {
      var MAX_TITLE_LENGTH = 35;

      return {
         restrict : 'A',
         templateUrl : 'plugins/html5-process-portal/scripts/directives/partials/ProcessActivityFilter.html',
         controller : [ '$scope', '$attrs', '$filter', FilterController ],
         link : function(scope, element, attr, ctrl)
         {

            /*
            */
            scope.handlers.applyFilter = function()
            {
               var displayText = [];

               if (ctrl.isActivityFilter())
               {
                  angular.forEach(scope.filterData.activities, function(value)
                  {
                     displayText.push(ctrl.idToName[value]);
                  });
               }
               else
               {
                  if ((scope.filterData.activities.processes) < 1)
                     return false;

                  angular.forEach(scope.filterData.processes, function(value)
                  {
                     displayText.push(ctrl.idToName[value]);
                  });
               }
               var title = displayText.join(',');
               if (title.length > MAX_TITLE_LENGTH)
               {
                  title = title.substring(0, MAX_TITLE_LENGTH - 3);
                  title += '...';
               }
               scope.setFilterTitle(title);
               return true;
            };
         }
      };
   }
   ;

   var DEFAULT_ACTIVITY = {
      "id" : "-1",
      "qualifiedId" : "-1",
      "order" : 0,
      "name" : "All Activites",
      "description" : "All Activites.",
      "implementationTypeId" : "Manual",
      "implementationTypeName" : "Manual",
      "auxillary" : false,
      "process" : "All processes"
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
    var FilterController = function($scope, $attrs, $filter)
    {

      var self = this;

      this.sdaFilterType = $attrs.type;

      this.loadAllActivities = function()
      {
         this.getAllActivities($scope, $filter);
      }

      this.isActivityFilter = function()
      {
         return self.sdaFilterType === FILTER_TYPE_ACTIVITY;
      }

      this.updateProcess = function()
      {
         if (self.isActivityFilter())
         {
            this.getActivitiesForSelectedProcesses($scope, $filter, $scope.filterData.processes);
         }
      }

      this.loadValues = function()
      {
         angular.forEach($scope.worklistCtrl.allAccessibleProcesses, function(data)
         {

            var found = $filter('filter')(self.processes, {
               name : data.name
            }, true);
            if (found.length < 1)
            {
               data['order'] = 1;
               self.processes.push(data)
            }

         });

         if (this.isActivityFilter())
         {
            this.loadAllActivities();
         }
      }
      this.intialize($scope);
      $scope.filterCtrl = this;
   }

   /*
    *
    */

    FilterController.prototype.getActivitiesForSelectedProcesses = function($scope,$filter,selectedProcesses)
    {
      var self = this;
      self.activities = [ DEFAULT_ACTIVITY ];

      if (selectedProcesses.indexOf('-1') > -1)
      {

         self.loadAllActivities();
      }
      else
      {

         angular.forEach(selectedProcesses, function(selectedProcess)
         {

            angular.forEach($scope.worklistCtrl.allAccessibleProcesses, function(process)
            {
               if (process.id === selectedProcess)
               {
                  var activities = process.activities;
                  angular.forEach(activities, function(activity)
                  {
                     activity['order'] = 1;
                     var found = $filter('filter')(self.activities, {
                        name : activity.name
                     }, true);
                     if (found.length < 1)
                     {
                        activity['order'] = 1;
                        self.activities.push(activity)
                     }
                  })
               }
            });
         });
      }
   }

   /*
    *
    */
    FilterController.prototype.intialize = function($scope)
    {
      this.i18n = $scope.$parent.i18n;
      this.showAuxillaryProcess = false;
      this.showAuxillaryActivity = false;

      this.idToName = {};
      this.createIdNamePairs($scope);
      if (angular.isUndefined($scope.filterData.processes))
      {
         this.processes = [ DEFAULT_PROCESS ];
         this.activities = [ DEFAULT_ACTIVITY ];
         this.loadValues();
      }
      else
      {
         this.processes = [ DEFAULT_PROCESS ];
         this.activities = [ DEFAULT_ACTIVITY ];
         this.loadValues();
         this.updateProcess();
      }

   }

   /**
    *
    */
    FilterController.prototype.createIdNamePairs = function($scope)
    {
      var self = this;
      self.idToName[DEFAULT_PROCESS.id] = DEFAULT_PROCESS.name;
      self.idToName[DEFAULT_ACTIVITY.id] = DEFAULT_ACTIVITY.name;
      angular.forEach($scope.worklistCtrl.allAccessibleProcesses, function(process)
      {
         self.idToName[process.id] = process.name;
         angular.forEach(process.activities, function(activity)
         {
            self.idToName[activity.qualifiedId] = activity.name;
         });
      });
   }

   /*
    *
    */
    FilterController.prototype.auxComparator = function(auxValue, showAux)
    {
      if (showAux)
      {
         return true;
      }
      else
      {
         return !auxValue;
      }
   }

   /*
    *
    */
    FilterController.prototype.getAllActivities = function($scope, $filter)
    {
      var self = this;
      self.activities = [];
      self.activities.push(DEFAULT_ACTIVITY);

      angular.forEach($scope.worklistCtrl.allAccessibleProcesses, function(process)
      {

         if (!angular.isUndefined(process.activities))
         {
            var activities = process.activities
            angular.forEach(activities, function(activity)
            {
               var found = $filter('filter')(self.activities, {
                  name : activity.name
               }, true);
               if (found.length < 1)
               {
                  activity['order'] = 1;
                  self.activities.push(activity)
               }
            });
         }
      });
   }

   /*
    *
    */
    FilterController.prototype.showHideAuxillaryProcess = function()
    {
      this.showAuxillaryProcess = !this.showAuxillaryProcess;
   }

   /*
    *
    */
    FilterController.prototype.showHideAuxillaryActivity = function()
    {
      this.showAuxillaryActivity = !this.showAuxillaryActivity;
   }

})();
