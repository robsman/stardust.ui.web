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
 * TODO: Convert and move most of the things to Angular directive
 * 
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
		var SERVER_DATE_FORMAT = "yy-mm-dd";

		var angularCompile;
		
		var interactionEndpoint;

		var dataMappings;
		var bindings;
		var clientDateFormat = "dd-mm-yy";

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
	        
	        InfinityBPMI18N.initPluginProps({
				pluginName : "manualActivity",
				singleEndPoint : interactionEndpoint + "/i18n"
			});
	        
	        getData(interactionEndpoint, "/dateFormats", {success: receiveDateFormats});

	        getData(interactionEndpoint, "/dataMappings", {success: generateMarkup});

			bootstrapAngular();
			
			getData(interactionEndpoint, "/inData", {success: bindInData});
			
			runInAngularContext(function($scope){
				$scope.initState = {};
				$scope.initState.success = true;

				$scope.saveData = saveData;
				$scope.addToList = addToList;
				$scope.removeFromList = removeFromList;
				$scope.selectListItem = selectListItem;
				$scope.isFormValid = isFormValid;
				$scope.openDocument = openDocument;
				$scope.openNestedList = openNestedList;
				$scope.closeNestedList = closeNestedList;
				$scope.showNestedList = showNestedList;
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
									// Format of ngModel is dm['X']['Y']
									var binding = attr.ngModel.substr(BINDING_PREFIX.length + 2);
									var dataMapping = binding.substr(0, binding.indexOf("']"));
	
									if (dataMapping) {
										// TODO: Post Only changed value and not full Data Mapping 
										log("Posting Data for Data Mapping: " + dataMapping);
										var transferData = {};
										transferData[dataMapping] = scope[BINDING_PREFIX][dataMapping];
										postData(interactionEndpoint, "/outData/" + dataMapping, transferData, {});
									}
								}
							}, true);
						}
					}
				};
			});

			angularModule.directive('sdDate', function($parse) {
				return {
					require : 'ngModel',
					link : function(scope, elm, attr, ctrl) {
						var ngModel = $parse(attr.ngModel);
						jQuery(function() {
							elm.datepicker({
								dateFormat : clientDateFormat,
								onSelect : function(dateText, inst) {
									scope.$apply(function(scope) {
										// Convert to Server Format
										var value = formatDate(dateText, clientDateFormat, SERVER_DATE_FORMAT);

										// Change binded variable
										ngModel.assign(scope, value);
									});
								}
							});
						});

						ctrl.$formatters.unshift(function(viewValue) {
							// Convert to Client Format
							return formatDate(viewValue, SERVER_DATE_FORMAT, clientDateFormat);
						});
					}
				};
			});

			angularModule.filter('sdFilterDate', function() {
				return function(value) {
					// Convert to Client Format
					return formatDate(value, SERVER_DATE_FORMAT, clientDateFormat);
				};
			});

			angularModule.directive('sdDynamicId', function() {
				return {
			        restrict: 'A',
			        link: function(scope, elm, attr, ctrl) {
			        },
			        transclude: true
			    };
			});

			angularModule.controller('ManualActivityCtrl', function($compile) {
				angularCompile = $compile;
			});

			angular.bootstrap(document, [moduleName]);
		};

		/*
		 * 
		 */
		function formatDate(value, fromFormat, toFormat) {
			if (value != undefined && value != null && value != "") {
				try {
					// Convert to Client Format
					var date = jQuery.datepicker.parseDate(fromFormat, value);
					value = jQuery.datepicker.formatDate(toFormat, date);
				} catch(e) {
					log(e);
				}
			}
			return value;
		}

		/*
		 * 
		 */
		function generateMarkup(json) {
			dataMappings = json;

			var nestedListsDiv = 
				"<div ng-show=\"showNestedDM\" class=\"panel-list-dialog\">" + 
					"<div class=\"panel-list-dialog-title\">{{nestedDMTitle}}" + 
						"<span class=\"panel-list-dialog-close\" ng-click=\"closeNestedList()\">X</span></div>" + 
					"<div class=\"panel-list-dialog-breadcrumb\">" + 
						"<span class=\"panel-list-dialog-breadcrumb-item\" ng-repeat=\"bCrumb in nestedDMs\">" + 
							"<a href=\"\" ng-click=\"showNestedList($index)\" disabled=\"{{$last}}\">{{bCrumb.label}}</a>" + 
								"<span ng-show=\"!$last\"> &raquo; </span></span></div>" + 
					"<div class=\"panel-list-dialog-content\"></div>" + 
				"</div>";

			var data = codeGenerator.create().generate(json, BINDING_PREFIX, i18nLabelProvider);
			data.html += nestedListsDiv;
			document.getElementsByTagName("body")[0].innerHTML = data.html;
			
			bindings = data.binding;
		};

		/*
		 * 
		 */
		function receiveDateFormats(json) {
			clientDateFormat = json.dateFormat;
			if (clientDateFormat) {
				clientDateFormat = clientDateFormat.toLowerCase();
			}
		}

		/*
		 * val: path object or string
		 */
		function i18nLabelProvider(val) {
			var key = val;

			if (("string" != typeof (val))) {
				var parts = val.fullXPath.substring(1).split("/");
				if (parts.length == 1) { // First Level means Data/DataMapping
					key = "Data." + val.id + ".Name";
				} else { // More than 1 level means XSD
					var prefix = "";
					for(var i = 0; i < parts.length; i++) {
						prefix += parts[i] + ".";
					}
					key = "StructuredType." + prefix + "Name";
				}
			}

			var value = InfinityBPMI18N.manualActivity.getProperty(key);
			return value;
		}

		/*
		 * 
		 */
		function bindInData(data) {
			runInAngularContext(function($scope){
				$scope[BINDING_PREFIX] = bindings;
				jQuery.extend($scope[BINDING_PREFIX], data);
				
				massageInData($scope[BINDING_PREFIX]);
			});
		};

		/*
		 * 
		 */
		function massageInData(data) {
			addIfBlankArray(dataMappings, data);
		}
		
		/*
		 * 
		 */
		function addIfBlankArray(arrPaths, data) {
			for (var key in arrPaths) {
				if (arrPaths[key].readonly) {
					continue;
				}
				
				if (arrPaths[key].isList) {
					var parts = arrPaths[key].fullXPath.substring(1).split("/");
					var currentBinding = data;
					for(var i = 0; i < parts.length; i++) {
						currentBinding = currentBinding[parts[i]];
						if (currentBinding == undefined) {
							break;
						}
					}

					if (currentBinding) {
						if (currentBinding.length == 0) {
							currentBinding.push({});
						} else {
							if (arrPaths[key].children) {
								addIfBlankArray(arrPaths[key].children, data);
							}
						}
					}
				} else if (arrPaths[key].children) {
					addIfBlankArray(arrPaths[key].children, data);
				}
			}
		}

		/*
		 * 
		 */
		function addToList(list) {
			var $scope = angular.element(document).scope();
			if (list != undefined) {
				list.push({});
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
		function removeFromList(list) {
			var $scope = angular.element(document).scope();
			if (list) {
				removeSelectedElements(list);
				if (list.length == 0) {
					list.push({});
				}
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
		function isFormValid() {
			var $scope = angular.element(document).scope();
			var ret = $scope.form.$valid;
			if (ret) {
				ret = areNestedFormsValid();
			}
			return ret;
		}

		/*
		 * 
		 */
		function areNestedFormsValid() {
			var $scope = angular.element(document).scope();
			var ret = true;
			for(var i = 0; i < 20; i++) {
				if ($scope['nForm' + i] != undefined) {
					if ($scope['nForm' + i].$invalid) {
						ret = false;
						break;
					}
				}
			}
			return ret;
		}

		/*
		 * 
		 */
		function isFocusNestedFormValid() {
			var $scope = angular.element(document).scope();
			var ret = true;
			
			if ($scope.nestedDMs && $scope.nestedDMs.length > 0) {
				for(var i = 0; i < $scope.nestedDMs.length; i++) {
					if ($scope.nestedDMs[i].visible) {
						if ($scope['nForm' + i] != undefined) {
							ret = $scope['nForm' + i].$valid;
						}
						break;
					}
				}
			}
			return ret;
		}

		/*
		 * 
		 */
		function saveData() {
			var success = false;
			if (isFormValid()) {
				var $scope = angular.element(document).scope();
				postData(interactionEndpoint, "/outData", $scope[BINDING_PREFIX], {success: function() {
					success = true;
				}, failure: function() {
					success = false;;
				}});
			}
			return success;
		}

		/*
		 * 
		 */
		function openDocument(xPath) {
			log("TODO: " + xPath);
		}

		/*
		 * 
		 */
		function openNestedList(binding, xPath, index, listBinding, parentLabel, childLabel, readonly) {
			var path = getPath(xPath);
			
			var breadcrumbLabel = parentLabel + "[" + index + "]." + childLabel;
			
			var scope = angular.element(document).scope();
			
			if (!scope.showNestedDM) {
				scope.nestedDMTitle = readonly ? i18nLabelProvider("panel.list.dialog.view") : i18nLabelProvider("panel.list.dialog.edit");
				scope.showNestedDM = true;
			}
			
			if (!scope.nestedDMs) {
				scope.nestedDMs = [];
			} else {
				// Dialog is already open
				if (scope.nestedDMs.length > 0) {
					if (!isFocusNestedFormValid()) {
						return;
					}
					for(var i = 0; i < scope.nestedDMs.length; i++) {
						if (scope.nestedDMs[i].label == breadcrumbLabel) {
							showNestedList(i);
							return;
						}
					}
				}
			}
			
			if (scope.nestedDMs.length > 0) {
				for(var i = 0; i < scope.nestedDMs.length; i++) {
					scope.nestedDMs[i].visible = false;
				}
			}
			scope.nestedDMs.push({visible: true, label: breadcrumbLabel});

			var nestedIndex = scope.nestedDMs.length - 1;

			var parts = xPath.substring(1).split("/");
			var lastPart = parts[parts.length - 1];

			var parentXPath = xPath.substring(0, xPath.lastIndexOf("/"));

			// NDM[0]
			var nestedBindingPrefix = listBinding + "[" + index + "]";

			var json = [];
			json.push(path);
			var data = codeGenerator.create().generate(json, nestedBindingPrefix, i18nLabelProvider, parentXPath, "nForm" + nestedIndex);
			
			for (var key in data.binding) {
				if (binding[key] == undefined) {
					jQuery.extend(binding, data.binding);
					// massageInData(scope[BINDING_PREFIX]);
					break;
				} else {
					break;
				}
			}

			var nestedHtml = "<div ng-show=\"nestedDMs[" + nestedIndex + "].visible\" class=\"Nested" + nestedIndex + "\">" + data.html + "</div>";
			jQuery(".panel-list-dialog-content").append(nestedHtml);

			jQuery(function() {
				var divElem = angular.element(".Nested" + nestedIndex);
				angularCompile(divElem)(divElem.scope());
			});
		}

		/*
		 * 
		 */
		function closeNestedList() {
			if (isFocusNestedFormValid()) {
				jQuery(".panel-list-dialog-content").html("");
				
				var scope = angular.element(document).scope();
				scope.showNestedDM = false;
				scope.nestedDMTitle = undefined;
				scope.nestedDMs = undefined;
			}
		}

		/*
		 * 
		 */
		function showNestedList(index) {
			var scope = angular.element(document).scope();
			
			if (scope.nestedDMs.length > 0 && isFocusNestedFormValid()) {
				// Remove HTML
				for(var i = index + 1; i < scope.nestedDMs.length; i++) {
					jQuery(".panel-list-dialog-content .Nested" + i).remove();
				}

				// Truncate Array
				scope.nestedDMs.length = index + 1;
				
				for(var i = 0; i < scope.nestedDMs.length; i++) {
					if (i == index) {
						scope.nestedDMs[i].visible = true;
					} else {
						scope.nestedDMs[i].visible = false;
					}
				}
			}
		}

		/*
		 * 
		 */
		function getPath(xPath) {
			var parts = xPath.substring(1).split("/");
			var found;
			var currentBinding = dataMappings;
			for(var i = 0; i < parts.length; i++) {
				found = false;
				for(var j in currentBinding) {
					if (parts[i] == currentBinding[j].id) {
						found = true;
						currentBinding = currentBinding[j];
						break;
					} 
				}
				if (found) {
					if (i < parts.length - 1) { // Last Part
						currentBinding = currentBinding.children;
					}
				} else {
					return null;
				}
			}
			return currentBinding;
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