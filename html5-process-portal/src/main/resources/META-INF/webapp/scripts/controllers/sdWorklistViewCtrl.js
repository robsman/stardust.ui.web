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

	var _sdViewUtilService, _sdWorklistService, _sdActivityInstanceService, _sdProcessDefinitionService;
	
	/*
	 * 
	 */
	function WorklistViewCtrl($scope, sdUtilService, sdViewUtilService, sdWorklistService, sdActivityInstanceService,
			sdProcessDefinitionService) {
		// Register for View Events
		sdViewUtilService.registerForViewEvents($scope, this.handleViewEvents, this);

		// Preserve to use later in life-cycle
		_sdViewUtilService = sdViewUtilService;
		_sdWorklistService = sdWorklistService;
		_sdActivityInstanceService = sdActivityInstanceService;
		_sdProcessDefinitionService = sdProcessDefinitionService;

		/*
		 * This needs to be defined here as it requires access to $scope
		 */
		WorklistViewCtrl.prototype.safeApply = function() {
			if ($scope.$root.$$phase !== '$apply' || $scope.$root.$$phase !== '$digest') {
				$scope.$apply();
			}
		};

		this.initialize(sdViewUtilService.getViewParams($scope));
		
		var self = this;
		// This is needed because $scope is not accessible later in instance methods!
		$scope.$watch("worklistDataTable", function(newVal, oldVal) {
			if (newVal != undefined && newVal != null && newVal != oldVal) {
				self.dataTable = $scope.worklistDataTable;
				// TODO: Can unregister here, to reduce the watchers!
			}
		});
	}

	/*
	 * 
	 */
	WorklistViewCtrl.prototype.initialize = function(viewParams) {
		// Initialize params
		this.query = {};
		if (viewParams.participantQId) {
		   this.query.participantQId = viewParams.participantQId;
		} else if (viewParams.userId) {
		   this.query.userId = viewParams.userId;
		}

		this.worklist = {};
		this.worklist.selectedWorkItems = [];

		this.fetchDescriptorCols();
	};

	/*
	 * 
	 */
	WorklistViewCtrl.prototype.handleViewEvents = function(event) {
		if (event.type == "ACTIVATED") {
			this.refresh();
		} else if (event.type == "DEACTIVATED") {
			
		}
	};

	/*
	 * 
	 */
	WorklistViewCtrl.prototype.refresh = function() {
		this.dataTable.refresh(true);
	};
	
	/*
	 * 
	 */
	WorklistViewCtrl.prototype.fetchPage = function(options) {
		var self = this;
		var deferred = jQuery.Deferred();

		var query = angular.extend({}, this.query);
		query.options = options;

		this.worklist.selectedWorkItems = [];

		_sdWorklistService.getWorklist(query).done(function(data) {
			self.worklist.list = data.list;
			self.worklist.totalCount = data.totalCount;
			
			var oids = [];
			angular.forEach(self.worklist.list, function(workItem, index){
				if (workItem.trivial == undefined || workItem.trivial) {
					oids.push(workItem.oid);
				}
			});

			_sdActivityInstanceService.getTrivialManualActivitiesDetails(oids).done(function(data) {
				self.worklist.trivialManualActivities = data;

				deferred.resolve(self.worklist);

				self.safeApply();
			});
		});

		return deferred.promise();
	};

	/*
	 * 
	 */
	WorklistViewCtrl.prototype.fetchDescriptorCols = function() {
		var self = this;

		_sdProcessDefinitionService.getDescriptorColumns().then(function(descriptors) {
			self.descritorCols = [];
			angular.forEach(descriptors, function(descriptor){
				self.descritorCols.push({
					field: "descriptors['" + descriptor.id + "'].value",
					title: descriptor.title,
					dataType: descriptor.type,
					sortable: descriptor.sortable
				});
			});
			
			self.ready = true;
			self.safeApply();
		});
	};

	/*
	 * 
	 */
	WorklistViewCtrl.prototype.activateWorkItem = function(workItem) {
		_sdViewUtilService.openView("activityPanel", "OID=" + workItem.oid, {"oid" : "" + workItem.oid});
	};

	/*
	 * 
	 */
	WorklistViewCtrl.prototype.openNotes = function(workItem) {
		_sdViewUtilService.openView("notesPanel", "oid=" + workItem.processInstance.oid, 
				{"oid": "" + workItem.processInstance.oid}, true);
	};

	/*
	 * 
	 */
	WorklistViewCtrl.prototype.openProcessHistory = function(workItem) {
		_sdViewUtilService.openView("processInstanceDetailsView", 
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
		_sdActivityInstanceService.completeAll([activityData]).done(function(data) {
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
			
			_sdActivityInstanceService.completeAll(activitiesData).done(function(data) {
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
			 'sdProcessDefinitionService', WorklistViewCtrl]);
})();