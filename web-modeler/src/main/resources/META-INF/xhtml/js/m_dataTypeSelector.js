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
				"bpm-modeler/js/m_messageDisplay", "bpm-modeler/js/m_modelerUtils"],
		function(m_utils, m_constants, m_extensionManager, m_model, m_dialog,
				m_i18nUtils, m_modelElementUtils, m_messageDisplay, m_modelerUtils) {
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
					this.enableOpenTypeDeclarationLink = options.enableOpenTypeDeclarationLink;

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

					if (this.enableOpenTypeDeclarationLink) {
						this.structuredDataTypeRow.append('<td><img id="typeDeclarationViewLink" style="padding-left: 5px;" class="imgLink imgLinkDisabled" src="plugins/bpm-modeler/images/icons/arrow.png" /></td>');
						this.typeDeclarationViewLink = m_utils.jQuerySelect(this.checkElementId(this.scope)+ " #typeDeclarationViewLink");
						this.typeDeclarationViewLink.click({
							panel : this
						}, function(event) {
							event.data.panel.openTypeDeclarationView();
						});
					}
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
					var dataTypes = [];
					dataTypes.push({type: "primitive", name:  m_i18nUtils.getProperty("modeler.element.properties.commonProperties.primitive")});
					dataTypes.push({type: "struct", name:  m_i18nUtils.getProperty("modeler.element.properties.commonProperties.structureData")});
					
					if (this.supportsDocumentTypes) {
					   dataTypes.push({type: "dmsDocument", name:  m_i18nUtils.getProperty("modeler.element.properties.commonProperties.document")});
					}
					
					var self = this;
					this.dataTypeSelect.empty();					
					jQuery.each(m_utils.convertToSortedArray(dataTypes, "name", true), function() {
						var dataType = this;
						self.dataTypeSelect.append("<option value=" + dataType.type + ">"
								+ dataType.name + "</option>");
					});

					if (this.supportsOtherData) {
						var propertiesData = m_i18nUtils
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
					
					var dataTypes = [];					
					dataTypes.push({type: "String", name: m_i18nUtils.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.string")});
					dataTypes.push({type: "boolean", name: m_i18nUtils.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.boolean")});
					dataTypes.push({type: "int", name: m_i18nUtils.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.int")});
					dataTypes.push({type: "long", name: m_i18nUtils.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.long")});
					dataTypes.push({type: "double", name: m_i18nUtils.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.double")});
					dataTypes.push({type: "Timestamp", name: m_i18nUtils.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.timestamp")});
					
					var self = this;
					jQuery.each(m_utils.convertToSortedArray(dataTypes, "name", true), function() {
						var dataType = this;
						self.primitiveDataTypeSelect.append("<option value=" + dataType.type + " title=" + dataType.type + ">" + dataType.name
								+ "</option>");
					});

					if (this.scopeModel && this.hideEnumerations != true) {
						this.primitiveDataTypeSelect
								.append("<optgroup label='" + m_i18nUtils.getProperty("modeler.enum.thisModel") + "'>");

						var typeDeclarationsSorted = m_utils.convertToSortedArray(this.scopeModel.typeDeclarations, "name", true);
						for ( var i in typeDeclarationsSorted) {
							if (typeDeclarationsSorted[i].isSequence()) continue;
							if (typeDeclarationsSorted[i].isEnumeration()){
								this.primitiveDataTypeSelect
								.append("<option value='" + typeDeclarationsSorted[i].getFullId() + "'>"
										+ typeDeclarationsSorted[i].name
										+ "</option>");
							}

						}
					}

					if (!this.restrictToCurrentModel && this.hideEnumerations != true) {
						this.primitiveDataTypeSelect
								.append("</optgroup><optgroup label='" + m_i18nUtils.getProperty("modeler.enum.otherModels") + "'>");

						var modelsSorted = m_utils.convertToSortedArray(m_model.getModels(), "name", true);
						for ( var n in modelsSorted) {
							if (this.scopeModel
									&& modelsSorted[n] == this.scopeModel) {
								continue;
							}

							var typeDeclarationsSorted = m_utils.convertToSortedArray(modelsSorted[n].typeDeclarations, "name", true);
							for ( var m in typeDeclarationsSorted) {
								if (m_modelElementUtils
										.hasPublicVisibility(typeDeclarationsSorted[m])) {

									if (typeDeclarationsSorted[m].isSequence()) continue;
										if (typeDeclarationsSorted[m]
												.getType() == "enumStructuredDataType") {
											this.primitiveDataTypeSelect
													.append("<option value='"+typeDeclarationsSorted[m].getFullId()+"'>"
															+ modelsSorted[n].name
															+ "/"
															+ typeDeclarationsSorted[m].name
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

						var typeDeclarationsSorted = m_utils.convertToSortedArray(this.scopeModel.typeDeclarations, "name", true);
						for ( var i in typeDeclarationsSorted) {
							if (!typeDeclarationsSorted[i].isSequence()) continue;
							// Enum data is shown under primitive
							if (typeDeclarationsSorted[i].getType() != "enumStructuredDataType") {
							this.structuredDataTypeSelect
									.append("<option value='"
											+ typeDeclarationsSorted[i]
													.getFullId()
											+ "'>"
											+ typeDeclarationsSorted[i].name
											+ "</option>");
							}
						}
					}

					if (!this.restrictToCurrentModel) {
						this.structuredDataTypeSelect
								.append("</optgroup><optgroup label='" + m_i18nUtils.getProperty("modeler.general.otherModels") + "'>");

						var modelsSorted = m_utils.convertToSortedArray(m_model.getModels(), "name", true);
						for ( var n in modelsSorted) {
							if (this.scopeModel
									&& modelsSorted[n] == this.scopeModel) {
								continue;
							}

							var typeDeclarationsSorted = m_utils.convertToSortedArray(modelsSorted[n].typeDeclarations, "name", true);
							for ( var m in typeDeclarationsSorted) {
								if (m_modelElementUtils
										.hasPublicVisibility(typeDeclarationsSorted[m])) {
									if (!typeDeclarationsSorted[m].isSequence()) continue;
									// Enum data is shown under primitive
									if (typeDeclarationsSorted[m]
											.getType() != "enumStructuredDataType") {
										this.structuredDataTypeSelect
										.append("<option value='"
												+ typeDeclarationsSorted[m]
														.getFullId()
												+ "'>"
												+ modelsSorted[n].name
												+ "/"
												+ typeDeclarationsSorted[m].name
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

					var typeDeclarationsSorted = m_utils.convertToSortedArray(this.scopeModel.typeDeclarations, "name", true);
					if (this.scopeModel) {
						for ( var i in typeDeclarationsSorted) {
							// Only composite structured types and not
							// enumerations
							// are listed here
							if (typeDeclarationsSorted[i]
									.isSequence()) {
								this.documentTypeSelect
										.append("<option value='"
												+ typeDeclarationsSorted[i]
														.getFullId()
												+ "'>"
												+ typeDeclarationsSorted[i].name
												+ "</option>");
							}
						}
					}

					if (!this.restrictToCurrentModel) {
						this.documentTypeSelect
								.append("</optgroup><optgroup label='" + m_i18nUtils.getProperty("modeler.general.otherModels") + "'>");

						var modelsSorted = m_utils.convertToSortedArray(m_model.getModels(), "name", true);
						for ( var n in modelsSorted) {
							if (this.scopeModel
									&& modelsSorted[n] == this.scopeModel) {
								continue;
							}

							var typeDeclarationsSorted = m_utils.convertToSortedArray(modelsSorted[n].typeDeclarations, "name", true);
							for ( var m in typeDeclarationsSorted) {
								// Only composite structured types (with public
								// visibility) and not
								// enumerations are listed here
								if (typeDeclarationsSorted[m]
										.isSequence()
										&& m_modelElementUtils
												.hasPublicVisibility(typeDeclarationsSorted[m])) {
									this.documentTypeSelect
											.append("<option value='"
													+ typeDeclarationsSorted[m]
															.getFullId()
													+ "'>"
													+ modelsSorted[n].name
													+ "/"
													+ typeDeclarationsSorted[m].name
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
						var typeDeclaration = m_model.findModel(m_model
								.stripModelId(fullId)).typeDeclarations[m_model
								.stripElementId(fullId)];
						return typeDeclaration ? typeDeclaration
								.isEnumeration() : false;
					} catch (e) {
						return false;
					}

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
					if (this.enableOpenTypeDeclarationLink) {
						// TODO - find a better way
						if (structuredDataTypeFullId
								&& structuredDataTypeFullId.indexOf(":") != (structuredDataTypeFullId.length - 1)) {
							this.typeDeclarationViewLink
									.removeClass("imgLinkDisabled");
						}
					}

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

				/**
				 *
				 */
				DataTypeSelector.prototype.openTypeDeclarationView = function() {
					var fullId = this.structuredDataTypeSelect.val();
					if (fullId && fullId !== m_constants.TO_BE_DEFINED) {
						var typeDeclaration = m_model.findTypeDeclaration(fullId);
						m_modelerUtils.openTypeDeclarationView(typeDeclaration);
					}
				};
			}
		});