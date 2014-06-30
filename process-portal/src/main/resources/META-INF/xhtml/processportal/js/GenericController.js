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
 * @author subodh.godbole
 */

if (!window.bpm) {
	bpm = {};
}

if (!window.bpm.portal) {
	bpm.portal = {};
}

if (!window.bpm.portal.GenericController) {
	bpm.portal.GenericController = function GenericController() {

		this.BINDING_PREFIX = "dm";
		this.SERVER_DATE_FORMAT = "yy-mm-dd";
		this.SERVER_DATE_TIME_FORMAT_SEPARATOR = "T";

		this.angularCompile = null;

		this.dataMappings = null;
		this.clientDateFormat = null;
		this.i18nLabelProvider = null;
		this.interaction = null;
		this.markupProvider = null;
		
		/*
		 * 
		 */
		GenericController.prototype.initialize = function (dataMappings, bindings, cliDateFormat, i18nProvider, interaction, markupProvider) {
			this.dataMappings = dataMappings;
			this.clientDateFormat = cliDateFormat;
			this.i18nLabelProvider = i18nProvider;
			this.interaction = interaction;
			this.markupProvider = markupProvider;

			this.interaction.fetchData(null, {
				success: function(data) {
					jQuery.extend(bindings, data);
				}
			}, this);
				
			this.marshalRecursively(this.dataMappings, bindings);
			this[this.BINDING_PREFIX] = bindings;

			var ang = new bpm.portal.GenericAngularApp({
				module : "ManualActivityModule", ctrl: "ManualActivityCtrl", dateFormat: this.clientDateFormat});
			ang.initialize();
			this.angularCompile = ang.getCompiler();

			this.loadCustomTheme();
		};

		/*
		 * 
		 */
		GenericController.prototype.loadCustomTheme = function() {
			var urlPrefix = "../..";
			if (this.getContextRootUrl) {
				urlPrefix = this.getContextRootUrl();	
			}
			
			jQuery.ajax({
				type : 'GET',
				url : urlPrefix + "/services/rest/common/html5/api/themes/current/custom",
				async : true
			}).done(function(json){
				var head = document.getElementsByTagName('head')[0];
				
				for(var i in json.stylesheets) {
					var link = document.createElement('link');
					link.href = urlPrefix + "/" + json.stylesheets[i];
					link.rel = 'stylesheet';
					link.type = 'text/css';
					head.appendChild(link);
				}
			}).fail(function(err){
				this.log("Failed in loading custom theme");
			});
		};

		/*
		 * 
		 */
		GenericController.prototype.formatDate = function(value, fromFormat, toFormat) {
			if (value != undefined && value != null && value != "") {
				try {
					var date = jQuery.datepicker.parseDate(fromFormat, value);
					value = jQuery.datepicker.formatDate(toFormat, date);
				} catch(e) {
					this.log(e);
				}
			}
			return value;
		};

		/*
		 * 
		 */
		GenericController.prototype.marshalRecursively = function(arrPaths, data, parentXPath) {
			for (var key in arrPaths) {
				var binding = this.getBinding(arrPaths[key], data, parentXPath, true);
				if (binding != undefined) {
					if (arrPaths[key].isList) {
						if (binding.length == 0 && !this.isReadonly(arrPaths[key])) {
							this.addToList(binding, arrPaths[key].fullXPath);
						} else {
							for(var k in binding) {
								if (arrPaths[key].children) {
									this.marshalRecursively(arrPaths[key].children, binding[k], arrPaths[key].fullXPath);
								} else {
									binding[k] = {$value: binding[k]};
									if (this.isDatePath(arrPaths[key])) {
										this.marshalDateTimesValue(arrPaths[key], binding[k], "$value");
									}
								}
							}
						}
					} else if (arrPaths[key].isPrimitive) {
						var bindingInfo = this.getBinding(arrPaths[key], data, parentXPath);
						if (arrPaths[key].typeName == "duration") {
							this.marshalDurationValue(bindingInfo.binding, bindingInfo.lastPart);
						} else if (this.isDatePath(arrPaths[key])) {
							this.marshalDateTimesValue(arrPaths[key], bindingInfo.binding, bindingInfo.lastPart);
						}else if ("boolean" == arrPaths[key].typeName || "java.lang.Boolean" == arrPaths[key].typeName) {
							this.marshalBooleanValue(bindingInfo.binding, bindingInfo.lastPart);	
						}

						if (arrPaths[key].isEnum) {
							this.marshalEnumValue(arrPaths[key], bindingInfo.binding, bindingInfo.lastPart);
						}
						
					} else if (arrPaths[key].children) {
						this.marshalRecursively(arrPaths[key].children, binding, arrPaths[key].fullXPath);
					} 
				}
			}
		};

		/*
		 * 
		 */
		GenericController.prototype.marshalDurationValue = function(binding, lastPart) {
			if (binding && binding[lastPart] && 
					null != binding[lastPart] && "" != binding[lastPart]) {
				try {
					// Remove Trailing Zeros
					var periods = binding[lastPart].split(":");
					var value = "";
					for(var j = 0; j < periods.length; j++) {
						value += (parseInt(periods[j])) + ":";
					}
					binding[lastPart] = value.substring(0, value.length - 1);
				} catch(e) {
					// TODO
				}
			}
		};

		/*
		 * 
		 */
		GenericController.prototype.marshalDateTimesValue = function(path, binding, lastPart) {
			if (this.isReadonly(path)) {
				return;
			}

			if (binding) {
				var haveTime = path.typeName == "dateTime" || path.typeName == "java.util.Date" 
					|| path.typeName == "java.util.Calendar" || path.typeName == "time";
				
				var value = binding[lastPart];
				var datePart = "";
				var timePart = "";
				if (value) {
					try {
						var dateParts = value.split(this.SERVER_DATE_TIME_FORMAT_SEPARATOR); // Get 2 Parts
						if (dateParts.length >= 1) {
							datePart = this.formatDate(dateParts[0], this.SERVER_DATE_FORMAT, this.clientDateFormat);
						}
						if (haveTime && dateParts.length >= 2) {
							var timeParts = dateParts[1].split(":"); // Get 3 Parts, and stripoff seconds part
							timePart = timeParts[0] + ":" + timeParts[1];
						}
					} catch(e) {
						this.log(e);
					}
				}

				binding[lastPart] = datePart;
				if (haveTime) {
					binding[lastPart + "_timePart"] = timePart;
				}
			}
		};

		/*
		 * 
		 */
		GenericController.prototype.marshalBooleanValue = function(binding, lastPart) {
			if (binding != null && binding != "" && lastPart != null && lastPart != "") {
				if (binding[lastPart] == null || binding[lastPart] == "" || binding[lastPart] == "false") {
					binding[lastPart] = false;
				} else if (binding[lastPart] == "true") {
					binding[lastPart] = true;
				}
			}
		};

		/*
		 * 
		 */
		GenericController.prototype.marshalEnumValue = function(path, binding, lastPart) {
			if (binding != null && binding != "" && lastPart != null && lastPart != "") {
				// If Enum value is blank then default it to 1st in List
				if (binding[lastPart] == null || binding[lastPart] == "") {
					if (path.enumValues && path.enumValues.length >= 1) {
						binding[lastPart] = path.enumValues[0];
					}
				}
			}
		};
			
		/*
		 * 
		 */
		GenericController.prototype.unmarshalOutData = function(data) {
			this.unmarshalRecursively(this.dataMappings, data, true);
			this.removeInternalVariables(data);
		}

		/*
		 * 
		 */
		GenericController.prototype.unmarshalRecursively = function(arrPaths, data, removeReadonly, parentXPath) {
			for (var key in arrPaths) {
				var bindingInfo = this.getBinding(arrPaths[key], data, parentXPath);
				var binding = bindingInfo.binding[bindingInfo.lastPart];

				if (binding != undefined) {
					if (removeReadonly && this.isReadonly(arrPaths[key])) {
						delete bindingInfo.binding[bindingInfo.lastPart];
						continue;
					}

					if (arrPaths[key].isList) {
						if (binding.length > 0) {
							for(var k in binding) {
								if (arrPaths[key].children) {
									this.unmarshalRecursively(arrPaths[key].children, binding[k], false, arrPaths[key].fullXPath);
								} else {
									if (this.isDatePath(arrPaths[key])) {
										this.unmarshalDateTimesValue(arrPaths[key], binding[k], "$value");
									}
									binding[k] = binding[k].$value;
								}
							}
						}
					} else if (arrPaths[key].isPrimitive) {
						if (this.isDatePath(arrPaths[key])) {
							this.unmarshalDateTimesValue(arrPaths[key], bindingInfo.binding, bindingInfo.lastPart);
						}
					} else if (arrPaths[key].children) {
						this.unmarshalRecursively(arrPaths[key].children, binding, false, arrPaths[key].fullXPath);
					}
				}
			}
		};

		/*
		 * 
		 */
		GenericController.prototype.unmarshalDateTimesValue = function(path, binding, lastPart) {
			if (this.isReadonly(path)) {
				return;
			}

			if (binding) {
				var dateValue = binding[lastPart];
				var timeValue = binding[lastPart + "_timePart"];
				
				var value = "";
				if (dateValue) {
					value = this.formatDate(dateValue, this.clientDateFormat, this.SERVER_DATE_FORMAT);
				}
				if (timeValue) {
					if (value.length > 0) {
						value += this.SERVER_DATE_TIME_FORMAT_SEPARATOR;
					}
					value += timeValue + ":00"; // Add seconds part
				}

				binding[lastPart] = value;
				if (binding[lastPart + "_timePart"] != undefined) {
					delete binding[lastPart + "_timePart"];
				}
			}
		};

		/*
		 * 
		 */
		GenericController.prototype.isDatePath = function(path) {
			if (path.typeName == "date" || path.typeName == "java.util.Date" || 
					path.typeName == "dateTime" || path.typeName == "java.util.Calendar" || 
					path.typeName == "time") {
				return true;
			}
			return false;
		};
		
		/*
		 * 
		 */
		GenericController.prototype.removeInternalVariables = function(object) {
			for (key in object) {
				if (key.indexOf("$") == 0) {
					delete object[key];
				} else if (object[key] != null) {
					// http://perfectionkills.com/instanceof-considered-harmful-or-how-to-write-a-robust-isarray/
					if (Object.prototype.toString.call(object[key]) === "[object Array]") {
						var arr = object[key];
						for (var n in arr) {
							this.removeInternalVariables(arr[n]);
						}
					} else if (typeof object[key] == "object") {
						this.removeInternalVariables(object[key]);
					}
				}
			}
		}

		/*
		 * 
		 */
		GenericController.prototype.addToList = function(list, xPath) {
			var path = this.getPath(xPath);
			if (path != null && list != undefined) {
				var value = {};
				if (path.isPrimitive) {
					value.$value = "";
					if ("boolean" === path.typeName || "java.lang.Boolean" === path.typeName) {
						this.marshalBooleanValue(value, "$value");
					}

					if (path.isEnum) {
						this.marshalEnumValue(path, value, "$value");
					}
				} else {
					for (var i in path.children) {
						if (path.children[i].isPrimitive && !path.children[i].isList) {
							var key = path.children[i].id;

							if ("boolean" === path.children[i].typeName || "java.lang.Boolean" === path.children[i].typeName) {
								this.marshalBooleanValue(value, key);
							}

							if (path.children[i].isEnum) {
								this.marshalEnumValue(path.children[i], value, key);
							}
						}
					}
				}
				list.push(value);
			}
		}

		/*
		 * 
		 */
		GenericController.prototype.selectListItem = function(event, obj) {
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
		GenericController.prototype.removeFromList = function(list, xPath) {
			var path = this.getPath(xPath);
			if (path != null && list != undefined) {
				this.removeSelectedElements(list);
				if (list.length == 0) {
					this.addToList(list, xPath);
				}
			}
		}

		/*
		 * 
		 */
		GenericController.prototype.removeSelectedElements = function(arr) {
			for(var i = 0 ; i < arr.length; i++) {
				if (arr[i].$$selected) {
					arr.splice(i, 1);
					this.removeSelectedElements(arr);
					break;
				}
			}
		};

		/*
		 * 
		 */
		GenericController.prototype.isFormValid = function() {
			var $scope = this.getAngularScope();
			var ret = $scope.form ? $scope.form.$valid : true;
			if (ret) {
				ret = this.areNestedFormsValid();
			}
			return ret;
		}

		/*
		 * 
		 */
		GenericController.prototype.areNestedFormsValid = function() {
			var $scope = this.getAngularScope();
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
		};

		/*
		 * 
		 */
		GenericController.prototype.isFocusNestedFormValid = function() {
			var $scope = this.getAngularScope();
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
		};

		/*
		 * 
		 */
		GenericController.prototype.saveData = function() {
			var success = false;
			var self = this;
			if (this.isFormValid()) {
				var $scope = this.getAngularScope();
				var toPost = jQuery.extend(true, {}, $scope[this.BINDING_PREFIX]);
				this.unmarshalOutData(toPost);
				this.interaction.saveData(null, toPost, {success: function(retData) {
					if (retData && retData.errors) {
						success = false;
						
						var msg = self.i18nLabelProvider.getLabel("panel.save.error", "Failure to save data");
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
					alert(self.i18nLabelProvider.getLabel("panel.save.error", "Failure to save data"));
				}});
			}
			return success;
		};

		/**
		 * @param xPath
		 * @author Yogesh.Manware
		 */
		GenericController.prototype.openDocument = function(xPath, documentPathLabel, readOnly) {
			var $scope = this.getAngularScope();
			
			var parts = xPath.substring(1).split("/");
			
			var currentBindings = $scope[this.BINDING_PREFIX];
			var lastPart = parts[parts.length - 1];
			
			if (currentBindings[lastPart].showDocMenu) {
				currentBindings[lastPart].showDocMenu = false;
				return;
			}
			
			documentPathLabel = this.i18nLabelProvider.getLabel("panel.fileUpload.dialog.msg", "Upload a Document to {0}").replace("{0}", documentPathLabel);
			
			if (!currentBindings[lastPart] || !currentBindings[lastPart].docId) {
				//Document is not set
				if (!readOnly && parent.iPopupDialog) {
					var self = this;
					parent.iPopupDialog.openPopup({
						attributes : {
							width : "50%",
							height : "60%",
							src : "../views-common/popups/fileUploadPopupDialogContent.html"
						},
						payload : {
							title : this.i18nLabelProvider.getLabel("panel.fileUpload.dialog.title", "Upload a Document"),
							message: documentPathLabel,							
							documentTypeName: currentBindings[lastPart].docTypeName,
							acceptFunction : function(fileUploadData) {
								self.runInAngularContext(function($scope){
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
									self.openDocumentViewer(currentBindings[lastPart], false);
								}
							}
						}
					});
				} else {
					//alert("not available");
				}
				
			}else{
				// Document is already set - get latest data from server!
				this.interaction.fetchData(lastPart, {success: function(retData) {
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
				
				this.openDocumentViewer(currentBindings[lastPart], readOnly);
			}
		}
		
		/**
		 * open document view using parent.postmessage
		 *  
		 */
		GenericController.prototype.openDocumentViewer = function(fileDetails, readOnly){
			var msg = {};
			msg.type = "OpenView";
			msg.data = {};
			msg.data.viewId = "documentView";
			msg.data.viewKey = window.btoa("documentOID=" + fileDetails.docId);
			msg.data.nested = true;
			
			msg.data.params = {};
			
			msg.data.params.documentId = fileDetails.docId;
			msg.data.params.docInteractionId = fileDetails.docInteractionId;
			msg.data.params.processInstanceOId = fileDetails.processInstanceOId;
			msg.data.params.dataPathId = fileDetails.dataPathId;
			msg.data.params.dataId = fileDetails.dataId;
			//msg.data.params.docTypeId = fileDetails.docTypeId;
			msg.data.params.disableAutoDownload = false;
			
			if(msg.data.params){
				msg.data.params.disableSaveAction = readOnly;	
			}
			
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
		GenericController.prototype.getWindowScrollPosition = function(targetWin) {
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
		GenericController.prototype.openNestedList = function(binding, xPath, index, listBinding, parentLabel, childLabel, readonly) {
			var path = this.getPath(xPath);
			
			var breadcrumbLabel = (index + 1) + ". " + this.i18nLabelProvider.getLabel("panel.list.dialog.breadcrumb.of", "of") + " " + 
				parentLabel + " / " + childLabel;
			
			var scope = this.getAngularScope();
			
			if (!scope.showNestedDM) {
				scope.nestedDMTitle = readonly ? this.i18nLabelProvider.getLabel("panel.list.dialog.view", "View Data") : this.i18nLabelProvider.getLabel("panel.list.dialog.edit", "Edit Data");
				scope.showNestedDM = true;

				var documentSize = {
					width: jQuery(document).outerWidth(),
					height: jQuery(document).outerHeight()
				};

				var windowSize = {
					width: jQuery(window).outerWidth(),
					height: jQuery(window).outerHeight()
				};

				var scrollPos = this.getWindowScrollPosition(window);
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
					if (!this.isFocusNestedFormValid()) {
						return;
					}
					for(var i = 0; i < scope.nestedDMs.length; i++) {
						if (scope.nestedDMs[i].label == breadcrumbLabel) {
							this.showNestedList(i);
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

			var data = this.markupProvider.getMarkup(path, nestedBindingPrefix, this.i18nLabelProvider, parentXPath, "nForm" + nestedIndex);
			
			for (var key in data.binding) {
				if (binding[key] == undefined) {
					jQuery.extend(binding, data.binding);
					this.marshalRecursively(path.children, binding[key], parentXPath + "/" + key);
					break;
				} else {
					break;
				}
			}

			var nestedHtml = "<div ng-show=\"nestedDMs[" + nestedIndex + "].visible\" class=\"Nested" + nestedIndex + "\">" + data.html + "</div>";
			jQuery(".panel-list-dialog-content").append(nestedHtml);

			var self = this;
			jQuery(function() {
				var divElem = angular.element(".Nested" + nestedIndex);
				self.angularCompile(divElem)(divElem.scope());
			});
		};

		/*
		 * 
		 */
		GenericController.prototype.closeNestedList = function() {
			if (this.isFocusNestedFormValid()) {
				jQuery(".panel-list-dialog-content").html("");
				
				var scope = this.getAngularScope();
				scope.showNestedDM = false;
				scope.nestedDMTitle = undefined;
				scope.nestedDMs = undefined;
			}
		}

		/*
		 * 
		 */
		GenericController.prototype.showNestedList = function(index) {
			var scope = this.getAngularScope();
			
			if (scope.nestedDMs.length > 0 && this.isFocusNestedFormValid()) {
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
		};

		/**
		 * 
		 * @param xPath
		 */
		GenericController.prototype.showDocumentMenu = function(xPath) {
			var typeDoc = this.getCurrentBindingFor(xPath);
			var selectedTypeDocStatus = typeDoc.showDocMenu;

			this.hideAllDocumentMenus_();
			if (typeDoc.docId && !selectedTypeDocStatus) {
				typeDoc.showDocMenu = true;
			} else {
				typeDoc.showDocMenu = false;
			}
		};
		
		/**
		 * 
		 */
		GenericController.prototype.hideAllDocumentMenus = function() {
			var self = this;
			var $scope = this.getAngularScope();
			
			if ($scope.documentTimeout) {
				clearTimeout($scope.documentTimeout);
			}
			
			$scope.documentTimeout = setTimeout(function() {
				self.runInAngularContext(self.hideAllDocumentMenus_());
			}, 3000);
		};
		
		/**
		 * 
		 */
		GenericController.prototype.hideAllDocumentMenus_ = function() {
			var $scope = this.getAngularScope();
			var currentBindings = $scope[this.BINDING_PREFIX];
			for ( var key in currentBindings) {
				if (currentBindings[key].showDocMenu) {
					currentBindings[key].showDocMenu = false;
				}
			}
		};
		
		/**
		 * 
		 * @param xPath
		 * @returns
		 */
		GenericController.prototype.getCurrentBindingFor = function(xPath) {
			var $scope = this.getAngularScope();
			var parts = xPath.substring(1).split("/");
			var lastPart = parts[parts.length - 1];

			var currentBindings = $scope[this.BINDING_PREFIX];
			return currentBindings[lastPart];
		}		
		
		/**
		 * 
		 * @param xPath
		 */
		GenericController.prototype.deleteDocument = function(xPath) {
			var self = this;

			var $scope = this.getAngularScope();
			var parts = xPath.substring(1).split("/");
			
			var currentBindings = $scope[this.BINDING_PREFIX];
			var lastPart = parts[parts.length - 1];
			
			if (currentBindings[lastPart] && currentBindings[lastPart].docId) {
				currentBindings[lastPart].showDocMenu = false;
				
				//	Document is set
				var transferData = {};
				transferData[lastPart] = currentBindings[lastPart];
				transferData[lastPart].deleteDocument = true;
				
				this.interaction.saveData(lastPart, transferData, {success: function(retData) {
					currentBindings[lastPart].docId = null;
					currentBindings[lastPart].docName = null;
					currentBindings[lastPart].docIcon = "../../plugins/views-common/images/icons/page_white_error.png";
				}, failure: function() {
					success = false;
					alert(self.i18nLabelProvider.getLabel("panel.save.error", "Failure to save data"));
				}});
			}else{
				// TODO: Document is not set - this should never happen 
			}
		}
		
		/**
		 * 
		 * @param xPath
		 * @param readOnly
		 * @returns {String}
		 */
		GenericController.prototype.getDocumentLinkClass = function(xPath, readOnly){
			if(this.isDocumentLinkDisabled(xPath, readOnly)){
				return "disabled";
			}
			return "";
		}
		
		/**
		 * 
		 * @param xPath
		 * @param readOnly
		 * @returns {Boolean}
		 */
		GenericController.prototype.isDocumentLinkDisabled = function(xPath, readOnly){
			var $scope = this.getAngularScope();
			var parts = xPath.substring(1).split("/");
			
			var currentBindings = $scope[this.BINDING_PREFIX];
			var lastPart = parts[parts.length - 1];
			
			if(readOnly){
				if(!currentBindings[lastPart].docId){
					return true;	
				}
			}
			return false;
		}
		/*
		 * 
		 */
		GenericController.prototype.getPath = function(xPath) {
			var parts = xPath.substring(1).split("/");
			var found;
			var currentPath = this.dataMappings;
			for(var i = 0; i < parts.length; i++) {
				found = false;
				for(var j in currentPath) {
					if (parts[i] == currentPath[j].id) {
						found = true;
						currentPath = currentPath[j];
						break;
					} 
				}
				if (found) {
					if (i < parts.length - 1) { // Not Last Part
						currentPath = currentPath.children;
					}
				} else {
					return null;
				}
			}
			return currentPath;
		}

		/*
		 * 
		 */
		GenericController.prototype.getBinding = function(path, data, ignoreXPath, upToLastLevel) {
			var ret;

			var xPath = path.fullXPath;
			if(ignoreXPath && xPath.indexOf(ignoreXPath) == 0) {
				xPath = xPath.substring(ignoreXPath.length);
			}

			var parts = xPath.substring(1).split("/");
			var lastPart = parts[parts.length - 1];
			var currentBinding = data;
			for(var i = 0; i < parts.length - 1; i++) {
				currentBinding = currentBinding[parts[i]];
				if (currentBinding == undefined) {
					break;
				}
			}

			if (upToLastLevel) {
				if (currentBinding) {
					ret = currentBinding[lastPart];
				}
			} else {
				ret = {};
				if (currentBinding) {
					ret.binding = currentBinding;
					ret.lastPart = lastPart;
				}
			}

			return ret;
		}

		/*
		 * 
		 */
		GenericController.prototype.isReadonly = function(path) {
			return path.properties["InputPreferences_readonly"] == "true" || path.readonly;
		}

		/**
		 * 
		 */
		GenericController.prototype.completeActivity = function() {
			this.closeActivityPanel("complete");
		};

		/**
		 * 
		 */
		GenericController.prototype.suspendActivity = function(save) {
			this.closeActivityPanel(save ? "suspendAndSave" : "suspend");
		};

		/**
		 * 
		 */
		GenericController.prototype.abortActivity = function() {
			this.closeActivityPanel("abort");
		};

		/**
		 * 
		 */
		GenericController.prototype.qaPassActivity = function() {
			this.closeActivityPanel("qaPass");
		};

		/**
		 * 
		 */
		GenericController.prototype.qaFailActivity = function() {
			this.closeActivityPanel("qaFail");
		};

		/*
		 * 
		 */
		GenericController.prototype.closeActivityPanel = function(command) {
			this.log("Closing Activity Panel for commnand " + command);
			if (command == "suspendAndSave" || command == "complete") {
				if (!this.saveData()) {
					this.log("Declained Close Activity Panel for command " + command);
					return;
				}
			}

			this.interaction.closeActivityPanel(command);
		};

		/*
		 * 
		 */
		GenericController.prototype.runInAngularContext = function(func) {
			var scope = this.getAngularScope();
			scope.$apply(func);
		};

		/*
		 * 
		 */
		GenericController.prototype.getAngularScope = function() {
			return angular.element(document).scope();
		};

		/*
		 * 
		 */
		GenericController.prototype.log = function(msg) {
			if (console) {
				console.log(msg);
			}
		}		
	};
}
