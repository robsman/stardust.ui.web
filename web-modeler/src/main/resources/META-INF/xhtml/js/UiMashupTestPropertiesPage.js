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
				"bpm-modeler/js/m_urlUtils", "bpm-modeler/js/m_command",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_dialog", "bpm-modeler/js/m_propertiesPage",
				"bpm-modeler/js/m_modelElementView", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_dataTypeSelector",
				"bpm-modeler/js/m_parameterDefinitionsPanel",
				"bpm-modeler/js/m_codeEditorAce", "bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_urlUtils, m_command,
				m_commandsController, m_dialog, m_propertiesPage,
				m_modelElementView, m_model, m_dataTypeSelector,
				m_parameterDefinitionsPanel, m_codeEditorAce, m_i18nUtils) {
			return {
				create : function(propertiesPanel, id) {
					var page = new UiMashupTestPropertiesPage(propertiesPanel,
							id);

					page.initialize();

					return page;
				}
			};

			/**
			 * 
			 */
			function UiMashupTestPropertiesPage(propertiesPanel, id) {
				var commentsText = m_i18nUtils
						.getProperty("modeler.element.properties.commonProperties.comments");
				var propertiesPage = m_propertiesPage.createPropertiesPage(
						propertiesPanel, id, commentsText,
						"../../images/icons/comments.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(UiMashupTestPropertiesPage.prototype,
						propertiesPage);

				/**
				 * 
				 */
				UiMashupTestPropertiesPage.prototype.initialize = function() {
					this.applicationFrame = jQuery("#applicationFrame");
					this.resetButton = jQuery("#resetButton");
					this.runButton = jQuery("#runButton");
					this.retrieveButton = jQuery("#retrieveButton");

					this.resetButton
							.prop(
									"title",
									m_i18nUtils
											.getProperty("modeler.model.propertyView.uiMashup.test.resetButton.title"));
					this.runButton
							.prop(
									"title",
									m_i18nUtils
											.getProperty("modeler.model.propertyView.uiMashup.test.runButton.title"));
					this.retrieveButton
							.prop(
									"title",
									m_i18nUtils
											.getProperty("modeler.model.propertyView.uiMashup.test.retrieveButton.title"));
					jQuery("label[for='inputDataTextArea']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.propertyView.uiMashup.test.inputDataTextArea.label"));
					jQuery("label[for='outputDataTextarea']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.propertyView.uiMashup.test.outputDataTextArea.label"));
					this.applicationFrame
					.attr(
							"src",
							"./emptyScreen.html");

					this.runButton
							.click(
									{
										view : this
									},
									function(event) {
										var view = event.data.view;

										var inputDataTextarea = jQuery("#inputDataTextarea");
										var outputDataTable = jQuery("#outputDataTable");

										outputDataTable.empty();

										// Send input data

										jQuery
												.ajax(
														{
															type : "POST",
															url : m_urlUtils
																	.getModelerEndpointUrl()
																	+ "/interactions/4711/inData",
															contentType : "application/json",
															data : inputDataTextarea
																	.val()
														})
												.done(
														function() {
															// Refresh external
															// UI

															if (view.propertiesPanel
																	.isEmbeddedConfiguration()) {
																var url = m_urlUtils
																		.getModelerEndpointUrl()
																		+ "/models/"
																		+ view
																				.getModel().id
																		+ "/embeddedWebApplication/"
																		+ view
																				.getApplication().id
																		+ "?ippPortalBaseUri="
																		+ m_urlUtils
																				.getModelerEndpointUrl()
																		+ "/interactions/4711";

																view.applicationFrame
																		.attr(
																				"src",
																				url);
															} else {
																if (view.propertiesPanel.urlInput
																		.val()) {
																	view.applicationFrame
																			.attr(
																					"src",
																					view.propertiesPanel.urlInput
																							.val()
																							+ "?ippPortalBaseUri="
																							+ m_urlUtils
																									.getModelerEndpointUrl()
																							+ "/interactions/4711");
																} else {
																	view.applicationFrame
																	.attr(
																			"src",
																			"./emptyScreen.html");
																}
															}
														}).fail(function() {
															view.applicationFrame
															.attr(
																	"src",
																	"./emptyScreen.html");
												});
									});
					this.resetButton
							.click(
									{
										view : this
									},
									function(event) {
										var view = event.data.view;
										var inputDataTextarea = jQuery("#inputDataTextarea");
										var outputDataTextarea = jQuery("#outputDataTextarea");

										inputDataTextarea.empty();
										outputDataTextarea.empty();

										var inputData = "{";

										for ( var n = 0; n < view
												.getApplication().contexts["externalWebApp"].accessPoints.length; ++n) {
											var parameterDefinition = view
													.getApplication().contexts["externalWebApp"].accessPoints[n];

											if (parameterDefinition.direction == m_constants.OUT_ACCESS_POINT) {
												continue;
											}

											if (n > 0) {
												inputData += ",";
											}

											if (parameterDefinition.dataType == "struct") {
												var typeDeclaration = m_model
														.findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);

												inputData += parameterDefinition.id;
												inputData += ": ";
												inputData += JSON
														.stringify(
																typeDeclaration
																		.createInstance(),
																null, 3);
											} 
											else if (parameterDefinition.dataType == "dmsDocument") {
												var typeDeclaration = m_model
														.findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);

												inputData += parameterDefinition.id;
												inputData += ": ";
												inputData += JSON
														.stringify(
																typeDeclaration
																		.createInstance(),
																null, 3);
											} 
											else {
												// Deal with primitives 
											}
										}

										inputData += "}";

										inputDataTextarea.append(inputData);
									});
					this.retrieveButton.click({
						view : this
					}, function(event) {
						var view = event.data.view;

						var outputDataTextarea = jQuery("#outputDataTextarea");

						jQuery.ajax(
								{
									type : "GET",
									url : m_urlUtils.getModelerEndpointUrl()
											+ "/interactions/4711/outData",
									contentType : "application/json"
								}).done(function(data) {
							outputDataTextarea.val(JSON.stringify(data));
						}).fail(function() {
						});
					});
				};

				/**
				 * 
				 */
				UiMashupTestPropertiesPage.prototype.getApplication = function() {
					return this.propertiesPanel.getModelElement();
				};

				/**
				 * 
				 */
				UiMashupTestPropertiesPage.prototype.validate = function() {
					this.propertiesPanel.clearErrorMessages();

					return true;
				};
			}
		});