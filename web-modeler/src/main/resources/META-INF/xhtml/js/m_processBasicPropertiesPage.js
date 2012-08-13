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
			"m_commandsController", "m_command", "m_basicPropertiesPage" ],
		function(m_utils, m_constants,
				m_commandsController, m_command, m_basicPropertiesPage) {
			return {
				create: function(propertiesPanel) {
					var page = new ProcessBasicPropertiesPage(propertiesPanel);

					page.initialize();

					return page;
				}
			};

			function ProcessBasicPropertiesPage(propertiesPanel) {

				// Inheritance

				var basicPropertiesPage = m_basicPropertiesPage.create(
						propertiesPanel);

				m_utils.inheritFields(this, basicPropertiesPage);
				m_utils.inheritMethods(ProcessBasicPropertiesPage.prototype,
						basicPropertiesPage);

				// Field initialization

				/**
				 * 
				 */
				ProcessBasicPropertiesPage.prototype.getDocumentationCreationUrl = function() {
					var url = "/models/" + this.getModelElement().modelId
							+ "/createDocumentation";

					return url;
				};

				/**
				 * 
				 */
				ProcessBasicPropertiesPage.prototype.initialize = function() {
					this.initializeBasicPropertiesPage();
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
					this.setModelElement();
				};

				/**
				 * 
				 */
				ProcessBasicPropertiesPage.prototype.validate = function() {
					if (this.validateModelElement()) {
						return true;
					}

					return true;
				};
			}
		});