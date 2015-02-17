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
      [ '$filter','$parse', ActivityFilter ]);

   /*
    *
    */
    function ActivityFilter()
    {
      var MAX_TITLE_LENGTH = 35;

      return {
         restrict : 'A',
         templateUrl : 'plugins/html5-process-portal/scripts/directives/partials/ProcessActivityFilter.html',
         controller : [ '$scope', '$attrs', '$filter','$parse', FilterController ],
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
                  if ((scope.filterData.processes) < 1)
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
    var FilterController = function($scope, $attrs, $filter,$parse)
    {

      var self = this;

      this.filterType = $attrs.sdaFilterType;
      
      var allProcessBinding  = $parse($attrs.sdaProcesses);
      
      this.allAccessibleProcesses = allProcessBinding($scope);

      this.loadAllActivities = function()
      {
         this.getAllActivities($filter);
      }

      this.isActivityFilter = function()
      {
         return self.filterType == FILTER_TYPE_ACTIVITY;
      }

      this.updateProcess = function()
      {
         if (self.isActivityFilter())
         {
            this.getActivitiesForSelectedProcesses($filter, $scope.filterData.processes);
         }
      }

      this.loadValues = function()
      {
         angular.forEach(self.allAccessibleProcesses, function(data)
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

    FilterController.prototype.getActivitiesForSelectedProcesses = function($filter,selectedProcesses)
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

            angular.forEach(self.allAccessibleProcesses, function(process)
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
    FilterController.prototype.createIdNamePairs = function()
    {
      var self = this;
      self.idToName[DEFAULT_PROCESS.id] = DEFAULT_PROCESS.name;
      self.idToName[DEFAULT_ACTIVITY.id] = DEFAULT_ACTIVITY.name;
      angular.forEach(self.allAccessibleProcesses, function(process)
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
    FilterController.prototype.getAllActivities = function($filter)
    {
      var self = this;
      self.activities = [];
      self.activities.push(DEFAULT_ACTIVITY);

      angular.forEach(self.allAccessibleProcesses, function(process)
      {

         if (!angular.isUndefined(process.activities))
         {
            angular.forEach(process.activities, function(activity)
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
