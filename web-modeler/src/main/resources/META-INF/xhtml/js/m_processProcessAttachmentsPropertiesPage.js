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
				create: function(propertiesPanel) {
					var page = new ProcessProcessAttachmentsPropertiesPage(propertiesPanel);
					
					page.initialize();
					
					return page;
				}
			};

			/**
			 * 
			 */
			function ProcessProcessAttachmentsPropertiesPage(propertiesPanel) {
				var propertiesPage = m_propertiesPage.createPropertiesPage(
						propertiesPanel, "processAttachmentsPropertiesPage", "Process Attachments", "../../images/icons/generic-properties-page.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(ProcessProcessAttachmentsPropertiesPage.prototype,
						propertiesPage);

				/**
				 * 
				 */
				ProcessProcessAttachmentsPropertiesPage.prototype.initialize = function() {
					this.supportsAttachmentsInput = this.mapInputId("supportsAttachmentsInput");
					this.uniquePerRootProcessInstanceInput = this.mapInputId("uniquePerRootProcessInstanceInput");
					
					this.registerCheckboxInputForModelElementAttributeChangeSubmission(this.supportsAttachmentsInput, "@TODOsupportsAttachmentsInput");
					this.registerCheckboxInputForModelElementAttributeChangeSubmission(this.uniquePerRootProcessInstanceInput, "@TODOuniquePerRootProcessInstanceInput");
				};

				/**
				 * 
				 */
				ProcessProcessAttachmentsPropertiesPage.prototype.setElement = function() {
				};

				/**
				 * 
				 */
				ProcessProcessAttachmentsPropertiesPage.prototype.validate = function() {
					this.propertiesPanel.clearErrorMessages();
					
					return true;
				};
			}
		});