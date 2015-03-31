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
			[ '$q', '$scope', '$filter', '$element', 'sdUserManagerDetailService', 'sdLoggerService',
					'sdViewUtilService', UserManagerDetailViewCtrl ]);

	/*
	 * 
	 */
	function UserManagerDetailViewCtrl($q, $scope, $filter, $element, sdUserManagerDetailService, sdLoggerService,
			sdViewUtilService) {
		var trace = sdLoggerService.getLogger('bcc-ui.sdUserManagerDetailViewCtrl');

		this.columnSelector = 'admin';
		this.exportFileName = new Date();
		this.assignedRolesTable = null;
		this.assignableRolesTable = null;
		this.activityTable = null;
		this.rowSelectionAssignedRolesTable = null;
		this.rowSelectionAssignableRolesTable = null;
		this.showActivityListTab = false;
		this.activeTab = 1;

		UserManagerDetailViewCtrl.prototype.handleViewEvents = function(event) {
			if (event.type == "ACTIVATED") {
				this.refreshOnlyActiveTab();
			} else if (event.type == "DEACTIVATED") {
				// TODO
			}
		};

		// Register for View Events
		sdViewUtilService.registerForViewEvents($scope, this.handleViewEvents, this);

		/**
		 * 
		 */
		UserManagerDetailViewCtrl.prototype.getUserManagerDetails = function() {
			var self = this;
			sdUserManagerDetailService.getUserManagerDetails(self.viewParams.userOid).then(function(data) {
				self.userManagerDetails = data;
				self.showAssignedRolesTable = true;
				self.showAssignableRolesTable = true;
				if (self.assignedRolesTable != undefined && self.assignableRolesTable != undefined) {
					self.assignedRolesTable.refresh();
					self.assignableRolesTable.refresh();
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
			self.viewParams = sdViewUtilService.getViewParams($scope);
			self.getUserManagerDetails();
		};

		this.initialize();

		/**
		 * 
		 */
		UserManagerDetailViewCtrl.prototype.getAssignedRoles = function() {
			var self = this;
			return self.userManagerDetails.assignedRoleList;
		};

		/**
		 * 
		 */
		UserManagerDetailViewCtrl.prototype.getAssignableRoles = function() {
			var self = this;
			return self.userManagerDetails.assignableRoleList;
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
				self.getUserManagerDetails();
			} else {
				self.activityTable.refresh();
			}
		};

		/**
		 * 
		 */

		UserManagerDetailViewCtrl.prototype.removeRoleFromUser = function(rowSelectionAssignedRolesTable) {
			var self = this;
			var roleIds = self.getSelectedRoleIds(rowSelectionAssignedRolesTable);
			sdUserManagerDetailService.removeRoleFromUser(roleIds, self.viewParams.userOid).then(function(data) {
				self.userAuthorizationMsg = data.userAuthorization;
				self.refresh();
			}, function(error) {
				trace.log(error);
			});
		};

		/**
		 * 
		 */
		UserManagerDetailViewCtrl.prototype.addRoleToUser = function(rowSelectionAssignableRolesTable) {
			var self = this;
			var roleIds = self.getSelectedRoleIds(rowSelectionAssignableRolesTable);

			sdUserManagerDetailService.addRoleToUser(roleIds, self.viewParams.userOid).then(function(data) {
				self.userAuthorizationMsg = data.userAuthorization;
				self.refresh();
			}, function(error) {
				trace.log(error);
			});

		};

		/**
		 * 
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
			self.dialogElem = $element.find('#alertMsg');
			self.dialogElem.detach();
		};

		/**
		 * 
		 */

		UserManagerDetailViewCtrl.prototype.getActivitiesForUser = function(params) {

			var self = this;
			var deferred = $q.defer();
			self.activities = {};
			sdUserManagerDetailService.getAllActivitiesForUser(params, self.viewParams.userOid).then(function(data) {
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

	}
})();