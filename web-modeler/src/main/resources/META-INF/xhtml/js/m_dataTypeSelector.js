/**
 * Utility functions for dialog programming.
 * 
 * @author Marc.Gille
 */
define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_extensionManager", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_dialog", "bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_extensionManager, m_model, m_dialog,
				m_i18nUtils) {
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
				/**
				 * Options are
				 * 
				 * scope submitHandler supportsOtherData
				 */
				DataTypeSelector.prototype.initialize = function(options) {
					this.scope = options.scope;
					this.submitHandler = options.submitHandler;
					this.supportsOtherData = options.supportsOtherData;

					// TODO - not sure what the purpose of this flag is
					// setting it to true by default to make things work as
					// before
					// needs to be set appropriately where required.
					this.supportDocumentTypes = true;

					this.dataTypeSelect = jQuery("#" + this.scope
							+ " #dataTypeSelect");
					this.primitiveDataTypeRow = jQuery("#" + this.scope
							+ " #primitiveDataTypeRow");
					this.primitiveDataTypeSelect = jQuery("#" + this.scope
							+ " #primitiveDataTypeSelect");
					this.structuredDataTypeRow = jQuery("#" + this.scope
							+ " #structuredDataTypeRow");
					this.structuredDataTypeSelect = jQuery("#" + this.scope
							+ " #structuredDataTypeSelect");
					this.documentTypeSelect = jQuery("#" + this.scope
							+ " #documentTypeSelect");
					this.documentTypeRow = jQuery("#" + this.scope
							+ " #documentTypeRow");
					this.otherTypeRow = jQuery("#" + this.scope
							+ " #otherTypeRow");
					this.otherTypeName = jQuery("#" + this.scope
							+ " #otherTypeName");

					this.initializeDataTypeOptions();

					var propertiesData = m_i18nUtils
							.getProperty("modeler.element.properties.commonProperties.primitive");

					this.dataTypeSelect.append("<option value='primitive'>"
							+ propertiesData + "</option>");
					propertiesData = m_i18nUtils
							.getProperty("modeler.element.properties.commonProperties.dataStructure");
					this.dataTypeSelect.append("<option value='struct'>"
							+ propertiesData + "</option>");

					if (options.supportDocumentTypes) {
						propertiesData = m_i18nUtils
								.getProperty("modeler.element.properties.commonProperties.document");
						this.dataTypeSelect
								.append("<option value='dmsDocument'>"
										+ propertiesData + "</option>");
					}

					if (this.supportsOtherData) {
						propertiesData = m_i18nUtils
								.getProperty("modeler.element.properties.commonProperties.other");
						4
						this.dataTypeSelect.append("<option value='other'>"
								+ propertiesData + "</option>");
						// this.dataTypeSelect.append("<option
						// value='other'>Other</option>");
					}

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
										event.data.panel
												.setStructuredDataType(event.data.panel.structuredDataTypeSelect
														.val());
										event.data.panel.submitChanges();
									});
					this.documentTypeSelect
							.change(
									{
										panel : this
									},
									function(event) {
										event.data.panel
												.setDocumentDataType(event.data.panel.documentTypeSelect
														.val());
										event.data.panel.submitChanges();
									});
				};

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

					if (this.supportDocumentTypes) {
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

					this.populateDataStructuresSelectInput();
					this.populateDocumentTypesSelectInput();
				};

				/**
				 * 
				 */
				DataTypeSelector.prototype.populateDataStructuresSelectInput = function() {
					this.structuredDataTypeSelect.empty();
					this.structuredDataTypeSelect.append("<option value='"
							+ m_constants.TO_BE_DEFINED
							+ "'>(To be defined)</option>");

					if (this.scopeModel) {
						this.structuredDataTypeSelect
								.append("<optgroup label=\"This Model\">");

						for ( var i in this.scopeModel.typeDeclarations) {
							this.structuredDataTypeSelect
									.append("<option value='"
											+ this.scopeModel.typeDeclarations[i]
													.getFullId()
											+ "'>"
											+ this.scopeModel.typeDeclarations[i].name
											+ "</option>");
						}
					}

					this.structuredDataTypeSelect
							.append("</optgroup><optgroup label=\"Other Models\">");

					for ( var n in m_model.getModels()) {
						if (this.scopeModel
								&& m_model.getModels()[n] == this.scopeModel) {
							continue;
						}

						for ( var m in m_model.getModels()[n].typeDeclarations) {
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

					this.structuredDataTypeSelect.append("</optgroup>");
				};

				/**
				 * 
				 */
				DataTypeSelector.prototype.populateDocumentTypesSelectInput = function() {
					this.documentTypeSelect.empty();
					this.documentTypeSelect.append("<option value='"
							+ m_constants.TO_BE_DEFINED
							+ "'>(To be defined)</option>");

					this.documentTypeSelect
							.append("<optgroup label=\"This Model\">");

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

					this.documentTypeSelect
							.append("</optgroup><optgroup label=\"Other Models\">");

					for ( var n in m_model.getModels()) {
						if (this.scopeModel
								&& m_model.getModels()[n] == this.scopeModel) {
							continue;
						}

						for ( var m in m_model.getModels()[n].typeDeclarations) {
							// Only composite structured types and not
							// enumerations are listed here
							if (m_model.getModels()[n].typeDeclarations[m]
									.isSequence()) {
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
						this
								.setStructuredDataType(data.structuredDataTypeFullId);
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
				DataTypeSelector.prototype.getDataType = function(data) {
					data.dataType = this.dataTypeSelect.val();

					if (this.dataTypeSelect.val() == m_constants.PRIMITIVE_DATA_TYPE) {
						data.primitiveDataType = this.primitiveDataTypeSelect
								.val();
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
						primitiveDataType) {
					if (primitiveDataType == null) {
						primitiveDataType = "String";
					}

					this.primitiveDataTypeSelect.val(primitiveDataType);

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

					this.otherTypeName
							.append("<b>"
									+ extension.readableName
									+ "</b> not yet supported for the Browser Modeler.");
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
						if (m_constants.STRUCTURED_DATA_TYPE == this.dataTypeSelect
								.val()) {
							structTypeFullId = this.structuredDataTypeSelect
									.val()
						} else if (m_constants.DOCUMENT_DATA_TYPE == this.dataTypeSelect
								.val()) {
							structTypeFullId = this.documentTypeSelect.val()
						}

						// TODO Check for changes?
						this.submitHandler.submitDataChanges({
							dataType : this.dataTypeSelect.val(),
							primitiveDataType : this.primitiveDataTypeSelect
									.val(),
							structuredDataTypeFullId : structTypeFullId
						});
					}
				};
			}
		});