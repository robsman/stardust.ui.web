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
 * This services makes available the requireJS modules in Angular context
 * 
 * @author Subodh.Godbole
 */

'use strict';

angular.module('bpm-ui.services').provider('sgRequireJSService', function () {
	var self = this;
	
	self.$get = ['$rootScope', function ($rootScope) {

		var service = {};

		/*
		 * 
		 */
		service.getModule = function(modules) {
			var deferred = jQuery.Deferred();

			if ("string" == typeof (modules)) {
				modules = [modules];
			}

			var paths = {};
			var deps = [];
			for(var i in modules) {
				paths["module" + i] = modules[i];
				deps.push("module" + i);
			}
			
			var baseUrl = location.pathname.substring(0, location.pathname.indexOf('/', 1));
			var r = requirejs.config({
				waitSeconds: 0,
				baseUrl: baseUrl,
				paths: paths
			});

			
			r(deps, function(){
				var args = arguments ? Array.prototype.slice.call(arguments, 0) : [];
				deferred.resolve.apply(null, args);
			});

			return deferred.promise();
		}

		return service;
	}];
});