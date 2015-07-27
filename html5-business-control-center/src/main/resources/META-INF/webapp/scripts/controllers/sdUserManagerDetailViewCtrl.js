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
					'sdLoggedInUserService', 'sdPreferenceService', 'sdDataTableHelperService',
					UserManagerDetailViewCtrl]);
	var _q;
	var _scope;
	var _element;
	var _sdUserManagerDetailService;
	var _sdViewUtilService;
	var trace;
	var _sdLoggedInUserService;
	var _sdPreferenceService;
	var _sdDataTableHelperService;
	/*
	 * 
	 */
	function UserManagerDetailViewCtrl($q, $scope, $element, sdUserManagerDetailService, sdLoggerService,
			sdViewUtilService, sdLoggedInUserService, sdPreferenceService, sdDataTableHelperService) {
		trace = sdLoggerService.getLogger('bcc-ui.sdUserManagerDetailViewCtrl');
		_q = $q;
		_scope = $scope;
		_element = $element;
		_sdUserManagerDetailService = sdUserManagerDetailService;
		_sdViewUtilService = sdViewUtilService;
		_sdLoggedInUserService = sdLoggedInUserService;
		_sdPreferenceService = sdPreferenceService;
		_sdDataTableHelperService = sdDataTableHelperService;

		this.columnSelector = _sdLoggedInUserService.getUserInfo().isAdministrator ? 'admin' : true;
		this.exportFileNameForAssignedRoles = "AssignedRoles";
		this.exportFileNameForAssignableRoles = "AssignableRoles";
		this.assignedRolesTable = null;
		this.assignableRolesTable = null;
		this.activityTable = null;
		this.rowSelectionAssignedRolesTable = null;
		this.rowSelectionAssignableRolesTable = null;
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
				self.assignedRolesTable.refresh();
				self.assignableRolesTable.refresh();
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

		result.list = _sdDataTableHelperService.columnSort(options, result.list);
		result.list = _sdDataTableHelperService.paginate(options, result.list);

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

		result.list = _sdDataTableHelperService.columnSort(options, result.list);
		result.list = _sdDataTableHelperService.paginate(options, result.list);

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
	 * @param rowSelectionAssignedRolesTable
	 */
	UserManagerDetailViewCtrl.prototype.removeRoleFromUser = function(rowSelectionAssignedRolesTable) {
		var self = this;
		var roleIds = self.getSelectedRoleIds(rowSelectionAssignedRolesTable);
		_sdUserManagerDetailService.removeRoleFromUser(roleIds, self.viewParams.userOid).then(function(data) {
			self.userAuthorizationMsg = data.userAuthorization;
			self.refresh();
		}, function(error) {
			trace.log(error);
		});
	};

	/**
	 * 
	 * @param rowSelectionAssignableRolesTable
	 */
	UserManagerDetailViewCtrl.prototype.addRoleToUser = function(rowSelectionAssignableRolesTable) {
		var self = this;
		var roleIds = self.getSelectedRoleIds(rowSelectionAssignableRolesTable);

		_sdUserManagerDetailService.addRoleToUser(roleIds, self.viewParams.userOid).then(function(data) {
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
	UserManagerDetailViewCtrl.prototype.getSelectedRoleIds = function(selectedRoles) {
		var roleIds = [];
		for ( var roleIndex in selectedRoles) {
			roleIds.push(selectedRoles[roleIndex].roleId);
		}
		return roleIds;
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