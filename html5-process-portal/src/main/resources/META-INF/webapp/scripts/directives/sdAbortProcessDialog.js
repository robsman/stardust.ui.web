/*****************************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. self program and
 * the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies self distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ****************************************************************************************/

/*
 * @author Abhay.Thappan
 */
(function(){
	'use strict';

	angular.module('bpm-common').directive( 'sdAbortProcessDialog', [ 'sdProcessInstanceService','sdLoggerService', 'sdUtilService', 'sdWorkflowPerspectiveConfigService', AbortProcess]);

	var trace;
	
	var ABORT_SCOPE = {
			ROOT :'RootHierarchy',
			SUB : 'SubHierarchy'
		};
	/**
	 * 
	 */
	function AbortProcess(sdProcessInstanceService, sdLoggerService, sdUtilService, sdWorkflowPerspectiveConfigService){

		var directiveDefObject = {
			restrict: 'A',
			scope :{
				processesToAbort : '=sdaProcessesToAbort',
				showDialog : '=sdaShowDialog',
				abortCompleted: '&sdaOnAbortComplete'
			},
			template: '<div  sd-dialog  '+
							'sda-show="showDialog" '+
							'sda-type="custom" '+
							'sda-title="{{abortProcessCtrl.i18n(\'views-common-messages.views-common-process-abortProcess-label\')}}" '+
							'sda-scope="this" '+
							'sda-on-open="abortProcessCtrl.onOpenDialog(res)"' +
							'sda-template="' +
							 sdUtilService.getBaseUrl() + 'plugins/html5-process-portal/scripts/directives/partials/abortProcessDialogBody.html"> '+
					 '<\/div> ',
			
			controller: AbortProcessController
		};

	/**
	 * 
	 */
   function AbortProcessController($attrs, $scope, $element){

	    var self = this;
	    trace = sdLoggerService.getLogger('bpm-common.sdAbortProcessDialog');
	    
	   intialize();
	    
	    /**
		 * 
		 */
	   function intialize(){
		    self.abortCompleted = $scope.abortCompleted;
			self.onOpen = $scope.onOpen;

		    self.i18n = $scope.$parent.i18n;
		    self.notification = {
					result : null,
					error : false
				};
		    
		    sdWorkflowPerspectiveConfigService.loadConfig().then(function(){
		    	var abortScope =  sdWorkflowPerspectiveConfigService.getAbortProcessScope();
		    	if (abortScope == '') {
		    		isPromptRequired = true;
		    		self.configuredScope = ABORT_SCOPE.SUB;
		    	} else {
		    		self.configuredScope = abortScope;
		    	}
		    });	

		    var isPromptRequired = false;

			self.abortProcess = {
					scope : abortScope,
					processes : [],
					isPromptRequired : isPromptRequired
			};
		    
		    self.onOpenDialog = onOpenDialog;
		    self.closeThisDialog = closeThisDialog;
		    self.resetValues = resetValues;
		    self.abortProcesses = abortProcesses;
		    self.confirm = confirm;
		    self.abortCompleted = abortCompleted;
		}
	    
	   /**
	    * 
	    */
		function onOpenDialog(result) {
	      self.resetValues();
		  if (angular.isDefined(self.onOpen)) {
				self.onOpen();
			}
		}

		/**
		 * 
		 */
		function closeThisDialog(scope) {
			scope.closeThisDialog();
		}

		
		 /**
		 * 
		 */
		function resetValues(){
			this.abortProcess.scope =  this.configuredScope;
			this.notification.result = {};
			this.notification.error = false;
		};

		
		 /**
	     * 
	     */
		function abortCompleted(scope){
			// passing the notification to the notification dialog 
		    scope.abortCompleted({notification:self.notification});
	    };
		
		/**
		 * 
		 */
		function abortProcesses(processesToAbort){
			    self.abortProcess.processes = processesToAbort;
			    return sdProcessInstanceService.abortProcesses( this.abortProcess.scope, this.abortProcess.processes);
			};
		
		/**
		 * 
		 */
		function confirm(scope) {
			self.abortProcesses(scope.processesToAbort).then(function(data) {
			    self.notification.result = data;
			    if (self.notification.result.failure.length > 0) {
				trace.debug("Failure in aborting activities ", self.notification.result.failure);
			    } else {
				trace.debug("Activities aborted sucessfully.");
			    }
			    self.abortCompleted(scope);
			    
			    self.closeThisDialog(scope);
			    
			}, function(data) {
			    self.notification.result = {};
			    self.notification.error = true;
			    self.abortCompleted(scope);
			    self.closeThisDialog(scope);
			    trace.error("Error in aborting activities.");
			});
		}
		
	   
	    /**
	     * 
	     */
		function hideDialog(){
			$scope.showDialog = false;
	    };
	    
	    /**
	     * 
	     */
	    function openDialog(){
	    	$scope.showDialog = true;
	    };
	    
	    $scope.abortProcessCtrl = self;
	}
       return directiveDefObject;
	}
})();
