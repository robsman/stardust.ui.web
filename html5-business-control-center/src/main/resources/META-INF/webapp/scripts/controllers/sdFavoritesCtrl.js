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

	angular.module("bcc-ui").controller('sdFavoritesCtrl',
			[ '$q', 'sdFavoriteViewService', 'sdLoggerService', 'sdViewUtilService', FavoritesCtrl ]);

	var _q;
	var _sdFavoriteViewService;
	var trace;
	var _sdViewUtilService;

	/**
	 * 
	 */
	function FavoritesCtrl($q, sdFavoriteViewService, sdLoggerService, sdViewUtilService) {
		trace = sdLoggerService.getLogger('bcc-ui.sdFavoritesCtrl');
		_q = $q;
		_sdFavoriteViewService = sdFavoriteViewService;
		_sdViewUtilService = sdViewUtilService;
		this.showPanelContent = true;
		this.getAllFavorites();
	}

	/**
	 * 
	 * @returns
	 */
	FavoritesCtrl.prototype.getAllFavorites = function() {
		var self = this;
		_sdFavoriteViewService.getAllFavorite().then(function(data) {
			self.favorites = data;
		}, function(error) {
			trace.log(error);
		});
	};
	
	FavoritesCtrl.prototype.refresh = function() {
		var self = this;
		self.getAllFavorites();
	}
	
	FavoritesCtrl.prototype.openView= function(preferenceId,preferenceName) {
		_sdViewUtilService.openView(preferenceId, "id=" + preferenceName, {
			"preferenceId" : "" + preferenceId,
			"preferenceName" : "" + preferenceName
		}, false);
	}
})();