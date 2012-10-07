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
		[ "m_utils", "m_constants", "m_commandsController", "m_dialog", "m_modelElementView",
				"m_model"],
		function(m_utils, m_constants, m_commandsController, m_dialog, m_modelElementView, m_model) {
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
				var view = m_modelElementView.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(CamelApplicationView.prototype, view);

				/**
				 *
				 */
				CamelApplicationView.prototype.initialize = function(
						application) {
					this.id = "camelApplicationView";

					this.camelContextInput = jQuery("#camelContextInput");
					this.routeTextarea = jQuery("#routeTextarea");
					this.additionalBeanSpecificationTextarea = jQuery("#additionalBeanSpecificationTextarea");
					this.requestDataInput = jQuery("#requestDataInput");
					this.responseDataInput = jQuery("#responseDataInput");

					this.registerInputForModelElementAttributeChangeSubmission(
							this.camelContextInput, "carnot:engine:camel::camelContextId");
					this.registerInputForModelElementAttributeChangeSubmission(
							this.routeTextarea, "carnot:engine:camel::routeEntries");
					this.registerInputForModelElementAttributeChangeSubmission(
							this.additionalBeanSpecificationTextarea, "carnot:engine:camel::additionalSpringBeanDefinitions");

					this.initializeModelElementView(application);
				};

				/**
				 *
				 */
				CamelApplicationView.prototype.setModelElement = function(
						application) {
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
			}
		});