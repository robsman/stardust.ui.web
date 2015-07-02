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
 * @author Aditya.Gaikwad
 */

(function() {
	'use strict';

	angular.module('admin-ui').controller('sdUserGroupCtrl',
			[ '$q', 'sdUserGroupService', 'sdLoggedInUserService', controller ]);

	// Closures for our dependencies injected by the DI subsystem.
	var _sdUserGroupService;
	var _q;

	/*
	 * 
	 */
	function controller($q, sdUserGroupService, sdLoggedInUserService) {
		_sdUserGroupService = sdUserGroupService;
		_q = $q;

		this.initialize(sdLoggedInUserService);
	}

	/*
	 * 
	 */
	controller.prototype.initialize = function(sdLoggedInUserService) {
		this.userGroupDataTable = null; // This will be set to underline data
		// table instance automatically

		this.data = {};
		this.selectionExpr = null;
		this.showCreateUserGroup = false;
		this.showModifyUserGroup = false;
		this.columnSelector = sdLoggedInUserService.getUserInfo().isAdministrator ? 'admin' : true;

		this.ready = false;

		this.query = {
			options : {}
		};

		this.newUserGroup = {
			id : '',
			name : '',
			validFrom : '',
			validTo : '',
			description : ''
		};

	};

	/*
	 * 
	 */
	controller.prototype.fetchUserGroups = function(options) {
		var deferred = _q.defer();

		this.query.options = options;
		var self = this;
		_sdUserGroupService.fetchUserGroups(this.query).then(function(data) {
			self.data.totalCount = data.totalCount;
			self.data.list = data.list;
			self.data.activeCount = data.activeCount;
			deferred.resolve(self.data);
		}, function(error) {
			deferred.reject(error);
		});

		return deferred.promise;
	};

	/*
	 * 
	 */
	controller.prototype.refresh = function() {
		this.userGroupDataTable.refresh(true);
	};

	/*
	 * 
	 */
	controller.prototype.openCreateUserGroupDialog = function() {
		this.newUserGroup = {};
		this.errorExists = null;
		this.showCreateUserGroup = true;
	};

	/*
	 * 
	 */
	controller.prototype.onConfirmCreateUser = function(res) {
		var self = this;
		self.submitted = true;
		if (!(self.createUserGroupForm.$valid)) {
			self.showCreateUserGroup = true;
			return;
		}
		if (!(self.validateDateRange(this.newUserGroup.validFrom, this.newUserGroup.validTo))) {
			self.showDateError();
			self.showCreateUserGroup = true;
			return;
		}
		_sdUserGroupService.createUserGroup(this.newUserGroup).then(
				function(data) {
					self.showCreateUserGroup = false;
					self.refresh();
				},
				function(error) {
					self.errorExists = true;
					self.errorMessage = error.data.message.substr(error.data.message.indexOf(" - ") + 2,
							error.data.message.length);
					self.showCreateUserGroup = true;
				});
	};

	/*
	 * 
	 */
	controller.prototype.onCancelCreateUser = function() {
		this.submitted = false;
		this.newUserGroup = {};
		this.showCreateUserGroup = false;
	};

	/*
	 * 
	 */
	controller.prototype.openModifyUserGroup = function(selectedUserGroup) {
		this.errorExists = null;
		this.newUserGroup = angular.copy(selectedUserGroup);
		this.showModifyUserGroup = true;
	};

	/*
	 * 
	 */
	controller.prototype.onConfirmModifyUser = function(res) {
		var self = this;
		if (!(self.validateDateRange(this.newUserGroup.validFrom, this.newUserGroup.validTo))) {
			self.showDateError();
			self.showModifyUserGroup = true;
			return;
		}
		_sdUserGroupService.modifyUserGroup(this.newUserGroup).then(
				function(data) {
					self.showModifyUserGroup = false;
					self.refresh();
				},
				function(error) {
					self.errorExists = true;
					self.errorMessage = error.data.message.substr(error.data.message.indexOf(" - ") + 2,
							error.data.message.length);
					self.showModifyUserGroup = true;
				});
		this.newUserGroup = {};
	};

	/*
	 * 
	 */
	controller.prototype.onCancelModifyUser = function() {
		this.newUserGroup = {};
		this.showModifyUserGroup = false;
	};

	/*
	 * 
	 */
	controller.prototype.invalidateUserGroup = function() {
		var self = this;
		if (self.selectionExpr.validTo == null) {
			_sdUserGroupService.invalidateUserGroup(self.selectionExpr.id).then(
					function(data) {
						self.showNotificationUserGroup = true;
						self.notificationStatus = "admin-portal-messages.views-userGroupMgmt-notifySuccessMsg";
						self.notificationMessage = "admin-portal-messages.views-userGroupMgmt-notifyUserGroupInvalidate";
					});
		} else {//Already Validated
			self.showNotificationUserGroup = true;
			self.notificationStatus = "admin-portal-messages.views-userGroupMgmt-notifyNonValidateMsg";
			self.notificationMessage = "admin-portal-messages.views-userGroupMgmt-notifyUserGroupNotValidateMsg";
		}
	};
	
	/*
	 * 
	 */
	controller.prototype.onCloseNotificationUserGroup = function() {
		this.showNotificationUserGroup = false;
		this.refresh();
	};
	
	/*
	 * 
	 */
	controller.prototype.validateDateRange = function(validFrom, validTo) {
		if (!(validFrom && validTo)) {
			return true;
		}
		if (validFrom && validFrom == null || validTo && validTo == null) {
			return true;
		}
		if ((validFrom != null && validFrom != "")
				&& (validTo != null && validTo != "")) {
			if (validTo >= validFrom) {
				return true;
			}
		}
		return false;
	};
	
	/*
	 * 
	 */
	controller.prototype.showDateError = function() {
		this.errorExists = true;
		this.errorMessage = "admin-portal-messages.views-userGroupMgmt-invalidDate";
	};
	
	
	

})();