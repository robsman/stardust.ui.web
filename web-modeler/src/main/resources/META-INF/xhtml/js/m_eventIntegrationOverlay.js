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
				create : function() {
					var overlay = new EventIntegrationOverlay();

					return overlay;
				}
			};

			/**
			 * 
			 */
			function EventIntegrationOverlay() {
				/**
				 * 
				 */
				EventIntegrationOverlay.prototype.initializeEventIntegrationOverlay = function(
						page, id) {
					this.page = page;
					this.id = id;
				};

				/**
				 * 
				 */
				EventIntegrationOverlay.prototype.mapInputId = function(inputId) {
					return jQuery("#" + this.id + " #" + inputId);
				};

				/**
				 * 
				 */
				EventIntegrationOverlay.prototype.registerForRouteChanges = function(
						input) {
					input.change({
						overlay : this
					}, function(event) {
						event.data.overlay.submitRouteChanges();
					});

				};

				/**
				 * 
				 */
				EventIntegrationOverlay.prototype.submitChanges = function(
						changes) {
					this.page.submitChanges(changes);
				};

				/**
				 * 
				 */
				EventIntegrationOverlay.prototype.submitEventClassChanges = function() {
					this.submitChanges({
						modelElement : {
							eventClass : this.id
						}
					});
				};

				/**
				 * 
				 */
				EventIntegrationOverlay.prototype.submitRouteChanges = function() {
					var route = "<route>";

					route += "<from uri=\"";
					route += this.getEndpointUri();
					route += "\"/></route>";

					m_utils.debug("Submitting Route: " + route);

					this.submitChanges({
						modelElement : {
							attributes : {
								"carnot:engine:camel::camelRouteExt" : route
							}
						}
					});
				};
			}
		});