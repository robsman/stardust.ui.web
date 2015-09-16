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
			'sdRoleAssignmentCtrl',
			['$q', '$filter', '$timeout', 'sdRoleAssignmentService', 'sdLoggerService', 'sdCommonViewUtilService',
					'sdLoggedInUserService', 'sdPreferenceService', 'sdDataTableHelperService', RoleAssignmentCtrl]);

	var _q;
	var _sdRoleAssignmentService;
	var _sdCommonViewUtilService;
	var trace;
	var _timeout;
	var _sdLoggedInUserService;
	var _sdPreferenceService;
	var _sdDataTableHelperService;
	var _filter;

	/**
	 * 
	 */
	function RoleAssignmentCtrl($q, $filter, $timeout, sdRoleAssignmentService, sdLoggerService,
			sdCommonViewUtilService, sdLoggedInUserService, sdPreferenceService, sdDataTableHelperService) {
		trace = sdLoggerService.getLogger('bcc-ui.sdRoleAssignmentCtrl');
		_q = $q;
		_sdRoleAssignmentService = sdRoleAssignmentService;
		_sdCommonViewUtilService = sdCommonViewUtilService;
		_timeout = $timeout;
		_sdLoggedInUserService = sdLoggedInUserService;
		_sdPreferenceService = sdPreferenceService;
		_sdDataTableHelperService = sdDataTableHelperService;
		_filter = $filter;

		this.roleAssignmentTable = null;
		this.columnSelector = _sdLoggedInUserService.getUserInfo().isAdministrator ? 'admin' : true;
		this.exportFileNameForRoleAssignment = "RoleAssignment"
		this.rowSelectionForRoleAssignment = null;
		this.getRoleAssignments();
	}

	/**
	 * 
	 * @returns
	 */
	RoleAssignmentCtrl.prototype.getRoleAssignments = function() {
		var self = this;
		self.roleAssignments = {};
		_sdRoleAssignmentService.getRoleAssignments().then(function(data) {
			self.roleAssignments.list = data.list;
			self.roleAssignments.totalCount = data.totalCount;
			self.columns = data.columns;
			self.columnsLabelMap = data.columnsDefinition;
			self.showRoleAssignmentTable = true;
			_timeout(function() {
				self.createTable = true;
			}, 0, true);
		}, function(error) {
			trace.log(error);
		});
	};

	/**
	 * 
	 * @returns
	 */
	RoleAssignmentCtrl.prototype.getRoleAssignmentData = function(options) {
		var self = this;
		var result = {
			list : self.roleAssignments.list,
			totalCount : self.roleAssignments.totalCount
		}
		
		return result;
	};

	/**
	 * 
	 * @param list
	 * @param textSearch
	 * @returns
	 */
	RoleAssignmentCtrl.prototype.filterRoleAssignments = function(list, textSearch) {
		var rows = _filter('filter')(list, function(item, index) {
			var newTextSearch = textSearch.replaceAll("*", ".*");
			return item.teamMember.match(new RegExp('^' + newTextSearch, "i"));
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
	 */
	RoleAssignmentCtrl.prototype.refresh = function() {
		var self = this;
		self.createTable = false;
		self.getRoleAssignments();
	};

	/**
	 * 
	 * @param userOid
	 * @param userId
	 */
	RoleAssignmentCtrl.prototype.openUserManagerView = function(userOid, userId) {
		_sdCommonViewUtilService.openUserManagerDetailView(userOid, userId, true);
	};

	/**
	 * 
	 * @param colValue
	 * @returns {String}
	 */
	RoleAssignmentCtrl.prototype.getExportValue = function(colValue) {
		if (colValue == true) {
			return 'Yes';
		} else {
			return 'No';
		}
	};

	/**
	 * 
	 */

	RoleAssignmentCtrl.prototype.preferenceDelegate = function(prefInfo) {
		var preferenceStore = _sdPreferenceService
				.getStore(prefInfo.scope, 'ipp-business-control-center', 'preference'); // Override
		preferenceStore.marshalName = function(scope) {
			return "ipp-business-control-center.roleAssignment.selectedColumns";
		}
		return preferenceStore;
	};
})();