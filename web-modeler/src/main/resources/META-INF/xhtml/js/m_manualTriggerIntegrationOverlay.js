/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define(
		[ "m_utils", "m_constants", "m_commandsController", "m_command",
				"m_model", "m_accessPoint", "m_parameterDefinitionsPanel","m_eventIntegrationOverlay"],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_model, m_accessPoint, m_parameterDefinitionsPanel, m_eventIntegrationOverlay) {

			return {
				create : function(page, id) {
					var overlay = new ManualTriggerIntegrationOverlay();

					overlay.initialize(page, id);

					return overlay;
				}
			};

			/**
			 * 
			 */
			function ManualTriggerIntegrationOverlay() {
				var eventIntegrationOverlay = m_eventIntegrationOverlay
						.create();

				m_utils.inheritFields(this, eventIntegrationOverlay);
				m_utils.inheritMethods(ManualTriggerIntegrationOverlay.prototype,
						eventIntegrationOverlay);

				/**
				 * 
				 */
				ManualTriggerIntegrationOverlay.prototype.initialize = function(
						page, id) {
					this.initializeEventIntegrationOverlay(page, id);

					this.participantOutput = this
							.mapInputId("participantOutput");
				};

				/**
				 * 
				 */
				ManualTriggerIntegrationOverlay.prototype.submitEventClassChanges = function(
						parameterMappings) {
					if (parameterMappings == null) {
						parameterMappings = [];
					}

					this.submitChanges({
						modelElement : {
							eventClass : this.id,
							parameterMappings : parameterMappings
						}
					});
				};

				/**
				 * 
				 */
				ManualTriggerIntegrationOverlay.prototype.activate = function() {
					this.submitEventClassChanges();
				};

				/**
				 * 
				 */
				ManualTriggerIntegrationOverlay.prototype.update = function() {
					this.participantOutput.empty();

					// TODO I18N
					
					if (this.page.propertiesPanel.participant != null) {
						this.participantOutput.append("Started by <b>"
								+ this.page.propertiesPanel.participant.name
								+ ".</b>");
					} else {
						this.participantOutput
								.append("Starting participant to be defined.</b>");
					}
				};
			}
		});