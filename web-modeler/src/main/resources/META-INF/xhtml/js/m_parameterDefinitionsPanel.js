/**
 * Utility functions for dialog programming.
 * 
 * @author Marc.Gille
 */
define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_extensionManager", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_typeDeclaration", "bpm-modeler/js/m_dialog",
				"bpm-modeler/js/m_dataTypeSelector",
				"bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_extensionManager, m_model,
				m_typeDeclaration, m_dialog, m_dataTypeSelector, m_i18nUtils) {
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
					} else {
						this.options.scope = "#" + this.options.scope + " ";
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
					this.parameterDefinitionsTable = jQuery(this.options.scope
							+ " #parameterDefinitionsTable");

					this.parameterDefinitionsTable.css("width",
							this.options.tableWidth);

					this.parameterDefinitionsTableBody = jQuery(this.options.scope
							+ " #parameterDefinitionsTable tbody");
					this.parameterDefinitionNameInput = jQuery(this.options.scope
							+ " #parameterDefinitionNameInput");
					this.parameterDefinitionDirectionSelect = jQuery(this.options.scope
							+ " #parameterDefinitionDirectionSelect");
					this.addParameterDefinitionButton = jQuery(this.options.scope
							+ " #addParameterDefinitionButton");
					this.deleteParameterDefinitionButton = jQuery(this.options.scope
							+ " #deleteParameterDefinitionButton");

					this.currentFocusInput = this.parameterDefinitionNameInput;

					if (this.options.supportsDataTypeSelection) {
						this.dataTypeSelector = m_dataTypeSelector.create({
							scope : "parameterDefinitionTypeSelector",
							submitHandler : this,
							supportsOtherData : false
						});
					}

					if (this.options.supportsOrdering) {
						this.moveParameterDefinitionUpButton = jQuery(this.options.scope
								+ " #moveParameterDefinitionUpButton");
						this.moveParameterDefinitionDownButton = jQuery(this.options.scope
								+ " #moveParameterDefinitionDownButton");
					}

					if (this.options.supportsDescriptors) {
						this.descriptorInput = jQuery(this.options.scope
								+ " #parameterDefinitionDescriptorInput");
						this.keyDescriptorInput = jQuery(this.options.scope
								+ " #parameterDefinitionKeyDescriptorInput");
					}

					if (this.options.supportsDataMappings) {
						this.parameterDefinitionDataSelect = jQuery(this.options.scope
								+ " #parameterDefinitionDataSelect");

						if (this.options.supportsDataPathes) {
							this.parameterDefinitionPathInput = jQuery(this.options.scope
									+ " #parameterDefinitionPathInput");
						}
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

					m_utils.debug("Row Index: " + this.selectedRowIndex);

					this.currentParameterDefinition = this.parameterDefinitions[this.selectedRowIndex];

					// Select row

					var tableRows = jQuery(this.options.scope
							+ " #parameterDefinitionsTable tr");

					m_utils.debug("Table Rows: " + tableRows);

					jQuery(tableRows[this.selectedRowIndex]).addClass(
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

					if (this.options.supportsDataMappings) {
						this.populateDataItemsList();
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
							.append("<option value=\"TO_BE_DEFINED\">(To be defined))</option>");

					var modelname = m_i18nUtils
							.getProperty("modeler.element.properties.commonProperties.thisModel");
					this.parameterDefinitionDataSelect
							.append("<optgroup label=\"" + modelname + "\">");

					for ( var i in this.scopeModel.dataItems) {
						var dataItem = this.scopeModel.dataItems[i];

						this.parameterDefinitionDataSelect
								.append("<option value='"
										+ dataItem.getFullId() + "'>"
										+ dataItem.name + "</option>");
					}

					var othermodel = m_i18nUtils
							.getProperty("modeler.element.properties.commonProperties.otherModel")
					this.parameterDefinitionDataSelect
							.append("</optgroup><optgroup label=\""
									+ othermodel + "\">");

					for ( var n in m_model.getModels()) {
						if (m_model.getModels()[n] == this.scopeModel) {
							continue;
						}

						for ( var m in m_model.getModels()[n].dataItems) {
							var dataItem = m_model.getModels()[n].dataItems[m];

							this.parameterDefinitionDataSelect
									.append("<option value='"
											+ dataItem.getFullId() + "'>"
											+ m_model.getModels()[n].name + "/"
											+ dataItem.name + "</option>");
						}
					}

					this.parameterDefinitionDataSelect.append("</optgroup>");
				};

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
								content += parameterDefinition.structuredDataTypeFullId; // TODO
								// Format
							}

							content += "</td>";
						}

						if (this.options.supportsDataMappings) {
							content += "<td style=\"width: "
									+ this.options.mappingColumnWidth + "\">";

							if (parameterDefinition.dataFullId != null) {
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

						jQuery(
								this.options.scope
										+ "table#parameterDefinitionsTable tr")
								.mousedown(
										{
											panel : this
										},
										function(event) {
											event.data.panel
													.deselectParameterDefinitions();
											jQuery(this).addClass("selected");

											var index = jQuery(this).attr("id");

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
					jQuery(
							this.options.scope
									+ "table#parameterDefinitionsTable tr.selected")
							.removeClass("selected");
				};

				/**
				 * 
				 */
				ParameterDefinitionsPanel.prototype.populateParameterDefinitionFields = function() {
					if (this.currentParameterDefinition == null) {
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

							if (this.currentParameterDefinition.dataFullId == null) {
								this.parameterDefinitionDataSelect
										.val(m_constants.TO_BE_DEFINED);
							} else {
								this.parameterDefinitionDataSelect
										.val(this.currentParameterDefinition.dataFullId);
							}

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
							this.parameterDefinitionDirectionSelect.attr(
									"disabled", true);
							this.parameterDefinitionDataSelect.attr("disabled",
									true);
							if (this.options.supportsDataPathes) {
								this.parameterDefinitionPathInput.attr(
										"disabled", true);
							}
						} else {
							this.parameterDefinitionDirectionSelect
									.removeAttr("disabled");
							this.parameterDefinitionDataSelect
									.removeAttr("disabled");
							if (this.options.supportsDataPathes) {
								this.parameterDefinitionPathInput
										.removeAttr("disabled");
							}
						}

						if (this.currentFocusInput) {
							// Set focus and select

							this.currentFocusInput.focus();
							this.currentFocusInput.select();
						}
					}
				};

				/**
				 * 
				 */
				ParameterDefinitionsPanel.prototype.addParameterDefinition = function() {
					var n = this.parameterDefinitions.length;

					this.currentParameterDefinition = {
						id : "New_" + n, // TODO: Anticipates renaming of ID
						// on server
						name : "New " + n,
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