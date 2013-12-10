/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_extensionManager",
				"bpm-modeler/js/m_command",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_dialog", "bpm-modeler/js/m_modelElementView",
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_dataTypeSelector",
				"bpm-modeler/js/m_i18nUtils",
				"bpm-modeler/js/m_jsfViewManager",
				"bpm-modeler/js/m_elementConfiguration",
				"bpm-modeler/js/m_angularContextUtils" ],

		function(m_utils, m_constants, m_extensionManager, m_command,
				m_commandsController, m_dialog, m_modelElementView, m_model,
				m_dataTypeSelector, m_i18nUtils, m_jsfViewManager, m_elementConfiguration,
				m_angularContextUtils) {
			var view;

			return {
				initialize : function(fullId) {
					m_utils.initializeWaitCursor(m_utils.jQuerySelect("html"));
					m_utils.showWaitCursor();

					var data = m_model.findData(fullId);

					m_utils.debug("===>  Data");
					m_utils.debug(data);

					view = new DataView();
					// TODO Unregister!
					// In Initializer?
					i18primitivedataproperties();
					m_commandsController.registerCommandHandler(view);

					view.initialize(data);
					m_utils.hideWaitCursor();
				}
			};


			function i18primitivedataproperties() {

				m_utils.jQuerySelect("label[for='guidOutput']")
				.text(
						m_i18nUtils
								.getProperty("modeler.element.properties.commonProperties.uuid"));

				m_utils.jQuerySelect("label[for='idOutput']")
				.text(
						m_i18nUtils
								.getProperty("modeler.element.properties.commonProperties.id"));

				m_utils.jQuerySelect("#dataName")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.createPrimitiveData.dataName"));
				m_utils.jQuerySelect("#description")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));
				m_utils.jQuerySelect("#publicVisibility")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.publicVisibility"));
				m_utils.jQuerySelect("#dataType")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.dataType"));
				m_utils.jQuerySelect("#primitiveType")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.primitiveType"));
				m_utils.jQuerySelect("[for='structuredDataTypeSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.structuredType"));
				m_utils.jQuerySelect("#dataStructure")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.dataStructure"));
				m_utils.jQuerySelect("#documentType")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.createPrimitiveData.dataTypeProperties.documentType"));
				m_utils.jQuerySelect("#defaultValue")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.createPrimitiveData.dataTypeProperties.defaultValue"));
				m_utils.jQuerySelect("#defaultValue1")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.createPrimitiveData.dataTypeProperties.defaultValue"));


				m_utils.jQuerySelect("#doubleInputTextError").text(
						m_i18nUtils.getProperty("modeler.element.properties.commonProperties.primitiveType.error.number"));
				m_utils.jQuerySelect("#intInputTextError").text(
						m_i18nUtils.getProperty("modeler.element.properties.commonProperties.primitiveType.error.number"));
				m_utils.jQuerySelect("#longInputTextError").text(
						m_i18nUtils.getProperty("modeler.element.properties.commonProperties.primitiveType.error.number"));
				m_utils.jQuerySelect("#TimestampInputTextError").text(
						m_i18nUtils.getProperty("modeler.element.properties.commonProperties.primitiveType.error.timestamp"));
			}

			/**
			 *
			 */
			function DataView() {
				var view = m_modelElementView.create();
				var viewManager = m_jsfViewManager.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(DataView.prototype, view);
				var currentPrimitiveType =null;
				/**
				 *
				 */
				DataView.prototype.initialize = function(data) {
					this.id = "dataView";
					this.view = m_utils.jQuerySelect("#" + this.id);
					this.publicVisibilityCheckbox = m_utils.jQuerySelect("#publicVisibilityCheckbox");

					this.dataTypeSelector = m_dataTypeSelector.create({
						scope : "dataView",
						submitHandler : this,
						supportsOtherData : true,
						supportsDocumentTypes : true
					});

					this.publicVisibilityCheckbox
							.change(
									{
										"view" : this
									},
									function(event) {
										var view = event.data.view;

										if (!view.validate()) {
											return;
										}

										if (view.modelElement.attributes["carnot:engine:visibility"]
												&& view.modelElement.attributes["carnot:engine:visibility"] != "Public") {
											view
													.submitChanges({
														attributes : {
															"carnot:engine:visibility" : "Public"
														}
													});
										} else {
											view
													.submitChanges({
														attributes : {
															"carnot:engine:visibility" : "Private"
														}
													});
										}
									});

					// Timestamp handling
					this.timestampInputText = m_utils.jQuerySelect("#TimestampInputText");
					this.timestampInputText.get(0).id = "TimestampInputText" + Math.floor((Math.random()*10000) + 1);
					this.timestampInputText.datepicker({dateFormat: 'dd.mm.yy'});
					this.timestampInputText.change({"view" : this}, timestampChangeHandler);
					
					var primitiveDataTypeSelect = m_utils.jQuerySelect("#primitiveDataTypeSelect");
					this.currentPrimitiveType = null!=primitiveDataTypeSelect ? primitiveDataTypeSelect.val():null;
					if(null!=currentPrimitiveType)
					this.updateDefaultValueForEnum(currentPrimitiveType);
					
					this.enumInputSelect = m_utils.jQuerySelect("#enumInputSelect");
					this.enumInputSelect.change({"view" : this}, enumSelectChangeHandler);
					
					this.initializeModelElementView(data);
					this.view.css("visibility", "visible");
				};

				/**
				 * Populate the Enums for selected Enum Type
				 */
				DataView.prototype.updateDefaultValueForEnum = function(
						primitiveDataTypeSelect) {
					if(this.dataTypeSelector.isEnumTypeDeclaration(primitiveDataTypeSelect)){
						this.populateEnumsForType(primitiveDataTypeSelect);
					}
				};
				/**
				 *
				 */
				DataView.prototype.setModelElement = function(data) {
					this.data = data;

					this.initializeModelElement(data);
					this.updateViewIcon();

					this.dataTypeSelector.setScopeModel(this.data.model);
					this.dataTypeSelector.setDataType(this.data);
					var showStructPrimitive = false;
					// For structured ENUM data, show primitive dropdown
					if (this.data.dataType == m_constants.STRUCTURED_DATA_TYPE
							&& this.dataTypeSelector.dataTypeSelect.val() == m_constants.PRIMITIVE_DATA_TYPE) {
						showStructPrimitive = true;
					}
					this.initializeDataType(this.data,
							this.data.attributes["carnot:engine:defaultValue"], showStructPrimitive);
				
					if (!this.data.attributes["carnot:engine:visibility"] ||
							"Public" == this.data.attributes["carnot:engine:visibility"]) {
						this.publicVisibilityCheckbox.attr("checked", true);
					} else {
						this.publicVisibilityCheckbox.attr("checked", false);
					}
				};

				/**
				 * TODO - handle unsupported data types too.?
				 */
				DataView.prototype.updateViewIcon = function() {
					var dataViewIcon = m_elementConfiguration
							.getIconForElementType(this.data.dataType);
					if (dataViewIcon) {
						viewManager.updateView("dataView",
								m_constants.VIEW_ICON_PARAM_KEY + "="
										+ dataViewIcon, this.data.uuid);
					}
				};

				/**
				 *
				 */
				DataView.prototype.toString = function() {
					return "Lightdust.DataView";
				};

				/**
				 *
				 */
				DataView.prototype.validate = function() {
					this.clearErrorMessages();

					this.nameInput.removeClass("error");

					if (m_utils.isEmptyString(this.nameInput.val())) {
						this.errorMessages.push("Data name must not be empty.");
						this.nameInput.addClass("error");
					}

					if (this.errorMessages.length > 0) {
						this.showErrorMessages();

						return false;
					}

					return true;
				};

				
				/**
				 * Read the model and populate typeDeclaration facets
				 */
				DataView.prototype.populateEnumsForType = function(
						typeDeclaration) {
					this.enumInputSelect.empty();
					var typeDeclarationObj = m_model
							.findTypeDeclaration(typeDeclaration);
					for ( var i in typeDeclarationObj.getTypeDeclaration().facets) {
						if (typeDeclarationObj.getTypeDeclaration().facets[i].classifier=='enumeration') {
							this.enumInputSelect.append("<option value='"
									+ typeDeclarationObj.getTypeDeclaration().facets[i].name + "'>"
									+ typeDeclarationObj.getTypeDeclaration().facets[i].name
									+ "</option>");
						}
					}
				};
				/**
				 * 
				 */
				DataView.prototype.initializeDataType = function(data,
						defaultValue, structEnum) {
					if (data.dataType == m_constants.PRIMITIVE_DATA_TYPE || structEnum) {
						var primitiveDataTypeSelect = m_utils.jQuerySelect("#primitiveDataTypeSelect");
						if(null == this.currentPrimitiveType){
							this.updateDefaultValueForEnum(primitiveDataTypeSelect.val());
						}else if(null!=this.currentPrimitiveType && !this.currentPrimitiveType.match(primitiveDataTypeSelect.val())){
							this.updateDefaultValueForEnum(primitiveDataTypeSelect.val());
						}
						this.currentPrimitiveType = primitiveDataTypeSelect.val();
						var self = this;
						m_angularContextUtils.runInAngularContext(function($scope) {
							$scope.dataType = primitiveDataTypeSelect.val();

							if (primitiveDataTypeSelect.val() == 'Timestamp') {
								var dateValue = defaultValue;
								if (defaultValue.indexOf(" ") > -1) {
									dateValue = defaultValue.substring(0, defaultValue.indexOf(" "));
								}

								try {
									var dateObj = jQuery.datepicker.parseDate("yy/mm/dd", dateValue);
									var dateFormat = jQuery.datepicker.formatDate('dd.mm.yy', dateObj);
									self.timestampInputText.val(dateFormat);
									$scope.timestampInputTextError = false;
								} catch(e){
									// Date parsing error.
									$scope.timestampInputTextError = true;
									self.timestampInputText.val(dateValue);
								}
							}else {
								if(self.dataTypeSelector.isEnumTypeDeclaration(primitiveDataTypeSelect.val())){
									if(null!=defaultValue){
										self.enumInputSelect.val(defaultValue);	
									}
									$scope.enumDataType = true;
									$scope.structEnum = structEnum;
									return;
								}
								$scope.defaultValue = defaultValue;
								if ($scope.dataType == 'boolean') {
									$scope.defaultValue = $scope.defaultValue == "true" ? true : false;
								}
									$scope.enumDataType = false;
								$scope.inputId = $scope.dataType + 'InputText';

								// Somehow initializeDataType() gets called again and again! hence the check
								if (!$scope.watchRegistered) {
									$scope.$watch('defaultValue', function(newValue, oldValue) {
										// Seems that due to issue in Angular this condition is required - $scope.form.<id>.$valid
										if (newValue !== oldValue && $scope.form[$scope.inputId].$valid) {
											if ($scope.dataType == 'boolean') {
												newValue = newValue ? "true" : "false";
											}
											self.submitModelElementAttributeChange("carnot:engine:defaultValue", newValue);
										}
									});
									$scope.watchRegistered = true;
								}
							}
						}, m_utils.jQuerySelect("#dataTypeTab").get(0));
					} else {
						m_angularContextUtils.runInAngularContext(function($scope) {
							$scope.dataType = null;
						}, m_utils.jQuerySelect("#dataTypeTab").get(0));
					}
				};

				/*
				 * Handler function only applies when Data type is Timestamp
				 */
				function timestampChangeHandler(event) {
					var view = event.data.view;
					m_angularContextUtils.runInAngularContext(function($scope) {
						try {
							var dateValue = view.timestampInputText.val();
							var dtObj = jQuery.datepicker.parseDate('dd.mm.yy', dateValue);
							var dateFomat = jQuery.datepicker.formatDate('yy/mm/dd', dtObj);
							if (!m_utils.isEmptyString(dateFomat)) {
								dateFomat += ' 00:00:00:000';
							}
							view.submitModelElementAttributeChange("carnot:engine:defaultValue", dateFomat);
							$scope.timestampInputTextError = false;
						} catch(e){
							// Parse Error
							$scope.timestampInputTextError = true;
						}
					}, m_utils.jQuerySelect("#dataTypeTab").get(0));
				}
				
				/**
				 * 
				 */
				function enumSelectChangeHandler(event) {
					var view = event.data.view;
					m_angularContextUtils.runInAngularContext(function($scope) {
							var dateValue = view.enumInputSelect.val();
							view.submitModelElementAttributeChange("carnot:engine:defaultValue", dateValue);
					}, m_utils.jQuerySelect("#dataTypeTab").get(0));
				}

				/**
				 *
				 */
				DataView.prototype.submitDataChanges = function(dataChanges) {
					this.submitChanges(dataChanges);
				};

				/**
				 * Overrides the postProcessCommand to update the structured
				 * type list, in case it's changed.
				 */
				DataView.prototype.postProcessCommand = function(
						command) {
					this.dataTypeSelector.setScopeModel(this.data.model);
					this.dataTypeSelector.setDataTypeSelectVal(this.data);
				};
			}
		});