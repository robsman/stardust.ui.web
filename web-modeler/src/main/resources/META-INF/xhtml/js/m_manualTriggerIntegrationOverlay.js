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
				"m_model", "m_accessPoint", "m_parameterDefinitionsPanel" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_model, m_accessPoint, m_parameterDefinitionsPanel) {

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
				/**
				 * 
				 */
				ManualTriggerIntegrationOverlay.prototype.initialize = function(
						page, id) {
					this.page = page;
					this.id = id;
					this.participantOutput = this
							.mapInputId("participantOutput");
				};

				/**
				 * 
				 */
				ManualTriggerIntegrationOverlay.prototype.mapInputId = function(
						inputId) {
					return jQuery("#" + this.id + " #" + inputId);
				};

				/**
				 * 
				 */
				ManualTriggerIntegrationOverlay.prototype.activate = function() {
				};

				/**
				 * 
				 */
				ManualTriggerIntegrationOverlay.prototype.update = function() {
					this.participantOutput.empty();

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