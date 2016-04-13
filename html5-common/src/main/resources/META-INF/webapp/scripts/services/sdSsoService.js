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
 * @author Johnson.Quadras
 */

(function () {

	 angular.module('bpm-common.services').service('sdSsoService',
	 	['$injector', 'sdLoggerService', 'sdEnvConfigService', '$q','$interval' ,'sdSessionService',
	 	SsoService]);

	/*
	 *
	 */
	var DEFAULTS = {
			heatbeatInterval : 60
	}

	function SsoService ($injector, sdLoggerService, sdEnvConfigService, $q, $interval, sdSessionService) {

		var trace = sdLoggerService.getLogger('bpm-common.services.sdSsoService');
		var ippBaseUrl = sdEnvConfigService.getBaseUrl();
		var ssoServiceURL = sdEnvConfigService.getSsoServiceUrl();
		/*
		 *
		 */
		this.initialize = function () {
			var deferred = $q.defer();

			var ssoServiceURL = sdEnvConfigService.getSsoServiceUrl();

			if (ssoServiceURL) {
				beginInitilization().done(function (data) {
					startHeartbeat();
					deferred.resolve(data);
				}).fail(function (data) {
					trace.debug("SSO Initialization failed : ", data);
					var failure = {};
					if (data.status) {
						failure.status = data.status;
					}
					if (data.statusText) {
						failure.statusText = data.statusText;
					}
					deferred.reject(failure);
				});
			} else {
				var noURlfailure = {
					message : "Couldn't find ssoServiceURL in Env config."
				};

				deferred.reject(noURlfailure);
			}

			return deferred.promise;
		};

		function beginInitilization () {
			return getSAMLResponse().then(loginIPPWithSAML).then(initializeIPP);
		}

		function getSAMLResponse () {
			trace.debug("Calling IDP to retrieve SAML Response");
			return jQuery.ajax({
				type : "GET",
				url : ssoServiceURL
			});
		}

		function loginIPPWithSAML (data, textStatus, jqXHR) {
			var samlResponseIndex = 'name=\"SAMLResponse\" value=\"'.length;
			var saml = data;
			var samlIndex = data.indexOf('name=\"SAMLResponse\" value=\"');

			if (samlIndex === -1) {
				var defered = $.Deferred();
				var msg = "Failed to retrieve SAML Resonse from IDP";
				trace.error(msg);
				defered.reject();
				return defered;
			}

			var data = data.substring(samlIndex + samlResponseIndex);
			saml = data.substring(0, data.indexOf('\"'));
			trace.debug('SAML token retrieved from  Response.Logging in IPP with SAML token.');

			return jQuery.ajax({
				type : "POST",
				data : {
					SAMLResponse : saml
				},
				url : ippBaseUrl + "acs/POST.do"
			});
		}

		function initializeIPP (data, textStatus, jqXHR) {
			trace.debug("Initialize IPP");
			var url = ippBaseUrl + "index.jsp";
			return jQuery.ajax({
				type : "GET",
				url : url
			});
		}


		function startHeartbeat() {

			var interval = sdEnvConfigService.getHeartbeatInterval();

			if(interval) {
					trace.debug("Hearbeat Interval configured in config :- "+interval +" seconds");
			} else {
				interval = DEFAULTS.heatbeatInterval;
					trace.debug("Hearbeat Interval not configured in config.Using default :- "+interval +" seconds");
			}
			sdSessionService.startHeartbeat(interval);
		}

	}
})();
