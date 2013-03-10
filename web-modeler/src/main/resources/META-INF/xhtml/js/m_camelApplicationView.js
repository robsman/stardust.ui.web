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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_extensionManager",
				"bpm-modeler/js/m_session",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_dialog", "bpm-modeler/js/m_modelElementView",
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_extensionManager, m_session,
				m_commandsController, m_dialog, m_modelElementView, m_model,
				m_i18nUtils) {
			return {
				initialize : function(fullId) {
					var view = new CamelApplicationView();
					i18camelrouteproperties();
					// TODO Unregister!
					// In Initializer?
					m_commandsController.registerCommandHandler(view);

					view.initialize(m_model.findApplication(fullId));
				}
			};

			function i18camelrouteproperties() {
				$("label[for='guidOutput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.uuid"));

				$("label[for='idOutput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.id"));

				jQuery("#applicationName")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.applicationName"));
				jQuery("#description")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));
				jQuery("#camelConfiguration")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.camelRoute.camelConfigurationProperties.tab"));
				jQuery("#camelContext")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.camelRoute.camelConfigurationProperties.camelContext"));

				jQuery("#addBeanSpec")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.camelRoute.camelConfigurationProperties.additionalBeanSpecification"));
				jQuery("#direction")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.direction"));
				jQuery("label[for='publicVisibilityCheckbox']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.publicVisibility"));
			}
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
					this.application = application;

					this.publicVisibilityCheckbox = jQuery("#publicVisibilityCheckbox");
					this.overlayAnchor = jQuery("#overlayAnchor");

					this.publicVisibilityCheckbox
							.change(
									{
										"view" : this
									},
									function(event) {
										var view = event.data.view;

										if (!view.validate()) {
											return;
										}

										if (view.modelElement.attributes["carnot:engine:visibility"]
												&& view.modelElement.attributes["carnot:engine:visibility"] != "Public") {
											view
													.submitChanges({
														attributes : {
															"carnot:engine:visibility" : "Public"
														}
													});
										} else {
											view
													.submitChanges({
														attributes : {
															"carnot:engine:visibility" : "Private"
														}
													});
										}
									});

					if (this.application.attributes["carnot:engine:camel::applicationIntegrationOverlay"] == null) {
						this.setOverlay("genericEndpoint");
					} else {
						this
								.setOverlay(this.application.attributes["carnot:engine:camel::applicationIntegrationOverlay"]);
					}

					this.initializeModelElementView(application);
				};

				/**
				 * 
				 */
				CamelApplicationView.prototype.insertPropertiesTab = function(
						scope, id, name, icon) {
					var propertiesTabs = jQuery("#propertiesTabs");
					var propertiesTabsList = jQuery("#propertiesTabsList");
					var lastListItem = propertiesTabsList.children().last();

					// propertiesTabsList.append("<li><a href='#" + id
					// + "Tab'><img src='" + icon
					// + "'></img><span class='tabLabel' id='" + id + "'>"
					// + name + "</span> </a></li>");

					lastListItem.before("<li><a href='#" + id
							+ "Tab'><img src='" + icon
							+ "'></img><span class='tabLabel' id='" + id + "'>"
							+ name + "</span> </a></li>");

					var html = jQuery("#" + scope + " #" + id + "Tab").html();

					jQuery("#" + scope + " #" + id + "Tab").empty();
					propertiesTabs.append("<div id='" + id + "Tab'>" + html
							+ "</div>");
				};

				/**
				 * 
				 */
				CamelApplicationView.prototype.setOverlay = function(overlay) {
					var extension = m_extensionManager.findExtensions(
							"applicationIntegrationOverlay", "id", overlay)[0];

					this.overlayAnchor.empty();

					var view = this;

					this.overlayAnchor.load(extension.pageHtmlUrl, function(
							response, status, xhr) {
						if (status == "error") {
							var msg = "Properties Page Load Error: "
									+ xhr.status + " " + xhr.statusText;

							jQuery(this).append(msg);
						} else {
							view.overlayController = extension.provider
									.create(view);
						}
					});
				};

				/**
				 * 
				 */
				CamelApplicationView.prototype.getModelElement = function() {
					return this.application;
				};

				/**
				 * 
				 */
				CamelApplicationView.prototype.getApplication = function() {
					return this.application;
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

					if (!this.application.attributes["carnot:engine:visibility"]
							|| "Public" == this.application.attributes["carnot:engine:visibility"]) {
						this.publicVisibilityCheckbox.attr("checked", true);
					} else {
						this.publicVisibilityCheckbox.attr("checked", false);
					}

					this.overlayController.update();
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

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
						this.errorMessages
								.push("Application name must not be empty.");
						this.nameInput.addClass("error");
					}

					if (this.overlayController) {
						this.overlayController.validate();
					}

					if (this.errorMessages.length > 0) {
						this.showErrorMessages();

						return false;
					}

					return true;
				};
			}
		});