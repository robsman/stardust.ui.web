	(function(){
		'use strict';

		angular.module('bpm-common').directive('sdProcessActivityFilter',ActivityFilter);

		/*
		*
		*/

		function ActivityFilter(){

			return {
				restrict : 'A',
				templateUrl :'plugins/html5-process-portal/scripts/directives/partials/ProcessActivityFilter.html',
				scope :{
					sdaFilterType : '@',
					sdaProcesses :'=',
					sdaSelectedValues:'=',
					sdaGroupItems:'@'
				},
				controller :['$scope',FilterController]
			};

		}

		var DEFAULT_ACTIVITY ={
			"id":"all",
			"qualifiedId":"all",
			"name":"All Activites",
			"description":"All Activites.",
			"implementationTypeId":"Manual",
			"implementationTypeName":"Manual",
			"auxillary":false,
			"process":"All processes"
		};


		var DEFAULT_PROCESS = {
			"id":"all",
			"name":"All Processes",
			"description":"All Processes",
			"modelOid":-1,
			"modelName":"PredefinedModel",
			"auxillary":false,
			"activities":[],
			"model":""
		};

		var FILTER_TYPE_ACTIVITY = "activity";

		/**
		*
		*/
		var FilterController = function($scope){

			var self = this;

			this.intialize($scope);

			this.loadAllActivities = function(){
				this.getAllActivities($scope);
			}

			this.isActivityFilter = function(){
				return $scope.sdaFilterType === FILTER_TYPE_ACTIVITY;
			}

			this.updateProcess = function(){
				if(self.isActivityFilter()){
					this.getActivitiesForSelectedProcesses($scope,$scope.sdaSelectedValues.processes);
				}
			}

			this.loadValues = function(){
				angular.forEach($scope.sdaProcesses,function(data){
					self.processes.push(data);
				});

				if(this.isActivityFilter()){
					this.loadAllActivities();
				}
			}

			this.loadValues();
			$scope.filterCtrl = this;
		}


		/*
		*
		*/

		FilterController.prototype.getActivitiesForSelectedProcesses = function($scope,selectedProcesses){
			var self = this;
			self.activities = [DEFAULT_ACTIVITY];

			if(selectedProcesses.indexOf('all') > -1 ){

				self.loadAllActivities();
			}else{

				angular.forEach(selectedProcesses,function(selectedProcess){

					angular.forEach( $scope.sdaProcesses,function(process){
						if(process.id === selectedProcess){
							var activities =  process.activities;
							angular.forEach(activities,function(activity){
								activity['process'] = process.name;
								self.activities.push(activity);
							})
						}
					});
				});
			}
		}


		/*
		*
		*/
		FilterController.prototype.intialize = function($scope){
			this.i18n = $scope.$parent.i18n;
			this.showAuxillaryProcess = false;
			this.showAuxillaryActivity = false;

			this.processes = [DEFAULT_PROCESS];
			this.activities = [DEFAULT_ACTIVITY];
		}

		/*
		*
		*/
		FilterController.prototype.auxComparator = function(auxValue , showAux){
			if ( showAux ) {
				return true;
			}else{
				return !auxValue;
			}
		}

		/*
		*
		*/
		FilterController.prototype.getAllActivities = function($scope){
			var self = this;
			self.activities = [];
			self.activities.push(DEFAULT_ACTIVITY);

			angular.forEach($scope.sdaProcesses, function(process) {

				if( !angular.isUndefined(process.activities) ){
					var activities = process.activities
					angular.forEach(activities, function(activity) {
						activity['process'] = process.name;
						self.activities.push(activity);
					});
				}
			});
		}

		/*
		*
		*/
		FilterController.prototype.showHideAuxillaryProcess = function(){
			this.showAuxillaryProcess = !this.showAuxillaryProcess;
		}

		/*
		*
		*/
		FilterController.prototype.showHideAuxillaryActivity = function(){
			this.showAuxillaryActivity = !this.showAuxillaryActivity;
		}


	})();
