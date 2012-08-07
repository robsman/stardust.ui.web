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
		[ "m_utils", "m_constants", "m_command", "m_commandsController", "m_basicPropertiesPage" ],
		function(m_utils, m_constants, m_command, m_commandsController, m_basicPropertiesPage) {
			return {
				create : function(propertiesPanel) {
					var page = new DataBasicPropertiesPage(propertiesPanel);
					
					page.initialize();
					
					return page;
				}
			};

			function DataBasicPropertiesPage(propertiesPanel) {
				// Inheritance

				var propertiesPage = m_basicPropertiesPage.create(propertiesPanel);

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(DataBasicPropertiesPage.prototype,
						propertiesPage);

				/**
				 * 
				 */
				DataBasicPropertiesPage.prototype.initialize = function() {	
					this.initializeBasicPropertiesPage();

					this.primitiveInput = this.mapInputId("primitiveInput");
					this.primitiveList = this.mapInputId("primitiveList");
					this.dataStructureInput = this.mapInputId("dataStructureInput");
					this.dataStructureList = this.mapInputId("dataStructureList");
					this.documentInput = this.mapInputId("documentInput");
					this.documentTypeList = this.mapInputId("documentTypeList");

					// Initialize callbacks

					this.primitiveInput
							.click(
									{
										"callbackScope" : this
									},
									function(event) {
										if (event.data.callbackScope.primitiveInput
												.is(":checked")) {
											event.data.callbackScope
													.setPrimitiveDataType(m_constants.STRING_PRIMITIVE_DATA_TYPE);
										}
									});

					this.dataStructureInput
							.click(
									{
										"callbackScope" : this
									},
									function(event) {
										if (event.data.callbackScope.dataStructureInput
												.is(":checked")) {
											event.data.callbackScope
													.setStructuredDataType(m_constants.TO_BE_DEFINED);
										}
									});

					this.documentInput
							.click(
									{
										"callbackScope" : this
									},
									function(event) {
										if (event.data.callbackScope.documentInput
												.is(":checked")) {
											event.data.callbackScope
													.setDocumentDataType(m_constants.TO_BE_DEFINED);
										}
									});
				};
				
				/**
				 * 
				 */
				DataBasicPropertiesPage.prototype.populateDataStructuresSelectInput = function() {
					this.dataStructureList.empty();
					this.dataStructureList.append("<option value='"
							+ m_constants.TO_BE_DEFINED
							+ "'>(To be defined)</option>");

					for ( var n in this.propertiesPanel.models) {
						for ( var m in this.propertiesPanel.models[n].structuredDataTypes) {
							this.dataStructureList
									.append("<option value='"
											+ this.propertiesPanel.models[n].structuredDataTypes[m]
													.getFullId()
											+ "'>"
											+ this.propertiesPanel.models[n].name
											+ "/"
											+ this.propertiesPanel.models[n].structuredDataTypes[m].name
											+ "</option>");
						}
					}
				};

				/**
				 * 
				 */
				DataBasicPropertiesPage.prototype.populateDocumentTypesSelectInput = function() {
					this.documentTypeList.empty();
					this.documentTypeList.append("<option value='"
							+ m_constants.TO_BE_DEFINED
							+ "'>(To be defined)</option>");
					this.documentTypeList
							.append("<option value='GENERIC_DOCUMENT_TYPE'>(Generic Document)</option>");

					for ( var n in this.propertiesPanel.models) {
						for ( var m in this.propertiesPanel.models[n].structuredDataTypes) {
							this.documentTypeList
									.append("<option value='"
											+ this.propertiesPanel.models[n].structuredDataTypes[m]
													.getFullId()
											+ "'>"
											+ this.propertiesPanel.models[n].name
											+ "/"
											+ this.propertiesPanel.models[n].structuredDataTypes[m].name
											+ "</option>");
						}
					}
				}

				/**
				 * 
				 */
				DataBasicPropertiesPage.prototype.setPrimitiveDataType = function(
						primitiveDataType) {
					this.primitiveInput.attr("checked", true);
					this.primitiveList.removeAttr("disabled");
					this.primitiveList.val(primitiveDataType);
					this.dataStructureInput.attr("checked", false);
					this.dataStructureList.attr("disabled", true);
					this.documentInput.attr("checked", false);
					this.documentTypeList.attr("disabled", true);
				};

				/**
				 * 
				 */
				DataBasicPropertiesPage.prototype.setStructuredDataType = function(
						structuredDataTypeFullId) {
					this.dataStructureInput.attr("checked", true);
					this.dataStructureList.removeAttr("disabled");
					this.dataStructureList.val(structuredDataTypeFullId);
					this.primitiveInput.attr("checked", false);
					this.primitiveList.attr("disabled", true);
					this.documentInput.attr("checked", false);
					this.documentTypeList.attr("disabled", true);
				};

				/**
				 * 
				 */
				DataBasicPropertiesPage.prototype.setDocumentDataType = function(
						documentDataTypeFullId) {
					this.primitiveInput.attr("checked", false);
					this.primitiveList.attr("disabled", true);
					this.dataStructureInput.attr("checked", false);
					this.dataStructureList.attr("disabled", true);
					this.documentInput.attr("checked", true);
					this.documentTypeList.removeAttr("disabled");
					this.documentTypeList.val(documentDataTypeFullId);
				};

				/**
				 * 
				 */
				DataBasicPropertiesPage.prototype.setElement = function() {
					this.setModelElement();

					this.populateDataStructuresSelectInput();
					this.populateDocumentTypesSelectInput();

					if (this.propertiesPanel.data.type == m_constants.PRIMITIVE_DATA_TYPE) {
						this
								.setPrimitiveDataType(this.propertiesPanel.data.primitiveDataType);
					} else if (this.propertiesPanel.data.type == m_constants.STRUCTURED_DATA_TYPE) {
						this
								.setStructuredDataType(this.propertiesPanel.data.structuredDataTypeFullId);
					} else if (this.propertiesPanel.data.type == m_constants.DOCUMENT_DATA_TYPE) {
						this
								.setDocumentDataType(this.propertiesPanel.data.structuredDataTypeFullId);
					}
				};

				/**
				 * 
				 */
				DataBasicPropertiesPage.prototype.validate = function() {
					this.propertiesPanel.clearErrorMessages();
					this.nameInput.removeClass("error");

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
						this.propertiesPanel.errorMessages
								.push("Data name must not be empty.");
						this.nameInput.addClass("error");

						this.propertiesPanel.showErrorMessages();

						return false;
					}

					return true;
				};

				/**
				 * 
				 */
				DataBasicPropertiesPage.prototype.apply = function() {
					if (this.primitiveInput.is(":checked")) {
						this.propertiesPanel.data.type = m_constants.PRIMITIVE_DATA_TYPE;
						if (this.primitiveList.val() == m_constants.TO_BE_DEFINED) {
							this.propertiesPanel.data.primitiveDataType = null;
						} else {
							this.propertiesPanel.data.primitiveDataType = this.primitiveList
									.val();
						}
					} else if (this.dataStructureInput.is(":checked")) {
						this.propertiesPanel.data.type = m_constants.STRUCTURED_DATA_TYPE;
						if (this.dataStructureList.val() == m_constants.TO_BE_DEFINED) {
							this.propertiesPanel.data.structuredDataFullId = null;
						} else {
							this.propertiesPanel.data.structuredDataTypeFullId = this.dataStructureList
									.val();
						}
					} else if (this.documentInput.is(":checked")) {
						this.propertiesPanel.data.type = m_constants.DOCUMENT_DATA_TYPE;
						if (this.documentTypeList.val() == m_constants.TO_BE_DEFINED) {
							this.propertiesPanel.data.structuredDataTypeFullId = null;
						} else {
							this.propertiesPanel.data.structuredDataTypeFullId = this.documentTypeList
									.val();
						}
					}
				};

				/**
				 * 
				 */
				DataBasicPropertiesPage.prototype.submitChanges = function(
						changes) {
					m_commandsController.submitCommand(m_command
							.createUpdateModelElementCommand(
									this.propertiesPanel.element.oid, changes,
									this.propertiesPanel.element));
				};

			}
		});