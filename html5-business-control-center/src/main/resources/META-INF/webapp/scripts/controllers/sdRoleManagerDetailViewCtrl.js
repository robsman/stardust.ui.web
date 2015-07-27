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
			'sdRoleManagerDetailViewCtrl',
			['$q', '$scope', '$filter', '$element', 'sdRoleManagerDetailService', 'sdLoggerService',
					'sdViewUtilService', 'sdLoggedInUserService', 'sdPreferenceService', 'sdDataTableHelperService',
					RoleManagerDetailViewCtrl]);
	var _q;
	var _scope;
	var _filter;
	var _element;
	var _sdRoleManagerDetailService;
	var _sdViewUtilService;
	var trace;
	var _sdLoggedInUserService;
	var _sdPreferenceService;
	var _sdDataTableHelperService;

	/*
	 * 
	 */
	function RoleManagerDetailViewCtrl($q, $scope, $filter, $element, sdRoleManagerDetailService, sdLoggerService,
			sdViewUtilService, sdLoggedInUserService, sdPreferenceService, sdDataTableHelperService) {
		trace = sdLoggerService.getLogger('bcc-ui.sdRoleManagerDetailViewCtrl');
		_q = $q;
		_scope = $scope;
		_filter = $filter;
		_element = $element;
		_sdRoleManagerDetailService = sdRoleManagerDetailService;
		_sdViewUtilService = sdViewUtilService;
		_sdLoggedInUserService = sdLoggedInUserService;
		_sdPreferenceService = sdPreferenceService;
		_sdDataTableHelperService = sdDataTableHelperService;

		this.columnSelector = _sdLoggedInUserService.getUserInfo().isAdministrator ? 'admin' : true;
		this.exportFileNameForAssignedUsers = "AssignedUsers";
		this.exportFileNameForAssignableUsers = "AssignableUsers";
		this.assignedUsersTable = null;
		this.assignableUsersTable = null;
		this.activityTable = null;
		this.rowSelectionAssignedUsersTable = null;
		this.rowSelectionAssignableUsersTable = null;
		this.showActivityListTab = false;
		this.activeTab = 1;

		this.showLoggedInAssignedUsers = true;

		this.initialize();

		// Register for View Events
		_sdViewUtilService.registerForViewEvents(_scope, this.handleViewEvents, this);
	}

	/**
	 * 
	 * @param event
	 */
	RoleManagerDetailViewCtrl.prototype.handleViewEvents = function(event) {
		if (event.type == "ACTIVATED") {
			this.refreshOnlyActiveTab();
		} else if (event.type == "DEACTIVATED") {
			// TODO
		}
	};

	/**
	 * 
	 */
	RoleManagerDetailViewCtrl.prototype.getRoleManagerDetails = function() {
		var self = this;
		_sdRoleManagerDetailService.getRoleManagerDetails(self.viewParams.roleId, self.viewParams.departmentOid).then(
				function(data) {
					self.roleManagerDetails = data;
					if(self.assignedUsersTable != undefined && self.assignableUsersTable != undefined){
						self.assignedUsersTable.refresh();
						self.assignableUsersTable.refresh();
					}else{
						self.table1 = true;
						self.table2 = true;
					}
					
				}, function(error) {
					trace.log(error);
				});

	};

	/**
	 * 
	 */
	RoleManagerDetailViewCtrl.prototype.initialize = function() {
		var self = this;
		self.viewParams = _sdViewUtilService.getViewParams(_scope);
		self.getRoleManagerDetails();
	};

	/**
	 * 
	 * @returns
	 */
	RoleManagerDetailViewCtrl.prototype.getAssignedUsers = function(options) {
		var self = this;
		var deferred = _q.defer();
		var result = {
			list : self.roleManagerDetails.assignedUserList,
			totalCount : self.roleManagerDetails.assignedUserList.length
		}

		if (self.showLoggedInAssignedUsers) {

			var rows = _filter('filter')(self.roleManagerDetails.assignedUserList, {
				loggedIn : 'Yes'
			}, true);

			result.list = rows;
			result.totalCount = rows.length;
		}

		result.list = _sdDataTableHelperService.columnSort(options, result.list);
		result.list = _sdDataTableHelperService.paginate(options, result.list);

		deferred.resolve(result);
		return deferred.promise;
	};

	/**
	 * 
	 * @returns
	 */
	RoleManagerDetailViewCtrl.prototype.getAssignableUsers = function(options) {
		var self = this;
		var deferred = _q.defer();
		var result = {
			list : self.roleManagerDetails.assignableUserList,
			totalCount : self.roleManagerDetails.assignableUserList.length
		}

		if (self.showLoggedInAssignableUsers) {

			var rows = _filter('filter')(self.roleManagerDetails.assignableUserList, {
				loggedIn : 'Yes'
			}, true);

			result.list = rows;
			result.totalCount = rows.length;
		}

		result.list = _sdDataTableHelperService.columnSort(options, result.list);
		result.list = _sdDataTableHelperService.paginate(options, result.list);

		deferred.resolve(result);
		return deferred.promise;
	};

	/**
	 * 
	 */
	RoleManagerDetailViewCtrl.prototype.refresh = function() {
		var self = this;
		self.getRoleManagerDetails();
	};

	/**
	 * 
	 */
	RoleManagerDetailViewCtrl.prototype.refreshOnlyActiveTab = function() {
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
	RoleManagerDetailViewCtrl.prototype.refreshLoggedInAssignedUsers = function() {
		var self = this;
		self.assignedUsersTable.refresh();
	};

	/**
	 * 
	 */
	RoleManagerDetailViewCtrl.prototype.refreshLoggedInAssignableUsers = function() {
		var self = this;
		self.assignableUsersTable.refresh();
	};

	/**
	 * 
	 */
	RoleManagerDetailViewCtrl.prototype.removeUserFromRole = function(rowSelectionAssignedUsersTable) {
		var self = this;
		var userIds = this.getSelectedUserIds(rowSelectionAssignedUsersTable);
		_sdRoleManagerDetailService.removeUserFromRole(userIds, self.viewParams.roleId, self.viewParams.departmentOid)
				.then(function(data) {
					self.userAuthorizationMsg = data.userAuthorization;
					self.refresh();
				}, function(error) {
					trace.log(error);
				});

	};

	/**
	 * 
	 */
	RoleManagerDetailViewCtrl.prototype.addUserToRole = function(rowSelectionAssignableUsersTable) {
		var self = this;
		var userIds = this.getSelectedUserIds(rowSelectionAssignableUsersTable);
		_sdRoleManagerDetailService.addUserToRole(userIds, self.viewParams.roleId, self.viewParams.departmentOid).then(
				function(data) {
					self.userAuthorizationMsg = data.userAuthorization;
					self.refresh();
				}, function(error) {
					trace.log(error);
				});

	};

	/**
	 * 
	 */
	RoleManagerDetailViewCtrl.prototype.getSelectedUserIds = function(selectedUsers) {
		var userIds = [];
		for ( var assignedUser in selectedUsers) {
			userIds.push(selectedUsers[assignedUser].userOid);
		}
		return userIds;
	};

	/**
	 * 
	 */
	RoleManagerDetailViewCtrl.prototype.closeAlertMsg = function() {
		self.userAuthorizationMsg = false;
		self.dialogElem = _element.find('#alertMsg');
		self.dialogElem.detach();
	};

	/**
	 * 
	 */
	RoleManagerDetailViewCtrl.prototype.getActivitiesForRole = function(params) {

		var self = this;
		var deferred = _q.defer();
		self.activities = {};
		_sdRoleManagerDetailService.getAllActivitiesForRole(params, self.viewParams.roleId,
				self.viewParams.departmentOid).then(function(data) {
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
	RoleManagerDetailViewCtrl.prototype.showActivityList = function() {
		var self = this;
		self.showActivityListTab = true;
		self.activeTab = 2;

	};

	/**
	 * 
	 */
	RoleManagerDetailViewCtrl.prototype.showUserAssignment = function() {
		var self = this;
		self.activeTab = 1;

	};
	/**
	 * 
	 * @param prefInfo
	 * @returns preferenceStore
	 */
	RoleManagerDetailViewCtrl.prototype.preferenceForAssignedUserTable = function(prefInfo) {
		var preferenceStore = _sdPreferenceService
				.getStore(prefInfo.scope, 'ipp-business-control-center', 'preference'); // Override
		preferenceStore.marshalName = function(scope) {
			return "ipp-business-control-center.userAssigned.selectedColumns";
		}
		return preferenceStore;
	};

	/**
	 * 
	 * @param prefInfo
	 * @returns
	 */
	RoleManagerDetailViewCtrl.prototype.preferenceForAssignableUserTable = function(prefInfo) {
		var preferenceStore = _sdPreferenceService
				.getStore(prefInfo.scope, 'ipp-business-control-center', 'preference'); // Override
		preferenceStore.marshalName = function(scope) {
			return "ipp-business-control-center.userAssignable.selectedColumns";
		}
		return preferenceStore;
	};
})();