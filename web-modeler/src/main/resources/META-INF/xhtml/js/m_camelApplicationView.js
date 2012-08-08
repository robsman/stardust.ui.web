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
		[ "m_utils", "m_commandsController", "m_dialog", "m_modelElementView",
				"m_model", "m_typeDeclaration" ],
		function(m_utils, m_commandsController, m_dialog, m_modelElementView, m_model,
				m_typeDeclaration) {
			return {
				initialize : function(fullId) {
					var view = new CamelApplicationView();
					
					// TODO Unregister!
					// In Initializer?
					m_commandsController.registerCommandHandler(view);

					view.initialize(m_model.findApplication(fullId));
				}
			};

			/**
			 * 
			 */
			function CamelApplicationView() {
				// Inheritance

				var view = m_modelElementView.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(CamelApplicationView.prototype, view);

				this.camelContextInput = jQuery("#camelContextInput");
				this.routeTextarea = jQuery("#routeTextarea");
				this.additionalBeanSpecificationTextarea = jQuery("#additionalBeanSpecificationTextarea");
				this.requestDataInput = jQuery("#requestDataInput");
				this.responseDataInput = jQuery("#responseDataInput");

				this.registerTextInputForModelElementAttributeChangeSubmission(
						this.camelContextInput, "carnot:engine:camel::camelContextId");
				this.registerTextInputForModelElementAttributeChangeSubmission(
						this.routeTextarea, "carnot:engine:camel::routeEntries");
				this.registerTextInputForModelElementAttributeChangeSubmission(
						this.additionalBeanSpecificationTextarea, "carnot:engine:camel::additionalSpringBeanDefinitions");

				/**
				 * 
				 */
				CamelApplicationView.prototype.initialize = function(
						application) {
					this.initializeModelElementView();
					
					this.application = application;

					this.initializeModelElement(application);
					
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
				CamelApplicationView.prototype.toString = function() {
					return "Lightdust.CamelApplicationView";
				};

				/**
				 * 
				 */
				CamelApplicationView.prototype.validate = function() {
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
				CamelApplicationView.prototype.processCommand = function(
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