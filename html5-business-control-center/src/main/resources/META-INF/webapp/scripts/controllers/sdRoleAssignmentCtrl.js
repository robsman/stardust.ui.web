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
			[ '$q', '$timeout', 'sdRoleAssignmentService', 'sdLoggerService', 'sdViewUtilService',
			  RoleAssignmentCtrl ]);

	var _q;
	var _sdRoleAssignmentService;
	var _sdViewUtilService;
	var trace;
	var _timeout;

	/**
	 * 
	 */
	function RoleAssignmentCtrl($q, $timeout, sdRoleAssignmentService, sdLoggerService, sdViewUtilService) {
		trace = sdLoggerService.getLogger('bcc-ui.sdRoleAssignmentCtrl');
		_q = $q;
		_sdRoleAssignmentService = sdRoleAssignmentService;
		_sdViewUtilService = sdViewUtilService;
		_timeout = $timeout

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
			self.columns = data.roleColumns;
			self.showRoleAssignmentTable= true;
			_timeout(function() {
				self.createTable = true;
			}, 0, true);
		}, function(error) {
			trace.log(error);
		});
	};

	/**
	 * 
	 * @returns
	 */
	RoleAssignmentCtrl.prototype.getRoleAssignmentData = function(){
		var self = this;
		return self.roleAssignments;
	};

	/**
	 * 
	 */
	RoleAssignmentCtrl.prototype.refresh = function() {
		var self = this;
		self.createTable = false;
		self.getRoleAssignments();		
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
	
	RoleAssignmentCtrl.prototype.getExportValue = function(colValue){
		if(colValue == true){
			return 'Yes';
		}else{
			return 'No';
		}
	};
})();