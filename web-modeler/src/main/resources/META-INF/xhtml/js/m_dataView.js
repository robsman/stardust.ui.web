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
					var data = m_model.findData(fullId);

					m_utils.debug("===>  Data");
					m_utils.debug(data);

					view = new DataView();
					// TODO Unregister!
					// In Initializer?
					i18primitivedataproperties();
					m_commandsController.registerCommandHandler(view);

					view.initialize(data);

				}
			};


			function i18primitivedataproperties() {

				$("label[for='guidOutput']")
				.text(
						m_i18nUtils
								.getProperty("modeler.element.properties.commonProperties.uuid"));

				$("label[for='idOutput']")
				.text(
						m_i18nUtils
								.getProperty("modeler.element.properties.commonProperties.id"));

				jQuery("#dataName")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.createPrimitiveData.dataName"));
				jQuery("#description")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));
				jQuery("#publicVisibility")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.publicVisibility"));
				jQuery("#dataType")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.dataType"));
				jQuery("#primitiveType")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.primitiveType"));
				jQuery("[for='structuredDataTypeSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.structuredType"));
				jQuery("#dataStructure")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.dataStructure"));
				jQuery("#documentType")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.createPrimitiveData.dataTypeProperties.documentType"));
				jQuery("#defaultValue")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.createPrimitiveData.dataTypeProperties.defaultValue"));
				jQuery("#defaultValue1")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.createPrimitiveData.dataTypeProperties.defaultValue"));


				jQuery("#doubleInputTextError").text(
						m_i18nUtils.getProperty("modeler.element.properties.commonProperties.primitiveType.error.number"));
				jQuery("#intInputTextError").text(
						m_i18nUtils.getProperty("modeler.element.properties.commonProperties.primitiveType.error.number"));
				jQuery("#longInputTextError").text(
						m_i18nUtils.getProperty("modeler.element.properties.commonProperties.primitiveType.error.number"));
				jQuery("#TimestampInputTextError").text(
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

				/**
				 *
				 */
				DataView.prototype.initialize = function(data) {
					this.id = "dataView";
					this.publicVisibilityCheckbox = jQuery("#publicVisibilityCheckbox");

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
					this.timestampInputText = jQuery("#TimestampInputText");
					this.timestampInputText.datepicker({dateFormat: 'dd.mm.yy'});
					this.timestampInputText.change({"view" : this}, timestampChangeHandler);

					this.initializeModelElementView(data);
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
					this.initializeDataType(this.data,
							this.data.attributes["carnot:engine:defaultValue"]);

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

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
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
				 *
				 */
				DataView.prototype.initializeDataType = function(data,
						defaultValue) {
					if (data.dataType == m_constants.PRIMITIVE_DATA_TYPE) {
						var primitiveDataTypeSelect = jQuery("#primitiveDataTypeSelect");

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
							} else {
								$scope.defaultValue = defaultValue;
								if ($scope.dataType == 'boolean') {
									$scope.defaultValue = $scope.defaultValue == "true" ? true : false;
								}

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
						});
					} else {
						m_angularContextUtils.runInAngularContext(function($scope) {
							$scope.dataType = null;
						});
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
							var dateFomat = jQuery.datepicker.formatDate('yy/mm/dd', dtObj) + ' 00:00:00:000';
							view.submitModelElementAttributeChange("carnot:engine:defaultValue", dateFomat);
							$scope.timestampInputTextError = false;
						} catch(e){
							// Parse Error
							$scope.timestampInputTextError = true;
						}
					});
				}

				/**
				 *
				 */
				DataView.prototype.submitDataChanges = function(dataChanges) {
					this.initializeDataType(dataChanges);
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