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
					return new ProcessDisplayPropertiesPage(propertiesPanel);
				}
			};

			function ProcessDisplayPropertiesPage(newPropertiesPanel, newId,
					newTitle) {

				// Inheritance

				var propertiesPage = m_propertiesPage.createPropertiesPage(
						newPropertiesPanel, "displayPropertiesPage", "Display");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(ProcessDisplayPropertiesPage.prototype,
						propertiesPage);

				// Field initialization

				/**
				 * 
				 */
				ProcessDisplayPropertiesPage.prototype.setElement = function() {
				};

				/**
				 * 
				 */
				ProcessDisplayPropertiesPage.prototype.validate = function() {
					this.propertiesPanel.clearErrorMessages();
					
					return true;
				};

				/**
				 * 
				 */
				ProcessDisplayPropertiesPage.prototype.submitChanges = function(changes) {
					m_commandsController.submitCommand(m_command
							.createUpdateModelElementCommand(this.propertiesPanel.element.model.id,
									this.propertiesPanel.element.oid,
									changes));
				};

			}
		});