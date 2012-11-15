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
				"m_model", "m_accessPoint", "m_parameterDefinitionsPanel", "m_eventIntegrationOverlay" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_model, m_accessPoint, m_parameterDefinitionsPanel, m_eventIntegrationOverlay) {

			return {
				create : function(page, id) {
					var overlay = new ScanEventIntegrationOverlay();

					overlay.initialize(page, id);

					return overlay;
				}
			};

			/**
			 * 
			 */
			function ScanEventIntegrationOverlay() {
				var eventIntegrationOverlay = m_eventIntegrationOverlay
						.create();

				m_utils.inheritFields(this, eventIntegrationOverlay);
				m_utils.inheritMethods(ScanEventIntegrationOverlay.prototype,
						eventIntegrationOverlay);

				/**
				 * 
				 */
				ScanEventIntegrationOverlay.prototype.initialize = function(
						page, id) {
					this.initializeEventIntegrationOverlay(page, id);

					this.documentDataList = this.mapInputId("documentDataList");
				};

				/**
				 * 
				 */
				ScanEventIntegrationOverlay.prototype.submitEventClassChanges = function(
						parameterMappings) {
					if (parameterMappings == null) {
						parameterMappings = [];
					}

					this.submitChanges({
						modelElement : {
							eventClass : this.id,
							parameterMappings : parameterMappings
						}
					});
				};

				/**
				 * 
				 */
				ScanEventIntegrationOverlay.prototype.activate = function() {
					this.submitEventClassChanges();
				};

				/**
				 * 
				 */
				ScanEventIntegrationOverlay.prototype.update = function() {
					if (this.page.propertiesPanel.element.modelElement.documentDataId != null) {
						this.documentDataList
								.val(this.page.propertiesPanel.element.modelElement.documentDataId);
					} else {
						this.documentDataList.val(m_constants.TO_BE_DEFINED);
					}
				};

				/**
				 * 
				 */
				ScanEventIntegrationOverlay.prototype.validate = function() {
					return true;
				};
			}
		});