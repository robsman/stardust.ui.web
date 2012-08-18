/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * 
 */
define(
		[ "m_utils", "m_constants", "m_commandsController", "m_propertiesPanel", "m_propertiesPage"],
		function(m_utils, m_constants, m_commandsController, m_propertiesPanel, m_propertiesPage) {

			var gatewayPropertiesPanel = null;

			return {
				initialize : function(models) {
					gatewayPropertiesPanel = new GatewayPropertiesPanel(
							models);
					
					m_commandsController.registerCommandHandler(gatewayPropertiesPanel);					
					
					gatewayPropertiesPanel.initialize();
				},				
				getInstance : function(element) {
					return gatewayPropertiesPanel;
				}
			};

			/**
			 * 
			 */
			function GatewayPropertiesPanel(models) {
				// Inheritance

				var propertiesPanel = m_propertiesPanel
						.createPropertiesPanel("gatewayPropertiesPanel");

				m_utils.inheritFields(this, propertiesPanel);
				m_utils.inheritMethods(GatewayPropertiesPanel.prototype,
						propertiesPanel);

				// Member initialization

				this.models = models;

				/**
				 * 
				 */
				GatewayPropertiesPanel.prototype.toString = function() {
					return "Lightdust.GatewayPropertiesPanel";
				};

				/**
				 * 
				 */
				GatewayPropertiesPanel.prototype.setElement = function(
						newElement) {
					this.clearErrorMessages();

					this.element = newElement;

					if (this.element.properties == null) {
						this.element.properties = {};
					}

					for ( var n in this.propertiesPages) {
						this.propertiesPages[n].setElement();
					}
				};
			}
		});