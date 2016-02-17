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
			['$scope','sdWorkflowOverviewService', 'sdLoggerService', 'sdViewUtilService',
					'sdLoggedInUserService', 'sdI18nService','sdWorklistConstants', 'sgPubSubService','sdSidebarService', WorkflowOverviewPanelCtrl ]);
	var _sdWorkflowOverviewService;
	var _sdViewUtilService;
	var trace;
	var _sdI18nService;
	var _sdWorklistConstants;
	/**
	 *
	 */
	function WorkflowOverviewPanelCtrl($scope, sdWorkflowOverviewService, sdLoggerService, sdViewUtilService, 
			 sdLoggedInUserService, sdI18nService, sdWorklistConstants, sgPubSubService, sdSidebarService) {
		trace = sdLoggerService.getLogger('workflow-ui.sdWorkflowOverviewPanelCtrl');
		_sdWorkflowOverviewService = sdWorkflowOverviewService;
		_sdViewUtilService = sdViewUtilService;
		_sdI18nService = sdI18nService;
		_sdWorklistConstants = sdWorklistConstants;
		this.userInfo = sdLoggedInUserService.getUserInfo();
		this.collapsePanelHandle = null;
		var self = this;
		
		sgPubSubService.subscribe("sdActivePerspectiveChange", function(){
			var activePerspective = sdSidebarService.getActivePerspectiveName();
			if(self.collapsePanelHandle.expanded() && activePerspective === "WorkflowExecution"){
				if(self.syncPanel == true){
					self.syncPanel = false;
					self.getOverviewCounts();
				}
				
			}
		});
		
		sgPubSubService.subscribe('sdRefreshLaunchPanel', function(){
			var activePerspective = sdSidebarService.getActivePerspectiveName();
			if(self.collapsePanelHandle.expanded() && activePerspective === "WorkflowExecution"){
				self.getOverviewCounts();
			}else{
				self.syncPanel = true;
			}				
		});
		
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
		    		"type" : _sdWorklistConstants.Types["PERSONAL"],
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
    		        "type" : _sdWorklistConstants.Types["TOTAL"],
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
		            "type" : _sdWorklistConstants.Types["HIGH_CRITICALITY"],
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
	    		         "type" : _sdWorklistConstants.Types["WORK_SINCE"],
			    	 };
	      _sdViewUtilService.openView("worklistPanel","id=" + "lastNWorkedOn" + "&dateID=" + self.dateId, params);
	};

	/**
	 *
	 */
	WorkflowOverviewPanelCtrl.prototype.refreshOverviewPanel = function(){
		this.getOverviewCounts();
	};
	
	/**
	 * 
	 */
	WorkflowOverviewPanelCtrl.prototype.refreshPanelToSync = function() {
		var self = this;
		if(self.syncPanel){
			self.syncPanel = false;
			self.getOverviewCounts();
		}		
	};

})();


