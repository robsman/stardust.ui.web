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
define([ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_messageDisplay", "bpm-modeler/js/m_canvasManager",
		"bpm-modeler/js/m_communicationController", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_logger",
		"bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_diagram", "bpm-modeler/js/m_testSymbol" ], function(
		m_utils, m_constants, m_messageDisplay, m_canvasManager,
		m_communicationController, m_constants, m_logger, m_commandsController,
		m_diagram, m_testSymbol) {
	return {
		createTestSymbol : function(diagram) {
			diagram.newSymbol = m_testSymbol.createTestSymbol(diagram);
		}
	};
});