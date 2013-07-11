/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * Timer Event overlay for Intermediate events
 *
 * @author Yogesh.Manware
 *
 * Note: don't remove the code around following elements - this is for future development
 *  - Automatic Binding
 *  - Consume On Match
 *  - Event Action
 *
 */

define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_accessPoint",
				"bpm-modeler/js/m_parameterDefinitionsPanel",
				"bpm-modeler/js/m_eventIntegrationOverlay",
				"bpm-modeler/js/m_i18nUtils", "bpm-modeler/js/m_dialog" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_model, m_accessPoint, m_parameterDefinitionsPanel,
				m_eventIntegrationOverlay, m_i18nUtils, m_dialog) {

			return {
				create : function(page, id) {
					var overlay = new IntermediateErrorEventIntegrationOverlay();
					overlay.initialize(page, id);
					return overlay;
				}
			};

			/**
			 *
			 */
			/**
			 * @returns {IntermediateErrorEventIntegrationOverlay}
			 */
			function IntermediateErrorEventIntegrationOverlay() {
				var eventIntegrationOverlay = m_eventIntegrationOverlay
						.create();

				m_utils.inheritFields(this, eventIntegrationOverlay);
				m_utils.inheritMethods(
						IntermediateErrorEventIntegrationOverlay.prototype,
						eventIntegrationOverlay);

				/**
				 *
				 */
				IntermediateErrorEventIntegrationOverlay.prototype.initialize = function(
						page, id) {
					this.initializeEventIntegrationOverlay(page, id);

					/*jQuery("label[for='autoBindingInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.autoBinding"));*/
					jQuery("label[for='logHandlerInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.logHandler"));

					/*jQuery("label[for='consumeOnMatchInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.consumeOnMatch"));*/

					jQuery("label[for='interruptingInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.interrupting"));

					jQuery("label[for='eventTriggerInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.eventTrigger"));

					jQuery("label[for='delayTimerInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.delayTimer"));

					jQuery("label[for='dataSelect']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.eventTrigger.data"));

					jQuery("label[for='dataPathInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.eventTrigger.data.path"));

					/*jQuery("label[for='eventActionSelect']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.eventAction"));*/

					this.configurationSpan = this.mapInputId("configuration");

					this.configurationSpan
							.text(m_i18nUtils
									.getProperty("modeler.element.properties.event.configuration"));
					this.parametersSpan = this.mapInputId("parameters");

					this.parametersSpan
							.text(m_i18nUtils
									.getProperty("modeler.element.properties.event.parameters"));

					// this.autoBindingInput = this.mapInputId("autoBindingInput");
					this.logHandlerInput = this.mapInputId("logHandlerInput");
					// this.consumeOnMatchInput = this.mapInputId("consumeOnMatchInput");

					this.interruptingInput = this
							.mapInputId("interruptingInput");

					this.eventTriggerInput = this
							.mapInputId("eventTriggerInput");

					// this.eventActionSelect = this.mapInputId("eventActionSelect");
					// this.initializeEventActionSelect(this.eventActionSelect);

					this.registerForRouteChanges(this.logHandlerInput);
					//this.registerForRouteChanges(this.consumeOnMatchInput);
					this.registerForRouteChanges(this.eventTriggerInput);
				};

				IntermediateErrorEventIntegrationOverlay.prototype.initializeInterruptingSelect = function(
						select) {
					select
							.append("<option value='abortActivity'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.event.abortActivity")
									+ "</option>");
					select
							.append("<option value='completeActivity'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.event.completeActivity")
									+ "</option>");
				};

				/*IntermediateErrorEventIntegrationOverlay.prototype.initializeEventActionSelect = function(
						select) {
					select
							.append("<option value='exceptionFlow'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.eventAction.exceptionFlow")
									+ "</option>");
				};*/

				/**
				 *
				 */
				IntermediateErrorEventIntegrationOverlay.prototype.getEndpointUri = function() {
					var uri = "timer://timerEndpoint";
					var separator = "?";

					return uri;
				};

				/**
				 *
				 */
				IntermediateErrorEventIntegrationOverlay.prototype.activate = function() {
					var parameterMappings = [];

					this.submitOverlayChanges(parameterMappings);
				};

				IntermediateErrorEventIntegrationOverlay.prototype.getRouteDefinitions = function() {
					return "<from uri=\"" + this.getEndpointUri() + "\"/>"
							+ this.getAdditionalRouteDefinitions();
				};

				IntermediateErrorEventIntegrationOverlay.prototype.getAdditionalRouteDefinitions = function() {
					return "<to uri=\"ipp:direct\"/>";
				};

				/**
				 *
				 */
				IntermediateErrorEventIntegrationOverlay.prototype.update = function() {
					// retrieve and populated stored values
					// this.showHideEventTriggerFields('constant');

					// this.autoBindingInput.attr("disabled", "disabled");
					this.interruptingInput.attr("checked", "checked");
					this.interruptingInput.attr("disabled", "disabled");
					// this.eventActionSelect.attr("disabled", "disabled");

					var route = null;
					if (this.page.propertiesPanel.element.modelElement.attributes) {
						route = this.page.propertiesPanel.element.modelElement.attributes["carnot:engine:camel::camelRouteExt"];
					}

					if (route == null) {
						return;
					}
				};

				/**
				 *
				 */
				IntermediateErrorEventIntegrationOverlay.prototype.validate = function() {
					return true;
				};
			}
		});