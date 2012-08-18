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

			var eventPropertiesPanel = null;

			return {
				initialize : function(models) {
					eventPropertiesPanel = new EventPropertiesPanel(
							models);
				
					m_commandsController.registerCommandHandler(eventPropertiesPanel);					
					
					eventPropertiesPanel.initialize();
				},
				getInstance : function(element) {
					return eventPropertiesPanel;
				}
			};

			/**
			 * 
			 */
			function EventPropertiesPanel(models) {

				// Inheritance

				var propertiesPanel = m_propertiesPanel
						.createPropertiesPanel("eventPropertiesPanel");

				m_utils.inheritFields(this, propertiesPanel);
				m_utils.inheritMethods(EventPropertiesPanel.prototype,
						propertiesPanel);

				// Member initialization

				this.models = models;

				/**
				 * 
				 */
				EventPropertiesPanel.prototype.toString = function() {
					return "Lightdust.EventPropertiesPanel";
				};

				/**
				 * 
				 */
				EventPropertiesPanel.prototype.setElement = function(
						element) {
					this.clearErrorMessages();

					this.element = element;

					if (this.element.modelElement.participantFullId != null) {
						this.participant = m_model
								.findParticipant(this.element.modelElement.participantFullId);
					}

					for ( var n in this.propertiesPages) {
						this.propertiesPages[n].setElement();
					}
				};
			}
		});