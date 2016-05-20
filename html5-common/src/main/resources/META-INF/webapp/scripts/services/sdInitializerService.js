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

(function () {
	'use strict';

angular.module('bpm-common.services').provider(
		'sdInitializerService',
		function () {
			this.$get = [
					'$q',
					'sdSsoService',
					'sdLoggedInUserService',
					'sdEnvConfigService',
					'sdLocalizationService',
					'sgPubSubService',
					'sdLoggerService',
					'sgI18nService',
					function ($q, sdSsoService, sdLoggedInUserService, sdEnvConfigService, sdLocalizationService,
							sgPubSubService, sdLoggerService, sgI18nService) {
						var service = new InitializerService($q, sdSsoService, sdLoggedInUserService,
								sdEnvConfigService, sdLocalizationService, sgPubSubService, sdLoggerService, sgI18nService);
						return service;
					}];
		});

/**
 *
 */
function InitializerService ($q, sdSsoService, sdLoggedInUserService, sdEnvConfigService, sdLocalizationService,
		sgPubSubService, sdLoggerService, sgI18nService) {

	var trace = sdLoggerService.getLogger('bpm-common.services.sdInitializerService');
	var self = this;


	/**
	 *
	 */
	this.start = function () {

		if (sdEnvConfigService.getSsoServiceUrl()) {
			trace.debug("SSO service URL found.Initializing Portal with SSO");
			intializePortalWithSSO();
		} else {
			trace.debug("SSO service URL not found in env config.Initializing Portal without SSO");
			initializePortal();
		}
	};

	/**
	 *
	 */
	function intializePortalWithSSO () {
		sdSsoService.initialize().then(function (sucessData) {
			initializePortal(sucessData);
		}, function (failure) {
			afterIntialization(false, failure);
		});
	}

	/**
	 *
	 */
	function initializePortal (sucessData) {
		loadLoggedInUserData().then(function () {
			afterIntialization(true, sucessData);
		},function( error ) {
			trace.debug("Failed to load logged in user data : ", error);
			afterIntialization(false, error);
		});
	}

	/**
	 *
	 */
	function loadLoggedInUserData () {

		var userInfoPromise = sdLoggedInUserService.loadUserInfo();
		var runTimePermissionsPromise = sdLoggedInUserService.loadRuntimePermissions();
		var localizationInfoPromise = sdLocalizationService.loadInfo();

		var servicesToBeLoaded = [userInfoPromise, runTimePermissionsPromise, localizationInfoPromise];
		trace.debug("Loading Portal Initialization services.");
		return $q.all(servicesToBeLoaded);
	}

	/**
	 *
	 */
	function afterIntialization (initializationSuccess, data) {
		trace.debug("Portal Initialization complete.");
		var pubSubMessage = {
			message : ''
		};

		if (initializationSuccess) {
			if (sdEnvConfigService.isConfigured()) {
				sgI18nService.addResourceBundle();
			}

			var msg = "IPP Initialization successful.";
			pubSubMessage.message = msg;
			trace.debug("Publishing Success Event : "+ self.events.success , pubSubMessage);
			sgPubSubService.publish( self.events.success, pubSubMessage);
		} else {
			var msg = "IPP Initialization failed.";
			pubSubMessage.message = msg;
			angular.merge(pubSubMessage, data);
			trace.debug("Publishing Failure Event : "+ self.events.failure, pubSubMessage);
			sgPubSubService.publish( self.events.failure, pubSubMessage);
		}
	}


	/**
	 *
	 */
	InitializerService.prototype.events = {
			success : "sd-initialization-success",
			failure : "sd-initialization-failure"
	};
}

/**
 * Decorate I18N Service, in order to add bundle
 */
angular.module('bpm-common.services').decorator('sgI18nService',
		['$delegate', '$http', 'sdEnvConfigService', 'sdLoggerService', I18nServiceDecorator]);

/*
 * 
 */
function I18nServiceDecorator($delegate, $http, sdEnvConfigService, sdLoggerService) {
	var I18N_MESSAGE_URL = 'services/rest/common/html5/api/messages/';

	var trace = sdLoggerService.getLogger('bpm-common.services.sgI18nService.decorator');
	
	/*
	 * Decorate by adding new method for adding bundle
	 * This will be invoked after initialization is complete
	 */
	$delegate.addResourceBundle = function() {
		var locale = this.locale() || 'en';
		var url = sdEnvConfigService.getBaseUrl() + I18N_MESSAGE_URL + locale;
		var namespace = 'translation';

		trace.info('Fetching resource bundle, url: ' + url);

		$http.get(url).success(function(resources) {
			try {
				trace.info('Adding resource bundle');
				window.i18n.addResourceBundle(locale, namespace, resources[locale][namespace]);
			} catch (e) {
				trace.error('Error in adding resource bundle', e);
			}
        }).error(function(err) {
        	trace.error('Error in fetching resource bundle, url: ' + url, err);
        });
	}

	return $delegate;	
}

})();
