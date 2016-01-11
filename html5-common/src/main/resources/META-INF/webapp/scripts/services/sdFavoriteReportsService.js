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
 * @author Abhay.Thappan
 */

(function() {
	'use strict';

	angular.module('bpm-common.services').provider(
			'sdFavoriteReportsService',
			function() {
				this.$get = [ '$resource', 'sdLoggerService', 'sdUtilService',
						function($resource, sdLoggerService, sdUtilService) {
							var service = new FavoriteReportsService($resource, sdLoggerService, sdUtilService);
							return service;
						} ];
			});

	/*
	 * 
	 */
	function FavoriteReportsService($resource, sdLoggerService, sdUtilService) {
		var REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/favorites-reports";
		var trace = sdLoggerService.getLogger('bpm-common.services.sdFavoriteReportsService');

		/**
		 * 
		 */
		FavoriteReportsService.prototype.getAllFavoriteReports = function() {
			// Prepare URL
			var restUrl = REST_BASE_URL;

			var favoriteReports = $resource(restUrl, {}, {
				getAllFavoriteReports : {
					method : 'GET',
					isArray : true
				}
			});
			return favoriteReports.getAllFavoriteReports().$promise;

		};

		/**
		 * 
		 */
		FavoriteReportsService.prototype.removeFromFavoriteReports = function(documentId) {
			// Prepare URL
			var restUrl = REST_BASE_URL + '/:documentId';

			var reportResource = $resource(restUrl, {
				documentId : '@documentId'
			}, {
				removeFromFavorite : {
					method : 'DELETE'
				}
			});

			var urlTemplateParams = {};
			urlTemplateParams.documentId = documentId;
			return reportResource.removeFromFavorite(urlTemplateParams).$promise;

		};

	}
	;
})();
