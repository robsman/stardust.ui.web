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
 * @author Nikhil.Gahlot
 */

(function() {
	'use strict';

	angular.module('workflow-ui').controller('sdProcessTableViewCtrl', 
			['$scope', 'sdUtilService', 'sdViewUtilService', ProcessTableViewCtrl]);

	var _sdViewUtilService;
	
	/*
	 * 
	 */
	function ProcessTableViewCtrl($scope, sdUtilService, sdViewUtilService) {
		// Register for View Events
		sdViewUtilService.registerForViewEvents($scope, this.handleViewEvents, this);

		// Preserve to use later in life-cycle
		_sdViewUtilService = sdViewUtilService;

		this.initialize();
		
		/*
		 * This needs to be defined here as it requires access to $scope
		 */
		ProcessTableViewCtrl.prototype.safeApply = function() {
			sdUtilService.safeApply($scope);
		};
	}

	/*
	 * 
	 */
	ProcessTableViewCtrl.prototype.handleViewEvents = function(event) {
		if (event.type == "ACTIVATED") {
			this.refresh();
		} else if (event.type == "DEACTIVATED") {
			// TODO
		}
	};

	/*
	 * 
	 */
	ProcessTableViewCtrl.prototype.initialize = function() {
		this.dataTable = null; // This will be set to underline data table instance automatically
	};

	/*
	 * 
	 */
	ProcessTableViewCtrl.prototype.refresh = function() {
		this.dataTable.refresh(true);
	};
})();