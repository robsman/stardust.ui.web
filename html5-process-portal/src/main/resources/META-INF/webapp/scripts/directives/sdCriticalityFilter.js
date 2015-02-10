/*****************************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or
 * initial documentation
 ****************************************************************************************/
/**
 * @author Johnson.Quadras
 */
(function()
{
   'use strict';

   angular.module('bpm-common').directive('sdCriticalityFilter',
            [ 'sdCriticalityService', CriticalityFilter ]);

   /*
    */
   function CriticalityFilter()
   {
      var MAX_TITLE_LENGTH = 35;
      return {
         restrict : 'A',
         templateUrl : 'plugins/html5-process-portal/scripts/directives/partials/CriticalityFilter.html',
         controller : [ '$scope', 'sdCriticalityService', CriticalityFilterController ],
         link : function(scope, element, attr, ctrl)
         {
            /*
             */
            scope.handlers.applyFilter = function()
            {
               if (scope.filterData.criticalityLike.length < 1)
               {
                  return false;
               }
               var displayText = [];
               angular.forEach(scope.filterData.criticalityLike, function(value)
               {
                  displayText.push(value.label);
               })
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

   /*
    * 
    */
   var CriticalityFilterController = function($scope, sdCriticalityService)
   {
      var self = this;
      this.i18n = $scope.$parent.i18n;
      this.intialize($scope);

      this.loadAvailableCriticalities = function()
      {
         sdCriticalityService.getAllCriticalities().then(function(criticalities)
         {
            self.availableCriticalities = criticalities;
         });
      }

      this.loadAvailableCriticalities();
      $scope.criticalityCtrl = this;
   };

   /*
    * 
    */
   CriticalityFilterController.prototype.intialize = function($scope)
   {

      $scope.filterData.criticalityLike = $scope.filterData.criticalityLike || [];
      this.data = [];
      this.availableCriticalities = [];
      this.matchVal = "";
      $scope.criticalityCtrl = this;
   };
   /*
    * 
    */
   CriticalityFilterController.prototype.tagPreMapper = function(item, index)
   {
      var tagClass = "glyphicon glyphicon-flag criticality-flag-" + item.color;
      return tagClass;
   };

   /**
    * 
    */
   CriticalityFilterController.prototype.getCriticalities = function(value)
   {

      var results = [];

      this.availableCriticalities.forEach(function(v)
      {
         if (v.label.indexOf(value) > -1)
         {
            results.push(v);
         }
      });

      this.data = results;
   };

})();
