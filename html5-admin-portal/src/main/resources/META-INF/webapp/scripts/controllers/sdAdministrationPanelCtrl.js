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

	angular.module("admin-ui").controller('sdAdministrationPanelCtrl',
			[ 'sdLoggerService', 'sdViewUtilService', AdministrationPanelCtrl ]);

	var trace;
	var _sdViewUtilService;

	/**
	 * 
	 */
	function AdministrationPanelCtrl(sdLoggerService, sdViewUtilService) {
		trace = sdLoggerService.getLogger('admin-ui.sdAdministrationPanelCtrl');
		_sdViewUtilService = sdViewUtilService;
		this.showPanelContent = true;
	}
	/**
	 * 
	 * @param viewId
	 */
	AdministrationPanelCtrl.prototype.openView = function(viewId) {
		_sdViewUtilService.openView(viewId, null, null, false);
	}
})();