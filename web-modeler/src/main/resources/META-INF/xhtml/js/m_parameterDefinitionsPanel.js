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
 * Utility functions for dialog programming.
 *
 * @author Marc.Gille
 */
define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_urlUtils",
				"bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_extensionManager", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_typeDeclaration", "bpm-modeler/js/m_dialog",
				"bpm-modeler/js/m_dataTypeSelector",
				"bpm-modeler/js/m_i18nUtils", "bpm-modeler/js/m_user" ],
		function(m_utils, m_urlUtils, m_constants, m_extensionManager, m_model,
				m_typeDeclaration, m_dialog, m_dataTypeSelector, m_i18nUtils, m_user) {
			return {
				create : function(options) {
					var panel = new ParameterDefinitionsPanel();

					panel.initialize(options);

					return panel;
				}
			};

			/**
			 * Options are scope submitHandler supportsOrdering
			 * supportsDataMappings supportsDescriptors
			 * supportsDataTypeSelection supportsDataPathes directionColumnWidth
			 * nameColumnWidth typeColumnWidth mappingColumnWidth tableWidth
			 */
			function ParameterDefinitionsPanel() {
				/**
				 *
				 */
				ParameterDefinitionsPanel.prototype.initialize = function(
						options) {
					this.options = options;

					if (this.options.scope == null) {
						this.options.scope = "";
						this.dataTypeSelectorScope = "parameterDefinitionTypeSelector";
					} else {
						this.options.scope = "#" + this.options.scope + " ";
						this.dataTypeSelectorScope = this.options.scope;
					}

					// TODO: Change width via CSS and classes here

					if (this.options.tableWidth == null) {
						this.options.tableWidth = "350px";
					}

					if (this.options.directionColumnWidth == null) {
						this.options.directionColumnWidth = "50px";
					}

					if (this.options.nameColumnWidth == null) {
						this.options.nameColumnWidth = "150px";
					}

					if (this.options.typeColumnWidth == null) {
						this.options.typeColumnWidth = "150px";
					}

					if (this.options.mappingColumnWidth == null) {
						this.options.mappingColumnWidth = "200px";
					}

					this.parameterDefinitions = [];
					this.currentParameterDefinition = null;
					this.selectedRowIndex = -1;
					this.parameterDefinitionsTable = m_utils.jQuerySelect(this.options.scope
							+ " #parameterDefinitionsTable");

					this.parameterDefinitionsTable.css("width",
							this.options.tableWidth);

					this.parameterDefinitionsTableBody = m_utils.jQuerySelect(this.options.scope
							+ " #parameterDefinitionsTable tbody");
					this.parameterDefinitionIdOutput = m_utils.jQuerySelect(this.options.scope
							+ " #parameterDefinitionIdOutput");
					this.parameterDefinitionIdOutputLabel = m_utils.jQuerySelect(this.options.scope
							+ " label[for='parameterDefinitionIdOutput']");
					this.parameterDefinitionNameInput = m_utils.jQuerySelect(this.options.scope
							+ " #parameterDefinitionNameInput");
					
					this.parameterDefinitionDirectionSelect = m_utils.jQuerySelect(this.options.scope
							+ " #parameterDefinitionDirectionSelect");
					this.parameterDefinitionDirectionSelect.empty();
					var direction = m_i18nUtils.getProperty("modeler.element.properties.commonProperties.in");
					this.parameterDefinitionDirectionSelect.append("<option value=\"IN\">" + direction + "</option>");
					if (options.supportsInOutDirection) {
						direction = m_i18nUtils.getProperty("modeler.element.properties.commonProperties.inout");
						this.parameterDefinitionDirectionSelect.append("<option value=\"INOUT\">" + direction + "</option>");
					}
					direction = m_i18nUtils.getProperty("modeler.element.properties.commonProperties.out");
					this.parameterDefinitionDirectionSelect.append("<option value=\"OUT\">" + direction + "</option>");
					
					
					this.addParameterDefinitionButton = m_utils.jQuerySelect(this.options.scope
							+ " #addParameterDefinitionButton");
					this.deleteParameterDefinitionButton = m_utils.jQuerySelect(this.options.scope
							+ " #deleteParameterDefinitionButton");

					this.addParameterDefinitionButton.attr("src", m_urlUtils
							.getContextName()
							+ "/plugins/bpm-modeler/images/icons/add.png");
					this.deleteParameterDefinitionButton.attr("src", m_urlUtils
							.getContextName()
							+ "/plugins/bpm-modeler/images/icons/delete.png");

					this.currentFocusInput = this.parameterDefinitionNameInput;

					if (this.options.supportsDataTypeSelection) {
						this.dataTypeSelector = m_dataTypeSelector
								.create({
									scope : this.dataTypeSelectorScope,
									submitHandler : this,
									supportsOtherData : (typeof this.options.supportsOtherData === "undefined") ? false
											: this.options.supportsOtherData,
									supportsDocumentTypes : (typeof this.options.supportsDocumentTypes === "undefined") ? true
											: this.options.supportsDocumentTypes,
									restrictToCurrentModel : (typeof this.options.restrictToCurrentModel === "undefined") ? false
											: this.options.restrictToCurrentModel,
									hideEnumerations : (typeof this.options.hideEnumerations === "undefined") ? false
											: this.options.hideEnumerations
								});
					}

					if (this.options.supportsOrdering) {
						this.moveParameterDefinitionUpButton = m_utils.jQuerySelect(this.options.scope
								+ " #moveParameterDefinitionUpButton");
						this.moveParameterDefinitionDownButton = m_utils.jQuerySelect(this.options.scope
								+ " #moveParameterDefinitionDownButton");
					}

					if (this.options.supportsDescriptors) {
						this.descriptorInput = m_utils.jQuerySelect(this.options.scope
								+ " #parameterDefinitionDescriptorInput");
						this.keyDescriptorInput = m_utils.jQuerySelect(this.options.scope
								+ " #parameterDefinitionKeyDescriptorInput");
					}

					if (this.options.supportsDataMappings) {
						this.parameterDefinitionDataSelect = m_utils.jQuerySelect(this.options.scope
								+ " #parameterDefinitionDataSelect");

						if (this.options.supportsDataPathes) {
							this.parameterDefinitionPathInput = m_utils.jQuerySelect(this.options.scope
									+ " #parameterDefinitionPathInput");
						}
					} else {
						m_utils.jQuerySelect(this.options.scope
								+ " #parameterDefinitionDataSelect").hide();
						m_utils.jQuerySelect(this.options.scope
								+ " label[for='parameterDefinitionDataSelect']").hide();
					}

					if (this.options.supportsDataTypeSelection) {
						this.dataTypeSelector.setPrimitiveDataType();
					}

					this.addParameterDefinitionButton.click({
						panel : this
					}, function(event) {
						event.data.panel.addParameterDefinition();
					});
					this.deleteParameterDefinitionButton.click({
						panel : this
					}, function(event) {
						event.data.panel.deleteParameterDefinition();
					});

					if (this.options.supportsOrdering) {
						this.moveParameterDefinitionUpButton.click({
							panel : this
						}, function(event) {
							event.data.panel.moveParameterDefinitionUp();
						});
						this.moveParameterDefinitionDownButton.click({
							panel : this
						}, function(event) {
							event.data.panel.moveParameterDefinitionDown();
						});
					}

					this.parameterDefinitionNameInput
							.change(
									{
										panel : this
									},
									function(event) {
										if (event.data.panel.currentParameterDefinition != null) {
											// Blank names are not allowed.
											if (jQuery
													.trim(event.data.panel.parameterDefinitionNameInput
															.val()) == "") {
												event.data.panel.parameterDefinitionNameInput
														.val(event.data.panel.currentParameterDefinition.name);
												return;
											}

											event.data.panel.currentParameterDefinition.name = event.data.panel.parameterDefinitionNameInput
													.val();
											event.data.panel.currentFocusInput = this.parameterDefinitionDirectionSelect;
											event.data.panel.submitChanges();
										}
									});
					this.parameterDefinitionDirectionSelect
							.change(
									{
										panel : this
									},
									function(event) {
										if (event.data.panel.currentParameterDefinition != null) {
											event.data.panel.currentParameterDefinition.direction = event.data.panel.parameterDefinitionDirectionSelect
													.val();

											// Reset descriptor and key-descriptor on direction change
											event.data.panel.currentParameterDefinition.descriptor = false;
											event.data.panel.currentParameterDefinition.keyDescriptor = false;

											// Switch back to standard focus
											// handling

											event.data.panel.currentFocusInput = null;

											event.data.panel.submitChanges();
										}
									});

					if (this.options.supportsDescriptors) {
						this.descriptorInput
								.change(
										{
											"panel" : this
										},
										function(event) {
											event.data.panel.currentParameterDefinition.descriptor = event.data.panel.descriptorInput
													.prop("checked");
											// Reset key-descriptor input on descriptor change.
											event.data.panel.currentParameterDefinition.keyDescriptor = false;

											if (event.data.panel.descriptorInput
													.prop("checked")) {
												event.data.panel.currentFocusInput = event.data.panel.keyDescriptorInput;
											} else {
												// Switch back to standard focus
												// handling

												event.data.panel.currentFocusInput = null;
											}

											event.data.panel.submitChanges();
										});
						this.keyDescriptorInput
								.change(
										{
											"panel" : this
										},
										function(event) {
											event.data.panel.currentParameterDefinition.keyDescriptor = event.data.panel.keyDescriptorInput
													.prop("checked");

											// Switch back to standard focus
											// handling

											event.data.panel.currentFocusInput = null;

											event.data.panel.submitChanges();
										});
					}

					if (this.options.supportsDataMappings) {
						this.parameterDefinitionDataSelect
								.change(
										{
											panel : this
										},
										function(event) {
											if (event.data.panel.currentParameterDefinition != null) {
												if (event.data.panel.parameterDefinitionDataSelect
														.val() == m_constants.TO_BE_DEFINED) {
													event.data.panel.currentParameterDefinition.dataFullId = null;
												} else {
													event.data.panel.currentParameterDefinition.dataFullId = event.data.panel.parameterDefinitionDataSelect
															.val();
												}

												// Switch back to standard focus
												// handling

												event.data.panel.currentFocusInput = null;

												event.data.panel
														.submitChanges();
											}
										});

						if (this.options.supportsDataPathes) {
							this.parameterDefinitionPathInput
									.change(
											{
												panel : this
											},
											function(event) {
												if (event.data.panel.currentParameterDefinition != null) {
													event.data.panel.currentParameterDefinition.dataPath = event.data.panel.parameterDefinitionPathInput
															.val();

													// Switch back to standard
													// focus handling

													event.data.panel.currentFocusInput = null;

													event.data.panel
															.submitChanges();
												}
											});
						}
					}

					if (this.options.readOnlyParameterList) {
						m_dialog
								.makeInvisible(this.deleteParameterDefinitionButton);
						m_dialog
								.makeInvisible(this.addParameterDefinitionButton);
					}

					if (this.options.hideDirectionSelection) {
						m_dialog
								.makeInvisible(m_utils.jQuerySelect(this.options.scope
										+ "label[for='parameterDefinitionDirectionSelect']"));
						m_dialog
								.makeInvisible(this.parameterDefinitionDirectionSelect);
					}
				};

				/**
				 *
				 */
				ParameterDefinitionsPanel.prototype.setParameterDefinitions = function(
						parameterDefinitions) {
					this.parameterDefinitions = parameterDefinitions;

					m_utils.debug("===> Parameter Definitions:")
					m_utils.debug(parameterDefinitions)

					this.initializeParameterDefinitionsTable();
					this.selectCurrentParameterDefinition();
					this.populateParameterDefinitionFields();
				};

				/**
				 */
				ParameterDefinitionsPanel.prototype.selectCurrentParameterDefinition = function() {
					if (this.parameterDefinitions.length == 0) {
						this.selectedRowIndex = -1;
						this.currentParameterDefinition = null;

						return;
					}

					// Select first parameter for non-empty parameter
					// definitions list and none preselected

					if (this.selectedRowIndex < 0) {
						this.selectedRowIndex = 0;
					}

					// Select last parameter if previous index exceeds length

					if (this.selectedRowIndex >= this.parameterDefinitions.length) {
						this.selectedRowIndex = this.parameterDefinitions.length - 1;
					}

					this.currentParameterDefinition = this.parameterDefinitions[this.selectedRowIndex];

					// Select row

					var tableRows = m_utils.jQuerySelect(this.options.scope
							+ " #parameterDefinitionsTable tr");

					m_utils.jQuerySelect(tableRows[this.selectedRowIndex]).addClass(
							"selected");
				};

				/**
				 *
				 */
				ParameterDefinitionsPanel.prototype.setScopeModel = function(
						scopeModel) {
					this.scopeModel = scopeModel;

					if (this.options.supportsDataTypeSelection) {
						this.dataTypeSelector.setScopeModel(scopeModel);
					}
				};

				/**
				 *
				 */
				ParameterDefinitionsPanel.prototype.submitChanges = function() {
					if (this.options.submitHandler) {
						this.options.submitHandler
								.submitParameterDefinitionsChanges(this.parameterDefinitions);
					}
				};

				/**
				 *
				 */
				ParameterDefinitionsPanel.prototype.setDescriptor = function() {
					this.descriptorInput.attr("checked", true);
					this.keyDescriptorInput.attr("checked", false);
				};

				/**
				 *
				 */
				ParameterDefinitionsPanel.prototype.setKeyDescriptor = function() {
					this.descriptorInput.attr("checked", false);
					this.keyDescriptorInput.attr("checked", true);
				};

				/**
				 *
				 */
				ParameterDefinitionsPanel.prototype.populateDataItemsList = function() {
					this.parameterDefinitionDataSelect.empty();

					this.parameterDefinitionDataSelect
							.append("<option value=\"TO_BE_DEFINED\">"
									+ m_i18nUtils
											.getProperty("modeler.general.toBeDefined")
									+ "</option>");

					if (this.scopeModel) {
						var modelname = m_i18nUtils
								.getProperty("modeler.element.properties.commonProperties.thisModel");
						this.parameterDefinitionDataSelect
								.append("<optgroup label=\"" + modelname
										+ "\">");

						for ( var i in this.scopeModel.dataItems) {
							var dataItem = this.scopeModel.dataItems[i];
							// Show only data items from this model and not
							// external references.
							if ((!dataItem.externalReference || this.options.showExternalDataReferences)
									&& this.isDataOfSelectedType(dataItem)) {
								this.parameterDefinitionDataSelect
										.append("<option value='"
												+ dataItem.getFullId() + "'>"
												+ dataItem.name + "</option>");
							}
						}
					}

					// TODO - Delete this
					// Other model types are not not needed for formal
					// parameters
					// var othermodel = m_i18nUtils
					// .getProperty("modeler.element.properties.commonProperties.otherModel")
					// this.parameterDefinitionDataSelect
					// .append("</optgroup><optgroup label=\""
					// + othermodel + "\">");
					//
					// for ( var n in m_model.getModels()) {
					// if (this.scopeModel
					// && m_model.getModels()[n] == this.scopeModel) {
					// continue;
					// }
					//
					// for ( var m in m_model.getModels()[n].dataItems) {
					// var dataItem = m_model.getModels()[n].dataItems[m];
					//
					// if (this.isDataOfSelectedType(dataItem)) {
					// this.parameterDefinitionDataSelect
					// .append("<option value='"
					// + dataItem.getFullId() + "'>"
					// + m_model.getModels()[n].name
					// + "/" + dataItem.name
					// + "</option>");
					// }
					// }
					// }
					//
					// this.parameterDefinitionDataSelect.append("</optgroup>");

					if (!this.currentParameterDefinition
							|| !this.currentParameterDefinition.dataFullId) {
						this.parameterDefinitionDataSelect
								.val(m_constants.TO_BE_DEFINED);
					} else {
						this.parameterDefinitionDataSelect
								.val(this.currentParameterDefinition.dataFullId);
					}
				};

				/**
				 *
				 */
				ParameterDefinitionsPanel.prototype.isDataOfSelectedType = function(
						data) {
					if (this.options.supportsDataTypeSelection == false) {
						return true;
					}

					if (this.dataTypeSelector
							&& data.dataType === this.dataTypeSelector.dataTypeSelect
									.val()) {
						if (data.dataType === m_constants.PRIMITIVE_DATA_TYPE
								&& data.primitiveDataType === this.dataTypeSelector.primitiveDataTypeSelect
										.val()) {
							return true
						} else if (data.dataType === m_constants.STRUCTURED_DATA_TYPE
								&& data.structuredDataTypeFullId === this.dataTypeSelector.structuredDataTypeSelect
										.val()) {
							return true
						} else if (data.dataType === m_constants.DOCUMENT_DATA_TYPE
								&& data.structuredDataTypeFullId === this.dataTypeSelector.documentTypeSelect
										.val()) {
							return true
						}
					}

					return false;
				}

				/**
				 *
				 */
				ParameterDefinitionsPanel.prototype.initializeParameterDefinitionsTable = function() {
					this.parameterDefinitionsTableBody.empty();

					for ( var m = 0; m < this.parameterDefinitions.length; ++m) {
						var parameterDefinition = this.parameterDefinitions[m];

						var content = "<tr id=\"" + m + "\">";

						content += "<td class=\"";

						if (parameterDefinition.direction == "IN") {
							if (this.options.supportsDescriptors) {
								if (parameterDefinition.descriptor) {
									content += "descriptorDataPathListItem";
								} else if (parameterDefinition.keyDescriptor) {
									content += "keyDescriptorDataPathListItem";
								} else {
									content += "inDataPathListItem";
								}
							} else {
								content += "inDataPathListItem";
							}
						} else if (parameterDefinition.direction == "INOUT") {
							content += "inoutDataPathListItem";
						} else {
							content += "outDataPathListItem";
						}

						content += "\" style=\"width: "
								+ this.options.directionColumnWidth
								+ "\"></td>";

						content += "<td style=\"width: "
								+ this.options.nameColumnWidth + "\">"
								+ parameterDefinition.name;
						content += "</td>";

						if (this.options.supportsDataTypeSelection) {
							content += "<td style=\"width: "
									+ this.options.typeColumnWidth + "\">";
							if (parameterDefinition.dataType == m_constants.PRIMITIVE_DATA_TYPE) {
								content += m_typeDeclaration
										.getPrimitiveTypeLabel(parameterDefinition.primitiveDataType); // TODO
								// Convert
							} else {
								if (parameterDefinition.structuredDataTypeFullId) {
									content += m_model
											.stripElementId(parameterDefinition.structuredDataTypeFullId); // TODO
								}
								// Format
							}

							content += "</td>";
						}

						if (this.options.supportsDataMappings) {
							content += "<td style=\"width: "
									+ this.options.mappingColumnWidth + "\">";

							if (parameterDefinition.dataFullId != null
									&& m_model
											.findData(parameterDefinition.dataFullId)) {
								var data = m_model
										.findData(parameterDefinition.dataFullId);

								content += data.name;

								if (this.options.supportsDataPathes) {
									if (parameterDefinition.dataPath != null) {
										content += ".";
										content += parameterDefinition.dataPath;
									}
								}
							}

							content += "</td>";
						}

						var newValue = m_i18nUtils
								.getProperty("modeler.element.properties.commonProperties.inputText.new");
						content = content.replace(">New", ">" + newValue);
						newValue = m_i18nUtils
								.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.element.selectType.string");
						content = content.replace("String", newValue);

						this.parameterDefinitionsTableBody.append(content);

						m_utils.jQuerySelect(
								this.options.scope
										+ "table#parameterDefinitionsTable tr")
								.mousedown(
										{
											panel : this
										},
										function(event) {
											event.data.panel
													.deselectParameterDefinitions();
											m_utils.jQuerySelect(this).addClass("selected");

											var index = m_utils.jQuerySelect(this).attr("id");

											event.data.panel.currentParameterDefinition = event.data.panel.parameterDefinitions[index];
											event.data.panel.selectedRowIndex = index;

											event.data.panel
													.populateParameterDefinitionFields();
										});
					}

					// Initialize event handling

					this.parameterDefinitionsTable.tableScroll({
						height : 150
					});
				};

				/**
				 *
				 */
				ParameterDefinitionsPanel.prototype.deselectParameterDefinitions = function(
						dataPath) {
					m_utils.jQuerySelect(
							this.options.scope
									+ "table#parameterDefinitionsTable tr.selected")
							.removeClass("selected");
				};

				/**
				 *
				 */
				ParameterDefinitionsPanel.prototype.populateParameterDefinitionFields = function() {
					if (!this.currentParameterDefinition
							|| (this.currentParameterDefinition.attributes && this.currentParameterDefinition.attributes["stardust:predefined"])) {
						this.parameterDefinitionNameInput
								.attr("disabled", true);
						this.parameterDefinitionDirectionSelect.attr(
								"disabled", true);

						if (this.options.supportsDataTypeSelection) {
							this.dataTypeSelector.disable();
						}

						if (this.options.supportsDescriptors) {
							this.descriptorInput.attr("disabled", true);
							this.keyDescriptorInput.attr("disabled", true);
						}

						if (this.options.supportsDataMappings) {
							this.parameterDefinitionDataSelect.attr("disabled",
									true);
							if (this.options.supportsDataPathes) {
								this.parameterDefinitionPathInput.attr(
										"disabled", true);
							}
						}

						this.deleteParameterDefinitionButton.attr("disabled",
								true);
					} else {
						if (this.options.readOnlyParameterList) {
							this.parameterDefinitionNameInput.attr("disabled",
									true);
							this.parameterDefinitionDirectionSelect.attr(
									"disabled", true);
						} else {
							this.parameterDefinitionNameInput
									.removeAttr("disabled");
							this.parameterDefinitionDirectionSelect
									.removeAttr("disabled");
						}

						if (this.options.displayParameterId) {
							if (m_user.getCurrentRole() == m_constants.INTEGRATOR_ROLE) {
								this.parameterDefinitionIdOutput.text(this.currentParameterDefinition.id);
								this.parameterDefinitionIdOutput.show();
								this.parameterDefinitionIdOutputLabel.show();
							} else {
								this.parameterDefinitionIdOutput.hide();
								this.parameterDefinitionIdOutputLabel.hide();
							}
						} else {
							this.parameterDefinitionIdOutput.hide();
							this.parameterDefinitionIdOutputLabel.hide();							
						}

						this.parameterDefinitionNameInput
								.val(this.currentParameterDefinition.name);
						this.parameterDefinitionDirectionSelect
								.val(this.currentParameterDefinition.direction);

						if (this.options.supportsDataTypeSelection) {
							if (this.options.readOnlyParameterList) {
								this.dataTypeSelector.disable();
							} else {
								this.dataTypeSelector.enable();
							}

							this.dataTypeSelector
									.setDataType(this.currentParameterDefinition);
						}

						if (this.options.supportsDescriptors) {
							if (this.currentParameterDefinition.direction == "IN") {
								if (!this.options.readOnlyParameterList) {
									this.descriptorInput.removeAttr("disabled");
									this.keyDescriptorInput
											.removeAttr("disabled");
								} else {
									this.descriptorInput.attr("disabled", true);
									this.keyDescriptorInput.attr("disabled",
											true);
								}

								this.descriptorInput
										.prop(
												"checked",
												this.currentParameterDefinition.descriptor);

								if (this.currentParameterDefinition.descriptor) {
									this.keyDescriptorInput
											.prop(
													"checked",
													this.currentParameterDefinition.keyDescriptor);
								} else {
									this.keyDescriptorInput.attr("disabled",
											true);
									this.keyDescriptorInput.prop("checked",
											false);
								}
							} else {
								this.descriptorInput.attr("disabled", true);
								this.keyDescriptorInput.attr("disabled", true);
								this.descriptorInput.prop("checked", false);
								this.keyDescriptorInput.prop("checked", false);
							}
						}

						if (this.options.supportsDataMappings) {
							this.parameterDefinitionDataSelect
									.removeAttr("disabled");

							this.populateDataItemsList();

							if (this.options.supportsDataPathes) {
								this.parameterDefinitionPathInput
										.removeAttr("disabled");
								this.parameterDefinitionPathInput
										.val(this.currentParameterDefinition.dataPath);
							}
						}

						// Disable name and direction for Process_attachments
						// TODO - check if this is a good place to check for
						// process attachments
						// data paths.
						// May be data paths themselves can have parameter
						// indicating whether
						// they are read only or not?
						if (this.currentParameterDefinition.dataFullId
								&& (-1 != this.currentParameterDefinition.dataFullId
										.indexOf("PROCESS_ATTACHMENTS"))) {
							this.parameterDefinitionNameInput.attr(
									"disabled", true);
							this.parameterDefinitionDirectionSelect.attr(
									"disabled", true);
							this.parameterDefinitionDataSelect.attr("disabled",
									true);
							if (this.options.supportsDataPathes) {
								this.parameterDefinitionPathInput.attr(
										"disabled", true);
							}
						} else {
							this.parameterDefinitionNameInput
									.removeAttr("disabled");
							this.parameterDefinitionDirectionSelect
									.removeAttr("disabled");

							if (this.options.supportsDataMappings) {
								this.parameterDefinitionDataSelect
										.removeAttr("disabled");
								if (this.options.supportsDataPathes) {
									this.parameterDefinitionPathInput
											.removeAttr("disabled");
								}
							}
						}

						this.deleteParameterDefinitionButton.removeAttr("disabled");

						if (this.currentFocusInput) {
							// Set focus and select

							this.currentFocusInput.focus();
							this.currentFocusInput.select();
						}
					}

					if (this.scopeModel && this.scopeModel.isReadonly()
							&& this.dataTypeSelectorScope) {
						m_utils.markControlsReadonlyForScope(
								this.dataTypeSelectorScope, this.scopeModel
										.isReadonly());
					}
				};

				/**
				 *
				 */
				ParameterDefinitionsPanel.prototype.addParameterDefinition = function() {
					var n = this.getNextIdIndex();

					this.currentParameterDefinition = {
						id : "New_" + n, // TODO: Anticipates renaming of ID
						// on server
						name : "New " + n, // TODO - i18n
						direction : "IN",
						dataFullId : null,
						dataPath : null
					};

					if (this.options.supportsDescriptors) {
						this.currentParameterDefinition.descriptor = false;
						this.currentParameterDefinition.keyDescriptor = false;
					}

					if (this.options.supportsDataTypeSelection) {
						this.dataTypeSelector
								.getDataType(this.currentParameterDefinition);
					}

					this.parameterDefinitions
							.push(this.currentParameterDefinition);

					// New parameter definitions are always appended

					this.selectedRowIndex = this.parameterDefinitions.length - 1;
					this.currentFocusInput = this.parameterDefinitionNameInput;

					this.submitChanges();
				};

				/**
				 *
				 */
				ParameterDefinitionsPanel.prototype.deleteParameterDefinition = function() {
					m_utils.debug("Deleting "
							+ this.currentParameterDefinition.id);

					var changedParameterDefinitions = [];

					for ( var n = 0; n < this.parameterDefinitions.length; ++n) {
						if (this.parameterDefinitions[n].id != this.currentParameterDefinition.id) {
							changedParameterDefinitions
									.push(this.parameterDefinitions[n]);
						}
					}

					this.parameterDefinitions = changedParameterDefinitions;

					this.selectedRowIndex = this.parameterDefinitions.length - 1;
					this.currentFocusInput = this.parameterDefinitionNameInput;

					this.submitChanges();
				};
				
				/**
				 * 
				 */
				ParameterDefinitionsPanel.prototype.getNextIdIndex = function() {
					var n = 0;
					var idOrNameExists = true;
					while (idOrNameExists) {
						n++;
						var newId = "New_" + n;
						var newName = "New " + n;  // TODO - i18n
						var idOrNameExists = false;
						for (var i = 0; i < this.parameterDefinitions.length; i++) {
							if (this.parameterDefinitions[i].id === newId
									|| this.parameterDefinitions[i].name === newName) {
								idOrNameExists = true;
								break;
							}
						}
					}
					
					return n;
				}

				/**
				 *
				 */
				ParameterDefinitionsPanel.prototype.moveParameterDefinitionUp = function() {
					var changedParameterDefinitions = [];

					for ( var n = 0; n < this.parameterDefinitions.length; ++n) {
						if (n + 1 < this.parameterDefinitions.length
								&& this.parameterDefinitions[n + 1].id == this.currentParameterDefinition.id) {
							changedParameterDefinitions
									.push(this.parameterDefinitions[n + 1]);
							changedParameterDefinitions
									.push(this.parameterDefinitions[n]);

							this.selectedRowIndex = n;

							++n;
						} else {
							changedParameterDefinitions
									.push(this.parameterDefinitions[n]);
						}
					}

					this.parameterDefinitions = changedParameterDefinitions;
					this.currentFocusInput = this.parameterDefinitionNameInput;

					this.submitChanges();
				};

				/**
				 *
				 */
				ParameterDefinitionsPanel.prototype.moveParameterDefinitionDown = function() {
					var changedParameterDefinitions = [];

					for ( var n = 0; n < this.parameterDefinitions.length; ++n) {
						if (n + 1 < this.parameterDefinitions.length
								&& this.parameterDefinitions[n].id == this.currentParameterDefinition.id) {
							changedParameterDefinitions
									.push(this.parameterDefinitions[n + 1]);
							changedParameterDefinitions
									.push(this.parameterDefinitions[n]);

							this.selectedRowIndex = n + 1;

							++n;
						} else {
							changedParameterDefinitions
									.push(this.parameterDefinitions[n]);
						}
					}

					this.parameterDefinitions = changedParameterDefinitions;
					this.currentFocusInput = this.parameterDefinitionNameInput;

					this.submitChanges();
				};

				/**
				 *
				 */
				ParameterDefinitionsPanel.prototype.validate = function() {
				};

				/**
				 *
				 */
				ParameterDefinitionsPanel.prototype.submitDataChanges = function(
						dataChanges) {
					if (this.currentParameterDefinition == null) {
						return null;
					}

					this.currentParameterDefinition.dataType = dataChanges.dataType;
					this.currentParameterDefinition.primitiveDataType = dataChanges.primitiveDataType;
					this.currentParameterDefinition.structuredDataTypeFullId = dataChanges.structuredDataTypeFullId;

					this.submitChanges();
				};
			}
		});