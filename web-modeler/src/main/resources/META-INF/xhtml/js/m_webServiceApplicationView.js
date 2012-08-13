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
		[ "m_utils", "m_communicationController", "m_command",
				"m_commandsController", "m_dialog", "m_modelElementView",
				"m_model", "m_typeDeclaration" ],
		function(m_utils, m_communicationController, m_command,
				m_commandsController, m_dialog, m_modelElementView, m_model,
				m_typeDeclaration) {
			return {
				initialize : function(fullId) {
					var view = new WebServiceApplicationView();
					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(m_model.findApplication(fullId));
				}
			};

			/**
			 * 
			 */
			function WebServiceApplicationView() {
				// Inheritance

				var view = m_modelElementView.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(WebServiceApplicationView.prototype,
						view);

				/**
				 * 
				 */
				WebServiceApplicationView.prototype.initialize = function(
						application) {
					this.initializeModelElementView();

					m_utils.debug("===> Application");
					m_utils.debug(application);

					this.application = application;

					this.initializeModelElement(application);

					this.webServiceStructure = {};
					this.wsdlUrlInput = jQuery("#wsdlUrlInput");
					this.browseButton = jQuery("#browseButton");
					this.serviceSelect = jQuery("#serviceSelect");

					this.browseButton.click({
						view : this
					}, function(event) {
						event.data.view.loadWebServiceStructure()
					});
					// carnot:engine:wsPortName: "MortgageIndexHttpGet"
					// carnot:engine:wsRuntime: "jaxws"
					// carnot:engine:wsSoapProtocol: "SOAP 1.1 Protocol"
					// carnot:engine:wsdlUrl:
					// "http://www.webservicex.net/MortgageIndex.asmx?WSDL"

					this.wsdlUrlInput
							.val(this.application.attributes["carnot:engine:wsServiceName"]);
				};

				/**
				 * 
				 */
				WebServiceApplicationView.prototype.toString = function() {
					return "Lightdust.WebServiceApplicationView";
				};

				/**
				 * 
				 */
				WebServiceApplicationView.prototype.validate = function() {
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
				WebServiceApplicationView.prototype.loadWebServiceStructure = function() {
					var successCallback = {
						callbackScope : this,
						callbackMethod : "setWebServiceStructure"
					};

					m_communicationController
							.syncPostData(
									{
										url : m_communicationController
												.getEndpointUrl()
												+ "/webServices/structure"
									},
									JSON.stringify({
										wsdlUrl : "bla"
									}),
									{
										"success" : function(serverData) {
											successCallback.callbackScope[successCallback.callbackMethod]
													(serverData);
										},
										"error" : function() {
											m_utils.debug("Error");
										}
									});
				};

				/**
				 * 
				 */
				WebServiceApplicationView.prototype.setWebServiceStructure = function(
						webServiceStructure) {
					m_utils.debug("===> Web Service Structure");
					m_utils.debug(webServiceStructure);

					this.serviceSelect.empty();

					for ( var m in webServiceStructure.services) {
						var service = webServiceStructure.services[m];

						this.serviceSelect.append("<option value=\""
								+ service.name + "\">" + service.name
								+ "</option>");
					}
				};

				/**
				 * 
				 */
				WebServiceApplicationView.prototype.processCommand = function(
						command) {
					// Parse the response JSON from command pattern

					var object = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != object
							&& null != object.changes
							&& null != object.changes.modified
							&& 0 != object.changes.modified.length
							&& object.changes.modified[0].oid == this.application.oid) {

						m_utils.inheritFields(this.application,
								object.changes.modified[0]);

						this.initialize(this.application);
					}
				};
			}
		});