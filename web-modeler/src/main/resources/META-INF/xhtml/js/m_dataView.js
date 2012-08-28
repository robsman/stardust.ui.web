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
		[ "m_utils", "m_constants", "m_extensionManager", "m_command", "m_commandsController",
				"m_dialog", "m_modelElementView", "m_model",
				"m_typeDeclaration", "m_dataTypeSelector" ],
		function(m_utils, m_constants, m_extensionManager, m_command, m_commandsController,
				m_dialog, m_modelElementView, m_model, m_typeDeclaration, m_dataTypeSelector) {
			var view;

			return {
				initialize : function(fullId) {
					var data = m_model.findData(fullId);

					m_utils.debug("===>  Data");
					m_utils.debug(data);

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
				var view = m_modelElementView.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(DataView.prototype, view);

				/**
				 * 
				 */
				DataView.prototype.initialize = function(data) {
					this.data = data;

					this.initializeModelElementView();
					this.initializeModelElement(data);

					this.primitiveInput = jQuery("#primitiveInput");

					this.dataTypeSelector = m_dataTypeSelector.create("dataView", this);

					this.dataTypeSelector.setScopeModel(this.data.model);
					this.dataTypeSelector.setDataType(data);
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
				DataView.prototype.submitDataChanges = function(dataChanges) {
					this.submitChanges(dataChanges);
				};
				
				/**
				 * 
				 */
				DataView.prototype.processCommand = function(command) {
					// Parse the response JSON from command pattern

					var object = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != object && null != object.changes
							&& null != object.changes.modified
							&& 0 != object.changes.modified.length
							&& object.changes.modified[0].oid == this.data.oid) {

						m_utils.inheritFields(this.data,
								object.changes.modified[0]);

						this.initialize(this.data);
					}
				};
			}
		});