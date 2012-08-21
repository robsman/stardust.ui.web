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
				"m_communicationController", "m_websocketModel", "m_websocketInvite" ],
		function(m_utils, m_constants, m_user, m_session, m_command,
				m_commandsController, m_model, m_typeDeclaration,
				m_accessPoint, m_dataTraversal, m_dialog,
				m_communicationController, m_websocketModel, m_websocketInvite) {
			var invite = false;

			return {
				initialize : function() {
					if (window.top.sessionLogPanel == null) {
						invite = m_websocketInvite.init("/invite/" + m_user.getCurrentUser().account);
						window.top.sessionLogPanel = new SessionLogPanel();
						m_session.initialize();
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

				this.sessionLogTable = jQuery("#sessionLogPanel #sessionLogTable");
				this.chatTextArea = jQuery("#sessionLogPanel #chatTextArea");
				var url = m_communicationController.getEndpointUrl()+"/users/getOfflineInvites";
				var command = null;
				// Query for getting a offline Invite. Works if websocket instantiation is completed
				// can be put into commandscontroller later on. Just for simplicity located here.
				if(invite){
					m_communicationController.getHead({"url" : url}, new function(){
					return{
						"success" : function(command){
							m_utils.debug("recived command ok");
						},
						"error" : function(command){
							m_utils.debug("recived command error" +command);
							}
						};
					} );
				}

				// Reacts now to enter button when typing in the text area :3
				this.chatTextArea.keyup({ panel : this},
								function(event) {
									var code = (event.keyCode ? event.keyCode
											: event.which);
									if (code == 13 && joined) {
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
									} else if ((!joined)
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
					var row = jQuery("<tr class=\"sessionLogTableRow\">");
					var imageUrl = null;

					row.append("<td valign=\"top\"><table><tr><td>");

					if (command.type == m_constants.REQUEST_JOIN_COMMAND) {
						imageUrl = "../images/"+ "sheldor-photo.gif";

					} else if (command.type == m_constants.CONFIRM_JOIN_COMMAND) {
						imageUrl = "../images/" + "sheldor-photo.gif"; // command.oldObject.imageUrl;
					} else {
						imageUrl = "../images/" + "sheldor-photo.gif"; // m_session.current().participants[command.account].imageUrl;
					}

					if (command.type != m_constants.REQUEST_JOIN_COMMAND && command.type != "UPDATE_INVITED_USERS_COMMAND") {
						row.append("<img src=\""+ imageUrl+ "\"/> </td><td valign=\"top\"><span id=\"userTag\">");
						row.append(command.account);
						row.append("</span><td><span id=\"dateTag\">");

						var date = new Date(command.timestamp);

						row.append(date);
						row.append("</span></td></tr></table></td></tr><table><tr><td valign=\"top\" align=\"left\">");

					}


					if (command.type == m_constants.REQUEST_JOIN_COMMAND) {

						var url = null;
						var date = new Date(command.timestamp);

						if(command.account != user.account){
							row.append("<img src=\""+ imageUrl+ "\"/> </td><td valign=\"top\"><span id=\"userTag\">");
							row.append(command.account);
							row.append("</span><td><span id=\"dateTag\">");
							row.append(date);
							row.append("</span></td></tr></table></td></tr><table><tr><td valign=\"top\" align=\"left\">");
							row.append("has asked you ");
							row.append(command.oldObject.firstName);
							row.append(" ");
							row.append(command.oldObject.lastName);
							row.append(" to join a modeling session: accept. <a id=\"acceptLink\">");
							var iconJoin = jQuery("<img src=\"../images/icons/accept.png\">");

							iconJoin.bind("click", function(event) {
								var old = {"sessionOwner" : command.account};
								var prospect = command.oldObject;
								console.log(old);
								console.log(prospect);
								m_websocketInvite.send(jQuery.stringifyJSON(m_command.createAcceptInvite(old, prospect)));
							});
							row.append(iconJoin).append("</a>.");

							row.append("decline. <a id=\"declineLink\">");
							var decline = jQuery("<img src=\"../images/icons/decline.png\">");
							decline.bind("click", function(event) {
								var prospect = command.oldObject;
								var old = {"sessionOwner" : command.account};
								m_websocketInvite.send(jQuery.stringifyJSON(m_command.createDeclineInvite(old, prospect)));
							});
							row.append(decline).append("</a>.");
						}else{
							row.append("<img src=\""+ imageUrl+ "\"/> </td><td valign=\"top\"><span id=\"userTag\">");
							row.append(command.account);
							row.append("</span><td><span id=\"dateTag\">");

							var date = new Date(command.timestamp);

							row.append(date);
							row.append("</span></td></tr></table></td></tr><table><tr><td valign=\"top\" align=\"left\">");
							row.append("You invited following people ");
							row.append(command.oldObject.firstName);
							row.append("    ");
							row.append(command.oldObject.lastName);
							row.append(" to join a modeling session. Wainting for the invited user to accept or decline your offer. If the user is not online this could take a while.");

						}


					} else if(command.type == m_constants.ACCEPT_INVITE_COMMAND){
						if(user.account == command.oldObject.sessionOwner){
							row.append("User "+command.newObject.account);
							row.append(" accepted your invitation. Confirm join? ");
							var prospect = command.newObject;
							var iconJoin = jQuery("<img src=\"../images/icons/accept.png\"></a>");
							iconJoin.bind("click", function(event) {
								var old = command.account;
								var prospect = command.newObject;
								m_websocketInvite.send(jQuery.stringifyJSON(m_command.createConfirmJoinCommand(prospect)));

							});
							row.append(iconJoin);
							//m_inviteWebsocket.send(jQuery.stringifyJSON(m_command.createConfirmJoinCommand(prospect)));

						}else{
							row.append(" accepted the invitation. Waiting for confirmation. ");

						}
					}else if(command.type == m_constants.DECLINE_INVITE_COMMAND){
						row.append(" declined the invitation.");

					}else if (command.type == m_constants.CONFIRM_JOIN_COMMAND) {

							if (command.modelSession != null
									&& (user.account == command.account || user.account == command.oldObject.account) && !m_session.joined) {
								url = "/model/" +  command.modelSession;
								m_websocketModel.init(url);
								m_session.joined = true;

							}

						row.append("The joining of User ");
						row.append(command.oldObject.firstName + " " +command.oldObject.lastName);
						row.append(" was confirmed.");
					} else if (command.type == m_constants.SUBMIT_CHAT_MESSAGE_COMMAND) {
						row.append(command.newObject);
					} else if (command.type == m_constants.UPDATE_INVITED_USERS_COMMAND) {
						console.log(m_session.joined);
						console.log(m_session.owner);
						console.log(window.top.modelingSession.collaborators);

					} else {
						if (command.commandId == "modelElement.update") {
							row.append("Model element updated.");
						} else if (command.commandId == "activitySymbol.create") {
							row.append("Activity created.");
						} else if (command.commandId == "eventSymbol.create") {
							row.append("Event created.");
						} else if (command.commandId == "gatewaySymbol.create") {
							row.append("Gateway created.");
						} else if (command.commandId == "swimlaneSymbol.create") {
							row.append("Swimlane created.");
						} else if (command.commandId == "process.create") {
							row.append("Process Definition created.");
						} else if (command.commandId == "activitySymbol.delete") {
							row.append("Activity deleted.");
						} else if (command.commandId == "eventSymbol.delete") {
							row.append("Event deleted.");
						} else if (command.commandId == "gatewaySymbol.delete") {
							row.append("Gateway deleted.");
						} else if (command.commandId == "swimlaneSymbol.delete") {
							row.append("Swimlane deleted.");
						} else if (command.commandId == "process.delete") {
							row.append("Process Definition deleted.");

						} else {
							row.append("Other modification performed.");
						}
					}

					row.append("</td></tr></table></td></tr>");

					jQuery("#sessionLogPanel #sessionLogTable").append(row);
					jQuery("#sessionLogPanel #sessionLogTablePanel").prop(
									{
										scrollTop : jQuery("#sessionLogPanel #sessionLogTablePanel").prop("scrollHeight")
												- jQuery("#sessionLogPanel #sessionLogTablePanel").height()
									});

				};
			}
			;
		});