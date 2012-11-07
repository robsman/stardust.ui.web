/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ "m_utils", "m_constants", "m_commandsController", "m_command",
		"m_model", "m_accessPoint", "m_parameterDefinitionsPanel", "m_eventIntegrationOverlay" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_model, m_accessPoint, m_parameterDefinitionsPanel, m_eventIntegrationOverlay) {

			return {
				create : function(page, id) {
					var overlay = new MessageEventIntegrationOverlay();

					overlay.initialize(page, id);

					return overlay;
				}
			};

			/**
			 * 
			 */
			function MessageEventIntegrationOverlay() {
				var eventIntegrationOverlay = m_eventIntegrationOverlay
						.create();

				m_utils.inheritFields(this, eventIntegrationOverlay);
				m_utils.inheritMethods(MessageEventIntegrationOverlay.prototype,
						eventIntegrationOverlay);

				/**
				 * 
				 */
				MessageEventIntegrationOverlay.prototype.initialize = function(
						page, id) {
					this.initializeEventIntegrationOverlay(page, id);

					this.typeSelect = this
							.mapInputId("typeSelect");
					this.nameInput = this
					.mapInputId("nameInput");
					
					this.registerForRouteChanges(this.typeSelect);					
					this.registerForRouteChanges(this.nameInput);					
				};

				/**
				 * 
				 */
				MessageEventIntegrationOverlay.prototype.getEndpointUri = function() {
					var uri = "jms:";

					uri += this.typeSelect.val();
					uri += ":";
					uri += this.nameInput.val();

					return uri;
				};

				/**
				 * 
				 */
				MessageEventIntegrationOverlay.prototype.activate = function() {
					this.submitEventClassChanges();
				};

				/**
				 * 
				 */
				MessageEventIntegrationOverlay.prototype.update = function() {
					this.submitEventClassChanges();
				};
			}
		});