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

'use strict';

angular.module('workflow-ui').controller('sdWorklistViewCtrl', 
		['$scope', 'sdViewUtilService', 'sdWorklistService', 'sdActivityInstanceService', 
		 function($scope, sdViewUtilService, sdWorklistService, sdActivityInstanceService) {

	// Register for View Events
	sdViewUtilService.registerForViewEvents($scope, handleViewEvents);

	/*
	 * 
	 */
	function handleViewEvents(event) {
		if (event.type == "ACTIVATED") {
			$scope.refresh();
		}
	};

	var viewParams = sdViewUtilService.getViewParams($scope);

	var query = {};
	
	if (viewParams.participantQId) {
	   query.participantQId = viewParams.participantQId;
	} else if (viewParams.userId) {
	   query.userId = viewParams.userId;
	}

	$scope.worklist = {};
	$scope.worklist.selectedWorkItems = [];
	
	/*
	 * 
	 */
	$scope.refresh = function() {
		$scope.worklist.selectedWorkItems = [];

		sdWorklistService.getWorklist(query).done(function(data) {
			$scope.worklist.workItems = data.list;
			$scope.worklist.totalCount = data.totalCount;
			
			var oids = [];
			angular.forEach($scope.worklist.workItems, function(workItem, index){
				if (workItem.trivial == undefined || workItem.trivial) {
					oids.push(workItem.oid);
				}
			});

			sdActivityInstanceService.getTrivialManualActivitiesDetails(oids).done(function(data) {
				console.log("Trivial Data =");
				console.log(data);
				$scope.worklist.trivialManualActivities = data;
				$scope.$apply();
			});
		});
	};
	
	$scope.refresh();

	/*
	 * 
	 */
	$scope.activateWorkItem = function(workItem) {
		sdViewUtilService.openView("activityPanel", "OID=" + workItem.oid, {"oid" : "" + workItem.oid});
	};

	/*
	 * 
	 */
	$scope.openNotes = function(workItem) {
		sdViewUtilService.openView("notesPanel", "oid=" + workItem.processInstance.oid, 
				{"oid": "" + workItem.processInstance.oid}, true);
	};

	/*
	 * 
	 */
	$scope.openProcessHistory = function(workItem) {
		sdViewUtilService.openView("processInstanceDetailsView", 
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
	$scope.complete = function(workItem) {
		var outData = $scope.worklist.trivialManualActivities[workItem.oid].inOutData;
		var activityData = {oid: workItem.oid, outData: outData};
		sdActivityInstanceService.completeAll([activityData]).done(function(data) {
			$scope.refresh();
		});
	};

	/*
	 * 
	 */
	$scope.completeAll = function() {
		console.log($scope.worklist.selectedWorkItems);
		if ($scope.worklist.selectedWorkItems.length > 0) {

			var activitiesData = [];
			angular.forEach($scope.worklist.selectedWorkItems, function(workItem, index){
				var outData = $scope.worklist.trivialManualActivities[workItem.oid].inOutData;
				activitiesData.push({oid: workItem.oid, outData: outData});
			});
			
			sdActivityInstanceService.completeAll(activitiesData).done(function(data) {
				$scope.refresh();
			});
		}
	};

	/*
	 * 
	 */
	$scope.openDelegateDialog = function(workItem) {
	};
}]);
