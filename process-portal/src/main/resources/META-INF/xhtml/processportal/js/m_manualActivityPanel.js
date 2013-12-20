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
 * 
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
		var SERVER_DATE_TIME_FORMAT_SEPARATOR = "T";

		var angularCompile;
		
		var interactionEndpoint;

		var dataMappings;
		var bindings;
		var clientDateFormat = "dd-mm-yy";
		var configuration;

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
	        
	        getData(interactionEndpoint, "/configuration", {success: receiveConfiguration});

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
				$scope.deleteDocument = deleteDocument;
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
				"<iframe ng-show=\"showNestedDM\" class=\"panel-list-dialog-modal-iframe\"" +
					"style=\"width: {{sizes.frameWidth}}; height: {{sizes.frameHeight}};\" src=\"about:blank\"></iframe>\n" +
				"<div ng-show=\"showNestedDM\" class=\"panel-list-dialog\" style=\"left: {{sizes.dialogLeft}}; top: {{sizes.dialogTop}}\">" + 
					"<div class=\"panel-list-dialog-title\">" +
						"<span class=\"panel-list-dialog-title-text\">{{nestedDMTitle}}</span>" + 
						"<input type=\"button\" class=\"panel-list-dialog-close\" ng-click=\"closeNestedList()\"></input></div>" + 
					"<div class=\"panel-list-dialog-breadcrumb\">" + 
						"<span class=\"panel-list-dialog-breadcrumb-item\" ng-repeat=\"bCrumb in nestedDMs\">" + 
							"<a href=\"\" ng-click=\"showNestedList($index)\" ng-show=\"!$last\">{{bCrumb.label}}</a>" + 
							"<span ng-show=\"$last\">{{bCrumb.label}}</span>" +
							"<span ng-show=\"!$last\"> &raquo; </span></span></div>" + 
					"<div class=\"panel-list-dialog-content\"></div>" + 
					"<div class=\"panel-list-dialog-footer\">" +
						"<input type=\"button\" value=\"Close\" class=\"panel-list-dialog-footer-control\" ng-click=\"closeNestedList()\" /></div>" +
				"</div>";

			var data = codeGenerator.create(configuration).generate(json, BINDING_PREFIX, i18nLabelProvider());
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
		 * 
		 */
		function receiveConfiguration(json) {
			configuration = json;
		}

		/*
		 *
		 */
		function i18nLabelProvider() {
			return {
				getLabel : getLabel,
				getEnumerationLabels : getEnumerationLabels,
				getDescription : getDescription
			};
			
			/*
			 * val: path object or string
			 */
			function getLabel(val, defaultValue) {
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

				var value = InfinityBPMI18N.manualActivity.getProperty(key, defaultValue);
				return value;
			}

			/*
			 * 
			 */
			function getEnumerationLabels(path) {
				var labels;
				if (path.typeName == "PROCESS_PRIORITY") {
					labels = {};
					for(var i in path.enumValues) {
						var optKey = "" + path.enumValues[i];
						var i18nKey = "common.process.priority.options." + optKey;
						labels[optKey] = InfinityBPMI18N.manualActivity.getProperty(i18nKey);
					}
				}
				return labels;
			}

			/*
			 * 
			 */
			function getDescription(path) {
				// TODO
			}
		}

		/*
		 * 
		 */
		function bindInData(data) {
			runInAngularContext(function($scope){
				$scope[BINDING_PREFIX] = bindings;
				jQuery.extend($scope[BINDING_PREFIX], data);
				
				marshalInData($scope[BINDING_PREFIX]);
			});
		};

		/*
		 * 
		 */
		function marshalInData(data) {
			marshalForLists(dataMappings, data);
			marshalForPrimitives(dataMappings, data);
		}
		
		/*
		 * 
		 */
		function marshalForLists(arrPaths, data) {
			for (var key in arrPaths) {
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
							currentBinding.push(arrPaths[key].isPrimitive ? {$value: ""} : {});
						} else {
							if (arrPaths[key].children) {
								marshalForLists(arrPaths[key].children, data);
							} else {
								for(var k in currentBinding) {
									currentBinding[k] = {$value: currentBinding[k]};
								}
							}
						}
					}
				} else if (arrPaths[key].children) {
					marshalForLists(arrPaths[key].children, data);
				}
			}
		}

		/*
		 * 
		 */
		function marshalForPrimitives(arrPaths, data) {
			for (var key in arrPaths) {
				if (arrPaths[key].isPrimitive) {
					if (arrPaths[key].typeName == "duration") {
						var parts = arrPaths[key].fullXPath.substring(1).split("/");
						var lastPart = parts[parts.length - 1];
						var currentBinding = data;
						for(var i = 0; i < parts.length - 1; i++) {
							currentBinding = currentBinding[parts[i]];
							if (currentBinding == undefined) {
								break;
							}
						}
						
						if (currentBinding && currentBinding[lastPart] && 
								null != currentBinding[lastPart] && "" != currentBinding[lastPart]) {
							try {
								// Remove Trailing Zeros
								var periods = currentBinding[lastPart].split(":");
								var value = "";
								for(var j = 0; j < periods.length; j++) {
									value += (parseInt(periods[j])) + ":";
								}
								currentBinding[lastPart] = value.substring(0, value.length - 1);
							} catch(e) {
								// TODO
							}
						}
					} else if (arrPaths[key].typeName == "date" || arrPaths[key].typeName == "java.util.Date") {
						var parts = arrPaths[key].fullXPath.substring(1).split("/");
						var lastPart = parts[parts.length - 1];
						var currentBinding = data;
						for(var i = 0; i < parts.length - 1; i++) {
							currentBinding = currentBinding[parts[i]];
							if (currentBinding == undefined) {
								break;
							}
						}
						
						if (currentBinding) {
							var value = currentBinding[lastPart];
							var datePart;
							if (value) {
								try {
									var dateParts = value.split(SERVER_DATE_TIME_FORMAT_SEPARATOR); // To get 2 parts
									if (dateParts.length >= 1) {
										datePart = dateParts[0];
									}
								} catch(e) {
									log(e);
								}
							}
							currentBinding[lastPart] = datePart;
						}
					} else if (arrPaths[key].typeName == "dateTime" || 
							arrPaths[key].typeName == "java.util.Calendar" || arrPaths[key].typeName == "time") {
						var parts = arrPaths[key].fullXPath.substring(1).split("/");
						var lastPart = parts[parts.length - 1];
						var currentBinding = data;
						for(var i = 0; i < parts.length - 1; i++) {
							currentBinding = currentBinding[parts[i]];
							if (currentBinding == undefined) {
								break;
							}
						}
						
						if (currentBinding) {
							var value = currentBinding[lastPart];
							var datePart;
							var timePart;
							if (value) {
								try {
									var dateParts = value.split(SERVER_DATE_TIME_FORMAT_SEPARATOR); // Get 2 Parts
									if (dateParts.length >= 1) {
										datePart = dateParts[0];
									}
									if (dateParts.length >= 2) {
										var timeParts = dateParts[1].split(":"); // Get 3 Parts, and stripoff seconds part
										timePart = timeParts[0] + ":" + timeParts[1];
									}
								} catch(e) {
									log(e);
								}
							}
							currentBinding[lastPart] = datePart;
							currentBinding[lastPart + "_timePart"] = timePart;
						}
					}
				} else if (arrPaths[key].children) {
					marshalForPrimitives(arrPaths[key].children, data);
				}
			}
		}

		/*
		 * 
		 */
		function unmarshalOutData(data) {
			unmarshalForLists(dataMappings, data);
			unmarshalForPrimitives(dataMappings, data);
			removeInternalVariables(data);
		}

		/*
		 * 
		 */
		function unmarshalForLists(arrPaths, data) {
			for (var key in arrPaths) {
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
						if (currentBinding.length > 0) {
							if (arrPaths[key].children) {
								unmarshalForLists(arrPaths[key].children, data);
							} else {
								for(var k in currentBinding) {
									currentBinding[k] = currentBinding[k].$value;
								}
							}
						}
					}
				} else if (arrPaths[key].children) {
					unmarshalForLists(arrPaths[key].children, data);
				}
			}
		}

		/*
		 * 
		 */
		function unmarshalForPrimitives(arrPaths, data) {
			for (var key in arrPaths) {
				if (arrPaths[key].isPrimitive) {
					if (arrPaths[key].typeName == "dateTime" || 
							arrPaths[key].typeName == "java.util.Calendar" || arrPaths[key].typeName == "time") {
						var parts = arrPaths[key].fullXPath.substring(1).split("/");
						var lastPart = parts[parts.length - 1];
						var currentBinding = data;
						for(var i = 0; i < parts.length - 1; i++) {
							currentBinding = currentBinding[parts[i]];
							if (currentBinding == undefined) {
								break;
							}
						}

						if (currentBinding) {
							var dateValue = currentBinding[lastPart];
							var timeValue = currentBinding[lastPart + "_timePart"];
							
							var value = "";
							if (dateValue) {
								value = dateValue;
							}
							if (timeValue) {
								if (value.length > 0) {
									value += SERVER_DATE_TIME_FORMAT_SEPARATOR;
								}
								value += timeValue + ":00"; // Add seconds part
							}
							currentBinding[lastPart] = value;
							delete currentBinding[lastPart + "_timePart"];
						}
					}
				} else if (arrPaths[key].children) {
					unmarshalForPrimitives(arrPaths[key].children, data);
				}
			}
		}

		/*
		 * 
		 */
		function removeInternalVariables(object) {
			for (key in object) {
				if (key.indexOf("$") == 0) {
					delete object[key];
				} else if (object[key] != null) {
					// http://perfectionkills.com/instanceof-considered-harmful-or-how-to-write-a-robust-isarray/
					if (Object.prototype.toString.call(object[key]) === "[object Array]") {
						var arr = object[key];
						for (var n in arr) {
							removeInternalVariables(arr[n]);
						}
					} else if (typeof object[key] == "object") {
						removeInternalVariables(object[key]);
					}
				}
			}
		}

		/*
		 * 
		 */
		function addToList(list, primitive) {
			if (list != undefined) {
				list.push(primitive ? {$value: ""} : {});
			}
		}

		/*
		 * 
		 */
		function selectListItem(event, obj) {
			// Select if target is Row/Column 
			if (event.target.localName.toLowerCase() == "td" || event.target.localName.toLowerCase() == "tr") {
				if (obj.$$selected == undefined || obj.$$selected == false) {
					obj.$$selected = true;
				} else {
					obj.$$selected = false;
				}
			}
		}
		
		

		/*
		 * 
		 */
		function removeFromList(list) {
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
				var toPost = jQuery.extend(true, {}, $scope[BINDING_PREFIX]);
				unmarshalOutData(toPost);
				postData(interactionEndpoint, "/outData", toPost, {success: function(retData) {
					if (retData && retData.errors) {
						success = false;
						
						var msg = i18nLabelProvider().getLabel("panel.save.error", "Failure to save data");
						msg += "\n";
						for(var key in retData.errors) {
							msg += "\n" + key + " -> " + retData.errors[key];
						}

						alert(msg);
					} else {
						success = true;	
					}
				}, failure: function() {
					success = false;
					alert(i18nLabelProvider().getLabel("panel.save.error", "Failure to save data"));
				}});
			}
			return success;
		}

		/**
		 * @param xPath
		 * @author Yogesh.Manware
		 */
		function openDocument(xPath, documentPathLabel, readonly) {
			var $scope = angular.element(document).scope();
			var parts = xPath.substring(1).split("/");
			
			var currentBindings = $scope[BINDING_PREFIX];
			var lastPart = parts[parts.length - 1];
			
			documentPathLabel = i18nLabelProvider().getLabel("panel.fileUpload.dialog.msg").replace("{0}", documentPathLabel);
			
			if (!currentBindings[lastPart] || !currentBindings[lastPart].docId) {
				//Document is not set
				if (!readonly && parent.iPopupDialog) {
					parent.iPopupDialog.openPopup({
						attributes : {
							width : "50%",
							height : "45%",
							src : "../views-common/popups/fileUploadPopupDialogContent.html"
						},
						payload : {
							title : i18nLabelProvider().getLabel("panel.fileUpload.dialog.title"),
							message: documentPathLabel,							
							documentTypeName: currentBindings[lastPart].docTypeName,
							acceptFunction : function(fileUploadData) {
								runInAngularContext(function($scope){
									//Form Data
									var params = {};
									params.description = fileUploadData.fileDescription;
									params.comments = fileUploadData.versionComment;
									currentBindings[lastPart].params = params;

									//File data
									currentBindings[lastPart].docId = fileUploadData.fileDetails.uuid;
									currentBindings[lastPart].docIcon = fileUploadData.fileDetails.docIcon;
									currentBindings[lastPart].docName = fileUploadData.fileDetails.fileName;
								});
								
								if(fileUploadData.openDocument){
									openDocumentViewer(currentBindings[lastPart]);
								}
							}
						}
					});
				} else {
					//alert("not available");
				}
				
			}else{
				// Document is already set - get latest data from server!
				getData(interactionEndpoint, "/inData/" + lastPart, {success: function(retData) {
					if (retData[lastPart].docId) {
						// it is JCR document
						currentBindings[lastPart].docId = retData[lastPart].docId;
						// reset client side stale data
						currentBindings[lastPart].params = null;
					}else{
						// it is file system document so no change in client side data
					}
				}, failure: function() {
					success = false;
					alert("Failure while getting data");
				}});
				
				openDocumentViewer(currentBindings[lastPart]);
			}
		}
		
		/**
		 * open document view using parent.postmessage
		 *  
		 */
		function openDocumentViewer(fileDetails){
			var msg = {};
			msg.type = "OpenView";
			msg.data = {};
			msg.data.viewId = "documentView";
			msg.data.viewKey = "documentOID=" + fileDetails.docId;
			msg.data.nested = true;
			
			msg.data.params = {};
			
			msg.data.params.documentId = fileDetails.docId;
			msg.data.params.docInteractionId = fileDetails.docInteractionId;
			msg.data.params.processInstanceOId = fileDetails.processInstanceOId;
			msg.data.params.dataPathId = fileDetails.dataPathId;
			msg.data.params.dataId = fileDetails.dataId;
			msg.data.params.docTypeId = fileDetails.docTypeId;
			msg.data.params.disableAutoDownload = false;
			
			//Form data, post it to server only if it is not jcr document
			if (fileDetails.params) {
				msg.data.params.description = fileDetails.params.description;
				msg.data.params.comments = fileDetails.params.comments;
			}
			parent.postMessage(JSON.stringify(msg), "*");			
		}
		
		/*
		 * 
		 */
	    function getWindowScrollPosition(targetWin) {
	    	var scrollX = 0;
	    	var scrollY = 0;
			if (navigator.appName == 'Netscape') {
				scrollX = targetWin.pageXOffset;
				scrollY = targetWin.pageYOffset;
			} else if (navigator.appName == 'Microsoft Internet Explorer') {
				scrollX = targetWin.document.body.scrollLeft;
				scrollY = targetWin.document.body.scrollTop;
			}

			return {'x' : scrollX, 'y' : scrollY};
	    }

		/*
		 * 
		 */
		function openNestedList(binding, xPath, index, listBinding, parentLabel, childLabel, readonly) {
			var path = getPath(xPath);
			
			var breadcrumbLabel = (index + 1) + ". " + i18nLabelProvider().getLabel("panel.list.dialog.breadcrumb.of") + " " + 
				parentLabel + " / " + childLabel;
			
			var scope = angular.element(document).scope();
			
			if (!scope.showNestedDM) {
				scope.nestedDMTitle = readonly ? i18nLabelProvider().getLabel("panel.list.dialog.view") : i18nLabelProvider().getLabel("panel.list.dialog.edit");
				scope.showNestedDM = true;

				var documentSize = {
					width: jQuery(document).outerWidth(),
					height: jQuery(document).outerHeight()
				};

				var windowSize = {
					width: jQuery(window).outerWidth(),
					height: jQuery(window).outerHeight()
				};

				var scrollPos = getWindowScrollPosition(window);
		    	var dialogLeft, dialogTop;
				try {
					var elemDialogContent = jQuery(".panel-list-dialog-content");
					elemDialogContent.width(windowSize.width * 0.85);
					elemDialogContent.height(windowSize.height * 0.65);
					
					var elemDialog = jQuery(".panel-list-dialog");
					var widthOffset = elemDialog.width();
		    		var heightOffset = elemDialog.height();

		    		dialogLeft = (((windowSize.width - widthOffset)/ 2) + scrollPos.x);
		    		dialogTop = (((windowSize.height - heightOffset)/ 2) + scrollPos.y);

		    		if (dialogLeft <= 0) {
		    			dialogLeft = 5;
		    		}
		    		
		    		if (dialogTop <= 0) {
		    			dialogTop = 5;
		    		}
				} catch(e) {
					dialogLeft = (scrollPos.x + 200);
					dialogTop = (scrollPos.y + 200);
				}
	    		
				scope.sizes = {
					frameWidth: documentSize.width + "px",
					frameHeight: documentSize.height + "px",
					dialogLeft: dialogLeft + "px",
					dialogTop: dialogTop + "px"
				}
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
			var data = codeGenerator.create().generate(json, nestedBindingPrefix, i18nLabelProvider(), parentXPath, "nForm" + nestedIndex);
			
			for (var key in data.binding) {
				if (binding[key] == undefined) {
					jQuery.extend(binding, data.binding);
					// marshalInData(scope[BINDING_PREFIX]);
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

		/**
		 * 
		 * @param param
		 */
		function deleteDocument(xPath) {
			var $scope = angular.element(document).scope();
			var parts = xPath.substring(1).split("/");
			
			var currentBindings = $scope[BINDING_PREFIX];
			var lastPart = parts[parts.length - 1];
			
			if (currentBindings[lastPart] && currentBindings[lastPart].docId) {
				//	Document is set
				var transferData = {};
				transferData[lastPart] = currentBindings[lastPart];
				transferData[lastPart].deleteDocument = true;
				
				postData(interactionEndpoint, "/outData/" + lastPart, transferData, {success: function(retData) {
					currentBindings[lastPart].docId = null;
					currentBindings[lastPart].docIcon = "../../plugins/views-common/images/icons/page_white_error.png";
				}, failure: function() {
					success = false;
					alert(i18nLabelProvider().getLabel("panel.save.error", "Failure to save data"));
				}});
			}else{
				// TODO: Document is not set - this should never happen 
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