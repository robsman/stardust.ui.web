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
					var dialog = jQuery("#decorationConfigurationDialog")
							.dialog({
								autoOpen : false,
								draggable : false
							});

					// Decoration list

					var decorationList = jQuery("#decorationList");
					var decorationExtensions = m_extensionManager
							.findExtensions("modelDecoration");

					for ( var n = 0; n < decorationExtensions.length; ++n) {
						var decorationExtension = decorationExtensions[n];

						decorationList.append("<option value='"
								+ decorationExtension.id + "'>"
								+ decorationExtension.title + "</option>");

						var contentDiv = jQuery("<div></div>");

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

												jQuery(
														"#decorationConfigurationDialog")
														.append(msg);
											} else {
												// extension.provider.create();
											}
										});
					}

					decorationList
							.change(
									{
										palette : this
									},
									function(event) {
										decorationId = decorationList.val();
										jQuery(
												"#decorationConfigurationDialog #contentAnchor")
												.empty();
										jQuery(
												"#decorationConfigurationDialog #contentAnchor")
												.append(
														event.data.palette.dialogContent[decorationId]);

										// TODO Following is nonsense; assuming
										// that close/apply buttons are
										// available. Use provider object in the
										// future

										jQuery(
												"#decorationConfigurationDialog #closeButton")
												.click(
														function() {
															m_utils
																	.debug("Close dialog");
															jQuery(
																	"#decorationConfigurationDialog")
																	.dialog(
																			"close");
														});

										jQuery(
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
															jQuery(
																	"#decorationConfigurationDialog")
																	.dialog(
																			"close");
														});

										jQuery("#decorationConfigurationDialog")
												.dialog('open');
									});

					jQuery("#decorationConfigurationButton").click(
							function() {
								jQuery("#decorationConfigurationDialog")
										.dialog('open');
							});

					jQuery("#decorationRefreshButton").click(
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