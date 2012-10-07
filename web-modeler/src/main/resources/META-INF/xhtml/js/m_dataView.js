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
				"m_commandsController", "m_dialog", "m_modelElementView",
				"m_model", "m_dataTypeSelector" ],
		function(m_utils, m_constants, m_extensionManager, m_command,
				m_commandsController, m_dialog, m_modelElementView, m_model,
				m_dataTypeSelector) {
			var view;

			return {
				initialize : function(fullId) {
					var data = m_model.findData(fullId);

					m_utils.debug("===>  Data");
					m_utils.debug(data);

					view = new DataView();
					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(data);
				}
			};

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
					this.registerInputForModelElementAttributeChangeSubmission(
							this.primitiveDefaultCheckboxInput,
							"carnot:engine:defaultValue");

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
				DataView.prototype.submitDataChanges = function(dataChanges) {
					this.initializeDataType(dataChanges);
					this.submitChanges(dataChanges);
				};
			}
		});