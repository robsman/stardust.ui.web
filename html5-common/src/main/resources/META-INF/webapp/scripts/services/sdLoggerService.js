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

(function(){
	'use strict';

	angular.module('bpm-common.services').provider('sdLoggerService', function () {
		this.$get = ['$log', function ($log) {
			var service = new LoggerService($log);
			return service;
		}];
	});

	/*
	 * 
	 */
	function LoggerService($log) {
		var MAIN_MODULE = 'bpm-portal.';

		/*
		 * 
		 */
		LoggerService.prototype.getLogger = function(moduleName) {
			var prefix = MAIN_MODULE + moduleName + ' =>';

			return {
				log: function() {
					logIt($log.log, Array.prototype.slice.call(arguments, 0));
				},
				debug: function() {
					logIt($log.log, Array.prototype.slice.call(arguments, 0));
				},
				info: function() {
					logIt($log.info, Array.prototype.slice.call(arguments, 0));
				},
				warn: function() {
					logIt($log.warn, Array.prototype.slice.call(arguments, 0));
				},
				error: function() {
					logIt($log.error, Array.prototype.slice.call(arguments, 0));
				},
				printStackTrace : function(msg) {
					if (msg != undefined) {
						logIt($log.error, msg);
					}

					if (console.trace) {
						console.trace();
					} else {
						logIt($log.warn, 'Could not log stack trace as browser does not support console.trace().');
					}
				}
			};

			/*
			 * 
			 */
			function logIt(loggerFunc, args) {
				args = [prefix].concat(args);
				if (!loggerFunc) {
					loggerFunc = $log.log;
				}
				loggerFunc.apply($log, args);
			}
		};
	};
})();
