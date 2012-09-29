/**
 * @author Marc Gille
 * @author Robert Sauer
 */

require.config({
	paths : {
		'jquery': 'libs/jquery/jquery-1.7.2',

		'jquery-ui': 'libs/jquery/plugins/jquery-ui-1.8.19.custom.min',
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

		'extensions': '../../../services/rest/bpm-modeler/config/ui/extensions.js?p=extensions',
	},
	shim: {
		'jquery-ui': ['jquery'],
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
		'codemirror.util.javascript-hint': ['codemirror'],
	}
});

require(["require",
         "jquery",
         "jquery-ui",
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
		 
		 "m_utils",
		 "m_communicationController",
		 "m_urlUtils",
		 "m_constants",
		 "m_command",
		 "m_commandsController",
		 "m_view",
		 "m_modelElementView",
		 "m_messageTransformationApplicationView"],
		 function(require) {
	require("m_messageTransformationApplicationView").initialize(
			jQuery.url.setUrl(window.location.search).param("fullId"));
});

