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
