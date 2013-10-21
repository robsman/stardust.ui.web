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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_communicationController", "bpm-modeler/js/m_command",
				"bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_dialog", "bpm-modeler/js/m_typeDeclaration", "bpm-modeler/js/m_structuredTypeBrowser","bpm-modeler/js/m_i18nUtils",
				"bpm-modeler/js/m_messageDisplay", "bpm-modeler/js/m_urlUtils" ],
		function(m_utils, m_constants, m_communicationController, m_command,
				m_commandsController, m_model,
				m_dialog, m_typeDeclaration, m_structuredTypeBrowser, m_i18nUtils, m_messageDisplay, m_urlUtils) {
			return {
				initialize : function() {
					var wizard = new ImportTypeDeclarationsWizard();
					i18importtypeproperties();

					wizard.initialize(payloadObj.model);

				}
			};


			function i18importtypeproperties() {

				m_utils.jQuerySelect("#titleText")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.panel"));
				m_utils.jQuerySelect("#dialogCloseIcon").attr("title",
						m_i18nUtils.getProperty("modeler.common.value.close"));
				m_utils.jQuerySelect("#import")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.text"));
				m_utils.jQuerySelect("#importMessage")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.message"));
				m_utils.jQuerySelect("#url")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.url"));
				m_utils.jQuerySelect("#loadFromUrlButton")
						.attr(
								"value",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.load"));

				m_utils.jQuerySelect("#dataStructElement")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.heading.dataStructureElemnets"));
				m_utils.jQuerySelect("#structureDefinitionHintPanel")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.definitionPanel"));
				m_utils.jQuerySelect("#select")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.checkbox"));
				m_utils.jQuerySelect("#elementColumn")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.propertyView.elementTable.column.element.name"));
				m_utils.jQuerySelect("#typeColumn")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.type"));
				m_utils.jQuerySelect("#cardinalityColumn")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.cardinality"));
				m_utils.jQuerySelect("#importButton")
						.attr(
								"value",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.import"));
				m_utils.jQuerySelect("#cancelButton")
						.attr(
								"value",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.cancel"));

			}
			/**
			 *
			 */
			function ImportTypeDeclarationsWizard() {
				this.tree = m_utils.jQuerySelect("#typeDeclarationsTable");
				this.tableBody = m_utils.jQuerySelect("tbody", this.tree);
				this.urlTextInput = m_utils.jQuerySelect("#urlTextInput");
				this.loadFromUrlButton = m_utils.jQuerySelect("#loadFromUrlButton");
				this.importButton = m_utils.jQuerySelect("#importButton");
				this.cancelButton = m_utils.jQuerySelect("#cancelButton");
				this.closeButton = m_utils.jQuerySelect("#dialogCloseIcon");
				this.selectAllCheckbox = m_utils.jQuerySelect("#selectAllCheckbox");
				this.selectAll = false;

				var view = this;
				this.loadFromUrlButton.click(function(event) {
					view.loadFromUrl();
				});

				this.importButton.click(function(event) {
					view.performImport();
					closePopup();
				});

				this.cancelButton.click(function(event) {
					closePopup();
				});

				this.closeButton.click(function(event) {
					closePopup();
				});

				/**
				 *
				 */
				this.selectAllCheckbox.click(function(event) {
					view.selectAll = !view.selectAll;
					m_utils.jQuerySelect("table#typeDeclarationsTable tbody tr.top-level")
						.each(function() {
							m_utils.jQuerySelect(this).toggleClass("selected", view.selectAll);
					});
				});

				/**
				 *
				 */
				ImportTypeDeclarationsWizard.prototype.initialize = function(model) {
					this.model = model;

					this.tree.tableScroll({
						height : 300
					});
					this.tree.treeTable();
				};

				/**
				 * <code>structure</code> allows to pass a structure if no
				 * structure cannot be retrieved from the server.
				 */
				ImportTypeDeclarationsWizard.prototype.loadFromUrl = function(structure) {

					if (!this.urlTextInput.val()) {
						this.urlTextInput.addClass("error");
						m_messageDisplay
								.showErrorMessage(m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.errorMessage.emptyURL"));
						return;
					} else if (!m_urlUtils.validate(this.urlTextInput.val())) {
						this.urlTextInput.addClass("error");
						m_messageDisplay
								.showErrorMessage(m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.errorMessage.invalidURL"));
						return;
					}
					m_utils.jQuerySelect("body").css("cursor", "progress");
					// this.clearErrorMessages();
					this.urlTextInput.removeClass("error");

					var view = this;

					m_communicationController
							.syncPostData(
									{
										url : m_communicationController
												.getEndpointUrl()
												+ "/typeDeclarations/loadFromUrl"
									},
									JSON.stringify({
										url : this.urlTextInput.val()
									}),
									{
										"success" : function(serverData) {
											m_messageDisplay.clearAllMessages();
											view.urlTextInput.removeClass("error");
											jQuery.proxy(view.setSchema, view)(serverData);
											m_utils.jQuerySelect("body").css("cursor", "auto");
										},
										"error" : function() {
											m_utils.jQuerySelect("body").css("cursor", "auto");
											if (structure == null) {
												m_messageDisplay
														.showErrorMessage(m_i18nUtils
																.getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.errorMessage.xsdLoadFailed"));
												view.urlTextInput
														.addClass("error");
											} else {
												jQuery.proxy(view.setSchema, view)(structure);
											}
										}
									});
				};
				
				/**
				 *
				 */
				ImportTypeDeclarationsWizard.prototype.setSchema = function(schema) {
					m_utils.showWaitCursor();
					var timeStart = new Date().getTime();
					this.schema = schema;
					m_utils.debug("===> Type Declarations");
					m_utils.debug(schema);

					if (this.tableBody) {
						this.tableBody.remove();
					}
					this.tableBody = document.createElement("tbody");
					
					for ( var name in this.schema.elements) {
						var element = this.schema.elements[name];

						var path = "element-" + name.replace(/:/g, "-");

						var elementType = element.type ? element.type : element.name;

						var schemaType = m_typeDeclaration.resolveSchemaTypeFromSchema(elementType, schema);
						var row = m_structuredTypeBrowser.generateChildElementRow("element-", element, schemaType,
								function(row, element, schemaType) {
							m_utils.jQuerySelect("<td><span class='data-element'>" + element.name + "</span></td>").appendTo(row);
							m_utils.jQuerySelect("<td>" + elementType + "</td>").appendTo(row);
							m_utils.jQuerySelect("<td></td>").appendTo(row);
						});

						row.data("element", element);

						row.addClass("top-level");
						
						var view = this;
						jQuery.each(row, function() {
							view.tableBody.appendChild(this);
						});
						
						// drill into elements, too (requires element's schemaType, see above)
						if (schemaType) {
							m_structuredTypeBrowser.insertChildElementRowsEagerly(row);
						}
					}
					var view = this;
					//check if xsd contains any complex types
					if (this.schema.types) {
						jQuery.each(this.schema.types, function(i, type) {
							var schemaType = m_typeDeclaration.resolveSchemaTypeFromSchema(type.name, view.schema);

							var path = "type-" + type.name.replace(/:/g, "-");

							var row = m_structuredTypeBrowser.generateChildElementRow("type-", type, schemaType,
									function(row, element, schemaType) {

								m_utils.jQuerySelect("<td><span class='data-element'>" + type.name + "</span></td>").appendTo(row);
								m_utils.jQuerySelect("<td>" + type.name + "</td>").appendTo(row);
								m_utils.jQuerySelect("<td></td>").appendTo(row);

								row.data("typeDeclaration", type);
							});
							row.addClass("top-level");

							jQuery.each(row, function() {
								view.tableBody.appendChild(this);
							});

							m_structuredTypeBrowser.insertChildElementRowsEagerly(row);
						});
					}
					
					this.tree.append(this.tableBody);

					this.tree.tableScroll({
						height : 170
					});
					// TODO - hack
					// The table scroll plugin sets height to auto if the
					// initial height is less than the provided height
					// settig max-height in the plugin should also work.
					m_utils.jQuerySelect("div.tablescroll_wrapper").css("max-height", "170px");
					this.tree.treeTable({
						indent: 14,
						onNodeShow: function() {
							m_structuredTypeBrowser.insertChildElementRowsLazily(m_utils.jQuerySelect(this));
						}
					});

					m_utils.jQuerySelect("table#typeDeclarationsTable tbody tr.top-level").mousedown(function() {
						// allow multi-select, but restrict to top-level entries
						m_utils.jQuerySelect(this).toggleClass("selected");
					});
					var timeEnd = new Date().getTime();
					m_utils.debug("Total Time to render the tree===> " + (timeEnd - timeStart));
					m_utils.hideWaitCursor();
				};

				/**
				 *
				 */
				ImportTypeDeclarationsWizard.prototype.performImport = function() {

					// collect selected types
					var typeDeclarations = [];
					var elements = [];
					m_utils.jQuerySelect("tr.selected", this.tableBody).each(function() {
						var row = m_utils.jQuerySelect(this);
						var typeDeclaration = row.data("typeDeclaration");
						if (typeDeclaration) {
							typeDeclarations.push(typeDeclaration);
						} else if (row.data("element")) {
							elements.push(row.data("element"));
						}
					});

					var view = this;
					jQuery.each(typeDeclarations, function() {
						var xref = view.schema.targetNamespace ? "{" + view.schema.targetNamespace + "}" + this.name : this.name;
						m_commandsController.submitCommand(
								m_command.createCreateTypeDeclarationCommand(
										view.model.id,
										view.model.id,
										{
										    // must keep the original name as ID as otherwise the type can't be resolved eventually
											"id": this.name,
											"name": this.name,
											"typeDeclaration" : {
												type: {
													classifier: "ExternalReference",
													location: view.urlTextInput.val(),
													xref: xref
												}
											}
										}));
					});

					//TODO: do we required separate treatment for elements? Elements and Type Declaration processing appears same.
					jQuery.each(elements, function() {
						var xref = view.schema.targetNamespace ? "{" + view.schema.targetNamespace + "}" + this.name : this.name;
						m_commandsController.submitCommand(
								m_command.createCreateTypeDeclarationCommand(
										view.model.id,
										view.model.id,
										{
										    // must keep the original name as ID as otherwise the type can't be resolved eventually
											"id": this.name,
											"name": this.name,
											"typeDeclaration" : {
												type: {
													classifier: "ExternalReference",
													location: view.urlTextInput.val(),
													xref: xref
												}
											}
										}));
					});
				};
			}
			;
		});