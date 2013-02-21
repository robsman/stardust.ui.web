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
				"bpm-modeler/js/m_i18nUtils", "bpm-modeler/js/m_command",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/ChangeSynchronization",
				"bpm-modeler/js/EventSynchronization",
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_modelElementUtils",
				"bpm-modeler/js/m_process", "bpm-modeler/js/m_accessPoint",
				"bpm-modeler/js/m_dataTraversal", "bpm-modeler/js/m_dialog",
				"bpm-modeler/js/m_activitySymbol" ],
		function(m_utils, m_constants, m_urlUtils, m_i18nUtils, m_command,
				m_commandsController, ChangeSynchronization,
				EventSynchronization, m_model, m_modelElementUtils, m_process,
				m_accessPoint, m_dataTraversal, m_dialog, m_activitySymbol) {
			return {
				initialize : function() {
					var wizard = new ProcessInterfaceTestWrapper();

					wizard.initialize(payloadObj.callerWindow,
							payloadObj.process, payloadObj.viewManager);
				}
			};

			/**
			 * 
			 */
			function ProcessInterfaceTestWrapper() {
				this.introLabel = jQuery("#introLabel");
				this.modelInput = jQuery("#modelInput");
				this.processDefinitionNameInput = jQuery("#processDefinitionNameInput");
				this.participantSelect = jQuery("#participantSelect");
				this.dataInputActivityNameInput = jQuery("#dataInputActivityNameInput");
				this.subprocessActivityNameInput = jQuery("#subprocessActivityNameInput");
				this.dataOutputActivityNameInput = jQuery("#dataOutputActivityNameInput");
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
				ProcessInterfaceTestWrapper.prototype.initialize = function(
						callerWindow, process, viewManager) {
					this.callerWindow = callerWindow;
					this.process = process;
					this.viewManager = viewManager;

					this.introLabel.empty();
					this.introLabel
							.append("Create a Wrapper Process Definition to test Process Interface <b>"
									+ this.process.name
									+ "</b> with Interactive Activities to enter Input and Output Data:"); // TODO
																											// I18N

					this.modelInput.empty();

					var models = m_model.getModels();

					for ( var n in models) {
						this.modelInput.append("<option value='" + models[n].id
								+ "'>" + models[n].name + "</option>");
					}

					this.modelInput.val(this.process.model.id);

					this.participantSelect.empty();
					this.participantSelect
							.append("<option value='NONE'>(None)</option>");
					this.participantSelect
							.append("<optgroup label=\""
									+ m_i18nUtils
											.getProperty("modeler.element.properties.commonProperties.thisModel")
									+ "\">");

					// Insures that the Administrator role is preselected

					var selected = "selected";

					for ( var i in this.process.model.participants) {
						// Show only participants from this model and not
						// external references.
						if (!this.process.model.participants[i].externalReference) {
							this.participantSelect.append("<option "
									+ selected
									+ " value='"
									+ this.process.model.participants[i]
											.getFullId() + "'>"
									+ this.process.model.participants[i].name
									+ "</option>");

							selected = "";
						}
					}

					this.participantSelect
							.append("</optgroup><optgroup label=\""
									+ m_i18nUtils
											.getProperty("modeler.element.properties.commonProperties.otherModel")
									+ "\">");

					for ( var n in m_model.getModels()) {
						if (m_model.getModels()[n] == this.process.model) {
							continue;
						}

						for ( var m in m_model.getModels()[n].participants) {
							if (m_modelElementUtils.hasPublicVisibility(m_model
									.getModels()[n].participants[m])
									&& !m_model.getModels()[n].participants[m].externalReference
									&& !(m_constants.ADMIN_ROLE_ID === m_model
											.getModels()[n].participants[m].id)) {
								this.participantSelect
										.append("<option value='"
												+ m_model.getModels()[n].participants[m]
														.getFullId()
												+ "'>"
												+ m_model.getModels()[n].name
												+ "/"
												+ m_model.getModels()[n].participants[m].name
												+ "</option>");
							}
						}
					}

					this.participantSelect.append("</optgroup>");

					this.processDefinitionNameInput.val(this.process.name
							+ " Test"); // TODO I18N
					this.dataInputActivityNameInput.val("Enter Data"); // TODO
																		// I18N
					this.subprocessActivityNameInput.val(this.process.name);
					this.dataOutputActivityNameInput.val("Retrieve Data"); // TODO
																			// I18N
				};

				/**
				 * 
				 */
				ProcessInterfaceTestWrapper.prototype.createViaCallback = function() {
					var parameter = {
						processDefinitionName : this.processDefinitionNameInput.val(),
						participantFullId : this.participantSelect.val(),
						processFullId : this.process.getFullId(),
						dataInputActivityName : this.dataInputActivityNameInput
								.val(),
						subprocessActivityName : this.subprocessActivityNameInput.val(),		
						dataOutputActivityName : this.dataOutputActivityNameInput
								.val()
					};

					jQuery
							.ajax(
									{
										type : "POST",
										url : m_urlUtils
												.getModelerEndpointUrl()
												+ "/models/"
												+ encodeURIComponent(this.process.model.id)
												+ "/processes/createProcessInterfaceTestWrapperProcess",
										contentType : "application/json",
										data : JSON.stringify(parameter)
									}).done().fail();
				};
			}
		});