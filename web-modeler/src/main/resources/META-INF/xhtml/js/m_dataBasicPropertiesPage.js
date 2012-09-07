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
		[ "m_utils", "m_constants", "m_extensionManager", "m_command",
				"m_commandsController", "m_dialog", "m_basicPropertiesPage",
				"m_dataTypeSelector" ],
		function(m_utils, m_constants, m_extensionManager, m_command,
				m_commandsController, m_dialog, m_basicPropertiesPage,
				m_dataTypeSelector) {
			return {
				create : function(propertiesPanel) {
					var page = new DataBasicPropertiesPage(propertiesPanel);

					page.initialize();

					return page;
				}
			};

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
						supportsOtherData : true
					});
					this.publicVisibilityCheckbox = this
							.mapInputId("publicVisibilityCheckbox");
					this.primitiveDefaultTextInputRow = this
							.mapInputId("primitiveDefaultTextInputRow");
					this.primitiveDefaultTextInput = this
							.mapInputId("primitiveDefaultTextInput");
					this.primitiveDefaultCheckboxInputRow = this
							.mapInputId("primitiveDefaultCheckboxInputRow ");
					this.primitiveDefaultCheckboxInput = this
							.mapInputId("primitiveDefaultCheckboxInput");

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

					this.registerInputForModelElementAttributeChangeSubmission(
							this.primitiveDefaultTextInput,
							"carnot:engine:defaultValue");
					this.registerInputForModelElementAttributeChangeSubmission(
							this.primitiveDefaultCheckboxInput,
							"carnot:engine:defaultValue");
				};

				/**
				 * 
				 */
				DataBasicPropertiesPage.prototype.getModelElement = function() {
					return this.propertiesPanel.data;
				};

				/**
				 * 
				 */
				DataBasicPropertiesPage.prototype.assembleChangedObjectFromProperty = function(
						property, value) {
					var element = {};

					element[property] = value;

					return element;
				};

				/**
				 * 
				 */
				DataBasicPropertiesPage.prototype.assembleChangedObjectFromAttribute = function(
						attribute, value) {
					var element = {
						attributes : {}
					};

					element.attributes[attribute] = value;

					return element;
				};

				/**
				 * 
				 */
				DataBasicPropertiesPage.prototype.getModelElement = function() {
					return this.propertiesPanel.data;
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
						if (data.primitiveDataType == m_constants.BOOLEAN_PRIMITIVE_DATA_TYPE) {
							m_dialog
									.makeInvisible(this.primitiveDefaultTextInputRow);
							m_dialog
									.makeVisible(this.primitiveDefaultCheckboxInputRow);

							if (defaultValue != null) {
								this.primitiveDefaultCheckboxInput
										.val(defaultValue);
							} else {
								this.primitiveDefaultCheckboxInput.val(null);
							}
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
				DataBasicPropertiesPage.prototype.submitChanges = function(
						changes) {
					m_commandsController.submitCommand(m_command
							.createUpdateModelElementWithUUIDCommand(this
									.getModel().id,
									this.getModelElement().uuid, changes));
				};
			}
		});