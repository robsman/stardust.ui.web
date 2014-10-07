/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/*
 * @author Subodh.Godbole
 */

'use strict';

angular.module('bpm-common.services').provider('sdUtilService', function () {
	var self = this;
	
	self.$get = ['$rootScope', function ($rootScope) {

		var service = {};

		/*
		 * Copies properties (attributes and functions) which does not start with $
		 * Properties starting with $ are considered as provide to parentObject and hence skipped
		 */
		service.extend = function(childObject, parentObject, addProxies) {
			var proxies = addProxies ? addProxies : true;

			for (var member in parentObject) {
				if (member.indexOf("$") != 0) {
					childObject[member] = parentObject[member];
				}

				if(proxies && angular.isFunction(childObject[member])){
					childObject["$" + member] = createProxyFunc(parentObject, member);
				}
			}
		};

		/*
		 * 
		 */
		function createProxyFunc(obj, member) {
			function proxyFunc() {
				var args = Array.prototype.slice.call(arguments, 0);
				obj[member].apply(obj, args);
			};
			
			return proxyFunc;
		}

		return service;
	}];
});
