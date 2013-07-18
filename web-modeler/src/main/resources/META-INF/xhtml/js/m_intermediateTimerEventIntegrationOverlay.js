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
 *  - Delegate Activity
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

					/*jQuery("label[for='autoBindingInput']").text(
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

					/*jQuery("label[for='eventActionSelect']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent_intermediate.eventAction"));

					jQuery("label[for='delegateToSelect']")
					.text(
							m_i18nUtils
									.getProperty("modeler.element.properties.timerEvent_intermediate.delegateTo"));*/

					this.configurationSpan = this.mapInputId("configuration");

					this.configurationSpan
							.text(m_i18nUtils
									.getProperty("modeler.element.properties.event.configuration"));
					this.parametersSpan = this.mapInputId("parameters");

					this.parametersSpan
							.text(m_i18nUtils
									.getProperty("modeler.element.properties.event.parameters"));

					//this.autoBindingInput = this.mapInputId("autoBindingInput");
					this.logHandlerInput = this.mapInputId("logHandlerInput");
					//this.consumeOnMatchInput = this.mapInputId("consumeOnMatchInput");
					this.interruptingInput = this.mapInputId("interruptingInput");
					this.eventTriggerSelect = this.mapInputId("eventTriggerSelect");

					this.delayTimerRow = this.mapInputId("delayTimerRow");
					this.delayTimerInput = this.mapInputId("delayTimerInput");
					this.delayTimerUnitSelect = this.mapInputId("delayTimerUnitSelect");

					this.dataSelectRow = this.mapInputId("dataSelectRow");
					this.dataSelect = this.mapInputId("dataSelect");
					this.dataPathRow = this.mapInputId("dataPathRow");
					this.dataPathInput = this.mapInputId("dataPathInput");

					//this.eventActionSelect = this.mapInputId("eventActionSelect");
					//this.delegateToSelect = this.mapInputId("delegateToSelect");

					this.initializedelayTimerUnitSelect(this.delayTimerUnitSelect);
					this.initializeEventTriggerSelect(this.eventTriggerSelect);
					this.initializeDataSelect(this.dataSelect);
					//this.initializeEventActionSelect(this.eventActionSelect);
					//this.initializeDelegateToSelect(this.delegateToSelect);

					this.logHandlerInput.change({
						overlay : this
					}, function(event) {
						var overlay = event.data.overlay;
						overlay.submitChanges({
							modelElement : {
								logHandler :  overlay.logHandlerInput.prop("checked")
							}
						});
					});


					this.interruptingInput.change({
						overlay : this
					}, function(event) {
						var overlay = event.data.overlay;
						overlay.submitChanges({
							modelElement : {
								interrupting :  overlay.interruptingInput.prop("checked")
							}
						});
					});

					this.eventTriggerSelect.change({
						overlay : this
					}, function(event) {

						var overlay = event.data.overlay;
						var eventTrigger = overlay.eventTriggerSelect.val();
						overlay.showHideEventTriggerFields();

						if ('constant' == eventTrigger) {
							overlay.updateConstant();
						} else if ('data' == eventTrigger) {
							overlay.updateData();
						}
					});

					this.dataSelect.change({
						overlay : this
					}, function(event) {
						event.data.overlay.updateData();
					});

					this.dataPathInput.change({
						overlay : this
					}, function(event) {
						event.data.overlay.updateData();
					});

					this.delayTimerInput.change({
						overlay : this
					}, function(event) {
						event.data.overlay.updateConstant();
					});

					/*
					 * this.eventActionSelect.change({ "page" : this },
					 * function(event) { var page = event.data.page;
					 *
					 * var eventAction = page.eventActionSelect.val();
					 * page.showHideEventActionFields(eventAction); });
					 */


					//this.showHideEventActionFields();
				};

				/**
				 *
				 */
				IntermediateTimerEventIntegrationOverlay.prototype.updateData = function() {
					this.submitChanges({
						modelElement : {
							attributes : {
								"carnot:engine:useData" : true,
								"carnot:engine:data" : this.dataSelect.val(),
								"carnot:engine:dataPath" : this.dataPathInput.val()
							}
						}
					});
				};

				/**
				 *
				 */
				IntermediateTimerEventIntegrationOverlay.prototype.updateConstant = function() {
					this.submitChanges({
						modelElement : {
							attributes : {
								"carnot:engine:useData" : false,
								"carnot:engine:delay" : this.delayTimerInput.val(),
							}
						}
					});
				};


				/*IntermediateTimerEventIntegrationOverlay.prototype.showHideEventActionFields = function(
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
				};*/


				IntermediateTimerEventIntegrationOverlay.prototype.showHideEventTriggerFields = function() {
					var selectedVal = this.eventTriggerSelect.val();
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
					this.dataSelect.empty();

					this.dataSelect.append("<option value='"
							+ null
							+ "'>" + m_i18nUtils.getProperty("modeler.general.toBeDefined") + "</option>");

					var dataItems = this.page.propertiesPanel.getModel().dataItems;

					for ( var m in dataItems) {
						this.dataSelect.append("<option value='"
								+ dataItems[m].getFullId() + "'>"
								+ dataItems[m].name + "</option>");
					}
				};

				/**
				 * initialize data
				 */
				IntermediateTimerEventIntegrationOverlay.prototype.update = function() {
					// retrieve and populated stored values
					/*this.autoBindingInput.prop("checked", true);
					this.autoBindingInput.prop("disabled", true);
					this.consumeOnMatchInput.prop("disabled", true);*/

					var modelElement = this.page.propertiesPanel.element.modelElement;
					if (modelElement.isBoundaryEvent()) {
						this.interruptingInput.prop("checked",
								modelElement.interrupting);
						this.interruptingInput.prop("disabled", false);
					} else {
						this.interruptingInput.prop("checked", false);
						this.interruptingInput.prop("disabled", true);
					}

					this.logHandlerInput.prop("checked",
							modelElement.logHandler);
					var useData = modelElement.attributes["carnot:engine:useData"];
					if (useData == true) {
						this.eventTriggerSelect.val('data');
						this.dataSelect.val(modelElement.attributes["carnot:engine:data"]);
						this.dataPathInput.val(modelElement.attributes["carnot:engine:dataPath"]);
					} else if (useData == false) {
						this.eventTriggerSelect.val('constant');
						this.delayTimerInput.val(modelElement.attributes["carnot:engine:delay"]);
					}else{
						this.eventTriggerSelect.val('');
					}

					this.showHideEventTriggerFields();
				};

				/*IntermediateTimerEventIntegrationOverlay.prototype.initializeEventActionSelect = function(
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

				};*/

				/*IntermediateTimerEventIntegrationOverlay.prototype.initializeDelegateToSelect = function(
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
*/
				/**
				 *
				 */
				IntermediateTimerEventIntegrationOverlay.prototype.getEndpointUri = function() {
					return "";
				};

				/**
				 *
				 */
				IntermediateTimerEventIntegrationOverlay.prototype.activate = function() {
					// It is invoked when there are multiple options in overlay
					// dropdown, here we have only one option.
				};

				IntermediateTimerEventIntegrationOverlay.prototype.getRouteDefinitions = function() {
					//not required in this case?
					return "";
				};



				/**
				 *
				 */
				IntermediateTimerEventIntegrationOverlay.prototype.validate = function() {
					return true;
				};
			}
		});