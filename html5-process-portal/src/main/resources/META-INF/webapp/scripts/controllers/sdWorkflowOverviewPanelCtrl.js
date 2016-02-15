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
			'sdWorkflowOverviewPanelCtrl',
			['sdWorkflowOverviewService', 'sdLoggerService', 'sdViewUtilService',
					'sdLoggedInUserService', 'sdI18nService', WorkflowOverviewPanelCtrl ]);
	var _sdWorkflowOverviewService;
	var _sdViewUtilService;
	var trace;
	var _sdLoggedInUserService;
	var _sdI18nService;
	/**
	 *
	 */
	function WorkflowOverviewPanelCtrl(sdWorkflowOverviewService, sdLoggerService, sdViewUtilService, sdLoggedInUserService, sdI18nService) {
		trace = sdLoggerService.getLogger('workflow-ui.sdWorkflowOverviewPanelCtrl');
		_sdWorkflowOverviewService = sdWorkflowOverviewService;
		_sdViewUtilService = sdViewUtilService;
		_sdLoggedInUserService = sdLoggedInUserService;
		_sdI18nService = sdI18nService;

		this.userInfo = _sdLoggedInUserService.getUserInfo();

		this.getOverviewCounts();

	}

	/**
	 *
	 */
	WorkflowOverviewPanelCtrl.prototype.getOverviewCounts = function(){
		var self = this;
		_sdWorkflowOverviewService.getWorkflowOverviewCounts().then(function(data){
			self.workflowOverviewCounts = data;
		},function(error){
			trace.error(error);
		});
	};

	/**
	 *
	 */
	WorkflowOverviewPanelCtrl.prototype.openDirectUserWorkActionHTML5 = function(){
		 var self = this;

      var params = {"id" : self.userInfo.id,
		    		"type" : "personal",
		    		"name": self.userInfo.displayName
		    	   };
      _sdViewUtilService.openView("worklistPanel","id=" + self.userInfo.id, params);
	};

	/**
	 *
	 */
	WorkflowOverviewPanelCtrl.prototype.openAllAssignedActivitiesActionHTML5 = function(){
		 var self = this;

      var params = {"id" : "allActivities",
    		        "type" : "allAssigned",
    		        "name": self.userInfo.displayName
		    	   };
      _sdViewUtilService.openView("worklistPanel","id=allActivities", params);

	};

	/**
	 *
	 */
	WorkflowOverviewPanelCtrl.prototype.openCriticalActivitiesActionHTML5 = function(){
		 var self = this;

      var params = {"id" : "criticalActivities",
		            "type" : "highCriticality",
		    		"name": self.userInfo.displayName
		    		};
      _sdViewUtilService.openView("worklistPanel","id=criticalActivities", params);

	};

	/**
	 *
	 */
	WorkflowOverviewPanelCtrl.prototype.dateIdChange = function(){
		var self = this;

	    if("-" === self.dateId){
	         return;
	      }
	      var name = _sdI18nService.translate('processportal.launchPanels-workflowOverview-pastProcessInstances.' + self.dateId);
	      var params = { "id" : "lastNWorkedOn",
	    		         "name" : name,
	    		         "from" : self.dateId,
	    		         "type" : "myWorkSince"
			    	 };
	      _sdViewUtilService.openView("worklistPanel","id=" + "lastNWorkedOn" + "&dateID=" + self.dateId, params);
	};

	/**
	 *
	 */
	WorkflowOverviewPanelCtrl.prototype.refreshOverviewPanel = function(){
		this.getOverviewCounts();
	};

})();


