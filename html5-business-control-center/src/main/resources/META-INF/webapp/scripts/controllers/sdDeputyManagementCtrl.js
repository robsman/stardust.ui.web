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
					'sdDeputyManagementCtrl',
					[ '$q', 'sdDeputyManagementService', 'sdLoggerService', 'sdUtilService',
							DeputyManagementCtrl ]);

	var _q;
	var _sdDeputyManagementService;
	var trace;
	var _sdUtilService;
	var rootURL;

	/**
	 * 
	 */
	function DeputyManagementCtrl($q, sdDeputyManagementService, sdLoggerService, sdUtilService) {
		trace = sdLoggerService.getLogger('bcc-ui.sdDeputyManagementCtrl');
		_q = $q;
		_sdDeputyManagementService = sdDeputyManagementService;
		_sdUtilService = sdUtilService;

		rootURL = _sdUtilService.getRootUrl();

		this.usersTable = null;
		this.userDeputiesTable = null;
		this.showUserDeputiesTable = true
		this.deputies = [];
		this.rowSelectionForUsers = null;
		this.showHideNonDepParticipant = true;
		this.searchMode = "SIMILAR_USERS";
		this.getUsers();
	}

	/**
	 * 
	 * @returns
	 */
	DeputyManagementCtrl.prototype.getUsers = function() {
		var deferred = _q.defer();
		var self = this;
		_sdDeputyManagementService.loadUsers().then(function(data) {
			self.allUsers = data.list;

			self.usersWithDeputies = [];

			angular.forEach(data.list, function(user) {
				if (user.hasDeputies) {
					self.usersWithDeputies.push(user);
				}
			});
			if (self.usersTable != undefined) {
				self.usersTable.refresh();
			} else {
				self.showUsersTable = true;
			}

			deferred.resolve(data.list);
		}, function(error) {
			trace.log(error);
			// deferred.reject(error);
		});

		return deferred.promise;
	};

	/**
	 * 
	 */
	DeputyManagementCtrl.prototype.getUsersData = function() {
		var self = this;
		var deferred = _q.defer();
		if (self.showHideNonDepParticipant) {
			deferred.resolve(self.allUsers);
		} else {
			deferred.resolve(self.usersWithDeputies);
		}

		return deferred.promise;
	};

	/**
	 * 
	 * @param userImageURI
	 * @returns
	 */
	DeputyManagementCtrl.prototype.getUserImageURL = function(userImageURI) {
		return rootURL + userImageURI;
	};

	/**
	 * 
	 * @param userOID
	 */
	DeputyManagementCtrl.prototype.getDeputiesForUser = function(userOID) {
		var self = this;
		_sdDeputyManagementService.loadDeputiesForUser(userOID).then(function(data) {
			self.deputies = data.list;
			if (self.userDeputiesTable != undefined) {
				self.userDeputiesTable.refresh();
			}
		}, function(error) {
			trace.log(error);
		});
	}

	/**
	 * 
	 * @param info
	 */
	DeputyManagementCtrl.prototype.onSelect = function(info) {
		var self = this;
		if (info.action == "select") {
			self.getDeputiesForUser(info.current.userOID);
		} else {
			self.deputies = [];
			self.userDeputiesTable.refresh();
		}
	};

	/**
	 * 
	 * @param showHideIndicator
	 */
	DeputyManagementCtrl.prototype.showHideNonDeputyParticipant = function(showHideIndicator) {
		var self = this;
		self.showHideNonDepParticipant = showHideIndicator;
		self.usersTable.refresh();
		self.deputies = [];
		self.userDeputiesTable.refresh();
	};
	/**
	 * 
	 * @param searchValue
	 * @returns
	 */
	DeputyManagementCtrl.prototype.getDeputyUsersData = function(searchValue) {
		var self = this;
		var deferred = _q.defer();
		if (angular.isDefined(searchValue) && searchValue.length > 0) {
			_sdDeputyManagementService.getDeputyUsersData(self.rowSelectionForUsers.userOID, searchValue,
					self.searchMode).then(function(data) {
				deferred.resolve(data.list);
			}, function(result) {
				// Error occurred
				trace.log('An error occurred while fetching participants.\n Caused by: ' + result);
				deferred.reject(result);
			});
		} else {
			deferred.resolve({});
		}

		return deferred.promise;
	};

	/**
	 * 
	 */
	DeputyManagementCtrl.prototype.openCreateOrModifyDeputyDialog = function(rowData, mode) {
		var self = this;
		self.deputy = {};
		self.deputy.mode = mode;
		_sdDeputyManagementService.getAuthorizations(self.rowSelectionForUsers.userOID).then(function(data) {
			self.deputy.sourceParticipants = data.list;
			self.deputy.targetParticipants = [];
			if (self.deputy.mode == "EDIT") {
				angular.extend(self.deputy.targetParticipants, rowData.participants);
				self.getSourceParticipantForEdit();
				self.deputy.validFrom = rowData.validFrom;
				self.deputy.validTo = rowData.validTo;
				self.deputy.deputyDisplayName = rowData.userDisplayName;
				self.deputy.deputyOID = rowData.userOID;
			}

			self.showCreateOrModifyDeputyDialog = true;
		}, function(result) {
			// Error occurred
			trace.log('An error occurred while fetching authorization.\n Caused by: ' + result);
		});

	};
	
	/**
	 * 
	 */
	DeputyManagementCtrl.prototype.getSourceParticipantForEdit = function() {
		var self = this;
		angular.forEach(self.deputy.targetParticipants, function(participant) {
			self.removeFromArry(self.deputy.sourceParticipants, participant);
		});
	}
	/**
	 * 
	 */
	DeputyManagementCtrl.prototype.add = function() {
		var self = this;
		angular.forEach(self.deputy.selectedSourceParticipants, function(participant) {
			self.removeFromArry(self.deputy.sourceParticipants, participant);
			self.deputy.targetParticipants.push(participant)

		});

		self.deputy.selectedSourceParticipants = [];
		self.deputy.selectedTargetParticipants = [];
	};
	/**
	 * 
	 */
	DeputyManagementCtrl.prototype.addAll = function() {
		var self = this;
		self.deputy.selectedSourceParticipants = [];
		angular.extend(self.deputy.selectedSourceParticipants, self.deputy.sourceParticipants);
		self.add();
	};
	/**
	 * 
	 */
	DeputyManagementCtrl.prototype.remove = function() {
		var self = this;
		angular.forEach(self.deputy.selectedTargetParticipants, function(participant) {
			self.deputy.sourceParticipants.push(participant);
			self.removeFromArry(self.deputy.targetParticipants, participant);

		});

		self.deputy.selectedSourceParticipants = [];
		self.deputy.selectedTargetParticipants = [];
	};
	/**
	 * 
	 */
	DeputyManagementCtrl.prototype.removeAll = function() {
		var self = this;
		self.deputy.selectedTargetParticipants = [];
		angular.extend(self.deputy.selectedTargetParticipants, self.deputy.targetParticipants);
		self.remove();
	};

	/**
	 * 
	 */
	DeputyManagementCtrl.prototype.removeFromArry = function(array, val) {
		var i = array.map(function(e) {
			return e.value;
		}).indexOf(val.value);
		if (i > -1) {
			array.splice(i, 1);
		}
	};

	/**
	 * 
	 * @param res
	 */
	DeputyManagementCtrl.prototype.onConfirmAddEditDeputy = function(res) {
		var self = this;
		// validating data
		var error = this.validateData();
		if (error) {
			return false;
		} else {
			var userOID = self.rowSelectionForUsers.userOID;
			if (self.deputy.mode == "EDIT") {
				var deputyOID = self.deputy.deputyOID;
			} else {
				var deputyOID = self.deputy.participantDataSelected[0].OID;
			}

			var validFrom = self.deputy.validFrom;
			var validTo = self.deputy.validTo;
			var modelParticipantIds = [];
			angular.forEach(self.deputy.targetParticipants, function(participant) {
				modelParticipantIds.push(participant.value);
			});

			var mode = self.deputy.mode;

			var userRow = {
				userOID : self.rowSelectionForUsers.userOID
			};

			delete self.deputy;

			_sdDeputyManagementService.addOrModifyDeputy(userOID, deputyOID, validFrom, validTo, modelParticipantIds, mode)
					.then(function(data) {
						self.getUsers().then(function(data) {
							setTimeout(function() {
								self.usersTable.setSelection(userRow);
							}, 500);
						});
					}, function(result) {
						// Error occurred
						trace.log('An error occurred while adding deputy.\n Caused by: ' + result);
					});

		}

	};
    /**
     * 
     * @returns {Boolean}
     */
	DeputyManagementCtrl.prototype.validateData = function() {
		var self = this;
		var error = false;
		// Validate Deputy User
		if (self.deputy.mode == "ADD" && _sdUtilService.isEmpty(self.deputy.participantDataSelected)) {
			error = true;
			self.addEditDeputyForm.$error.invalidDeputy = true;
		} else {
			self.addEditDeputyForm.$error.invalidDeputy = false;
		}

		// Validate Dates
		if (!_sdUtilService.validateDateRange(self.deputy.validFrom, self.deputy.validTo)) {
			error = true;
			self.addEditDeputyForm.$error.invalidDateRange = true;
		} else {
			self.addEditDeputyForm.$error.invalidDateRange = false;
		}

		// Validate Authorizations
		if (_sdUtilService.isEmpty(self.deputy.targetParticipants)) {
			error = true;
			self.addEditDeputyForm.$error.noAuthsSelected = true;

		} else {
			self.addEditDeputyForm.$error.noAuthsSelected = false;
		}

		return error;

	};

	/**
	 * 
	 */
	DeputyManagementCtrl.prototype.removeUserDeputy = function(deputyOID) {
		var self = this;
		var userRow = {
			userOID : self.rowSelectionForUsers.userOID
		};
		_sdDeputyManagementService.removeUserDeputy(self.rowSelectionForUsers.userOID, deputyOID).then(function(data) {
			self.getUsers().then(function(data) {
				setTimeout(function() {
					self.usersTable.setSelection(userRow);
				}, 500);
			});
		}, function(result) {
			// Error occurred
			trace.log('An error occurred while removing user deputy.\n Caused by: ' + result);
		});
	};
})();