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
		[ "m_utils", "m_constants", "m_commandsController", "m_user" ],
		function(m_utils, m_constants, m_commandsController, m_user) {

			return {
				current : function() {
					if (window.top.modelingSession == null) {

						// TODO Obtain from server

						window.top.modelingSession = new Session(m_user
								.createUser("sheldor", "Sheldon", "Cooper",
										"sheldor@particle.edu",
										"../images/test-image.jpg"));

						m_commandsController
								.registerCommandHandler(window.top.modelingSession);
					}

					m_utils.debug("Session: ");
					m_utils.debug(window.top.modelingSession);
					
					return window.top.modelingSession;
				}
			};

			/**
			 * 
			 */
			function Session(sessionOwner) {
				this.owner = sessionOwner;
				this.participants = {};
				this.prospects = {};
				this.startTime = new Date();

				this.participants[this.owner.account] = this.owner;

				/**
				 * 
				 */
				Session.prototype.toString = function() {
					return "Lightdust.Session";
				};

				/**
				 * 
				 */
				Session.prototype.processCommand = function(command) {
					if (command.type == m_constants.REQUEST_JOIN_COMMAND) {
						// TODO For testing, get from server using the account
						// attribute

						this.prospects[prospect.account] = command.oldObject;
					} else if (command.type == m_constants.CONFIRM_JOIN_COMMAND) {
						this.participants[command.newObject.account] = this.prospects[command.newObject.account];

						delete this.prospects[command.newObject.account];
					}
				};
			}
		});