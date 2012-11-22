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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_model", "bpm-modeler/js/m_propertiesPanel", "bpm-modeler/js/m_propertiesPage"],
		function(m_utils, m_constants, m_commandsController, m_model, m_propertiesPanel, m_propertiesPage) {

			var testPropertiesPanel = null;

			return {
				initialize : function(models) {
					testPropertiesPanel = new TestPropertiesPanel(
							models);
				
					m_commandsController.registerCommandHandler(testPropertiesPanel);					
					
					testPropertiesPanel.initialize();
				},
				getInstance : function(element) {
					return testPropertiesPanel;
				}
			};

			/**
			 * 
			 */
			function TestPropertiesPanel(models) {
				var propertiesPanel = m_propertiesPanel
						.createPropertiesPanel("testPropertiesPanel");

				m_utils.inheritFields(this, propertiesPanel);
				m_utils.inheritMethods(TestPropertiesPanel.prototype,
						propertiesPanel);

				// Member initialization

				this.models = models;

				/**
				 * 
				 */
				TestPropertiesPanel.prototype.toString = function() {
					return "Lightdust.TestPropertiesPanel";
				};

				/**
				 * 
				 */
				TestPropertiesPanel.prototype.setElement = function(
						element) {
					this.clearErrorMessages();

					this.element = element;

					for ( var n in this.propertiesPages) {
						this.propertiesPages[n].setElement();
					}
				};
			}
		});