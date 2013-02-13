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
					this.metadataStructureLabel = this
							.mapInputId("metadataStructureLabel");

					this.documentDataList.change({
						overlay : this
					}, function(event) {
						var overlay = event.data.overlay;

						overlay.submitOverlayChanges();
					});
				};

				/**
				 *
				 */
				ScanEventIntegrationOverlay.prototype.getImplementation = function() {
					return "scan";
				};

				/**
				 *
				 */
				ScanEventIntegrationOverlay.prototype.setDocumentData = function() {
					this.metadataStructureLabel.empty();

					if (this.page.getModelElement().parameterMappings != null
							&& this.page.getModelElement().parameterMappings[0]
							&& this.page.getModelElement().parameterMappings[0].dataFullId
							&& this.page.getModelElement().parameterMappings[0].dataFullId != m_constants.TO_BE_DEFINED) {
						this.documentDataList
								.val(this.page.getModelElement().parameterMappings[0].dataFullId);

						var data = m_model
								.findData(this.documentDataList.val());
						if (data.structuredDataTypeFullId) {
							var structuredDataType = m_model
									.findTypeDeclaration(data.structuredDataTypeFullId);
							var model = m_model.findModel(m_model
									.stripModelId(structuredDataType
											.getFullId()));

							if (model.id == this.scopeModel.id) {
								this.metadataStructureLabel
										.append(structuredDataType.name);
							} else {
								this.metadataStructureLabel.append(model.name
										+ "/" + structuredDataType.name);
							}
						} else {
							this.metadataStructureLabel
									.append("<span style='color: grey;'><i>"
											+ m_i18nUtils
													.getProperty("modeler.general.defaultLiteral")
											+ "</i></span>");
						}
					} else {
						this.documentDataList.val(m_constants.TO_BE_DEFINED);
					}
				};

				/**
				 *
				 */
				ScanEventIntegrationOverlay.prototype.submitOverlayChanges = function() {
					if (this.documentDataList.val() != null
							&& this.documentDataList.val() != m_constants.TO_BE_DEFINED) {
						var data = m_model
								.findData(this.documentDataList.val());
						var participantFullId = this.page.getElement().parentSymbol.participantFullId;
						var modelId = null;
						var participantId = null;
						var participantAttribute = null;

						if (participantFullId) {
							modelId = m_model.stripModelId(participantFullId);
							participantId = m_model
									.stripElementId(participantFullId);

							if (modelId == this.scopeModel.id) {
								participantAttribute = participantId;
							} else {
								participantAttribute = "{" + modelId + "}"
										+ participantId;
							}
						}

						this
								.submitChanges({
									modelElement : {
										parameterMappings : [ {
											id : data.id,
											name : data.name,
											direction : m_constants.OUT_ACCESS_POINT,
											dataType : "dmsDocument",
											dataFullId : this.documentDataList
													.val()
										} ],
										implementation : this
												.getImplementation(),
										attributes : {
											"carnot:engine:integration::overlay" : this.id,
											"carnot:engine:participant" : participantAttribute
										}
									}
								});
					} else {
						this
								.submitChanges({
									modelElement : {
										parameterMappings : [],
										implementation : this
												.getImplementation(),
										attributes : {
											"carnot:engine:integration::overlay" : this.id,
											"carnot:engine:participant" : participantAttribute
										}
									}
								});
					}
				};

				/**
				 *
				 */
				ScanEventIntegrationOverlay.prototype.populateDataItemsList = function() {
					this.documentDataList.empty();

					this.documentDataList.append("<option value='"
							+ m_constants.TO_BE_DEFINED
							+ "'>"
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
					m_utils.debug("Scan Trigger");
					m_utils.debug(this.page.getModelElement());

					this.scopeModel = this.page.getModel();

					this.populateDataItemsList();

					this.setDocumentData();
				};

				/**
				 *
				 */
				ScanEventIntegrationOverlay.prototype.validate = function() {
					return true;
				};
			}
		});