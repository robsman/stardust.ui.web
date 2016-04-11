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
					function ($q, sdSsoService, sdLoggedInUserService, sdEnvConfigService, sdLocalizationService,
							sgPubSubService, sdLoggerService) {
						var service = new InitializerService($q, sdSsoService, sdLoggedInUserService,
								sdEnvConfigService, sdLocalizationService, sgPubSubService, sdLoggerService);
						return service;
					}];
		});

/**
 *
 */
function InitializerService ($q, sdSsoService, sdLoggedInUserService, sdEnvConfigService, sdLocalizationService,
		sgPubSubService, sdLoggerService) {

	var trace = sdLoggerService.getLogger('bpm-common.services.sdInitializerService');
	var self = this;
	
	this.event = {
			initializeSuccess : "sd-initialization-success",
			initializeFailure : "sd-initialization-failure"
	}

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
		trace.debug("Loading Portal Initialization services");
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
			var msg = "IPP Initialization successfull.";
			pubSubMessage.message = msg;
			trace.debug("Publishing Sucess Event : "+ self.event.initializeSuccess , pubSubMessage);
			sgPubSubService.publish( self.event.initializeSuccess, pubSubMessage);
		} else {
			var msg = "IPP Initialization failed.";
			pubSubMessage.message = msg;
			angular.merge(pubSubMessage, data);
			trace.debug("Publishing event :-"+ self.event.initializeFailure, pubSubMessage);
			sgPubSubService.publish( self.event.initializeFailure, pubSubMessage);
		}
	}
}
})();
