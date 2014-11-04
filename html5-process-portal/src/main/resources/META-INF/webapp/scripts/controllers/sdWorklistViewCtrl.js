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

	var _sdViewUtilService;
	
	/*
	 * 
	 */
	function WorklistViewCtrl($scope, sdUtilService, sdViewUtilService) {
		// Register for View Events
		sdViewUtilService.registerForViewEvents($scope, this.handleViewEvents, this);

		// Preserve to use later in life-cycle
		_sdViewUtilService = sdViewUtilService;

		this.initialize();
		
		/*
		 * This needs to be defined here as it requires access to $scope
		 */
		WorklistViewCtrl.prototype.safeApply = function() {
			sdUtilService.safeApply($scope);
		};
	}

	/*
	 * 
	 */
	WorklistViewCtrl.prototype.handleViewEvents = function(event) {
		if (event.type == "ACTIVATED") {
			this.refresh();
		} else if (event.type == "DEACTIVATED") {
			// TODO
		}
	};

	/*
	 * 
	 */
	WorklistViewCtrl.prototype.initialize = function() {
		this.dataTable = null; // This will be set to underline data table instance automatically
	};

	/*
	 * 
	 */
	WorklistViewCtrl.prototype.refresh = function() {
		this.dataTable.refresh(true);
	};
	
	angular.module('workflow-ui').controller('sdWorklistViewCtrl', 
			['$scope', 'sdUtilService', 'sdViewUtilService', WorklistViewCtrl]);
})();