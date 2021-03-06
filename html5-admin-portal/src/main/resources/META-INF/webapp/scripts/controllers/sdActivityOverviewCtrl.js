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
 * @author Johnson.Quadras
 */

(function() {
	'use strict';

	angular.module('admin-ui').controller('sdActivityOverviewCtrl',
			['$scope', '$q', '$filter', 'sdActivityInstanceService','sdViewUtilService', Controller]);

	/*
	 *
	 */
	function Controller($scope, $q, $filter, sdActivityInstanceService, sdViewUtilService) {

		// Register for View Events
		sdViewUtilService.registerForViewEvents($scope, this.handleViewEvents, this);

		this.initialize(sdActivityInstanceService);

		/*
		 *
		 */
		Controller.prototype.getData = function(params) {

			var self = this;
			var deferred = $q.defer();
			self.activities = {};

			this.fetchActivityInstanceCounts(sdActivityInstanceService);

			sdActivityInstanceService.getAllActivities(params).then(
					function(data) {
						self.activities.list = data.list;
						self.activities.totalCount = data.totalCount;
						deferred.resolve(self.activities);
					}, function(error) {
						deferred.reject(error);
					});

			return deferred.promise;
		}

		/*
		 *
		 */
		Controller.prototype.refresh = function() {
			this.dataTable.refresh(true);
		};

		this.refreshRequired = false;

	}

	/**
	 *
	 */
	Controller.prototype.registerRefreshRequired = function() {
		this.refreshRequired = true;
	};

	/*
	 *
	 */
	Controller.prototype.handleViewEvents = function(event) {
		if (event.type == "ACTIVATED") {
			if(this.refreshRequired) {
				this.refresh();
				this.refreshRequired = false;
			}
		}
	};

	/**
	 *
	 */
	Controller.prototype.fetchActivityInstanceCounts = function(
			sdActivityInstanceService) {
		var self = this;

		sdActivityInstanceService.getAllCounts().then(function(result) {
			self.count = result;
		});
	}

	/*
	 *
	 */
	Controller.prototype.initialize = function(sdActivityInstanceService) {

		this.count = {
			active : '',
			waiting : '',
			aborted : '',
			completed : '',
			total : '',
			halted : ''
		};

		this.activities = {
			list : [],
			totalCount : 0
		};

		this.dataTable = null; // This will be set to underline data
	};

})();
