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
		[ "m_utils", "m_constants", "m_commandsController", "m_command" ],
		function(m_utils, m_constants, m_commandsController, m_command) {
			return {
				create : function(propertiesPanel) {
					var palette = new DecorationPalette();

					palette.initialize();

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
				DecorationPalette.prototype.initialize = function() {
					m_utils.debug("Palette initialization");

//					var dialog = jQuery("#dialog").dialog({
//						autoOpen : false,
//						draggable : false
//					});
//
//					var dialogCloseButton = jQuery("#dialog #closeButton");
//
//					dialogCloseButton.click(function() {
//						dialog.dialog("close");
//					});
//
//					var dialogApplyButton = jQuery("#dialog #applyButton");
//					var dummy = this;
//					dialogApplyButton.click(function() {
//						m_commandsController.submitImmediately(m_command
//								.createRetrieveCommand("/models/"
//										+ this.modelId + "/processes/"
//										+ this.processId + "/decorations/"
//										+ decorationId, {
//								// Parameters from dialog
//								}), {
//							"callbackScope" : dummy,
//							"method" : "applyDecoration"
//						});
//						dialog.dialog("close");
//					});

					// Decoration list

					var decorationList = jQuery("#decorationList");

					decorationList.change(function() {
						decorationId = decorationList.val();
//						dialog.dialog('open');
					});

//					var decorationConfigurationButton = jQuery("#decorationConfigurationButton");
//
//					decorationConfigurationButton.click(function() {
//						dialog.dialog('open');
//					});
//
				};
			}
		});