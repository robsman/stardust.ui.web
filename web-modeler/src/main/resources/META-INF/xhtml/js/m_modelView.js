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
			var view;

			return {
				initialize : function(modelId) {
					var model = m_model.findModel(modelId);

					view = new ModelView();
					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(model);
				}
			};

			/**
			 * 
			 */
			function ModelView() {
				// Inheritance

				var view = m_view.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(ModelView.prototype, view);

				this.nameInput = jQuery("#nameInput");

				this.nameInput.change({
					"view" : this
				}, function(event) {
					var view = event.data.view;

					if (!view.validate()) {
						return;
					}

					if (view.model.name != view.nameInput.val()) {
						view.submitChanges({
							name : view.nameInput.val()
						});
					}
				});

				/**
				 * 
				 */
				ModelView.prototype.initialize = function(
						model) {
					this.model = model;

					this.nameInput.val(this.model.name);

					if (this.model.attributes == null) {
						this.model.attributes = {};
					}
				};

				/**
				 * 
				 */
				ModelView.prototype.toString = function() {
					return "Lightdust.ModelView";
				};

				/**
				 * 
				 */
				ModelView.prototype.validate = function() {
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
				ModelView.prototype.submitChanges = function(changes) {
					// Generic attributes

					if (changes.attributes == null) {
						changes.attributes = {};
					}

					m_commandsController.submitCommand(m_command
							.createUpdateModelElementCommand(
									this.model.model.id,
									this.model.oid, changes));
				};

				/**
				 * 
				 */
				ModelView.prototype.processCommand = function(
						command) {
					// Parse the response JSON from command pattern

					var object = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != object && null != object.changes
							&& null != object.changes.modified
							&& 0 != object.changes.modified.length
							&& object.changes.modified[0].oid == this.model.oid) {

						m_utils.inheritFields(this.model, object.changes.modified[0]);
						
						this.initialize(this.model);
					}
				};
			}
		});