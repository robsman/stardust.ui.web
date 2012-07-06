/**
 * @author shrikant.gangal
 */
define([ "m_toolbarManager", "m_constants" ], function(m_toolbarManager,
		m_constants) {
	var currentSelection;
	var toolSelectActions = {
		createModelToolSelected : function(data) {
			fireToolSelectedEvent(data);
		},
		importModelToolSelected : function(data) {
			fireToolSelectedEvent(data);
		},
		saveModelToolSelected : function(data) {
			fireToolSelectedEvent(data);
		},
		saveAllModelsToolSelected : function(data) {
			fireToolSelectedEvent(data);
		},
		refreshModelsToolSelected : function(data) {
			fireToolSelectedEvent(data);
		}
	};

	function fireToolSelectedEvent(data) {
		jQuery(document).trigger("TOOL_CLICKED_EVENT", {
			"id" : data.toolId
		});
	}

	return {
		init : function(toolbarDiv) {
			m_toolbarManager.init(toolbarDiv, toolSelectActions);
		}
	};
});