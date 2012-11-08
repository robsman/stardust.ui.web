/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ "m_utils", "m_constants", "m_commandsController", "m_command",
		"m_model", "m_accessPoint", "m_parameterDefinitionsPanel", "m_eventIntegrationOverlay"], function(
		m_utils, m_constants, m_commandsController, m_command, m_model,
		m_accessPoint, m_parameterDefinitionsPanel, m_eventIntegrationOverlay) {

	return {
		create : function(page, id) {
			var overlay = new TimerEventIntegrationOverlay();

			overlay.initialize(page, id);

			return overlay;
		}
	};

	/**
	 * 
	 */
	function TimerEventIntegrationOverlay() {
		var eventIntegrationOverlay = m_eventIntegrationOverlay.create();

		m_utils.inheritFields(this, eventIntegrationOverlay);
		m_utils.inheritMethods(TimerEventIntegrationOverlay.prototype,
				eventIntegrationOverlay);

		/**
		 * 
		 */
		TimerEventIntegrationOverlay.prototype.initialize = function(page, id) {
			this.page = page;
			this.id = id;
		};

		/**
		 * 
		 */
		FileEventIntegrationOverlay.prototype.getEndpointUri = function() {
			var uri = "timer://";

			return uri;
		};

		/**
		 * 
		 */
		TimerEventIntegrationOverlay.prototype.activate = function() {
			this.submitEventClassChanges();
		};

		/**
		 * 
		 */
		TimerEventIntegrationOverlay.prototype.update = function() {
			this.submitEventClassChanges();
			
			var route = this.page.propertiesPanel.element.modelElement.attributes["carnot:engine:camel::camelRouteExt"];

			if (route == null) {
				return;
			}

			var xmlDoc = jQuery.parseXML(route);
			var xmlObject = jQuery(xmlDoc);
			var from = jQuery(xmlObject).find("from");
			var uri = from.attr("uri");
			var uri = uri.split("//");

			if (uri[1] != null) {
				uri = uri[1].split("?");
				this.fileOrDirectoryNameInput.val(uri[0]);

				if (uri[1] != null) {
					var options = uri[1].split("&");

					for ( var n = 0; n < options.length; ++n) {
						var option = options[n];

						option = option.split("=");

						var name = option[0];
						var value = option[1];

						if (name == "") {
						} 
					}
				}
			}
		};
	}
});