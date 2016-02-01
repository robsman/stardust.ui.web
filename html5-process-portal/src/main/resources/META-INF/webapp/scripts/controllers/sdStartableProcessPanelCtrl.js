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
			'sdStartableProcessPanelCtrl',
			[ '$scope', 'sdStartableProcessService', 'sdLoggerService', 'sdViewUtilService', 'sdUtilService',
					 'sdI18nService', 'sdDialogService', 'sdCommonViewUtilService',
					StartableProcessPanelCtrl ]);
	var _scope;
	var _sdStartableProcessService;
	var _sdViewUtilService;
	var _sdUtilService;
	var trace;
	var _sdI18nService;
	var _sdDialogService;
	var _sdCommonViewUtilService;
	
	/**
	 * 
	 */
	function StartableProcessPanelCtrl($scope, sdStartableProcessService, sdLoggerService, sdViewUtilService,
			sdUtilService, sdI18nService, sdDialogService, sdCommonViewUtilService) {
		trace = sdLoggerService.getLogger('workflow-ui.sdStartableProcessPanelCtrl');
		_scope = $scope;
		_sdStartableProcessService = sdStartableProcessService;
		_sdViewUtilService = sdViewUtilService;
		_sdUtilService = sdUtilService;
		_sdI18nService = sdI18nService;
		_sdDialogService = sdDialogService;
		_sdCommonViewUtilService = sdCommonViewUtilService;
		this.getStartableProcesses();

	}

	/**
	 * 
	 */
	StartableProcessPanelCtrl.prototype.getStartableProcesses = function() {
		var self = this;
		_sdStartableProcessService.getStartableProcesses().then(function(data) {
			self.startableProcesses = data.list;
		}, function(error) {
			trace.error(error);
		});
	};

	/**
	 * 
	 * @param startableProcess
	 */
	StartableProcessPanelCtrl.prototype.startProcess = function(startableProcess) {
		var self = this;
		self.processId = startableProcess.processDefinition.id;
		self.processName = startableProcess.name;
         
		if (_sdUtilService.isEmpty(startableProcess.participantNodes)
				&& !_sdUtilService.isEmpty(startableProcess.deptList) && startableProcess.deptList.length === 1) {
			//if participant Nodes are empty but department list is not not empty

		} else if (!_sdUtilService.isEmpty(startableProcess.participantNodes)) {
			// if process is having scoped participant
			self.participantNodes = startableProcess.participantNodes;
			self.showDeptDialog = true;
		} else if (_sdUtilService.isEmpty(startableProcess.participantNodes)) {
			//if process is not having any scoped participant and departments
			_sdStartableProcessService.startProcess(self.processId).then(function(data) {
						
				trace.log("Result from startProcess:-" + data);
						
						if (data.processStarted) {
							_sdDialogService.info(_scope, _sdI18nService.translate(
									'processportal.common-processStarted-message', {}, [ self.processName ]), {});
						} else if (data.activityInstanceOid != undefined) {
							var params = {
								"oid" : "" + data.activityInstanceOid
							};

							if (data.assemblyLineActivity) {
								params.assemblyLineActivity = true;
							}
							_sdViewUtilService.openView("activityPanel", "oid=" + data.activityInstanceOid, params, false);
						}
					}, function(error) {
						trace.error(error);
					});
		}
	};
    /**
     * 
     * @param departmentOid
     */
	StartableProcessPanelCtrl.prototype.startProcessOnSelectDepartment = function(departmentOid) {
		var self = this;
		_sdStartableProcessService.startProcessOnSelectDepartment(departmentOid, self.processId).then(
				function(data) {
					self.departmentDialog.close();
					trace.log("Result from startProcessOnSelectDepartment:-" + data);
					if (data.processStarted) {
						_sdDialogService.info(_scope, _sdI18nService.translate(
								'processportal.common-processStarted-message', {}, [ self.processName ]), {});
					} else if (data.activityInstanceOid != undefined) {
						_sdCommonViewUtilService.openActivityView(data.activityInstanceOid);
					}
				}, function(error) {
					trace.error(error);
				});
	}
	/**
	 * 
	 */
	StartableProcessPanelCtrl.prototype.refreshMyStartableProcessPanel = function() {
		this.getStartableProcesses();
	};
    /**
     * 
     * @param data
     * @param e
     */
	StartableProcessPanelCtrl.prototype.eventCallback = function(data, e) {
		var self = this;
		if (data.treeEvent === "node-expand") {
			// complete tree is already loaded
			data.valueItem.isLoaded = true;
			data.deferred.resolve();
		} else if (data.treeEvent === "node-click" && data.valueItem.children == undefined) {
			self.startProcessOnSelectDepartment(data.valueItem.OID);
		}
	};
})();
