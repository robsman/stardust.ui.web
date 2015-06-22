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
 * 
 */

(function() {
	'use strict';

	angular.module('bcc-ui.services').provider(
			'sdResourceLoginService',
			function() {
				this.$get = [
						'$resource',
						'sdLoggerService',
						'sdUtilService',
						function($resource, sdLoggerService, sdUtilService) {
							var service = new ResourceLoginService($resource, sdLoggerService,
								sdUtilService);
							return service;
						} ];
			});

	/*
	 * 
	 */
	function ResourceLoginService($resource, sdLoggerService, sdUtilService) {
		var REST_BASE_URL = "services/rest/portal/loginTimeInfo";
		var trace = sdLoggerService.getLogger('bcc-ui.services.sdResourceLoginService');

		/**
		 * 
		 */
		ResourceLoginService.prototype.getResourceLoginTimeInfo = function(query) {
			var restUrl = REST_BASE_URL	
			return sdUtilService.ajax(restUrl, '', '');

		};

	}
	;
})();
