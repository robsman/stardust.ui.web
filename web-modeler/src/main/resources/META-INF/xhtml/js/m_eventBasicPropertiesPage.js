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
				"bpm-modeler/js/m_basicPropertiesPage",
				"bpm-modeler/js/m_dataTraversal", "bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_extensionManager, m_session, m_user,
				m_commandsController, m_command, m_dialog,
				m_basicPropertiesPage, m_dataTraversal, m_i18nUtils) {
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
					this.intermediateEventPanel = this
							.mapInputId("intermediateEventPanel");
					this.bindingInformation = this
							.mapInputId("bindingInformation");
					this.interruptingInput = this
							.mapInputId("interruptingInput");
					this.throwingInput = this.mapInputId("throwingInput");
					this.catchingInput = this.mapInputId("catchingInput");
					this.implementationPanel = this
							.mapInputId("implementationPanel");
					this.eventClassSelect = this.mapInputId("eventClassSelect");
					this.eventIntegrationOverlaySelect = this
							.mapInputId("eventIntegrationOverlaySelect");
					this.overlayTableCell = jQuery("#overlayTableCell");

					var eventIntegrationOverlays = m_extensionManager
							.findExtensions("eventIntegrationOverlay");

					this.overlays = {};
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

					this.interruptingInput
							.change(
									{
										"page" : this
									},
									function(event) {
										event.data.page
												.setInterrupting(event.data.page.interruptingInput
														.prop("checked"));
									});
					this.throwingInput.change({
						"page" : this
					}, function(event) {
						event.data.page
								.setThrowing(event.data.page.throwingInput
										.prop("checked"));
					});
					this.catchingInput.change({
						"page" : this
					}, function(event) {
						event.data.page
								.setCatching(event.data.page.catchingInput
										.prop("checked"));
					});
					this.eventClassSelect.change({
						"page" : this
					}, function(event) {
					});
					this.eventIntegrationOverlaySelect
							.change(
									{
										"page" : this
									},
									function(event) {
										page.overlayControllers[page.eventIntegrationOverlaySelect
												.val()].activate();
										page
												.setOverlay(page.eventIntegrationOverlaySelect
														.val());
										page.getModelElement().eventClass = page.extensions[page.eventIntegrationOverlaySelect
												.val()];
									});
				};

				/**
				 * 
				 */
				EventBasicPropertiesPage.prototype.setEventClass = function(
						eventClass) {
					this.eventClassSelect.val(eventClass);
				};

				/**
				 * 
				 */
				EventBasicPropertiesPage.prototype.setInterrupting = function(
						interrupting) {
					this.interruptingInput.prop("checked", interrupting);
				};

				/**
				 * 
				 */
				EventBasicPropertiesPage.prototype.setCatching = function(
						catching) {
					this.catchingInput.prop("checked", true);
					this.throwingInput.prop("checked", false);
				};

				/**
				 * 
				 */
				EventBasicPropertiesPage.prototype.setThrowing = function(
						throwing) {
					this.catchingInput.prop("checked", false);
					this.throwingInput.prop("checked", true);
				};

				/**
				 * 
				 */
				EventBasicPropertiesPage.prototype.populateOverlaySelect = function() {
					this.eventIntegrationOverlaySelect.empty();

					// Add only those overlays, being supported for the event
					// class of the event

					for ( var e in this.extensions) {
						var extension = this.extensions[e];

						if (this.getModelElement().eventClass == extension.eventClass
								&& m_utils.isItemInArray(extension.eventTypes,
										this.getModelElement().eventType)) {
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
					}
				};

				/**
				 * 
				 */
				EventBasicPropertiesPage.prototype.populateEventClassSelect = function() {
					this.eventClassSelect.empty();

					if (this.getModelElement().eventType == m_constants.START_EVENT_TYPE) {
						this.eventClassSelect
								.append("<option value='none'>None</option>");
						this.eventClassSelect
								.append("<option value='message'>Message</option>");
						this.eventClassSelect
								.append("<option value='timer'>Timer</option>");
					} else if (this.getModelElement().eventType == m_constants.INTERMEDIATE_EVENT_TYPE) {
						this.eventClassSelect
								.append("<option value='message'>Message</option>");
						this.eventClassSelect
								.append("<option value='timer'>Timer</option>");

						if (this.getModelElement()) {
							this.eventClassSelect
									.append("<option value='error'>Error</option>");
						}
					} else if (this.getModelElement().eventType == m_constants.STOP_EVENT_TYPE) {
						this.eventClassSelect
								.append("<option value='none'>None</option>");
						this.eventClassSelect
								.append("<option value='message'>Message</option>");
						this.eventClassSelect
								.append("<option value='error'>Error</option>");
					}
				};

				/**
				 * 
				 */
				EventBasicPropertiesPage.prototype.setOverlay = function(
						overlay) {

					for ( var id in this.overlays) {
						m_dialog.makeInvisible(this.overlays[id]);
					}

					if (overlay == null) {
						if (this.getModelElement().eventType == m_constants.START_EVENT) {
							this.setOverlay("manualTrigger");
							this.overlayControllers["manualTrigger"].activate();
						} else {
							return;
						}
					}

					this.eventIntegrationOverlaySelect.val(overlay);

					m_dialog.makeVisible(this.overlays[overlay]);

					// TODO Distinguish from activate; update only on change
					this.overlayControllers[overlay].update();
				};

				/**
				 * 
				 */
				EventBasicPropertiesPage.prototype.setElement = function() {
					this.setModelElement();

					m_utils.debug("Event ");
					m_utils.debug(this.getModelElement());

					this.populateOverlaySelect();
					this.populateEventClassSelect();

					if (this.getModelElement().eventType == m_constants.START_EVENT_TYPE
							|| this.getModelElement().eventType == m_constants.STOP_EVENT_TYPE) {
						this.catchingInput.prop("disabled", true);
						this.throwingInput.prop("disabled", true);
					} else {
						this.catchingInput.prop("disabled", false);
						this.throwingInput.prop("disabled", false);
					}

					if (this.getModelElement().eventType == m_constants.STOP_EVENT_TYPE) {
						this.interruptingInput.prop("disabled", true);
					} else {
						this.interruptingInput.prop("disabled", false);
					}

					this.setInterrupting(this.getModelElement().interrupting);
					this.setCatching(!this.getModelElement().throwing);
					this.setThrowing(this.getModelElement().throwing);

					if (this.getModelElement().eventType == m_constants.INTERMEDIATE_EVENT_TYPE) {
						m_dialog.makeVisible(this.intermediateEventPanel);
						this.bindingInformation.empty();

						// Display, whether event is bound

						if (this.getModelElement().isBoundaryEvent()) {
							this.bindingInformation
									.append(m_i18nUtils
											.getProperty(
													"modeler.diagram.toolbar.tool.event.boundMessage")
											.replace(
													"{0}",
													this.getModelElement().bindingActivityUuid));
						} else {
							this.bindingInformation
									.append(m_i18nUtils
											.getProperty("modeler.diagram.toolbar.tool.event.notBoundMessage"));
						}
					} else {
						m_dialog.makeInvisible(this.intermediateEventPanel);
					}

					// Make visible for all event types, knowing that it would
					// not work with end events

					m_dialog.makeVisible(this.namePanel);
					m_dialog.makeVisible(this.descriptionPanel);

					this
							.setEventClass(this.propertiesPanel.element.modelElement.eventClass);

					if (this.propertiesPanel.element.modelElement.eventType == m_constants.START_EVENT_TYPE) {
						if (this.getModelElement().attributes["carnot:engine:camel::camelContextId"] != null) {
							this.setOverlay("genericCamelRouteEvent");
							this.overlayControllers["genericCamelRouteEvent"]
									.activate();
						} else if (this.getModelElement().documentDataId != null) {
							this.setOverlay("scanEvent");
							this.overlayControllers["scanEvent"].activate();
						} else {
							var overlay = this.getModelElement().attributes["carnot:engine:eventIntegrationOverlay"];

							this.setOverlay(overlay);
						}
					}

					if (m_user.getCurrentRole() == m_constants.INTEGRATOR_ROLE) {
						m_dialog.makeVisible(this.implementationPanel);
					} else {
						m_dialog.makeInvisible(this.implementationPanel);
					}
				};

				/**
				 * TODO Review, symbol returned as model element, because that
				 * is where name and description is bound
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