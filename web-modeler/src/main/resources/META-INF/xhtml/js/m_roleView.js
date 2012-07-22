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
		[ "m_utils", "m_command", "m_commandsController", "m_dialog", "m_view",
				"m_model"],
		function(m_utils, m_command, m_commandsController, m_dialog, m_view, m_model) {
			return {
				initialize : function(fullId) {
					var role = m_model.findParticipant(fullId);

					var view = new RoleView();
					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(role);
				}
			};

			/**
			 * 
			 */
			function RoleView() {
				// Inheritance

				var view = m_view.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(RoleView.prototype, view);

				this.guidOutput = jQuery("#guidOutput");
				this.idOutput = jQuery("#idOutput");
				this.nameInput = jQuery("#nameInput");

				this.nameInput.change({
					"view" : this
				}, function(event) {
					var view = event.data.view;

					if (!view.validate()) {
						return;
					}

					if (view.role.name != view.nameInput.val()) {
						view.submitChanges({
							name : view.nameInput.val()
						});
					}
				});

				/**
				 * 
				 */
				RoleView.prototype.initialize = function(
						role) {
					this.role= role;

					this.guidOutput.empty();
					this.guidOutput.append(this.role.oid);
					this.idOutput.empty();
					this.idOutput.append(this.role.id);
					this.nameInput.val(this.role.name);
				};

				/**
				 * 
				 */
				RoleView.prototype.toString = function() {
					return "Lightdust.RoleView";
				};

				/**
				 * 
				 */
				RoleView.prototype.validate = function() {
					this.clearErrorMessages();

					this.nameInput.removeClass("error");
					this.camelContextInput.removeClass("error");

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
						this.errorMessages
								.push("Application name must not be empty.");
						this.nameInput.addClass("error");
					}

					if (this.camelContextInput.val() == null
							|| this.camelContextInput.val() == "") {
						this.errorMessages
								.push("Camel Context must not be empty.");
						this.nameInput.addClass("error");
					}

					if (this.errorMessages.length > 0) {
						this.showErrorMessages();

						return false;
					}

					return true;
				};

				/**
				 * 
				 */
				RoleView.prototype.submitChanges = function(changes) {
					// Generic attributes

					if (changes.attributes == null) {
						changes.attributes = {};
					}

					changes.attributes["carnot:engine:camel::producerMethodName"] = "executeMessage(java.lang.Object)";

					m_commandsController.submitCommand(m_command
							.createUpdateModelElementCommand(
									this.application.model.id,
									this.application.oid, changes));
				};

				/**
				 * 
				 */
				RoleView.prototype.processCommand = function(
						command) {
					m_utils.debug("===> Camel Process Command");
					m_utils.debug(command);

					// Parse the response JSON from command pattern

					var object = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != object && null != object.changes
							&& null != object.changes.modified
							&& 0 != object.changes.modified.length
							&& object.changes.modified[0].oid == this.application.oid) {

						m_utils.inheritFields(this.application, object.changes.modified[0]);
						
						this.initialize(this.application);
					}
				};
			}
		});