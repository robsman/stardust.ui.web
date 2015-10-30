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
	
	
	var DEFAULTS = {
			FAX_FORMAT :  new RegExp("^\\(?([0-9]{3})\\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})$")
	}

	angular.module('bpm-common.services').provider('sdUtilService', function() {
		this.$get = [ '$rootScope', '$parse', '$q', '$http', 'sdLoggerService', function($rootScope, $parse, $q, $http, sdLoggerService) {
			var service = new UtilService($rootScope, $parse, $q, $http, sdLoggerService);
			return service;
		} ];
	});

	/*
	 * 
	 */
	function UtilService($rootScope, $parse, $q, $http, sdLoggerService) {
		
		var trace = sdLoggerService.getLogger('bpm-common.services.sdUtilService');
		
		/*
		 * 
		 */
		UtilService.prototype.safeApply = function($scope) {
			var root = $scope.$root ? $scope.$root : $scope;
			if (!root.$$phase) {
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
		trace.log("watching scope for: " , watchforElement);
	    if ($parse(watchforElement)($scope)) {
	      callback();
	    } else {
	      // If not available Watch for it
	      $scope.watchForIt = function() {
					return $parse(watchforElement)($scope) ? "GotIt" : "";
	      };

	      trace.log("Registering Watch for Element: " , watchforElement);
	      var unregister = $scope.$watch("watchForIt()", function(newVal, oldVal) {
	        if (newVal !== oldVal) {
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

		/*
		 * Flattens out the Tree Structure into flat array
		 */
		UtilService.prototype.marshalDataForTree = function(data, parentRow) {
			var treeLevel = parentRow ? parentRow.$$treeInfo.level + 1 : 0;
			var retData = [];

			for (var i = 0; i < data.length; i++) {
				retData.push(data[i]);

				data[i].$$treeInfo = {};
				data[i].$$treeInfo.level = treeLevel;
				data[i].$$treeInfo.levels = [];
				for (var j = 0; j < treeLevel; j++) {
					data[i].$$treeInfo.levels.push(j);
				}

				data[i].$$treeInfo.parent = null;
				data[i].$$treeInfo.parents = {};

				if (parentRow) {
					data[i].$$treeInfo.parents = angular.copy(parentRow.$$treeInfo.parents);
					data[i].$$treeInfo.parents[parentRow.$$treeInfo.id] = true;
				}

				if (data[i].children != undefined) {
					data[i].$leaf = false;
				} else {
					data[i].$leaf = (data[i].$leaf == undefined || data[i].$leaf == true) ? true : false;
				}

				if (!data[i].$leaf) {
					data[i].$expanded = data[i].$expanded ? true : false;

					data[i].$$treeInfo.id = 'r' + (Math.floor(Math.random()*10000) + 1);
					data[i].$$treeInfo.loaded = false;
					
					// Process children
					if (data[i].children != undefined && angular.isArray(data[i].children)) {
						data[i].$$treeInfo.loaded = true;
						var childrenArray = this.marshalDataForTree(data[i].children, data[i]);
						for (var j = 0; j < childrenArray.length; j++) {
							retData.push(childrenArray[j]);
						}
					}
				}
			}

			return retData;
		};

		/*
		 * Returns visible (expanded) tree rows
		 */
		UtilService.prototype.rebuildTreeTable = function(treeTableData, filterFunc) {
			var rebuiltData = [], collapsedParents = {};
			for (var i = 0; i < treeTableData.length; i++) {
				var visible = filterFunc(treeTableData[i]);
				if (!visible) {
					if (!treeTableData[i].$leaf) {
						treeTableData[i].$expanded = false;
						collapsedParents[treeTableData[i].$$treeInfo.id] = true;
					}
					continue;
				}
				
				if (!treeTableData[i].$expanded) {
					collapsedParents[treeTableData[i].$$treeInfo.id] = true;
				}
						
				if (this.isTreeTableNodeVisible(treeTableData[i], collapsedParents)) {
					rebuiltData.push(treeTableData[i]);
				}
			}

			return rebuiltData;
		};

		/*
		 * Checks if tree row is visible or not
		 */
		UtilService.prototype.isTreeTableNodeVisible = function(row, collapsedParents) {
			for (var parent in collapsedParents) {
				if (row.$$treeInfo.parents[parent]) {
					return false;
				}
			}
			
			return true;
		};

		/*
		 *
		 */
		UtilService.prototype.expandTreeTable = function(treeTableData) {
			for (var i = 0; i < treeTableData.length; i++) {
				if (treeTableData[i].$expanded != undefined && treeTableData[i].$$treeInfo.loaded) {
					treeTableData[i].$expanded = true;
				}
			}
		};

		/*
		 *
		 */
		UtilService.prototype.collapseTreeTable = function(treeTableData) {
			for (var i = 0; i < treeTableData.length; i++) {
				if (treeTableData[i].$expanded != undefined) {
					treeTableData[i].$expanded = false;
				}
			}
		};

		/*
		 * 
		 */
		UtilService.prototype.insertChildrenIntoTreeTable = function(treeTableData, rowData, children) {
			var treeRowIndex;
			for (var i = 0; i < treeTableData.length; i++) {
				if (treeTableData[i].$$treeInfo.id == rowData.$$treeInfo.id) {
					treeRowIndex = i;
					break;
				}
			}

			var args = [treeRowIndex + 1, 0].concat(children);									
			treeTableData.splice.apply(treeTableData, args);
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
		
	   /**
	    * Returns true if browser is IE and browser is below version 10
	    */
		UtilService.prototype.isIEBelow10 = function(){
		    var myNav = navigator.userAgent.toLowerCase();
		    return (myNav.indexOf('msie') != -1) ? parseInt(myNav.split('msie')[1]) < 10 : false;
		};
		  
		  
	  /**
	   *Downloads the content as a file. Tested with UTF-8 (text) content.
	   *Still to be determined how this works with binary content (images/wav files).
	   *Note:ref ng-grid
	   *@param: content - data to save to file
	   *@param: filename - name of file, if not provided with = {timestamp}.txt
	   *@param: mimeType - defaults to UTF-8 charset
	   */
		UtilService.prototype.downloadAsFile = function(content,filename,mimeType){

		    var a = document.createElement('a');
		    var strMimeType = mimeType || 'application/octet-stream;charset=utf-8';
		    var rawFile;
		  
		    if (!filename) {
		      filename = new Date().getTime() + ".txt";
		    }
		  
		    if (this.isIEBelow10()) {
		      var frame = document.createElement('iframe');
		      document.body.appendChild(frame);
		      frame.contentWindow.document.open("text/html", "replace");
		      frame.contentWindow.document.write(content);
		      frame.contentWindow.document.close();
		      frame.contentWindow.focus();
		      //must save as .txt as SaveAs will only work with text files in IE9 (requires a registry change otherwise)
		      frame.contentWindow.document.execCommand('SaveAs', true, filename + ".txt");
		      document.body.removeChild(frame);
		      return true;
		    }
		  
		    // IE10+
		    if (navigator.msSaveBlob) {
		      return navigator.msSaveBlob(new Blob(["\ufeff", content], {
		        type: strMimeType
		      }), filename);
		    }
		  
		    //html5 A[download]
		    if ('download' in a) {
		      var blob = new Blob([content], {
		        type: strMimeType
		      });
		      rawFile = URL.createObjectURL(blob);
		      a.setAttribute('download', filename);
		    } else {
		      rawFile = 'data:' + strMimeType + ',' + encodeURIComponent(content);
		      a.setAttribute('target', '_blank');
		      a.setAttribute('download', filename);
		    }
		  
		  
		    a.href = rawFile;
		    a.setAttribute('style', 'display:none;');
		    document.body.appendChild(a);
		    setTimeout(function() {
		      if (a.click) {
		        a.click();
		        // Safari 5
		      } else if (document.createEvent) {
		        var eventObj = document.createEvent('MouseEvents');
		        eventObj.initEvent('click', true, true);
		        a.dispatchEvent(eventObj);
		      }
		      document.body.removeChild(a);
		  
		    }, 100);
	   	};
		
	   	UtilService.prototype.resolveProperty = function(obj, field) {
			try {
				if (field == null || obj == null) {
					return obj;
				}

				if (field.indexOf('.') > -1) {
					var parts = field.split('.');
					var current = obj;
					for ( var j in parts) {
						current = current[parts[j]];
					}
					return current;
				}
				return obj[field];				
			} catch(e) {
				trace.error('Could not resolve property: ' + field + ' of: ', obj, e);
				return obj;
			}
		};
		
		/**
		 *  Checks for falsy value 
		 *  no/ false/ "false" / "FALSE" /0 / "0"  = false
		 *  Rest all true
		 */
	 	UtilService.prototype.toBoolean = function (value) {
			   if (value && value.length !== 0) {
			    var v = angular.lowercase("" + value);
			    value = !(v == '0' || v == 'false' || v == 'no');
			  } else {
			    value = false;
			  }
			  return value;
			}
	 	
		/**
		 *  Checks is a number is fax number
		 */
	 	UtilService.prototype.isFaxNumber = function (value,customFormat) {
	 		
	 		var fax_format = DEFAULTS.FAX_FORMAT;
	 		
	 		if(customFormat){
	 			fax_format = customFormat;
	 		}
	 		return fax_format.test(value); 
		}
	 	
	};
})();
