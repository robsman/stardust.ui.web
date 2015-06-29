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

	angular.module("admin-ui").controller(
			'sdParticipantManagementCtrl',
			[ '$q', 'sdParticipantManagementService', 'sdLoggerService', 'sdUtilService', 'sdUserService',
					ParticipantManagementCtrl ]);

	var _q;
	var _sdParticipantManagementService
	var trace;
	var _sdUtilService;
	
	var _sdUserService;

	/**
	 * 
	 */
	function ParticipantManagementCtrl($q, sdParticipantManagementService, sdLoggerService, sdUtilService,
			sdUserService) {
		trace = sdLoggerService.getLogger('admin-ui.sdParticipantManagementCtrl');
		_q = $q;
		_sdParticipantManagementService = sdParticipantManagementService;
		_sdUtilService = sdUtilService;
		_sdUserService = sdUserService;
		this.allUsersTable = null;
		this.showAllUsersTable = true;
		this.hideInvalidatedUsers = false;
		this.columnSelector = "admin";
		this.rowSelectionForAllUsersTable = null;
		this.exportFileNameForAllUsers = "AllUsers";

		this.getAllCounts();
	}

	/**
	 * 
	 * @returns
	 */
	ParticipantManagementCtrl.prototype.getAllUsers = function(options) {
		var deferred = _q.defer();
		var self = this;
		self.allUsers = {};
		var query = {
			'options' : options,
			'hideInvalidatedUsers' : self.hideInvalidatedUsers
		}
		_sdParticipantManagementService.getAllUsers(query).then(function(data) {
			self.allUsers.list = data.list;
			self.allUsers.totalCount = data.totalCount;
			deferred.resolve(self.allUsers);
		}, function(error) {
			trace.log(error);
			deferred.reject(error);
		});

		return deferred.promise;
	};

	ParticipantManagementCtrl.prototype.getAllCounts = function() {
		var self = this;
		_sdUserService.getAllCounts().then(function(data) {
			self.activeCount = data.activeCount;
			self.allCount = data.totalCount;
		}, function(error) {
			trace.log(error);
		});
	}

	/**
	 * 
	 */
	ParticipantManagementCtrl.prototype.refresh = function() {
		var self = this;
		self.allUsersTable.refresh();
		self.getAllCounts();
	};

	ParticipantManagementCtrl.prototype.changeHideInvalidatedUsersFlag = function() {
		var self = this;
		self.hideInvalidatedUsers = !self.hideInvalidatedUsers;
		self.allUsersTable.refresh();
	};
	/**
	 * @param mode
	 * @param oid
	 */
	ParticipantManagementCtrl.prototype.openCreateCopyModifyUser = function(mode, oid) {
		var self = this;
		self.mode = mode;
		_sdParticipantManagementService.openCreateCopyModifyUser(mode, oid).then(function(data) {
			self.user = data;
			if (mode == 'CREATE_USER') {
				self.title = 'views-common-messages.views-createUser-title';
				self.titleParams = '';
			} else if (mode == 'COPY_USER') {
				self.title = 'views-common-messages.views-copyUser-title';
				self.titleParams = self.rowSelectionForAllUsersTable[0].displayName;
			} else if (mode == 'MODIFY_USER') {
				self.title = 'views-common-messages.views-modifyUser-title';
				self.titleParams = '';
			}
			self.user.isPasswordEnabled = self.isPasswordEnabled(mode, self.user.isInternalAuthentication);
			self.initDisplayFormats();
			self.showUserProfileDialog = true;
			self.loadUserProfileDialog = true;
		}, function(error) {
			trace.log(error);
		});
	};
	/**
	 * 
	 * @param mode
	 * @param isInternalAuthentication
	 * @returns {Boolean}
	 */
	ParticipantManagementCtrl.prototype.isPasswordEnabled = function(mode, isInternalAuthentication) {
		if ((mode == 'MODIFY_USER' && isInternalAuthentication) || mode == 'MODIFY_PROFILE_CONFIGURATION') {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 */
	ParticipantManagementCtrl.prototype.initDisplayFormats = function() {
		var self = this;
		var USER_NAME_DISPLAY_FORMAT_0 = "{1}, {0} ({2})";
		var USER_NAME_DISPLAY_FORMAT_1 = "{0} {1} ({2})";
		var USER_NAME_DISPLAY_FORMAT_2 = "{1} {0} ({2})";
		self.user.displayFormats = [];
		if (!_sdUtilService.isEmpty(self.user.firstName) && !_sdUtilService.isEmpty(self.user.lastName)) {
			self.user.displayFormats.push({
				'value' : USER_NAME_DISPLAY_FORMAT_0,
				'label' : _sdUtilService.format(USER_NAME_DISPLAY_FORMAT_0, [ self.user.firstName, self.user.lastName,
						self.user.account ])
			});
			self.user.displayFormats.push({
				'value' : USER_NAME_DISPLAY_FORMAT_1,
				'label' : _sdUtilService.format(USER_NAME_DISPLAY_FORMAT_1, [ self.user.firstName, self.user.lastName,
						self.user.account ])
			});
			self.user.displayFormats.push({
				'value' : USER_NAME_DISPLAY_FORMAT_2,
				'label' : _sdUtilService.format(USER_NAME_DISPLAY_FORMAT_2, [ self.user.firstName, self.user.lastName,
						self.user.account ])
			});
		}
	};
	/**
	 * 
	 * @param res
	 * @returns {Boolean}
	 */
	ParticipantManagementCtrl.prototype.onConfirmFromCreateUser = function(res) {
		var self = this;
		if (self.userProfileForm.$valid) {
			var error = this.validateData();
			if (error) {
				return false;
			} else {
				var deferred = _q.defer();
				var user = {};
				angular.extend(user, self.user);
				delete user.allRealms;
				_sdParticipantManagementService.createCopyModifyUser(user, self.mode).then(function(data) {
					if (data.success == true) {
						deferred.resolve();
						self.allUsersTable.refresh();
						self.getAllCounts();
					} else if (data.success == false) {
						if (data.passwordValidationMsg != undefined) {
							self.passwordValidationMsg = data.passwordValidationMsg;
							self.userProfileForm.$error.passwordValidationMsg = true;
						} else if (data.validationMsg != undefined) {
							self.validationMsg = data.validationMsg;
							self.userProfileForm.$error.validationMsg = true;
						}
						deferred.reject();
					}
				}, function(error) {
					trace.log(error);
					deferred.reject();
				});
				return deferred.promise;

			}
		} else {

			return false;
		}
	};

	/**
	 * 
	 * @returns {Boolean}
	 */
	ParticipantManagementCtrl.prototype.validateData = function() {
		var self = this;
		var error = false;

		// Validate Dates
		if (!_sdUtilService.validateDateRange(self.user.validFrom, self.user.validTo)) {
			error = true;
			self.userProfileForm.$error.invalidDateRange = true;
		} else {
			self.userProfileForm.$error.invalidDateRange = false;
		}

		// Validate password
		if (self.user.changePassword) {
			if (_sdUtilService.validatePassword(self.user.password, self.user.confirmPassword)) {
				error = true;
				self.userProfileForm.$error.passwordMismatch = true;

			} else {
				self.userProfileForm.$error.passwordMismatch = false;
			}
		}

		return error;

	};
	/**
	 * 
	 * @param res
	 */
	ParticipantManagementCtrl.prototype.onCloseFromCreateUser = function(res) {
		var self = this;
		self.loadUserProfileDialog = false;
		delete self.user;
	};
	/**
	 * 
	 */
	ParticipantManagementCtrl.prototype.invalidateUsers = function() {
		var self = this;
		var oids = this.getSelectedUserOids(self.rowSelectionForAllUsersTable);
		_sdParticipantManagementService.invalidateUsers(oids).then(function(data) {
			self.activityInstances = data.activityInstances;
			self.notificationMap = data.notificationMap;
			self.allUsersTable.refresh();
			self.getAllCounts();
			if (self.activityInstances != undefined && self.activityInstances.length > 0) {
				self.showDefaultDelegateDialog = true;
			} else {
				self.showNotificationDialog = true;
			}

		}, function(error) {
			trace.log(error);

		});

	};
	/**
	 * 
	 * @param res
	 */
	ParticipantManagementCtrl.prototype.onConfrimFromDefaultDelegateDialog = function(res) {
		var self = this;
		var userOids = this.getInvalidatedUserOids(self.notificationMap.success);
		_sdParticipantManagementService.delegateToDefaultPerformer(self.activityInstances, userOids).then(
				function(data) {

					if (data.success != undefined && data.success) {
						self.showNotificationDialog = true;
					} else {
						self.showStrandedActivitiesAlert = true;
					}

				}, function(error) {
					trace.log(error);

				});
	};
	/**
	 * 
	 * @param res
	 */
	ParticipantManagementCtrl.prototype.onCloseFromDefaultDelegateDialog = function(res) {
		var self = this;
		self.showStrandedActivitiesAlert = true;
	};
	/**
	 * 
	 * @param res
	 */
	ParticipantManagementCtrl.prototype.onConfirmFromStrandedActivitiesAlert = function(res) {
		var self = this;
		self.showNotificationDialog = true;
	};

	/**
	 * 
	 */
	ParticipantManagementCtrl.prototype.getSelectedUserOids = function(selectedUsers) {
		var oids = [];
		for ( var user in selectedUsers) {
			oids.push(selectedUsers[user].oid);
		}
		return oids;
	};

	/**
	 * 
	 */
	ParticipantManagementCtrl.prototype.getInvalidatedUserOids = function(successNotificationList) {
		var oids = [];
		for ( var index in successNotificationList) {
			oids.push(successNotificationList[index].OID);
		}
		return oids;
	};

	/**
	 * 
	 */
	/**
	 * 
	 */
	ParticipantManagementCtrl.prototype.setShowAllUsersTable = function() {
		var self = this;
		self.showAllUsersTable = !self.showAllUsersTable;
	};

})();