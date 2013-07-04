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
				$("#propertiesPanelShowControl").click(function() {
					showPropertiesPane();
				});
				$("#propertiesPanelHideControl").click(function() {
					hidePropertiesPane();
				});
			}

			function initialize() {
				i18nProcessScreen();
				$("#modelerDiagramPanelWrapper").css("overflow", "auto");
				$("#modelerPropertiesPanelWrapper").css("width", "0px").css(
						"overflow", "hidden");

				m_dialog.makeVisible($("#propertiesPanelShowControl"));
				m_dialog.makeInvisible($("#propertiesPanelHideControl"));
				m_dialog.makeInvisible($("#modelerPropertiesPanelWrapper"));

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
				jQuery("#basicPropertiesPage div.heading")
						.text(
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.general.heading"));
				jQuery("#description")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));
				jQuery("#nameInput")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.name"));
				jQuery("#processPropertiesPanel div.propertiesPanelTitle")
						.text(
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.processAttachments.titleHeader"));
				jQuery("#processInterfacePropertiesPage div.heading")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.processInterface"));
				jQuery("#providesProcessInterfacePanel div.heading")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.parameterDefinitions"));
				jQuery("#dataPathPropertiesPage div.heading")
						.text(
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.dataPath.header"));

				// alert(m_i18nUtils.getProperty("modeler.propertyView.processs.dataPath.deleteParameter.title"));
				jQuery("#deleteParameterDefinitionButton")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.delete"));
				jQuery("#moveParameterDefinitionUpButton")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.moveUp"));
				jQuery("#moveParameterDefinitionDownButton")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.moveDown"));

				jQuery(
						"#dataPathPropertiesPage input.addParameterDefinitionButton")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.dataPath.addButton.title"));

				$("label[for='defaultPriorityInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.general.defaultPriority"));

				$(
						"#dataPathPropertiesPage label[for='parameterDefinitionNameInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.name"));
				$(
						"#dataPathPropertiesPage label[for='parameterDefinitionDirectionSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.direction"));
				$(
						"#dataPathPropertiesPage label[for='parameterDefinitionDescriptorInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.dataPath.descriptor"));

				$(
						"#dataPathPropertiesPage label[for='parameterDefinitionKeyDescriptorInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.dataPath.keyDescriptor"));
				$(
						"#dataPathPropertiesPage label[for='parameterDefinitionDataSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.data"));
				$(
						"#dataPathPropertiesPage label[for='parameterDefinitionPathInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.dataPath.pathInput"));

				$("#displayPropertiesPage label[for='auxiliaryProcessInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.display.auxiliaryValue"));
				jQuery("#displayPropertiesPage div.heading")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyPages.commonProperties.display"));
				jQuery("#addParameterDefinitionButton")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.dataPath.addButton.title"));

				jQuery("#participantdata")
						.text(
								m_i18nUtils
										.getProperty("modeler.swimlane.properties.participant"));
				jQuery("#datadesc")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.processs.processDefinition.description"));

				$(
						"#dataPathPropertiesPage label[for='targetProcessingTimeInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.name"));


				$(
				"#basicPropertiesPage label[for='nameInput']")
				.text(
						m_i18nUtils
								.getProperty("modeler.element.properties.commonProperties.name"));


				$("#displayPropertiesPage label[for='auxiliaryActivityInput']").text(m_i18nUtils.getProperty("modeler.activity.propertyPages.display.isAuxiliaryActivity"));


				$("#datatableid")
						.find(
								"#primitiveDataTypeRow label[for='primitiveDataTypeSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.primitiveType"));
				$("#datatableid")
						.find("#datatypeid label[for='dataTypeSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.dataType"));
				$("#datatableid")
						.find("#structuredDataTypeRow label[for='structuredDataTypeSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.structuredType"));

				$("#datatableid")
						.find(
								"#primitiveDefaultTextInputRow label[for='primitiveDefaultTextInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.dataTypeProperties.defaultValue"));

				jQuery("#activityheading")
						.text(
								m_i18nUtils
										.getProperty("modeler.activity.propertyPages.title"));

				$("#activitydescription label[for='descriptionInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));

				var primitiveDataTypeSelect = $("#datatableid").find(
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

				$("#datapublicvisibility")
						.find("label[for='publicVisibilityCheckbox']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.publicVisibility"));
				$("#datadescription")
						.find("label[for='descriptionInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));
				$("#dataname")
						.find("label[for='nameInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.name"));
				$("#dataopendataview")
						.find("#viewLink")
						.text(
								m_i18nUtils
										.getProperty("modeler.diagram.toolbar.tool.data.openDataView"));

				$("#startEventPanel")
						.find("label[for='eventTypeSelectInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.diagram.toolbar.tool.startEvent.eventType"));
				$("#annotationdesc")
						.find("label[for='descriptionInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));
				$("#annotationName")
						.find("label[for='nameInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.name"));

				// TODO These queries are far to course grained; will hit a lot of elements
//				jQuery("#event")
//						.text(
//								m_i18nUtils
//										.getProperty("modeler.diagram.toolbar.tool.event.title"));
				jQuery("#annotationheading")
						.text(
								m_i18nUtils
										.getProperty("modeler.diagram.toolbar.tool.annotation.title"));

				$("#gatewayName label[for='nameInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.name"));
				$("#gatewayDesc label[for='descriptionInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));
				$("#gatewaytypeinputselect label[for='gatewayTypeInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.diagram.toolbar.tool.gateway.type"));

				var gatewayTypeInputselect = jQuery("#gatewayTypeInput");

				selectdata = m_i18nUtils.getProperty("modeler.diagram.toolbar.tool.gateway.typeSelect.and");
				gatewayTypeInputselect.append("<option value=\"and\">"+selectdata+"</option>" );

				selectdata = m_i18nUtils.getProperty("modeler.diagram.toolbar.tool.gateway.typeSelect.xor");
				gatewayTypeInputselect.append("<option value=\"xor\">"+selectdata+"</option>" );

				var directionSelect = $("#parameterDefinitionTypeSelector").find("#parameterDefinitionDirectionSelect");

				selectdata = m_i18nUtils.getProperty("modeler.element.properties.commonProperties.in");
				directionSelect.append("<option value=\"IN\">"+ selectdata + "</option>");

				selectdata = m_i18nUtils.getProperty("modeler.element.properties.commonProperties.out");
				directionSelect.append("<option value=\"OUT\">"+ selectdata + "</option>");

				selectdata = m_i18nUtils.getProperty("modeler.element.properties.commonProperties.inout");
				directionSelect.append("<option value=\"INOUT\">" + selectdata + "</option>");

				directionSelect = $("#directionlistTable").find("#parameterDefinitionDirectionSelect");

				selectdata = m_i18nUtils.getProperty("modeler.element.properties.commonProperties.in");
				directionSelect.append("<option value=\"IN\">"+ selectdata + "</option>");

				selectdata = m_i18nUtils.getProperty("modeler.element.properties.commonProperties.out");
				directionSelect.append("<option value=\"OUT\">"+ selectdata + "</option>");


				jQuery("#gatewayid")
				.text(m_i18nUtils.getProperty("modeler.element.properties.commonProperties.gateway"));

				jQuery("#dataid")
				.text(m_i18nUtils.getProperty("modeler.element.properties.commonProperties.data"));
			}

			function hidePropertiesPane() {
				if (true == propertiesPaneVisible) {
					propertiesPaneVisible = false;
					$("#modelerPropertiesPanelWrapper").css("width", "0px")
							.css("overflow", "hidden");

					m_dialog.makeVisible($("#propertiesPanelShowControl"));
					m_dialog.makeInvisible($("#propertiesPanelHideControl"));
					m_dialog.makeInvisible($("#modelerPropertiesPanelWrapper"));
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
				innerWidth = document.body.scrollWidth - $("div.toolbar-section").first().offset().left - 10;
				if ($("div.sg-footer-bar").length > 0) {
					innerHeight = $("div.sg-footer-bar").first().offset().top - $("div.toolbar-section").first().offset().top - 10;
				} else {
					innerHeight = document.body.scrollHeight - $("div.toolbar-section").first().offset().top - 50;
				}

				var diagWidth = innerWidth - HORIZONTAL_SCROLL_OFFSET;

				if (true == propertiesPaneVisible) {
					$("#modelerPropertiesPanelWrapper").css("width", "auto");
					diagWidth = diagWidth
							- $("#modelerPropertiesPanelWrapper")[0].offsetWidth;
				}

				if (diagWidth < getToolbarWidth()) {
					diagWidth = getToolbarWidth();
				}

				$("#modelerDiagramPanelWrapper").css("width", (diagWidth + 10) + "px");
				$("#toolbar").css("width", (diagWidth) + "px");
				$("#messagePanel").css("width", (diagWidth) + "px");
				$("#scrollpane").css("width", (diagWidth) + "px");
				$("#scrollpane").css("height", (innerHeight - getToolbarHeight() - 50) + "px");

			}

			function getToolbarWidth() {
				return $("div.toolbar-section").last().offset().left + $("div.toolbar-section").last().width()
							- $("div.toolbar-section").first().offset().left + 20;
			}

			function getToolbarHeight() {
				return $("div.toolbar-section").last().height();
			}

			function showPropertiesPane() {
				if (false == propertiesPaneVisible) {
					propertiesPaneVisible = true;
					m_dialog.makeVisible($("#modelerPropertiesPanelWrapper"));
					// Collapse properties panel
					$("#modelerPropertiesPanelWrapper").css("width", "0px");
					// Expand properties panel using new values
					$("#modelerPropertiesPanelWrapper").css("width", "auto").css("overflow", "auto");
					m_dialog.makeVisible($("#propertiesPanelHideControl"));
					m_dialog.makeInvisible($("#propertiesPanelShowControl"));
					adjustPanels();
				}
			}

			return {
				initialize : function(fullId) {
					m_utils.initializeWaitCursor($("html"));
					m_utils.showWaitCursor();

					initialize();
					m_modelerCanvasController.initialize(fullId, "canvas",
							5000, 1000, 'toolbar');
					m_propertiesPanel
					.initializeProcessPropertiesPanel(m_processPropertiesPanel
							.getInstance());
					this.showPropertiesPane();

					m_utils.hideWaitCursor();

					var process = m_model.findProcess(fullId);
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