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
		[ "m_utils", "m_command", "m_commandsController", "m_dialog", "m_modelElementView",
				"m_model"],
		function(m_utils, m_command, m_commandsController, m_dialog, m_modelElementView, m_model) {
			return {
				initialize : function(fullId) {
					m_utils.debug("Full Id");
					m_utils.debug(fullId);

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
					
					m_utils.debug("Application");
					m_utils.debug(this.application);
					
					this.unsupportedMessagePanel.empty();
					this.unsupportedMessagePanel.append("The Application Type <b>" + this.application.applicationType + "</b> is not yet supported for the Browser Modeler. No further details provided.");
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
							&& object.changes.modified[0].oid == this.application.oid) {

						m_utils.inheritFields(this.application, object.changes.modified[0]);
						
						this.initialize(this.application);
					}
				};
			}
		});