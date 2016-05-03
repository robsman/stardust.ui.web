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

	angular.module("bcc-ui").controller(
			'sdResourceLoginCtrl',
			[ 'sdResourceLoginService', 'sdCommonViewUtilService', '$q', 'sdLoggerService', '$filter', 'sgI18nService', ResourceLoginCtrl ]);

	var _sdResourceLoginService = null;
	var _q = null;
	var trace = null;
	var _filter = null;
	var _sgI18nService = null;
	var _sdCommonViewUtilService = null;

	/**
	 * 
	 */
	function ResourceLoginCtrl(sdResourceLoginService, sdCommonViewUtilService, $q, sdLoggerService, $filter,
			sgI18nService) {

		_sdResourceLoginService = sdResourceLoginService;
		_q = $q;
		trace = sdLoggerService.getLogger('bcc-ui.sdResourceLoginCtrl');
		_filter = $filter;
		_sgI18nService = sgI18nService;
		_sdCommonViewUtilService = sdCommonViewUtilService;

		this.exportFileName = "Resource Login";
		this.dataTable = null;
	}
	;
    /**
     * 
     * @param options
     * @returns
     */
	ResourceLoginCtrl.prototype.getResourceLoginTimeInfo = function(options) {
		var self = this;
		var deferred = _q.defer();
		_sdResourceLoginService.getResourceLoginTimeInfo().then(function(result) {
			deferred.resolve(result);
		}).then(function(failure) {
			trace.log('Failed to retrive Resource Login Time info.', failure);
			deferred.reject(failure);
		});
		return deferred.promise;
	};

	/**
	 * 
	 */
	ResourceLoginCtrl.prototype.refresh = function() {
		this.dataTable.refresh();
	};

	/**
	 * 
	 * @param userOid
	 * @param userId
	 */
	ResourceLoginCtrl.prototype.openUserManagerView = function(userOid, userId) {
		_sdCommonViewUtilService.openUserManagerDetailView(userOid, userId, true);
	};

})();