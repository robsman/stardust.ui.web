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
 * @author Nikhil.Gahlot
 */
(function(){
	'use strict';

	angular.module('bpm-common').directive( 'sdAbortProcessDialog', [ 'sdProcessInstanceService', 'sdUtilService', AbortProcess]);
	
	var ABORT_SCOPE = {
			ROOT : 'root',
			SUB : 'sub'
		};

	/**
    * 
    */
	function AbortProcess(sdProcessInstanceService, sdUtilService){
		
		return {
			restrict: 'A',
			template: '<div  sd-dialog  '+
							'sda-show="showDialog" '+
							'sda-type="confirm" '+
							'sda-title="{{abortProcessCtrl.i18n(\'views-common-messages.views-common-process-abortProcess-label\')}}" '+
							'sda-scope="this" '+
							'sda-confirm-action-label="{{abortProcessCtrl.i18n(\'views-common-messages.common-ok\')}}" ' +
							'sda-cancel-action-label="{{abortProcessCtrl.i18n(\'views-common-messages.common-close\')}}" ' +
							'sda-on-open="abortProcessCtrl.onConfirm(res)" '+
							'sda-template="' +
							 sdUtilService.getBaseUrl() + 'plugins/html5-process-portal/scripts/directives/partials/abortProcessDialogBody.html"> '+
					 '<\/div> ' +
					 '<span style="float: left;" ' +
					 		'sd-dialog="abortProcessCtrl.abortProcessNotification" ' +
					 		'sda-title="{{abortProcessCtrl.i18n(\'admin-portal-messages.common-notification-title\')}}" '+
					 		'sda-type="custom" ' +
					 		'sda-scope="this" ' +
					 		'sda-template="' +
							 sdUtilService.getBaseUrl() + 'plugins/html5-process-portal/scripts/directives/partials/abortProcessNotification.html"> ' +
					 '</span>',
			scope :{
				processesToAbort : '=sdaProcessesToAbort',
				showDialog : '=sdaShowDialog',
				abortCompleted: '&sdaOnAbortComplete'
			},
			controller: [ '$scope', 'sdProcessInstanceService', AbortProcessController]
		};
	};

	/**
    * 
    */
	var AbortProcessController = function( $scope, sdProcessInstanceService){
		
		var self = this;

		this.intialize( $scope, sdProcessInstanceService);
		
		/**
		 * 
		 */
		AbortProcessController.prototype.abortCompleted = function (){
			$scope.abortCompleted();
		};
		/**
		 * 
		 */
		AbortProcessController.prototype.hideDialog = function (){
			$scope.showDialog = false;
		};
		/**
		 * 
		 */
		AbortProcessController.prototype.openDialog = function (){
			$scope.showDialog = true;
		};
		/**
		 * 
		 */
		AbortProcessController.prototype.getProcesses = function (){
			this.abortProcess.processes = $scope.processesToAbort;
		};

		/**
         * 
        */
		AbortProcessController.prototype.showNotification = function (){
			self.abortProcessNotification.open();
		};

		$scope.abortProcessCtrl = this;
	}

	/**
    * 
    */
	AbortProcessController.prototype.intialize = function ( $scope, sdProcessInstanceService){

		this.i18n = $scope.$parent.i18n;
		this.sdProcessInstanceService = sdProcessInstanceService;
		this.notification = {
			result : null,
			error : false
		};
		this.abortProcess = {
			scope : ABORT_SCOPE.SUB,
			processes : []
		};
	};

	/**
    * 
    */
	AbortProcessController.prototype.abortProcesses = function (){
		this.getProcesses();
		return this.sdProcessInstanceService.abortProcesses( this.abortProcess.scope, this.abortProcess.processes);
	};
	/**
    * 
    */
	AbortProcessController.prototype.resetValues = function (){
		this.abortProcess.scope = ABORT_SCOPE.SUB;
		this.notification.result = {};
		this.notification.error = false;
	};
	
	/**
    * 
    */
	AbortProcessController.prototype.onConfirm = function(res) {
		
		var self = this;
		var promise = res.promise;
		
		this.resetValues();

		promise.then(function(data) {
			self.abortProcesses().then(function(data) {
				// success
				self.showNotification();
				self.abortCompleted();
				self.notification.result = data;
			}, function(data) {
				// Failure
				self.notification.result = {};
				self.notification.error = true;
				self.showNotification();
			});
		})
	};
	
})();
