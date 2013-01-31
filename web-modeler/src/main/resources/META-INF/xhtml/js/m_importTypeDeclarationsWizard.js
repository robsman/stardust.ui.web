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
				"bpm-modeler/js/m_dialog", "bpm-modeler/js/m_typeDeclaration", "bpm-modeler/js/m_structuredTypeBrowser","bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_communicationController, m_command,
				m_commandsController, m_model,
				m_dialog, m_typeDeclaration, m_structuredTypeBrowser,m_i18nUtils) {
			return {
				initialize : function() {
					var wizard = new ImportTypeDeclarationsWizard();
					i18importtypeproperties();

					wizard.initialize(payloadObj.model);

				}
			};


			function i18importtypeproperties() {

				jQuery("#titleText")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.panel"));
				jQuery("#dialogCloseIcon").attr("title",
						m_i18nUtils.getProperty("modeler.common.value.close"));
				jQuery("#import")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.text"));
				jQuery("#importMessage")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.message"));
				jQuery("#url")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.url"));
				jQuery("#loadFromUrlButton")
						.attr(
								"value",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.load"));

				jQuery("#dataStructElement")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.heading.dataStructureElemnets"));
				jQuery("#structureDefinitionHintPanel")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.definitionPanel"));
				jQuery("#select")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.checkbox"));
				jQuery("#elementColumn")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.propertyView.elementTable.column.element.name"));
				jQuery("#typeColumn")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.type"));
				jQuery("#cardinalityColumn")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.cardinality"));
				jQuery("#importButton")
						.attr(
								"value",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.import"));
				jQuery("#cancelButton")
						.attr(
								"value",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.structuredTypes.importTypeDeclarations.cancel"));

			}
			/**
			 *
			 */
			function ImportTypeDeclarationsWizard() {
				this.tree = jQuery("#typeDeclarationsTable");
				this.tableBody = jQuery("tbody", this.tree);
				this.urlTextInput = jQuery("#urlTextInput");
				this.loadFromUrlButton = jQuery("#loadFromUrlButton");
				this.importButton = jQuery("#importButton");
				this.cancelButton = jQuery("#cancelButton");
				this.closeButton = jQuery("#dialogCloseIcon");
				this.selectAllCheckbox = jQuery("#selectAllCheckbox");
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
					jQuery("table#typeDeclarationsTable tbody tr.top-level")
						.each(function() {
							jQuery(this).toggleClass("selected", view.selectAll);
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

					if ( !this.urlTextInput.val()) {
						this.urlTextInput.addClass("error");
						return;
					}

					jQuery("body").css("cursor", "progress");
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
											jQuery.proxy(view.setSchema, view)(serverData);
											jQuery("body").css("cursor", "auto");
										},
										"error" : function() {
											jQuery("body").css("cursor", "auto");
											if (structure == null) {
												view.errorMessages
														.push("Could not load XSD from URL.");
												view.showErrorMessages();
												view.urlTextInput
														.addClass("error");
												view.serviceSelect.empty();
												view.portSelect.empty();
												view.operationSelect.empty();
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
					this.schema = schema;
					m_utils.debug("===> Type Declarations");
					m_utils.debug(schema);

					this.tableBody.empty();

					for ( var name in this.schema.elements) {
						var element = this.schema.elements[name];

						var path = "element-" + name.replace(/:/g, "-");

						var schemaType = m_typeDeclaration.resolveSchemaTypeFromSchema(element.type, schema);
						var row = m_structuredTypeBrowser.generateChildElementRow("element-", element, schemaType,
								function(row, element, schemaType) {
							jQuery("<td><span class='data-element'>" + element.name + "</span></td>").appendTo(row);
							jQuery("<td>" + element.type + "</td>").appendTo(row);
							jQuery("<td></td>").appendTo(row);
						});

						row.data("element", element);

						row.addClass("top-level");
						this.tableBody.append(row);

						// drill into elements, too (requires element's schemaType, see above)
						if (schemaType) {
							m_structuredTypeBrowser.insertChildElementRowsEagerly(row);
						}
					}

					var view = this;
					jQuery.each(this.schema.types, function(i, type) {
						var schemaType = m_typeDeclaration.resolveSchemaTypeFromSchema(type.name, view.schema);

						var path = "type-" + type.name.replace(/:/g, "-");

						var row = m_structuredTypeBrowser.generateChildElementRow("type-", type, schemaType,
								function(row, element, schemaType) {

							jQuery("<td><span class='data-element'>" + type.name + "</span></td>").appendTo(row);
							jQuery("<td>" + type.name + "</td>").appendTo(row);
							jQuery("<td></td>").appendTo(row);

							row.data("typeDeclaration", type);
						});
						row.addClass("top-level");
						view.tableBody.append(row);

						m_structuredTypeBrowser.insertChildElementRowsEagerly(row);
					});

					this.tree.tableScroll({
						height : 150
					});
					this.tree.treeTable({
						indent: 14,
						onNodeShow: function() {
							m_structuredTypeBrowser.insertChildElementRowsLazily(jQuery(this));
						}
					});

					jQuery("table#typeDeclarationsTable tbody tr.top-level").mousedown(function() {
						// allow multi-select, but restrict to top-level entries
						jQuery(this).toggleClass("selected");
					});
				};

				/**
				 *
				 */
				ImportTypeDeclarationsWizard.prototype.performImport = function() {

					// collect selected types
					var typeDeclarations = [];
					jQuery("tr.selected", this.tableBody).each(function() {
						var row = jQuery(this);
						var typeDeclaration = row.data("typeDeclaration");
						if (typeDeclaration) {
							typeDeclarations.push(typeDeclaration);
						} else if (row.data("element")) {
							var element = row.data("element");
						}
					});

					var view = this;
					jQuery.each(typeDeclarations, function() {
						var xref = view.schema.targetNamespace ? "{" + view.schema.targetNamespace + "}" + this.name : undefined;
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