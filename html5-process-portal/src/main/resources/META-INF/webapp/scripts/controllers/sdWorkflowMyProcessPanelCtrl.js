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
			[ 'sdWorkflowWorklistService', 'sdLoggerService', 'sdViewUtilService', '$scope', 'sgPubSubService', WorkflowMyProcessPanelCtrl ]);
	var _sdWorkflowWorklistService;
	var _sdViewUtilService;
	var trace;
	var _scope;
	/**
	 *
	 */
	function WorkflowMyProcessPanelCtrl(sdWorkflowWorklistService, sdLoggerService, sdViewUtilService,
			$scope, sgPubSubService) {
		trace = sdLoggerService.getLogger('workflow-ui.sdWorkflowMyProcessPanelCtrl');
		_sdWorkflowWorklistService = sdWorkflowWorklistService;
		_sdViewUtilService = sdViewUtilService;
		_scope = $scope;
		
		this.collapsePanelHandle = null;
		var self = this;
		sgPubSubService.subscribe('sdRefreshLaunchPanel', function(){			
			if(self.collapsePanelHandle.expanded()){
				self.refreshMyProcessesPanel();
			}else{
				self.syncPanel = true;
			}			
		});

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
	/**
	 * 
	 */
	WorkflowMyProcessPanelCtrl.prototype.refreshPanelToSync = function() {
		var self = this;
		if(self.syncPanel){
			self.syncPanel = false;
			self.getUserProcesses();
		}		
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
