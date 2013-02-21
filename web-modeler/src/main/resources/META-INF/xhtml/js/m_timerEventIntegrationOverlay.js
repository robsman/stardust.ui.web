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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_accessPoint",
				"bpm-modeler/js/m_parameterDefinitionsPanel",
				"bpm-modeler/js/m_eventIntegrationOverlay",
				"bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_model, m_accessPoint, m_parameterDefinitionsPanel,
				m_eventIntegrationOverlay, m_i18nUtils) {

			return {
				create : function(page, id) {
					var overlay = new TimerEventIntegrationOverlay();

					overlay.initialize(page, id);

					return overlay;
				}
			};

			/**
			 * 
			 */
			function TimerEventIntegrationOverlay() {
				var eventIntegrationOverlay = m_eventIntegrationOverlay
						.create();

				m_utils.inheritFields(this, eventIntegrationOverlay);
				m_utils.inheritMethods(TimerEventIntegrationOverlay.prototype,
						eventIntegrationOverlay);

				/**
				 * 
				 */
				TimerEventIntegrationOverlay.prototype.initialize = function(
						page, id) {
					this.initializeEventIntegrationOverlay(page, id);

					jQuery("label[for='repeatIntervalInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent.repeatInterval"));
					jQuery("label[for='repeatCountInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent.repeatCount"));

					jQuery("label[for='fixedRateInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent.fixedRate"));

					this.configurationSpan = this.mapInputId("configuration");

					this.configurationSpan
							.text(m_i18nUtils
									.getProperty("modeler.element.properties.event.configuration"));
					this.parametersSpan = this.mapInputId("parameters");

					this.parametersSpan
							.text(m_i18nUtils
									.getProperty("modeler.element.properties.event.parameters"));

					this.repeatIntervalInput = this
							.mapInputId("repeatIntervalInput");
					this.repeatIntervalUnitSelect = this
							.mapInputId("repeatIntervalUnitSelect");
					this.fixedRateInput = this.mapInputId("fixedRateInput");

					this
							.initializeIntervalUnitSelect(this.repeatIntervalUnitSelect);

					this.repeatCountInput = this.mapInputId("repeatCountInput");

					this.registerForRouteChanges(this.repeatCountInput);
					this.registerForRouteChanges(this.repeatIntervalInput);
					this.registerForRouteChanges(this.repeatIntervalUnitSelect);
					this.registerForRouteChanges(this.fixedRateInput);
				};

				/**
				 * 
				 */
				TimerEventIntegrationOverlay.prototype.getEndpointUri = function() {
					var uri = "timer://timerEndpoint";
					var separator = "?";

					if (this.repeatCountInput.val() != null) {
						uri += separator + "repeatCount="
								+ this.repeatCountInput.val();
						separator = "&";
					}
					if (this.fixedRateInput.prop("checked") == true) {
						uri += separator + "fixedRate=true";
						separator = "&";
					}

					if (this.getIntervalInMilliseconds(this.repeatIntervalInput
							.val(), this.repeatIntervalUnitSelect.val()) != null) {
						uri += separator
								+ "period="
								+ this.getIntervalInMilliseconds(
										this.repeatIntervalInput.val(),
										this.repeatIntervalUnitSelect.val());
					}
					uri = uri.replace(/&/g, "&amp;");
					return uri;
				};

				/**
				 * 
				 */
				TimerEventIntegrationOverlay.prototype.activate = function() {
					this.repeatIntervalInput.val(5000);
					this.repeatCountInput.val(1);

					var parameterMappings = [];

					/*
					 * parameterMappings.push(this
					 * .createPrimitiveParameterMapping("Message", "message",
					 * "String"));
					 */
					/*
					 * parameterMappings.push(this
					 * .createPrimitiveParameterMapping("Calendar", "calendar",
					 * "String")); parameterMappings.push(this
					 * .createPrimitiveParameterMapping("Fire Time", "fireTime",
					 * "String")); parameterMappings.push(this
					 * .createPrimitiveParameterMapping("Job Detail",
					 * "jobDetail", "String")); parameterMappings.push(this
					 * .createPrimitiveParameterMapping("Job Instance",
					 * "jobInstance", "String")); parameterMappings.push(this
					 * .createPrimitiveParameterMapping("Job Runtime",
					 * "jobRuntTime", "String")); parameterMappings.push(this
					 * .createPrimitiveParameterMapping( "Merged Job Data Map",
					 * "mergedJobDataMap", "String"));
					 * parameterMappings.push(this
					 * .createPrimitiveParameterMapping("Next Fire Time",
					 * "nextFireTime", "String")); parameterMappings.push(this
					 * .createPrimitiveParameterMapping( "Previous Fire Time",
					 * "previousFireTime", "String"));
					 * parameterMappings.push(this
					 * .createPrimitiveParameterMapping( "Scheduled Fire Time",
					 * "scheduledFireTime", "String"));
					 * parameterMappings.push(this
					 * .createPrimitiveParameterMapping("Refire Count",
					 * "refireCount", "String")); parameterMappings.push(this
					 * .createPrimitiveParameterMapping("Trigger Name",
					 * "triggerName", "String")); parameterMappings.push(this
					 * .createPrimitiveParameterMapping("Trigger Group",
					 * "triggerGroup", "String"));
					 */

					this.submitOverlayChanges(parameterMappings);
				};

				TimerEventIntegrationOverlay.prototype.getRouteDefinitions= function() {
					return "<from uri=\""+this.getEndpointUri()+"\"/>"+this.getAdditionalRouteDefinitions();
				}
				
				TimerEventIntegrationOverlay.prototype.getAdditionalRouteDefinitions = function() {
					return "<to uri=\"ipp:direct\"/>";
				};
				/**
				 * 
				 */
				TimerEventIntegrationOverlay.prototype.update = function() {
					var route = this.page.propertiesPanel.element.modelElement.attributes["carnot:engine:camel::camelRouteExt"];

					if (route == null) {
						return;
					}

					// TODO Need better URL encoding

					// route = route.replace(/&/g, "&amp;");

					var xmlDoc = jQuery
							.parseXML("<route>" + route + "</route>");
					var xmlObject = jQuery(xmlDoc);
					var from = jQuery(xmlObject).find("from");
					var uri = from.attr("uri");
					var protocolAndRest = uri.split("://");

					var parametersAndOptions = protocolAndRest[1].split("?");
					var options = parametersAndOptions[1];

					// Map options

					if (options) {
						var nameValues = options.split("&");

						for ( var n = 0; n < nameValues.length; ++n) {
							var nameValue = nameValues[n].split("=");
							var name = nameValue[0];
							var value = nameValue[1];

							m_utils.debug("name: " + name);
							m_utils.debug("value: " + value);

							if (name == "repeatCount") {
								this.repeatCountInput.val(value);
							} else if (name == "period") {
								var intervalWithUnit = this
										.getIntervalWithUnit(value);
								this.repeatIntervalInput
										.val(intervalWithUnit.value);
								this.repeatIntervalUnitSelect
										.val(intervalWithUnit.unit);
							} else if (name == "fixedRate") {
								if(value =="true")
								this.fixedRateInput.prop("checked",
										value == "true");
							}
						}
					}

					// this.parameterMappingsPanel.setScopeModel(this.page
					// .getModel());
					// this.parameterMappingsPanel
					// .setParameterDefinitions(this.page.getEvent().parameterMappings);
				};

				/**
				 * 
				 */
				TimerEventIntegrationOverlay.prototype.validate = function() {
					this.repeatCountInput.removeClass("error");
					this.repeatIntervalInput.removeClass("error");

					if (m_utils.isEmptyString(this.repeatCountInput.val())) {
						this.getPropertiesPanel().errorMessages
								.push(m_i18nUtils
										.getProperty("modeler.general.fieldMustNotBeEmpty"));
						this.repeatCountInput.addClass("error");
						this.repeatCountInput.focus();

						this.getPropertiesPanel().showErrorMessages();

						return false;
					}

					if (!m_utils.isNumber(this.repeatCountInput.val())) {
						this.getPropertiesPanel().errorMessages
								.push(m_i18nUtils
										.getProperty("modeler.general.fieldMustContainANumber"));
						this.repeatCountInput.addClass("error");
						this.repeatCountInput.focus();

						this.getPropertiesPanel().showErrorMessages();

						return false;
					}

					if (m_utils.isEmptyString(this.repeatIntervalInput.val())) {
						this.getPropertiesPanel().errorMessages
								.push(m_i18nUtils
										.getProperty("modeler.general.fieldMustNotBeEmpty"));
						this.repeatIntervalInput.addClass("error");
						this.repeatIntervalInput.focus();

						this.getPropertiesPanel().showErrorMessages();

						return false;
					}

					if (!m_utils.isNumber(this.repeatIntervalInput.val())) {
						this.getPropertiesPanel().errorMessages
								.push(m_i18nUtils
										.getProperty("modeler.general.fieldMustContainANumber"));
						this.repeatIntervalInput.addClass("error");
						this.repeatIntervalInput.focus();

						this.getPropertiesPanel().showErrorMessages();

						return false;
					}

					return true;
				};
			}
		});