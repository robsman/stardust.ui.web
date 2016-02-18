/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * @author Subodh.Godbole
 */

(function() {
	/**
	 * Request Interceptor for enabling cross domain ajax calls via angular $resource / $http
	 */
	angular.module('bpm-ui').factory('corssDomainRequestInterceptor', function() {
		return {
			'request' : function(config) {
				config.crossDomain = true;
				config.withCredentials = true;
				return config;
			}
		}
	});

	angular.module('bpm-ui').config(['$httpProvider', function ($httpProvider) {
    	$httpProvider.interceptors.push('corssDomainRequestInterceptor');
    }]);

	/**
	 * For enabling cross domain ajax calls via jQuery.ajax
	 */
	if (jQuery && jQuery.ajax) {
		jQuery.ajaxSetup({
			crossDomain: true,
			xhrFields : {
				withCredentials: true
			}
		});
	}
})();