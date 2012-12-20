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
				"bpm-modeler/js/m_modelElementUtils" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_model, m_basicPropertiesPage, m_participant, m_i18nUtils, m_modelElementUtils) {
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

					this.propertiesPanelTitle = jQuery("#swimlanePropertiesPanelTitle");
					this.newParticipantName = this
							.mapInputId("newParticipantName");
					this.participantList = this.mapInputId("participantList");

					this.registerInputForModelElementChangeSubmission(
							this.participantList, "participantFullId");
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

					for ( var i in this.getModel().participants) {
						// Show only participants from this model and not
						// external references.
						if (!this.getModel().participants[i].externalReference) {
							this.participantList
							.append("<option value='"
									+ this.getModel().participants[i]
											.getFullId()
									+ "'>"
									+ this.getModel().participants[i].name
									+ "</option>");
						}
					}
					var othermodel = m_i18nUtils.getProperty("modeler.element.properties.commonProperties.otherModel");
					this.participantList
							.append("</optgroup><optgroup label=\""+othermodel+"\">");

					for ( var n in m_model.getModels()) {
						if (m_model.getModels()[n] == this.getModel()) {
							continue;
						}

						for ( var m in m_model.getModels()[n].participants) {
							if (m_modelElementUtils.hasPublicVisibility(m_model.getModels()[n].participants[m])) {
								this.participantList
								.append("<option value='"
										+ m_model.getModels()[n].participants[m]
												.getFullId()
										+ "'>"
										+ m_model.getModels()[n].name
										+ "/"
										+ m_model.getModels()[n].participants[m].name
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

					if (this.getModelElement().participantFullId != null) {
						this.participantList
								.val(this.getModelElement().participantFullId);
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
				}

				/**
				 *
				 */
				SwimlaneBasicPropertiesPage.prototype.getModelElement = function() {
					return this.propertiesPanel.element;
				};

				/**
				 *
				 */
				SwimlaneBasicPropertiesPage.prototype.validate = function() {
					if (this.validateModelElement()) {
						return true;
					}

					return false;
				};
			}
		});