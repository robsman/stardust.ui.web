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
					view = new UiMashupApplicationView();
					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(m_model.findApplication(fullId));
				}
			};

			/**
			 * 
			 */
			function UiMashupApplicationView() {
				// Inheritance

				var view = m_view.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(UiMashupApplicationView.prototype, view);

				this.nameInput = jQuery("#nameInput");

				this.nameInput.change({
					"view" : this
				}, function(event) {
					var view = event.application.view;

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
				UiMashupApplicationView.prototype.initialize = function(
						application) {
					this.application = application;

					this.nameInput.val(this.application.name);

					if (this.application.attributes == null) {
						this.application.attributes = {};
					}
				};

				/**
				 * 
				 */
				UiMashupApplicationView.prototype.toString = function() {
					return "Lightdust.UiMashupApplicationView";
				};

				/**
				 * 
				 */
				UiMashupApplicationView.prototype.validate = function() {
					this.clearErrorMessages();

					this.nameInput.removeClass("error");
					this.camelContextInput.removeClass("error");

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
						this.errorMessages
								.push("Data name must not be empty.");
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
				UiMashupApplicationView.prototype.submitChanges = function(changes) {
					// Generic attributes

					if (changes.attributes == null) {
						changes.attributes = {};
					}

					m_commandsController.submitCommand(m_command
							.createUpdateModelElementCommand(
									this.application.model.id,
									this.application.oid, changes));
				};

				/**
				 * 
				 */
				UiMashupApplicationView.prototype.processCommand = function(
						command) {
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