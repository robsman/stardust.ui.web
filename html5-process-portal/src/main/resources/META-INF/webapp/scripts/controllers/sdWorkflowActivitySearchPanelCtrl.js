/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Abhay.Thappan
 */
(function() {
	'use strict';
	
	angular.module("workflow-ui").controller(
			'sdWorkflowActivitySearchPanelCtrl',
			['sdLoggerService', 'sdViewUtilService',
					'sdI18nService', 'sdActivitySearchService', 
					'sdUtilService', 'sdWorklistConstants', WorkflowActivitySearchPanelCtrl ]);
	var _sdViewUtilService;
	var trace;
	var _sdActivitySearchService;
	var _sdI18nService;
	var _sdUtilService;
	var _sdWorklistConstants;
	/**
	 * 
	 */
	function WorkflowActivitySearchPanelCtrl(sdLoggerService, sdViewUtilService, sdI18nService, 
			sdActivitySearchService, sdUtilService, sdWorklistConstants) {
		trace = sdLoggerService.getLogger('workflow-ui.sdWorkflowActivitySearchPanelCtrl');
		_sdViewUtilService = sdViewUtilService;
		_sdI18nService = sdI18nService;
		_sdActivitySearchService = sdActivitySearchService;
		_sdUtilService = sdUtilService;
		_sdWorklistConstants = sdWorklistConstants;
		
		this.allActivitiesParams = {
				'name' : _sdI18nService.translate("processportal.launchPanels-activitySearch-allActivities"),
				'id' : "allActivityInstances",
				'type' : _sdWorklistConstants.Types.ALIVE
		};
		
		this.allResubmissionActParams = {
				'name' : _sdI18nService.translate("processportal.launchPanels-activitySearch-resubmission"),
				'id' : "allResubmissionInstances",
				'type' : _sdWorklistConstants.Types.RESUBMISSION
		};
		
		this.worklistMap = {};
		this.worklistMapOfParams = {};
		this.firstName = '';
		this.lastName = '';
		this.users = [];
		this.index = 1;
	}

	/**
	 * 
	 */
	WorkflowActivitySearchPanelCtrl.prototype.getAllResubmissionActivityInstances = function() {
		var self = this;
		_sdActivitySearchService.getAllResubmissionActivityInstances().then(function(data) {
			self.worklistMap['allResubmissionInstances'] = data.totalCount;
            //checking in map if search for resubmission is already created or not
			if(self.worklistMapOfParams['allResubmissionInstances'] == undefined){
				self.allResubmissionActParams.index = self.index;
				self.worklistMapOfParams['allResubmissionInstances'] = self.allResubmissionActParams;
				self.index++;
			}			
			self.openWorklistView("allResubmissionInstances",self.allResubmissionActParams);
		}, function(error) {
			trace.error(error);
		});
	};
	
	/**
	 * 
	 */
	WorkflowActivitySearchPanelCtrl.prototype.getAllActivityInstances = function() {
		var self = this;
		_sdActivitySearchService.getAllActivityInstances().then(function(data) {
			self.worklistMap['allActivityInstances'] = data.totalCount;
            //checking in map if search for all available activities is already created or not
			if(self.worklistMapOfParams['allActivityInstances'] == undefined){
				self.allActivitiesParams.index = self.index;
				self.worklistMapOfParams['allActivityInstances'] = self.allActivitiesParams;
				self.index++;
			}
			
			self.openWorklistView("allActivityInstances",self.allActivitiesParams);
		}, function(error) {
			trace.error(error);
		});
	};
	
	/**
	 * 
	 */
	WorkflowActivitySearchPanelCtrl.prototype.getWorklistForUser = function(userOID, userId, name) {
		var self = this;
		_sdActivitySearchService.getWorklistForUser(userOID).then(function(data) {
			var id = "userWorklistSearch" + userOID;
			self.worklistMap[id] = data.totalCount;
			var worklistForUserParams = {
					"queryParams": "fetchAllStates=true",
					"type" : _sdWorklistConstants.Types.ALL
			};
			
			worklistForUserParams.id = id;			
			worklistForUserParams.name = name;
			worklistForUserParams.userId = userId;
            //checking in map if search for perticular item is already created or not
			if(self.worklistMapOfParams[id] == undefined){
				worklistForUserParams.index = self.index;
				self.worklistMapOfParams[id] = worklistForUserParams;
				self.index++;
			}
			
			self.openWorklistView(id, worklistForUserParams);
		}, function(error) {
			trace.error(error);
		});
	};
	
	/**
	 * 
	 */
	WorkflowActivitySearchPanelCtrl.prototype.getUsersByCriteria = function() {
		var self = this;
		_sdActivitySearchService.getUsersByCriteria(self.firstName, self.lastName).then(function(data) {
			self.users = data.list;
		}, function(error) {
			trace.error(error);
		});
	};
	
	/**
	 * 
	 */
	WorkflowActivitySearchPanelCtrl.prototype.clearUsers = function() {
		var self = this;
		self.users = [];
		self.firstName = '';
		self.lastName = '';
	};
	
	WorkflowActivitySearchPanelCtrl.prototype.openWorklistView = function(id, params){
		 var worklistParams = angular.copy(params);
         delete worklistParams.index;
		_sdViewUtilService.openView("worklistPanel","id=" +id, worklistParams);
	};
	
	WorkflowActivitySearchPanelCtrl.prototype.clearSearch = function(){
		var self = this;
		self.worklistMap = {};
		self.worklistMapOfParams = {};
		self.index = 1;
	};
	
	WorkflowActivitySearchPanelCtrl.prototype.worklistMapNotEmpty = function(){
		var self = this;
		return !_sdUtilService.isEmpty(self.worklistMap);
	};

})();
