/**
 * Utility functions for dialog programming.
 * 
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_extensionManager", "m_model", "m_dialog" ],
		function(m_utils, m_constants, m_extensionManager, m_model, m_dialog) {
			return {
				create : function(scope, submitHandler) {
					var panel = new DataTypeSelector();

					panel.initialize(scope, submitHandler);

					return panel;
				}
			};

			/**
			 * 
			 */
			function DataTypeSelector() {
				/**
				 * 
				 */
				DataTypeSelector.prototype.initialize = function(scope,
						submitHandler) {
					this.submitHandler = submitHandler;
					this.dataTypeSelect = jQuery("#" + scope
							+ " #dataTypeSelect");
					this.primitiveDataTypeRow = jQuery("#" + scope
							+ " #primitiveDataTypeRow");
					this.primitiveDataTypeSelect = jQuery("#" + scope
							+ " #primitiveDataTypeSelect");
					this.structuredDataTypeRow = jQuery("#" + scope
							+ " #structuredDataTypeRow");
					this.structuredDataTypeSelect = jQuery("#" + scope
							+ " #structuredDataTypeSelect");
					this.documentTypeSelect = jQuery("#" + scope
							+ " #documentTypeSelect");
					this.documentTypeRow = jQuery("#" + scope
							+ " #documentTypeRow");
					this.otherTypeRow = jQuery("#" + scope + " #otherTypeRow");
					this.otherTypeName = jQuery("#" + scope + " #otherTypeName");

					this.dataTypeSelect.change({
						"panel" : this
					}, function(event) {
						event.data.panel.setDataType({
							dataType : event.data.panel.dataTypeSelect.val()
						});
					});
				};

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

					this.structuredDataTypeSelect
							.append("<optgroup label=\"This Model\"></optgroup>");

					for ( var i in this.scopeModel.structuredDataTypes) {
						this.structuredDataTypeSelect.append("<option value='"
								+ this.scopeModel.structuredDataTypes[i]
										.getFullId() + "'>"
								+ this.scopeModel.structuredDataTypes[i].name
								+ "</option>");
					}

					this.structuredDataTypeSelect
							.append("</optgroup><optgroup label=\"Other Models\">");

					for ( var n in m_model.getModels()) {
						if (m_model.getModels()[n] == this.scopeModel) {
							continue;
						}

						for ( var m in m_model.getModels()[n].structuredDataTypes) {
							this.structuredDataTypeSelect
									.append("<option value='"
											+ m_model.getModels()[n].structuredDataTypes[m]
													.getFullId()
											+ "'>"
											+ m_model.getModels()[n].name
											+ "/"
											+ m_model.getModels()[n].structuredDataTypes[m].name
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
							.append("<option value='GENERIC_DOCUMENT_TYPE'>(Generic Document)</option>");

					this.documentTypeSelect
							.append("<optgroup label=\"This Model\"></optgroup>");

					for ( var i in this.scopeModel.structuredDataTypes) {
						this.documentTypeSelect.append("<option value='"
								+ this.scopeModel.structuredDataTypes[i]
										.getFullId() + "'>"
								+ this.scopeModel.structuredDataTypes[i].name
								+ "</option>");
					}

					this.documentTypeSelect
							.append("</optgroup><optgroup label=\"Other Models\">");

					for ( var n in m_model.getModels()) {
						if (m_model.getModels()[n] == this.scopeModel) {
							continue;
						}

						for ( var m in m_model.getModels()[n].structuredDataTypes) {
							this.documentTypeSelect
									.append("<option value='"
											+ m_model.getModels()[n].structuredDataTypes[m]
													.getFullId()
											+ "'>"
											+ m_model.getModels()[n].name
											+ "/"
											+ m_model.getModels()[n].structuredDataTypes[m].name
											+ "</option>");
						}
					}

					this.documentTypeSelect.append("</optgroup>");
				};

				/**
				 * 
				 */
				DataTypeSelector.prototype.setDataType = function(data) {
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
						this.dataTypeSelect.val("others");
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

					this.submitChanges();
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

					this.submitChanges();
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

					this.submitChanges();
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
				DataTypeSelector.prototype.submitChanges = function() {
					if (this.submitHandler) {
						// TODO Check for changes?
						this.submitHandler
								.submitDataChanges({
									dataType : this.dataTypeSelect.val(),
									primitiveDataType : this.primitiveDataTypeSelect
											.val(),
									structuredDataTypeFullId : this.structuredDataTypeSelect
											.val()
								});
					}
				};
			}
		});