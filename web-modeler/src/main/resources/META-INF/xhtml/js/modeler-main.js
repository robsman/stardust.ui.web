/**
 * @author Robert Sauer
 */

require.config({
	paths : {
		'jquery': 'libs/jquery/jquery-1.7.2',
		'json' :'libs/json/json2',
		'raphael': 'libs/raphael/2.0.1/raphael',

		'jquery-ui': 'libs/jquery/plugins/jquery-ui-1.8.19.custom.min',
		'jquery.download': 'libs/jquery/plugins/download.jQuery',
		'jquery.form': 'libs/jquery/plugins/jquery.form',
		'jquery.impromptu': 'libs/jquery/plugins/jquery-impromptu.3.1.min',
		'jquery.jeditable': 'libs/jquery/plugins/jquery.jeditable',
		'jquery.print': 'libs/jquery/plugins/jquery.print',
		'jquery.simplemodal': 'libs/jquery/plugins/jquery.simplemodal.1.4.1.min',
		'jquery.tablescroll': 'libs/jquery/plugins/jquery.tablescroll',
		'jquery.treeTable': 'libs/jquery/plugins/jquery.treeTable',
		'jquery.url': 'libs/jquery/plugins/jquery.url',
		
		'codemirror': 'libs/codemirror/codemirror-2.34',
		'codemirror.mode.javascript': 'libs/codemirror/mode/javascript/javascript',
		'codemirror.util.dialog': 'libs/codemirror/util/dialog',
		'codemirror.util.searchcursor': 'libs/codemirror/util/searchcursor',
		'codemirror.util.search': 'libs/codemirror/util/search',
		'codemirror.util.match-highlighter': 'libs/codemirror/util/match-highlighter',
		'codemirror.util.simple-hint': 'libs/codemirror/util/simple-hint',
		'codemirror.util.javascript-hint': 'libs/codemirror/util/javascript-hint',

		'modeler-plugins': '../../../services/rest/bpm-modeler/config/ui/plugins/modeler-plugins',
		'extensions': '../../../services/rest/bpm-modeler/config/ui/extensions.js?p=extensions',
	},
	shim: {
		'json': {
			exports: "JSON"
		},
		'raphael': {
			exports: 'Raphael'
		},
		'extensions': {
			exports: "extensions"
		},

		'jquery-ui': ['jquery'],
		'jquery.download': ['jquery'],
		'jquery.form': ['jquery'],
		'jquery.impromptu': ['jquery'],
		'jquery.jeditable': ['jquery'],
		'jquery.print': ['jquery'],
		'jquery.simplemodal': ['jquery'],
		'jquery.tablescroll': ['jquery'],
		'jquery.treeTable': ['jquery'],
		'jquery.url': ['jquery'],
		
		'codemirror.mode.javascript': ['codemirror'],
		'codemirror.util.dialog': ['codemirror'],
		'codemirror.util.searchcursor': ['codemirror'],
		'codemirror.util.search': ['codemirror.util.searchcursor', 'codemirror.util.dialog'],
		'codemirror.util.match-highlighter': ['codemirror.util.searchcursor'],
		'codemirror.util.simple-hint': ['codemirror'],
		'codemirror.util.javascript-hint': ['codemirror']
	}
});

require(["require",
	 "jquery",
	 "json",
	 "raphael",
     "extensions_jquery",
	 "extensions_raphael",

	 "jquery-ui",
	 "jquery.download",
	 "jquery.form",
	 "jquery.impromptu",
	 "jquery.jeditable",
	 "jquery.print",
	 "jquery.simplemodal",
	 "jquery.tablescroll",
	 "jquery.treeTable",
	 "jquery.url",

	 "codemirror",
	 "codemirror.mode.javascript",
	 "codemirror.util.dialog",
	 "codemirror.util.searchcursor",
	 "codemirror.util.search",
	 "codemirror.util.match-highlighter",
	 "codemirror.util.simple-hint",
	 "codemirror.util.javascript-hint",
	 
	 "modeler-plugins",
	 "extensions",
	 "m_extensionManager",
	 "m_modelerViewLayoutManager",
	 "m_dataTypeSelector",
	 "m_parameterDefinitionsPanel",

	 "m_logger",
	 "m_utils",
	 "m_communicationController",
	 "m_canvasManager",
	 "m_toolbarManager",
	 "m_urlUtils",
	 "m_constants",
	 "m_user",
	 "m_command",
	 "m_commandsController",
	 "m_accessPoint",
	 "m_diagram",
	 "m_modelerCanvasController",
	 "m_propertiesPanel",
	 "m_activityPropertiesPanel",
	 "m_gatewayPropertiesPanel",
	 "m_eventPropertiesPanel",
	 "m_dataFlowPropertiesPanel",
	 "m_controlFlowPropertiesPanel",
	 "m_command",
	 "m_drawable",
	 "m_symbol",
	 "m_poolSymbol",
	 "m_swimlaneSymbol",
	 "m_activitySymbol",
	 "m_eventSymbol",
	 "m_gatewaySymbol",
	 "m_dataSymbol",
	 "m_testSymbol",
	 "m_connection",
	 ], function (require) {

		// after modeler-plugins are loaded the properly initialized module loader will be injected into m_extensionManager

		var layoutManager = require("m_modelerViewLayoutManager");
		layoutManager.initialize(
				$.url.setUrl(window.location.search).param("fullId"));
});

