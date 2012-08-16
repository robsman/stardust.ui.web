/**
 * @author Marc Gille
 * @author Robert Sauer
 */

require.config({
	paths : {
		'jquery': 'libs/jquery/jquery-1.7.2',
		'json' :'libs/json/json2',

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
	},
	shim: {
		'json': {
			exports: "JSON"
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
	}
// baseUrl: '../js',
// urlArgs: 'v=1.0'
});


require(["require",
         "jquery",
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
         "extensions_jquery",
         "m_utils",
         "m_extensionManager",
		 "extensions",
		 "m_jsfViewManager",
		 "m_communicationController",
		 "m_urlUtils",
		 "m_constants",
		 "m_command",
		 "m_commandsController",
		 "m_view",
		 "m_modelView"], function(require) {
	require("m_modelView").initialize(
			jQuery.url.setUrl(window.location.search).param("modelId"));
});

