/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define(
		[ "m_utils", "m_constants", "m_commandsController", "m_command",
				"m_model", "m_accessPoint", "m_parameterDefinitionsPanel" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_model, m_accessPoint, m_parameterDefinitionsPanel) {

			return {
				create : function(page, id) {
					var overlay = new EmailEventIntegrationOverlay();

					overlay.initialize(page, id);

					return overlay;
				}
			};

			/**
			 * 
			 */
			function EmailEventIntegrationOverlay() {

				/**
				 * 
				 */
				EmailEventIntegrationOverlay.prototype.initialize = function(page,
						id) {
					this.page = page;
					this.id = id;
				};

				/**
				 * 
				 */
				EmailEventIntegrationOverlay.prototype.activate = function() {
				};

				/**
				 * 
				 */
				EmailEventIntegrationOverlay.prototype.update = function() {
				};
			}
		});