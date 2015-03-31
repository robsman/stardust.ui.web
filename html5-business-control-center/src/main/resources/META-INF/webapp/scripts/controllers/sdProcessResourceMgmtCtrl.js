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
			[ '$q', '$scope', '$filter', 'sdProcessResourceMgmtService', 'sdLoggerService', 'sdViewUtilService',
					ProcessResourceMgmtCtrl ]);

	/*
	 * 
	 */
	function ProcessResourceMgmtCtrl($q, $scope, $filter, sdProcessResourceMgmtService, sdLoggerService,
			sdViewUtilService) {
		var trace = sdLoggerService.getLogger('bcc-ui.sdProcessResourceMgmtCtrl');

		this.rolesTable = null;
		this.usersTable = null;
		this.columnSelector = 'admin';
		this.exportFileName = new Date();
		this.rowSelectionForRoles = null;
		this.rowSelectionForUsers = null;
		this.showRolesTable = true;

		/**
		 * 
		 * @param options
		 * @returns
		 */
		ProcessResourceMgmtCtrl.prototype.getProcessResourceRoles = function() {
			var deferred = $q.defer();
			var self = this;
			sdProcessResourceMgmtService.getProcessResourceRoles().then(function(data) {
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
		 */
		ProcessResourceMgmtCtrl.prototype.filterRolesArray = function(list, textSearch) {
			var rows = $filter('filter')(list, function(item, index) {
				var newTextSearch = textSearch.replaceAll("*", ".*");
				return item.name.match(new RegExp('^' + newTextSearch, "i"));
			}, true);

			return rows;

		};

		/**
		 * 
		 */
		ProcessResourceMgmtCtrl.prototype.filterUsersArray = function(list, textSearch) {
			var rows = $filter('filter')(list, function(item, index) {
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
		 * @param options
		 * @returns
		 * 
		 */
		ProcessResourceMgmtCtrl.prototype.getProcessResourceUsers = function() {
			var deferred = $q.defer();
			sdProcessResourceMgmtService.getProcessResourceUsers().then(function(data) {
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
		 */
		ProcessResourceMgmtCtrl.prototype.openRoleManagerView = function(roleId, departmentOid, name) {
			sdViewUtilService.openView("roleManagerDetailViewHtml5", "roleId=" + roleId, {
				"roleId" : "" + roleId,
				"departmentOid" : "" + departmentOid,
				"roleName" : "" + name
			}, true);
		};

		/**
		 * 
		 */
		ProcessResourceMgmtCtrl.prototype.openUserManagerView = function(userOid, userId) {
			sdViewUtilService.openView("userManagerDetailViewHtml5", "userOid=" + userOid, {
				"userOid" : "" + userOid,
				"userId" : "" + userId
			}, true);
		};
	}
})();