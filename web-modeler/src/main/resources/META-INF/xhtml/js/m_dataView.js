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
		[ "m_utils", "m_constants", "m_extensionManager", "m_command", "m_commandsController",
				"m_dialog", "m_modelElementView", "m_model",
				"m_typeDeclaration" ],
		function(m_utils, m_constants, m_extensionManager, m_command, m_commandsController,
				m_dialog, m_modelElementView, m_model, m_typeDeclaration) {
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
				// Inheritance

				var view = m_modelElementView.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(DataView.prototype, view);

				/**
				 * 
				 */
				DataView.prototype.initialize = function(data) {
					this.data = data;

					this.initializeModelElementView();

					this.primitiveInput = jQuery("#primitiveInput");
					this.primitiveList = jQuery("#primitiveList");
					this.dataStructureInput = jQuery("#dataStructureInput");
					this.dataStructureList = jQuery("#dataStructureList");
					this.documentInput = jQuery("#documentInput");
					this.documentTypeList = jQuery("#documentTypeList");
					this.otherTypeInput = jQuery("#otherTypeInput");
					this.otherTypeName = jQuery("#otherTypeName");

					this.primitiveInput
							.click(
									{
										"view" : this
									},
									function(event) {
										if (event.data.view.primitiveInput
												.is(":checked")) {
											event.data.view
													.setPrimitiveDataType(m_constants.STRING_PRIMITIVE_DATA_TYPE);
										}
									});
					this.dataStructureInput
							.click(
									{
										"view" : this
									},
									function(event) {
										if (event.data.view.dataStructureInput
												.is(":checked")) {
											event.data.view
													.setStructuredDataType(m_constants.TO_BE_DEFINED);
										}
									});
					this.documentInput
							.click(
									{
										"view" : this
									},
									function(event) {
										if (event.data.view.documentInput
												.is(":checked")) {
											event.data.view
													.setDocumentDataType(m_constants.TO_BE_DEFINED);
										}
									});
					this.populateDataStructuresSelectInput();
					this.populateDocumentTypesSelectInput();
					this.initializeModelElement(data);

					if (this.data.dataType == m_constants.PRIMITIVE_DATA_TYPE) {
						this.setPrimitiveDataType(this.data.primitiveDataType);
					} else if (this.data.dataType == m_constants.STRUCTURED_DATA_TYPE) {
						this
								.setStructuredDataType(this.data.structuredDataTypeFullId);
					} else if (this.data.dataType == m_constants.DOCUMENT_DATA_TYPE) {
						this
								.setDocumentDataType(this.data.structuredDataTypeFullId);
					} else {
						this.setOtherDataType(this.data.dataType);
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
				DataView.prototype.populateDataStructuresSelectInput = function() {
					this.dataStructureList.empty();
					this.dataStructureList.append("<option value='"
							+ m_constants.TO_BE_DEFINED
							+ "'>(To be defined)</option>");

					for ( var n in m_model.getModels()) {
						for ( var m in m_model.getModels()[n].structuredDataTypes) {
							this.dataStructureList
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
				};

				/**
				 * 
				 */
				DataView.prototype.populateDocumentTypesSelectInput = function() {
					this.documentTypeList.empty();
					this.documentTypeList.append("<option value='"
							+ m_constants.TO_BE_DEFINED
							+ "'>(To be defined)</option>");
					this.documentTypeList
							.append("<option value='GENERIC_DOCUMENT_TYPE'>(Generic Document)</option>");

					for ( var n in m_model.getModels()) {
						for ( var m in m_model.getModels()[n].structuredDataTypes) {
							this.documentTypeList
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
				DataView.prototype.setPrimitiveDataType = function(
						primitiveDataType) {
					this.primitiveInput.attr("checked", true);
					this.primitiveList.removeAttr("disabled");
					this.primitiveList.val(primitiveDataType);
					this.dataStructureInput.attr("checked", false);
					this.dataStructureList.attr("disabled", true);
					this.documentInput.attr("checked", false);
					this.documentTypeList.attr("disabled", true);
					this.otherTypeInput.attr("checked", false);
					this.otherTypeInput.attr("disabled", true);
				};

				/**
				 * 
				 */
				DataView.prototype.setStructuredDataType = function(
						structuredDataTypeFullId) {
					this.dataStructureInput.attr("checked", true);
					this.dataStructureList.removeAttr("disabled");
					this.dataStructureList.val(structuredDataTypeFullId);
					this.primitiveInput.attr("checked", false);
					this.primitiveList.attr("disabled", true);
					this.documentInput.attr("checked", false);
					this.documentTypeList.attr("disabled", true);
					this.otherTypeInput.attr("checked", false);
					this.otherTypeInput.attr("disabled", true);
				};

				/**
				 * 
				 */
				DataView.prototype.setDocumentDataType = function(
						documentDataTypeFullId) {
					this.primitiveInput.attr("checked", false);
					this.primitiveList.attr("disabled", true);
					this.dataStructureInput.attr("checked", false);
					this.dataStructureList.attr("disabled", true);
					this.documentInput.attr("checked", true);
					this.documentTypeList.removeAttr("disabled");
					this.documentTypeList.val(documentDataTypeFullId);
					this.otherTypeInput.attr("checked", false);
					this.otherTypeInput.attr("disabled", true);
				};

				/**
				 * 
				 */
				DataView.prototype.setOtherDataType = function(dataType) {
					this.primitiveInput.attr("checked", false);
					this.primitiveList.attr("disabled", true);
					this.dataStructureInput.attr("checked", false);
					this.dataStructureList.attr("disabled", true);
					this.documentInput.attr("checked", false);
					this.documentTypeList.attr("disabled", true);
					this.otherTypeInput.attr("checked", true);
					this.otherTypeName.empty();
					
					var extension = m_extensionManager.findExtensions(
							"dataType", "id", dataType)[0];
							
					this.otherTypeName.append("<b>" + extension.readableName + "</b> (Not yet supported for the Browser Modeler)");
				};

				/**
				 * 
				 */
				DataView.prototype.processCommand = function(command) {
					// Parse the response JSON from command pattern

					var object = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != object && null != object.changes
							&& null != object.changes.modified
							&& 0 != object.changes.modified.length
							&& object.changes.modified[0].oid == this.data.oid) {

						m_utils.inheritFields(this.data,
								object.changes.modified[0]);

						this.initialize(this.data);
					}
				};
			}
		});