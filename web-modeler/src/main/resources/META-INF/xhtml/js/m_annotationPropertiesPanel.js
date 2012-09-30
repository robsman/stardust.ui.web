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
		[ "m_utils", "m_constants", "m_commandsController", "m_model", "m_propertiesPanel", "m_propertiesPage"],
		function(m_utils, m_constants, m_commandsController, m_model, m_propertiesPanel, m_propertiesPage) {

			var annotationPropertiesPanel = null;

			return {
				initialize : function(diagram) {
					annotationPropertiesPanel = new AnnotationPropertiesPanel();
				
					m_commandsController.registerCommandHandler(annotationPropertiesPanel);					
					
					annotationPropertiesPanel.initialize(diagram);
				},
				getInstance : function(element) {
					return annotationPropertiesPanel;
				}
			};

			/**
			 * 
			 */
			function AnnotationPropertiesPanel(models) {
				var propertiesPanel = m_propertiesPanel
						.createPropertiesPanel("annotationPropertiesPanel");

				m_utils.inheritFields(this, propertiesPanel);
				m_utils.inheritMethods(AnnotationPropertiesPanel.prototype,
						propertiesPanel);

				/**
				 * 
				 */
				AnnotationPropertiesPanel.prototype.toString = function() {
					return "Lightdust.AnnotationPropertiesPanel";
				};

				/**
				 * 
				 */
				AnnotationPropertiesPanel.prototype.setElement = function(
						element) {
					this.clearErrorMessages();

					this.element = element;

					for ( var n in this.propertiesPages) {
						this.propertiesPages[n].setElement();
					}
				};
			}
		});