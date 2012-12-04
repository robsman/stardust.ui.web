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
					this.eventClassSelect = this.mapInputId("eventClassSelect");

					this.interruptingInput
							.change(
									{
										"page" : this
									},
									function(event) {
										var page = 
											event.data.page;
						
										page
												.setInterrupting(page.interruptingInput
														.prop("checked"));
										page.submitChanges({modelElement: {interrupting: page.interruptingInput
											.prop("checked")}});
									});
					this.throwingInput.change({
						"page" : this
					}, function(event) {
						var page = 
							event.data.page;
						
						page
								.setThrowing(page.throwingInput
										.prop("checked"));
						page.submitChanges({modelElement: {throwing: page.throwingInput
							.prop("checked")}});
					});
					this.catchingInput.change({
						"page" : this
					}, function(event) {
						var page = 
							event.data.page;
						
						page
								.setCatching(page.catchingInput
										.prop("checked"));
						page.submitChanges({modelElement: {throwing: !page.throwingInput
							.prop("checked")}});
					});
					this.eventClassSelect.change({
						"page" : this
					}, function(event) {
						var page = 
							event.data.page;
						
						page
								.getModelElement().eventClass = page.eventClassSelect.val();
						page.submitChanges({modelElement: page.getModelElement()});
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
					this.catchingInput.prop("checked", catching);
					this.throwingInput.prop("checked", !catching);
				};

				/**
				 * 
				 */
				EventBasicPropertiesPage.prototype.setThrowing = function(
						throwing) {
					this.catchingInput.prop("checked", !throwing);
					this.throwingInput.prop("checked", throwing);
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
				EventBasicPropertiesPage.prototype.setElement = function() {
					this.setModelElement();

					m_utils.debug("Event ");
					m_utils.debug(this.getModelElement());

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
													"modeler.eventPropertiesPanel.basicPropertiesPage.boundMessage")
											.replace(
													"{0}",
													this.getModelElement().bindingActivityUuid));
						} else {
							this.bindingInformation
									.append(m_i18nUtils
											.getProperty("modeler.eventPropertiesPanel.basicPropertiesPage.notBoundMessage"));
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
					// We allow empty names
					
					return true;
				};
			}
		});