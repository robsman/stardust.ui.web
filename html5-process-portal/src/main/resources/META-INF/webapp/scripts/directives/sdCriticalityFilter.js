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
         template : "<div class=\"priority-criticality-filter-container\"> "
                  + "<label ng-bind=\"criticalityCtrl.i18n('views-common-messages.views-activityTable-criticalityFilter-autoComplete-title')\"><\/label> "
                  + "<div sd-auto-complete  "
                  + "sda-item-pre-class=\"criticalityCtrl.tagPreMapper(item,index)\"  "
                  + "sda-tag-pre-class=\"criticalityCtrl.tagPreMapper(item,index)\"  "
                  + "sda-matches=\"criticalityCtrl.data\"  "
                  + "sda-match-str=\"criticalityCtrl.matchVal\"  "
                  + "sda-change=\"criticalityCtrl.getCriticalities(criticalityCtrl.matchVal)\"  "
                  + "sda-text-property=\"label\" "
                  + "sda-container-class=\"priority-criticality-filter-ac-container\" "
                  + "sda-item-hot-class=\"sd-ac-item-isActive\" "
                  + "sda-selected-matches=\"criticalityCtrl.like\"><\/div><\/div> ",
         controller : [ '$scope', 'sdCriticalityService', CriticalityFilterController ],
         link : function(scope, element, attr, ctrl)
         {

            /*
             */
            scope.handlers.applyFilter = function()
            {
               scope.filterData.rangeLike = [];
               var displayText = [];
               angular.forEach(ctrl.like, function(value)
               {
                  displayText.push(value.label);
                  scope.filterData.rangeLike.push({
                     'from' : value.rangeFrom,
                     'to' : value.rangeTo,
                     'label' : value.label
                  });
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
         sdCriticalityService.getAllCriticalities().then(
                  function(criticalities)
         {
            self.availableCriticalities = criticalities;

                     var criticalityValues = [];

                     angular.forEach($scope.filterData.rangeLike, function(criticality)
                     {
                        criticalityValues.push(criticality.label)
         });
                     self.like = self.getCriticalityByValue(self.availableCriticalities,
                              criticalityValues);
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

      this.data = [];
      this.availableCriticalities = [];
      this.matchVal = "";
      this.like = this.like || [];
      $scope.filterData.rangeLike = $scope.filterData.rangeLike || [];
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

   /**
    * 
    */
   CriticalityFilterController.prototype.getCriticalityByValue = function(
            allCriticalities, criticalityValues)
   {
      var criticalityObjs = [];
      angular.forEach(allCriticalities, function(criticality)
      {
         if (criticalityValues.indexOf(criticality.label) > -1)
         {
            criticalityObjs.push(criticality);
         }
      });
      return criticalityObjs;
   };

})();
