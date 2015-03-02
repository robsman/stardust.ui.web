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
				"bpm-modeler/js/m_dataTypeSelector", "bpm-modeler/js/m_model", "bpm-modeler/js/m_i18nUtils",
				"bpm-modeler/js/m_angularContextUtils", "bpm-modeler/js/m_messageDisplay"],
		function(m_utils, m_constants, m_extensionManager, m_command,
				m_commandsController, m_dialog, m_basicPropertiesPage,
				m_dataTypeSelector, m_model, m_i18nUtils, m_angularContextUtils, m_messageDisplay) {
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
				this.currentPrimitiveType = null;

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
					// ** like query is used in the jquery selector as the id gets updated and
					// properties panels are re-initialized on tab change. **
					this.timestampInputText = m_utils.jQuerySelect("[id^='TimestampInputText']");
					this.timestampInputText.get(0).id = "TimestampInputText" + Math.floor((Math.random()*10000) + 1);
					this.timestampInputText.datepicker({dateFormat: 'dd.mm.yy'});
					this.timestampInputText.change({"view" : this}, timestampChangeHandler);

					this.enumInputSelect = m_utils.jQuerySelect("#enumInputSelect");
					this.enumInputSelect.change({"view" : this}, enumSelectChangeHandler);
					
					this.volatileDataInput = m_utils.jQuerySelect("#volatileDataInput");
          this.volatileDataInput.change({"view" : this}, volatileDataChangeHandler);
          
          m_utils.jQuerySelect("label[for='volatileDataInput']")
          .text(m_i18nUtils.getProperty("modeler.element.properties.commonProperties.volatileData"));
          
					
					// I18N
					m_utils.jQuerySelect("#doubleInputTextError").text(
							m_i18nUtils.getProperty("modeler.element.properties.commonProperties.primitiveType.error.number"));
					m_utils.jQuerySelect("#intInputTextError").text(
							m_i18nUtils.getProperty("modeler.element.properties.commonProperties.primitiveType.error.number"));
					m_utils.jQuerySelect("#longInputTextError").text(
							m_i18nUtils.getProperty("modeler.element.properties.commonProperties.primitiveType.error.number"));
					m_utils.jQuerySelect("#TimestampInputTextError").text(
							m_i18nUtils.getProperty("modeler.element.properties.commonProperties.primitiveType.error.timestamp"));
				};

				/**
				 * Read the model and populate typeDeclaration facets
				 */
				DataBasicPropertiesPage.prototype.populateEnumsForType = function(
						typeDeclaration) {
					this.enumInputSelect.empty();
					var typeDeclarationObj = m_model
							.findTypeDeclaration(typeDeclaration);
					for ( var i in typeDeclarationObj.getTypeDeclaration().facets) {
						if (typeDeclarationObj.getTypeDeclaration().facets[i].classifier
								.match('enumeration')) {
							this.enumInputSelect.append("<option value='"
									+ typeDeclarationObj.getTypeDeclaration().facets[i].name + "'>"
									+ typeDeclarationObj.getTypeDeclaration().facets[i].name
									+ "</option>");
						}
					}
				};
				
				/**
				 * Populate the Enums for selected Enum Type
				 */
				DataBasicPropertiesPage.prototype.updateDefaultValueForEnum = function(
						primitiveDataTypeSelect) {
						if(this.dataTypeSelector.isEnumTypeDeclaration(primitiveDataTypeSelect)){
							this.populateEnumsForType(primitiveDataTypeSelect);
						}
				};
				
				/**
				 *
				 */
				DataBasicPropertiesPage.prototype.getModelElement = function() {
					if (this.propertiesPanel.element.modelElement
							&& this.propertiesPanel.element.modelElement.externalReference) {
						if (this.propertiesPanel.element.modelElement.dataFullId
								&& m_model.findData(this.propertiesPanel.element.modelElement.dataFullId)) {
							return m_model.findData(this.propertiesPanel.element.modelElement.dataFullId);
						} else {
							return;
						}
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
					m_messageDisplay.clear();
					if (!this.getModelElement()) {
						m_messageDisplay
								.showMessage(m_i18nUtils
										.getProperty("modeler.propertyPanel.data.elementNotFound"));
					}

					this.setModelElement();

					m_utils.debug("===> Data");
					m_utils.debug(this.propertiesPanel.element);
					m_utils.debug(this.getModelElement());

					// Set scope model to this model and not to the model
					// to which the model element belongs as it may belong to
					// another model and then the "this model" / "other model"
					// semantics in the properties page goes for a toss.
					this.dataTypeSelector.setScopeModel(this.propertiesPanel.getModel());

					this.dataTypeSelector.setDataType(this.getModelElement());
					var showStructPrimitive = false;
					// For structured ENUM data, show primitive dropdown
					if (this.getModelElement().dataType == m_constants.STRUCTURED_DATA_TYPE
							&& this.dataTypeSelector.dataTypeSelect.val() == m_constants.PRIMITIVE_DATA_TYPE) {
						showStructPrimitive = true;
					}
					this
							.initializeDataType(
									this.getModelElement(),
									this.getModelElement().attributes["carnot:engine:defaultValue"], showStructPrimitive);

					if (!this.getModelElement().attributes["carnot:engine:visibility"]
							|| "Public" == this.getModelElement().attributes["carnot:engine:visibility"]) {
						this.publicVisibilityCheckbox.attr("checked", true);
					} else {
						this.publicVisibilityCheckbox.attr("checked", false);
					}
					
					var volatileVal = this.getModelElement().attributes["carnot:engine:volatile"];
					if (volatileVal == true || volatileVal == "true") {
            this.volatileDataInput.attr("checked", true);
          } else {
            this.volatileDataInput.attr("checked", false);
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

					this.submitChanges(dataChanges);
				};

				/**
				 *
				 */
				DataBasicPropertiesPage.prototype.initializeDataType = function(
						data, defaultValue, structEnum) {
					if (data.dataType == m_constants.PRIMITIVE_DATA_TYPE || structEnum) {
						var primitiveDataTypeSelect = this.dataTypeSelector.primitiveDataTypeSelect;
						if(null == this.currentPrimitiveType){
							this.updateDefaultValueForEnum(primitiveDataTypeSelect.val());
						}else if(null!=this.currentPrimitiveType && !this.currentPrimitiveType.match(primitiveDataTypeSelect.val())){
							this.updateDefaultValueForEnum(primitiveDataTypeSelect.val());
						}
						this.currentPrimitiveType = primitiveDataTypeSelect.val();
						var self = this;
						m_angularContextUtils.runInAngularContext(function($scope) {
							$scope.dataType = primitiveDataTypeSelect.val();
							$scope.enumDataType = false;
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
									if(defaultValue!=null){
										self.enumInputSelect.val(defaultValue);
									}else{
										self.submitModelElementAttributeChange("carnot:engine:defaultValue", self.enumInputSelect.val());											
									}
									$scope.enumDataType = true;
									if(structEnum){
										self.enumInputSelect.attr("disabled","disabled");
									}else{
										self.enumInputSelect.removeAttr("disabled");
									}
									return;
									}
								$scope.defaultValue = defaultValue;
								$scope.enumDataType = false;
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
						}, m_utils.jQuerySelect("#datatableid").get(0));
					} else {
						m_angularContextUtils.runInAngularContext(function($scope) {
							$scope.dataType = null;
						}, m_utils.jQuerySelect("#datatableid").get(0));
					}

					if (data.isReadonly()) {
						m_utils.markControlsReadonly('modelerPropertiesPanelWrapper');
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
					}, m_utils.jQuerySelect("#datatableid").get(0));
				};
				
				/**
				 * 
				 */
				function enumSelectChangeHandler(event) {
					var view = event.data.view;
					m_angularContextUtils.runInAngularContext(function($scope) {
							var dateValue = view.enumInputSelect.val();
							view.submitModelElementAttributeChange("carnot:engine:defaultValue", dateValue);
					}, m_utils.jQuerySelect("#dataTypeTab").get(0));
				};

			  /**
         * 
         */
				function volatileDataChangeHandler(event) {
          var view = event.data.view;
          m_angularContextUtils.runInAngularContext(function($scope) {
            var value = false; 
              if(view.volatileDataInput.is(":checked")) {
                value = true;  
              }
              view.submitModelElementAttributeChange("carnot:engine:volatile", value);
          }, m_utils.jQuerySelect("#dataTypeTab").get(0));
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