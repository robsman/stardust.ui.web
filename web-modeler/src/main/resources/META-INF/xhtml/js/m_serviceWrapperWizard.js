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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_command", "bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_accessPoint",
				"bpm-modeler/js/m_dataTraversal", "bpm-modeler/js/m_dialog" ],
		function(m_utils, m_constants, m_command, m_commandsController,
				m_model, m_accessPoint, m_dataTraversal,
				m_dialog) {
			return {
				initialize : function() {
					var wizard = new ServiceWrapperWizard(
							payloadObj.createCallback);

					wizard.initialize(payloadObj.application);
				}
			};

			/**
			 * 
			 */
			function ServiceWrapperWizard(createCallback) {
				this.createCallback = createCallback;
				this.modelInput = jQuery("#modelInput");
				this.processDefinitionNameInput = jQuery("#processDefinitionNameInput");
				this.requestParameterDataNameInput = jQuery("#requestParameterDataNameInput");
				this.responseParameterDataNameInput = jQuery("#responseParameterDataNameInput");
				this.requestTransformationActivityNameInput = jQuery("#requestTransformationActivityNameInput");
				this.serviceInvocationActivityNameInput = jQuery("#serviceInvocationActivityNameInput");
				this.responseTransformationActivityNameInput = jQuery("#responseTransformationActivityNameInput");
				this.serviceRequestParameterTypeInput = jQuery("#serviceRequestParameterTypeInput");
				this.serviceResponseParameterTypeInput = jQuery("#serviceResponseParameterTypeInput");
				this.createButton = jQuery("#createButton");
				this.cancelButton = jQuery("#cancelButton");

				this.createButton.click({
					"wizard" : this
				}, function(event) {
					event.data.wizard.create();
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
				ServiceWrapperWizard.prototype.initialize = function(application) {
					this.application = application;

					this.modelInput.empty();

					var models = m_model.getModels();

					for ( var n in models) {
						this.modelInput.append("<option value='" + models[n].id
								+ "'>" + models[n].name + "</option>");
					}

					this.serviceRequestParameterTypeInput.empty();

					for ( var n in models) {
						for ( var m in models[n].structuredDataTypes) {
							this.serviceRequestParameterTypeInput
									.append("<option value='"
											+ models[n].structuredDataTypes[m]
													.getFullId()
											+ "'>"
											+ models[n].name
											+ "/"
											+ models[n].structuredDataTypes[m].name
											+ "</option>");
						}
					}

					this.serviceResponseParameterTypeInput.empty();

					for ( var n in models) {
						for ( var m in models[n].structuredDataTypes) {
							this.serviceResponseParameterTypeInput
									.append("<option value='"
											+ models[n].structuredDataTypes[m]
													.getFullId()
											+ "'>"
											+ models[n].name
											+ "/"
											+ models[n].structuredDataTypes[m].name
											+ "</option>");
						}
					}

					this.modelInput.val(this.application.model.id);
					this.processDefinitionNameInput.val(this.application.name);
					this.requestParameterDataNameInput.val(this.application.name
							+ " Request Parameter");
					this.requestTransformationActivityNameInput
							.val(this.application.name
									+ " Request Transformation");
					this.serviceInvocationActivityNameInput
							.val(this.application.name);
					this.responseTransformationActivityNameInput
							.val(this.application.name
									+ " Response Transformation");
					this.responseParameterDataNameInput
							.val(this.application.name + " Response Parameter");
				};

				/**
				 * 
				 */
				ServiceWrapperWizard.prototype.create = function() {
					this
							.createCallback({
								id : this.application.id,
								name : this.processDefinitionNameInput
										.val(),
								applicationId : this.application.id,
								requestParameterDataNameInput : this.requestParameterDataNameInput
										.val(),
								serviceRequestParameterTypeId : this.serviceRequestParameterTypeInput
										.val(),
								requestTransformationActivityName : this.requestTransformationActivityNameInput
										.val(),
								serviceInvocationActivityName : this.serviceInvocationActivityNameInput
										.val(),
								responseTransformationActivityName : this.responseTransformationActivityNameInput
										.val(),
								serviceResponseParameterTypeId : this.serviceResponseParameterTypeInput
										.val(),
								responseParameterDataNameInput : this.responseParameterDataNameInput
										.val()
							});
				};
			}
			;
		});