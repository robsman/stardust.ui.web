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
				"bpm-modeler/js/m_session", "bpm-modeler/js/m_user",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_dialog",
				"bpm-modeler/js/m_propertiesPage",
				"bpm-modeler/js/m_dataTraversal", "bpm-modeler/js/m_i18nUtils",
				"bpm-modeler/js/m_model" ],
		function(m_utils, m_constants, m_extensionManager, m_session, m_user,
				m_commandsController, m_command, m_dialog, m_propertiesPage,
				m_dataTraversal, m_i18nUtils, m_model) {
			return {
				create : function(propertiesPanel) {
					var page = new EventImplementationPropertiesPage(
							propertiesPanel);

					page.initialize();

					return page;
				}
			};

			/**
			 *
			 */
			function EventImplementationPropertiesPage(propertiesPanel) {
				var propertiesPage = m_propertiesPage
						.createPropertiesPage(propertiesPanel,
								"implementationPropertiesPage",
								"Implementation", // TODO I18N
								"../../images/icons/event-implementation-properties-page.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(
						EventImplementationPropertiesPage.prototype,
						propertiesPage);

				/**
				 *
				 */
				EventImplementationPropertiesPage.prototype.initialize = function() {
					this.noImplementationPanel = this
							.mapInputId("noImplementationPanel");
					this.implementationPanel = this
							.mapInputId("implementationPanel");
					this.eventIntegrationOverlaySelect = this
							.mapInputId("eventIntegrationOverlaySelect");
					this.overlayTableCell = this.mapInputId("overlayTableCell");

					var eventIntegrationOverlays = m_extensionManager
							.findExtensions("eventIntegrationOverlay");

					this.overlays = {};
					this.overlay = null;
					this.supportedOverlays = {};
					this.overlayControllers = {};
					this.extensions = {};

					for ( var n = 0; n < eventIntegrationOverlays.length; n++) {
						var extension = eventIntegrationOverlays[n];

						this.extensions[extension.id] = extension;

						if (!m_session.initialize().technologyPreview
								&& extension.visibility == "preview") {
							continue;
						}

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
												var extension = page.extensions[jQuery(
														this).attr("id")];
												page.overlayControllers[jQuery(
														this).attr("id")] = extension.provider
														.create(page, jQuery(
																this)
																.attr("id"));
												m_dialog
														.makeInvisible(page.overlays[extension.id]);
											}
										});
					}

					this.eventIntegrationOverlaySelect
							.change(
									{
										"page" : this
									},
									function(event) {
										if (event.data.page.eventIntegrationOverlaySelect
												.val() != m_constants.TO_BE_DEFINED) {
											event.data.page.overlayControllers[page.eventIntegrationOverlaySelect
													.val()].activate();
										} else {
											event.data.page
													.submitNoneImplementation();
										}
									});
				};

				/**
				 *
				 */
				EventImplementationPropertiesPage.prototype.populateSupportedOverlays = function() {
					this.supportedOverlays = {};

					// Add only those overlays, being supported for the event
					// class of the event

					for ( var e in this.extensions) {
						var extension = this.extensions[e];

						if (this.getModelElement().eventClass == extension.eventClass
								&& m_utils.isItemInArray(extension.eventTypes,
										this.getModelElement().eventType)) {
							this.supportedOverlays[extension.id] = extension;
						}
					}
				};

				/**
				 *
				 */
				EventImplementationPropertiesPage.prototype.submitNoneImplementation = function() {
					// Event class change needs to be submitted as well
					
					this.submitChanges({
						modelElement : {
							participantFullId : null,
							eventClass: this.getModelElement().eventClass,
							implementation : "none"
						}
					});
				};

				/**
				 *
				 */
				EventImplementationPropertiesPage.prototype.populateOverlaySelect = function() {
					this.eventIntegrationOverlaySelect.empty();
					this.eventIntegrationOverlaySelect.append("<option value='"
							+ m_constants.TO_BE_DEFINED
							+ "'>"
							+ m_i18nUtils
									.getProperty("modeler.general.toBeDefined")
							+ "</option>");

					for ( var e in this.supportedOverlays) {
						var extension = this.extensions[e];

						this.eventIntegrationOverlaySelect
								.append("<option value='"
										+ extension.id
										+ "'>"
										+ m_i18nUtils
												.getProperty("modeler.element.properties."
														+ extension.id
														+ ".title")
										+ "</option>");
					}
				};

				/**
				 *
				 */
				EventImplementationPropertiesPage.prototype.setOverlay = function(
						overlay) {
					if (overlay == m_constants.TO_BE_DEFINED) {
						overlay == null;
					}

					for ( var id in this.overlays) {
						m_dialog.makeInvisible(this.overlays[id]);
					}

					// TODO Review - may not be needed anymore
					if (overlay == null) {
						if (this.getModelElement().eventType == m_constants.START_EVENT) {
							this.overlayControllers["manualTrigger"].activate();
							this.setOverlay("manualTrigger");
						} else {
							this.eventIntegrationOverlaySelect
									.val(m_constants.TO_BE_DEFINED);

							return;
						}
					}

					this.eventIntegrationOverlaySelect.val(overlay);

					m_dialog.makeVisible(this.overlays[overlay]);
					this.overlayControllers[overlay].update();
				};

				/**
				 *
				 */
				EventImplementationPropertiesPage.prototype.setElement = function() {
					m_utils.debug("Event ");
					m_utils.debug(this.getModelElement());

					this.populateSupportedOverlays();
					this.populateOverlaySelect();

					if (m_user.getCurrentRole() == m_constants.INTEGRATOR_ROLE && this.getModelElement().eventType != m_constants.STOP_EVENT_TYPE) {
						m_dialog.makeInvisible(this.noImplementationPanel);
						m_dialog.makeVisible(this.implementationPanel);

						var overlay = null;

						if (this.getModelElement().implementation == "manual") {
							overlay = "manualTrigger";
						} else if (this.getModelElement().implementation == "scan") {
							overlay = "scanEvent";
						} else if (this.getModelElement().implementation == "camel") {
							overlay = this.getModelElement().attributes["carnot:engine:integration::overlay"];

							if (overlay == null) {
								overlay = "genericCamelRouteEvent";
							}
						}

						if (this.supportedOverlays[overlay]) {
							this.setOverlay(overlay);
							this.overlay = this.overlayControllers[overlay];
							this.overlay.update();
						} else {
							this.setOverlay(null);
						}
					} else {
						this.noImplementationPanel.empty();
						this.noImplementationPanel
								.append("No implementation available."); // TODO
						// I18N
						m_dialog.makeVisible(this.noImplementationPanel);
						m_dialog.makeInvisible(this.implementationPanel);
					}
				};

				/**
				 * TODO Review, symbol returned as model element, because that
				 * is where name and description is bound
				 */
				EventImplementationPropertiesPage.prototype.getModelElement = function() {
					return this.propertiesPanel.element.modelElement;
				};

				/**
				 *
				 */
				EventImplementationPropertiesPage.prototype.getEvent = function() {
					return this.propertiesPanel.element.modelElement;
				};

				/**
				 *
				 */
				EventImplementationPropertiesPage.prototype.validate = function(
						changes) {
					this.propertiesPanel.clearErrorMessages();

					if (changes && changes.modelElement
							&& "scan" == changes.modelElement.implementation
							&& this.getElement().parentSymbol.participantFullId) {
						var participant = m_model.findParticipant(this
								.getElement().parentSymbol.participantFullId);

						if (m_constants.CONDITIONAL_PERFORMER_PARTICIPANT_TYPE == participant.type) {
							this.propertiesPanel.errorMessages
									.push(m_i18nUtils
											.getProperty("modeler.swimlane.properties.conditionalParticipant.scanTrigger.error"));
							this.propertiesPanel.showErrorMessages();
							return false;
						}
					}

					if (this.overlay) {
						if (this.overlay.validate()) {
							return true;
						}

						return false;
					}

					return true;
				};
			}
		});