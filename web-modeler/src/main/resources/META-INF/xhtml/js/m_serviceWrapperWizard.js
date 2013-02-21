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
			"bpm-modeler/js/m_urlUtils",
				"bpm-modeler/js/m_command",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/ChangeSynchronization",
				"bpm-modeler/js/EventSynchronization",
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_process",
				"bpm-modeler/js/m_accessPoint",
				"bpm-modeler/js/m_dataTraversal", "bpm-modeler/js/m_dialog",
				"bpm-modeler/js/m_activitySymbol" ],
		function(m_utils, m_constants, m_urlUtils, m_command, m_commandsController,
				ChangeSynchronization, EventSynchronization, m_model,
				m_process, m_accessPoint, m_dataTraversal, m_dialog,
				m_activitySymbol) {
			return {
				initialize : function() {
					var wizard = new ServiceWrapperWizard();

					wizard.initialize(payloadObj.callerWindow, payloadObj.application,
							payloadObj.viewManager);
				}
			};

			/**
			 * 
			 */
			function ServiceWrapperWizard() {
				this.modelInput = jQuery("#modelInput");
				this.processDefinitionNameInput = jQuery("#processDefinitionNameInput");
				this.requestDataTypeInput = jQuery("#requestDataTypeInput");
				this.requestDataNameInput = jQuery("#requestDataNameInput");
				this.responseDataTypeInput = jQuery("#responseDataTypeInput");
				this.responseDataNameInput = jQuery("#responseDataNameInput");
				this.serviceInvocationActivityNameInput = jQuery("#serviceInvocationActivityNameInput");
				this.createButton = jQuery("#createButton");
				this.cancelButton = jQuery("#cancelButton");

				var self = this;
				
				this.createButton.click({
					"wizard" : this
				}, function(event) {
					event.data.wizard.createViaCallback();
					closePopup();
				});

				this.cancelButton.click({
					"wizard" : this
				}, function(event) {
					closePopup();
				});

				/**
				 * 
				 */
				ServiceWrapperWizard.prototype.initialize = function(callerWindow,
						application, viewManager) {
					this.callerWindow = callerWindow;

					this.application = application;
					this.viewManager = viewManager;

					this.modelInput.empty();

					var models = m_model.getModels();

					for ( var n in models) {
						this.modelInput.append("<option value='" + models[n].id
								+ "'>" + models[n].name + "</option>");
					}

					this.requestDataTypeInput.empty();
					this.responseDataTypeInput.empty();

					for ( var n in this.application.contexts.application.accessPoints) {
						var accessPoint = this.application.contexts.application.accessPoints[n];

						if (accessPoint.direction === m_constants.IN_ACCESS_POINT) {
							this.requestDataTypeInput.append("<option value='"
									+ accessPoint.structuredDataTypeFullId
									+ "'>"
									+ accessPoint.structuredDataTypeFullId
									+ "</option>");
						} else {
							this.responseDataTypeInput.append("<option value='"
									+ accessPoint.structuredDataTypeFullId
									+ "'>"
									+ accessPoint.structuredDataTypeFullId
									+ "</option>");
						}
					}

					this.modelInput.val(this.application.model.id);
					this.processDefinitionNameInput.val(this.application.name);
					this.requestDataNameInput.val(this.application.name
							+ " Request Data");
					this.serviceInvocationActivityNameInput
							.val(this.application.name);
					this.responseDataNameInput.val(this.application.name
							+ " Response Data");
				};

				/**
				 * 
				 */
				ServiceWrapperWizard.prototype.createViaCallback = function() {
					var parameter = {
								processDefinitionName : this.processDefinitionNameInput
										.val(),
								requestDataTypeFullId : this.requestDataTypeInput
										.val(),
								requestDataName : this.requestDataNameInput
										.val(),
								responseDataTypeFullId : this.responseDataTypeInput
										.val(),
								responseDataName : this.responseDataNameInput
										.val(),
								serviceInvocationActivityName : this.serviceInvocationActivityNameInput
										.val(),
								applicationFullId : this.application.getFullId()
							};
					
					jQuery
					.ajax({
						type : "POST",
						url : m_urlUtils
								.getModelerEndpointUrl()
								+ "/models/"
								+ encodeURIComponent(this.application.model.id)
								+ "/processes/createWrapperProcess",
						contentType : "application/json",
						data : JSON.stringify(parameter)
					}).done().fail();
				};

				/**
				 * Experiment for local create
				 */
				ServiceWrapperWizard.prototype.create = function() {
					var self = this;
					var model = m_model.findModel(this.modelInput.val());
					var process;
					var activitySymbol;

					m_process
							.createSynchronized(model,
									self.processDefinitionNameInput.val(),
									"Default", "Default")
							.done(
									function(process) {
										// TODO Should have a clear point to
										// define a View Manager > JIRA

										// TODO Open View functionality should
										// be added to the views > JIRA

										EventSynchronization
												.create(
														"VIEW_LOADED",
														"",
														function() {
															self.viewManager
																	.openView(
																			"processDefinitionView",
																			"processId="
																					+ encodeURIComponent(process.id)
																					+ "&modelId="
																					+ encodeURIComponent(process.model.id)
																					+ "&processName="
																					+ encodeURIComponent(process.name)
																					+ "&fullId="
																					+ encodeURIComponent(process
																							.getFullId())
																					+ "&uuid="
																					+ process.uuid
																					+ "&modelUUID="
																					+ model.uuid,
																			process.uuid);
														}, self.callerWindow)
												.done(
														function() {
															try
															{
																self.callerWindow.alert("Diagram loaded");

															process.diagram.clearCurrentToolSelection();
															process.diagram.mode = process.diagram.CREATE_MODE;
															
															var symbol = m_activitySymbol
															.createActivitySymbolFromApplication(
																	process.diagram,
																	self.application);
															
															self.callerWindow.alert("Symbol created " + symbol);
															process.diagram.newSymbol = symbol;
															//process.diagram.placeNewSymbol(100, 100, true);
															self.callerWindow.alert("Activity symbol created");
															}
															catch (x)
															{
																self.callerWindow.alert(x);																															
															}
														}).fail();
									}).fail();
				};
			}
		});