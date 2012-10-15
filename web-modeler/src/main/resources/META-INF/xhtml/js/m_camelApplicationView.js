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
		[ "m_utils", "m_constants", "m_extensionManager", "m_session",
				"m_commandsController", "m_dialog", "m_modelElementView",
				"m_model" ],
		function(m_utils, m_constants, m_extensionManager, m_session,
				m_commandsController, m_dialog, m_modelElementView, m_model) {
			return {
				initialize : function(fullId) {
					var view = new CamelApplicationView();

					// TODO Unregister!
					// In Initializer?
					m_commandsController.registerCommandHandler(view);

					view.initialize(m_model.findApplication(fullId));
				}
			};

			/**
			 * 
			 */
			function CamelApplicationView() {
				var modelElementView = m_modelElementView.create();

				m_utils.inheritFields(this, modelElementView);
				m_utils.inheritMethods(CamelApplicationView.prototype,
						modelElementView);

				/**
				 * 
				 */
				CamelApplicationView.prototype.initialize = function(
						application) {
					this.id = "camelApplicationView";

					this.overlayTableCell = jQuery("#overlayTableCell");
					this.genericEndpointOverlay = jQuery("#overlayTableCell #genericEndpoint");
					this.camelContextInput = jQuery("#camelContextInput");
					this.routeTextarea = jQuery("#routeTextarea");
					this.endpointTypeSelectInput = jQuery("#endpointTypeSelectInput");
					this.additionalBeanSpecificationTextarea = jQuery("#additionalBeanSpecificationTextarea");
					this.requestDataInput = jQuery("#requestDataInput");
					this.responseDataInput = jQuery("#responseDataInput");

					this.overlays = {};
					this.overlayControllers = {};

					this.overlays["genericEndpoint"] = this.genericEndpointOverlay;
					this.overlayControllers["genericEndpoint"] = this;

					this.registerInputForModelElementAttributeChangeSubmission(
							this.camelContextInput,
							"carnot:engine:camel::camelContextId");
					this.registerInputForModelElementAttributeChangeSubmission(
							this.routeTextarea,
							"carnot:engine:camel::routeEntries");
					this
							.registerInputForModelElementAttributeChangeSubmission(
									this.additionalBeanSpecificationTextarea,
									"carnot:engine:camel::additionalSpringBeanDefinitions");

					var integrationApplicationOverlays = m_extensionManager
							.findExtensions("applicationIntegrationOverlay");

					var extensions = {};

					for ( var n = 0; n < integrationApplicationOverlays.length; n++) {
						var extension = integrationApplicationOverlays[n];

						extensions[extension.id] = extension;

						if (!m_session.initialize().technologyPreview
								&& extension.visibility == "preview") {
							continue;
						}

						this.endpointTypeSelectInput.append("<option value='"
								+ extension.id + "'>" + extension.name
								+ "</option>");

						var pageDiv = jQuery("<div id=\"" + extension.id
								+ "\"></div>");

						this.overlays[extension.id] = pageDiv;

						this.overlayTableCell.append(pageDiv);

						// TODO this variable may be overwritten in the
						// loop, find mechanism to pass data to load
						// callback

						var view = this;

						pageDiv
								.load(
										extension.pageHtmlUrl,
										function(response, status, xhr) {
											if (status == "error") {
												var msg = "Properties Page Load Error: "
														+ xhr.status
														+ " "
														+ xhr.statusText;

												jQuery(this).append(msg);
											} else {
												var extension = extensions[jQuery(
														this).attr("id")];
												view.overlayControllers[jQuery(
														this).attr("id")] = extension.provider
														.create(view);
											}
										});
					}

					this.endpointTypeSelectInput.change({
						view : this
					}, function(event) {
						var view = event.data.view;

						view.overlayControllers[view.endpointTypeSelectInput
								.val()].activate();
						view.setOverlay(view.endpointTypeSelectInput.val());

					});

					this.initializeModelElementView(application);
				};

				/**
				 * 
				 */
				CamelApplicationView.prototype.setOverlay = function(overlay) {
					this.endpointTypeSelectInput.val(overlay);

					for ( var id in this.overlays) {
						m_dialog.makeInvisible(this.overlays[id]);
					}

					m_dialog.makeVisible(this.overlays[overlay]);
					this.overlayControllers[overlay].update();
				};

				/**
				 * Overlay protocol
				 */
				CamelApplicationView.prototype.activate = function() {
					if (this.application.attributes["carnot:engine:camel::camelContextId"] == null) {
						this.submitChanges({attributes: {"carnot:engine:camel::camelContextId": "Default"}});
					}
				};

				/**
				 * Overlay protocol
				 */
				CamelApplicationView.prototype.update = function() {
					this.camelContextInput
							.val(this.application.attributes["carnot:engine:camel::camelContextId"]);
					this.routeTextarea
							.val(this.application.attributes["carnot:engine:camel::routeEntries"]);
					this.additionalBeanSpecificationTextarea
							.val(this.application.attributes["carnot:engine:camel::additionalSpringBeanDefinitions"]);
				};

				/**
				 * 
				 */
				CamelApplicationView.prototype.setModelElement = function(
						application) {
					this.application = application;

					m_utils.debug("===> Application");
					m_utils.debug(application);
					
					this.initializeModelElement(application);

					if (this.application.attributes["carnot:engine:camel::applicationIntegrationOverlay"] == null) {
						this.setOverlay("genericEndpoint");
					} else {
						this
								.setOverlay(this.application.attributes["carnot:engine:camel::applicationIntegrationOverlay"]);
					}
				};

				/**
				 * 
				 */
				CamelApplicationView.prototype.toString = function() {
					return "Lightdust.CamelApplicationView";
				};

				/**
				 * 
				 */
				CamelApplicationView.prototype.validate = function() {
					this.clearErrorMessages();

					this.nameInput.removeClass("error");
					this.camelContextInput.removeClass("error");

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
						this.errorMessages
								.push("Application name must not be empty.");
						this.nameInput.addClass("error");
					}

					if (this.camelContextInput.val() == null
							|| this.camelContextInput.val() == "") {
						this.errorMessages
								.push("Camel Context must not be empty.");
						this.nameInput.addClass("error");
					}

					if (this.errorMessages.length > 0) {
						this.showErrorMessages();

						return false;
					}

					return true;
				};
			}
		});