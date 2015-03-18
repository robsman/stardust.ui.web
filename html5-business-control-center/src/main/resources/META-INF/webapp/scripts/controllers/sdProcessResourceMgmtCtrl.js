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
			[ '$q', '$scope', 'sdProcessResourceMgmtService', 'sdUtilService',
					'sdLoggerService', ProcessResourceMgmtCtrl ]);

	/*
	 * 
	 */
	function ProcessResourceMgmtCtrl($q, $scope, sdProcessResourceMgmtService,
			sdUtilService, sdLoggerService) {
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
		ProcessResourceMgmtCtrl.prototype.getProcessResourceRoles = function(options) {
			var self = this;
			return self.processResourceRoleList

		};
		
		/**
		 * 
		 * @param options
		 * @returns
		 */
		ProcessResourceMgmtCtrl.prototype.getProcessResourceUsers = function(options) {
			var self = this;
			return self.processResourceUserList

		};

		ProcessResourceMgmtCtrl.prototype.refresh = function(){
			var self = this;
			self.getProcessResourceRolesAndUsers();
			self.rolesTable.refresh();
			self.usersTable.refresh();
		};
	}
})();