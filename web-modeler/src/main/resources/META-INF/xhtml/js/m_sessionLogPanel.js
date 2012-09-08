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

				this.chatTextArea = jQuery("#sessionLogPanel #chatTextArea");
				var url = m_communicationController.getEndpointUrl()
						+ "/users/getOfflineInvites";
				var command = null;

				// Query for getting a offline Invite. Works if websocket
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
				var url = m_communicationController.getEndpointUrl()
						+ "/users/getOfflineInvites";
				var command = null;

				// Query for getting a offline Invite. Works if websocket
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

				// Reacts now to enter button when typing in the text area :3

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
																.createSubmitChatMessageCommand(jQuery(
																		"#sessionLogPanel #chatTextArea")
																		.val())));
										jQuery("#sessionLogPanel #chatTextArea")
												.val("");
										jQuery("#sessionLogPanel #chatTextArea")
												.focus();
									} else if ((!m_session.getInstance().joined)
											&& code == 13) {
										m_commandsController
												.broadcastCommand(m_command
														.createSubmitChatMessageCommand("You are currently not in a chat/model session, invite people to create a modelsession. "));
										jQuery("#sessionLogPanel #chatTextArea")
												.val("");
										jQuery("#sessionLogPanel #chatTextArea")
												.focus();
									}
								});

				/**
				 * 
				 */
				SessionLogPanel.prototype.processCommand = function(command) {
					var user = m_user.getCurrentUser();
					var row = jQuery("<tr class=\"sessionLogTableRow\"><td valign=\"top\"><table><tr><td>");
					var imageUrl = null;

					if (command.type == m_constants.REQUEST_JOIN_COMMAND) {
						imageUrl = "../images/" + "sheldor-photo.gif";
					} else if (command.type == m_constants.CONFIRM_JOIN_COMMAND) {
						imageUrl = "../images/" + "sheldor-photo.gif"; // command.oldObject.imageUrl;
					} else {
						imageUrl = "../images/" + "sheldor-photo.gif"; // m_session.current().participants[command.account].imageUrl;
					}

					var view = null;
					var template = "<img src=\"{{{image}}}\"/> </td><td valign=\"top\"><span id=\"userTag\"> {{firstname}} {{lastname}} </span>"
							+ "<td><span id=\"dateTag\"> {{date}} </span></td></tr></table></td></tr>"
							+ "<table><tr><td valign=\"top\" align=\"left\">";

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

							view = {
								image : imageUrl,
								account : command.account,
								date : new Date(command.timestamp),
								firstname : command.oldObject.firstName,
								lastname : command.oldObject.lastName
							};

							template += " has asked you {{firstname}} {{lastname}} to join a modeling session: ";

							var rendered = mustache.render(template, view);

							row.append(rendered);
							row.append("accept ");
							row.append(join);
							row.append(" or decline ");
							row.append(decline);

						} else {

							template += "You invited {{firstname}} {{lastname}} to join your modeling session. "
									+ "Waiting for the invited user to accept or decline your offer. If the user is not online this could take a while. ";

							view = {
								image : imageUrl,
								account : command.account,
								date : new Date(command.timestamp),
								firstname : command.oldObject.firstName,
								lastname : command.oldObject.lastName
							};

							var rendered = mustache.render(template, view);
							row.append(rendered);

						}

					} else if (command.type == m_constants.ACCEPT_INVITE_COMMAND) {
						if (user.account == command.oldObject.sessionOwner) {
							template += "User {{firstname}} {{lastname}} accepted your invitation. Confirm join? </td></tr></table></td></tr>";

							view = {
								image : imageUrl,
								account : command.account,
								date : new Date(command.timestamp),
								firstname : command.newObject.firstName,
								lastname : command.newObject.lastName
							};

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

							template += "accepted the invitation. Waiting for confirmation. </td></tr></table></td></tr>";

							view = {
								image : imageUrl,
								account : command.account,
								date : new Date(command.timestamp)

							};

							var rendered = mustache.render(template, view);
							row.append(rendered);
						}
					} else if (command.type == m_constants.DECLINE_INVITE_COMMAND) {

						template += "declined the invitation. </td></tr></table></td></tr>";

						view = {
							image : imageUrl,
							account : command.account,
							date : new Date(command.timestamp)

						};

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

						template += "The joining of {{firstname}} {{lastname}} was confirmed. </td></tr></table></td></tr>";

						view = {
							image : imageUrl,
							account : command.account,
							date : new Date(command.timestamp),
							firstname : command.oldObject.firstName,
							lastname : command.oldObject.lastName

						};

						var rendered = mustache.render(template, view);
						row.append(rendered);

					} else if (command.type == m_constants.SUBMIT_CHAT_MESSAGE_COMMAND) {

						template += "{{message}} </td></tr></table></td></tr>";

						view = {
							image : imageUrl,
							account : command.account,
							date : new Date(command.timestamp),
							message : command.newObject

						};

						var rendered = mustache.render(template, view);
						row.append(rendered);

					} else if (command.type == m_constants.UPDATE_INVITED_USERS_COMMAND) {
						console.log("SessionLogPanel starts working on "
								+ m_constants.UPDATE_INVITED_USERS_COMMAND);
						var participants = m_session.initialize().participants;
						var prospects = m_session.initialize().prospects;
						console.log(participants);

						jQuery("#sessionUserPanel #sessionOwner").empty();
						var owner = m_session.initialize().owner;
						var ownerList = jQuery("<li><span>" + owner.account
								+ " (owner) </span></li>");
						var ownerColor = "#" + owner.color;

						var participantRow = jQuery("#sessionUserPanel #sessionJoined");
						ownerList.css("color", ownerColor);
						ownerList.find("span").css("color", "#000000");

						participantRow.empty();
						participantRow.append(ownerList);
						jQuery.each(participants, function(item, value) {
							var tableRow = jQuery("<li><span>" + value.account
									+ "</span></li>")
							var hexColor = "#" + value.color;
							tableRow.css("color", hexColor);
							tableRow.find("span").css("color", "#000000");
							participantRow.append(tableRow);
						});

						jQuery.each(prospects, function(item, value) {
							var tableRow = jQuery("<li><span>" + value.account
									+ " (Prospect)</span></li>")
							participantRow.append(tableRow);

						});

						console.log("SessionLogPanel stops working on "
								+ m_constants.UPDATE_INVITED_USERS_COMMAND);

					} else if (command.type == "ERROR") {

						template += "<strong> {{message}} </strong> </td></tr></table></td></tr>";
						view = {
							image : imageUrl,
							account : command.account,
							date : new Date(command.timestamp),
							message : command.oldObject.errormessage

						};

						var rendered = mustache.render(template, view);
						row.append(rendered);

					} else {

						template += "{{message}}</td></tr></table></td></tr>";
						view = {
							image : imageUrl,
							account : command.account,
							date : new Date(command.timestamp),
							message : function() {
								var note = null;
								if (command.commandId == "modelElement.update") {
									note = "Model element updated.";
								} else if (command.commandId == "activitySymbol.create") {
									note = "Activity created.";
								} else if (command.commandId == "eventSymbol.create") {
									note = "Event created.";
								} else if (command.commandId == "gatewaySymbol.create") {
									note = "Gateway created.";
								} else if (command.commandId == "swimlaneSymbol.create") {
									note = "Swimlane created.";
								} else if (command.commandId == "process.create") {
									note = "Process Definition created.";
								} else if (command.commandId == "activitySymbol.delete") {
									note = "Activity deleted.";
								} else if (command.commandId == "eventSymbol.delete") {
									note = "Event deleted.";
								} else if (command.commandId == "gatewaySymbol.delete") {
									note = "Gateway deleted.";
								} else if (command.commandId == "swimlaneSymbol.delete") {
									note = "Swimlane deleted.";
								} else if (command.commandId == "process.delete") {
									note = "Process Definition deleted.";
								} else {
									note = "Other modification performed.";
								}
								return note;
							}
						};

						var rendered = mustache.render(template, view);
						row.append(rendered);
					}

					jQuery("#sessionLogPanel #sessionLogTable").append(row);
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
			}
			;

		});