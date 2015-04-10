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
			[ '$q', 'sdRoleAssignmentService', 'sdLoggerService', 'sdViewUtilService',
			  RoleAssignmentCtrl ]);

	var _q;
	var _sdRoleAssignmentService;
	var _sdViewUtilService;
	var trace;

	/**
	 * 
	 */
	function RoleAssignmentCtrl($q, sdRoleAssignmentService, sdLoggerService, sdViewUtilService) {
		trace = sdLoggerService.getLogger('bcc-ui.sdRoleAssignmentCtrl');
		_q = $q;
		_sdRoleAssignmentService = sdRoleAssignmentService;
		_sdViewUtilService = sdViewUtilService;

		this.roleAssignmentTable = null;
		this.columnSelector = 'admin';
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
			self.roleAssignments.list =  data.list;
			self.roleAssignments.totalCount = data.totalCount;
			self.descriptors = data.list[0].descriptors;
			self.showRoleAssignmentTable= true;
		}, function(error) {
			trace.log(error);
		});
	};

	RoleAssignmentCtrl.prototype.getRoleAssignmentData = function(){
		var self = this;
		return self.roleAssignments;
	};

	/**
	 * 
	 */
	RoleAssignmentCtrl.prototype.refresh = function() {
		var self = this;
		self.roleAssignmentTable.refresh();
	};

	/**
	 * 
	 * @param userOid
	 * @param userId
	 */
	RoleAssignmentCtrl.prototype.openUserManagerView = function(userOid, userId) {
		_sdViewUtilService.openView("userManagerDetailViewHtml5", "userOid=" + userOid, {
			"userOid" : "" + userOid,
			"userId" : "" + userId
		}, true);
	};
})();