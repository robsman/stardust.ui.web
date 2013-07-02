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
				"bpm-modeler/js/m_i18nUtils"],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_model, m_accessPoint, m_parameterDefinitionsPanel, m_i18nUtils) {

			return {
				create : function() {
					var overlay = new EventIntegrationOverlay();

					return overlay;
				}
			};

			/**
			 *
			 */
			function EventIntegrationOverlay() {
				/**
				 *
				 */
				EventIntegrationOverlay.prototype.initializeEventIntegrationOverlay = function(
						page, id) {
					this.page = page;
					this.id = id;

					this.parameterMappingsPanelAnchor = this
							.mapInputId("parameterMappingsPanelAnchor");

					if (this.parameterMappingsPanelAnchor != null) {
						var overlay = this;

						this.parameterMappingsPanelAnchor
								.load(
										"plugins/bpm-modeler/views/modeler/parameterDefinitionsPanel.html",
										function(response, status, xhr) {
											if (status == "error") {
												var msg = "Properties Page Load Error: "
														+ xhr.status
														+ " "
														+ xhr.statusText;

												jQuery(this).append(msg);
											} else {
												overlay.parameterMappingsPanel = m_parameterDefinitionsPanel
														.create({
															scope : overlay.id,
															submitHandler : overlay,
															supportsOrdering : false,
															supportsDataMappings : true,
															supportsDescriptors : false,
															supportsDataTypeSelection : true,
															readOnlyParameterList : true,
															hideDirectionSelection : true
														});
											}
										});

						this.propertiesTabs = this.mapInputId("propertiesTabs");
					}

					if (this.propertiesTabs != null) {
						this.propertiesTabs.tabs();
					}
				};

				/**
				 *
				 */
				EventIntegrationOverlay.prototype.mapInputId = function(inputId) {
					return jQuery("#" + this.id + " #" + inputId);
				};

				/**
				 *
				 */
				EventIntegrationOverlay.prototype.registerForRouteChanges = function(
						input) {
					input.change({
						overlay : this
					}, function(event) {
						if (event.data.overlay.validate()) {
							event.data.overlay.submitRouteChanges();
						}
					});
				};

				/**
				 *
				 */
				EventIntegrationOverlay.prototype.getEvent = function() {
					this.page.getEvent();
				};

				/**
				 *
				 */
				EventIntegrationOverlay.prototype.createPrimitiveParameterMapping = function(
						name, id, primitiveDataType) {
					return {
						name : name,
						id : id,
						direction : m_constants.OUT_ACCESS_POINT,
						dataType : "primitive",
						primitiveDataType : primitiveDataType
					};
				};

				/**
				 *
				 */
				EventIntegrationOverlay.prototype.getPropertiesPanel = function() {
					return this.page.propertiesPanel;
				};

				/**
				 *
				 */
				EventIntegrationOverlay.prototype.getImplementation = function() {
					return "camel";
				};

				/**
				 *
				 */
				EventIntegrationOverlay.prototype.getCamelContext = function() {
					return "defaultCamelContext";
				};

				/**
				 * Dummy function
				 */
				EventIntegrationOverlay.prototype.getAdditionalRouteDefinitions = function() {
					return "";
				};

				/**
				 * Dummy function
				 */
				EventIntegrationOverlay.prototype.getAdditionalBeanSpecifications = function() {
					return "";
				};

				/**
				 *
				 */
				EventIntegrationOverlay.prototype.initializeIntervalUnitSelect = function(
						select) {
					select
							.append("<option value='1'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.event.milliseconds")
									+ "</option>");
					select
							.append("<option value='1000'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.event.seconds")
									+ "</option>");
					select
							.append("<option value='60000'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.event.minutes")
									+ "</option>");
					select
							.append("<option value='3600000'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.event.hours")
									+ "</option>");
					select
							.append("<option value='86400000'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.event.days")
									+ "</option>");
				};

				/**
				 *
				 */
				EventIntegrationOverlay.prototype.initializedelayTimerUnitSelect = function(
						select) {
					select
							.append("<option value='1'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.event.milliseconds")
									+ "</option>");
					select
							.append("<option value='1000'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.event.seconds")
									+ "</option>");
					select
							.append("<option value='60000'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.event.minutes")
									+ "</option>");
				};

				/**
				 *
				 */
				EventIntegrationOverlay.prototype.getIntervalInMilliseconds = function(
						value, unitFactor) {
					return (value == null ? 0 : value * unitFactor);
				};

				/**
				 *
				 */
				EventIntegrationOverlay.prototype.getIntervalWithUnit = function(
						value) {
					if (value > 86400000 && value % 86400000 == 0) {
						return {
							value : value / 86400000,
							unit : 86400000

						};
					} else if (value > 3600000 && value % 3600000 == 0) {
						return {
							value : value / 3600000,
							unit : 3600000

						};
					} else if (value > 60000 && value % 60000 == 0) {
						return {
							value : value / 60000,
							unit : 60000

						};
					} else if (value > 1000 && value % 1000 == 0) {
						return {
							value : value / 1000,
							unit : 1000

						};
					}

					return {
						value : value,
						unit : 1
					};
				};

				/**
				 *
				 */
				EventIntegrationOverlay.prototype.getRouteContent = function()
				{
					return this.getRouteDefinitions();
				};

				/**
				 *
				 */
				EventIntegrationOverlay.prototype.submitChanges = function(
						changes) {
					this.page.submitChanges(changes);
				};

				/**
				 * Callback for Parameter Definitions Panel
				 */
				EventIntegrationOverlay.prototype.submitParameterDefinitionsChanges = function(
						parameterMappings) {
					if (parameterMappings == null) {
						parameterMappings = [];
					}

					this.submitChanges({
						modelElement : {
							parameterMappings : parameterMappings
						}
					});
				};

				/**
				 *
				 */
				EventIntegrationOverlay.prototype.submitOverlayChanges = function(
						parameterMappings) {
					if (parameterMappings == null) {
						parameterMappings = [];
					}

					this
							.submitChanges({
								modelElement : {
									parameterMappings : parameterMappings,
									implementation : this.getImplementation(),
									attributes : {
										"carnot:engine:integration::overlay" : this.id,
										"carnot:engine:camel::camelContextId" : this.getCamelContext(),
										"carnot:engine:camel::camelRouteExt" : this.getRouteContent(),
										"carnot:engine:camel::additionalSpringBeanDefinitions" : this.getAdditionalBeanSpecifications(),
										"carnot:engine:camel::username" : "${camelTriggerUsername}",
										"carnot:engine:camel::password" : "${camelTriggerPassword}"
									}
								}
							});
				};

				/**
				 *
				 */
				EventIntegrationOverlay.prototype.submitRouteChanges = function() {
					this
							.submitChanges({
								modelElement : {
									attributes : {
										"carnot:engine:camel::camelContextId" : this.getCamelContext(),
										"carnot:engine:camel::camelRouteExt" : this.getRouteContent(),
										"carnot:engine:camel::additionalSpringBeanDefinitions" : this
												.getAdditionalBeanSpecifications()
									}
								}
							});
				};
			}

			/**
			 *
			 */
			EventIntegrationOverlay.prototype.validate = function() {
				return true;
			};
		});