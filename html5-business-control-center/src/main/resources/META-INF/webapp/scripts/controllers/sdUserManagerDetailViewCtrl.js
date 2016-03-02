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

	angular.module("bcc-ui").controller(
			'sdUserManagerDetailViewCtrl',
			['$q', '$scope', '$element', 'sdUserManagerDetailService', 'sdLoggerService', 'sdViewUtilService',
					'sdLoggedInUserService', 'sdPreferenceService',
					UserManagerDetailViewCtrl]);
	var _q;
	var _scope;
	var _element;
	var _sdUserManagerDetailService;
	var _sdViewUtilService;
	var trace;
	var _sdLoggedInUserService;
	var _sdPreferenceService;
	/*
	 * 
	 */
	function UserManagerDetailViewCtrl($q, $scope, $element, sdUserManagerDetailService, sdLoggerService,
			sdViewUtilService, sdLoggedInUserService, sdPreferenceService) {
		trace = sdLoggerService.getLogger('bcc-ui.sdUserManagerDetailViewCtrl');
		_q = $q;
		_scope = $scope;
		_element = $element;
		_sdUserManagerDetailService = sdUserManagerDetailService;
		_sdViewUtilService = sdViewUtilService;
		_sdLoggedInUserService = sdLoggedInUserService;
		_sdPreferenceService = sdPreferenceService;

		this.columnSelector = _sdLoggedInUserService.getUserInfo().isAdministrator ? 'admin' : true;
		this.exportFileNameForAssignedRoles = "AssignedRoles";
		this.exportFileNameForAssignableRoles = "AssignableRoles";
		this.assignedRolesTable = null;
		this.assignableRolesTable = null;
		this.activityTable = null;
		this.showActivityListTab = false;
		this.activeTab = 1;

		this.initialize();

		// Register for View Events
		_sdViewUtilService.registerForViewEvents(_scope, this.handleViewEvents, this);

	}

	/**
	 * 
	 * @param event
	 */
	UserManagerDetailViewCtrl.prototype.handleViewEvents = function(event) {
		if (event.type == "ACTIVATED") {
			this.refreshOnlyActiveTab();
		} else if (event.type == "DEACTIVATED") {
			// TODO
		}
	};

	/**
	 * 
	 */
	UserManagerDetailViewCtrl.prototype.getUserManagerDetails = function() {
		var self = this;
		_sdUserManagerDetailService.getUserManagerDetails(self.viewParams.userOid).then(function(data) {
			self.userManagerDetails = data;
			if(self.assignedRolesTable != undefined && self.assignableRolesTable != undefined){
				self.assignedRolesTable.refresh(true);
				self.assignableRolesTable.refresh(true);
			}else{
				self.showAssignedRolesTable = true;
				self.showAssignableRolesTable = true;
			}
			
		}, function(error) {
			trace.log(error);
		});

	};

	/**
	 * 
	 */
	UserManagerDetailViewCtrl.prototype.initialize = function() {
		var self = this;
		self.viewParams = _sdViewUtilService.getViewParams(_scope);
		self.getUserManagerDetails();
	};

	/**
	 * 
	 */
	UserManagerDetailViewCtrl.prototype.getAssignedRoles = function(options) {
		var self = this;
		var deferred = _q.defer();
		var result = {
			list : self.userManagerDetails.assignedRoleList,
			totalCount : self.userManagerDetails.assignedRoleList.length
		}

		deferred.resolve(result);
		return deferred.promise;
	};

	/**
	 * 
	 */
	UserManagerDetailViewCtrl.prototype.getAssignableRoles = function(options) {
		var self = this;
		var deferred = _q.defer();
		var result = {
			list : self.userManagerDetails.assignableRoleList,
			totalCount : self.userManagerDetails.assignableRoleList.length
		}

		deferred.resolve(result);
		return deferred.promise;
	};

	/**
	 * 
	 */
	UserManagerDetailViewCtrl.prototype.refresh = function() {
		var self = this;
		self.getUserManagerDetails();
	};

	/**
	 * 
	 */
	UserManagerDetailViewCtrl.prototype.refreshOnlyActiveTab = function() {
		var self = this;
		if (self.activeTab == 1) {
			self.refresh();
		} else {
			self.activityTable.refresh();
		}
	};

	/**
	 * 
	 */
	UserManagerDetailViewCtrl.prototype.removeRoleFromUser = function() {
		var self = this;
		var roles = self.getSelectedRoles(self.assignedRolesTable.getSelection());
		_sdUserManagerDetailService.removeRoleFromUser(roles, self.viewParams.userOid).then(function(data) {
			self.userAuthorizationMsg = data.userAuthorization;
			self.refresh();
		}, function(error) {
			trace.log(error);
		});
	};

	/**
	 * 
	 */
	UserManagerDetailViewCtrl.prototype.addRoleToUser = function() {
		var self = this;
		var roles = self.getSelectedRoles(self.assignableRolesTable.getSelection());

		_sdUserManagerDetailService.addRoleToUser(roles, self.viewParams.userOid).then(function(data) {
			self.userAuthorizationMsg = data.userAuthorization;
			self.refresh();
		}, function(error) {
			trace.log(error);
		});

	};

	/**
	 * 
	 * @param selectedRoles
	 * @returns
	 */
	UserManagerDetailViewCtrl.prototype.getSelectedRoles = function(selectedRoles) {
		var roles = [];
		for ( var roleIndex in selectedRoles) {
			roles.push({ 
				        'roleId':selectedRoles[roleIndex].roleId, 
				        'departmentOid': selectedRoles[roleIndex].departmentOid
				      });
		}
		return roles;
	};

	/**
	 * 
	 */
	UserManagerDetailViewCtrl.prototype.closeAlertMsg = function() {
		self.userAuthorizationMsg = false;
		self.dialogElem = _element.find('#alertMsg');
		self.dialogElem.detach();
	};

	/**
	 * 
	 * @param params
	 * @returns
	 */
	UserManagerDetailViewCtrl.prototype.getActivitiesForUser = function(params) {

		var self = this;
		var deferred = _q.defer();
		self.activities = {};
		_sdUserManagerDetailService.getAllActivitiesForUser(params, self.viewParams.userOid).then(function(data) {
			self.activities.list = data.list;
			self.activities.totalCount = data.totalCount;
			deferred.resolve(self.activities);
		}, function(error) {
			deferred.reject(error);
		});

		return deferred.promise;
	};

	/**
	 * 
	 */
	UserManagerDetailViewCtrl.prototype.showActivityList = function() {
		var self = this;
		self.showActivityListTab = true;
		self.activeTab = 2;

	};

	/**
	 * 
	 */
	UserManagerDetailViewCtrl.prototype.showRoleAssignment = function() {
		var self = this;
		self.activeTab = 1;

	};

	/**
	 * 
	 */

	UserManagerDetailViewCtrl.prototype.preferenceForAssignedRoleTable = function(prefInfo) {
		var preferenceStore = _sdPreferenceService
				.getStore(prefInfo.scope, 'ipp-business-control-center', 'preference'); // Override
		preferenceStore.marshalName = function(scope) {
			return "ipp-business-control-center.roleAssigned.selectedColumns";
		}
		return preferenceStore;
	};

	UserManagerDetailViewCtrl.prototype.preferenceForAssignableRoleTable = function(prefInfo) {
		var preferenceStore = _sdPreferenceService
				.getStore(prefInfo.scope, 'ipp-business-control-center', 'preference'); // Override
		preferenceStore.marshalName = function(scope) {
			return "ipp-business-control-center.roleAssignable.selectedColumns";
		}
		return preferenceStore;
	};

})();