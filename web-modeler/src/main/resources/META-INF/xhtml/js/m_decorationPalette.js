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
		[ "m_utils", "m_constants", "m_extensionManager",
				"m_communicationController", "m_commandsController",
				"m_command" ],
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
				/**
				 * 
				 */
				DecorationPalette.prototype.initialize = function(diagram) {
					var dialog = jQuery("#decorationConfigurationDialog")
							.dialog({
								autoOpen : false,
								draggable : false
							});

					jQuery("#decorationConfigurationDialog #closeButton")
							.click(
									function() {
										jQuery("#decorationConfigurationDialog")
												.dialog("close");
									});

					jQuery("#decorationConfigurationDialog #applyButton")
							.click(
									function() {
										m_commandsController
												.submitImmediately(
														m_command
																.createRetrieveCommand(
																		"/models/"
																				+ this.modelId
																				+ "/processes/"
																				+ this.processId
																				+ "/decorations/"
																				+ decorationId,
																		{
																		// Parameters
																		// from
																		// dialog
																		}),
														{
															"callbackScope" : diagram,
															"method" : "applyDecoration"
														});
										jQuery("#decorationConfigurationDialog")
												.dialog("close");
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

						jQuery("#decorationConfigurationDialog")
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
							.change(function() {
								decorationId = decorationList.val();
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
											+ diagram.modelId
											+ "/processes/"
											+ diagram.processId
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