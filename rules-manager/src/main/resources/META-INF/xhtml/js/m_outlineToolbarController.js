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
define([ "bpm-modeler/js/m_toolbarManager", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_utils" ], function(m_toolbarManager,
		m_constants, m_utils) {
	var currentSelection;
	var toolSelectActions = {
		createModelToolSelected : function(data) {
			fireToolSelectedEvent(data);
		},
		importModelToolSelected : function(data) {
			fireToolSelectedEvent(data);
		},
		undoChangeToolSelected : function(data) {
			fireToolSelectedEvent(data);
		},
		redoChangeToolSelected : function(data) {
			fireToolSelectedEvent(data);
		},
		saveAllRulesToolSelected : function(data) {
			fireToolSelectedEvent(data);
		},
		refreshRulesToolSelected : function(data) {
			fireToolSelectedEvent(data);
		}
	};

	function fireToolSelectedEvent(data) {
		m_utils.jQuerySelect(document).trigger("TOOL_CLICKED_EVENT", {
			"id" : data.toolId
		});
	}

	return {
		init : function(toolbarDiv) {
			m_toolbarManager.init(toolbarDiv, toolSelectActions);
		}
	};
});