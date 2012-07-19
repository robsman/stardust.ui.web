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
				initialize : function() {
					var modelId = jQuery.url.setUrl(window.location.search)
							.param("modelId");
					var applicationId = jQuery.url.setUrl(
							window.location.search).param("applicationId");
					var model = m_model.findModel(modelId);
					var application = model.applications[applicationId];

					view = new RoleView();
					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(application);
				}
			};

			/**
			 * 
			 */
			function RoleView() {
				// Inheritance

				var view = m_view.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(RoleView.prototype, view);

				this.nameInput = jQuery("#nameInput");
				this.camelContextInput = jQuery("#camelContextInput");
				this.routeTextarea = jQuery("#routeTextarea");
				this.additionalBeanSpecificationTextarea = jQuery("#additionalBeanSpecificationTextarea");
				this.requestDataInput = jQuery("#requestDataInput");
				this.responseDataInput = jQuery("#responseDataInput");

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
				this.camelContextInput
						.change(
								{
									"view" : this
								},
								function(event) {
									var view = event.data.view;

									if (!view.validate()) {
										return;
									}

									if (view.application.attributes["carnot:engine:camel::camelContextId"] !=
											 view.nameInput.val()) {
										view
												.submitChanges({
													attributes : {
														"carnot:engine:camel::camelContextId" : view.camelContextInput
																.val()
													}
												});
									}
								});
				this.routeTextarea
						.change(
								{
									"view" : this
								},
								function(event) {
									var view = event.data.view;

									if (!view.validate()) {
										return;
									}

									if (view.application.attributes["carnot:engine:camel::routeEntries"] !=
											 view.routeTextarea.val()) {
										view
												.submitChanges({
													attributes : {
														"carnot:engine:camel::routeEntries" : view.routeTextarea
																.val()
													}
												});
									}
								});
				this.additionalBeanSpecificationTextarea
						.change(
								{
									"view" : this
								},
								function(event) {
									var view = event.data.view;

									if (!view.validate()) {
										return;
									}

									if (view.application.attributes["carnot:engine:camel::additionalSpringBeanDefinitions"] != view.additionalBeanSpecificationTextarea
											.val()) {
										view
												.submitChanges({
													attributes : {
														"carnot:engine:camel::additionalSpringBeanDefinitions" : view.additionalBeanSpecificationTextarea
																.val()
													}
												});
									}
								});

				/**
				 * 
				 */
				RoleView.prototype.initialize = function(
						application) {
					this.application = application;

					this.nameInput.val(this.application.name);

					if (this.application.attributes == null) {
						this.application.attributes = {};
					}

					if (this.application.attributes["carnot:engine:camel::camelContextId"] == null) {
						this.application.attributes["carnot:engine:camel::camelContextId"] = "Default";
					}

					this.camelContextInput
							.val(this.application.attributes["carnot:engine:camel::camelContextId"]);
					this.routeTextarea
							.val(this.application.attributes["carnot:engine:camel::routeEntries"]);
					this.additionalBeanSpecificationTextarea
							.val(this.application.attributes["carnot:engine:camel::additionalSpringBeanDefinitions"]);
				};

				/**
				 * 
				 */
				RoleView.prototype.toString = function() {
					return "Lightdust.RoleView";
				};

				/**
				 * 
				 */
				RoleView.prototype.validate = function() {
					this.clearErrorMessages();

					this.nameInput.removeClass("error");
					this.camelContextInput.removeClass("error");

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
						this.errorMessages
								.push("Application name must not be empty.");
						this.nameInput.addClass("error");
					}

					if (this.camelContextInput.val() == null
							|| this.camelContextInput.val() == "") {
						this.errorMessages
								.push("Camel Context must not be empty.");
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
				RoleView.prototype.submitChanges = function(changes) {
					// Generic attributes

					if (changes.attributes == null) {
						changes.attributes = {};
					}

					changes.attributes["carnot:engine:camel::producerMethodName"] = "executeMessage(java.lang.Object)";

					m_commandsController.submitCommand(m_command
							.createUpdateModelElementCommand(
									this.application.model.id,
									this.application.oid, changes));
				};

				/**
				 * 
				 */
				RoleView.prototype.processCommand = function(
						command) {
					m_utils.debug("===> Camel Process Command");
					m_utils.debug(command);

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