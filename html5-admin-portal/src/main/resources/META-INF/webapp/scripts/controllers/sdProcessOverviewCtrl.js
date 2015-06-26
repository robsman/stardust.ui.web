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

	angular.module('admin-ui').controller('sdProcessOverviewCtrl',
			[ '$q', '$filter', 'sdProcessInstanceService', 'sdLoggedInUserService', Controller ]);

	/*
	 * 
	 */
	function Controller($q, $filter, sdProcessInstanceService, sdLoggedInUserService) {

		this.initialize(sdProcessInstanceService, sdLoggedInUserService);

		/*
		 * 
		 */
		Controller.prototype.getData = function(params) {

			var self = this;
			var deferred = $q.defer();
			self.processList = {};

			this.fetchProcessInstanceCounts(sdProcessInstanceService);

			sdProcessInstanceService.getProcesslist(params).then(function(data) {
				self.processList.list = data.list;
				self.processList.totalCount = data.totalCount;

				deferred.resolve(self.processList);
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

	}

	/**
	 * 
	 */
	Controller.prototype.fetchProcessInstanceCounts = function(sdProcessInstanceService) {
		var self = this;

		sdProcessInstanceService.getProcessInstanceCounts().then(function(result) {
			self.count = result;
		});
	}

	/*
	 * 
	 */
	Controller.prototype.initialize = function(sdProcessInstanceService, sdLoggedInUserService) {

		this.count = {
			active : '',
			interrupted : '',
			aborted : '',
			completed : '',
			total : ''
		};

		this.processList = {
			list : [],
			totalCount : 0
		};

		this.dataTable = null; // This will be set to underline data
		this.columnSelector = sdLoggedInUserService.getUserInfo().isAdministrator ? 'admin' : true;

		this.fetchProcessInstanceCounts(sdProcessInstanceService);
	};

})();