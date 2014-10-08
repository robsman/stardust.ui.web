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
 * @author Subodh.Godbole
 */

(function() {
	'use strict';

	/*
	 * 
	 */
	function WorklistViewCtrl($scope, sdUtilService, sdViewUtilService, sdWorklistService, sdActivityInstanceService) {
		// Register for View Events
		sdViewUtilService.registerForViewEvents($scope, this.$handleViewEvents, this);

		// Preserve to use later in life-cycle
		this.$sdViewUtilService = sdViewUtilService;
		this.$sdWorklistService = sdWorklistService;
		this.$sdActivityInstanceService = sdActivityInstanceService;

		this.$initialize(sdViewUtilService.getViewParams($scope));

		/*
		 * This needs to be defined here as it requires access to $scope
		 */
		WorklistViewCtrl.prototype.$safeApply = function() {
			if ($scope.$root.$$phase !== '$apply' || $scope.$root.$$phase !== '$digest') {
				$scope.$apply();
			}
		};

		// At last, expose required info on 'scope'
		sdUtilService.extend($scope, this);
	}

	/*
	 * 
	 */
	WorklistViewCtrl.prototype.$initialize = function(viewParams) {
		// Initialize params
		this.query = {};
		if (viewParams.participantQId) {
		   this.query.participantQId = viewParams.participantQId;
		} else if (viewParams.userId) {
		   this.query.userId = viewParams.userId;
		}

		this.worklist = {};
		this.worklist.selectedWorkItems = [];

		// Update
		this.refresh();
	};

	/*
	 * 
	 */
	WorklistViewCtrl.prototype.$handleViewEvents = function(event) {
		if (event.type == "ACTIVATED") {
			this.refresh();
		} else if (event.type == "DEACTIVATED") {
			
		}
	};

	/*
	 * 
	 */
	WorklistViewCtrl.prototype.refresh = function() {
		var self = this;

		this.worklist.selectedWorkItems = [];

		this.$sdWorklistService.getWorklist(this.query).done(function(data) {
			self.worklist.workItems = data.list;
			self.worklist.totalCount = data.totalCount;
			
			var oids = [];
			angular.forEach(self.worklist.workItems, function(workItem, index){
				if (workItem.trivial == undefined || workItem.trivial) {
					oids.push(workItem.oid);
				}
			});

			self.$sdActivityInstanceService.getTrivialManualActivitiesDetails(oids).done(function(data) {
				self.worklist.trivialManualActivities = data;
				self.$safeApply();
			});
		});
	};

	/*
	 * 
	 */
	WorklistViewCtrl.prototype.activateWorkItem = function(workItem) {
		this.$sdViewUtilService.openView("activityPanel", "OID=" + workItem.oid, {"oid" : "" + workItem.oid});
	};

	/*
	 * 
	 */
	WorklistViewCtrl.prototype.openNotes = function(workItem) {
		this.$sdViewUtilService.openView("notesPanel", "oid=" + workItem.processInstance.oid, 
				{"oid": "" + workItem.processInstance.oid}, true);
	};

	/*
	 * 
	 */
	WorklistViewCtrl.prototype.openProcessHistory = function(workItem) {
		this.$sdViewUtilService.openView("processInstanceDetailsView", 
				"processInstanceOID=" + workItem.processInstance.oid, 
				{
					"oid": "" + workItem.oid,
					"processInstanceOID": "" + workItem.processInstance.oid
				}, true
		);
	};

	/*
	 * 
	 */
	WorklistViewCtrl.prototype.complete = function(workItem) {
		var self = this;

		var outData = self.worklist.trivialManualActivities[workItem.oid].inOutData;
		var activityData = {oid: workItem.oid, outData: outData};
		this.$sdActivityInstanceService.completeAll([activityData]).done(function(data) {
			self.refresh();
		});
	};

	/*
	 * 
	 */
	WorklistViewCtrl.prototype.completeAll = function() {
		var self = this;

		if (this.worklist.selectedWorkItems.length > 0) {
			var activitiesData = [];
			angular.forEach(this.worklist.selectedWorkItems, function(workItem, index){
				var outData = self.worklist.trivialManualActivities[workItem.oid].inOutData;
				activitiesData.push({oid: workItem.oid, outData: outData});
			});
			
			this.$sdActivityInstanceService.completeAll(activitiesData).done(function(data) {
				self.refresh();
			});
		}
	};

	/*
	 * 
	 */
	WorklistViewCtrl.prototype.openDelegateDialog = function(workItem) {
		
	};

	angular.module('workflow-ui').controller('sdWorklistViewCtrl', 
			['$scope', 'sdUtilService', 'sdViewUtilService', 'sdWorklistService', 'sdActivityInstanceService', 
			 WorklistViewCtrl]);
})();