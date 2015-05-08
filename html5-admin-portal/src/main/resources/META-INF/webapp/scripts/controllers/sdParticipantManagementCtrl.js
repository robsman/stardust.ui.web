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

	angular.module("admin-ui").controller('sdParticipantManagementCtrl',
			[ '$q', 'sdParticipantManagementService', 'sdLoggerService', ParticipantManagementCtrl ]);

	var _q;
	var _sdParticipantManagementService
	var trace;

	/**
	 * 
	 */
	function ParticipantManagementCtrl($q, sdParticipantManagementService, sdLoggerService) {
		trace = sdLoggerService.getLogger('admin-ui.sdParticipantManagementCtrl');
		_q = $q;
		_sdParticipantManagementService = sdParticipantManagementService;

		this.allUsersTable = null;
		this.hideInvalidatedUsers = false;
		this.columnSelector="admin";
		this.rowSelectionForAllUsersTable = null;
		this.exportFileNameForAllUsers = "AllUsers";
	}

	/**
	 * 
	 * @returns
	 */
	ParticipantManagementCtrl.prototype.getAllUsers = function(options) {
		var deferred = _q.defer();
		var self = this;
		self.allUsers = {};
		var query ={
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

	ParticipantManagementCtrl.prototype.refresh = function() {
		var self = this;
		self.allUsersTable.refresh();
	};
})();