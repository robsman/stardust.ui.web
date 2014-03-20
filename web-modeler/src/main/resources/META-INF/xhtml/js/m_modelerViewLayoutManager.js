/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author shrikant.gangal
 */
define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_dialog", "bpm-modeler/js/m_modelerCanvasController", "bpm-modeler/js/m_propertiesPanel", "bpm-modeler/js/m_processPropertiesPanel","bpm-modeler/js/m_i18nUtils", "bpm-modeler/js/m_model"],
		function(m_utils, m_constants, m_dialog, m_modelerCanvasController, m_propertiesPanel, m_processPropertiesPanel,m_i18nUtils, m_model) {
			var innerHeight = 600;
			var innerWidth = 800;
			var propertiesPaneVisible = false;
			var HORIZONTAL_SCROLL_OFFSET = 30;

			function initPropertiesPanelCollapseClickHandlers() {
				m_utils.jQuerySelect("#propertiesPanelShowControl").click(function() {
					propertiesPaneVisible = false;
					showPropertiesPane();
				});
				m_utils.jQuerySelect("#propertiesPanelHideControl").click(function() {
					propertiesPaneVisible = true;
					hidePropertiesPane();
				});
			}

			function initialize() {
				propertiesPaneVisible = false;
				i18nProcessScreen();
				m_utils.jQuerySelect("#modelerDiagramPanelWrapper").css("overflow", "auto");
				m_utils.jQuerySelect("#modelerPropertiesPanelWrapper").css("width", "0px").css(
						"overflow", "hidden");

				m_dialog.makeVisible(m_utils.jQuerySelect("#propertiesPanelShowControl"));
				m_dialog.makeInvisible(m_utils.jQuerySelect("#propertiesPanelHideControl"));
				m_dialog.makeInvisible(m_utils.jQuerySelect("#modelerPropertiesPanelWrapper"));

				initPropertiesPanelCollapseClickHandlers();

				window.parent.EventHub.events.subscribe("PROCESS_IFRAME_RESIZED", setDimensions);
				//window.parent.ippPortalMain.InfinityBpm.ProcessPortal.resizeProcessDefinitionIFrame(window.frameElement.id, null);
			}

			/**
			 * set Dimensions
			 */
			function setDimensions(dimensions) {
				if (dimensions.anchorId == 'processDefinitionFrameAnchor') {
					innerHeight = dimensions.height;
					innerWidth = dimensions.width;
					adjustPanels();
				}
			}

			function i18nProcessScreen() {
				m_utils.jQuerySelect("#basicPropertiesPage div.heading")
						.text(
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.general.heading"));
				m_utils.jQuerySelect("#description")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));
				m_utils.jQuerySelect("#nameInput")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.name"));
				m_utils.jQuerySelect("#processPropertiesPanel div.propertiesPanelTitle")
						.text(
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.processAttachments.titleHeader"));
				m_utils.jQuerySelect("#processInterfacePropertiesPage div.heading")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.processInterface"));
				m_utils.jQuerySelect("#providesProcessInterfacePanel div.heading")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.parameterDefinitions"));
				m_utils.jQuerySelect("#dataPathPropertiesPage div.heading")
						.text(
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.dataPath.header"));

				// alert(m_i18nUtils.getProperty("modeler.propertyView.processs.dataPath.deleteParameter.title"));
				m_utils.jQuerySelect("#deleteParameterDefinitionButton")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.delete"));
				m_utils.jQuerySelect("#moveParameterDefinitionUpButton")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.moveUp"));
				m_utils.jQuerySelect("#moveParameterDefinitionDownButton")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.moveDown"));

				m_utils.jQuerySelect(
						"#dataPathPropertiesPage input.addParameterDefinitionButton")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.dataPath.addButton.title"));

				m_utils.jQuerySelect("label[for='defaultPriorityInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.general.defaultPriority"));

				m_utils.jQuerySelect(
						"#dataPathPropertiesPage label[for='parameterDefinitionNameInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.name"));
				m_utils.jQuerySelect(
						"#dataPathPropertiesPage label[for='parameterDefinitionIdOutput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.id"));
				m_utils.jQuerySelect(
						"#dataPathPropertiesPage label[for='parameterDefinitionDirectionSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.direction"));
				m_utils.jQuerySelect(
						"#dataPathPropertiesPage label[for='parameterDefinitionDescriptorInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.dataPath.descriptor"));

				m_utils.jQuerySelect(
						"#dataPathPropertiesPage label[for='parameterDefinitionKeyDescriptorInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.dataPath.keyDescriptor"));
				m_utils.jQuerySelect(
						"#dataPathPropertiesPage label[for='parameterDefinitionDataSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.data"));
				m_utils.jQuerySelect(
						"#dataPathPropertiesPage label[for='parameterDefinitionPathInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.dataPath.pathInput"));

				m_utils.jQuerySelect("#displayPropertiesPage label[for='auxiliaryProcessInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.display.auxiliaryValue"));
				m_utils.jQuerySelect("#displayPropertiesPage div.heading")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyPages.commonProperties.display"));
				m_utils.jQuerySelect("#addParameterDefinitionButton")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.dataPath.addButton.title"));

				m_utils.jQuerySelect("#participantdata")
						.text(
								m_i18nUtils
										.getProperty("modeler.swimlane.properties.participant"));
				m_utils.jQuerySelect("#datadesc")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.processs.processDefinition.description"));

				m_utils.jQuerySelect(
						"#dataPathPropertiesPage label[for='targetProcessingTimeInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.name"));


				m_utils.jQuerySelect(
				"#basicPropertiesPage label[for='nameInput']")
				.text(
						m_i18nUtils
								.getProperty("modeler.element.properties.commonProperties.name"));


				m_utils.jQuerySelect("#displayPropertiesPage label[for='auxiliaryActivityInput']").text(m_i18nUtils.getProperty("modeler.activity.propertyPages.display.isAuxiliaryActivity"));


				m_utils.jQuerySelect("#datatableid")
						.find(
								"#primitiveDataTypeRow label[for='primitiveDataTypeSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.primitiveType"));
				m_utils.jQuerySelect("#datatableid")
						.find("#datatypeid label[for='dataTypeSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.dataType"));
				m_utils.jQuerySelect("#datatableid")
						.find("#structuredDataTypeRow label[for='structuredDataTypeSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.structuredType"));

				m_utils.jQuerySelect("#datatableid")
						.find(
								"#primitiveDefaultTextInputRow label[for='primitiveDefaultTextInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.dataTypeProperties.defaultValue"));

				m_utils.jQuerySelect("#activityheading")
						.text(
								m_i18nUtils
										.getProperty("modeler.activity.propertyPages.title"));

				m_utils.jQuerySelect("#activitydescription label[for='descriptionInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));

				var primitiveDataTypeSelect = m_utils.jQuerySelect("#datatableid").find(
						"#primitiveDataTypeRow").find(
						"#primitiveDataTypeSelect");

				var selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.string");
				primitiveDataTypeSelect.append("<option value=\"String\">"
						+ selectdata + "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.boolean");
				primitiveDataTypeSelect.append("<option value=\"boolean\">"
						+ selectdata + "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.int");
				primitiveDataTypeSelect.append("<option value=\"int\">"
						+ selectdata + "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.long");
				primitiveDataTypeSelect.append("<option value=\"long\">"
						+ selectdata + "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.double");
				primitiveDataTypeSelect.append("<option value=\"double\">"
						+ selectdata + "</option>");

				// Commented as we don't support Money values yet.
//				selectdata = m_i18nUtils
//						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.decimal");
//				primitiveDataTypeSelect.append("<option value=\"Decimal\">"
//						+ selectdata + "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.calender");
				primitiveDataTypeSelect.append("<option value=\"Calendar\">"
						+ selectdata + "</option>");

				m_utils.jQuerySelect("#datapublicvisibility")
						.find("label[for='publicVisibilityCheckbox']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.publicVisibility"));
				m_utils.jQuerySelect("#datadescription")
						.find("label[for='descriptionInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));
				m_utils.jQuerySelect("#dataname")
						.find("label[for='nameInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.name"));
				m_utils.jQuerySelect("#dataopendataview")
						.find("#viewLink")
						.text(
								m_i18nUtils
										.getProperty("modeler.diagram.toolbar.tool.data.openDataView"));

				m_utils.jQuerySelect("#startEventPanel")
						.find("label[for='eventTypeSelectInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.diagram.toolbar.tool.startEvent.eventType"));
				m_utils.jQuerySelect("#annotationdesc")
						.find("label[for='descriptionInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));
				m_utils.jQuerySelect("#annotationName")
						.find("label[for='nameInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.name"));

				// TODO These queries are far to course grained; will hit a lot of elements
//				m_utils.jQuerySelect("#event")
//						.text(
//								m_i18nUtils
//										.getProperty("modeler.diagram.toolbar.tool.event.title"));
				m_utils.jQuerySelect("#annotationheading")
						.text(
								m_i18nUtils
										.getProperty("modeler.diagram.toolbar.tool.annotation.title"));

				m_utils.jQuerySelect("#gatewayName label[for='nameInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.name"));
				m_utils.jQuerySelect("#gatewayDesc label[for='descriptionInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));
				m_utils.jQuerySelect("#gatewaytypeinputselect label[for='gatewayTypeInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.diagram.toolbar.tool.gateway.type"));

				var gatewayTypeInputselect = m_utils.jQuerySelect("#gatewayTypeInput");

				selectdata = m_i18nUtils.getProperty("modeler.diagram.toolbar.tool.gateway.typeSelect.and");
				gatewayTypeInputselect.append("<option value=\"and\">"+selectdata+"</option>" );

				selectdata = m_i18nUtils.getProperty("modeler.diagram.toolbar.tool.gateway.typeSelect.xor");
				gatewayTypeInputselect.append("<option value=\"xor\">"+selectdata+"</option>" );

				selectdata = m_i18nUtils.getProperty("modeler.diagram.toolbar.tool.gateway.typeSelect.or");
				gatewayTypeInputselect.append("<option value=\"or\">"+selectdata+"</option>" );
				
				m_utils.jQuerySelect("#gatewayid")
				.text(m_i18nUtils.getProperty("modeler.element.properties.commonProperties.gateway"));

				m_utils.jQuerySelect("#dataid")
				.text(m_i18nUtils.getProperty("modeler.element.properties.commonProperties.data"));
			}

			function hidePropertiesPane() {
				if (true == propertiesPaneVisible) {
					propertiesPaneVisible = false;
					m_utils.jQuerySelect("#modelerPropertiesPanelWrapper").css("width", "0px")
							.css("overflow", "hidden");

					m_dialog.makeVisible(m_utils.jQuerySelect("#propertiesPanelShowControl"));
					m_dialog.makeInvisible(m_utils.jQuerySelect("#propertiesPanelHideControl"));
					m_dialog.makeInvisible(m_utils.jQuerySelect("#modelerPropertiesPanelWrapper"));
					adjustPanels();
				}
			}

			/*
			 * Ideally setting of toolbar / diagram pane width etc. should have been handled with width = auto / 100% etc but that
			 * didn't work in latest FF (worked in FF4) hence taking a tedious way.
			 */

			function adjustPanels() {
				// Assign available horizontal width to innerWidth with a hardcoded offset of 10 pixels
				// to avoid extra horizontal scrolls
				innerWidth = document.body.scrollWidth - m_utils.jQuerySelect("div.toolbar-section").first().offset().left - 10;
				if (m_utils.jQuerySelect("div.sg-footer-bar").length > 0) {
					innerHeight = m_utils.jQuerySelect("div.sg-footer-bar").first().offset().top - m_utils.jQuerySelect("div.toolbar-section").first().offset().top - 10;
				} else {
					innerHeight = document.body.scrollHeight - m_utils.jQuerySelect("div.toolbar-section").first().offset().top - 50;
				}

				var diagWidth = innerWidth - HORIZONTAL_SCROLL_OFFSET;

				if (true == propertiesPaneVisible) {
					m_utils.jQuerySelect("#modelerPropertiesPanelWrapper").css("width", "auto");
					diagWidth = diagWidth
							- m_utils.jQuerySelect("#modelerPropertiesPanelWrapper")[0].offsetWidth;
				}

				if (diagWidth < getToolbarWidth()) {
					diagWidth = getToolbarWidth();
				}

				m_utils.jQuerySelect("#modelerDiagramPanelWrapper").css("width", (diagWidth + 10) + "px");
				m_utils.jQuerySelect("#toolbar").css("width", (diagWidth) + "px");
				m_utils.jQuerySelect("#messagePanel").css("width", (diagWidth) + "px");
				m_utils.jQuerySelect("#scrollpane").css("width", (diagWidth) + "px");
				m_utils.jQuerySelect("#scrollpane").css("height", (innerHeight - getToolbarHeight() - 50) + "px");

			}

			function getToolbarWidth() {
				return m_utils.jQuerySelect("div.toolbar-section").last().offset().left + m_utils.jQuerySelect("div.toolbar-section").last().width()
							- m_utils.jQuerySelect("div.toolbar-section").first().offset().left + 20;
			}

			function getToolbarHeight() {
				return m_utils.jQuerySelect("div.toolbar-section").last().height();
			}

			function showPropertiesPane() {
				if (false == propertiesPaneVisible) {
					propertiesPaneVisible = true;
					m_dialog.makeVisible(m_utils.jQuerySelect("#modelerPropertiesPanelWrapper"));
					// Collapse properties panel
					m_utils.jQuerySelect("#modelerPropertiesPanelWrapper").css("width", "0px");
					// Expand properties panel using new values
					m_utils.jQuerySelect("#modelerPropertiesPanelWrapper").css("width", "auto").css("overflow", "auto");
					m_dialog.makeVisible(m_utils.jQuerySelect("#propertiesPanelHideControl"));
					m_dialog.makeInvisible(m_utils.jQuerySelect("#propertiesPanelShowControl"));
					adjustPanels();
				}
			}

			return {
				initialize : function(fullId) {
					m_utils.initializeWaitCursor(m_utils.jQuerySelect("html"));
					m_utils.showWaitCursor();

					initialize();
					var newId = "canvas" + Math.floor((Math.random()*10000) + 1);
					m_utils.jQuerySelect("#canvas").get(0).id = newId;
					m_modelerCanvasController.initialize(fullId, newId,
							5000, 1000, 'toolbar');
					var process = m_model.findProcess(fullId);

					this.showPropertiesPane();

					m_utils.hideWaitCursor();

					if (process && process.isReadonly()) {
						m_utils.markControlsReadonly();
					}
				},

				reInitialize : function() {
					initialize();
				},

				showPropertiesPane : function() {
					showPropertiesPane();
				},

				hidePropertiesPane : function() {
					hidePropertiesPane();
				},

				adjustPanels : function() {
					adjustPanels();
				}
			};
		});