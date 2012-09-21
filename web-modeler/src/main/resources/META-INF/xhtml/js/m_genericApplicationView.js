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
		[ "m_utils", "m_extensionManager", "m_command", "m_commandsController", "m_dialog", "m_modelElementView",
				"m_model"],
		function(m_utils, m_extensionManager, m_command, m_commandsController, m_dialog, m_modelElementView, m_model) {
			return {
				initialize : function(fullId) {
					var view = new GenericApplicationView();
					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(m_model.findApplication(fullId));
				}
			};

			/**
			 *
			 */
			function GenericApplicationView() {
				// Inheritance

				var view = m_modelElementView.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(GenericApplicationView.prototype, view);

				this.unsupportedMessagePanel = jQuery("#unsupportedMessagePanel");

				/**
				 *
				 */
				GenericApplicationView.prototype.initialize = function(
						application) {
					this.initializeModelElementView();
					this.initializeModelElement(application);
					this.application = application;

					m_utils.debug("===> Application");
					m_utils.debug(this.application);

					var extension = m_extensionManager.findExtensions(
							"applicationType", "id", this.application.applicationType)[0];

					this.unsupportedMessagePanel.empty();
					this.unsupportedMessagePanel.append("Display and editing of the Application Type <b>" + extension.readableName + "</b> is not yet supported for the Browser Modeler. Please use the Eclipse Modeler to configure this Application. However, configured Applications of this type can be used for modeling.");
				};

				/**
				 *
				 */
				GenericApplicationView.prototype.toString = function() {
					return "Lightdust.GenericApplicationView";
				};

				/**
				 *
				 */
				GenericApplicationView.prototype.validate = function() {
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
				GenericApplicationView.prototype.processCommand = function(
						command) {
					// Parse the response JSON from command pattern

					var object = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != object && null != object.changes
							&& null != object.changes.modified
							&& 0 != object.changes.modified.length
							&& object.changes.modified[0].uuid == this.application.uuid) {

						m_utils.inheritFields(this.application, object.changes.modified[0]);

						this.initialize(this.application);
					}
				};
			}
		});