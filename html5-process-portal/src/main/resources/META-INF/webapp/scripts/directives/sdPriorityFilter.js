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
(function () {
	'use strict';

	angular.module('bpm-common').directive('sdPriorityFilter', ['sdPriorityService',PriorityFilter]);

		/*
		*/
		function PriorityFilter(){

			return {
				restrict : 'A',
				templateUrl :'plugins/html5-process-portal/scripts/directives/partials/PriorityFilter.html',
				controller :['$scope','sdPriorityService',PriorityFilterController],
				link: function(scope, element, attr, ctrl) {
					/*
					*/
					scope.handlers.applyFilter = function() {
						if(scope.filterData.priorityLike < 1 ){
							return false;
						}
						var displayText = [];
						angular.forEach(scope.filterData.priorityLike,function(value){
								displayText.push(value.label);
						});

						scope.setFilterTitle( displayText.join(','));
						return true;
					};
				}
			};
		}
		/*
		*
		*/
		var PriorityFilterController = function($scope,sdPriorityService){

			this.intialize($scope,sdPriorityService);
			$scope.priorityCtrl = this;
		};

		/**
		*
		*/
		PriorityFilterController.prototype.intialize = function($scope,sdPriorityService) {
			var self = this;
			this.i18n = $scope.$parent.i18n;
			$scope.filterData.priorityLike = $scope.filterData.priorityLike || [];
			this.data = [];
			this.matchVal="";

			sdPriorityService.getAllPriorities().then(function(value){
				self.priorities = value;
			});
		};

		/*
		*
		*/
		PriorityFilterController.prototype.tagPreMapper=function (item,index){
			var tagClass="fa fa-flag priority-flag-"+item.name;
			return tagClass;
		};

		/**
		*
		*/
		PriorityFilterController.prototype.getPriority = function(value) {

			var results=[];

			this.priorities.forEach(function(v){
				if(v.label.indexOf(value) > -1){
					results.push(v);
				}
			});

			this.data=results;
		};

	})();
