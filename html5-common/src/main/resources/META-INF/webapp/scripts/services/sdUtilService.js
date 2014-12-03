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

(function(){
	'use strict';

	angular.module('bpm-common.services').provider('sdUtilService', function () {
		this.$get = ['$rootScope', function ($rootScope) {
			var service = new UtilService($rootScope);
			return service;
		}];
	});

	/*
	 * 
	 */
	function UtilService($rootScope) {
		/*
		 * 
		 */
		UtilService.prototype.safeApply = function($scope) {
			var root = $scope.$root ? $scope.$root : $scope;
			if (root.$$phase !== '$apply' && root.$$phase !== '$digest') {
				$scope.$apply();
			}
		};

		/*
		 * Copies properties (attributes and functions) which does not start with $
		 * Properties starting with $ are considered as private and hence skipped
		 */
		UtilService.prototype.extend = function(childObject, parentObject, addFuncProxies) {
			var proxies = addFuncProxies ? addFuncProxies : true;
	
			for (var member in parentObject) {
				if (member.indexOf("$") != 0) {
					childObject[member] = parentObject[member];
	
					if(proxies && angular.isFunction(childObject[member])){
						childObject["$" + member] = createProxyFunc(parentObject, member);
					}
				}
			}
		};

		/*
		 * Creates proxy function for each function which does not start with $
		 * Proxy function is added by prefixing $ to the existing function
		 * The proxy function helps is retaining 'this' context while calling function on scope from markup (ng-click)
		 */
		UtilService.prototype.addFunctionProxies = function(obj) {
			for (var member in obj) {
				if (member.indexOf("$") != 0 && angular.isFunction(obj[member])) {
					obj["$" + member] = createProxyFunc(obj, member);
				}
			}
		};

		/*
		 * 
		 */
		UtilService.prototype.stopEvent = function(event) {
			if (event.stopPopogation) {
				event.stopPopogation();
			} else if (window.event){
				window.event.cancelBubble = true;
			}
		};

		/*
		 * 
		 */
		UtilService.prototype.parseFunction = function(funcAsStr) {
			var ret = null;

			try {
				if (funcAsStr.indexOf('(') != -1) {
					funcAsStr = funcAsStr.substring(funcAsStr.indexOf('(') + 1);
					
					if (funcAsStr.indexOf(')') != -1) {
						var params = funcAsStr.substring(0, funcAsStr.indexOf(')'));
						params = params.split(',');
						for(var i = 0; i < params.length; i++) {
							params[i] = params[i].trim();
						}
						ret = {};
						ret.params = params;
					}
				}
			} catch (e) {
			}

			return ret;
		};

		/*
		 * 
		 */
		UtilService.prototype.isEmpty = function(data) {
			var empty = false;

			switch(typeof data) {
				case 'string':
					if (data == '') {
						empty = true;
					}
					break;
				case 'object':
					if (angular.isArray(data)) {
						empty = data.length == 0;
					} else {
						var properties = 0;
						for(var key in data) {
							properties++;
							break;
						}
						empty = properties == 0;
					}
					break;
			}

			return empty;
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
	};
})();
