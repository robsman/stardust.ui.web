/**
 * Utility functions for dialog programming.
 * 
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_extensionManager", "m_model",
				"m_dialog", "m_dataTypeSelector" ],
		function(m_utils, m_constants, m_extensionManager, m_model, m_dialog,
				m_dataTypeSelector) {
			return {
				create : function(options) {
					var panel = new ParameterDefinitionsPanel();

					panel.initialize(options);

					return panel;
				}
			};

			/**
			 * Options are scope submitHandler listType supportsDataMappings
			 * supportsDescriptors supportsDataTypeSelection supportsDataPathes
			 * directionColumnWidth nameColumnWidth typeColumnWidth
			 * mappingColumnWidth tableWidth
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

					if (this.options.tableWidth == null) {
						this.options.tableWidth = "400px";
					}

					if (this.options.directionColumnWidth == null) {
						this.options.directionColumnWidth = "50px";
					}

					if (this.options.nameColumnWidth == null) {
						this.options.nameColumnWidth = "150px";
					}

					if (this.options.typeColumnWidth == null) {
						this.options.typeColumnWidth = "200px";
					}

					if (this.options.mappingColumnWidth == null) {
						this.options.mappingColumnWidth = "200px";
					}

					if (this.options.listType == "array") {
						this.parameterDefinitionsTable = [];
					} else {
						this.parameterDefinitionsTable = {};
					}

					this.currentParameterDefinition = null;
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

					if (this.options.listType == "array") {
						this.moveParameterDefinitionUpButton = jQuery(this.options.scope
								+ " #moveParameterDefinitionUpButton");
						this.moveParameterDefinitionDownButton = jQuery(this.options.scope
								+ " #moveParameterDefinitionDownButton");
					}

					if (this.options.supportsDataTypeSelection) {
						this.dataTypeSelector = m_dataTypeSelector.create(
								"parameterDefinitionTypeSelector", this);
					}

					if (this.options.listType == "array") {
						this.moveParameterDefinitionUpButton = jQuery(this.options.scope
								+ " #moveParameterDefinitionUpButton");
						this.moveParameterDefinitionDownButton = jQuery(this.options.scope
								+ " #moveParameterDefinitionButton");
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

					if (this.options.listType == "array") {
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
											event.data.panel
													.initializeParameterDefinitionsTable();
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
											event.data.panel
													.initializeParameterDefinitionsTable();
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
													.val();
											event.data.panel
													.initializeParameterDefinitionsTable();
										});
						this.keyDescriptorInput
								.change(
										{
											"panel" : this
										},
										function(event) {
											event.data.panel.currentDataPath.keyDescriptor = event.data.panel.keyDescriptorInput
													.val();
											event.data.panel
													.initializeParameterDefinitionsTable();
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

												event.data.panel
														.initializeParameterDefinitionsTable();
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
													event.data.panel
															.initializeParameterDefinitionsTable();
												}
											});
						}
					}
				};

				/**
				 * 
				 */
				ParameterDefinitionsPanel.prototype.setParameterDefinitions = function(
						parameterDefinitions) {
					this.parameterDefinitions = parameterDefinitions;

					this.initializeParameterDefinitionsTable();
					this.populateParameterDefinitionFields();

					this.selectCurrentParameterDefinition();
				};

				/**
				 * 
				 */
				ParameterDefinitionsPanel.prototype.selectCurrentParameterDefinition = function() {
					if (this.currentParameterDefinition == null) {
						return;
					}

					var id = 0;

					if (this.options.listType == "array") {
						for ( var n = 0; n < this.parameterDefinitions.length; ++n) {
							if (this.parameterDefinitions[n].id == this.currentParameterDefinition.id) {
								id = n;

								break;
							}
						}
					} else {
						id = this.currentParameterDefinition.id;
					}

					jQuery("table#parameterDefinitionsTable tr#" + id)
							.addClass("selected");
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

					this.parameterDefinitionDataSelect
							.append("<optgroup label=\"This Model\">");

					for ( var i in this.scopeModel.dataItems) {
						var dataItem = this.scopeModel.dataItems[i];

						this.parameterDefinitionDataSelect
								.append("<option value='"
										+ dataItem.getFullId() + "'>"
										+ dataItem.name + "</option>");
					}

					this.parameterDefinitionDataSelect
							.append("</optgroup><optgroup label=\"Other Models\">");

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

					for ( var m in this.parameterDefinitions) {
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
								content += parameterDefinition.primitiveDataType; // TODO
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

											id = jQuery(this).attr("id");

											event.data.panel.currentParameterDefinition = event.data.panel.parameterDefinitions[id];
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
						this.parameterDefinitionNameInput
								.removeAttr("disabled");
						this.parameterDefinitionDirectionSelect
								.removeAttr("disabled");
						this.parameterDefinitionNameInput
								.val(this.currentParameterDefinition.name);
						this.parameterDefinitionDirectionSelect
								.val(this.currentParameterDefinition.direction);

						if (this.options.supportsDataTypeSelection) {
							this.dataTypeSelector.enable();
							this.dataTypeSelector
									.setDataType(this.currentParameterDefinition);
						}

						if (this.options.supportsDescriptors) {
							this.descriptorInput.removeAttr("disabled");
							this.keyDescriptorInput.removeAttr("disabled");
							this.descriptorInput
									.val(this.currentParameterDefinition.descriptor);
							this.keyDescriptorInput
									.val(this.currentParameterDefinition.keyDescriptor);
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
					}
				};

				/**
				 * 
				 */
				ParameterDefinitionsPanel.prototype.addParameterDefinition = function() {
					var n = 1;

					if (this.parameterDefinitions == null) {
						this.parameters = {};
					} else {
						for ( var m in this.parameterDefinitions) {
							++n;
						}
					}

					this.currentParameterDefinition = {
						id : "New" + n,
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

					if (this.options.listType == "array") {
						this.parameterDefinitions
								.push(this.currentParameterDefinition);
					} else {
						this.parameterDefinitions[this.currentParameterDefinition.id] = this.currentParameterDefinition;
					}

					this.submitChanges(this.parameterDefinitions);
				};

				/**
				 * 
				 */
				ParameterDefinitionsPanel.prototype.deleteParameterDefinition = function() {
					var changedParameterDefinitions = [];

					for ( var n = 0; n < this.parameterDefinitions.length; ++n) {
						if (this.parameterDefinitions[n].id != this.currentParameterDefinition.id) {
							changedParameterDefinitions
									.push(this.parameterDefinitions[n]);
						}
					}

					this.parameterDefinitions = changedParameterDefinitions;

					this.submitChanges(this.parameterDefinitions);
				};

				/**
				 * 
				 */
				ParameterDefinitionsPanel.prototype.moveParameterDefinitionUp = function(
						dataPathId) {
					var changedParameterDefinitions = [];

					for ( var n = 0; n < this.parameterDefinitions.length; ++n) {
						if (n + 1 < this.parameterDefinitions.length
								&& this.parameterDefinitions[n + 1].id == dataPathId)
							changedParameterDefinitions
									.push(this.parameterDefinitions[n + 1]);
						changedParameterDefinitions
								.push(this.parameterDefinitions[n]);

						++n;
					}

					this.parameterDefinitions = changedParameterDefinitions;

					this.submitChanges(this.parameterDefinitions);
				};

				/**
				 * 
				 */
				ParameterDefinitionsPanel.prototype.moveParameterDefinitionDown = function(
						dataPathId) {
					var changedParameterDefinitions = [];

					for ( var n = 0; n < this.parameterDefinitions.length; ++n) {
						if (n + 1 < this.parameterDefinitions.length
								&& this.parameterDefinitions[n + 1].id == dataPathId)
							changedParameterDefinitions
									.push(this.parameterDefinitions[n + 1]);
						changedParameterDefinitions
								.push(this.parameterDefinitions[n]);

						++n;
					}

					this.parameterDefinitions = changedParameterDefinitions;

					this.submitChanges(this.parameterDefinitions);
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

					this.submitChanges(this.parameterDefinitions);
				};
			}
		});