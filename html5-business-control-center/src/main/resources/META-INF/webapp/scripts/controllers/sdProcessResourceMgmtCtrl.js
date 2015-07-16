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
			['$q', '$filter', 'sdProcessResourceMgmtService', 'sdLoggerService', 'sdViewUtilService',
					'sdCommonViewUtilService', 'sdLoggedInUserService', 'sdPreferenceService',
					'sdDataTableHelperService', ProcessResourceMgmtCtrl]);

	var _q;
	var _filter;
	var _sdProcessResourceMgmtService;
	var _sdViewUtilService;
	var trace;
	var _sdCommonViewUtilService
	var _sdLoggedInUserService;
	var _sdPreferenceService;
	var _sdDataTableHelperService;

	/**
	 * 
	 */
	function ProcessResourceMgmtCtrl($q, $filter, sdProcessResourceMgmtService, sdLoggerService, sdViewUtilService,
			sdCommonViewUtilService, sdLoggedInUserService, sdPreferenceService, sdDataTableHelperService) {
		trace = sdLoggerService.getLogger('bcc-ui.sdProcessResourceMgmtCtrl');
		_q = $q;
		_filter = $filter;
		_sdProcessResourceMgmtService = sdProcessResourceMgmtService;
		_sdViewUtilService = sdViewUtilService;
		_sdCommonViewUtilService = sdCommonViewUtilService;
		_sdLoggedInUserService = sdLoggedInUserService;
		_sdPreferenceService = sdPreferenceService;
		_sdDataTableHelperService = sdDataTableHelperService;

		this.rolesTable = null;
		this.usersTable = null;
		this.columnSelector = _sdLoggedInUserService.getUserInfo().isAdministrator ? 'admin' : true;
		this.exportFileNameForRolesAndOrg = "RolesAndOrgnizations"
		this.exportFileNameForUsers = "Users"
		this.rowSelectionForRoles = null;
		this.rowSelectionForUsers = null;

		this.getProcessResourceRolesData();

	}

	ProcessResourceMgmtCtrl.prototype.getProcessResourceRolesData = function() {
		var self = this;
		_sdProcessResourceMgmtService.getProcessResourceRoles().then(function(data) {
			self.processResourceRoleList = data.processResourceRoleList;

			_sdProcessResourceMgmtService.getProcessResourceUsers().then(function(data) {
				self.processResourceUserList = data.processResourceUserList;
					self.showRolesTable = true;
					self.showUsersTable = true;
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
			list : [],
			totalCount : self.processResourceRoleList.length
		}
		if (options.filters != undefined) {
			var rows = this.filterRolesArray(self.processResourceRoleList, options.filters.name.textSearch);
			result.list = rows;
			result.totalCount = rows.length;
		} else {
			result.list = self.processResourceRoleList;
		}

		result.list = _sdDataTableHelperService.columnSort(options, result.list);
		result.list = _sdDataTableHelperService.paginate(options, result.list);

		deferred.resolve(result);
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
		return this.replace(new RegExp(str1.replace(/([\/\,\!\\\^\$\{\}\[\]\(\)\.\*\+\?\|\<\>\-\&])/g, "\\$&"), (ignore
				? "gi"
				: "g")), (typeof (str2) == "string") ? str2.replace(/\$/g, "$$$$") : str2);
	}

	/**
	 * 
	 * @returns
	 */
	ProcessResourceMgmtCtrl.prototype.getProcessResourceUsersData = function() {
		_sdProcessResourceMgmtService.getProcessResourceUsers().then(function(data) {
			self.processResourceUserList = data.processResourceUserList;

			if (self.usersTable != undefined) {
				self.usersTable.refresh();
			} else {
				self.showUsersTable = true;
			}
		}, function(error) {
			trace.log(error);
		});
	};

	/**
	 * 
	 * @returns
	 */
	ProcessResourceMgmtCtrl.prototype.getProcessResourceUsers = function(options) {
		var deferred = _q.defer();
		var self = this;
		var result = {
			list : [],
			totalCount : self.processResourceUserList.length
		}
		if (options.filters != undefined) {
			var rows = this.filterUsersArray(self.processResourceUserList, options.filters.userName.textSearch);
			result.list = rows;
			result.totalCount = rows.length;
		} else {
			result.list = self.processResourceUserList;
			//result.totalCount = self.processResourceUserList.length;
		}

		result.list = _sdDataTableHelperService.columnSort(options, result.list);
		result.list = _sdDataTableHelperService.paginate(options, result.list);
		deferred.resolve(result);
		return deferred.promise;
	};

	/**
	 * 
	 */
	ProcessResourceMgmtCtrl.prototype.refresh = function() {
		var self = this;
		self.showRolesTable = false;
		self.showUsersTable = false;
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