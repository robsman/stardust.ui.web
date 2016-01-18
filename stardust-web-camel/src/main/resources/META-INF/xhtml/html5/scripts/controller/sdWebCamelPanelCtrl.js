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

	angular.module("camel-ui").controller('sdWebCamelPanelCtrl',
			[ 'sdLoggerService', 'sdViewUtilService', WebCamelPanelCtrl ]);

	var trace;
	var _sdViewUtilService;

	/**
	 * 
	 */
	function WebCamelPanelCtrl(sdLoggerService, sdViewUtilService) {
		trace = sdLoggerService.getLogger('camel-ui.sdWebCamelPanelCtrl');
		_sdViewUtilService = sdViewUtilService;
	}
	/**
	 * 
	 * @param viewId
	 */
	WebCamelPanelCtrl.prototype.openView = function(viewId) {
		_sdViewUtilService.openView(viewId, null, null, false);
	}
})();