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
 * @author Subodh.Godbole
 */

define(["processportal/js/codeGenerator"], function(codeGenerator){
	return {
		initialize : function() {
			var mAPanel = new ManualActivityPanel();
			mAPanel.initialize();
		}
	};
	
	/*
	 * 
	 */
	function ManualActivityPanel() {
		var REST_END_POINT = "/services/rest/process-portal/manualActivity/";
		var BINDING_PREFIX = "dm";

		var interactionEndpoint;

		var dataMappings;
		var bindings;

		/*
		 * 
		 */
		ManualActivityPanel.prototype.initialize = function() {
	        var urlPrefix = window.location.href;
	        urlPrefix = urlPrefix.substring(0, urlPrefix.indexOf("/plugins"));

			var interactionId = window.location.search;
	        interactionId = interactionId.substring(interactionId.indexOf('interactionId') + 14);
	        interactionId = interactionId.indexOf('&') >= 0 ? interactionId.substring(0, interactionId.indexOf('&')) : interactionId;

	        interactionEndpoint = urlPrefix + REST_END_POINT + interactionId;
	        log("Interaction Rest End Point: " + interactionEndpoint);
	        
	        getData(interactionEndpoint, "/dataMappings", {success: generateMarkup});

			bootstrapAngular();
			
			getData(interactionEndpoint, "/inData", {success: bindInData});
			
			runInAngularContext(function($scope){
				$scope.initState = {};
				$scope.initState.success = true;

				$scope.addToList = addToList;
				$scope.removeFromList = removeFromList;
				$scope.selectListItem = selectListItem;
			});
		};

		/*
		 * 
		 */
		function bootstrapAngular() {
			var moduleName = 'ManualActivityModule';
			var angularModule = angular.module(moduleName, []);
			
			// Taken From - http://jsfiddle.net/cn8VF/
			// This is to delay model updates till element is in focus
			angularModule.directive('ngModelOnblur', function() {
				return {
					restrict : 'A',
					require : 'ngModel',
					link : function(scope, elm, attr, ngModelCtrl) {
						if (attr.type === 'radio' || attr.type === 'checkbox') {
							return;
						}
						elm.unbind('input').unbind('keydown').unbind('change');
						elm.bind('blur', function() {
							scope.$apply(function() {
								ngModelCtrl.$setViewValue(elm.val());
							});
						});
					}
				};
			});

			angularModule.directive('sdPostData', function() {
				return {
					require : 'ngModel',
					link : function(scope, elm, attr, ngModelCtrl) {
						if (attr.ngModel.indexOf(BINDING_PREFIX) == 0) {
							log("Watching for: " + attr.ngModel);
							scope.$watch(attr.ngModel, function(newValue, oldValue) {
								if (scope.initState && scope.initState.success && 
										newValue != undefined && newValue != oldValue) {
									var binding = attr.ngModel.substr(BINDING_PREFIX.length + 1);
									var dataMapping = binding.substr(0, binding.indexOf("["));

									// TODO: Post Only changed value and not full Data Mapping 
									log("Posting Data for Data Mapping: " + dataMapping);
									var transferData = {};
									transferData[dataMapping] = scope[BINDING_PREFIX][dataMapping];
									postData(interactionEndpoint, "/outData/" + dataMapping, transferData, {});
								}
							}, true);
						}
					}
				};
			});

			angular.bootstrap(document, [moduleName]);
		};

		/*
		 * 
		 */
		function generateMarkup(json) {
			dataMappings = json;

			var data = codeGenerator.create().generate(json, BINDING_PREFIX);
			document.getElementsByTagName("body")[0].innerHTML = data.html;
			
			bindings = data.binding;
		};

		/*
		 * 
		 */
		function bindInData(data) {
			runInAngularContext(function($scope){
				$scope[BINDING_PREFIX] = bindings;
				jQuery.extend($scope[BINDING_PREFIX], data);
			});
		};

		/*
		 * 
		 */
		function addToList(xPath) {
			var $scope = angular.element(document).scope();
			var parts = xPath.substring(1).split("/");
			
			var currentBinding = $scope[BINDING_PREFIX];
			for(var i = 0; i < parts.length - 1; i++) {
				currentBinding = currentBinding[parts[i]];
			}

			if (currentBinding) {
				var lastPart = parts[parts.length-1];
				if (currentBinding[lastPart] == undefined) {
					currentBinding[lastPart] = [];
				}
				currentBinding = currentBinding[lastPart];
				currentBinding.push({});
			}
		}
		
		/*
		 * 
		 */
		function selectListItem(event, obj) {
			if (obj.$$selected == undefined || obj.$$selected == false) {
				obj.$$selected = true;
			} else {
				obj.$$selected = false;
			}
		}

		/*
		 * 
		 */
		function removeFromList(xPath) {
			var $scope = angular.element(document).scope();
			var parts = xPath.substring(1).split("/");
			
			var currentBinding = $scope[BINDING_PREFIX];
			for(var i = 0; i < parts.length; i++) {
				currentBinding = currentBinding[parts[i]];
			}

			if (currentBinding) {
				removeSelectedElements(currentBinding);
			}
		}

		/*
		 * 
		 */
		function removeSelectedElements(arr) {
			for(var i = 0 ; i < arr.length; i++) {
				if (arr[i].$$selected) {
					arr.splice(i, 1);
					removeSelectedElements(arr);
					break;
				}
			}
		}

		/*
		 * 
		 */
		function getData(baseUrl, extension, callbacks) {
			var endpoint = baseUrl + extension;
	        log(endpoint);

			jQuery.ajax({
				type: 'GET',
				url: endpoint,
				async: false,
				success: callbacks.success,
				error: callbacks.failure ? callbacks.failure : function(errObj) {
					log('Failed to get ' + extension + ' - ' + errObj.status + ":" + errObj.statusText);
				}
			});
		};

		/*
		 * 
		 */
		function postData(baseUrl, extension, data, callbacks) {
			var endpoint = baseUrl + extension;
	        log(endpoint);

			jQuery.ajax({
				type: 'POST',
				url: endpoint,
				async: false,
				contentType: 'application/json',
				data: JSON.stringify(data),
				success: callbacks.success ? callbacks.success : null,
				error: callbacks.failure ? callbacks.failure : function(errObj) {
					log('Failed to post ' + extension + ' - ' + errObj.status + ":" + errObj.statusText);
				}
			});
		};

		/*
		 * 
		 */
		function runInAngularContext(func) {
			var scope = angular.element(document).scope();
			scope.$apply(func);
		};

		/*
		 * 
		 */
		function log(msg) {
			if (console) {
				console.log(msg);
			}
		}
	};
});