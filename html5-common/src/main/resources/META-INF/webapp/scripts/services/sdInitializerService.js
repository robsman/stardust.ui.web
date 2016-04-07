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
	this.intializationFailureEvent = "sd-initialization-failure";
	this.intializationSuccessEvent = "sd-initialization-success";
	
	this.start = function () {

		if (sdEnvConfigService.getSsoServiceUrl()) {
			trace.debug("SSO service URL found.Intialization Portal with SSO");
			intializePortalWithSSO();
		} else {
			trace.debug("SSO service not found in env config URL found.Intialization Portal with out SSO");
			initializePortal();
		}
	}
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
		trace.debug("Loading Portal Intialization services");
		return $q.all(servicesToBeLoaded);
	}

	/**
	 * 
	 */
	function afterIntialization (initializationSuccess, data) {
		var pubSubMessage = {
			message : '',
			success : false
		};
	
		if (initializationSuccess) {
			trace.debug("Intialization success.Publishing event :-"+self.intializationSuccessEvent, data);
			var msg = "IPP Initialization successfull."
			pubSubMessage.message = msg;
			sgPubSubService.publish(self.intializationSuccessEvent, pubSubMessage);
		} else {
			trace.debug("Intialization failure.Publishing event :-"+self.intializationFailureEvent, data);
			var msg = "IPP Initialization failed."
			pubSubMessage.message = msg;
			angular.merge(pubSubMessage, data);
			sgPubSubService.publish(self.intializationFailureEvent, pubSubMessage);
		}
	
		
		
	}
}