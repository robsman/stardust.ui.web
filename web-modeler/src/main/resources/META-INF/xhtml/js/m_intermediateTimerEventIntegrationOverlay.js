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
 */

define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_accessPoint",
				"bpm-modeler/js/m_parameterDefinitionsPanel",
				"bpm-modeler/js/m_eventIntegrationOverlay",
				"bpm-modeler/js/m_i18nUtils", "bpm-modeler/js/m_dialog"],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_model, m_accessPoint, m_parameterDefinitionsPanel,
				m_eventIntegrationOverlay, m_i18nUtils, m_dialog) {

			return {
				create : function(page, id) {
					var overlay = new IntermediateTimerEventIntegrationOverlay();
					overlay.initialize(page, id);
					return overlay;
				}
			};

			/**
			 *
			 */
			/**
			 * @returns {IntermediateTimerEventIntegrationOverlay}
			 */
			function IntermediateTimerEventIntegrationOverlay() {
				var eventIntegrationOverlay = m_eventIntegrationOverlay
						.create();

				m_utils.inheritFields(this, eventIntegrationOverlay);
				m_utils.inheritMethods(
						IntermediateTimerEventIntegrationOverlay.prototype,
						eventIntegrationOverlay);

				/**
				 *
				 */
				IntermediateTimerEventIntegrationOverlay.prototype.initialize = function(
						page, id) {
					this.initializeEventIntegrationOverlay(page, id);

					jQuery("label[for='autoBindingInput']").text(
									m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.autoBinding"));
					jQuery("label[for='logHandlerInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.logHandler"));

					jQuery("label[for='consumeOnMatchInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.consumeOnMatch"));
					jQuery("label[for='interruptingInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.interrupting"));

					jQuery("label[for='eventTriggerSelect']")
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

					jQuery("label[for='eventActionSelect']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.eventAction"));

					jQuery("label[for='delegateToSelect']")
					.text(
							m_i18nUtils
									.getProperty("modeler.element.properties.timerEvent_intermediate.delegateTo"));

					this.configurationSpan = this.mapInputId("configuration");

					this.configurationSpan
							.text(m_i18nUtils
									.getProperty("modeler.element.properties.event.configuration"));
					this.parametersSpan = this.mapInputId("parameters");

					this.parametersSpan
							.text(m_i18nUtils
									.getProperty("modeler.element.properties.event.parameters"));

					this.autoBindingInput = this.mapInputId("autoBindingInput");
					this.logHandlerInput = this.mapInputId("logHandlerInput");
					this.consumeOnMatchInput = this.mapInputId("consumeOnMatchInput");
					this.interruptingInput = this.mapInputId("interruptingInput");
					this.interruptingSelect = this.mapInputId("interruptingSelect");
					this.eventTriggerSelect = this.mapInputId("eventTriggerSelect");

					this.delayTimerRow = this.mapInputId("delayTimerRow");
					this.delayTimerInput = this.mapInputId("delayTimerInput");
					this.delayTimerUnitSelect = this.mapInputId("delayTimerUnitSelect");

					this.dataSelectRow = this.mapInputId("dataSelectRow");
					this.dataSelect = this.mapInputId("dataSelect");
					this.dataPathRow = this.mapInputId("dataPathRow");
					this.dataPathInput = this.mapInputId("dataPathInput");

					this.eventActionSelect = this.mapInputId("eventActionSelect");
					this.delegateToSelect = this.mapInputId("delegateToSelect");

					this.initializedelayTimerUnitSelect(this.delayTimerUnitSelect);
					this.initializeInterruptingSelect(this.interruptingSelect);
					this.initializeEventTriggerSelect(this.eventTriggerSelect);
					this.initializeDataSelect(this.dataSelect);
					this.initializeEventActionSelect(this.eventActionSelect);
					this.initializeDelegateToSelect(this.delegateToSelect);

					this.registerForRouteChanges(this.autoBindingInput);
					this.registerForRouteChanges(this.logHandlerInput);
					this.registerForRouteChanges(this.consumeOnMatchInput);
					this.registerForRouteChanges(this.interruptingInput);
					this.registerForRouteChanges(this.interruptingSelect);
					this.registerForRouteChanges(this.eventTriggerSelect);
					this.registerForRouteChanges(this.delayTimerInput);
					this.registerForRouteChanges(this.delayTimerUnitSelect);
					this.registerForRouteChanges(this.eventActionSelect);

					this.eventTriggerSelect
							.change(
									{
										"page" : this
									},
									function(event) {
										var page = event.data.page;

										var eventTrigger = page.eventTriggerSelect.val();
										page.showHideEventTriggerFields(eventTrigger);

//										page.getModelElement().eventTrigger = page.eventTriggerSelect
//												.val();
//										page.submitChanges({
//											modelElement : page
//													.getModelElement()
//										});
									});

					this.eventActionSelect.change({
						"page" : this
						}, function(event) {
						var page = event.data.page;

						var eventAction = page.eventActionSelect.val();
						page.showHideEventActionFields(eventAction);
					});

					this.showHideEventTriggerFields();
					this.showHideEventActionFields();
				};

				IntermediateTimerEventIntegrationOverlay.prototype.showHideEventActionFields = function(
						selectedVal) {
					if ('delegateActivity' == selectedVal) {
						jQuery("label[for='delegateToSelect']").removeClass(
								"invisible");
						m_dialog.makeVisible(this.delegateToSelect);
					} else {
						jQuery("label[for='delegateToSelect']").addClass(
								"invisible");
						m_dialog.makeInvisible(this.delegateToSelect);
					}
				};


				IntermediateTimerEventIntegrationOverlay.prototype.showHideEventTriggerFields = function(
						selectedVal) {
					this.delayTimerRow.css("display", "none");
					this.dataSelectRow.css("display", "none");
					this.dataPathRow.css("display", "none");

					if ('constant' == selectedVal) {
						this.delayTimerRow.css("display", "table-row");
						this.dataSelectRow.css("display", "none");
						this.dataPathRow.css("display", "none");
					}

					if ('data' == selectedVal) {
						this.delayTimerRow.css("display", "none");
						this.dataSelectRow.css("display", "table-row");
						this.dataPathRow.css("display", "table-row");
					}
				};


				IntermediateTimerEventIntegrationOverlay.prototype.initializedelayTimerUnitSelect = function(
						select) {
					select.append("<option value='1'>"
									+ m_i18nUtils.getProperty("modeler.element.properties.event.milliseconds")
									+ "</option>");
					select.append("<option value='1000'>"
									+ m_i18nUtils.getProperty("modeler.element.properties.event.seconds")
									+ "</option>");
					select.append("<option value='60000'>"
									+ m_i18nUtils.getProperty("modeler.element.properties.event.minutes")
									+ "</option>");
					select.append("<option value='3600000'>"
									+ m_i18nUtils.getProperty("modeler.element.properties.event.hours")
									+ "</option>");
					select.append("<option value='86400000'>"
									+ m_i18nUtils.getProperty("modeler.element.properties.event.days")
									+ "</option>");
					select.append("<option value='2.63e+9'>"
							+ m_i18nUtils.getProperty("modeler.element.properties.event.months")
							+ "</option>");
					select.append("<option value='3.156e+10'>"
							+ m_i18nUtils.getProperty("modeler.element.properties.event.years")
							+ "</option>");

				};

				IntermediateTimerEventIntegrationOverlay.prototype.initializeInterruptingSelect = function(
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

				IntermediateTimerEventIntegrationOverlay.prototype.initializeEventTriggerSelect = function(
						select) {
					select
					.append("<option value=''>"
							+ m_i18nUtils
							.getProperty("modeler.general.toBeDefined")
							+ "</option>");

					select
							.append("<option value='constant'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.eventTrigger.constant")
									+ "</option>");
					select
							.append("<option value='data'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.eventTrigger.data")
									+ "</option>");
				};

				IntermediateTimerEventIntegrationOverlay.prototype.initializeDataSelect = function(
						select) {
					select.append("<option value='sample1'>"
							+ "Primitive Data1" + "</option>");
					select.append("<option value='sample2'>"
							+ "Primitive Data2" + "</option>");
				};

				IntermediateTimerEventIntegrationOverlay.prototype.initializeEventActionSelect = function(
						select) {
					select
							.append("<option value='exceptionFlow'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.eventAction.exceptionFlow")
									+ "</option>");
					select
							.append("<option value='abortProcess'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.eventAction.abortProcess")
									+ "</option>");
					select
							.append("<option value='activateActivity'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.eventAction.activateActivity")
									+ "</option>");
					select
					.append("<option value='delegateActivity'>"
							+ m_i18nUtils
									.getProperty("modeler.element.properties.timerEvent_intermediate.eventAction.delegateActivity")
							+ "</option>");
					select
							.append("<option value='hibernateActivity'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.eventAction.hibernateActivity")
									+ "</option>");
					select
							.append("<option value='suspendActivity'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.eventAction.suspendActivity")
									+ "</option>");

				};

				IntermediateTimerEventIntegrationOverlay.prototype.initializeDelegateToSelect = function(
						select) {
					select
							.append("<option value='default'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.eventAction.delegateActivity.default")
									+ "</option>");
					select
							.append("<option value='currentUser'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.eventAction.delegateActivity.currentUser")
									+ "</option>");
					select
							.append("<option value='participant'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.eventAction.delegateActivity.participant")
									+ "</option>");
					select
							.append("<option value='randomUser'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.eventAction.delegateActivity.randomUser")
									+ "</option>");

				};

				/**
				 *
				 */
				IntermediateTimerEventIntegrationOverlay.prototype.getEndpointUri = function() {
					var uri = "timer://timerEndpoint";
					var separator = "?";

					return uri;
				};

				/**
				 *
				 */
				IntermediateTimerEventIntegrationOverlay.prototype.activate = function() {
					this.delayTimerInput.val(0);
					var parameterMappings = [];

					this.submitOverlayChanges(parameterMappings);
				};

				IntermediateTimerEventIntegrationOverlay.prototype.getRouteDefinitions = function() {
					return "<from uri=\"" + this.getEndpointUri() + "\"/>"
							+ this.getAdditionalRouteDefinitions();
				};

				IntermediateTimerEventIntegrationOverlay.prototype.getAdditionalRouteDefinitions = function() {
					return "<to uri=\"ipp:direct\"/>";
				};
				/**
				 *
				 */
				IntermediateTimerEventIntegrationOverlay.prototype.update = function() {
					// retrieve and populated stored values
					// this.showHideEventTriggerFields('constant');
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
				IntermediateTimerEventIntegrationOverlay.prototype.validate = function() {
					return true;
				};
			}
		});