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
 * @author Johnson.Quadras
 */
(function(){
	'use strict';

	angular.module('bpm-common').directive( 'sdAbortActivityDialog', [ 'sdActivityInstanceService','sdLoggerService', 'sdUtilService', 'sdWorkflowPerspectiveConfigService', AbortActivity]);

	var trace;
	/**
	 * 
	 */
	function AbortActivity(sdActivityInstanceService, sdLoggerService, sdUtilService, sdWorkflowPerspectiveConfigService){

		var directiveDefObject = {
			restrict: 'A',
			scope :{
				activitiesToAbort : '=sdaActivitiesToAbort',
				showDialog : '=sdaShowDialog',
				abortCompleted: '&sdaOnAbortComplete'
			},
			template: '<div  sd-dialog  '+
							'sda-show="showDialog" '+
							'sda-type="custom" '+
							'sda-title="{{abortActivityCtrl.i18n(\'views-common-messages.views-common-activity-abortActivity-label\')}}" '+
							'sda-scope="this" '+
							'sda-on-open="abortActivityCtrl.onOpenDialog(res)"' +
							'sda-template="' +
							 sdUtilService.getBaseUrl() + 'plugins/html5-process-portal/scripts/directives/partials/abortActivityDialogBody.html"> '+
					 '<\/div> ',
			
			controller: AbortActivityController
		};

	/**
	 * 
	 */
   function AbortActivityController($attrs, $scope, $element){

	    var self = this;
	    trace = sdLoggerService.getLogger('bpm-common.sdAbortActivityDialog');
	    
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
		    var abortScope =  sdWorkflowPerspectiveConfigService.getAbortActivityScope();
		    var isPromptRequired = false;
		    if(abortScope == ''){
			isPromptRequired = true;
			self.configuredScope = 'SubHierarchy'; 
		    }else{
			self.configuredScope = abortScope;
		    } 
		    
		    
		    self.abortActivity = {
			    scope : abortScope,
			    activities : [],
			    isPromptRequired : isPromptRequired
		    };
		    
		    
		    self.onOpenDialog = onOpenDialog;
		    self.closeThisDialog = closeThisDialog;
		    self.resetValues = resetValues;
		    self.abortActivities = abortActivities;
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
		    self.abortActivity.scope = self.configuredScope;
		    self.notification.result = {};
		    self.notification.error = false;
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
		function abortActivities(activitiesToAbort){
			    self.abortActivity.activities = activitiesToAbort;
			    trace.debug("Aborting activities with following params ",self.abortActivity.scope, self.abortActivity.activities);
			    return sdActivityInstanceService.abortActivities( self.abortActivity.scope, self.abortActivity.activities);
			};
		
		/**
		 * 
		 */
		function confirm(scope) {
			self.abortActivities(scope.activitiesToAbort).then(function(data) {
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
	    
	    $scope.abortActivityCtrl = self;
	}
       return directiveDefObject;
	}
})();
