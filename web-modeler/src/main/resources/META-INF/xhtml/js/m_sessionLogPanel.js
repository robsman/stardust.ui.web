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
		[ "m_utils", "m_constants", "m_user", "m_session", "m_command",
				"m_commandsController", "m_model", "m_typeDeclaration",
				"m_accessPoint", "m_dataTraversal", "m_dialog",
				"m_communicationController", "m_websocketModel",
				"m_websocketInvite", "mustache" ],
		function(m_utils, m_constants, m_user, m_session, m_command,
				m_commandsController, m_model, m_typeDeclaration,
				m_accessPoint, m_dataTraversal, m_dialog,
				m_communicationController, m_websocketModel, m_websocketInvite,
				mustache) {
			var invite = false;

			return {
				initialize : function() {
					if (window.top.sessionLogPanel == null) {
						invite = m_websocketInvite.init("/invite/"
								+ m_user.getCurrentUser().account);
						m_session.initialize();

						window.top.sessionLogPanel = new SessionLogPanel();

						window.top.sessionLogPanel.initialize();

						m_commandsController
								.registerCommandHandler(window.top.sessionLogPanel);
					}
				}
			};

			/**
			 * 
			 * 
			 */
			function SessionLogPanel() {
				this.startTimeSpan = jQuery("#sessionLogPanel #startTimeSpan");
				this.chatTextArea = jQuery("#sessionLogPanel #chatTextArea");
				this.sessionLogTable = jQuery("#sessionLogPanel #sessionLogTable");

				var url = m_communicationController.getEndpointUrl()
						+ "/users/getOfflineInvites";
				var command = null;

				// Query for getting a offline Invite. Works if websocket
				// instantiation is completed
				// can be put into commandscontroller later on. Just for
				// simplicity located here.

				// TODO Logic in object creation
				if (invite) {
					m_communicationController.getHead({
						"url" : url
					}, new function() {
						return {
							"success" : function(command) {
								m_utils.debug("recived command ok");
							},
							"error" : function(command) {
								m_utils
										.debug("recived command error"
												+ command);
							}
						};
					});
				}

				// Query for getting an offline Invite. Works if websocket
				// instantiation is completed
				// can be put into commandscontroller later on. Just for
				// simplicity located here.
				if (invite) {
					m_communicationController.getHead({
						"url" : url
					}, new function() {
						return {
							"success" : function(command) {
								m_utils.debug("recived command ok");
							},
							"error" : function(command) {
								m_utils
										.debug("recived command error"
												+ command);
							}
						};
					});
				}

				this.chatTextArea
						.keyup(
								{
									panel : this
								},
								function(event) {
									var code = (event.keyCode ? event.keyCode
											: event.which);

									if (code == 13
											&& m_session.getInstance().joined) {
										m_websocketModel
												.send(jQuery
														.stringifyJSON(m_command
																.createSubmitChatMessageCommand(event.data.panel.chatTextArea
																		.val())));
										event.data.panel.chatTextArea.val("");
										event.data.panel.chatTextArea.focus();
									} else if ((!m_session.getInstance().joined)
											&& code == 13 /* ENTER */) {
										m_commandsController
												.broadcastCommand(m_command
														.createSubmitChatMessageCommand("No other user has joined your modeling session to consume chat messages."));
										event.data.panel.chatTextArea.val("");
										event.data.panel.chatTextArea.focus();
									}
								});

				/**
				 * 
				 */
				SessionLogPanel.prototype.initialize = function() {
					this.populateParticipantTable();
					this.startTimeSpan.empty();
					this.startTimeSpan.append(" (started "
							+ m_utils.formatDate(new Date(), "H:i:s") + ")"); // I18N
					this.sessionLogTable.empty();
				};

				/**
				 * 
				 */
				SessionLogPanel.prototype.processCommand = function(command) {
					var user = m_user.getCurrentUser();
					var row = jQuery("<tr class=\"sessionLogTableRow\"><td valign=\"top\"><table cellspacing='0' cellpadding='0' width='100%'>");
					var imageUrl = "../images/" + "anonymous-user.gif"; // m_session.current().participants[command.account].imageUrl;

					var view = {
						image : imageUrl,
						account : command.account,
						date : m_utils.formatDate(new Date(command.timestamp),
								"H:i:s")
					};
					var commandUser = m_session.getInstance()
							.getUserByAccount(command.account);

					view.commandUserName = command.account;

					if (commandUser != null) {
						view.commandUserName = commandUser.firstName
								+ " "
								+ commandUser.lastName;
					}

					var template = "<tr><td><table cellspacing='0' cellpadding='0' width='100%'><tr><td valign=\"top\"><span id=\"userTag\">{{commandUserName}}</span></td>"
						+ "<td align=\"right\"><span id=\"dateTag\">{{date}}</span></td></tr></table></td></tr>";

					template += "<tr><td valign=\"top\" align=\"left\">";

					if (command.type == m_constants.REQUEST_JOIN_COMMAND) {
						var url = null;
						var date = new Date(command.timestamp);

						if (command.account != user.account) {

							var join = jQuery("<img src=\"../images/icons/accept.png\">");

							join.bind("click", function(event) {
								var old = {
									"sessionOwner" : command.account
								};
								var prospect = command.oldObject;
								m_websocketInvite.send(jQuery
										.stringifyJSON(m_command
												.createAcceptInvite(old,
														prospect)));
								join.unbind(event);
								decline.unbind(event);
							});

							var decline = jQuery("<img src=\"../images/icons/decline.png\">");
							decline.bind("click", function(event) {
								var prospect = command.oldObject;
								var old = {
									"sessionOwner" : command.account
								};
								m_websocketInvite.send(jQuery
										.stringifyJSON(m_command
												.createDeclineInvite(old,
														prospect)));
								join.unbind(event);
								decline.unbind(event);
							});

							template += "{{commandUserName}} has asked you to join a modeling session.";

							var rendered = mustache.render(template, view);

							row.append(rendered);
							row.append("Accept ");
							row.append(join);
							row.append(" or decline ");
							row.append(decline);

						} else {

							template += "You invited {{firstname}} {{lastname}} to join your modeling session. "
									+ "Waiting for the invited user to accept or decline your offer. If the user is not online this could take a while.";

							view.firstname = command.oldObject.firstName;
							view.lastname = command.oldObject.lastName;

							var rendered = mustache.render(template, view);

							row.append(rendered);
						}

					} else if (command.type == m_constants.ACCEPT_INVITE_COMMAND) {
						if (user.account == command.oldObject.sessionOwner) {
							template += "User {{firstname}} {{lastname}} accepted your invitation.<br>Confirm join?";

							view.firstname = command.newObject.firstName;
							view.lastname = command.newObject.lastName;

							var prospect = command.newObject;
							var iconJoin = jQuery("<img src=\"../images/icons/accept.png\"></a>");
							iconJoin
									.bind(
											"click",
											function(event) {
												var old = command.account;
												var prospect = command.newObject;
												m_websocketInvite
														.send(jQuery
																.stringifyJSON(m_command
																		.createConfirmJoinCommand(prospect)));

											});

							var rendered = mustache.render(template, view);

							row.append(rendered);
							row.append(iconJoin);
						} else {
							template += " accepted the invitation. Waiting for confirmation.";

							var rendered = mustache.render(template, view);

							row.append(rendered);
						}
					} else if (command.type == m_constants.DECLINE_INVITE_COMMAND) {
						template += " declined the invitation.";

						var rendered = mustache.render(template, view);

						row.append(rendered);

					} else if (command.type == m_constants.CONFIRM_JOIN_COMMAND) {
						if (command.modelSession != null
								&& (user.account == command.account || user.account == command.oldObject.account)
								&& !m_session.getInstance().joined) {
							url = "/model/" + command.modelSession;

							m_websocketModel.init(url);
							m_session.getInstance().joined = true;
						}

						template += "The joining of {{firstname}} {{lastname}} was confirmed.";

						view.firstname = command.oldObject.firstName;
						view.lastname = command.oldObject.lastName;

						var rendered = mustache.render(template, view);

						row.append(rendered);

					} else if (command.type == m_constants.SUBMIT_CHAT_MESSAGE_COMMAND) {

						template += "{{message}}";

						view.message = command.newObject;

						var rendered = mustache.render(template, view);

						row.append(rendered);

					} else if (command.type == m_constants.UPDATE_INVITED_USERS_COMMAND) {
						this.populateParticipantTable();
					} else if (command.type == "ERROR") {
						template += "<span class='errorMessage'>{{message}}</span>";

						view.message = command.oldObject.errormessage;

						var rendered = mustache.render(template, view);

						row.append(rendered);
					} else {
						template += "{{message}}";

						if (command.commandId == "modelElement.update") {
							view.message = "Model element updated.";
						} else if (command.commandId == "activitySymbol.create") {
							view.message = "Activity created.";
						} else if (command.commandId == "eventSymbol.create") {
							view.message = "Event created.";
						} else if (command.commandId == "gatewaySymbol.create") {
							view.message = "Gateway created.";
						} else if (command.commandId == "swimlaneSymbol.create") {
							view.message = "Swimlane created.";
						} else if (command.commandId == "process.create") {
							view.message = "Process Definition created.";
						} else if (command.commandId == "activitySymbol.delete") {
							view.message = "Activity deleted.";
						} else if (command.commandId == "eventSymbol.delete") {
							view.message = "Event deleted.";
						} else if (command.commandId == "gatewaySymbol.delete") {
							view.message = "Gateway deleted.";
						} else if (command.commandId == "swimlaneSymbol.delete") {
							view.message = "Swimlane deleted.";
						} else if (command.commandId == "process.delete") {
							view.message = "Process Definition deleted.";
						} else {
							view.message = "Other modification performed.";
						}

						var rendered = mustache.render(template, view);

						row.append(rendered);
					}

					row.append("</td></tr>");
					row.append("</table></td></tr>");

					this.sessionLogTable.append(row);
					jQuery("#sessionLogPanel #sessionLogTablePanel")
							.prop(
									{
										scrollTop : jQuery(
												"#sessionLogPanel #sessionLogTablePanel")
												.prop("scrollHeight")
												- jQuery(
														"#sessionLogPanel #sessionLogTablePanel")
														.height()
									});
				};

				/**
				 * 
				 */
				SessionLogPanel.prototype.populateParticipantTable = function() {
					var participantsTable = jQuery("#sessionParticipantsPanel #sessionParticipantsTable");

					participantsTable.empty();
					participantsTable.append(this.getParticipantRow(m_session
							.initialize().owner, "Owner"));

					for ( var participant in m_session.initialize().participants) {
						participantsTable
								.append(this
										.getParticipantRow(
												m_session.initialize().participants[participant],
												"Participant"));
					}

					for ( var prospect in m_session.initialize().prospects) {
						participantsTable.append(this.getParticipantRow(
								m_session.initialize().prospects[prospect],
								"Prospect"));
					}
				};

				/**
				 * 
				 */
				SessionLogPanel.prototype.getParticipantRow = function(
						participant, role) {
					var row = jQuery("<tr></tr>");

					if (participant.color == null) {
						participant.color = "dddddd";
					}

					participant.imageUrl = "../images/" + "anonymous-user.gif"; // m_session.current().participants[command.account].imageUrl;

					row
							.append("<td><table><tr><td><img src='"
									+ participant.imageUrl
									+ "'></td><td><div style='background-color: #"
									+ participant.color
									+ "; width: 10px; height: 50px;'/></td></tr></table></td>");

					var cell = jQuery("<td valign='top'></td>");
					var content = "<span class='nameSpan'>"
							+ participant.firstName + " "
							+ participant.lastName
							+ "</span><br><span class='accountSpan'>"
							+ participant.account;

					if (m_session.getInstance().loggedInUser.account == participant.account) {
						content += " <i>(You)</i></span>"
					}

					cell.append(content);
					row.append(cell);

					row.append("<td valign=\"top\"><span class=\"roleTag\">"
							+ role + "</span></td>");

					cell = jQuery("<td valign='top'></td>");

					var image;

					if (m_session.getInstance().loggedInUser.account == m_session
							.getInstance().owner.account) {
						if (m_session.getInstance().loggedInUser.account == participant.account) {
							image = jQuery("<a><img src='../images/icons/decline.png' title='Terminate Session'></a>"); // I18N

							image.click({
								panel : this
							}, function(event) {
								event.data.panel.terminateSession();
							});
						} else {
							image = jQuery("<a><img src='../images/icons/decline.png' title='Dismiss Participant'></a>"); // I18N

							image.click({
								panel : this,
								participant : participant
							}, function(event) {
								event.data.panel
										.dismissParticipant(participant);
							});
						}

						cell.append(image);
					} else if (m_session.getInstance().loggedInUser.account == participant.account) {
						image = jQuery("<a><img src='../images/icons/decline.png' title='Leave Session'></a>"); // I18N

						image.click({
							panel : this
						}, function(event) {
							event.data.panel.leaveSession();
						});
						cell.append(image);
					}

					row.append(cell);

					return row;
				};

				/**
				 * 
				 */
				SessionLogPanel.prototype.leaveSession = function() {
					m_utils.debug("===> Leave session");
					m_session.renew();
					this.initialize();
				};

				/**
				 * 
				 */
				SessionLogPanel.prototype.terminateSession = function() {
					m_utils.debug("===> Terminate session");
					m_session.renew();
					this.initialize();
				};

				/**
				 * 
				 */
				SessionLogPanel.prototype.dismissParticipant = function(
						participant) {
					m_utils.debug("===> Dismiss participant "
							+ participant.account);
					m_session.renew();
					this.initialize();
				};
			}
		});