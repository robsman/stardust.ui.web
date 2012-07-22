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
					var data = m_model.findData(fullId);

					view = new DataView();
					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(data);
				}
			};

			/**
			 * 
			 */
			function DataView() {
				// Inheritance

				var view = m_view.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(DataView.prototype, view);

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

					if (view.data.name != view.nameInput.val()) {
						view.submitChanges({
							name : view.nameInput.val()
						});
					}
				});

				/**
				 * 
				 */
				DataView.prototype.initialize = function(
						data) {
					this.data = data;

					this.guidOutput.empty();
					this.guidOutput.append(this.data.oid);
					this.idOutput.empty();
					this.idOutput.append(this.data.id);
					this.nameInput.val(this.data.name);

					if (this.data.attributes == null) {
						this.data.attributes = {};
					}
				};

				/**
				 * 
				 */
				DataView.prototype.toString = function() {
					return "Lightdust.DataView";
				};

				/**
				 * 
				 */
				DataView.prototype.validate = function() {
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
				DataView.prototype.submitChanges = function(changes) {
					// Generic attributes

					if (changes.attributes == null) {
						changes.attributes = {};
					}

					m_commandsController.submitCommand(m_command
							.createUpdateModelElementCommand(
									this.data.model.id,
									this.data.oid, changes));
				};

				/**
				 * 
				 */
				DataView.prototype.processCommand = function(
						command) {
					// Parse the response JSON from command pattern

					var object = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != object && null != object.changes
							&& null != object.changes.modified
							&& 0 != object.changes.modified.length
							&& object.changes.modified[0].oid == this.data.oid) {

						m_utils.inheritFields(this.data, object.changes.modified[0]);
						
						this.initialize(this.data);
					}
				};
			}
		});