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
			'sdWorkflowWorklistPanelCtrl',
			[ 'sdWorkflowWorklistService', 'sdLoggerService', 'sdViewUtilService',
					'sdI18nService', 'sdActivityInstanceService', 'sdDialogService', 'sdCommonViewUtilService',
					'$scope', 'sgPubSubService', 'sdSidebarService', '$timeout', 'sdUtilService',
					'sdWorkflowOverviewService' , WorkflowWorklistPanelCtrl ]);
	var _sdWorkflowWorklistService;
	var _sdViewUtilService;
	var trace;
	var _sdActivityInstanceService;
	var _sdI18nService;
	var _sdDialogService;
	var _sdCommonViewUtilService;
	var _scope;
	var _timeout;
	var _sdUtilService;
	var _sgPubSubService;
	var _sdWorkflowOverviewService;
	/**
	 *
	 */
	function WorkflowWorklistPanelCtrl(sdWorkflowWorklistService, sdLoggerService, sdViewUtilService,
			sdI18nService, sdActivityInstanceService, sdDialogService, sdCommonViewUtilService,
			$scope, sgPubSubService, sdSidebarService, $timeout, sdUtilService,
			sdWorkflowOverviewService) {
		trace = sdLoggerService.getLogger('workflow-ui.sdWorkflowWorklistPanelCtrl');
		_sdWorkflowWorklistService = sdWorkflowWorklistService;
		_sdViewUtilService = sdViewUtilService;
		_sdI18nService = sdI18nService;
		_sdActivityInstanceService = sdActivityInstanceService;
		_sdDialogService = sdDialogService;
		_sdCommonViewUtilService = sdCommonViewUtilService;
		_scope = $scope;
		_timeout = $timeout;
		_sdUtilService = sdUtilService;
		_sgPubSubService = sgPubSubService;
		_sdWorkflowOverviewService = sdWorkflowOverviewService;
		this.showEmptyWorklists = false;

		this.collapsePanelHandle = null;
		var self = this;

		_sgPubSubService.subscribe('sdRefreshLaunchPanel', function() {
			var activePerspective = sdSidebarService.getActivePerspectiveName();
			if(self.collapsePanelHandle.expanded() && activePerspective === "WorkflowExecution"){
				self.getUserAssignments(self.showEmptyWorklists);
			}
		});
	}

	/**
	 *
	 */
	WorkflowWorklistPanelCtrl.prototype.getUserAssignments = function(showEmptyWorklist) {
		var self = this;
		_sdWorkflowWorklistService.getUserAssignments(showEmptyWorklist).then(function(data) {
			self.workflowMyAssignments = data.list;
			//build our set of user icon classes and insert them in the dom
			self.createUserIcons(data.list);
			// Added this logic to expand the parent node by default.
			_timeout(function(){
				self.workflowMyAssignments.forEach(function(view){
		          self.treeApi.expandNode(view.uuid);
		        });
		      },0);
		}, function(error) {
			trace.error(error);
		});
	};

	/**
	 * Given our list of data returned form getUserAssignments, recursively parse
	 * the list and build out a style tag containing customized icon classes for those
	 * that users whose icon supports a profile portrait.
	 * @param  {[type]} list [description]
	 * @return {[type]}      [description]
	 */
	WorkflowWorklistPanelCtrl.prototype.createUserIcons = function(list){

		var that = this;
		var iconMap = {};
		var style;
		var fx;

		fx = function(nodes){
			nodes.forEach(function(item){
				iconMap[item.uuid] = that.createUserStyle(item);
				if(item.children && item.children.length > 0){
					fx(item.children);
				}
			});
		}
		fx(list);

		if(Object.keys(iconMap).length > 0){

			style = document.createElement('style');
			style.type = 'text/css';

			for(var uuid in iconMap){
				if(iconMap[uuid]){
					style.innerHTML += iconMap[uuid];
				}
			}

			document.getElementsByTagName('head')[0].appendChild(style);
		}
	}

	/**
	 * Unit-Of-Work function to build a set of classes that allow user icons to
	 * appear within our sdTree structure.
	 * @param  {[type]} item    [description]
	 * @param  {[type]} hashMap [description]
	 * @return {[type]}         [description]
	 */
	WorkflowWorklistPanelCtrl.prototype.createUserStyle =function(item,hashMap){

		var style;
		var cssText;

		if(item.icon.indexOf("/")==0){

			cssText = "";

			// First css rule to take care of non hover appearance
			cssText += ".node-" + item.uuid
				    + " + span"
				    + "{color:#2a5db0;"
				    + "background: url("
					+ _sdUtilService.getRootUrl() + item.icon
					+ ") left no-repeat !important; background-size: 12px 12px !important;"
					+ "padding-left: 1.2em !important;}";

			// second css rule to take care of hover otherwise the image will
			// disappear on hover using the default css
			cssText += ".node-" + item.uuid
			        + " + span:hover "
			        + "{background: url("
					+ _sdUtilService.getRootUrl() + item.icon
					+ ") left no-repeat !important;  background-size: 12px 12px !important;}";

			return 	cssText;
		}
		else{
			return false;
		}
		
	}

	/**
	 *
	 */
	WorkflowWorklistPanelCtrl.prototype.refreshMyAssignmentPanel = function(showEmptyWorklist) {
		_sdWorkflowOverviewService.resetCache().then(function(){
			_sgPubSubService.publish('sdRefreshLaunchPanel');
		});
	};

	/**
	 *
	 */
	WorkflowWorklistPanelCtrl.prototype.refreshPanelToSync = function() {
		var self = this;
		if(self.syncPanel){
			self.syncPanel = false;
			self.getUserAssignments(self.showEmptyWorklists, false);
		}
	};


	WorkflowWorklistPanelCtrl.prototype.eventCallback = function(data, e) {
		var self = this;
		if (data.treeEvent === "node-expand") {
			// complete tree is already loaded
			data.valueItem.isLoaded = true;
			data.deferred.resolve();
		} else if (data.treeEvent === "node-click" && data.valueItem.children == undefined
				&& data.valueItem.isAssemblyLineParticipant) {
			_sdWorkflowWorklistService.getNextAssemblyLineActivity().then(function(data) {
				self.assemblyLineActivity = data;
				self.openActivity(self.assemblyLineActivity);
			}, function(error) {
				trace.error(error);
			});
		} else if (data.treeEvent === "node-click" && data.valueItem.children == undefined) {
			var params = {
				"userId" : data.valueItem.userId,
				"id" : data.valueItem.id,
				"participantQId" : data.valueItem.participantQId,
				"departmentQId" : data.valueItem.departmentQId,
				"name" : data.valueItem.labelName
			};

			if(!data.valueItem.participantQId) {
				params['type'] = "personal";
			}
			_sdViewUtilService.openView("worklistPanel", "id=" + data.valueItem.viewKey, params);

		} else if (data.treeEvent === "node-click" && data.valueItem.children != undefined) {
			var params = {
				"type" : "unified",
				"userId" : data.valueItem.userId,
				"id" : data.valueItem.id,
				"name" : data.valueItem.labelName
			};
			_sdViewUtilService.openView("worklistPanel", "id=" + data.valueItem.viewKey, params);
		} else if (data.treeEvent==="node-collapse") {
		       data.deferred.resolve();
	    }
	};

	/**
	 *
	 * @param name
	 * @param activityCount
	 * @returns
	 */
	WorkflowWorklistPanelCtrl.prototype.getLabel = function(name, activityCount) {
		name = name.length > 30 ? name.substring(0,30) + "..." : name;
		return name +': ' + _sdI18nService.translate("processportal.launchPanels.worklists.items", "", [ activityCount ]);
	};
    /**
     *
     * @param ai
     */
	WorkflowWorklistPanelCtrl.prototype.openActivity = function(ai) {
		if (ai.activity != undefined) {
			if (ai.defaultCaseActivity) {
				var params = {
					"processInstanceOID" : "" + ai.processInstanceOID,
					"assemblyLineActivity" : true
				};

				_sdViewUtilService.openView('caseDetailsView', "processInstanceOID=" + ai.processInstanceOID, params,
						false);

				return;
			}
			_sdActivityInstanceService.activate(ai.activityOID).then(
					function(data) {
						_sdCommonViewUtilService.openActivityView(ai.activityOID);
					},
					function(result) {
						trace.error("Error in activating worklist item : ", ai.activityOID, ".Error : ",
								result.failure[0].message);
						var options = {
							title : sgI18nService.translate('views-common-messages.common-error', 'Error')
						};
						var message = result.failure[0].message;
						_sdDialogService.error(_scope, message, options);
					});
		}
	};

	/**
	 *
	 * @param item
	 * @returns
	 */
	WorkflowWorklistPanelCtrl.prototype.iconCallback = function(item) {
		if (item.icon.indexOf("/") > -1) {
			var css = [ "pi", "pi-lg" ];
			css.push("node-" + item.uuid);
			return css.join(" ");
		} else {
			return item.icon;
		}
	};


	/**
	 * Getting tree API handler.
	 *
	 * @param api
	 */
     WorkflowWorklistPanelCtrl.prototype.onTreeInit = function(api){
		 var self = this;
		 self.treeApi = api;
	  };

})();
