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

	angular.module("bcc-ui")
			.controller(
					'sdProcessResourceMgmtCtrl',
					[ '$q', '$filter', 'sdProcessResourceMgmtService', 'sdLoggerService', 'sdViewUtilService',
							'sdCommonViewUtilService', 'sdLoggedInUserService', 'sdPreferenceService',
							ProcessResourceMgmtCtrl ]);

	var _q;
	var _filter;
	var _sdProcessResourceMgmtService;
	var _sdViewUtilService;
	var trace;
	var _sdCommonViewUtilService
	var _sdLoggedInUserService;
	var _sdPreferenceService;

	/**
	 * 
	 */
	function ProcessResourceMgmtCtrl($q, $filter, sdProcessResourceMgmtService, sdLoggerService, sdViewUtilService,
			sdCommonViewUtilService, sdLoggedInUserService, sdPreferenceService) {
		trace = sdLoggerService.getLogger('bcc-ui.sdProcessResourceMgmtCtrl');
		_q = $q;
		_filter = $filter;
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
		this.rowSelectionForRoles = null;
		this.rowSelectionForUsers = null;
		this.showRolesTable = true;
	}

	/**
	 * 
	 * @returns
	 */
	ProcessResourceMgmtCtrl.prototype.getProcessResourceRoles = function() {
		var deferred = _q.defer();
		var self = this;
		_sdProcessResourceMgmtService.getProcessResourceRoles().then(function(data) {
			deferred.resolve(data.processResourceRoleList);
			self.showUsersTable = true;
			if (self.usersTable != undefined) {
				self.usersTable.refresh();
			}
		}, function(error) {
			trace.log(error);
			deferred.reject(error);
		});
		return deferred.promise;
	};

	/**
	 * 
	 * @param list
	 * @param textSearch
	 * @returns
	 */
	ProcessResourceMgmtCtrl.prototype.filterRolesArray = function(list, textSearch) {
		var rows = _filter('filter')(list, function(item, index) {
			var newTextSearch = textSearch.replaceAll("*", ".*");
			return item.name.match(new RegExp('^' + newTextSearch, "i"));
		}, true);

		return rows;

	};

	/**
	 * 
	 * @param list
	 * @param textSearch
	 * @returns
	 */
	ProcessResourceMgmtCtrl.prototype.filterUsersArray = function(list, textSearch) {
		var rows = _filter('filter')(list, function(item, index) {
			var newTextSearch = textSearch.replaceAll("*", ".*");
			return item.userName.match(new RegExp('^' + newTextSearch, "i"));
		}, true);

		return rows;

	};

	/**
	 * 
	 */
	String.prototype.replaceAll = function(str1, str2, ignore) {
		return this.replace(new RegExp(str1.replace(/([\/\,\!\\\^\$\{\}\[\]\(\)\.\*\+\?\|\<\>\-\&])/g, "\\$&"),
				(ignore ? "gi" : "g")), (typeof (str2) == "string") ? str2.replace(/\$/g, "$$$$") : str2);
	}

	/**
	 * 
	 * @returns
	 */
	ProcessResourceMgmtCtrl.prototype.getProcessResourceUsers = function() {
		var deferred = _q.defer();
		_sdProcessResourceMgmtService.getProcessResourceUsers().then(function(data) {
			deferred.resolve(data.processResourceUserList);
		}, function(error) {
			trace.log(error);
			deferred.reject(error);
		});
		return deferred.promise;
	};

	/**
	 * 
	 */
	ProcessResourceMgmtCtrl.prototype.refresh = function() {
		var self = this;
		self.rolesTable.refresh();
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
		var preferenceStore = _sdPreferenceService.getStore(prefInfo.scope, 'ipp-business-control-center', 'preference'); // Override
		preferenceStore.marshalName = function(scope) {
			return "ipp-business-control-center.ProcessResourceRoleMgmt.selectedColumns";
		}
		return preferenceStore;
	};

	ProcessResourceMgmtCtrl.prototype.preferenceDelegateForUsersTable = function(prefInfo) {
		var preferenceStore = _sdPreferenceService.getStore(prefInfo.scope, 'ipp-business-control-center', 'preference'); // Override
		preferenceStore.marshalName = function(scope) {
			return "ipp-business-control-center.ProcessResourceUserMgmt.selectedColumns";
		}
		return preferenceStore;
	};
})();