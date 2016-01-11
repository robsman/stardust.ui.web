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

	angular.module("bcc-ui").controller('sdBCCMyFavoriteReportsPanelCtrl',
			['sdLoggerService', 'sdCommonViewUtilService', 'sdFavoriteReportsService', BCCMyFavoriteReportsPanel]);

	var _sdFavoriteReportsService;
	var trace;
	var _sdCommonViewUtilService;

	/**
	 * 
	 */
	function BCCMyFavoriteReportsPanel(sdLoggerService, sdCommonViewUtilService, sdFavoriteReportsService) {
		trace = sdLoggerService.getLogger('bcc-ui.sdBCCMyFavoriteReportsPanelCtrl');
		_sdFavoriteReportsService = sdFavoriteReportsService;
		_sdCommonViewUtilService = sdCommonViewUtilService;
		this.showPanelContent = true;
		this.getAllFavoriteReports();
	}

	/**
	 * 
	 * @returns
	 */
	BCCMyFavoriteReportsPanel.prototype.getAllFavoriteReports = function() {
		var self = this;
		_sdFavoriteReportsService.getAllFavoriteReports().then(function(data) {
			self.favoriteReports = data;
		}, function(error) {
			trace.log(error);
		});
	};
	
	BCCMyFavoriteReportsPanel.prototype.removeFromFavoriteReports = function(documentId) {
		var self = this;
		_sdFavoriteReportsService.removeFromFavoriteReports(documentId).then(function(data) {
			self.refresh();
		}, function(error) {
			trace.log(error);
		});
	};
	
	BCCMyFavoriteReportsPanel.prototype.refresh = function() {
		var self = this;
		self.getAllFavoriteReports();
	}
	
	BCCMyFavoriteReportsPanel.prototype.openView= function(documentId) {
		_sdCommonViewUtilService.openDocumentView(documentId, false);
	}
})();