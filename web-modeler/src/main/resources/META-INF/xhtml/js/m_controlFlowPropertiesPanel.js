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
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_commandsController", "m_propertiesPanel", "m_propertiesPage"],
		function(m_utils, m_constants, m_commandsController, m_propertiesPanel, m_propertiesPage) {

			var controlFlowPropertiesPanel = null;

			return {
				initialize : function(models) {
					controlFlowPropertiesPanel = new ControlFlowPropertiesPanel(
							models);

					m_commandsController.registerCommandHandler(controlFlowPropertiesPanel);

					controlFlowPropertiesPanel.initialize();
				},
				getInstance : function(element) {
					return controlFlowPropertiesPanel;
				}
			};

			/**
			 * 
			 */
			function ControlFlowPropertiesPanel() {

				// Inheritance

				var propertiesPanel = m_propertiesPanel
						.createPropertiesPanel("controlFlowPropertiesPanel");

				m_utils.inheritFields(this, propertiesPanel);
				m_utils.inheritMethods(ControlFlowPropertiesPanel.prototype,
						propertiesPanel);

				// Member initialization

				/**
				 * 
				 */
				ControlFlowPropertiesPanel.prototype.toString = function() {
					return "Lightdust.ControlFlowPropertiesPanel";
				};

				/**
				 * 
				 */
				ControlFlowPropertiesPanel.prototype.setElement = function(
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