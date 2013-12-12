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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_extensionManager", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_dialog", "bpm-modeler/js/m_i18nUtils", "bpm-modeler/js/m_modelElementUtils",
				"bpm-modeler/js/m_messageDisplay"],
		function(m_utils, m_constants, m_extensionManager, m_model, m_dialog,
				m_i18nUtils, m_modelElementUtils, m_messageDisplay) {
			return {
				create : function(options) {
					var panel = new DataTypeSelector();

					panel.initialize(options);

					return panel;
				}
			};

			/**
			 *
			 */
			function DataTypeSelector() {

			DataTypeSelector.prototype.startsWith = function(str, prefix) {
						return str.indexOf(prefix) === 0;
				}
			DataTypeSelector.prototype.checkElementId = function(id, prefix) {
						if(this.startsWith(id,"#"))
							return id;
					return "#"+id;
				}

				/**
				 * Options are
				 *
				 * scope submitHandler supportsOtherData
				 */
				DataTypeSelector.prototype.initialize = function(options) {
					this.scope = options.scope;
					this.submitHandler = options.submitHandler;
					this.supportsOtherData = options.supportsOtherData;
					this.supportsDocumentTypes = options.supportsDocumentTypes;
					this.restrictToCurrentModel = options.restrictToCurrentModel;
					this.hideEnumerations = options.hideEnumerations;


					this.dataTypeSelect = m_utils.jQuerySelect(this.checkElementId(this.scope) + " #dataTypeSelect");
					this.primitiveDataTypeRow = m_utils.jQuerySelect(this.checkElementId(this.scope) + " #primitiveDataTypeRow");
					this.primitiveDataTypeSelect = m_utils.jQuerySelect(this.checkElementId(this.scope)+ " #primitiveDataTypeSelect");
					this.structuredDataTypeRow = m_utils.jQuerySelect(this.checkElementId(this.scope)	+ " #structuredDataTypeRow");
					this.structuredDataTypeSelect = m_utils.jQuerySelect(this.checkElementId(this.scope)+ " #structuredDataTypeSelect");
					this.documentTypeSelect = m_utils.jQuerySelect(this.checkElementId(this.scope)+ " #documentTypeSelect");
					this.documentTypeRow = m_utils.jQuerySelect(this.checkElementId(this.scope)+ " #documentTypeRow");
					this.otherTypeRow = m_utils.jQuerySelect(this.checkElementId(this.scope)+ " #otherTypeRow");
					this.otherTypeName = m_utils.jQuerySelect(this.checkElementId(this.scope)	+ " #otherTypeName");

					this.initializeDataTypeOptions();

					this.dataTypeSelect.change({
						panel : this
					}, function(event) {
						event.data.panel.setDataTypeSelectVal({
							dataType : event.data.panel.dataTypeSelect.val()
						});

						event.data.panel.submitChanges();
					});
					this.primitiveDataTypeSelect
							.change(
									{
										panel : this
									},
									function(event) {
										event.data.panel
												.setPrimitiveDataType(event.data.panel.primitiveDataTypeSelect
														.val());
										event.data.panel.submitChanges();
									});
					this.structuredDataTypeSelect
							.change(
									{
										panel : this
									},
									function(event) {
										if (event.data.panel.validate()) {
											event.data.panel
													.setStructuredDataType(event.data.panel.structuredDataTypeSelect
															.val());
											event.data.panel.submitChanges();
										}
									});
					this.documentTypeSelect
							.change(
									{
										panel : this
									},
									function(event) {
										if (event.data.panel.validate()) {
											event.data.panel
													.setDocumentDataType(event.data.panel.documentTypeSelect
															.val());
											event.data.panel.submitChanges();
										}
									});
				};

				DataTypeSelector.prototype.validate = function() {
					return this.validateCircularModelReference();
				}

				DataTypeSelector.prototype.validateCircularModelReference = function() {
					if (this.submitHandler && this.submitHandler.clearErrorMessages) {
						this.submitHandler.clearErrorMessages();
					} else {
						m_messageDisplay.clear();
					}

					this.structuredDataTypeSelect.removeClass("error");
					this.documentTypeSelect.removeClass("error");

					var dataInput;
					if (this.dataTypeSelect.val() === m_constants.STRUCTURED_DATA_TYPE) {
						dataInput = this.structuredDataTypeSelect;
					} else if (this.dataTypeSelect.val() === m_constants.DOCUMENT_DATA_TYPE) {
						dataInput = this.documentTypeSelect;
					}

					var otherModelId = m_model.stripModelId(dataInput.val());

					// Check needed for scenarios where the scope model is not the model to which the model element belongs.
					// e.g. Property panel for external data (here data belongs to external model, but scopeModel is this model,
					// as the property panel belongs to this model).
					if (this.submitHandler
							&& typeof this.submitHandler.getModelElement === "function"
							&& this.submitHandler.getModelElement()
							&& this.submitHandler.getModelElement().modelId) {
						var thisModelId = this.submitHandler.getModelElement().modelId;
					} else {
						var thisModelId = this.scopeModel ? this.scopeModel.id : null;
					}

					if (thisModelId
							&& thisModelId != otherModelId
							&& m_model.isModelReferencedIn(thisModelId, otherModelId)) {
						dataInput.addClass("error");
						if (this.submitHandler && this.submitHandler.errorMessages) {
							this.submitHandler.errorMessages
									.push(m_i18nUtils
											.getProperty("modeler.propertyPages.commonProperties.errorMessage.modelCircularReferenceNotAllowed"));
							this.submitHandler.showErrorMessages();
						} else {
							m_messageDisplay.showMessage(m_i18nUtils
									.getProperty("modeler.propertyPages.commonProperties.errorMessage.modelCircularReferenceNotAllowed"));
						}

						return false;
					}

					return true;
				}

				/**
				 *
				 */
				DataTypeSelector.prototype.initializeDataTypeOptions = function() {
					this.dataTypeSelect.empty();

					var propertiesData = m_i18nUtils
							.getProperty("modeler.element.properties.commonProperties.primitive");

					this.dataTypeSelect.append("<option value='primitive'>"
							+ propertiesData + "</option>");

					propertiesData = m_i18nUtils
							.getProperty("modeler.element.properties.commonProperties.structureData");

					this.dataTypeSelect.append("<option value='struct'>"
							+ propertiesData + "</option>");

					if (this.supportsDocumentTypes) {
						propertiesData = m_i18nUtils
								.getProperty("modeler.element.properties.commonProperties.document");
						this.dataTypeSelect
								.append("<option value='dmsDocument'>"
										+ propertiesData + "</option>");
					}

					if (this.supportsOtherData) {
						propertiesData = m_i18nUtils
								.getProperty("modeler.element.properties.commonProperties.other");
						this.dataTypeSelect.append("<option value='other'>"
								+ propertiesData + "</option>");
					}
				}

				/**
				 *
				 */
				DataTypeSelector.prototype.setScopeModel = function(scopeModel) {
					this.scopeModel = scopeModel;

					this.populatePrimitivesSelectInput();
					this.populateDataStructuresSelectInput();
					this.populateDocumentTypesSelectInput();
				};

				/**
				 *
				 */
				DataTypeSelector.prototype.populatePrimitivesSelectInput = function() {
					this.primitiveDataTypeSelect.empty();

					var dataType = null;
					dataType = m_i18nUtils
							.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.string");
					this.primitiveDataTypeSelect
							.append("<option value=\"String\" title=\"String\">" + dataType
									+ "</option>");
					dataType = m_i18nUtils
							.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.boolean");
					this.primitiveDataTypeSelect
							.append("<option value=\"boolean\" title=\"boolean\">" + dataType
									+ "</option>");
					dataType = m_i18nUtils
							.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.int");
					this.primitiveDataTypeSelect
							.append("<option value=\"int\" title=\"int\">" + dataType
									+ "</option>");
					dataType = m_i18nUtils
							.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.long");
					this.primitiveDataTypeSelect
							.append("<option value=\"long\" title=\"long\">" + dataType
									+ "</option>");
					dataType = m_i18nUtils
							.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.double");
					this.primitiveDataTypeSelect
							.append("<option value=\"double\" title=\"double\">" + dataType
									+ "</option>");
					dataType = m_i18nUtils
							.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.timestamp");
					this.primitiveDataTypeSelect
							.append("<option value=\"Timestamp\" title=\"Timestamp\">" + dataType
									+ "</option>");

					if (this.scopeModel && this.hideEnumerations != false) {
						this.primitiveDataTypeSelect
								.append("<optgroup label='" + m_i18nUtils.getProperty("modeler.enum.thisModel") + "'>");

						for ( var i in this.scopeModel.typeDeclarations) {
							if (this.scopeModel.typeDeclarations[i].isSequence()) continue;
							if (this.scopeModel.typeDeclarations[i]
							.getType() == "enumStructuredDataType"){
								this.primitiveDataTypeSelect
								.append("<option value='" + this.scopeModel.typeDeclarations[i].getFullId() + "'>"
										+ this.scopeModel.typeDeclarations[i].name
										+ "</option>");
							}

						}
					}

					if (!this.restrictToCurrentModel && this.hideEnumerations != false) {
						this.primitiveDataTypeSelect
								.append("</optgroup><optgroup label='" + m_i18nUtils.getProperty("modeler.enum.otherModels") + "'>");

						for ( var n in m_model.getModels()) {
							if (this.scopeModel
									&& m_model.getModels()[n] == this.scopeModel) {
								continue;
							}

							for ( var m in m_model.getModels()[n].typeDeclarations) {
								if (m_modelElementUtils
										.hasPublicVisibility(m_model
												.getModels()[n].typeDeclarations[m])) {

									if (m_model.getModels()[n].typeDeclarations[m].isSequence()) continue;
										if (m_model.getModels()[n].typeDeclarations[m]
												.getType() == "enumStructuredDataType") {
											this.primitiveDataTypeSelect
													.append("<option value='"+m_model.getModels()[n].typeDeclarations[m].getFullId()+"'>"
															+ m_model.getModels()[n].name
															+ "/"
															+ m_model.getModels()[n].typeDeclarations[m].name
															+ "</option>");
										}
								}
							}
						}

						this.primitiveDataTypeSelect.append("</optgroup>");
					}

					// Commented as we don't support Money and Calendar values yet.
//					dataType = m_i18nUtils
//							.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.decimal");
//					this.primitiveDataTypeSelect
//							.append("<option value=\"Decimal\" title=\"Decimal\">" + dataType
//									+ "</option>");
//					dataType = m_i18nUtils
//							.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.calender");
//					this.primitiveDataTypeSelect
//							.append("<option value=\"Calendar\" title=\"Calendar\">" + dataType
//									+ "</option>");
				}

				/**
				 *
				 */
				DataTypeSelector.prototype.populateDataStructuresSelectInput = function() {
					this.structuredDataTypeSelect.empty();
					this.structuredDataTypeSelect.append("<option value='"
							+ m_constants.TO_BE_DEFINED
							+ "'>" + m_i18nUtils.getProperty("modeler.general.toBeDefined") + "</option>");

					if (this.scopeModel) {
						this.structuredDataTypeSelect
								.append("<optgroup label='" + m_i18nUtils.getProperty("modeler.general.thisModel") + "'>");

						for ( var i in this.scopeModel.typeDeclarations) {
							if (!this.scopeModel.typeDeclarations[i].isSequence()) continue;
							// Enum data is shown under primitive
							if (this.scopeModel.typeDeclarations[i].getType() != "enumStructuredDataType") {
							this.structuredDataTypeSelect
									.append("<option value='"
											+ this.scopeModel.typeDeclarations[i]
													.getFullId()
											+ "'>"
											+ this.scopeModel.typeDeclarations[i].name
											+ "</option>");
							}
						}
					}

					if (!this.restrictToCurrentModel) {
						this.structuredDataTypeSelect
								.append("</optgroup><optgroup label='" + m_i18nUtils.getProperty("modeler.general.otherModels") + "'>");

						for ( var n in m_model.getModels()) {
							if (this.scopeModel
									&& m_model.getModels()[n] == this.scopeModel) {
								continue;
							}

							for ( var m in m_model.getModels()[n].typeDeclarations) {
								if (m_modelElementUtils
										.hasPublicVisibility(m_model
												.getModels()[n].typeDeclarations[m])) {
									if (!m_model.getModels()[n].typeDeclarations[m].isSequence()) continue;
									// Enum data is shown under primitive
									if (m_model.getModels()[n].typeDeclarations[m]
											.getType() != "enumStructuredDataType") {
										this.structuredDataTypeSelect
										.append("<option value='"
												+ m_model.getModels()[n].typeDeclarations[m]
														.getFullId()
												+ "'>"
												+ m_model.getModels()[n].name
												+ "/"
												+ m_model.getModels()[n].typeDeclarations[m].name
												+ "</option>");
									}
								}
							}
						}

						this.structuredDataTypeSelect.append("</optgroup>");
					}
				};

				/**
				 *
				 */
				DataTypeSelector.prototype.populateDocumentTypesSelectInput = function() {
					this.documentTypeSelect.empty();
					this.documentTypeSelect.append("<option value='"
							+ m_constants.TO_BE_DEFINED
							+ "'>" + m_i18nUtils.getProperty("modeler.general.toBeDefined") + "</option>");

					this.documentTypeSelect
							.append("<optgroup label='" + m_i18nUtils.getProperty("modeler.general.thisModel") + "'>");

					if (this.scopeModel) {
						for ( var i in this.scopeModel.typeDeclarations) {
							// Only composite structured types and not
							// enumerations
							// are listed here
							if (this.scopeModel.typeDeclarations[i]
									.isSequence()) {
								this.documentTypeSelect
										.append("<option value='"
												+ this.scopeModel.typeDeclarations[i]
														.getFullId()
												+ "'>"
												+ this.scopeModel.typeDeclarations[i].name
												+ "</option>");
							}
						}
					}

					if (!this.restrictToCurrentModel) {
						this.documentTypeSelect
								.append("</optgroup><optgroup label='" + m_i18nUtils.getProperty("modeler.general.otherModels") + "'>");

						for ( var n in m_model.getModels()) {
							if (this.scopeModel
									&& m_model.getModels()[n] == this.scopeModel) {
								continue;
							}

							for ( var m in m_model.getModels()[n].typeDeclarations) {
								// Only composite structured types (with public
								// visibility) and not
								// enumerations are listed here
								if (m_model.getModels()[n].typeDeclarations[m]
										.isSequence()
										&& m_modelElementUtils
												.hasPublicVisibility(m_model
														.getModels()[n].typeDeclarations[m])) {
									this.documentTypeSelect
											.append("<option value='"
													+ m_model.getModels()[n].typeDeclarations[m]
															.getFullId()
													+ "'>"
													+ m_model.getModels()[n].name
													+ "/"
													+ m_model.getModels()[n].typeDeclarations[m].name
													+ "</option>");
								}
							}
						}

						this.documentTypeSelect.append("</optgroup>");
					}
				};

				/**
				 *
				 */
				DataTypeSelector.prototype.setDataType = function(data) {
					if (data.isSupportedDataType
							&& typeof data.isSupportedDataType === "function") {
						this.supportsOtherData = !data.isSupportedDataType();
					} else {
						this.supportsOtherData = false;
					}

					this.initializeDataTypeOptions();

					this.setDataTypeSelectVal(data);
				};

				/**
				 *
				 */
				DataTypeSelector.prototype.setDataTypeSelectVal = function(data) {
					this.dataTypeSelect.val(data.dataType);

					if (data.dataType == null
							|| data.dataType == m_constants.PRIMITIVE_DATA_TYPE) {
						this.setPrimitiveDataType(data.primitiveDataType);
					} else if (data.dataType == m_constants.STRUCTURED_DATA_TYPE) {
						// If enum type show as Primitive
						if (this.isEnumTypeDeclaration(data.structuredDataTypeFullId)) {
								this.dataTypeSelect.val("primitive");
								this.setPrimitiveDataType(data.structuredDataTypeFullId,true);
							}else {
							this.setStructuredDataType(data.structuredDataTypeFullId);
						}
					} else if (data.dataType == m_constants.DOCUMENT_DATA_TYPE) {
						this.setDocumentDataType(data.structuredDataTypeFullId);
					} else {
						this.dataTypeSelect.val("other");
						this.setOtherDataType(data.dataType);
					}
				};

				/**
				 * 
				 */
				DataTypeSelector.prototype.isEnumTypeDeclaration = function(
						fullId) {
					try {
						var typeDeclaration = m_model
								.findModel(m_model.stripModelId(fullId)).typeDeclarations[m_model
								.stripElementId(fullId)];
						if (typeDeclaration.getType() == "enumStructuredDataType") {
							return true;
						}
					} catch (e) {
						return false;
					}
					return false;
				};
				
				/**
				 *
				 */
				DataTypeSelector.prototype.getDataType = function(data) {
					data.dataType = this.dataTypeSelect.val();

					if (this.dataTypeSelect.val() == m_constants.PRIMITIVE_DATA_TYPE) {
						data.primitiveDataType = this.primitiveDataTypeSelect
								.val();
						if(this.isEnumTypeDeclaration(data.primitiveDataType)){
							data.structuredDataTypeFullId = this.primitiveDataTypeSelect.val();
						}
					} else if (this.dataTypeSelect.val() == m_constants.STRUCTURED_DATA_TYPE) {
						data.structuredDataTypeFullId = this.structuredDataTypeSelect
								.val();
					} else if (this.dataTypeSelect.val() == m_constants.DOCUMENT_DATA_TYPE) {
						data.structuredDataTypeFullId = this.documentTypeSelect
								.val();
					}

					return data;
				};

				/**
				 *
				 */
				DataTypeSelector.prototype.setPrimitiveDataType = function(
						primitiveDataType,isEnum) {
					// Reinitialize the primitive type options to get rid of "Other" option
					this.populatePrimitivesSelectInput();

					if (primitiveDataType == null) {
						primitiveDataType = "String";
					}
					if (this.isSupportedPrimitiveDataType(primitiveDataType) || isEnum || this.isEnumTypeDeclaration(primitiveDataType)) {
							this.primitiveDataTypeSelect.val(primitiveDataType);
					}else {
							this.primitiveDataTypeSelect
							.append("<option value=\"other\">"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.commonProperties.other")
									+ "</option>");
					this.primitiveDataTypeSelect.val("other");
					}

					m_dialog.makeVisible(this.primitiveDataTypeRow);
					m_dialog.makeInvisible(this.structuredDataTypeRow);
					m_dialog.makeInvisible(this.documentTypeRow);

					if (this.otherTypeRow != null) {
						m_dialog.makeInvisible(this.otherTypeRow);
					}
				};

				/**
				 *
				 */
				DataTypeSelector.prototype.isSupportedPrimitiveDataType = function(
						primitiveDataType) {
					var supportedPrimitiveTypes = ["String", "boolean", "int", "long", "double", "Timestamp", "Enumeration" ];

					if (primitiveDataType
							&& supportedPrimitiveTypes.indexOf(primitiveDataType) > -1) {
						return true;
					}
					return false;
				};

				/**
				 *
				 */
				DataTypeSelector.prototype.setStructuredDataType = function(
						structuredDataTypeFullId) {
					this.structuredDataTypeSelect.val(structuredDataTypeFullId);

					m_dialog.makeInvisible(this.primitiveDataTypeRow);
					m_dialog.makeVisible(this.structuredDataTypeRow);
					m_dialog.makeInvisible(this.documentTypeRow);

					if (this.otherTypeRow != null) {
						m_dialog.makeInvisible(this.otherTypeRow);
					}
				};

				/**
				 *
				 */
				DataTypeSelector.prototype.setDocumentDataType = function(
						documentDataTypeFullId) {
					this.documentTypeSelect.val(documentDataTypeFullId);

					m_dialog.makeInvisible(this.primitiveDataTypeRow);
					m_dialog.makeInvisible(this.structuredDataTypeRow);
					m_dialog.makeVisible(this.documentTypeRow);

					if (this.otherTypeRow != null) {
						m_dialog.makeInvisible(this.otherTypeRow);
					}
				};

				/**
				 *
				 */
				DataTypeSelector.prototype.setOtherDataType = function(dataType) {
					if (this.otherTypeRow == null || this.otherTypeName == null) {
						throw "otherTypeInput not initialized.";
					}

					m_dialog.makeInvisible(this.primitiveDataTypeRow);
					m_dialog.makeInvisible(this.structuredDataTypeRow);
					m_dialog.makeInvisible(this.documentTypeRow);
					m_dialog.makeVisible(this.otherTypeRow);

					this.otherTypeName.empty();

					var extension = m_extensionManager.findExtensions(
							"dataType", "id", dataType)[0];

					if (extension) {
						this.otherTypeName
								.append(m_i18nUtils
										.getProperty(
												"modeler.propertyPages.commonProperties.infoMessage.elementNotSupported")
										.replace(
												"{0}",
												"<b>" + extension.readableName + "</b>"));
					}
				};

				/**
				 *
				 */
				DataTypeSelector.prototype.enable = function() {
					this.dataTypeSelect.removeAttr("disabled");
					this.primitiveDataTypeSelect.removeAttr("disabled");
					this.structuredDataTypeSelect.removeAttr("disabled");
					this.documentTypeSelect.removeAttr("disabled");

					if (this.otherTypeRow != null) {
						this.otherTypeRow.removeAttr("disabled");
					}
				};

				/**
				 *
				 */
				DataTypeSelector.prototype.disable = function() {
					this.dataTypeSelect.attr("disabled", true);
					this.primitiveDataTypeSelect.attr("disabled", true);
					this.structuredDataTypeSelect.attr("disabled", true);
					this.documentTypeSelect.attr("disabled", true);

					if (this.otherTypeSelect != null) {
						this.otherTypeSelect.attr("disabled", true);
					}
				};

				/**
				 *
				 */
				DataTypeSelector.prototype.submitChanges = function() {
					if (this.submitHandler) {

						var structTypeFullId;
						var primitiveDataType;
						if (m_constants.PRIMITIVE_DATA_TYPE == this.dataTypeSelect
								.val()) {
							primitiveDataType =this.primitiveDataTypeSelect.val();
							if(this.isEnumTypeDeclaration(primitiveDataType)){
								structTypeFullId = this.primitiveDataTypeSelect.val();
							}
						} else if (m_constants.STRUCTURED_DATA_TYPE == this.dataTypeSelect
								.val()) {
							structTypeFullId = this.structuredDataTypeSelect
									.val();
						} else if (m_constants.DOCUMENT_DATA_TYPE == this.dataTypeSelect
								.val()) {
							structTypeFullId = this.documentTypeSelect.val();
						}

						// TODO Check for changes?
						this.submitHandler.submitDataChanges({
							dataType : this.dataTypeSelect.val(),
							primitiveDataType : primitiveDataType,
							structuredDataTypeFullId : structTypeFullId
						});
					}
				};
			}
		});