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
				"bpm-modeler/js/m_extensionManager",
				"bpm-modeler/js/m_communicationController",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command" ],
		function(m_utils, m_constants, m_extensionManager,
				m_communicationController, m_commandsController, m_command) {
			return {
				create : function(diagram) {
					var palette = new DecorationPalette();

					palette.initialize(diagram);

					if (diagram.process.isReadonly()) {
						m_utils.markControlsReadonly();
					}

					return palette;
				}
			};

			/**
			 *
			 */
			function DecorationPalette() {
				this.dialogContent = {};
				/**
				 *
				 */
				DecorationPalette.prototype.initialize = function(diagram) {
					var dialog = m_utils.jQuerySelect("#decorationConfigurationDialog")
							.dialog({
								autoOpen : false,
								draggable : false
							});

					// Decoration list

					var decorationList = m_utils.jQuerySelect("#decorationList");
					var decorationExtensions = m_extensionManager
							.findExtensions("modelDecoration");

					for ( var n = 0; n < decorationExtensions.length; ++n) {
						var decorationExtension = decorationExtensions[n];

						decorationList.append("<option value='"
								+ decorationExtension.id + "'>"
								+ decorationExtension.title + "</option>");

						var contentDiv = m_utils.jQuerySelect("<div></div>");

						this.dialogContent[decorationExtension.id] = contentDiv;

						contentDiv
								.load(
										decorationExtension.dialogHtmlUrl,
										function(response, status, xhr) {
											if (status == "error") {
												var msg = "DecorationDialog Load Error: "
														+ xhr.status
														+ " "
														+ xhr.statusText;

												m_utils.jQuerySelect(
														"#decorationConfigurationDialog")
														.append(msg);
											} else {
												// extension.provider.create();
											}
										});
					}

					// Adjust layout of the diagram area to accommodate the additional toolbar items.
					require("bpm-modeler/js/m_modelerViewLayoutManager").adjustPanels();
					
					decorationList
							.change(
									{
										palette : this
									},
									function(event) {
										decorationId = decorationList.val();
										m_utils.jQuerySelect(
												"#decorationConfigurationDialog #contentAnchor")
												.empty();
										m_utils.jQuerySelect(
												"#decorationConfigurationDialog #contentAnchor")
												.append(
														event.data.palette.dialogContent[decorationId]);

										// TODO Following is nonsense; assuming
										// that close/apply buttons are
										// available. Use provider object in the
										// future

										m_utils.jQuerySelect(
												"#decorationConfigurationDialog #closeButton")
												.click(
														function() {
															m_utils
																	.debug("Close dialog");
															m_utils.jQuerySelect(
																	"#decorationConfigurationDialog")
																	.dialog(
																			"close");
														});

										m_utils.jQuerySelect(
												"#decorationConfigurationDialog #applyButton")
												.click(
														function() {
															m_utils
																	.debug("Apply Close dialog");
															m_communicationController
																	.postData(
																			{
																				url : m_communicationController
																						.getEndpointUrl()
																						+ "/models/"
																						+ encodeURIComponent(diagram.modelId)
																						+ "/processes/"
																						+ encodeURIComponent(diagram.processId)
																						+ "/decorations/"
																						+ decorationId
																			},
																			{},
																			{
																				"success" : function(
																						json) {
																					diagram
																							.applyDecoration(json);
																				},
																				"error" : function() {
																					alert('Could not retrieve decoration');
																				}
																			});
															m_utils.jQuerySelect(
																	"#decorationConfigurationDialog")
																	.dialog(
																			"close");
														});

										m_utils.jQuerySelect("#decorationConfigurationDialog")
												.dialog('open');
									});

					m_utils.jQuerySelect("#decorationConfigurationButton").click(
							function() {
								m_utils.jQuerySelect("#decorationConfigurationDialog")
										.dialog('open');
							});

					m_utils.jQuerySelect("#decorationRefreshButton").click(
							function() {
								m_communicationController.postData({
									url : m_communicationController
											.getEndpointUrl()
											+ "/models/"
											+ encodeURIComponent(diagram.modelId)
											+ "/processes/"
											+ encodeURIComponent(diagram.processId)
											+ "/decorations/"
											+ decorationList.val()
								}, {}, {
									"success" : function(json) {
										diagram.applyDecoration(json);
									},
									"error" : function() {
										alert('Could not retrieve decoration');
									}
								});
							});
				};
			}
		});