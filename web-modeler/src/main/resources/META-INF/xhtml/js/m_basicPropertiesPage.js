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
		[ "m_utils", "m_constants", "m_command", "m_commandsController",
				"m_propertiesPage", "m_activity" ],
		function(m_utils, m_constants, m_command, m_commandsController,
				m_propertiesPage, m_activity) {
			return {
				create : function(propertiesPanel) {
					return new BasicPropertiesPage(propertiesPanel);
				}
			};

			function BasicPropertiesPage(propertiesPanel) {
				// Inheritance

				var propertiesPage = m_propertiesPage.createPropertiesPage(
						propertiesPanel, "basicPropertiesPage", "Basic",
						"../../images/icons/basic-properties-page.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(BasicPropertiesPage.prototype,
						propertiesPage);

				/**
				 * 
				 */
				BasicPropertiesPage.prototype.initializeBasicPropertiesPage = function() {
					this.guidOutput = this.mapInputId("guidOutput");
					this.idOutput = this.mapInputId("idOutput");
					this.nameInput = this.mapInputId("nameInput");
					this.descriptionInput = this.mapInputId("descriptionInput");

					// this.initializeDocumentationHandling();

					// Initialize callbacks

					this.registerTextInputForModelElementChangeSubmission(
							this.nameInput, "name");
					this.registerTextInputForModelElementChangeSubmission(
							this.descriptionInput, "description");
				};

				/**
				 * 
				 */
				BasicPropertiesPage.prototype.setModelElement = function() {
					this.nameInput.removeClass("error");

					this.guidOutput.empty();
					this.guidOutput
							.append(this.propertiesPanel.element.modelElement.oid);
					this.idOutput.empty();
					this.idOutput
							.append(this.propertiesPanel.element.modelElement.id);
					this.nameInput
							.val(this.propertiesPanel.element.modelElement.name);
					this.descriptionInput
							.val(this.propertiesPanel.element.modelElement.description);
					// this.loadDocumentUrl();
				};

				/**
				 * 
				 */
				BasicPropertiesPage.prototype.validateModelElement = function() {
					this.propertiesPanel.clearErrorMessages();
					this.nameInput.removeClass("error");

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
						this.propertiesPanel.errorMessages
								.push("Name must not be empty.");
						this.nameInput.addClass("error");
						this.propertiesPanel.showErrorMessages();

						return false;
					}

					return true;
				};
			}
		});