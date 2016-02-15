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

	angular.module("workflow-ui").controller(
			'sdWorkflowMyProcessPanelCtrl',
			[ 'sdWorkflowWorklistService', 'sdLoggerService', 'sdViewUtilService', 'sdLoggedInUserService',
					'sdI18nService', 'sdActivityInstanceService', 'sdDialogService', 'sdCommonViewUtilService',
					'$scope', WorkflowMyProcessPanelCtrl ]);
	var _sdWorkflowWorklistService;
	var _sdViewUtilService;
	var trace;
	var _sdLoggedInUserService;
	var _sdActivityInstanceService;
	var _sdI18nService;
	var _sdDialogService;
	var _sdCommonViewUtilService;
	var _scope;
	/**
	 *
	 */
	function WorkflowMyProcessPanelCtrl(sdWorkflowWorklistService, sdLoggerService, sdViewUtilService,
			sdLoggedInUserService, sdI18nService, sdActivityInstanceService, sdDialogService, sdCommonViewUtilService, $scope) {
		trace = sdLoggerService.getLogger('workflow-ui.sdWorkflowMyProcessPanelCtrl');
		_sdWorkflowWorklistService = sdWorkflowWorklistService;
		_sdViewUtilService = sdViewUtilService;
		_sdLoggedInUserService = sdLoggedInUserService;
		_sdI18nService = sdI18nService;
		_sdActivityInstanceService = sdActivityInstanceService;
		_sdDialogService = sdDialogService;
		_sdCommonViewUtilService = sdCommonViewUtilService;
		_scope = $scope;

		this.userInfo = _sdLoggedInUserService.getUserInfo();

		this.getUserProcesses();

	}

	/**
	 *
	 */
	WorkflowMyProcessPanelCtrl.prototype.getUserProcesses = function() {
		var self = this;
		_sdWorkflowWorklistService.getUserProcesses().then(function(data) {
			self.workflowMyProcesses = data.list;
		}, function(error) {
			trace.error(error);
		});
	};

	/**
	 *
	 */
	WorkflowMyProcessPanelCtrl.prototype.refreshMyProcessesPanel = function() {
		this.getUserProcesses();
	};

	WorkflowMyProcessPanelCtrl.prototype.openWorklistView = function(process){
		  var params = {
					  	"id": process.id,
						"name" : process.name,
						 "processQId" : process.id
				       };
		_sdViewUtilService.openView("worklistPanel","id=" +process.id, params);
	};

})();
