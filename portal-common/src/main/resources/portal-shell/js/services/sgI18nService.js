/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
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
	'use strict';

	angular.module('shell.services').provider('sgI18nService', function() {
		this.$get = ['$q', '$resource', 'sgConfigService', function($q, $resource, sgConfigService) {
			var service = new I18nService($q, $resource, sgConfigService);
			return service;
		}];
	});

	/*
	 *
	 */
	function I18nService($q, $resource, sgConfigService) {
		var messages;

		/*
		 * 
		 */
		I18nService.prototype.load = function(params) {
			return sgConfigService.then(function(config) {
				return fetchMessages(config.endpoints.i18n, config.locale, params);
			}, function() {
				alert('Failed in loading i18n Endpoint');
			});
		}
	
		/**
	     * 
	     */
		I18nService.prototype.translate = function(key, defVal) {
			var ret;
			if (messages) {
				var parts = key.split('.');
				if (parts.length > 1) {
					var msgNode;
					for (var i = 0; i < parts.length - 1; i++) {
						msgNode = messages[parts[i]];
					}

					ret = msgNode[parts[parts.length - 1]];
				} else {
					ret = messages[key];
				}
				
				if (!ret && (defVal != undefined || defVal != null)) {
					ret = defVal;
				}
			} else {
				ret = key;
			}

			return ret;
		}

		/*
		 * 
		 */
		function fetchMessages(endpoint, locale, params) {
			var deferred = $q.defer();
			endpoint += '/' + locale;
			$resource(endpoint).get(params,
				function(results) {
					messages = results[locale].translation;
					deferred.resolve();
				},
				function() {
					deferred.reject('Could not load messages' + endpoint);
				}
			);
			return deferred.promise;
		}
	}
})();
