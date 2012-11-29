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
				"bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_dialog", "bpm-modeler/js/m_modelElementView",
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_dataTypeSelector","bpm-modeler/js/m_i18nUtils"],
		function(m_utils, m_constants, m_extensionManager, m_command,
				m_commandsController, m_dialog, m_modelElementView, m_model,
				m_dataTypeSelector, m_i18nUtils) {
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
										.getProperty("modeler.element.properties.commonProperties.dataType "));
				jQuery("#dataType1")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.dataType"));
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

			}

			/**
			 *
			 */
			function DataView() {
				var view = m_modelElementView.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(DataView.prototype, view);

				/**
				 *
				 */
				DataView.prototype.initialize = function(data) {
					this.id = "dataView";
					this.publicVisibilityCheckbox = jQuery("#publicVisibilityCheckbox");
					this.primitiveDefaultTextInputRow = jQuery("#primitiveDefaultTextInputRow");
					this.primitiveDefaultTextInput = jQuery("#primitiveDefaultTextInput");
					this.primitiveDefaultCheckboxInputRow = jQuery("#primitiveDefaultCheckboxInputRow ");
					this.primitiveDefaultCheckboxInput = jQuery("#primitiveDefaultCheckboxInput");
					this.primitiveDataTypeSelect = jQuery("#primitiveDataTypeSelect");
					var selectdata = null;
					selectdata = m_i18nUtils
							.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.string");
					this.primitiveDataTypeSelect
							.append("<option value=\"String\">" + selectdata
									+ "</option>");
					selectdata = m_i18nUtils
							.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.boolean");
					this.primitiveDataTypeSelect
							.append("<option value=\"boolean\">" + selectdata
									+ "</option>");
					selectdata = m_i18nUtils
							.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.int");
					this.primitiveDataTypeSelect
							.append("<option value=\"int\">" + selectdata
									+ "</option>");
					selectdata = m_i18nUtils
							.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.long");
					this.primitiveDataTypeSelect
							.append("<option value=\"long\">" + selectdata
									+ "</option>");
					selectdata = m_i18nUtils
							.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.double");
					this.primitiveDataTypeSelect
							.append("<option value=\"double\">" + selectdata
									+ "</option>");
					selectdata = m_i18nUtils
							.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.decimal");
					this.primitiveDataTypeSelect
							.append("<option value=\"Decimal\">" + selectdata
									+ "</option>");
					selectdata = m_i18nUtils
							.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.calender");
					this.primitiveDataTypeSelect
							.append("<option value=\"Calendar\">" + selectdata
									+ "</option>");


					this.dataTypeSelector = m_dataTypeSelector.create({
						scope : "dataView",
						submitHandler : this,
						supportsOtherData : true
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

										if (view.publicVisibilityCheckbox
												.is(":checked")
												&& view.data.attributes["carnot:engine:visibility"] != "Public") {
											view
													.submitChanges({
														attributes : {
															"carnot:engine:visibility" : "Public"
														}
													});
										} else if (!view.publicVisibilityCheckbox
												.is(":checked")
												&& view.data.attributes["carnot:engine:visibility"] == "Public") {
											view
													.submitChanges({
														attributes : {
															"carnot:engine:visibility" : "Private"
														}
													});
										}
									});
					this.registerInputForModelElementAttributeChangeSubmission(
							this.primitiveDefaultTextInput,
							"carnot:engine:defaultValue");

					// carnot:engine:defaultValue, in spite of being a checkbox (for boolean type)
					// is a string attribute
					// Hence not using the usual change listener for checkboxes
					this.primitiveDefaultCheckboxInput.change({
						"view" : this
					}, function(event) {
						var view = event.data.view;

						if (!view.validate()) {
							return;
						}

						if (view.primitiveDefaultCheckboxInput.is(":checked")) {
							view.submitChanges({
								attributes : {
									"carnot:engine:defaultValue" : "true"
								}
							});
						} else {
							view.submitChanges({
								attributes : {
									"carnot:engine:defaultValue" : "false"
								}
							});
						}
					});

					this.initializeModelElementView(data);
				};

				/**
				 *
				 */
				DataView.prototype.setModelElement = function(data) {
					this.data = data;

					this.initializeModelElement(data);

					this.dataTypeSelector.setScopeModel(this.data.model);
					this.dataTypeSelector.setDataType(this.data);
					this.initializeDataType(this.data,
							this.data.attributes["carnot:engine:defaultValue"]);

					if ("Public" == this.data.attributes["carnot:engine:visibility"]) {
						this.publicVisibilityCheckbox.attr("checked", true);
					} else {
						this.publicVisibilityCheckbox.attr("checked", false);
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
						if (data.primitiveDataType == m_constants.BOOLEAN_PRIMITIVE_DATA_TYPE) {
							m_dialog
									.makeInvisible(this.primitiveDefaultTextInputRow);
							m_dialog
									.makeVisible(this.primitiveDefaultCheckboxInputRow);
							this.primitiveDefaultCheckboxInput.attr("checked",
									(defaultValue == "true"));
						} else {
							m_dialog
									.makeVisible(this.primitiveDefaultTextInputRow);
							m_dialog
									.makeInvisible(this.primitiveDefaultCheckboxInputRow);

							if (defaultValue != null) {
								this.primitiveDefaultTextInput
										.val(defaultValue);
							} else {
								this.primitiveDefaultTextInput.val(null);
							}
						}
					} else {
						m_dialog
								.makeInvisible(this.primitiveDefaultTextInputRow);
						m_dialog
								.makeInvisible(this.primitiveDefaultCheckboxInputRow);
					}
				};

				/**
				 *
				 */
				DataView.prototype.submitDataChanges = function(dataChanges) {
					this.initializeDataType(dataChanges);
					this.submitChanges(dataChanges);
				};
			}
		});