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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_extensionManager", "bpm-modeler/js/m_command",
				"bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_user", "bpm-modeler/js/m_dialog",
				"bpm-modeler/js/m_propertiesPage", "bpm-modeler/js/m_i18nUtils"  ],
		function(m_utils, m_constants, m_extensionManager, m_command,
				m_commandsController, m_user, m_dialog, m_propertiesPage, m_i18nUtils) {
			return {
				create : function(propertiesPanel) {
					return new BasicPropertiesPage(propertiesPanel);
				}
			};

			/**
			 *
			 */
			function BasicPropertiesPage(propertiesPanel) {
				var generalProperties = m_i18nUtils.getProperty("modeler.processDefinition.propertyPages.general.heading");
				var propertiesPage = m_propertiesPage.createPropertiesPage(
						propertiesPanel, "basicPropertiesPage",
						generalProperties,
						"plugins/bpm-modeler/images/icons/table.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(BasicPropertiesPage.prototype,
						propertiesPage);

				/**
				 *
				 */
				BasicPropertiesPage.prototype.initializeBasicPropertiesPage = function() {
					this.guidOutputRow = this.mapInputId("guidOutputRow");
					this.idOutputRow = this.mapInputId("idOutputRow");
					this.guidOutput = this.mapInputId("guidOutput");
					this.idOutput = this.mapInputId("idOutput");
					this.nameInput = this.mapInputId("nameInput");
					this.descriptionInput = this.mapInputId("descriptionInput");
					this.documentationCreationLinkPanel = this
							.mapInputId("documentationCreationLinkPanel");
					this.documentationCreationLink = this
							.mapInputId("documentationCreationLink");
					this.openDocumentViewLink = this
							.mapInputId("openDocumentViewLink");
					this.openDocumentViewLinkPanel = this
							.mapInputId("openDocumentViewLinkPanel");

					this.registerInputForModelElementChangeSubmission(
							this.nameInput, "name");
					this.registerInputForModelElementChangeSubmission(
							this.descriptionInput, "description");

					this.initializeDocumentationHandling();

					var viewManagerExtension = m_extensionManager
							.findExtension("viewManager");
					this.viewManager = viewManagerExtension.provider.create();
				};

				/**
				 *
				 */
				BasicPropertiesPage.prototype.initializeDocumentationHandling = function() {
					if (this.documentationCreationLink != null) {
						this.documentationCreationLink
								.click(
										{
											page : this
										},
										function(event) {
											// TODO Trick to force object update
											// to create a new document
											event.data.page
													.submitChanges(event.data.page.propertiesPanel
															.wrapModelElementProperties({
																attributes : {
																	"documentation:externalDocumentUrl" : "@CREATE"
																}
															}));
										});
					}

					if (this.openDocumentViewLink != null) {
						this.openDocumentViewLink.click({
							page : this
						}, function(event) {
							event.data.page.viewManager.openView(
									"documentView", "documentOID="
											+ event.data.page.documentUrl,
									"documentOID="
											+ event.data.page.documentUrl);
						});
					}
				};

				/**
				 *
				 */
				BasicPropertiesPage.prototype.setModelElement = function() {
					//enable description section
					this.descriptionInput.show();
					m_utils.jQuerySelect("label[for='descriptionInput']").show();
					
					if (m_user.getCurrentRole() != m_constants.INTEGRATOR_ROLE) {
						m_dialog.makeInvisible(this.guidOutputRow);
						m_dialog.makeInvisible(this.idOutputRow);
					} else {
						m_dialog.makeVisible(this.guidOutputRow);
						m_dialog.makeVisible(this.idOutputRow);
					}

					this.nameInput.removeClass("error");

					this.guidOutput.empty();
					this.idOutput.empty();

					this.guidOutput.append(this.getModelElementUuid());
					this.idOutput.append(this.getModelElement().id);
					this.nameInput.val(this.getModelElement().name);
					this.descriptionInput
							.val(this.getModelElement().description);

					this.loadDocumentUrl();
				};

				/**
				 *
				 */
				BasicPropertiesPage.prototype.getModelElement = function() {
					return this.propertiesPanel.element.modelElement;
				};

				/**
				 *
				 */
				BasicPropertiesPage.prototype.getModelElementUuid = function() {
					// TODO Replace with uuid
					return this.propertiesPanel.element.uuid;
					// return this.getModelElement().uuid;
				};

				/**
				 *
				 */
				BasicPropertiesPage.prototype.validateModelElement = function() {
					this.propertiesPanel.clearErrorMessages();
					this.nameInput.removeClass("error");

					if (m_utils.isEmptyString(this.nameInput.val())) {
						this.propertiesPanel.errorMessages
								.push("Name must not be empty.");
						this.nameInput.addClass("error");
						this.nameInput.focus();
						this.propertiesPanel.showErrorMessages();

						return false;
					}

					return true;
				};

				/**
				 *
				 */
				BasicPropertiesPage.prototype.validateIntegerInput = function(
						input, message) {
					this.propertiesPanel.clearErrorMessages();
					input.removeClass("error");
					if (isNaN(input.val()) || (-1 != input.val().indexOf("."))) {
						this.propertiesPanel.errorMessages.push(message);
						input.addClass("error");
						input.focus();
						this.propertiesPanel.showErrorMessages();
						return false;
					}

					return true;
				};

				/**
				 *
				 */
				BasicPropertiesPage.prototype.loadDocumentUrl = function() {
					// if (this.propertiesPanel.getModelElement().attributes ==
					// null) {
					// return;
					// }
					//
					// this.documentUrl =
					// this.propertiesPanel.getModelElement().attributes["documentation:externalDocumentUrl"];
					//
					// if (this.documentationCreationLinkPanel != null
					// && this.openDocumentViewLinkPanel != null) {
					// if (this.documentUrl == null) {
					// m_dialog
					// .makeVisible(this.documentationCreationLinkPanel);
					// m_dialog
					// .makeInvisible(this.openDocumentViewLinkPanel);
					// } else {
					// m_dialog
					// .makeInvisible(this.documentationCreationLinkPanel);
					// m_dialog
					// .makeVisible(this.openDocumentViewLinkPanel);
					// }
					// }

					// TODO Add above back if document generation is supported

					m_dialog.makeInvisible(this.documentationCreationLinkPanel);
					m_dialog.makeInvisible(this.openDocumentViewLinkPanel);
				};

				/**
				 * Server Callback
				 */
				BasicPropertiesPage.prototype.setDocumentUrl = function(json) {
					this
							.submitChanges(this.propertiesPanel
									.wrapModelElementProperties({
										attributes : {
											"documentation:externalDocumentUrl" : json.documentUrl
										}
									}));
				};

			}
		});