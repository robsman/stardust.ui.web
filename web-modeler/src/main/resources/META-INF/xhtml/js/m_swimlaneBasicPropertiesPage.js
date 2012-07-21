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
				"m_propertiesPage", "m_participant" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_propertiesPage, m_participant) {
			return {
				create : function(propertiesPanel) {
					return new SwimlaneBasicPropertiesPage(propertiesPanel);
				}
			};

			/**
			 * 
			 */
			function SwimlaneBasicPropertiesPage(newPropertiesPanel, newId,
					newTitle) {

				// Inheritance

				var propertiesPage = m_propertiesPage.createPropertiesPage(
						newPropertiesPanel, "basicPropertiesPage", "Basic");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(SwimlaneBasicPropertiesPage.prototype,
						propertiesPage);

				// Field initialization

				this.nameInput = this.mapInputId("nameInput");
				this.title = this.mapInputId("swimlanePropertiesPanelTitle");
				this.createNewParticipantLink = this
						.mapInputId("createNewParticipantLink");
				this.newParticipantName = this.mapInputId("newParticipantName");
				this.descriptionInput = this.mapInputId("descriptionInput");
				this.participantList = this.mapInputId("participantList");

				// Initialize callbacks

				this.nameInput
						.change(
								{
									"page" : this
								},
								function(event) {
									var page = event.data.page;

									if (!page.validate()) {
										return;
									}

									if (page.propertiesPanel.element.modelElement.name != page.nameInput
											.val()) {
										page.propertiesPanel.element.modelElement.name = page.nameInput
												.val();
										page.submitChanges({
											modelElement : {
												name : page.nameInput.val()
											}
										});
									}
								});
				this.descriptionInput
						.change(
								{
									"page" : this
								},
								function(event) {
									var page = event.data.page;

									if (!page.validate()) {
										return;
									}

									if (page.propertiesPanel.element.modelElement.description != page.descriptionInput
											.val()) {
										page.propertiesPanel.element.modelElement.description = page.descriptionInput
												.val();
										page
												.submitChanges({
													modelElement : {
														description : page.descriptionInput
																.val()
													}
												});
									}
								});
				this.createNewParticipantLink
						.click(
								{
									"callbackScope" : this
								},
								function(event) {
									m_commandsController
											.submitImmediately(
													m_command
															.createCommand(
																	"/models/"
																			+ event.data.callbackScope.propertiesPanel.element.diagram.model.id
																			+ "/roles",
																	{
																		"id" : event.data.callbackScope.newParticipantName
																				.val(),
																		"name" : event.data.callbackScope.newParticipantName
																				.val()
																	}),
													{
														"callbackScope" : event.data.callbackScope,
														"method" : "setParticipantId"
													}, {});
								});

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
					this.refreshParticipantList();

					this.nameInput.removeClass("error");

					this.nameInput.val(this.propertiesPanel.element.name);
					this.descriptionInput
							.val(this.propertiesPanel.element.description);

					this.title
							.html(this.propertiesPanel.element.participantName);

					if (this.propertiesPanel.element.participantFullId != null) {
						this.participantList
								.val(this.propertiesPanel.element.participantFullId);
					} else {
						this.participantList.val("NONE");
					}
				};

				/**
				 * 
				 */
				SwimlaneBasicPropertiesPage.prototype.validate = function() {
					this.propertiesPanel.clearErrorMessages();
					this.nameInput.removeClass("error");

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
						this.propertiesPanel.errorMessages
								.push("Swimlane name must not be empty.");
						this.nameInput.addClass("error");
					
						this.propertiesPanel.showErrorMessages();

						return false;
					}
					
					return true;
				};

				/**
				 * 
				 */
				SwimlaneBasicPropertiesPage.prototype.setParticipantId = function(
						json) {
					var participant = m_participant.createParticipantFromJson(
							this.propertiesPanel.element.diagram.model, json);

					m_utils.debug("===> Set participant:");
					m_utils.debug(participant);

					this.refreshParticipantList();
					this.participantList.val(participant.getFullId());
				};

				/**
				 * 
				 */
				SwimlaneBasicPropertiesPage.prototype.submitChanges = function(
						changes) {
					m_commandsController.submitCommand(m_command
							.createUpdateModelElementCommand(
									this.propertiesPanel.element.oid, changes,
									this.propertiesPanel.element));
				};

			}
		});