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

	angular.module("benchmark-app").controller('sdBenchmarkPanelCtrl',
			[ 'sdLoggerService', 'sdViewUtilService', BenchmarkPanelCtrl ]);

	var trace;
	var _sdViewUtilService;

	/**
	 * 
	 */
	function BenchmarkPanelCtrl(sdLoggerService, sdViewUtilService) {
		trace = sdLoggerService.getLogger('benchmark-app.sdBenchmarkPanelCtrl');
		_sdViewUtilService = sdViewUtilService;
		this.tlvViewParams = {
			'preferenceName' : 'New TLV'
		};
	}
	/**
	 * 
	 * @param viewId
	 * @param params
	 */
	BenchmarkPanelCtrl.prototype.openView = function(viewId, params) {
		_sdViewUtilService.openView(viewId, null, params, false);
	}
})();