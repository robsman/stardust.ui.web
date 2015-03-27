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

(function() {
	'use strict';

	angular.module('bpm-common.services').provider('sdUtilService', function() {
		this.$get = [ '$rootScope', '$parse', function($rootScope, $parse) {
			var service = new UtilService($rootScope, $parse);
			return service;
		} ];
	});

	/*
	 * 
	 */
	function UtilService($rootScope, $parse) {
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
		 * Copies properties (attributes and functions) which does not start
		 * with $ Properties starting with $ are considered as private and hence
		 * skipped
		 */
		UtilService.prototype.extend = function(childObject, parentObject,
				addFuncProxies) {
			var proxies = addFuncProxies ? addFuncProxies : true;

			for ( var member in parentObject) {
				if (member.indexOf("$") != 0) {
					childObject[member] = parentObject[member];

					if (proxies && angular.isFunction(childObject[member])) {
						childObject["$" + member] = createProxyFunc(
								parentObject, member);
					}
				}
			}
		};

		/*
		 * Creates proxy function for each function which does not start with $
		 * Proxy function is added by prefixing $ to the existing function The
		 * proxy function helps is retaining 'this' context while calling
		 * function on scope from markup (ng-click)
		 */
		UtilService.prototype.addFunctionProxies = function(obj) {
			for ( var member in obj) {
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
			} else if (window.event) {
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
						var params = funcAsStr.substring(0, funcAsStr
								.indexOf(')'));
						params = params.split(',');
						for (var i = 0; i < params.length; i++) {
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
			if (data == undefined || data == null) {
				return true;
			}

			var empty = false;

			switch (typeof data) {
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
					for ( var key in data) {
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
		UtilService.prototype.assert = function(condition, msg) {
			if (!condition) {
				throw msg;
			}
		};

		/*
		 * For async, use angular resource
		 */
		UtilService.prototype.syncAjax = function(endpoint) {
			var data, failed;

			jQuery.ajax({
				type : 'GET',
				url : endpoint,
				async : false,
				contentType : 'application/json',
				success : function(result) {
					data = result;
				},
				error : function(errObj) {
					failed = true;
				}
			});

			if (failed) {
				throw 'Error in invoking syncAjaxPost()';
			}

			return data;
		};

		/*
		 * For async, use angular resource
		 */
		UtilService.prototype.syncAjaxSubmit = function(endpoint, value, type) {
			var data, failed;

			if (angular.isObject(value) || angular.isArray(value)) {
				value = angular.toJson(value);
			} else {
				value = '' + value;
			}

			jQuery.ajax({
				type : type ? type : 'POST',
				url : endpoint,
				async : false,
				contentType : 'application/json',
				data : value,
				success : function(result) {
					data = result;
				},
				error : function(errObj) {
					failed = true;
				}
			});

			if (failed) {
				throw 'Error in invoking syncAjaxSubmit()';
			}

			return data;
		};

		/**
		 * 
		 */
		UtilService.prototype.truncateTitle = function(title) {
			var MAX_TITLE_LENGTH = 35;
			return this.truncate(title, MAX_TITLE_LENGTH);
		}

		/**
		 * 
		 */
		UtilService.prototype.truncate = function(string, maxLength) {

			if (string.length > maxLength) {
				string = string.substring(0, maxLength - 3);
				string += '...';
			}
			return string;
		}

		/**
		 * 
		 */

		UtilService.prototype.getRootUrl = function() {
			return window.location.href.substring(0, location.href
					.indexOf("/main.html"));
		};

		/**
		 * 
		 */
		UtilService.prototype.prepareUrlParams = function(optionData) {
			var options = "";
			if (optionData.skip != undefined) {
				options += "&skip=" + optionData.skip;
			}
			if (optionData.pageSize != undefined) {
				options += "&pageSize=" + optionData.pageSize;
			}
			if (optionData.order != undefined) {
				// Supports only single column sort
				var index = optionData.order.length - 1;
				options += "&orderBy=" + optionData.order[index].name;
				options += "&orderByDir=" + optionData.order[index].dir;
			}

			return options;
		};
		
		UtilService.prototype.format = function(str, args) {
		    return str.replace(/{(\d+)}/g, function(match, number) { 
		      return typeof args[number] != 'undefined'
		        ? args[number]
		        : match;
		    });
		};

	  /**
	   * invokes the callback the given element is available on scope
	   * Note that it is only Availability trigger!
	   */
		UtilService.prototype.doWhenElementIsAvailable = function($scope, watchforElement, callback) {
	    console.log("watching scope for: " + watchforElement);
	    if ($parse(watchforElement)($scope)) {
	      callback();
	    } else {
	      // If not available Watch for it
	      $scope.watchForIt = function() {
	        return $parse(watchforElement)($scope) ? "GotIt"
	                : "";
	      };

	      console.log("Registering Watch for Element: " + watchforElement);
	      var unregister = $scope.$watch("watchForIt()", function(newVal, oldVal) {
	        if (newVal !== oldVal) {
	          console.log("Element is available now!");
	          unregister();
	          callback();
	        }
	      });
	    }
	  }
		
		/*
		 * 
		 */
		function createProxyFunc(obj, member) {
			function proxyFunc() {
				var args = Array.prototype.slice.call(arguments, 0);
				obj[member].apply(obj, args);
			}
			;

			return proxyFunc;
		}

	}
	;
})();
