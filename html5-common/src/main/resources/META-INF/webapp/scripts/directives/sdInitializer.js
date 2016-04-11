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
	'use strict';

	angular.module('bpm-common').directive('sdInitializer',
			['sgPubSubService', 'sdInitializerService', 'sdLoggerService', Initializer]);

	/*
	 * 
	 */
	function Initializer (sgPubSubService, sdInitializerService, sdLoggerService) {
		var trace = sdLoggerService.getLogger('bpm-common.services.sdInitializer');

		return {
			restrict : 'A',
			link : function (scope, element, attrs) {

				beforeIntialization();

				sdInitializerService.start();

				sgPubSubService.subscribe( sdInitializerService.event.initializeSuccess, function ( data ) {
					trace.debug("In "+ sdInitializerService.event.initializeSuccess);
					afterIntialization();
				});
				
				sgPubSubService.subscribe( sdInitializerService.event.initializeFailure, function ( data) {
					trace.debug("In "+ sdInitializerService.event.initializeFailure);
					afterIntialization();
				});

				/**
				 * 
				 */
				function afterIntialization () {
					element.removeClass('sd-portal-loading');
				}

				/**
				 * 
				 */
				function beforeIntialization () {
					element.addClass('sd-portal-loading');
				}

			}
		};
	}


})();