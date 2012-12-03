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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_propertiesPanel",
				"bpm-modeler/js/m_propertiesPage", "bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_commandsController, m_model,
				m_propertiesPanel, m_propertiesPage, m_i18nUtils) {

			var eventPropertiesPanel = null;

			return {
				initialize : function(diagram) {
					eventPropertiesPanel = new EventPropertiesPanel();

					m_commandsController
							.registerCommandHandler(eventPropertiesPanel);

					eventPropertiesPanel.initialize(diagram);
				},
				getInstance : function() {
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

				/**
				 * 
				 */
				EventPropertiesPanel.prototype.toString = function() {
					return "Lightdust.EventPropertiesPanel";
				};

				/**
				 * 
				 */
				EventPropertiesPanel.prototype.setElement = function(element) {
					this.clearErrorMessages();

					this.element = element;

					m_utils.debug("Event");
					m_utils.debug(element);

					this.titleSpan = jQuery("#" + this.id + " #title");
					
					m_utils.debug(this.titleSpan);
					
					this.titleSpan.empty();

					if (element.modelElement.eventType == m_constants.START_EVENT_TYPE) {
						this.titleSpan
								.append(
										m_i18nUtils
												.getProperty("modeler.eventPropertiesPanel.heading.startEvent"));
					} else if (element.modelElement.eventType == m_constants.INTERMEDIATE_EVENT_TYPE) {
						this.titleSpan
								.append(
										m_i18nUtils
												.getProperty("modeler.eventPropertiesPanel.heading.intermediateEvent"));
					} else if (element.modelElement.eventType == m_constants.STOP_EVENT_TYPE) {
						this.titleSpan
								.append(
										m_i18nUtils
												.getProperty("modeler.eventPropertiesPanel.heading.endEvent"));
					}

					if (this.element.modelElement.participantFullId != null) {
						this.participant = m_model
								.findParticipant(this.element.modelElement.participantFullId);
					}

					for ( var n in this.propertiesPages) {
						this.propertiesPages[n].setElement();
					}

					if (this.element.modelElement.eventType == m_constants.END_EVENT_TYPE) {
						this.disablePropertiesPage("commentsPropertiesPage");
					} else {
						this.enablePropertiesPage("commentsPropertiesPage");
					}
				};
			}
		});