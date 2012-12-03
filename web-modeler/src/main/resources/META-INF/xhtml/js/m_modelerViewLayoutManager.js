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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_dialog", "bpm-modeler/js/m_modelerCanvasController", "bpm-modeler/js/m_propertiesPanel", "bpm-modeler/js/m_processPropertiesPanel","bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_dialog, m_modelerCanvasController, m_propertiesPanel, m_processPropertiesPanel,m_i18nUtils) {
			var innerHeight;
			var innerWidth;
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
				innerHeight = window.innerHeight;
				innerWidth = window.innerWidth;
				i18nProcessScreen();
				$("#modelerDiagramPanelWrapper").css("width",
						(innerWidth - HORIZONTAL_SCROLL_OFFSET) + "px").css(
						"overflow", "auto");
				$("#modelerPropertiesPanelWrapper").css("width", "0px").css(
						"overflow", "hidden");

				setDrawingPaneDivWidths((parseInt($("#modelerDiagramPanelWrapper").css("width")) - 10));

				m_dialog.makeVisible($("#propertiesPanelShowControl"));
				m_dialog.makeInvisible($("#propertiesPanelHideControl"));
				m_dialog.makeInvisible($("#modelerPropertiesPanelWrapper"));

				initPropertiesPanelCollapseClickHandlers();
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
										.getProperty("modeler.processDefinition.propertyPages.general.defaultProperty"));

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
										.getProperty("modeler.propertyView.processs.processDefinition.participant"));
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
						.find(
								"#primitiveDefaultTextInputRow label[for='primitiveDefaultTextInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.createPrimitiveData.dataTypeProperties.defaultValue"));
				$("#datatableid")
						.find(
								"#primitiveDefaultCheckboxInputRow label[for='primitiveDefaultCheckboxInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.createPrimitiveData.dataTypeProperties.defaultValue"));

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

				selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.decimal");
				primitiveDataTypeSelect.append("<option value=\"Decimal\">"
						+ selectdata + "</option>");

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



				var processdefinationselect = $("#processdefinationselect").find("#processInterfaceTypeSelectInput");

				selectdata = m_i18nUtils.getProperty("modeler.processdefination.propertyPages.processInterface.type.noProcessInterface");
				processdefinationselect.append("<option value=\"noProcessInterface\">"+selectdata+"</option>" );

				selectdata = m_i18nUtils
						.getProperty("modeler.diagram.toolbar.tool.gateway.typeSelect.and");
				gatewayTypeInputselect.append("<option value=\"and\">"
						+ selectdata + "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.diagram.toolbar.tool.gateway.typeSelect.xor");
				gatewayTypeInputselect.append("<option value=\"xor\">"
						+ selectdata + "</option>");

				var processdefinationselect = $("#processdefinationselect")
						.find("#processInterfaceTypeSelectInput");

				selectdata = m_i18nUtils
						.getProperty("modeler.processdefination.propertyPages.processInterface.type.noProcessInterface");
				processdefinationselect
						.append("<option value=\"noProcessInterface\">"
								+ selectdata + "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.processdefination.propertyPages.processInterface.type.providesProcessInterface");
				processdefinationselect
						.append("<option value=\"providesProcessInterface\">"
								+ selectdata + "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.processdefination.propertyPages.processInterface.type.implementsProcessInterface");
				processdefinationselect
						.append("<option value=\"implementsProcessInterface\">"
								+ selectdata + "</option>");

				$("#processdefinationselect")
						.find("label[for='processInterfaceTypeSelectInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.type"));


				var directionSelect = $("#parameterDefinitionTypeSelector").find("#parameterDefinitionDirectionSelect");

				selectdata = m_i18nUtils.getProperty("modeler.element.properties.commonProperties.in");
				directionSelect.append("<option value=\"IN\">"+ selectdata + "</option>");

				selectdata = m_i18nUtils.getProperty("modeler.element.properties.commonProperties.out");
				directionSelect.append("<option value=\"OUT\">"+ selectdata + "</option>");

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
					$("#modelerDiagramPanelWrapper").css("width",
							(innerWidth - HORIZONTAL_SCROLL_OFFSET) + "px");
				}

				setDrawingPaneDivWidths((parseInt($("#modelerDiagramPanelWrapper").css("width")) - 10));

				m_dialog.makeVisible($("#propertiesPanelShowControl"));
				m_dialog.makeInvisible($("#propertiesPanelHideControl"));
				m_dialog.makeInvisible($("#modelerPropertiesPanelWrapper"));
			}

			function adjustPanels() {
				if (true == propertiesPaneVisible) {
					m_dialog.makeVisible($("#modelerPropertiesPanelWrapper"));
					// Collapse properties panel
					$("#modelerPropertiesPanelWrapper").css("width", "0px");
					$("#modelerDiagramPanelWrapper")
							.css(
									"width",
									(innerWidth
											- $("#modelerPropertiesPanelWrapper")[0].offsetWidth - HORIZONTAL_SCROLL_OFFSET)
											+ "px").css("overflow", "auto");

					// Expand properties panel using new values
					$("#modelerPropertiesPanelWrapper").css("width", "auto")
							.css("overflow", "auto");

					var diagWidth = innerWidth - $("#modelerPropertiesPanelWrapper")[0].offsetWidth - HORIZONTAL_SCROLL_OFFSET;

					if (diagWidth < getToolbarWidth()) {
						diagWidth = getToolbarWidth() + 10;
					}

					setDrawingPaneDivWidths((diagWidth - 10));

					$("#modelerDiagramPanelWrapper").css("width", (diagWidth) + "px").css("overflow", "auto");
				}
			}

			function showPropertiesPane() {
				if (false == propertiesPaneVisible) {
					propertiesPaneVisible = true;

					adjustPanels();

					m_dialog.makeVisible($("#propertiesPanelHideControl"));
					m_dialog.makeInvisible($("#propertiesPanelShowControl"));
				}
			}

			function getToolbarWidth() {
				return $("div.toolbar-section").last().offset().left + $("div.toolbar-section").last().width() + 20;
			}

			/*
			 * Ideally setting of toolbar / diagram pane width etc. should have been handled with width = auto / 100% etc but that
			 * didn't work in latest FF (worked in FF4) hence taking a tedious way.
			 *
			 * */
			function setDrawingPaneDivWidths(width) {
				$("#toolbar").css("width", (width) + "px");
				$("#messagePanel").css("width", (width) + "px");
				$("#scrollpane").css("width", (width) + "px");
			}

			return {
				initialize : function(fullId) {
					initialize();
					m_modelerCanvasController.initialize(fullId, "canvas",
							5000, 1000, 'toolbar');
					m_propertiesPanel
					.initializeProcessPropertiesPanel(m_processPropertiesPanel
							.getInstance());
					this.showPropertiesPane();
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