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
 * 
 */
define([ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
		"bpm-modeler/js/m_messageDisplay", "bpm-modeler/js/m_canvasManager",
		"bpm-modeler/js/m_communicationController",
		"bpm-modeler/js/m_constants", "bpm-modeler/js/m_logger",
		"bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_diagram",
		"bpm-modeler/js/m_activitySymbol", "bpm-modeler/js/m_eventSymbol",
		"bpm-modeler/js/m_gatewaySymbol", "bpm-modeler/js/m_dataSymbol",
		"bpm-modeler/js/m_annotationSymbol", "bpm-modeler/js/m_model",
		"bpm-modeler/js/m_process", "bpm-modeler/js/m_activity",
		"bpm-modeler/js/m_data", "bpm-modeler/js/m_i18nUtils" ],

function(m_utils, m_constants, m_messageDisplay, m_canvasManager,
		m_communicationController, m_constants, m_logger, m_commandsController,
		m_diagram, m_activitySymbol, m_eventSymbol, m_gatewaySymbol,
		m_dataSymbol, m_annotationSymbol, m_model, m_process, m_activity,
		m_data, m_i18nUtils) {

	function selectTool(toolButtonId) {
		$(".selected-tool").removeClass("selected-tool");
		$("#" + toolButtonId).addClass("selected-tool");
	}
	return {
		setSelectMode : function(diagram) {
			selectTool("selectModeButton");
			diagram.setSelectMode();
		},
		setSeparatorMode : function(diagram) {
			selectTool("separatorModeButton");
			diagram.setSeparatorMode();
		},
		createActivity : function(diagram) {
			diagram.clearCurrentToolSelection();
			selectTool("activityButton");
			diagram.mode = diagram.CREATE_MODE;
			diagram.newSymbol = m_activitySymbol.createActivitySymbol(diagram,
					m_constants.TASK_ACTIVITY_TYPE);
		},
		createSwimlane : function(diagram) {
			diagram.clearCurrentToolSelection();
			diagram.poolSymbol.createSwimlaneSymbol();
		},
		createStartEvent : function(diagram) {
			diagram.clearCurrentToolSelection();
			selectTool("startEventButton");
			diagram.mode = diagram.CREATE_MODE;
			diagram.newSymbol = m_eventSymbol.createStartEventSymbol(diagram);
		},
		createIntermediateEvent : function(diagram) {
			diagram.clearCurrentToolSelection();
			selectTool("intermediateEventButton");
			diagram.mode = diagram.CREATE_MODE;
			diagram.newSymbol = m_eventSymbol
					.createIntermediateEventSymbol(diagram);
		},
		createEndEvent : function(diagram) {
			diagram.clearCurrentToolSelection();
			selectTool("endEventButton");
			diagram.mode = diagram.CREATE_MODE;
			diagram.newSymbol = m_eventSymbol.createStopEventSymbol(diagram);
		},
		createData : function(diagram) {
			diagram.clearCurrentToolSelection();
			selectTool("dataButton");
			diagram.mode = diagram.CREATE_MODE;
			diagram.newSymbol = m_dataSymbol.createDataSymbol(diagram);
		},
		createGateway : function(diagram) {
			diagram.clearCurrentToolSelection();
			selectTool("gatewayButton");
			diagram.mode = diagram.CREATE_MODE;
			diagram.newSymbol = m_gatewaySymbol.createGatewaySymbol(diagram);
		},
		createConnector : function(diagram) {
			diagram.clearCurrentToolSelection();
			selectTool("connectorButton");
			diagram.mode = diagram.CONNECTION_MODE;
			var errorMessage = m_i18nUtils
					.getProperty("modeler.diagram.toolbar.tool.errorMessage");
			m_messageDisplay.showMessage(errorMessage);
		},
		createAnnotation : function(diagram) {
			diagram.clearCurrentToolSelection();
			selectTool("annotationButton");
			diagram.mode = diagram.CREATE_MODE;
			diagram.newSymbol = m_annotationSymbol.create(diagram);
		},
		zoomIn : function(diagram) {
			diagram.zoomIn();
		},
		zoomOut : function(diagram) {
			diagram.zoomOut();
		},
		print : function(diagram) {
			diagram.print();
		},
		flipOrientation : function(diagram) {
			diagram.flipFlowOrientation();
		},
		undo : function(diagram) {
			m_communicationController.postData({
				url : m_communicationController.getEndpointUrl()
						+ "/sessions/changes/mostCurrent/navigation"
			}, "undoMostCurrent", {
				success : function(data) {
					m_utils.debug("Undo");
					m_utils.debug(data);

					m_commandsController.broadcastCommandUndo(data);

					if (null != data.pendingUndo) {
						jQuery("#undo").removeAttr("disabled", "disabled");
					} else {
						jQuery("#undo").attr("disabled", "disabled");
					}

					if (null != data.pendingRedo) {
						jQuery("#redo").removeAttr("disabled", "disabled");
					} else {
						jQuery("#redo").attr("disabled", "disabled");
					}
				}
			});
		},
		redo : function(diagram) {
			m_communicationController.postData({
				url : m_communicationController.getEndpointUrl()
						+ "/sessions/changes/mostCurrent/navigation"
			}, "redoLastUndo", {
				success : function(data) {
					m_utils.debug("Redo");
					m_utils.debug(data);

					m_commandsController.broadcastCommand(data);

					if (null != data.pendingUndo) {
						jQuery("#undo").removeAttr("disabled", "disabled");
					} else {
						jQuery("#undo").attr("disabled", "disabled");
					}

					if (null != data.pendingRedo) {
						jQuery("#redo").removeAttr("disabled", "disabled");
					} else {
						jQuery("#redo").attr("disabled", "disabled");
					}
				}
			});
		}
	};
});