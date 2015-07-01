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
		this.$get = [ '$rootScope', '$parse', '$q', '$http', function($rootScope, $parse, $q, $http) {
			var service = new UtilService($rootScope, $parse, $q, $http);
			return service;
		} ];
	});

	/*
	 * 
	 */
	function UtilService($rootScope, $parse, $q, $http) {
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
		UtilService.prototype.extend = function(childObject, parentObject, addFuncProxies) {
			var proxies = addFuncProxies ? addFuncProxies : true;

			for ( var member in parentObject) {
				if (member.indexOf("$") != 0) {
					childObject[member] = parentObject[member];

					if (proxies && angular.isFunction(childObject[member])) {
						childObject["$" + member] = createProxyFunc(parentObject, member);
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
			if (event.stopPropagation) {
				event.stopPropagation();
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
						var params = funcAsStr.substring(0, funcAsStr.indexOf(')'));
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
			return window.location.href.substring(0, location.href.indexOf("/main.html"));
		};

		UtilService.prototype.format = function(str, args) {
			return str.replace(/{(\d+)}/g, function(match, number) {
				return typeof args[number] != 'undefined' ? args[number] : match;
		    });
		};

	  /**
	   * invokes the callback the given element is available on scope
	   * Note that it is only Availability trigger!
	   */
		UtilService.prototype.watch = function($scope, watchforElement, callback, unregister) {
	    console.log("watching scope for: " + watchforElement);
	    if ($parse(watchforElement)($scope)) {
	      callback();
	    } else {
	      // If not available Watch for it
	      $scope.watchForIt = function() {
					return $parse(watchforElement)($scope) ? "GotIt" : "";
	      };

	      console.log("Registering Watch for Element: " + watchforElement);
	      var unregister = $scope.$watch("watchForIt()", function(newVal, oldVal) {
	        if (newVal !== oldVal) {
	          console.log("Element is available now!");
	          if(unregister){
	            unregister();  
	          }
	          callback();
	        }
	      });
	    }
	  }

		/**
		 * 
		 */
		UtilService.prototype.convertToSortedArray = function(obj, field, ascending) {
      var sortedObjects = [];
  
      for ( var key in obj) {
        if (obj.hasOwnProperty(key)) {
          sortedObjects.push(obj[key]);
        }
      }
  
      var ascendingFactor = ascending ? 1 : -1;
  
      sortedObjects.sort(function(left, right) {
  
        var leftValue, rightValue;
  
        if (left[field] && right[field]) {
  
          leftValue = left[field].toLowerCase();
          rightValue = right[field].toLowerCase();
  
          if (leftValue < rightValue) { return -1 * ascendingFactor; }
          if (leftValue > rightValue) { return 1 * ascendingFactor; }
        }
        return 0;
      });
  
      return sortedObjects;
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

		/**
		 * 
		 */

		UtilService.prototype.ajax = function(restUrl, extension, value) {
			var deferred = $q.defer();

			var type;
			var data;
			if (angular.isObject(value) || angular.isArray(value)) {
				restUrl += extension;
				type = "POST";
				data = JSON.stringify(value);
			} else {
				restUrl += value + "/" + extension;
				type = "GET";
	}

			var httpResponse;
			if (type == "GET") {
				httpResponse = $http.get(restUrl);
			} else {
				httpResponse = $http.post(restUrl, data);
			}

			httpResponse.success(function(data) {
				deferred.resolve(data);
			}).error(function(data) {
				deferred.reject(data);
			});

			return deferred.promise;
		};
		
		/** 
		 * @param fromDate
		 * @param toDate
		 * @returns {Boolean}
		 */
		UtilService.prototype.validateDateRange = function(fromDate, toDate) {
			if (!this.isEmpty(fromDate) && !this.isEmpty(toDate)) {
				if (fromDate > toDate) {
					return false;
				}
			}
			return true;
		};
		/**
		 * 
		 */
		UtilService.prototype.validatePassword = function(password, passwordConfirmation) {
			var passwordValidationMsg = "";
				if (this.isEmpty(passwordConfirmation) || this.isEmpty(password)) {
					passwordValidationMsg = "views.createUser.password.empty";
				} else if (passwordConfirmation != password) {
					passwordValidationMsg = "views.createUser.password.mismatch";
				}
			return passwordValidationMsg;
		};
		
		/*
		 * 
		 */
		UtilService.prototype.getBaseUrl = function() {
			// When loaded from framework i.e index.html, location.href points
			// to contextRoot
			var baseURL = '';
			if (location.href.indexOf("plugins") > -1) {
				// If plugins encountered in the location url 
				baseURL = location.href.substring(0, location.href
						.indexOf("plugins"));
			}
			
			return baseURL;
		};
		
		
		/**
		 * Given an array of names generate a unique name with collisions being resolved
		 * by appending a numeric increment to the name and then recursing until no collisions occur.
		 * 
		 * @usage: generateUniqueName(['Anne','Bob'], 'Frank');
		 * @param names - array of names we are testing against.
		 * @param testName - name we are testing for uniqueness
		 * @param suffix - OPTIONAL Defaults to zero.
		 * 				 - MUST BE AN INTEGER --99.99% sure you should leave this blank as 
		 * 				   it is designed to be passed on subsequent recursive calls. 
		 *                 Passing a value in on the user invocation
		 *                 results in the increment starting at that suffix value.
		 * @returns a unique name!
		 */
		UtilService.prototype.generateUniqueName = function(names,testName,suffix){
			var i,
			  	found=false,
				temp;
				
			suffix = !suffix ? 0 : suffix;
			
			for(i=0;i<names.length;i++){
				temp = names[i];
				if(suffix === 0){
					if(temp === testName){
						found=true;
					}
				}
				else{
					if(temp === testName + " " + suffix){
						found = true;
					}
				}
			}
			
			if(found){
				return this.generateUniqueName(names,testName,suffix + 1);
			}
			else{
				if(suffix === 0){
					return testName;
				}
				else{
					return testName + " " + suffix;
				}
			}
		}
		

	}
	;
})();
