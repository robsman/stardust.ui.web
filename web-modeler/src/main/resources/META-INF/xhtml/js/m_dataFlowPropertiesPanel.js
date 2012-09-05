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
		[ "m_utils", "m_constants", "m_commandsController", "m_propertiesPanel", "m_propertiesPage"
				],
		function(m_utils, m_constants, m_commandsController, m_propertiesPanel, m_propertiesPage) {

			var dataFlowPropertiesPanel = null;

			return {
				initialize : function(diagram) {
					dataFlowPropertiesPanel = new DataFlowPropertiesPanel();

					m_commandsController.registerCommandHandler(dataFlowPropertiesPanel);					
					
					dataFlowPropertiesPanel.initialize(diagram);
				},
				getInstance : function() {
					return dataFlowPropertiesPanel;
				}
			};

			/**
			 * 
			 */
			function DataFlowPropertiesPanel() {
				var propertiesPanel = m_propertiesPanel
						.createPropertiesPanel("dataFlowPropertiesPanel");

				m_utils.inheritFields(this, propertiesPanel);
				m_utils.inheritMethods(DataFlowPropertiesPanel.prototype,
						propertiesPanel);

				/**
				 * 
				 */
				DataFlowPropertiesPanel.prototype.toString = function() {
					return "Lightdust.DataFlowPropertiesPanel";
				};

				/**
				 * 
				 */
				DataFlowPropertiesPanel.prototype.setElement = function(
						element) {
					this.clearErrorMessages();

					this.element = element;

					for ( var n in this.propertiesPages) {
						this.propertiesPages[n].setElement();
					}
				};
			}
		});