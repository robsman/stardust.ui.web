(function () {
	'use strict';

	angular.module('bpm-common').directive('sdPriorityFilter', ['sdPriorityService',PriorityFilter]);

		/*
		*/
		function PriorityFilter(){

			return {
				restrict : 'A',
				templateUrl :'plugins/html5-process-portal/scripts/directives/partials/PriorityFilter.html',
				scope :{
					sdaSelectedPriorities : '='
				},
				controller :['$scope','sdPriorityService',PriorityFilterController]
			};
		}
		/*
		*
		*/
		var PriorityFilterController = function($scope,sdPriorityService){

			$scope.sdaSelectedPriorities=[];
			this.data = [];
			this.matchVal="";
			this.intialize($scope,sdPriorityService);
			$scope.priorityCtrl = this;
		};

		/**
		*
		*/
		PriorityFilterController.prototype.intialize = function($scope,sdPriorityService) {
			var self = this;
			this.i18n = $scope.$parent.i18n;

			sdPriorityService.getAllPriorities().then(function(value){
				self.priorities = value;
			});
		};

		/*
		*
		*/
		PriorityFilterController.prototype.tagPreMapper=function (item,index){
			var tagClass="fa fa-flag priority-flag-"+item.category;
			return tagClass;
		};

		/**
		*
		*/
		PriorityFilterController.prototype.getPriority = function(value) {

			var results=[];

			this.priorities.forEach(function(v){
				if(v.name.indexOf(value) > -1){
					results.push(v);
				}
			});

			this.data=results;
		};

	})();
