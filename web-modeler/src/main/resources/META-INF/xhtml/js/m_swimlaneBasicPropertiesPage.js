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
				SwimlaneBasicPropertiesPage.prototype.getModelElement = function() {
					return this.propertiesPanel.element;
				};

				/**
				 * 
				 */
				SwimlaneBasicPropertiesPage.prototype.assembleChangedObjectFromProperty = function(
						property, value) {
					var element = {};

					element[property] = value;

					return element;
				};

				/**
				 * 
				 */
				SwimlaneBasicPropertiesPage.prototype.assembleChangedObjectFromAttribute = function(
						attribute, value) {
					var element = {
						attributes : {}
					};

					element.attributes[attribute] = value;

					return element;
				};

				/**
				 * 
				 */
				SwimlaneBasicPropertiesPage.prototype.refreshParticipantList = function() {
					this.participantList.empty();
					this.participantList
							.append("<option value='NONE'>None</option>");

					for ( var n in this.propertiesPanel.models) {
						for ( var m in this.propertiesPanel.models[n].participants) {
							this.participantList
									.append("<option value='"
											+ this.propertiesPanel.models[n].participants[m]
													.getFullId()
											+ "'>"
											+ this.propertiesPanel.models[n].name
											+ "/"
											+ this.propertiesPanel.models[n].participants[m].name
											+ "</option>");
						}
					}
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
				SwimlaneBasicPropertiesPage.prototype.validate = function() {
					if (this.validateModelElement()) {
						return true;
					}

					return true;
				};
			}
		});