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
		[ "m_utils", "m_constants", "m_communicationController", "m_command",
				"m_commandsController", "m_model",
				"m_accessPoint", "m_dataTraversal", "m_dialog" ],
		function(m_utils, m_constants, m_communicationController, m_command,
				m_commandsController, m_model,
				m_accessPoint, m_dataTraversal, m_dialog) {
			return {
				initialize : function() {
					var wizard = new ImportTypeDeclarationsWizard();

					wizard.initialize(payloadObj.model);
				}
			};

			/**
			 *
			 */
			function ImportTypeDeclarationsWizard() {
				this.tree = jQuery("#typeDeclarationsTable");
				this.tableBody = jQuery("table#typeDeclarationsTable tbody");
				this.urlTextInput = jQuery("#urlTextInput");
				this.loadFromUrlButton = jQuery("#loadFromUrlButton");
				this.importButton = jQuery("#importButton");
				this.cancelButton = jQuery("#cancelButton");

				var view = this;
				this.loadFromUrlButton.click(function(event) {
					view.loadFromUrl();
				});

				this.importButton.click(function(event) {
					view.import();
					closePopup();
				});

				this.cancelButton.click(function(event) {
					closePopup();
				});

				/**
				 *
				 */
				ImportTypeDeclarationsWizard.prototype.initialize = function(
						model) {
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
				ImportTypeDeclarationsWizard.prototype.loadFromUrl = function(
						structure) {

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
											jQuery.proxy(view.setTypeDeclarations, view)(serverData);
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
												jQuery.proxy(view.setTypeDeclarations, view)(structure);
											}
										}
									});
				};

				/**
				 *
				 */
				ImportTypeDeclarationsWizard.prototype.setTypeDeclarations = function(
						typeDeclarations) {
					this.typeDeclarations = typeDeclarations;
					m_utils.debug("===> Type Declarations");
					m_utils.debug(typeDeclarations);

					this.tableBody.empty();

					for ( var name in this.typeDeclarations.elements) {
						var element = this.typeDeclarations.elements[name];

						var path = "element-" + name.replace(/:/g, "-");

						var row = jQuery("<tr id='" + path + "'></tr>");

						jQuery("<td><span class='data-element'>" + element.name + "</span></td>").appendTo(row);
						jQuery("<td>" + element.name + "</td>").appendTo(row);
						jQuery("<td></td>").appendTo(row);

						row.data("element", element);

						this.tableBody.append(row);

						if (element.body != null) {
							this.populateRecursively(element.body.elements, path, true);
						}
					}

					for ( var name in this.typeDeclarations.types) {
						var type = this.typeDeclarations.types[name];

						var path = "type-" + name.replace(/:/g, "-");

						var row = jQuery("<tr id='" + path + "'></tr>");

						jQuery("<td><span class='data-element'>" + type.name + "</span></td>").appendTo(row);
						jQuery("<td>" + type.name + "</td>").appendTo(row);
						jQuery("<td></td>").appendTo(row);

						row.data("typeDeclaration", type);

						this.tableBody.append(row);

						if (type.body != null) {
							this.populateRecursively(type.body.elements, path, true);
						}
					}

					this.tree.tableScroll({
						height : 150
					});
					this.tree.treeTable({
						indent: 14
					});

					jQuery("table#typeDeclarationsTable tbody tr").mousedown(
							function() {
								// allow multi-select
								jQuery(this).toggleClass("selected");
							});
				};

				/**
				 *
				 */
				ImportTypeDeclarationsWizard.prototype.populateRecursively = function(
						elements, parentPath, readonly) {
					if (elements == null) {
						return;
					}

					for ( var childElement in elements) {
						var path = parentPath + "."
								+ elements[childElement].name;

						var content = "<tr id='" + path + "' class='child-of-"
								+ parentPath + "'>";

						content += "<td class='elementCell'>";
						content += "<span class='data-element'>"
								+ elements[childElement].name + "</span>";
						content += "</td>";
						content += "<td class='typeCell'>";

						if (readonly) {
							content += elements[childElement].type;
						} else {
							content += this
									.getTypeSelectList(elements[childElement].type);
						}

						content += "</td>"
								+ "<td align='right' class='cardinalityCell'>";

						// many, required, optional
						if (readonly) {
							if (elements[childElement].cardinality == "optional") {
								content += "Optional";
							} else if (elements[childElement].cardinality == "required") {
								content += "Required";
							} else if (elements[childElement].cardinality == "many") {
								content += "Many";
							}
						} else {
							content += ("<select size='1'><option value='1'"
									+ (elements[childElement].cardinality == "1" ? "selected"
											: "")
									+ ">Required</option><option value='N'"
									+ (elements[childElement].cardinality == "N" ? "selected"
											: "") + ">Many</option></select>");
						}

						content += "</td></tr>";

						this.tableBody.append(content);

						if (elements[childElement].type != null) {
							this
									.populateRecursively(
											elements[childElement].children,
											path, true);
						}
					}
				};

				/**
				 *
				 */
				ImportTypeDeclarationsWizard.prototype.import = function() {

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
													xref: "{" + view.typeDeclarations.targetNamespace + "}" + this.name
												}
											}
										}));
					});
				};
			}
			;
		});