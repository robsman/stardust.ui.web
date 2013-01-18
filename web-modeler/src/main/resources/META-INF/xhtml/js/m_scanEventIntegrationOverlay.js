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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_i18nUtils",
				"bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_accessPoint",
				"bpm-modeler/js/m_parameterDefinitionsPanel",
				"bpm-modeler/js/m_eventIntegrationOverlay" ],
		function(m_utils, m_i18nUtils, m_constants, m_commandsController,
				m_command, m_model, m_accessPoint, m_parameterDefinitionsPanel,
				m_eventIntegrationOverlay) {

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
				ScanEventIntegrationOverlay.prototype.submitOverlayChanges = function(
						parameterMappings) {
					if (parameterMappings == null) {
						parameterMappings = [];
					}

					this.submitChanges({
						modelElement : {
							parameterMappings : parameterMappings,
							attributes : {
								"carnot:engine:integration::overlay" : this.id
							}
						}
					});
				};

				/**
				 * 
				 */
				ScanEventIntegrationOverlay.prototype.populateDataItemsList = function() {
					this.documentDataList.empty();

					this.documentDataList
							.append("<option value=\"TO_BE_DEFINED\">"
									+ m_i18nUtils
											.getProperty("modeler.general.toBeDefined")
									+ "</option>");

					if (this.scopeModel) {
						this.documentDataList
								.append("<optgroup label=\""
										+ m_i18nUtils
												.getProperty("modeler.element.properties.commonProperties.thisModel")
										+ "\">");

						for ( var i in this.scopeModel.dataItems) {
							var dataItem = this.scopeModel.dataItems[i];
							if (dataItem.dataType === m_constants.DOCUMENT_DATA_TYPE) {
								this.documentDataList.append("<option value='"
										+ dataItem.getFullId() + "'>"
										+ dataItem.name + "</option>");
							}
						}
					}

					this.documentDataList
							.append("</optgroup><optgroup label=\""
									+ m_i18nUtils
											.getProperty("modeler.element.properties.commonProperties.otherModel")
									+ "\">");

					for ( var n in m_model.getModels()) {
						if (this.scopeModel
								&& m_model.getModels()[n] == this.scopeModel) {
							continue;
						}

						for ( var m in m_model.getModels()[n].dataItems) {
							var dataItem = m_model.getModels()[n].dataItems[m];

							if (dataItem.dataType === m_constants.DOCUMENT_DATA_TYPE) {
								this.documentDataList.append("<option value='"
										+ dataItem.getFullId() + "'>"
										+ m_model.getModels()[n].name + "/"
										+ dataItem.name + "</option>");
							}
						}
					}

					this.documentDataList.append("</optgroup>");
				};

				/**
				 * 
				 */
				ScanEventIntegrationOverlay.prototype.activate = function() {
					this.submitOverlayChanges();
				};

				/**
				 * 
				 */
				ScanEventIntegrationOverlay.prototype.update = function() {
					this.populateDataItemsList();

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