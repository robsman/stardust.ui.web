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
					m_utils.initializeWaitCursor(m_utils.jQuerySelect("html"));
					m_utils.showWaitCursor();

					var view = new CamelApplicationView();
					i18camelrouteproperties();
					// TODO Unregister!
					// In Initializer?
					m_commandsController.registerCommandHandler(view);

					view.initialize(m_model.findApplication(fullId));

					m_utils.hideWaitCursor();
				}
			};

			function i18camelrouteproperties() {
				m_utils.jQuerySelect("label[for='guidOutput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.uuid"));

				m_utils.jQuerySelect("label[for='idOutput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.id"));

				m_utils.jQuerySelect("#applicationName")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.applicationName"));
				m_utils.jQuerySelect("#description")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));
				m_utils.jQuerySelect("#camelConfiguration")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.camelRoute.camelConfigurationProperties.tab"));
				m_utils.jQuerySelect("#camelContext")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.camelRoute.camelConfigurationProperties.camelContext"));

				m_utils.jQuerySelect("#addBeanSpec")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.camelRoute.camelConfigurationProperties.additionalBeanSpecification"));
				m_utils.jQuerySelect("#direction")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.direction"));
				m_utils.jQuerySelect("label[for='publicVisibilityCheckbox']")
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

					this.view = m_utils.jQuerySelect("#camelApplicationView");

					this.publicVisibilityCheckbox = m_utils.jQuerySelect("#publicVisibilityCheckbox");
					this.overlayAnchor = m_utils.jQuerySelect("#overlayAnchor");

					this.publicVisibilityCheckbox
							.change(
									{
										"view" : this
									},
									function(event) {
										var view = event.data.view;

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

					var self = this;

					if (this.application.attributes["carnot:engine:camel::applicationIntegrationOverlay"] == null) {
						this
								.setOverlay("genericEndpointOverlay")
								.done(
										function() {
											self
													.initializeModelElementView(application);
											self.view.css("visibility",
													"visible");
										});
					} else {
						this
								.setOverlay(
										this.application.attributes["carnot:engine:camel::applicationIntegrationOverlay"])
								.done(
										function() {
											self
													.initializeModelElementView(application);
											self.view.css("visibility",
													"visible");
										});
					}
				};

				/**
				 * 
				 */
				CamelApplicationView.prototype.insertPropertiesTab = function(
						scope, id, name, icon) {
					var propertiesTabs = m_utils.jQuerySelect("#propertiesTabs");
					var propertiesTabsList = m_utils.jQuerySelect("#propertiesTabsList");
					var lastListItem = propertiesTabsList.children().last();

					// propertiesTabsList.append("<li><a href='#" + id
					// + "Tab'><img src='" + icon
					// + "'></img><span class='tabLabel' id='" + id + "'>"
					// + name + "</span> </a></li>");

					lastListItem.before("<li><a href='#" + id
							+ "Tab'><img src='" + icon
							+ "'></img><span class='tabLabel' id='" + id + "'>"
							+ name + "</span> </a></li>");

					var elem = m_utils.jQuerySelect("#" + scope + " #" + id + "Tab")
					var html = elem.html();

					m_utils.jQuerySelect("#" + scope + " #" + id + "Tab").empty();
					propertiesTabs.append("<div id='" + id + "Tab'>" + html
							+ "</div>");
					// After append remove old contents
					if (elem.length > 0) {
						elem[0].parentNode.removeChild(elem[0]);
					}
				};

				/**
				 * 
				 */
				CamelApplicationView.prototype.setOverlay = function(overlay) {
					var deferred = jQuery.Deferred();
					var extension = m_extensionManager.findExtensions(
							"applicationIntegrationOverlay", "id", overlay)[0];

					this.overlayAnchor.empty();

					var self = this;

					jQuery.ajax({
						type : 'GET',
						url : extension.pageHtmlUrl,
						async : false
					}).done(
							function(data) {
								self.overlayAnchor.append(data);

								self.overlayController = extension.provider
										.create(self);

								// Make sure that initial information for the
								// overlay is written to the server
								// TODO Ideally, this would only be invoked on
								// creation, but currently, the overlay code is
								// not bound at creation time, just the overlay
								// ID
								// is written. Hence, we are invoking per View
								// initialization

								self.overlayController.activate();

								deferred.resolve();
							}).fail(function(data) {
						self.overlayAnchor.append(data);

						deferred.reject();
					});

					return deferred.promise();
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

					m_utils.debug("===> Updating Overlay");

					this.overlayController.update();
					m_utils.debug("===> Done updating");
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