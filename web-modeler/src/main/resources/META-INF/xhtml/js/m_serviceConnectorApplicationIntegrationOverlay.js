/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define(
		[ "m_utils", "m_constants", "m_commandsController", "m_command",
				"m_parameterDefinitionsPanel" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_parameterDefinitionsPanel) {
			return {
				create : function(view) {
					var overlay = new ServiceConnectorApplicationIntegrationOverlay();

					overlay.initialize(view);

					return overlay;
				}
			};

			/**
			 * 
			 */
			function ServiceConnectorApplicationIntegrationOverlay() {
				/**
				 * 
				 */
				ServiceConnectorApplicationIntegrationOverlay.prototype.initialize = function(
						view) {
					this.view = view;
					this.serviceTypeSelectInput = jQuery("#serviceTypeSelectInput");
					this.systemTextInput = jQuery("#systemTextInput");
					this.serviceNameTextInput = jQuery("#serviceNameTextInput");

					this.parameterDefinitionsPanel = m_parameterDefinitionsPanel
							.create({
								scope : "serviceConnectorApplicationIntegrationOverlay",
								submitHandler : this,
								listType : "object",
								supportsDataMappings : false,
								supportsDescriptors : false,
								supportsDataTypeSelection : true
							});

					this.serviceTypeSelectInput
					.change(
							{
								overlay : this
							},
							function(event) {
								attributes = {};
								attributes["carnot:engine:camel:serviceConnectorApplicationIntegrationOverlay::serviceType"] = event.data.overlay.serviceTypeSelectInput.val();

								event.data.overlay.view.submitChanges({
									attributes : attributes
								});
							});

					this.systemTextInput
							.change(
									{
										overlay : this
									},
									function(event) {
										var overlay = event.data.overlay;
										attributes = {};
										attributes["carnot:engine:camel::routeEntries"] = "<to uri=\"isb://service/"
												+ overlay.systemTextInput.val()
												+ "/"
												+ overlay.serviceNameTextInput
														.val() + "\"/>";
										overlay.view.submitChanges({
											attributes : attributes
										});
									});
					this.serviceNameTextInput
							.change(
									{
										overlay : this
									},
									function(event) {
										var overlay = event.data.overlay;
										attributes = {};
										attributes["carnot:engine:camel::routeEntries"] = "<to uri=\"isb://service/"
												+ overlay.systemTextInput.val()
												+ "/"
												+ overlay.serviceNameTextInput
														.val() + "\"/>";
										overlay.view.submitChanges({
											attributes : attributes
										});
									});
				};

				/**
				 * 
				 */
				ServiceConnectorApplicationIntegrationOverlay.prototype.getModelElement = function() {
					return this.view.getModelElement();
				};

				/**
				 * 
				 */
				ServiceConnectorApplicationIntegrationOverlay.prototype.setServiceType = function(serviceType) {
					this.serviceTypeSelectInput.val(serviceType);
				};

				/**
				 * 
				 */
				ServiceConnectorApplicationIntegrationOverlay.prototype.activateOverlay = function() {
					this.setServiceType(this.getModelElement().attributes["carnot:engine:camel:serviceConnectorApplicationIntegrationOverlay::serviceType"]);
					
					var xml = jQuery(jQuery
							.parseXML(this.getModelElement().attributes["carnot:engine:camel::routeEntries"]));

					var overlay = this;
					
					jQuery(xml).find("to").each(function() {
						m_utils.debug("URI: " + jQuery(this).attr("uri"));
						var uriFragments = jQuery(this).attr("uri").split("/");

						if (uriFragments[0] == "isb:") {
							overlay.systemTextInput.val(uriFragments[3]);
							overlay.serviceNameTextInput.val(uriFragments[4]);
						}
					});

					// TODO Code Guard. Needed?

					if (this.getModelElement().accessPoints == null) {
						this.getModelElement().accessPoints = {};
					}

					this.parameterDefinitionsPanel.setScopeModel(this
							.getModelElement().model);
					this.parameterDefinitionsPanel.setParameterDefinitions(this
							.getModelElement().accessPoints);

					attributes = {};
					attributes["carnot:engine:camel::applicationIntegrationOverlay"] = "serviceConnectorApplicationIntegrationOverlay";

					this.view.submitChanges({
						attributes : attributes
					});
				};

				/**
				 * Callback for parameterDefinitionsPanel.
				 */
				ServiceConnectorApplicationIntegrationOverlay.prototype.submitParameterDefinitionsChanges = function(
						accessPoints) {
					this.view.submitChanges({
						"accessPoints" : accessPoints
					});
				};

			}
		});