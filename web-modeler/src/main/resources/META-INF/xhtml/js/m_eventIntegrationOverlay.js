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
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_accessPoint", "bpm-modeler/js/m_parameterDefinitionsPanel" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_model, m_accessPoint, m_parameterDefinitionsPanel) {

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
										"parameterDefinitionsPanel.html",
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
															readOnlyParameterList : true
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
				EventIntegrationOverlay.prototype.submitChanges = function(
						changes) {
					this.page.submitChanges(changes);
				};

				/**
				 * 
				 */
				EventIntegrationOverlay.prototype.submitOverlayChanges = function(
						parameterMappings) {
					if (parameterMappings == null) {
						parameterMappings = [];
					}

					var route = "<route>";

					route += "<from uri=\"";
					route += this.getEndpointUri();
					route += "\"/></route>";

					this.submitChanges({
						modelElement : {
							parameterMappings : parameterMappings,
							attributes : {
								"carnot:engine:integration::overlay" : this.id,
								"carnot:engine:camel::camelRouteExt" : route
							}
						}
					});
				};

				/**
				 * 
				 */
				EventIntegrationOverlay.prototype.submitRouteChanges = function() {
					var route = "<route>";

					route += "<from uri=\"";
					route += this.getEndpointUri();
					route += "\"/></route>";

					this.submitChanges({
						modelElement : {
							attributes : {
								"carnot:engine:camel::camelRouteExt" : route
							}
						}
					});
				};
			}
		});