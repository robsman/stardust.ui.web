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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_globalVariables", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_command",
				"bpm-modeler/js/m_user", "bpm-modeler/js/m_communicationController" ],
		function(m_utils, m_globalVariables, m_constants, m_commandsController, m_command, m_user,
				m_communicationController) {

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
				if (m_globalVariables.get("modelingSession") == null) {
					m_globalVariables.set("modelingSession",new Session(m_user
							.getCurrentUser()));
					m_globalVariables.get("modelingSession").initialize();
					m_commandsController
							.registerCommandHandler(m_globalVariables.get("modelingSession"));
				}

				return m_globalVariables.get("modelingSession");
			}

			/**
			 *
			 */
			function renew() {
				m_globalVariables.set("modelingSession",new Session(m_user
						.getCurrentUser()));
				m_commandsController
						.registerCommandHandler(m_globalVariables.get("modelingSession"));

				return m_globalVariables.get("modelingSession");
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
				this.currentProfile = m_constants.BUSINESS_ANALYST_ROLE;
				this.technologyPreview = false; // be careful when committing anything but false
				var sessionCallbackObj = this;
				var startTime = new Date();
				this.ownerJson = "";

				Session.prototype.toString = function() {
					return "Lightdust.Session";
				};

				/**
				 *
				 */
				Session.prototype.processCommand = function(command) {
					if (command.type == m_constants.ACCEPT_INVITE_COMMAND) {
						var oldObject = {
							"account" : command.oldObject.sessionOwner
						};
						m_commandsController.submitCommand(m_command
								.createFetchProspects(oldObject));

					} else if (command.type == m_constants.CONFIRM_JOIN_COMMAND) {
						joined = true;

						var oldObject = {
							"sessionId" : command.oldObject.sessionId
						};
						submitCommand(m_command.createFetchOwner(oldObject));

					} else if (command.type == m_constants.UPDATE_INVITED_USERS_COMMAND) {
						console.log("Session starts working on "
								+ m_constants.UPDATE_INVITED_USERS_COMMAND);
						this.owner.color = command.ownerColor;
						m_utils.debug(this.owner);

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

											m_globalVariables.set("modelingSession.technologyPreview", json.showTechnologyPreview);
											m_globalVariables.set("modelingSession.currentProfile", json.defaultProfile);
										},
										"error" : function() {
											alert('Error occured while fetching models');
										}
									});
				};

				Session.prototype.initialize = function() {
					refreshPreferences();
				};

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


				Session.prototype.isOwner = function(username){
					if(this.owner.account == username){
						return true;
					}
					return false;
				}

				function submitCommand(command) {
					var url = m_communicationController.getEndpointUrl()
							+ command.path;
					var obj = [];

					if (command.operation != null) {
						url += "/" + command.operation;
					}

					m_communicationController.postData({
						"url" : url,
						"sync" : command.sync ? true : false
					},
					// Added to remove any cyclic reference
					JSON.stringify(command, function(key, val) {
						if (typeof val == "object") {
							if (obj.indexOf(val) >= 0)
								return undefined;
							obj.push(val);
						}
						return val;
					}), new function() {
						return {
							"success" : function(command) {
								sessionCallbackObj.ownerJson = command;
								m_utils.debug("Command was placed in");
								m_utils.debug(sessionCallbackObj.ownerJson);

								sessionCallbackObj.owner = m_user.createUser(
										sessionCallbackObj.ownerJson.account,
										sessionCallbackObj.ownerJson.firstName,
										sessionCallbackObj.ownerJson.lastName,
										sessionCallbackObj.ownerJson.email,
										null);
								m_utils.debug(this.owner);
								m_utils.debug(sessionCallbackObj.owner);

								var oldObject = {
									"account" : command.account
								};
								m_commandsController.submitCommand(m_command
										.createFetchCollaborators(oldObject));
							},
							"error" : function(command) {
								alert('Error occured while fetching owner');
							}
						};
					});
				}
				;
			}
		});
