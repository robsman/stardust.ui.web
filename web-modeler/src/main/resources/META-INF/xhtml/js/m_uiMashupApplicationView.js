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
		[ "m_utils", "m_constants", "m_command", "m_commandsController",
				"m_dialog", "m_modelElementView", "m_model",
				"m_typeDeclaration", "m_dataTypeSelector", "m_parameterDefinitionsPanel" ],
		function(m_utils, m_constants, m_command, m_commandsController,
				m_dialog, m_modelElementView, m_model, m_typeDeclaration,
				m_dataTypeSelector, m_parameterDefinitionsPanel) {
			return {
				initialize : function(fullId) {
					var view = new UiMashupApplicationView();
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

				var view = m_modelElementView.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(UiMashupApplicationView.prototype, view);

				/**
				 *
				 */
				UiMashupApplicationView.prototype.initialize = function(
						application) {
					this.initializeModelElementView();

					this.application = application;

					m_utils.debug("===> Application");
					m_utils.debug(this.application);

					this.currentAccessPoint = null;

					this.initializeModelElement(application);

					this.parameterDefinitionsPanel = m_parameterDefinitionsPanel.create({scope: "uiMashupApplicationView",
						submitHandler: this, listType: "object",
						supportsDataMappings : false,
						supportsDescriptors : false,
						supportsDataTypeSelection : true
						});

					this.parameterDefinitionsPanel.setScopeModel(this.application.model);
					this.parameterDefinitionsPanel.setParameterDefinitions(this.application.accessPoints);
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

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
						this.errorMessages.push("Data name must not be empty.");
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
				UiMashupApplicationView.prototype.getModelElement = function() {
					return this.application;
				};

				/**
				 *
				 */
				UiMashupApplicationView.prototype.submitParameterDefinitionsChanges = function(
						parameterDefinitionsChanges) {
					this.submitChanges({accessPoints: parameterDefinitionsChanges});
				};

				/**
				 *
				 */
				UiMashupApplicationView.prototype.processCommand = function(
						command) {
					m_dialog.showAutoCursor();

					if (command.type == m_constants.CHANGE_USER_PROFILE_COMMAND) {
						this.initialize(this.application);

						return;
					}

					var object = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != object
							&& null != object.changes
							&& null != object.changes.modified
							&& 0 != object.changes.modified.length
							&& object.changes.modified[0].uuid == this.application.uuid) {

						m_utils.inheritFields(this.application,
								object.changes.modified[0]);

						this.initialize(this.application);
					}
				};
			}
		});