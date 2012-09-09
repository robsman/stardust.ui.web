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
		[ "m_utils", "m_constants", "m_commandsController", "m_command",
				"m_user" ],
		function(m_utils, m_constants, m_commandsController, m_command, m_user) {

			return {
				initialize : initialize,
				getInstance : function() {
					return initialize();
				},
				renew : renew
			};

			/**
			 * 
			 */
			function initialize() {
				if (window.top.modelingSession == null) {
					window.top.modelingSession = new Session(m_user
							.getCurrentUser());
					m_commandsController
							.registerCommandHandler(window.top.modelingSession);
				}

				return window.top.modelingSession;
			}

			/**
			 * 
			 */
			function renew() {
				window.top.modelingSession = new Session(m_user
						.getCurrentUser());
				m_commandsController
						.registerCommandHandler(window.top.modelingSession);

				return window.top.modelingSession;
			}

			/**
			 * 
			 */
			function Session(sessionOwner) {
				this.prospects = new Array();
				this.participants = new Array();
				this.owner = sessionOwner;
				this.loggedInUser = sessionOwner;
				this.joined = false;
				this.technologyPreview = false;
				var sessionCallbackObj = this;
				var startTime = new Date();

				Session.prototype.toString = function() {
					return "Lightdust.Session";
				};

				/**
				 * 
				 */
				Session.prototype.processCommand = function(command) {
					if (command.type == m_constants.ACCEPT_INVITE_COMMAND
							&& joined) {
						var oldObject = {
							"account" : command.oldObject.sessionOwner
						};
						m_commandsController.submitCommand(m_command
								.createFetchProspects(oldObject));

					} else if (command.type == m_constants.CONFIRM_JOIN_COMMAND) {
						this.joined = true;
						if (command.account != this.owner.account) {
							this.owner = m_user.createUser(command.account,
									"name", "lastname", null, null); // cannot
							// retrieve
							// data
							// while
							// sending
							// acceptinvitecommand.
							// In
							// the
							// class
							// the
							// user
							// service
							// is
							// not
							// available
							// therefor
							// I
							// cannot
							// get
							// any
							// information
							// about
							// the
							// owner.
						}

						var oldObject = {
							"account" : command.account
						};
						m_commandsController.submitCommand(m_command
								.createFetchCollaborators(oldObject));

					} else if (command.type == m_constants.UPDATE_INVITED_USERS_COMMAND) {
						console.log("Session starts working on "
								+ m_constants.UPDATE_INVITED_USERS_COMMAND);
						this.owner.color = command.ownerColor;

						if (command.operation == "updateProspects") {
							sessionCallbackObj.prospects = [];
							jQuery.each(command.oldObject.users, function(item,
									value) {
								sessionCallbackObj.prospects.push(m_user
										.createUser(value.account,
												value.firstName,
												value.lastName, value.email,
												null));
							});
						} else if (command.operation == "updateCollaborators") {
							sessionCallbackObj.participants = [];
							jQuery
									.each(
											command.oldObject.users,
											function(item, value) {
												var user = m_user.createUser(
														value.account,
														value.firstName,
														value.lastName,
														value.email, null,
														value.color);
												var idx = -1;
												for ( var i = 0; i < sessionCallbackObj.prospects.length; i++) {
													if (user.account == sessionCallbackObj.prospects[i].account) {
														idx = i;
														break;
													}
												}

												if (idx != -1) {
													sessionCallbackObj.prospects
															.splice(idx, 1);
												}
												sessionCallbackObj.participants
														.push(user);

											});
						}
					}
				};

				/**
				 * 
				 */
				function refreshPreferences() {
					m_communicationController
							.syncGetData(
									{
										url : m_communicationController
												.getEndpointUrl()
												+ "/preferences"
									},
									{
										"success" : function(json) {
											m_utils.debug("===> Preferences");
											m_utils.debug(json);

											window.top.modelingSession.technologyPreview = json.showTechnologyPreview;
											window.top.modelingSession.defaultProfile = json.defaultProfile;
										},
										"error" : function() {
											alert('Error occured while fetching models');
										}
									});
				}

				Session.prototype.getColorByUser = function(username) {
					if (username != this.owner.account) {
						var userCol = "";
						for ( var i = 0; i < this.participants.length; i++) {
							if (this.participants[i].account == username) {
								userCol = "#" + this.participants[i].color;
							}
						}
						;
						return userCol;
					} else {
						return "#" + this.owner.color;
					}
				};

				/**
				 * 
				 */
				Session.prototype.getUserByAccount = function(account) {
					if (this.owner.account == account) {
						return this.owner;
					}

					for ( var n = 0; n < this.prospects.length; ++n) {
						if (this.prospects[n].account == account) {
							return this.prospects[n];
						}
					}

					for (n = 0; n < this.participants.length; ++n) {
						if (this.participants[n].account == account) {
							return this.participants[n];
						}
					}
				};
			}
		});