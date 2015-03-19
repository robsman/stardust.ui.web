/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
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
			[ '$q', '$scope', '$filter', 'sdProcessResourceMgmtService',
					'sdUtilService', 'sdLoggerService', 'sdViewUtilService',
					ProcessResourceMgmtCtrl ]);

	/*
	 * 
	 */
	function ProcessResourceMgmtCtrl($q, $scope, $filter,
			sdProcessResourceMgmtService, sdUtilService, sdLoggerService,
			sdViewUtilService) {
		var trace = sdLoggerService
				.getLogger('bcc-ui.sdProcessResourceMgmtCtrl');

		this.rolesTable = null;
		this.usersTable = null;
		this.columnSelector = 'admin';
		this.exportFileName = new Date();
		this.rowSelectionForRoles = null;
		this.rowSelectionForUsers = null;

		/**
		 * This method will load the available roles and users data.
		 */
		ProcessResourceMgmtCtrl.prototype.getProcessResourceRolesAndUsers = function() {
			var self = this;
			sdProcessResourceMgmtService
					.getProcessResourceRolesAndUsers()
					.then(
							function(data) {
								self.processResourceRoleList = data.processResourceRoleList;
								self.processResourceUserList = data.processResourceUserList;
							}, function(error) {
								trace.log(error);
							});
		};

		/**
		 * 
		 */
		ProcessResourceMgmtCtrl.prototype.initialize = function() {
			this.getProcessResourceRolesAndUsers();
		};

		this.initialize();

		/**
		 * 
		 * @param options
		 * @returns
		 */
		ProcessResourceMgmtCtrl.prototype.getProcessResourceRoles = function(
				options) {
			var self = this;
			var deferred = $q.defer();
			// TO DO : The deferred object is not needed here but as the sdDataTable is 
			// not working as expected with local mode, completed implementation with remote mode.
			self.ProcessResourceRoles = {};
			if (options.filters != undefined) {
				var rows = this.filterRolesArray(self.processResourceRoleList,
						options.filters.name.textSearch);
				self.ProcessResourceRoles.list = rows;
				self.ProcessResourceRoles.totalCount = rows.length;
			} else {
				self.ProcessResourceRoles.list = self.processResourceRoleList;
				self.ProcessResourceRoles.totalCount = self.processResourceRoleList.length;
			}
			deferred.resolve(self.ProcessResourceRoles);
			return deferred.promise;

		};
		/**
		 * 
		 */
		ProcessResourceMgmtCtrl.prototype.filterRolesArray = function(list,
				textSearch) {
			var rows = $filter('filter')(list, function(item, index) {
				var newTextSearch = textSearch.replaceAll("*", ".*");
				return item.name.match(new RegExp('^' + newTextSearch, "i"));
			}, true);

			return rows;

		};

		/**
		 * 
		 */
		ProcessResourceMgmtCtrl.prototype.filterUsersArray = function(list,
				textSearch) {
			var rows = $filter('filter')(
					list,
					function(item, index) {
						var newTextSearch = textSearch.replaceAll("*", ".*");
						return item.userName.match(new RegExp('^'
								+ newTextSearch, "i"));
					}, true);

			return rows;

		};

		/**
		 * 
		 */
		String.prototype.replaceAll = function(str1, str2, ignore) {
			return this.replace(new RegExp(str1.replace(
					/([\/\,\!\\\^\$\{\}\[\]\(\)\.\*\+\?\|\<\>\-\&])/g, "\\$&"),
					(ignore ? "gi" : "g")), (typeof (str2) == "string") ? str2
					.replace(/\$/g, "$$$$") : str2);
		}

		/**
		 * 
		 * @param options
		 * @returns
		 * 
		 */
		ProcessResourceMgmtCtrl.prototype.getProcessResourceUsers = function(
				options) {
			var self = this;
			// TO DO : The deferred object is not needed here but as the sdDataTable is 
			// not working as expected with local mode, completed implementation with remote mode.
			var deferred = $q.defer();
			self.ProcessResourceUsers = {};
			if (options.filters != undefined) {
				var rows = this.filterUsersArray(self.processResourceUserList,
						options.filters.userName.textSearch);
				self.ProcessResourceUsers.list = rows;
				self.ProcessResourceUsers.totalCount = rows.length;
			} else {
				self.ProcessResourceUsers.list = self.processResourceUserList;
				self.ProcessResourceUsers.totalCount = self.processResourceUserList.length;
			}
			deferred.resolve(self.ProcessResourceUsers);
			return deferred.promise;
		};

		/**
		 * 
		 */
		ProcessResourceMgmtCtrl.prototype.refresh = function() {
			var self = this;
			self.getProcessResourceRolesAndUsers();
			self.rolesTable.refresh();
			self.usersTable.refresh();
		};

		/**
		 * 
		 */
		ProcessResourceMgmtCtrl.prototype.openRoleManagerView = function(
				roleId, departmentOid, name) {
			sdViewUtilService.openView("roleManagerDetailView", "roleId="
					+ roleId, {
				"roleId" : "" + roleId,
				"departmentOid" : "" + departmentOid,
				"roleName" : "" + name
			}, true);
		};

		/**
		 * 
		 */
		ProcessResourceMgmtCtrl.prototype.openUserManagerView = function(
				userOid, userId) {
			sdViewUtilService.openView("userManagerDetailView", "userOid="
					+ userOid, {
				"userOid" : "" + userOid,
				"userId" : "" + userId
			}, true);
		};
	}
})();