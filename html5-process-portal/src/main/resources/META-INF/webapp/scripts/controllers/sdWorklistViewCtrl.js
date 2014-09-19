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

angular.module('workflow-ui').controller('sdWorklistViewCtrl', ['$scope', 'sdViewUtilService', 'sdWorkflowService', function($scope, sdViewUtilService, sdWorkflowService) {
	var viewParams = sdViewUtilService.getViewParams($scope);

	var query = {
		participantQId : viewParams.participantQId
	};
	
	$scope.worklist = {};
	$scope.worklist.selectedWorkItems = [];

	/*
	 * 
	 */
	$scope.refresh = function() {
		sdWorkflowService.getWorklist(query).done(function(data) {
			$scope.worklist.workItems = data.list;
			$scope.worklist.totalCount = data.totalCount;
			$scope.$apply();
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
				{"oid": "" + workItem.oid}, true);
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
	};

	/*
	 * 
	 */
	$scope.openDelegateDialog = function(workItem) {
	};
}]);
