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
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_event", "bpm-modeler/js/m_dialog",
				"bpm-modeler/js/m_basicPropertiesPage",
				"bpm-modeler/js/m_dataTraversal", "bpm-modeler/js/m_i18nUtils", "bpm-modeler/js/m_model"],
		function(m_utils, m_constants, m_extensionManager, m_session, m_user,
				m_commandsController, m_command, m_event, m_dialog,
				m_basicPropertiesPage, m_dataTraversal, m_i18nUtils, m_model) {
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
					this.participantOutput = this
							.mapInputId("participantOutput");

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

					var interrupting = this.getModelElement().interrupting;

					if (this.getModelElement().eventType == m_constants.INTERMEDIATE_EVENT_TYPE) {
						interrupting = true;
					}

					var eventClasses = m_event.getPossibleEventClasses(this.getModelElement().eventType, interrupting,
							this.getModelElement().throwing,
							this.getModelElement().isBoundaryEvent(), false/* subProcess */);

					for (var n = 0; n < eventClasses.length; ++n)
						{
						this.eventClassSelect
						.append("<option value='" + eventClasses[n] + "'>" + m_i18nUtils
								.getProperty("modeler.eventPropertiesPanel.basicPropertiesPage.eventClass." + eventClasses[n]) + "</option>");
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
						this.catchingInput.hide();
						this.throwingInput.hide();
						m_utils.jQuerySelect("label[for='catchingInput']").hide();
						m_utils.jQuerySelect("label[for='throwingInput']").hide();
					} else {
						this.catchingInput.show();
						this.throwingInput.show();
						m_utils.jQuerySelect("label[for='catchingInput']").show();
						m_utils.jQuerySelect("label[for='throwingInput']").show();
					}

					if (this.getModelElement().eventType == m_constants.STOP_EVENT_TYPE) {
						this.interruptingInput.hide();
						this.participantOutput.hide();
						m_utils.jQuerySelect("label[for='interruptingInput']").hide();
					} else {
						this.interruptingInput.show();
						this.participantOutput.show();
						m_utils.jQuerySelect("label[for='interruptingInput']").show();
					}

					this.setInterrupting(this.getModelElement().interrupting);
					this.setCatching(!this.getModelElement().throwing);
					this.setThrowing(this.getModelElement().throwing);

					if (this.getModelElement().eventType == m_constants.INTERMEDIATE_EVENT_TYPE) {
						m_dialog.makeVisible(this.intermediateEventPanel);
						this.bindingInformation.empty();

//						this.catchingInput.prop("checked", true);
//						this.throwingInput.prop("checked", false);
//						this.catchingInput.prop("disabled", true);
//						this.throwingInput.prop("disabled", true);

						this.catchingInput.hide();
						this.throwingInput.hide();
						$("label[for='catchingInput']").hide();
						$("label[for='throwingInput']").hide();

						if (this.propertiesPanel.element.modelElement.eventClass == m_constants.ERROR_EVENT_CLASS) {
							this.interruptingInput.prop("checked", true);
							this.interruptingInput.prop("disabled", true);
						} else {
							this.interruptingInput.prop("disabled", false);
						}

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

					// TODO I18N

					this.participantOutput.empty();

					if (this.propertiesPanel.participant != null &&
							(this.propertiesPanel.element.modelElement.eventClass == m_constants.NONE_EVENT_CLASS ||
									this.propertiesPanel.element.modelElement.eventClass == m_constants.MESSAGE_EVENT_CLASS)) {
						this.participantOutput.append("Started by <b>"
								+ this.propertiesPanel.participant.name
								+ ".</b>");
					} else {
						this.participantOutput
								.append("No starting participant.</b>");
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
				EventBasicPropertiesPage.prototype.validate = function(changes) {
					m_utils.debug("===> Validate EventBasicPropertiesPage");
					this.propertiesPanel.clearErrorMessages();
					if (changes && changes.modelElement && !this.validateEventClass(changes.modelElement.eventClass)){
							return false;
					}
					return true;
				};

				EventBasicPropertiesPage.prototype.validateEventClass = function(eventClass){
					if (m_constants.NONE_EVENT_CLASS == eventClass
							&& this.getElement().parentSymbol.participantFullId) {
						var participant = m_model.findParticipant(this.getElement().parentSymbol.participantFullId);

						if (m_constants.CONDITIONAL_PERFORMER_PARTICIPANT_TYPE == participant.type) {
							this.propertiesPanel.errorMessages
									.push(m_i18nUtils
											.getProperty("modeler.swimlane.properties.conditionalParticipant.manualTrigger.error"));
							this.propertiesPanel.showErrorMessages();
							return false;
						}
					}
					return true;
				};
			}
		});