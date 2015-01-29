( function(){
	'use strict';

	angular.module('bpm-common').directive('sdStatusFilter', ['sdStatusService',StatusFilter]);

		/*
		*
		*/
		function StatusFilter(){
			return {
				restrict: 'A',
				templateUrl: 'plugins/html5-process-portal/scripts/directives/partials/StatusFilter.html',
				scope :{
					sdaSelectedStatuses : '='
				},
				controller: ['$scope','sdStatusService',StatusFilterController]
			};
		}

		/*
		*
		*/
		function StatusFilterController($scope,sdStatusService){

			var self = this;

			this.intiliaze($scope,sdStatusService);

			$scope.statusFilterCtrl = this;
		}


		StatusFilterController.prototype.intiliaze = function($scope,sdStatusService) {
			var self = this;
			this.i18n = $scope.$parent.i18n;
			sdStatusService.getAllStatuses().then(function(value){
				self.statuses = value;
			});
		};

	})();
