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
			'sdRoleManagerDetailViewCtrl',
			[ '$q', '$scope', '$filter', '$element',
					'sdProcessResourceMgmtService', 'sdUtilService',
					'sdLoggerService', 'sdViewUtilService',
					RoleManagerDetailViewCtrl ]);

	/*
	 * 
	 */
	function RoleManagerDetailViewCtrl($q, $scope, $filter, $element,
			sdProcessResourceMgmtService, sdUtilService, sdLoggerService,
			sdViewUtilService) {
		var trace = sdLoggerService
				.getLogger('bcc-ui.sdRoleManagerDetailViewCtrl');

		this.columnSelector = 'admin';
		this.exportFileName = new Date();
		this.assignedUsersTable = null;
		this.assignableUsersTable = null;
		this.activityTable = null;
		this.rowSelectionAssignedUsersTable = null;
		this.rowSelectionAssignableUsersTable = null;
		this.showActivityListTab = false;
		this.activeTab = 1;

		this.showLoggedInAssignedUsers = true;

		RoleManagerDetailViewCtrl.prototype.handleViewEvents = function(event) {
			if (event.type == "ACTIVATED") {
				this.refreshOnlyActiveTab();
			} else if (event.type == "DEACTIVATED") {
				// TODO
			}
		};

		// Register for View Events
		sdViewUtilService.registerForViewEvents($scope, this.handleViewEvents,
				this);

		/**
		 * 
		 */
		RoleManagerDetailViewCtrl.prototype.getRoleManagerDetails = function() {
			var self = this;
			sdProcessResourceMgmtService
					.getRoleManagerDetails(self.viewParams.roleId,
							self.viewParams.departmentOid)
					.then(
							function(data) {
								self.roleManagerDetails = data;
								self.table1 = true;
								self.table2 = true;
								if (self.assignedUsersTable != undefined
										&& self.assignableUsersTable != undefined) {
									self.assignedUsersTable.refresh();
									self.assignableUsersTable.refresh();
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
			self.viewParams = sdViewUtilService.getViewParams($scope);
			self.getRoleManagerDetails();
		};

		this.initialize();

		/**
		 * 
		 */
		RoleManagerDetailViewCtrl.prototype.getAssignedUsers = function(options) {
			var self = this;
			var deferred = $q.defer();
			self.assignedUsers = {};
			if (self.showLoggedInAssignedUsers) {

				var rows = $filter('filter')(
						self.roleManagerDetails.assignedUserList, {
							loggedIn : 'Yes'
						}, true);

				self.assignedUsers.list = rows;
				self.assignedUsers.totalCount = rows.length;

			} else {
				self.assignedUsers.list = self.roleManagerDetails.assignedUserList;
				self.assignedUsers.totalCount = self.roleManagerDetails.assignedUserList.length;
			}

			deferred.resolve(self.assignedUsers);
			return deferred.promise;
			// return self.roleManagerDetails.assignedUserList;
		};

		/**
		 * 
		 */
		RoleManagerDetailViewCtrl.prototype.getAssignableUsers = function() {
			var self = this;
			var deferred = $q.defer();
			self.assignableUsers = {};

			if (self.showLoggedInAssignableUsers) {

				var rows = $filter('filter')(
						self.roleManagerDetails.assignableUserList, {
							loggedIn : 'Yes'
						}, true);

				self.assignableUsers.list = rows;
				self.assignableUsers.totalCount = rows.length;

			} else {
				self.assignableUsers.list = self.roleManagerDetails.assignableUserList;
				self.assignableUsers.totalCount = self.roleManagerDetails.assignableUserList.length;
			}

			deferred.resolve(self.assignableUsers);
			return deferred.promise;
			// return self.roleManagerDetails.assignableUserList;
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
				self.getRoleManagerDetails();
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
			/*
			 * if(self.showLoggedInAssignedUsers){
			 *  }
			 */
		};

		/**
		 * 
		 */
		RoleManagerDetailViewCtrl.prototype.refreshLoggedInAssignableUsers = function() {
			var self = this;
			self.assignableUsersTable.refresh();
			/*
			 * if(self.showLoggedInAssignedUsers){
			 *  }
			 */
		};

		/**
		 * 
		 */
		RoleManagerDetailViewCtrl.prototype.removeUserFromRole = function(
				rowSelectionAssignedUsersTable) {
			var self = this;
			var userIds = this
					.getSelectedUserIds(rowSelectionAssignedUsersTable);
			sdProcessResourceMgmtService.removeUserFromRole(userIds,
					self.viewParams.roleId, self.viewParams.departmentOid)
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
		RoleManagerDetailViewCtrl.prototype.addUserToRole = function(
				rowSelectionAssignableUsersTable) {
			var self = this;
			var userIds = this
					.getSelectedUserIds(rowSelectionAssignableUsersTable);
			sdProcessResourceMgmtService.addUserToRole(userIds,
					self.viewParams.roleId, self.viewParams.departmentOid)
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
		RoleManagerDetailViewCtrl.prototype.getSelectedUserIds = function(
				selectedUsers) {
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
			self.dialogElem = $element.find('#alertMsg');
			self.dialogElem.detach();
		};

		RoleManagerDetailViewCtrl.prototype.getActivitiesForRole = function(
				params) {

			var self = this;
			var deferred = $q.defer();
			self.activities = {};
			sdProcessResourceMgmtService.getAllActivitiesForRole(params,
					self.viewParams.roleId, self.viewParams.departmentOid)
					.then(function(data) {
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

	}
})();