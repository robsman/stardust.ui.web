/*****************************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ****************************************************************************************/

/*
 * @author Johnson.Quadras
 */
(function(){
	'use strict';

	angular.module('bpm-common').directive( 'sdAbortActivityDialog', [ 'sdActivityInstanceService','sdLoggerService', 'sdUtilService', AbortActivity]);

	/**
	 * 
	 */
	function AbortActivity(sdActivityInstanceService, sdLoggerService, sdUtilService){

		return {
			restrict: 'A',
			template: '<div  sd-dialog  '+
							'sda-show="showDialog" '+
							'sda-type="confirm" '+
							'sda-title="{{abortActivityCtrl.i18n(\'views-common-messages.views-common-activity-abortActivity-label\')}}" '+
							'sda-scope="this" '+
							'sda-confirm-action-label="{{abortActivityCtrl.i18n(\'views-common-messages.common-ok\')}}" ' +
							'sda-cancel-action-label="{{abortActivityCtrl.i18n(\'views-common-messages.common-close\')}}" ' +
							'sda-on-open="abortActivityCtrl.onConfirm(res)" '+
							'sda-template="' +
							 sdUtilService.getBaseUrl() + 'plugins/html5-process-portal/scripts/directives/partials/abortActivityDialogBody.html"> '+
					 '<\/div> ' +
					 '<span style="float: left;" ' +
					 		'sd-dialog="abortActivityCtrl.abortActivityNotification" ' +
					 		'sda-title="{{abortActivityCtrl.i18n(\'views-common-messages.common-notification-title\')}}" '+
					 		'sda-type="custom" ' +
					 		'sda-scope="this" ' +
					 		'sda-template="' +
							 sdUtilService.getBaseUrl() + 'plugins/html5-process-portal/scripts/directives/partials/abortActivityNotification.html"> ' +
					 '</span>',
			scope :{
				activitiesToAbort : '=sdaActivitiesToAbort',
				showDialog : '=sdaShowDialog',
				abortCompleted: '&sdaOnAbortComplete'
			},
			controller: [ '$scope', 'sdActivityInstanceService', 'sdLoggerService', AbortActivityController]
		};
	};

	/**
	 * 
	 */

	var trace = null;
	var AbortActivityController = function( $scope, sdActivityInstanceService, sdLoggerService){

	    var self = this;
	    trace = sdLoggerService.getLogger('bpm-common.sdAbortActivityDialog');
	    this.intialize( $scope, sdActivityInstanceService);

	    /**
	     * 
	     */
	    AbortActivityController.prototype.abortCompleted = function (){
		$scope.abortCompleted();
	    };
	    /**
	     * 
	     */
	    AbortActivityController.prototype.hideDialog = function (){
		$scope.showDialog = false;
	    };
	    /**
	     * 
	     */
	    AbortActivityController.prototype.openDialog = function (){
		$scope.showDialog = true;
	    };
	    /**
	     * 
	     */
	    AbortActivityController.prototype.getActvities = function (){
		this.abortActivity.activities = $scope.activitiesToAbort;
	    };

	    /**
	     * 
	     */
	    AbortActivityController.prototype.showNotification = function (){
		self.abortActivityNotification.open();
	    };

	    $scope.abortActivityCtrl = this;
	}

	/**
	 * 
	 */
	AbortActivityController.prototype.intialize = function ( $scope, sdActivityInstanceService){

	    this.i18n = $scope.$parent.i18n;
	    this.sdActivityInstanceService = sdActivityInstanceService;
	    this.notification = {
		    result : null,
		    error : false
	    };
	    this.abortActivity = {
		    scope : 'activity',
		    activities : []
	    };
	};

	/**
	 * 
	 */
	AbortActivityController.prototype.abortActivities = function (){
	    this.getActvities();
	    trace.debug("Aborting activities with following params ",this.abortActivity.scope, this.abortActivity.activities);
	    return this.sdActivityInstanceService.abortActivities( this.abortActivity.scope, this.abortActivity.activities);
	};
	/**
	 * 
	 */
	AbortActivityController.prototype.resetValues = function (){
	    this.abortActivity.scope ='activity';
	    this.notification.result = {};
	    this.notification.error = false;
	};

	/**
	 * 
	 */
	AbortActivityController.prototype.onConfirm = function(res) {
	    var self = this;
	    var promise = res.promise;

	    this.resetValues();

	    promise.then(function(data) {
		self.abortActivities().then(function(data) {
		    // success
		    self.showNotification();
		    self.abortCompleted();
		    self.notification.result = data;
		    if (self.notification.result.failure.length > 0) {
			trace.debug("Failure in aborting activities ", self.notification.result.failure);
		    } else {
			trace.debug("Activities aborted sucessfully.");
		    }

		}, function(data) {
		    // Failure
		    self.notification.result = {};
		    self.notification.error = true;
		    self.showNotification();
		    trace.error("Error in aborting activities.");
		});
	    })
	};

})();
