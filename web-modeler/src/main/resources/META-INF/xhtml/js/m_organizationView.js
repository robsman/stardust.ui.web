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
				"m_model", "m_typeDeclaration" ],
		function(m_utils, m_command, m_commandsController, m_dialog, m_view, m_model,
				m_typeDeclaration) {
			var view;

			return {
				initialize : function(fullId) {
					var organization = m_model.findParticipant(fullId);

					view = new OrganizationView();
					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(organization);
				}
			};

			/**
			 * 
			 */
			function OrganizationView() {
				// Inheritance

				var view = m_view.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(OrganizationView.prototype, view);

				this.guidOutput = jQuery("#guidOutput");
				this.idOutput = jQuery("#idOutput");
				this.nameInput = jQuery("#nameInput");

				jQuery("#organizationTabs").tabs();

				this.nameInput.change({
					"view" : this
				}, function(event) {
					var view = event.data.view;

					if (!view.validate()) {
						return;
					}

					if (view.application.name != view.nameInput.val()) {
						view.submitChanges({
							name : view.nameInput.val()
						});
					}
				});

				/**
				 * 
				 */
				OrganizationView.prototype.initialize = function(
						organization) {
					this.organization = organization;

					this.guidOutput.empty();
					this.guidOutput.append(this.organization.oid);
					this.idOutput.empty();
					this.idOutput.append(this.organization.id);
					this.nameInput.val(this.organization.name);

					if (this.organization.attributes == null) {
						this.organization.attributes = {};
					}

				};

				/**
				 * 
				 */
				OrganizationView.prototype.toString = function() {
					return "Lightdust.OrganizationView";
				};

				/**
				 * 
				 */
				OrganizationView.prototype.validate = function() {
					this.clearErrorMessages();

					this.nameInput.removeClass("error");
					this.camelContextInput.removeClass("error");

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
						this.errorMessages
								.push("Application name must not be empty.");
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
				OrganizationView.prototype.submitChanges = function(changes) {
					// Generic attributes

					if (changes.attributes == null) {
						changes.attributes = {};
					}

					m_commandsController.submitCommand(m_command
							.createUpdateModelElementCommand(
									this.organization.model.id,
									this.organization.oid, changes));
				};

				/**
				 * 
				 */
				OrganizationView.prototype.processCommand = function(
						command) {
					// Parse the response JSON from command pattern

					var object = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != object && null != object.changes
							&& null != object.changes.modified
							&& 0 != object.changes.modified.length
							&& object.changes.modified[0].oid == this.organization.oid) {

						m_utils.inheritFields(this.organization, object.changes.modified[0]);
						
						this.initialize(this.organization);
					}
				};
			}
		});