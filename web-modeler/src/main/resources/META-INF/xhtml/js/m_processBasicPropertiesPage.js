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
		[ "m_utils", "m_constants", 
			"m_commandsController", "m_command", "m_propertiesPage" ],
		function(m_utils, m_constants,
				m_commandsController, m_command, m_propertiesPage) {
			return {
				createPropertiesPage : function(propertiesPanel) {
					return new ProcessBasicPropertiesPage(propertiesPanel);
				}
			};

			function ProcessBasicPropertiesPage(newPropertiesPanel, newId,
					newTitle) {

				// Inheritance

				var propertiesPage = m_propertiesPage.createPropertiesPage(
						newPropertiesPanel, "basicPropertiesPage", "Basic");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(ProcessBasicPropertiesPage.prototype,
						propertiesPage);

				// Field initialization

				this.nameInput = this.mapInputId("nameInput");
				this.descriptionInput = this.mapInputId("descriptionInput");

				this.initializeDocumentationHandling();

				this.nameInput
						.change(
								{
									"page" : this
								},
								function(event) {
									m_commandsController
											.submitCommand(m_command
													.createRenameCommand(
															event.data.page.propertiesPanel.element
																	.getPath(true),
															{
																"id" : event.data.page.propertiesPanel.element.id,
																"name" : event.data.page.propertiesPanel.element.name
															},
															{
																"name" : event.data.page.nameInput
																		.val()
															}));
								});

				/**
				 * 
				 */
				ProcessBasicPropertiesPage.prototype.getDocumentationCreationUrl = function() {
					var url = "/models/" + this.propertiesPanel.element.modelId
							+ "/createDocumentation";

					return url;
				};

				/**
				 * 
				 */
				ProcessBasicPropertiesPage.prototype.getModelElement = function() {
					return this.propertiesPanel.element;
				};

				/**
				 * 
				 */
				ProcessBasicPropertiesPage.prototype.setElement = function() {
					this.nameInput.removeClass("error");

					this.nameInput.val(this.propertiesPanel.element.name);
					this.descriptionInput
							.val(this.propertiesPanel.element.description);

					this.loadDocumentUrl();

					if (this.documentUrl == null) {
						this.documentationCreationLinkPanel.removeAttr("class");
						this.openDocumentViewLinkPanel.attr("class",
								"invisible");
					} else {
						this.documentationCreationLinkPanel.attr("class",
								"invisible");
						this.openDocumentViewLinkPanel.removeAttr("class");
					}

				};

				/**
				 * 
				 */
				ProcessBasicPropertiesPage.prototype.validate = function() {
					this.nameInput.removeClass("error");

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
						this.propertiesPanel.errorMessages
								.push("Process name must not be empty.");
						this.nameInput.addClass("error");
					}
				};

				/**
				 * 
				 */
				ProcessBasicPropertiesPage.prototype.apply = function() {
					this.propertiesPanel.element.name = this.nameInput.val();
					this.propertiesPanel.element.description = this.descriptionInput
							.val();
					this.saveDocumentUrl();
				};
				
				/**
				 * 
				 */
				ProcessBasicPropertiesPage.prototype.submitChanges = function(changes) {
					m_commandsController.submitCommand(m_command
							.createUpdateModelElementCommand(
									this.propertiesPanel.element.oid,
									changes,
									this.propertiesPanel.element));
				};

			}
		});