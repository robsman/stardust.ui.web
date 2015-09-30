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

   angular.module('bpm-common').directive( 'sdPriorityTableFilter', [ 'sdPriorityService', 'sdUtilService', PriorityFilter]);

		/*
		*/
   function PriorityFilter( sdPriorityService, sdUtilService) {
			return {
				restrict : 'A',
				template : '<div class="priority-criticality-filter-container"> '+
								'<label ng-bind="criticalityCtrl.i18n(\'views-common-messages.views-activityTable-priorityFilter-autoComplete-title\')"><\/label> '+
								'<div sd-auto-complete '+
									'sda-item-pre-class="priorityCtrl.tagPreMapper(item,index)"'+
									'sda-tag-pre-class="priorityCtrl.tagPreMapper(item,index)" '+
									'sda-matches="priorityCtrl.data" sda-match-str="priorityCtrl.matchVal"'+
									'sda-change="priorityCtrl.getPriority(priorityCtrl.matchVal)" '+
									'sda-text-property="label" '+
									'sda-container-class="priority-criticality-filter-ac-container" '+
									'sda-item-hot-class="sd-ac-item-isActive" '+
									'sda-selected-matches="filterData.like">' +
								'<\/div>' +
							'<\/div>',
         controller : [ '$scope', 'sdPriorityService', PriorityFilterController ],
         link : function( scope, element, attr, ctrl) {
            /*
             * 
             */
        	 scope.handlers.applyFilter = function() {
        		 var filterValues = [];
        		 var displayText = [];
        		 angular.forEach(scope.filterData.like, function(priority) {
        			 if (displayText.indexOf(priority.label) < 0) {
        				 displayText.push(priority.label);
        				 filterValues.push(priority.value);
        			 }
        		 });
        		 var title = displayText.join(',');
        		 scope.setFilterTitle(sdUtilService.truncateTitle(title));
        		 scope.filterData.like = filterValues;
        		 return true;
        	 };
         }
      };
   }
   /*
    * 
    */
   var PriorityFilterController = function( $scope, sdPriorityService) {

      this.intialize($scope, sdPriorityService);
      $scope.priorityCtrl = this;
   };

   /**
    * 
    */
   PriorityFilterController.prototype.intialize = function($scope, sdPriorityService) {
      var self = this;
      this.i18n = $scope.i18n;
      $scope.filterData.like = $scope.filterData.like || [];
      this.data = [];
      this.matchVal = "";

      sdPriorityService.getAllPriorities().then(
    		  function(value) {
    			  self.priorities = value;
    			  if ($scope.filterData.like.length > 0) {
    				  $scope.filterData.like = self.getPriorityFromValues(value, $scope.filterData.like);
    			  }
    		  });
   };

   /*
    * 
    */
   PriorityFilterController.prototype.tagPreMapper = function(item, index) {
      var tagClass = "glyphicon glyphicon-flag priority-flag-" + item.name;
      return tagClass;
   };

   /**
    * 
    */
   PriorityFilterController.prototype.getPriority = function(value) {

	   var results = [];

	   this.priorities.forEach(function(v) {
		   if (v.label.indexOf(value) > -1) {
			   results.push(v);
		   }
	   });

	   this.data = results;
   };

   PriorityFilterController.prototype.getPriorityFromValues = function( availablePriorities, values) {
	   var prioirtyObjs = [];

	   angular.forEach(availablePriorities, function(priority) {
		   if (values.indexOf(priority.value) > -1) {
			   prioirtyObjs.push(priority);
		   }
	   });
	   return prioirtyObjs;
   };

})();
