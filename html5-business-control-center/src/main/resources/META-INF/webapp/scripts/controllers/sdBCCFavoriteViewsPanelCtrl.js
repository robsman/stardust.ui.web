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

	angular.module("bcc-ui").controller('sdBCCFavoriteViewsPanelCtrl',
			['sdLoggerService', 'sdViewUtilService', BCCFavoriteViewsPanelCtrl ]);

	var trace;
	var _sdViewUtilService;

	/**
	 * 
	 */
	function BCCFavoriteViewsPanelCtrl(sdLoggerService, sdViewUtilService) {
		trace = sdLoggerService.getLogger('bcc-ui.sdBCCFavoriteViewsPanelCtrl');
		_sdViewUtilService = sdViewUtilService;
		this.showMyFavorite = true;
	}
	
	BCCFavoriteViewsPanelCtrl.prototype.openView= function(viewId,params) {		
		_sdViewUtilService.openView(viewId, null, params, false);
	}
})();