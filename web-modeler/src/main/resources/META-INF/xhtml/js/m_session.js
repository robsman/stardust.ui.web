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
		[ "m_utils", "m_constants", "m_communicationController",
				"m_commandsController", "m_command", "m_user" ],
		function(m_utils, m_constants, m_communicationController,
				m_commandsController, m_command, m_user) {
			this.prospects = new Array();
			this.participants = new Array();
			return {
				initialize : initialize,
				getInstance : function() {
					return initialize();
				}
			};

			function initialize() {
				if (window.top.modelingSession == null) {

					window.top.modelingSession = new Session(
							m_user.getCurrentUser);
					m_commandsController
							.registerCommandHandler(window.top.modelingSession);
					refreshPreferences();
				}

				m_utils.debug("Session: ");
				m_utils.debug(window.top.modelingSession);

				return window.top.modelingSession;
			}

			/**
			 * 
			 */
			function Session(sessionOwner) {
				var owner = sessionOwner;
				var startTime = new Date();
				var prospects = [];
				var collaborators = [];
				var joined = false;

				this.technologyPreview = true;

				Session.prototype.toString = function() {
					return "Lightdust.Session";
				};

				/**
				 * 
				 */
				Session.prototype.processCommand = function(command) {
					if (command.type == m_constants.REQUEST_JOIN_COMMAND
							&& joined) {
						m_commandsController.submitCommand(m_command
								.createFetchProspects(command.account));

					} else if (command.type == m_constants.CONFIRM_JOIN_COMMAND) {
						joined = true;
						if (command.account != owner) {
							owner = command.account;
						}
						console.log(owner);
						var oldObject = {
							"account" : owner
						};
						m_commandsController.submitCommand(m_command
								.createFetchCollaborators(oldObject));

					} else if (command.type == m_constants.UPDATE_INVITED_USERS_COMMAND) {
						jQuery
								.each(
										command.oldObject.users,
										function(item, value) {
											if (command.operation == "updateProspects") {
												console.log("hello!");
												prospects.push(value.account);
											} else if (command.operation == "updateCollaborators") {
												console.log("hello! Collabs");
												collaborators
														.push(value.account);
											}
										});

						console.log(collaborators);
						console.log(prospects);
					}
				};
			}

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

		});