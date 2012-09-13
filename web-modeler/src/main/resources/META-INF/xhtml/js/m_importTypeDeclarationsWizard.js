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
		[ "m_utils", "m_constants", "m_communicationController", "m_command", "m_commandsController",
				"m_model", "m_typeDeclaration", "m_accessPoint",
				"m_dataTraversal", "m_dialog" ],
		function(m_utils, m_constants, m_communicationController, m_command, m_commandsController,
				m_model, m_typeDeclaration, m_accessPoint, m_dataTraversal,
				m_dialog) {
			return {
				initialize : function() {
					var wizard = new ImportTypeDeclarationsWizard(
							payloadObj.importCallback);

					wizard.initialize(payloadObj.model);
				}
			};

			/**
			 * 
			 */
			function ImportTypeDeclarationsWizard(importCallback) {
				this.importCallback = importCallback;
				this.tree = jQuery("#typeDeclarationsTable");
				this.tableBody = jQuery("table#typeDeclarationsTable tbody");
				this.urlTextInput = jQuery("#urlTextInput");
				this.loadFromUrlButton = jQuery("#loadFromUrlButton");
				this.importButton = jQuery("#importButton");
				this.cancelButton = jQuery("#cancelButton");

				this.loadFromUrlButton.click({
					"view" : this
				}, function(event) {
					event.data.view.loadFromUrl();
				});

				this.importButton.click({
					wizard : this
				}, function(event) {
					event.data.wizard.create();
					closePopup();
				});

				this.cancelButton.click({
					wizard : this
				}, function(event) {
					closePopup();
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
				ImportTypeDeclarationsWizard.prototype.loadFromUrl = function(
						structure) {
					var successCallback = {
						callbackScope : this,
						callbackMethod : "setTypeDeclarations"
					};

					jQuery("body").css("cursor", "progress");
					//this.clearErrorMessages();
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
											successCallback.callbackScope[successCallback.callbackMethod]
													(serverData);
											jQuery("body")
													.css("cursor", "auto");
										},
										"error" : function() {
											jQuery("body")
													.css("cursor", "auto");
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
												successCallback.callbackScope[successCallback.callbackMethod]
														(structure);
											}
										}
									});
				};

				/**
				 * 
				 */
				ImportTypeDeclarationsWizard.prototype.setTypeDeclarations = function(
						typeDeclarations) {
					m_utils.debug("===> Type Declarations");
					m_utils.debug(typeDeclarations);

					this.tableBody.empty();

					for ( var element in typeDeclarations.elements) {
						var path = element.replace(/:/g, "-");

						var content = "<tr id='" + path + "'>";

						content += "<td>";
						content += "<span class='data-element'>"
								+ typeDeclarations.elements[element].name
								+ "</span>";
						content += "</td>";
						content += "<td>";
						content += typeDeclarations.elements[element].name;
						content += "</td>";
						content += "<td>";
						content += "</td>";
						content += "</tr>";

						this.tableBody.append(content);

						if (typeDeclarations.elements[element].body != null) {
							this
									.populateRecursively(
											typeDeclarations.elements[element].body.elements,
											path, true);
						}
					}

					for ( var type in typeDeclarations.types) {
						var path = type.replace(/:/g, "-");

						var content = "<tr id='" + path + "'>";

						content += "<td>";
						content += "<span class='data-element'>"
								+ typeDeclarations.types[type].name
								+ "</span>";
						content += "</td>";
						content += "<td>";
						content += typeDeclarations.types[type].name;
						content += "</td>";
						content += "<td>";
						content += "</td>";
						content += "</tr>";

						this.tableBody.append(content);

						if (typeDeclarations.types[type].body != null) {
							this
									.populateRecursively(
											typeDeclarations.types[type].body.elements,
											path, true);
						}
					}

					this.tree.tableScroll({
						height : 150
					});
					this.tree.treeTable();

					jQuery("table#typeDeclarationsTable tbody tr").mousedown(
							function() {
								jQuery("tr.selected").removeClass("selected");
								jQuery(this).addClass("selected");
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
//					this
//							.createCallback({
//								id : this.application.id,
//								name : this.processDefinitionNameInput
//										.val(),
//							});
				};
			}
			;
		});