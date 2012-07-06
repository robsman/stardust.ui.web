/**
 * @author Omkar.Patil
 */
define([ "m_toolbarManager", "m_constants" ], function(m_toolbarManager,
		m_constants) {
	var currentSelection;
	var toolSelectActions = {
			selectModeSelected : function(data) {
				jQuery(document).trigger("TOOL_CLICKED_EVENT", {
					"id" : data.toolId
				});
			},

		activityToolSelected : function(data) {
			jQuery(document).trigger("TOOL_CLICKED_EVENT", {
				"id" : data.toolId
			});
		},

		createswimlaneToolSelected : function(data) {
			jQuery(document).trigger("TOOL_CLICKED_EVENT", {
				"id" : data.toolId
			});
		},

		starteventToolSelected : function(data) {
			jQuery(document).trigger("TOOL_CLICKED_EVENT", {
				"id" : data.toolId
			});
		},

		endeventToolSelected : function(data) {
			jQuery(document).trigger("TOOL_CLICKED_EVENT", {
				"id" : data.toolId
			});
		},

		newConnectorToolSelected : function(data) {
			jQuery(document).trigger("TOOL_CLICKED_EVENT", {
				"id" : data.toolId
			});
		},

		dataToolSelected : function(data) {
			jQuery(document).trigger("TOOL_CLICKED_EVENT", {
				"id" : data.toolId
			});
		},

		gatewayToolSelected : function(data) {
			jQuery(document).trigger("TOOL_CLICKED_EVENT", {
				"id" : data.toolId
			});
		},

		zoomInToolSelected : function(data) {
			jQuery(document).trigger("TOOL_CLICKED_EVENT", {
				"id" : data.toolId
			});
		},

		zoomOutToolSelected : function(data) {
			jQuery(document).trigger("TOOL_CLICKED_EVENT", {
				"id" : data.toolId
			});
		},

		saveToolSelected : function(data) {
			jQuery(document).trigger("TOOL_CLICKED_EVENT", {
				"id" : data.toolId
			});
		},

		loadToolSelected : function(data) {
			jQuery(document).trigger("TOOL_CLICKED_EVENT", {
				"id" : data.toolId
			});
		},

		undoToolSelected : function(data) {
			jQuery(document).trigger("TOOL_CLICKED_EVENT", {
				"id" : data.toolId
			});
		},

		redoToolSelected : function(data) {
			jQuery(document).trigger("TOOL_CLICKED_EVENT", {
				"id" : data.toolId
			});
		},

		flipOrientationToolSelected : function(data) {
			jQuery(document).trigger("TOOL_CLICKED_EVENT", {
				"id" : data.toolId
			});
		},

		printToolSelected : function(data) {
			jQuery(document).trigger("TOOL_CLICKED_EVENT", {
				"id" : data.toolId
			});
		}
	};

	return {
		init : function(toolbarDiv) {
			m_toolbarManager.init(toolbarDiv, toolSelectActions);
			setupEventHandling();
		},

		getCurrentSelection : function() {
			return currentSelection;
		},

		resetCurrentSelection : function() {
			resetSelection();
		}
	}

	function setupEventHandling() {
		jQuery(document).bind('CONNECTOR_CREATED', function() {
			resetSelection();
		});
	}
	;

	function makeSelection(elementId) {
		currentSelection = elementId;
	}
	;

	function resetSelection() {
		currentSelection = null;
	}
	;
});