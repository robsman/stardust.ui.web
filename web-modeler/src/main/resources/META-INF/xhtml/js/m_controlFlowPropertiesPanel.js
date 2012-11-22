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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_propertiesPanel", "bpm-modeler/js/m_propertiesPage"],
		function(m_utils, m_constants, m_commandsController, m_propertiesPanel, m_propertiesPage) {

			var controlFlowPropertiesPanel = null;

			return {
				initialize : function(diagram) {
					controlFlowPropertiesPanel = new ControlFlowPropertiesPanel();

					m_commandsController.registerCommandHandler(controlFlowPropertiesPanel);

					controlFlowPropertiesPanel.initialize(diagram);
				},
				getInstance : function(element) {
					return controlFlowPropertiesPanel;
				}
			};

			/**
			 * 
			 */
			function ControlFlowPropertiesPanel() {
				var propertiesPanel = m_propertiesPanel
						.createPropertiesPanel("controlFlowPropertiesPanel");

				m_utils.inheritFields(this, propertiesPanel);
				m_utils.inheritMethods(ControlFlowPropertiesPanel.prototype,
						propertiesPanel);

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
						element) {
					this.clearErrorMessages();

					this.element = element;

					if (this.element.properties == null) {
						this.element.properties = {};
					}

					for ( var n in this.propertiesPages) {
						this.propertiesPages[n].setElement();
					}
				};
			}
		});