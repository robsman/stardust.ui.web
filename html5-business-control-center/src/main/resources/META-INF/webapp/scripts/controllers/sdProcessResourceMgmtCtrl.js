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
			'sdProcessResourceMgmtCtrl',
			['$q','sdProcessResourceMgmtService', 'sdLoggerService', 'sdViewUtilService',
					'sdCommonViewUtilService', 'sdLoggedInUserService', 'sdPreferenceService', ProcessResourceMgmtCtrl]);

	var _q;
	var _sdProcessResourceMgmtService;
	var _sdViewUtilService;
	var trace;
	var _sdCommonViewUtilService
	var _sdLoggedInUserService;
	var _sdPreferenceService;

	/**
	 * 
	 */
	function ProcessResourceMgmtCtrl($q, sdProcessResourceMgmtService, sdLoggerService, sdViewUtilService,
			sdCommonViewUtilService, sdLoggedInUserService, sdPreferenceService) {
		trace = sdLoggerService.getLogger('bcc-ui.sdProcessResourceMgmtCtrl');
		_q = $q;
		_sdProcessResourceMgmtService = sdProcessResourceMgmtService;
		_sdViewUtilService = sdViewUtilService;
		_sdCommonViewUtilService = sdCommonViewUtilService;
		_sdLoggedInUserService = sdLoggedInUserService;
		_sdPreferenceService = sdPreferenceService;

		this.rolesTable = null;
		this.usersTable = null;
		this.columnSelector = _sdLoggedInUserService.getUserInfo().isAdministrator ? 'admin' : true;
		this.exportFileNameForRolesAndOrg = "RolesAndOrgnizations"
		this.exportFileNameForUsers = "Users"

		this.getProcessResourceRolesData();

	}

	ProcessResourceMgmtCtrl.prototype.getProcessResourceRolesData = function() {
		var self = this;
		_sdProcessResourceMgmtService.getProcessResourceRoles().then(function(data) {
			self.processResourceRoleList = data.processResourceRoleList;

			_sdProcessResourceMgmtService.getProcessResourceUsers().then(function(data) {
				self.processResourceUserList = data.processResourceUserList;
				  if(self.rolesTable != undefined && self.usersTable != undefined){
					  self.rolesTable.refresh(true);
					  self.usersTable.refresh(true);
				  }else{
					  self.showRolesTable = true;
					  self.showUsersTable = true;
				  }
					
			}, function(error) {
				trace.log(error);
			});
		}, function(error) {
			trace.log(error);
		});
	};

	/**
	 * 
	 * @returns
	 */
	ProcessResourceMgmtCtrl.prototype.getProcessResourceRoles = function(options) {
		var deferred = _q.defer();
		var self = this;

		var result = {
			list : self.processResourceRoleList,
			totalCount : self.processResourceRoleList.length
		}

		deferred.resolve(result);
		return deferred.promise;
	};


	/**
	 * 
	 * @returns
	 */
	ProcessResourceMgmtCtrl.prototype.getProcessResourceUsers = function(options) {
		var deferred = _q.defer();
		var self = this;
		var result = {
			list : self.processResourceUserList,
			totalCount : self.processResourceUserList.length
		}

		deferred.resolve(result);
		return deferred.promise;
	};

	/**
	 * 
	 */
	ProcessResourceMgmtCtrl.prototype.refresh = function() {
		var self = this;
		self.getProcessResourceRolesData();
	};

	/**
	 * 
	 * @param roleId
	 * @param departmentOid
	 * @param name
	 */
	ProcessResourceMgmtCtrl.prototype.openRoleManagerView = function(roleId, departmentOid, name) {
		_sdViewUtilService.openView("roleManagerDetailView", "roleId=" + roleId, {
			"roleId" : "" + roleId,
			"departmentOid" : "" + departmentOid,
			"roleName" : "" + name
		}, true);
	};

	/**
	 * 
	 * @param userOid
	 * @param userId
	 */
	ProcessResourceMgmtCtrl.prototype.openUserManagerView = function(userOid, userId) {
		_sdCommonViewUtilService.openUserManagerDetailView(userOid, userId, true);
	};

	/**
	 * 
	 */

	ProcessResourceMgmtCtrl.prototype.preferenceDelegateForRolesTable = function(prefInfo) {
		var preferenceStore = _sdPreferenceService
				.getStore(prefInfo.scope, 'ipp-business-control-center', 'preference'); // Override
		preferenceStore.marshalName = function(scope) {
			return "ipp-business-control-center.ProcessResourceRoleMgmt.selectedColumns";
		}
		return preferenceStore;
	};

	ProcessResourceMgmtCtrl.prototype.preferenceDelegateForUsersTable = function(prefInfo) {
		var preferenceStore = _sdPreferenceService
				.getStore(prefInfo.scope, 'ipp-business-control-center', 'preference'); // Override
		preferenceStore.marshalName = function(scope) {
			return "ipp-business-control-center.ProcessResourceUserMgmt.selectedColumns";
		}
		return preferenceStore;
	};
})();