/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Marc.Gille
 */
define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_command",
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_basicPropertiesPage", "bpm-modeler/js/m_participant","bpm-modeler/js/m_i18nUtils",
				"bpm-modeler/js/m_modelElementUtils", "bpm-modeler/js/m_messageDisplay", "bpm-modeler/js/m_modelerUtils" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_model, m_basicPropertiesPage, m_participant, m_i18nUtils, m_modelElementUtils, m_messageDisplay, m_modelerUtils) {
			return {
				create : function(propertiesPanel) {
					var page = new SwimlaneBasicPropertiesPage(propertiesPanel);

					page.initialize();

					return page;
				}
			};

			/**
			 *
			 */
			function SwimlaneBasicPropertiesPage(newPropertiesPanel, newId,
					newTitle) {

				// Inheritance

				var basicPropertiesPage = m_basicPropertiesPage
						.create(newPropertiesPanel);

				m_utils.inheritFields(this, basicPropertiesPage);
				m_utils.inheritMethods(SwimlaneBasicPropertiesPage.prototype,
						basicPropertiesPage);

				/**
				 *
				 */
				SwimlaneBasicPropertiesPage.prototype.initialize = function() {
					this.initializeBasicPropertiesPage();

					this.propertiesPanelTitle = m_utils.jQuerySelect("#swimlanePropertiesPanelTitle");
					this.participantViewLink = m_utils.jQuerySelect("#participantViewLink");
					this.newParticipantName = this
							.mapInputId("newParticipantName");
					this.participantList = this.mapInputId("participantList");

					this.registerInputForModelElementChangeSubmission(
							this.participantList, "participantFullId");

					this.participantViewLink.click({
						panel : this
					}, function(event) {
						event.data.panel.openParticipantView();
					});
				};

				/**
				 *
				 */
				SwimlaneBasicPropertiesPage.prototype.refreshParticipantList = function() {
					this.participantList.empty();
					this.participantList
							.append("<option value='NONE'>(None)</option>");
					var modelname = m_i18nUtils.getProperty("modeler.element.properties.commonProperties.thisModel");
					this.participantList
							.append("<optgroup label=\""+modelname+"\">");

					var psrticipantsSorted = m_utils.convertToSortedArray(this.getModel().participants, "name", true);
					for ( var i in psrticipantsSorted) {
						// Show only participants from this model and not
						// external references.
						if (!psrticipantsSorted[i].externalReference) {
							this.participantList
							.append("<option value='"
									+ psrticipantsSorted[i]
											.getFullId()
									+ "'>"
									+ psrticipantsSorted[i].name
									+ "</option>");
						}
					}
					var othermodel = m_i18nUtils.getProperty("modeler.element.properties.commonProperties.otherModel");
					this.participantList
							.append("</optgroup><optgroup label=\""+othermodel+"\">");

					var modelsSorted = m_utils.convertToSortedArray(m_model.getModels(), "name", true);
					for ( var n in modelsSorted) {
						if (modelsSorted[n] == this.getModel()) {
							continue;
						}

						var participantsSorted = m_utils.convertToSortedArray(modelsSorted[n].participants, "name", true);
						for ( var m in participantsSorted) {
							if (m_modelElementUtils.hasPublicVisibility(participantsSorted[m])
									&& !participantsSorted[m].externalReference
									&& !(m_constants.ADMIN_ROLE_ID === participantsSorted[m].id)) {
								this.participantList
								.append("<option value='"
										+ participantsSorted[m]
												.getFullId()
										+ "'>"
										+ modelsSorted[n].name
										+ "/"
										+ participantsSorted[m].name
										+ "</option>");
							}
						}
					}

					this.participantList.append("</optgroup>");
				};

				/**
				 *
				 */
				SwimlaneBasicPropertiesPage.prototype.setElement = function() {
					this.setModelElement();

					this.refreshParticipantList();
					this.participantViewLink.addClass("imgLinkDisabled");

					if (this.getModelElement().participantFullId != null) {
						this.participantList
								.val(this.getModelElement().participantFullId);
						this.participantViewLink.removeClass("imgLinkDisabled");
					} else {
						this.participantList.val("NONE");
					}
					this.updatePropertiesPanelTitle();
				};

				/**
				 *
				 */
				SwimlaneBasicPropertiesPage.prototype.updatePropertiesPanelTitle = function() {
					this.propertiesPanelTitle.empty();
					if (this.getModelElement().participantFullId != null) {
						this.propertiesPanelTitle
								.append(this.getModelElement().name
										+ " ("
										+ m_model
												.findParticipant(this
														.getModelElement().participantFullId).name
										+ ")");
					} else {
						this.propertiesPanelTitle.append(this.getModelElement().name);
					}
				};

				/**
				 *
				 */
				SwimlaneBasicPropertiesPage.prototype.getModelElement = function() {
					return this.propertiesPanel.element;
				};

				/**
				 *
				 */
				SwimlaneBasicPropertiesPage.prototype.validate = function(input, property) {
					if(input && property){
						this.propertiesPanel.clearErrorMessages();
						input.removeClass("error");

						if("participantFullId" == property){
							var participantId = input.val();
							if (participantId != "NONE") {
								var participant = m_model.findParticipant(participantId);

								if(m_constants.CONDITIONAL_PERFORMER_PARTICIPANT_TYPE == participant.type){
									var eventSymbols = this.getModelElement().diagram.eventSymbols;
									for ( var n in eventSymbols) {
										if(this.getModelElement().oid == eventSymbols[n].parentSymbol.oid){
											if (eventSymbols[n]
													&& m_constants.NONE_EVENT_CLASS == eventSymbols[n].modelElement.eventClass) {
												this.propertiesPanel.errorMessages
												.push(m_i18nUtils
														.getProperty("modeler.swimlane.properties.conditionalParticipant.manualTrigger.error"));
												input.addClass("error");
												input.focus();
												this.propertiesPanel.showErrorMessages();
												return false;
											}
											else if(m_constants.MESSAGE_EVENT_CLASS == eventSymbols[n].modelElement.eventClass
													&& "scan" == eventSymbols[n].modelElement.implementation){
												this.propertiesPanel.errorMessages
												.push(m_i18nUtils
														.getProperty("modeler.swimlane.properties.conditionalParticipant.scanTrigger.error"));
												input.addClass("error");
												input.focus();
												this.propertiesPanel.showErrorMessages();
												return false;
											}
										}
									}
								}
							}
						}
					}
					if (this.validateModelElement()
							&& this.validateCircularModelReference(this.participantList)) {
						return true;
					}

					return false;
				};

				/**
				 *
				 */
				SwimlaneBasicPropertiesPage.prototype.openParticipantView = function() {
					var participant = m_model.findParticipant(this.getModelElement().participantFullId);
					m_modelerUtils.openParticipantView(participant);
				};
			}
		});