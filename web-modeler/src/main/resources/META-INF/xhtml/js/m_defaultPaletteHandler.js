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
define([ "m_utils", "m_constants", "m_messageDisplay",
		"m_canvasManager",
		"m_communicationController", "m_constants", "m_logger",
		"m_commandsController", "m_diagram", "m_activitySymbol",
		"m_eventSymbol", "m_gatewaySymbol", "m_dataSymbol", "m_model",
		"m_process", "m_activity", "m_data" ], function(m_utils, m_constants,
		m_messageDisplay, m_canvasManager,
		m_communicationController, m_constants, m_logger, m_commandsController,
		m_diagram, m_activitySymbol, m_eventSymbol, m_gatewaySymbol,
		m_dataSymbol, m_model, m_process, m_activity, m_data) {

	return {
		setSelectMode : function(diagram) {
			diagram.setSelectMode();
		},

		setSeparatorMode : function(diagram) {
			diagram.setSeparatorMode();
		},

		createActivity : function(diagram) {
			diagram.newSymbol = m_activitySymbol.createActivitySymbol(diagram,
					m_constants.MANUAL_ACTIVITY_TYPE);
		},

		createSwimlane : function(diagram) {
			diagram.poolSymbol.createSwimlaneSymbol();
		},

		createStartEvent : function(diagram) {
			diagram.newSymbol = m_eventSymbol.createStartEventSymbol(diagram);
		},

		createEndEvent : function(diagram) {
			diagram.newSymbol = m_eventSymbol.createStopEventSymbol(diagram);
	},

		createData : function(diagram) {
			diagram.newSymbol = m_dataSymbol.createDataSymbol(diagram);
		},

		createGateway : function(diagram) {
			diagram.newSymbol = m_gatewaySymbol.createGatewaySymbol(diagram);
		},

		createConnector : function(diagram) {
			diagram.mode = diagram.CONNECTION_MODE;
			m_messageDisplay
					.showMessage("Select first anchor point for connection.");
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