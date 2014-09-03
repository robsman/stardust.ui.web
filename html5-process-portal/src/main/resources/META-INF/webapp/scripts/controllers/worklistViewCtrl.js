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

angular.module('workflow-ui').controller('WorklistViewCtrl', ['$scope', 'sgViewParamService', 'workflowService', function($scope, sgViewParamService, workflowService) {
	console.log("View Params =");
	console.log(sgViewParamService.getParams($scope));

	$scope.worklist = {};

	// Invoke service to get the Data
	$scope.worklist.tableData = [
  	   {name: 'Activity A', processName: 'Process A', oid: 122},
	   {name: 'Activity B', processName: 'Process B', oid: 456},
	   {name: 'Activity C', processName: 'Process C', oid: 789}
	];

	$scope.worklist.activate = function(activity) {
		workflowService.activate(activity.oid);
	};
}]);
