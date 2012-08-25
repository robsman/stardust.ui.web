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
				"m_typeDeclaration" ],
		function(m_utils, m_constants, m_command, m_commandsController,
				m_dialog, m_modelElementView, m_model, m_typeDeclaration) {
			return {
				initialize : function(fullId) {
					var conditionalPerformer = m_model.findParticipant(fullId);
					var view = new ConditionalPerformerView();
					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(conditionalPerformer);
				}
			};

			/**
			 * 
			 */
			function ConditionalPerformerView() {
				var modelElementView = m_modelElementView.create();

				m_utils.inheritFields(this, modelElementView);
				m_utils.inheritMethods(ConditionalPerformerView.prototype,
						modelElementView);

				/**
				 * 
				 */
				ConditionalPerformerView.prototype.initialize = function(
						conditionalPerformer) {
					this.initializeModelElementView();

					this.conditionalPerformer = conditionalPerformer;

					this.initializeModelElement(conditionalPerformer);

					m_utils.debug("===> Conditional Performer");
					m_utils.debug(conditionalPerformer);

					this.publicVisibilityCheckbox = jQuery("#publicVisibilityCheckbox");
					this.performerTypeSelect = jQuery("#performerTypeSelect");
					this.bindingDataSelect = jQuery("#bindingDataSelect");
					this.bindingDataPathInput = jQuery("#bindingDataPathInput");

					this.publicVisibilityCheckbox
							.change(
									{
										"view" : this
									},
									function(event) {
										var view = event.data.view;

										if (!view.validate()) {
											return;
										}

										if (view.modelElement.attributes["carnot:engine:visibility"] != "Public") {
											view
													.submitChanges({
														attributes : {
															"carnot:engine:visibility" : "Public"
														}
													});
										}
									});

					this.populateBindingDataSelect();

					if ("Public" == this.conditionalPerformer.attributes["carnot:engine:visibility"])) {
						this.publicVisibilityCheckbox.attr("checked", true);
					} else {
						this.publicVisibilityCheckbox.attr("checked", false);
					}
				};

				/**
				 * 
				 */
				ConditionalPerformerView.prototype.populateBindingDataSelect = function() {
					this.bindingDataSelect.empty();

					for ( var n in m_model.getModels()) {
						for ( var m in m_model.getModels()[n].dataItems) {
							var data = m_model.getModels()[n].dataItems[m];

							this.bindingDataSelect.append("<option value='"
									+ data.getFullId() + "'>"
									+ m_model.getModels()[n].name + "/"
									+ data.name + "</option>");
						}
					}
				};

				/**
				 * 
				 */
				ConditionalPerformerView.prototype.toString = function() {
					return "Lightdust.ConditionalPerformerView";
				};

				/**
				 * 
				 */
				ConditionalPerformerView.prototype.validate = function() {
					this.clearErrorMessages();

					this.nameInput.removeClass("error");

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
				ConditionalPerformerView.prototype.processCommand = function(
						command) {
					// Parse the response JSON from command pattern

					var object = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != object
							&& null != object.changes
							&& null != object.changes.modified
							&& 0 != object.changes.modified.length
							&& object.changes.modified[0].oid == this.conditionalPerformer.oid) {

						m_utils.inheritFields(this.conditionalPerformer,
								object.changes.modified[0]);

						this.initialize(this.conditionalPerformer);
					}
				};
			}
		});