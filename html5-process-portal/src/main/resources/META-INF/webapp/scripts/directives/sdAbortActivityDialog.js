(function(){
	'use strict';

	angular.module('bpm-common').directive('sdAbortActivityDialog',['sdAbortActivityService','ngDialog',AbortActivity]);

	/**
	*
	*/
	function AbortActivity(sdAbortActivityService){

		return {
			restrict: 'A',
			templateUrl: 'plugins/html5-process-portal/scripts/directives/partials/AbortActivity.html',
			scope :{
				activitiesToAbort : '=sdaActivitiesToAbort',
				showDialog : '=sdaShowDialog',
				abortCompleted: '&sdaOnAbortComplete'
			},
			controller: ['$scope','sdAbortActivityService','ngDialog',AbortActivityController]
		};
	}

	/**
	*
	*/
	var AbortActivityController = function($scope, sdAbortActivityService,ngDialog){

		var self = this;

		this.intialize($scope,sdAbortActivityService,ngDialog);


		AbortActivityController.prototype.abortCompleted = function (){
			$scope.abortCompleted();
		}

		AbortActivityController.prototype.hideDialog = function (){
			$scope.showDialog = false;
		}

		AbortActivityController.prototype.openDialog = function (){
			$scope.showDialog = true;
		}

		AbortActivityController.prototype.getActvities = function (){
			this.abortActivity.activities = $scope.activitiesToAbort;
		}

		/**
		*
		*/
		AbortActivityController.prototype.showNotification = function (){

			var options = {
				template: 'plugins/html5-common/scripts/directives/dialogs/templates/info.html',
				userTemplate : 'plugins/html5-process-portal/scripts/directives/partials/AbortActivityNotification.html',
				controller: this,
				scope: $scope ,
				showOverlay:  true,
				title: self.i18n('admin-portal-messages.common-notification-title')
			};

			ngDialog.openConfirm(options);
		}




		$scope.abortActivityCtrl = this;

	}

	/**
	*
	*/
	AbortActivityController.prototype.intialize = function ($scope, sdAbortActivityService, ngDialog){

		this.i18n = $scope.$parent.i18n;
		this.sdAbortActivityService = sdAbortActivityService;

		this.notification = {
			result : null,
			error : false
		};


		this.abortActivity = {
			scope : 'activity',
			activities : []
		}
	}



	/**
	*
	*/
	AbortActivityController.prototype.abortActivities = function (){
		this.getActvities();
		return this.sdAbortActivityService.abortActivities(this.abortActivity);
	}


	/**
	*
	*/
	AbortActivityController.prototype.resetValues = function (){
		this.abortActivity.scope ='activity';
		this.notification.result = {};
		this.notification.error = false;

	}

	/**
	*
	*/
	AbortActivityController.prototype.onConfirm = function (res){

		var self = this;
		var promise = res.promise;

		this.resetValues();

		promise.then(function(data){

			self.abortActivities()
				.then(function(data){
						//success
						self.showNotification();
						self.abortCompleted();
						self.notification.result = data;

					},function(data){
						//Failure
						self.notification.result = {};
						self.notification.error= true;
						self.showNotification();
						console.log("Error in acquiring activities from Abort Activity Service");
					});

		})
		.catch(function(){
			console.log("dialog state: rejected");
		});
	};
}



)();
