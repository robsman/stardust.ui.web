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
		[ "m_utils", "m_constants", "m_propertiesPanel", "m_propertiesPage",
				"m_gatewayBasicPropertiesPage" ],
		function(m_utils, m_constants, m_propertiesPanel, m_propertiesPage,
				m_gatewayBasicPropertiesPage) {

			var gatewayPropertiesPanel = null;

			return {
				initialize : function(models) {
					gatewayPropertiesPanel = new GatewayPropertiesPanel(
							models);
					
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

				// Constants

				// Member initialization

				this.models = models;
				this.propertiesPages = [
						m_gatewayBasicPropertiesPage
								.createPropertiesPage(this)];

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

				/**
				 * 
				 */
				GatewayPropertiesPanel.prototype.apply = function() {
					this.applyPropertiesPages();
					this.element.refresh();
					this.element.submitUpdate();
				};
			}
		});