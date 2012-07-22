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
					return new ProcessTestPropertiesPage(propertiesPanel);
				}
			};

			function ProcessTestPropertiesPage(newPropertiesPanel, newId,
					newTitle) {

				// Inheritance

				var propertiesPage = m_propertiesPage.createPropertiesPage(
						newPropertiesPanel, "testPropertiesPage", "Test", "../images/icons/basic-properties-page.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(ProcessTestPropertiesPage.prototype,
						propertiesPage);

				// Field initialization

				/**
				 * 
				 */
				ProcessTestPropertiesPage.prototype.getModelElement = function() {
					return this.propertiesPanel.element;
				};

				/**
				 * 
				 */
				ProcessTestPropertiesPage.prototype.setElement = function() {
				};

				/**
				 * 
				 */
				ProcessTestPropertiesPage.prototype.validate = function() {
					return true;
				};
			}
		});