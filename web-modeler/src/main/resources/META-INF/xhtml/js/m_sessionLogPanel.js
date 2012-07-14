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
				"m_accessPoint", "m_dataTraversal", "m_dialog" ],
		function(m_utils, m_constants, m_user, m_session, m_command,
				m_commandsController, m_model, m_typeDeclaration,
				m_accessPoint, m_dataTraversal, m_dialog) {
			return {
				initialize : function() {
					if (window.top.sessionLogPanel == null) {
						window.top.sessionLogPanel = new SessionLogPanel();

						m_commandsController
								.registerCommandHandler(window.top.sessionLogPanel);
					}
				}
			};

			/**
			 * 
			 */
			function SessionLogPanel() {
				this.testJoinLink = jQuery("#sessionLogPanel #testJoinLink");
				this.sessionLogTable = jQuery("#sessionLogPanel #sessionLogTable");
				this.chatTextArea = jQuery("#sessionLogPanel #chatTextArea");

				this.testJoinLink.click({
					panel : this
				}, function(event) {
					var prospect = m_user.createUser("browndynamite", "Rajesh",
							"Kouthrapali", "rj@particle.edu",
							"../images/browndynamite-image.gif");

					m_commandsController.submitCommand(m_command
							.createRequestJoinCommand(prospect));
				});

				this.chatTextArea
						.change(
								{
									panel : this
								},
								function(event) {
									m_commandsController
											.submitCommand(m_command
													.createSubmitChatMessageCommand(event.data.panel.chatTextArea
															.val()));

									event.data.panel.chatTextArea.val("");
									event.data.panel.chatTextArea.focus();
								});

				/**
				 * 
				 */
				SessionLogPanel.prototype.processCommand = function(command) {
					var row = "<tr class=\"sessionLogTableRow\">";

					row += "<td valign=\"top\">";
					row += "<table><tr><td>";

					var imageUrl = null;

					if (command.type == m_constants.REQUEST_JOIN_COMMAND) {
						imageUrl = "../images/" + command.newObject.account + "-photo.gif"; //command.newObject.imageUrl;
					} else if (command.type == m_constants.CONFIRM_JOIN_COMMAND) {
						imageUrl = "../images/" + command.oldObject.account + "-photo.gif"; //command.oldObject.imageUrl;
					} else {
						imageUrl = "../images/" + "sheldor-photo.gif"; //m_session.current().participants[command.account].imageUrl;
					}

					row += "<img src=\"" + imageUrl + "\"/>";
					row += "</td><td valign=\"top\"><span id=\"userTag\">";
					row += command.account;
					row += "</span><br><span id=\"dateTag\">";
					row += command.timestamp;
					row += "</span></td></tr></table></td></tr>";
					row += "<tr>";
					row += "<td valign=\"top\" align=\"left\"><span id=\"messageTag\">";

					var prospect = null;

					if (command.type == m_constants.REQUEST_JOIN_COMMAND) {
						row += ("User " + command.newObject.firstName + " "
								+ command.newObject.lastName + " is intending to join this modeling session. <a id=\"joinSessionConfirmLink\"><img src=\"../../views-common/images/icons/user-invalidated.png\"</a>");

						prospect = command.newObject;

					} else if (command.type == m_constants.CONFIRM_JOIN_COMMAND) {
						row += ("User " + command.oldObject.firstName + " "
								+ command.oldObject.lastName + " joining was confirmed.");
					} else if (command.type == m_constants.SUBMIT_CHAT_MESSAGE_COMMAND) {
						row += command.newObject;
					} else {
						// TODO Can be more specific
						row += "Model element(s) added, changed or deleted.";
					}

					row += "</span></td>";
					row += "</tr>";
					row += "</table>";
					row += "</td>";
					row += "</tr>";

					if (jQuery("tr.sessionLogTableRow")[0] == null) {
						this.sessionLogTable.append(row);
					} else {
						jQuery("tr.sessionLogTableRow{0}").before(row);
					}

					if (prospect != null) {
						jQuery("#joinSessionConfirmLink")
								.click(
										{},
										function(event) {
											m_commandsController
													.submitCommand(m_command
															.createConfirmJoinCommand(prospect));
										});
					}
				};
			}
			;
		});