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

	angular.module('workflow-ui').controller('sdWorklistViewCtrl', 
			['$scope', 'sdUtilService', 'sdViewUtilService', '$interval', 'sdPortalConfigurationService', WorklistViewCtrl]);

	var _sdViewUtilService;
	var  _$interval;
	var _sdPortalConfigurationService;
	
	
	/*
	 * 
	 */
	function WorklistViewCtrl($scope, sdUtilService, sdViewUtilService, $interval, sdPortalConfigurationService) {
	    	var self = this;
		// Register for View Events
		sdViewUtilService.registerForViewEvents($scope, this.handleViewEvents, this);

		// Preserve to use later in life-cycle
		_sdViewUtilService = sdViewUtilService;
		_$interval = $interval;
		_sdPortalConfigurationService = sdPortalConfigurationService;

		this.initialize();
		
		this.registerForAutoRefresh();
		
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
	WorklistViewCtrl.prototype.registerForAutoRefresh = function($interval) {
	    var self = this;
	    var refreshInterval = _sdPortalConfigurationService.getRefreshIntervalInMillis();
	    if (refreshInterval > 0) {
		this.timer = _$interval(function() {
		    if (self.dataTable) {
			self.dataTable.refresh(true);
		    }
		}, refreshInterval);
	    }
	};


	/*
	 * 
	 */
	WorklistViewCtrl.prototype.handleViewEvents = function(event) {
		if (event.type == "ACTIVATED") {
			this.refresh();
			this.registerForAutoRefresh();
		} else if (event.type == "DEACTIVATED") {
		    if(this.timer){
			_$interval.cancel(this.timer);
		    }
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
})();