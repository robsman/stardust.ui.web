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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_extensionManager", "bpm-modeler/js/m_command",
				"bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_dialog", "bpm-modeler/js/m_basicPropertiesPage",
				"bpm-modeler/js/m_dataTypeSelector", "bpm-modeler/js/m_model", "bpm-modeler/js/m_i18nUtils", "angularjs"],
		function(m_utils, m_constants, m_extensionManager, m_command,
				m_commandsController, m_dialog, m_basicPropertiesPage,
				m_dataTypeSelector, m_model, m_i18nUtils) {
			return {
				create : function(propertiesPanel) {
					var page = new DataBasicPropertiesPage(propertiesPanel);

					page.initialize();

					return page;
				}
			};

			/**
			 *
			 */
			function DataBasicPropertiesPage(propertiesPanel) {
				var propertiesPage = m_basicPropertiesPage
						.create(propertiesPanel);

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(DataBasicPropertiesPage.prototype,
						propertiesPage);

				/**
				 *
				 */
				DataBasicPropertiesPage.prototype.initialize = function() {
					this.initializeBasicPropertiesPage();

					this.dataTypeSelector = m_dataTypeSelector.create({
						scope : "dataPropertiesPanel",
						submitHandler : this,
						supportsOtherData : true,
						supportsDocumentTypes : true
					});
					this.publicVisibilityCheckbox = this
							.mapInputId("publicVisibilityCheckbox");

					this.publicVisibilityCheckbox
							.change(
									{
										"page" : this
									},
									function(event) {
										var page = event.data.page;

										if (!page.validate()) {
											return;
										}

										if (page.publicVisibilityCheckbox
												.is(":checked")
												&& page.getModelElement().attributes["carnot:engine:visibility"] != "Public") {
											page
													.submitChanges({
														attributes : {
															"carnot:engine:visibility" : "Public"
														}
													});
										} else if (!page.publicVisibilityCheckbox
												.is(":checked")
												&& page.getModelElement().attributes["carnot:engine:visibility"] == "Public") {
											page
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

					// I18N
					jQuery("#doubleInputTextError").text(
							m_i18nUtils.getProperty("modeler.element.properties.commonProperties.primitiveType.error.number"));
					jQuery("#intInputTextError").text(
							m_i18nUtils.getProperty("modeler.element.properties.commonProperties.primitiveType.error.number"));
					jQuery("#longInputTextError").text(
							m_i18nUtils.getProperty("modeler.element.properties.commonProperties.primitiveType.error.number"));
					jQuery("#TimestampInputTextError").text(
							m_i18nUtils.getProperty("modeler.element.properties.commonProperties.primitiveType.error.timestamp"));
				};

				/**
				 *
				 */
				DataBasicPropertiesPage.prototype.getModelElement = function() {
					if (this.propertiesPanel.element.modelElement
							&& this.propertiesPanel.element.modelElement.externalReference) {
						return m_model.findData(this.propertiesPanel.element.modelElement.dataFullId);
					}

					return this.propertiesPanel.element.modelElement;
				};


				/**
				 *
				 */
				DataBasicPropertiesPage.prototype.getModel = function() {
					if (this.propertiesPanel.element.modelElement
							&& this.propertiesPanel.element.modelElement.externalReference) {
						return m_model.findModel(m_model.stripModelId(this.propertiesPanel.element.modelElement.dataFullId));
					}

					return this.propertiesPanel.getModel();
				};

				/**
				 *
				 */
				DataBasicPropertiesPage.prototype.setElement = function() {
					this.setModelElement();

					m_utils.debug("===> Data");
					m_utils.debug(this.propertiesPanel.element);
					m_utils.debug(this.getModelElement());

					this.dataTypeSelector
							.setScopeModel(this.getModelElement().model);
					this.dataTypeSelector.setDataType(this.getModelElement());
					this
							.initializeDataType(
									this.getModelElement(),
									this.getModelElement().attributes["carnot:engine:defaultValue"]);

					if ("Public" == this.getModelElement().attributes["carnot:engine:visibility"]) {
						this.publicVisibilityCheckbox.attr("checked", true);
					} else {
						this.publicVisibilityCheckbox.attr("checked", false);
					}
				};

				/**
				 *
				 */
				DataBasicPropertiesPage.prototype.validate = function() {
					if (this.validateModelElement()) {
						return true;
					}

					return false;
				};

				/**
				 *
				 */
				DataBasicPropertiesPage.prototype.submitDataChanges = function(
						dataChanges) {
					// These are changes on the data, not the symbol

					this.initializeDataType(dataChanges);
					this.submitChanges(dataChanges);
				};

				/**
				 *
				 */
				DataBasicPropertiesPage.prototype.initializeDataType = function(
						data, defaultValue) {
					if (data.dataType == m_constants.PRIMITIVE_DATA_TYPE) {
						var primitiveDataTypeSelect = this.dataTypeSelector.primitiveDataTypeSelect;

						var self = this;
						var scope = angular.element(document.body).scope();
						scope.$apply(function($scope) {
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
						var scope = angular.element(document.body).scope();
						scope.$apply(function($scope) {
							$scope.dataType = null;
						});
					}
				};

				/*
				 * Handler function only applies when Data type is Timestamp
				 */
				function timestampChangeHandler(event) {
					var view = event.data.view;
					var scope = angular.element(document.body).scope();
					scope.$apply(function($scope) {
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
				};

				/*
				 *
				 */
				DataBasicPropertiesPage.prototype.submitModelElementAttributeChange = function(attribute, value) {
					console.log('Can Submit' + value);
					if (this.getModelElement().attributes[attribute] != value) {
						var modelElement = {
							attributes : {}
						};
						modelElement.attributes[attribute] = value;
						this.submitChanges(modelElement);
					}
				};

				/**
				 *
				 */
				DataBasicPropertiesPage.prototype.submitChanges = function(
						changes) {
					m_commandsController.submitCommand(m_command
							.createUpdateModelElementWithUUIDCommand(this
									.getModel().id,
									this.getModelElement().uuid, changes));
				};
			}
		});