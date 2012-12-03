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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_extensionManager",
				"bpm-modeler/js/m_command",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_dialog", "bpm-modeler/js/m_modelElementView",
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_jsfViewManager",
				"bpm-modeler/js/m_elementConfiguration" ],
		function(m_utils, m_constants, m_extensionManager, m_command,
				m_commandsController, m_dialog, m_modelElementView, m_model,
				m_jsfViewManager, m_elementConfiguration) {
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
				var viewManager = m_jsfViewManager.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(GenericApplicationView.prototype, view);

				this.unsupportedMessagePanel = jQuery("#unsupportedMessagePanel");

				/**
				 *
				 */
				GenericApplicationView.prototype.initialize = function(
						application) {
					this.id = "genericApplicationView";

					this.initializeModelElementView(application);
				};

				/**
				 *
				 */
				GenericApplicationView.prototype.setModelElement = function(
						application) {
					this.initializeModelElement(application);

					this.application = application;
					this.updateViewIcon();
					m_utils.debug("===> Application");
					m_utils.debug(this.application);

					var extension = m_extensionManager.findExtensions(
							"applicationType", "id",
							this.application.applicationType)[0];

					this.unsupportedMessagePanel.empty();
					this.unsupportedMessagePanel
							.append("Display and editing of the Application Type <b>"
									+ extension.readableName
									+ "</b> is not yet supported for the Browser Modeler. Please use the Eclipse Modeler to configure this Application. However, configured Applications of this type can be used for modeling.");
				};


				/**
				 * TODO - handle unsupported data types too.?
				 */
				GenericApplicationView.prototype.updateViewIcon = function() {
					var icon = m_elementConfiguration
							.getIconForElementType(this.application.applicationType);
					if (icon) {
						viewManager.updateView("genericApplicationView",
								m_constants.VIEW_ICON_PARAM_KEY + "="
										+ icon, this.application.uuid);
					}
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
			}
		});