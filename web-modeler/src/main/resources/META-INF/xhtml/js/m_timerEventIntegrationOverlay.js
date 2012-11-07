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
		TimerEventIntegrationOverlay.prototype.activate = function() {
			this.submitEventClassChanges();
		};

		/**
		 * 
		 */
		TimerEventIntegrationOverlay.prototype.update = function() {
			this.submitEventClassChanges();
		};
	}
});