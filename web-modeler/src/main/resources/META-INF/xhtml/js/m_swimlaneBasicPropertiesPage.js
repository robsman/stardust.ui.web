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
		[ "m_utils", "m_constants", "m_commandsController", "m_command",
				"m_model", "m_basicPropertiesPage", "m_participant" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_model, m_basicPropertiesPage, m_participant) {
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

					this.title = this
							.mapInputId("swimlanePropertiesPanelTitle");
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

					this.participantList
							.append("<optgroup label=\"This Model\">");

					for ( var i in this.getModel().participants) {
						this.participantList
								.append("<option value='"
										+ this.getModel().participants[i]
												.getFullId()
										+ "'>"
										+ this.getModel().participants[i].name
										+ "</option>");
					}

					this.participantList
							.append("</optgroup><optgroup label=\"Other Models\">");

					for ( var n in m_model.getModels()) {
						if (m_model.getModels()[n] == this.getModel()) {
							continue;
						}

						for ( var m in m_model.getModels()[n].participants) {
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

					this.participantList.append("</optgroup>");
				};

				/**
				 *
				 */
				SwimlaneBasicPropertiesPage.prototype.setElement = function() {
					this.setModelElement();

					this.refreshParticipantList();

					this.title.empty();

					if (this.getModelElement().participantFullId != null) {
						this.participantList
								.val(this.getModelElement().participantFullId);
						this.title
								.append(this.getModelElement().name
										+ "("
										+ m_model
												.findParticipant(this
														.getModelElement().participantFullId).name
										+ ")");
					} else {
						this.participantList.val("NONE");
						this.title.append(this.getModelElement().name);
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
				SwimlaneBasicPropertiesPage.prototype.validate = function() {
					if (this.validateModelElement()) {
						return true;
					}

					return false;
				};
			}
		});