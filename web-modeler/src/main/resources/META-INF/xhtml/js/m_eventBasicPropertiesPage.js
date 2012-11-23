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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_extensionManager", "bpm-modeler/js/m_session",
				"bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_command", "bpm-modeler/js/m_dialog",
				"bpm-modeler/js/m_basicPropertiesPage", "bpm-modeler/js/m_dataTraversal", "bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_extensionManager, m_session,
				m_commandsController, m_command, m_dialog,
				m_basicPropertiesPage, m_dataTraversal, m_i18nUtils ) {
			return {
				create : function(propertiesPanel) {
					var page = new EventBasicPropertiesPage(propertiesPanel);

					page.initialize();

					return page;
				}
			};

			function EventBasicPropertiesPage(propertiesPanel) {
				var propertiesPage = m_basicPropertiesPage
						.create(propertiesPanel);

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(EventBasicPropertiesPage.prototype,
						propertiesPage);

				/**
				 * 
				 */
				EventBasicPropertiesPage.prototype.initialize = function() {
					this.initializeBasicPropertiesPage();

					this.namePanel = this.mapInputId("annotationName");
					this.descriptionPanel = this.mapInputId("annotationdesc");
					this.startEventPanel = this.mapInputId("startEventPanel");
					this.eventTypeSelectInput = this
							.mapInputId("eventTypeSelectInput");
					this.overlayTableCell = jQuery("#overlayTableCell");

					var eventIntegrationOverlays = m_extensionManager
							.findExtensions("eventIntegrationOverlay");

					this.overlays = {};
					this.overlayControllers = {};

					var extensions = {};

					for ( var n = 0; n < eventIntegrationOverlays.length; n++) {
						var extension = eventIntegrationOverlays[n];

						extensions[extension.id] = extension;

						if (!m_session.initialize().technologyPreview
								&& extension.visibility == "preview") {
							continue;
						}

						this.eventTypeSelectInput.append("<option value='"
								+ extension.id + "'>" + m_i18nUtils
								.getProperty("modeler.element.properties." + extension.id + ".title")
								+ "</option>");

						var pageDiv = jQuery("<div id=\"" + extension.id
								+ "\"></div>");

						this.overlays[extension.id] = pageDiv;

						this.overlayTableCell.append(pageDiv);

						// TODO this variable may be overwritten in the
						// loop, find mechanism to pass data to load
						// callback

						var page = this;

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
												page.overlayControllers[jQuery(
														this).attr("id")] = extension.provider
														.create(page, jQuery(
																this)
																.attr("id"));
												m_utils
														.debug("Overlay loaded: "
																+ jQuery(this)
																		.attr(
																				"id"));
											}
										});
					}

					// Initialize callbacks

					this.eventTypeSelectInput
							.change(
									{
										"page" : this
									},
									function(event) {
										page.overlayControllers[page.eventTypeSelectInput
												.val()].activate();
										page
												.setOverlay(page.eventTypeSelectInput
														.val());
									});

				};

				/**
				 * 
				 */
				EventBasicPropertiesPage.prototype.setOverlay = function(
						overlay) {
					this.eventTypeSelectInput.val(overlay);

					for ( var id in this.overlays) {
						m_dialog.makeInvisible(this.overlays[id]);
					}

					m_dialog.makeVisible(this.overlays[overlay]);
					this.overlayControllers[overlay].update();
				};

				/**
				 * 
				 */
				EventBasicPropertiesPage.prototype.setElement = function() {
					this.setModelElement();

					if (this.propertiesPanel.element.modelElement.eventType == m_constants.START_EVENT_TYPE) {
						m_dialog.makeVisible(this.namePanel);
						m_dialog.makeVisible(this.descriptionPanel);
						m_dialog.makeVisible(this.startEventPanel);

						if (this.propertiesPanel.element.modelElement.eventClass != null) {
							this
									.setOverlay(this.propertiesPanel.element.modelElement.eventClass);
						} else if (this.propertiesPanel.element.modelElement.attributes["carnot:engine:camel::camelContextId"] != null) {
							this.setOverlay("genericCamelRouteEvent");
							this.overlayControllers["genericCamelRouteEvent"].activate();
						} else if (this.propertiesPanel.element.modelElement.documentDataId != null) {
							this.setOverlay("scanEvent");
							this.overlayControllers["scanEvent"].activate();
						} else {
							this.setOverlay("manualTrigger");
							this.overlayControllers["manualTrigger"].activate();
						}
					} else {
						// TODO Allow editing once endEvent becomes a full blown event with triggers
						
						m_dialog.makeInvisible(this.namePanel);
						m_dialog.makeInvisible(this.descriptionPanel);
						m_dialog.makeInvisible(this.startEventPanel);
					}
				};

				/**
				 * TODO Review, symbol returned as model element, because that is where naem and description is bound
				 */
				EventBasicPropertiesPage.prototype.getModelElement = function() {
					return this.propertiesPanel.element.modelElement;
				};

				/**
				 * 
				 */
				EventBasicPropertiesPage.prototype.getEvent = function() {
					return this.propertiesPanel.element.modelElement;
				};

				/**
				 * 
				 */
				EventBasicPropertiesPage.prototype.validate = function() {
					this.propertiesPanel.clearErrorMessages();
					this.nameInput.removeClass("error");

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
						this.propertiesPanel.errorMessages
								.push("Event name must not be empty.");
						this.nameInput.addClass("error");

						this.propertiesPanel.showErrorMessages();

						return false;
					}

					return true;
				};
			}
		});