/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Abhay.Thappan
 */

(function() {
	'use strict';

	angular.module("bcc-ui").controller('sdStrandedActivitiesCtrl',
			[ '$q', 'sdStrandedActivitiesService', 'sdLoggerService', StrandedActivitiesCtrl ]);

	var _q;
	var _sdStrandedActivitiesService
	var trace;

	/**
	 * 
	 */
	function StrandedActivitiesCtrl($q, sdStrandedActivitiesService, sdLoggerService) {
		trace = sdLoggerService.getLogger('bcc-ui.sdDeputyManagementCtrl');
		_q = $q;
		_sdStrandedActivitiesService = sdStrandedActivitiesService;

		this.strandedActivitiesTable = null;
	}

	/**
	 * 
	 * @returns
	 */
	StrandedActivitiesCtrl.prototype.getStrandedActivities = function(params) {
		var deferred = _q.defer();
		var self = this;
		self.strandedActivities = {};
		_sdStrandedActivitiesService.getStrandedActivities(params).then(function(data) {
			self.strandedActivities.list = data.list;
			self.strandedActivities.totalCount = data.totalCount;
			deferred.resolve(self.strandedActivities);
		}, function(error) {
			trace.log(error);
			deferred.reject(error);
		});

		return deferred.promise;
	};
	
	StrandedActivitiesCtrl.prototype.refresh = function(){
		var self = this;
		self.strandedActivitiesTable.refresh();
	};
})();