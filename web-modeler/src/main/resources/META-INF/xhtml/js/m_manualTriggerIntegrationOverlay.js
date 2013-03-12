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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_command",
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_accessPoint", "bpm-modeler/js/m_parameterDefinitionsPanel","bpm-modeler/js/m_eventIntegrationOverlay"],
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
				};

				/**
				 * 
				 */
				ManualTriggerIntegrationOverlay.prototype.submitOverlayChanges = function(
						parameterMappings) {
					if (parameterMappings == null) {
						parameterMappings = [];
					}

					this.submitChanges({
						modelElement : {
							participantFullId : this.page
							.getElement().parentSymbol.participantFullId,
							parameterMappings : parameterMappings,
							attributes : {
								"carnot:engine:integration::overlay" : this.id
							}
						}
					});
				};

				/**
				 * 
				 */
				ManualTriggerIntegrationOverlay.prototype.activate = function() {
					this.submitOverlayChanges();
				};

				/**
				 * 
				 */
				ManualTriggerIntegrationOverlay.prototype.update = function() {
				};
				
				/**
				 * 
				 */
				ManualTriggerIntegrationOverlay.prototype.validate = function() {
					return true;
				};
			}
		});