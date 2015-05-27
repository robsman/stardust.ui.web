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

	angular.module('bpm-common').directive( 'sdAbortProcessDialog', [ 'sdProcessInstanceService', 'sdUtilService', 'sdPortalConfigurationService', AbortProcess]);
	
	var ABORT_SCOPE = {
			ROOT :'RootHierarchy',
			SUB : 'SubHierarchy'
		};

	/**
    * 
    */
	function AbortProcess(sdProcessInstanceService, sdUtilService, sdPortalConfigurationService){
		
		return {
			restrict: 'A',
			template: '<div  sd-dialog  '+
							'sda-show="showDialog" '+
							'sda-type="confirm" '+
							'sda-title="{{abortProcessCtrl.i18n(\'views-common-messages.views-common-process-abortProcess-label\')}}" '+
							'sda-scope="this" '+
							'sda-confirm-action-label="{{abortProcessCtrl.abortProcess.isPromptRequired ? abortProcessCtrl.i18n(\'views-common-messages.common-ok\') : abortProcessCtrl.i18n(\'views-common-messages.common-yes\') }}" ' +
							'sda-cancel-action-label="{{abortProcessCtrl.abortProcess.isPromptRequired ? abortProcessCtrl.i18n(\'views-common-messages.common-close\') : abortProcessCtrl.i18n(\'views-common-messages.common-no\')}}" ' +
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
			controller: [ '$scope', 'sdProcessInstanceService', 'sdUtilService', 'sdPortalConfigurationService', AbortProcessController]
		};
	};

	/**
    * 
    */
	var AbortProcessController = function( $scope, sdProcessInstanceService, sdUtilService, sdPortalConfigurationService){
		
		var self = this;

		this.intialize( $scope, sdProcessInstanceService, sdPortalConfigurationService);
		
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
	AbortProcessController.prototype.intialize = function ( $scope, sdProcessInstanceService, sdPortalConfigurationService){

		this.i18n = $scope.$parent.i18n;
		this.sdProcessInstanceService = sdProcessInstanceService;
		this.notification = {
			result : null,
			error : false
		};
		
		var abortScope =  sdPortalConfigurationService.getAbortProcessScope();

		var isPromptRequired = false;

		if (abortScope == '') {
		    isPromptRequired = true;
		    this.configuredScope = ABORT_SCOPE.SUB;
		} else {
		    this.configuredScope = abortScope;
		}

		this.abortProcess = {
			scope : abortScope,
			processes : [],
			isPromptRequired : isPromptRequired
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
		this.abortProcess.scope =  this.configuredScope;
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
