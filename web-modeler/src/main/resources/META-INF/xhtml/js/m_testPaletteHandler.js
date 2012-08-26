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
define([ "m_utils", "m_constants", "m_messageDisplay", "m_canvasManager",
		"m_communicationController", "m_constants", "m_logger",
		"m_commandsController", "m_diagram", "m_testSymbol" ], function(
		m_utils, m_constants, m_messageDisplay, m_canvasManager,
		m_communicationController, m_constants, m_logger, m_commandsController,
		m_diagram, m_testSymbol) {
	return {
		createTestSymbol : function(diagram) {
			diagram.newSymbol = m_testSymbol.createTestSymbol(diagram);
		}
	};
});