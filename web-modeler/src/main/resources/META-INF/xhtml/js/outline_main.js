/**
 * @author Omkar.Patil
 */

require.config({
	paths : {
		'jquery' : 'libs/jquery/jquery-1.7.2',
		'raphael' : 'libs/raphael/2.0.1/raphael',

		'jquery-ui': 'libs/jquery/plugins/jquery-ui-1.8.19.custom.min',
		'jquery.download': 'libs/jquery/plugins/download.jQuery',
		'jquery.form': 'libs/jquery/plugins/jquery.form',
		'jquery.impromptu': 'libs/jquery/plugins/jquery-impromptu.3.1.min',
		'jquery.jstree': 'libs/jquery/plugins/jquery.jstree',
		'jquery.simplemodal': 'libs/jquery/plugins/jquery.simplemodal.1.4.1.min',
		'jquery.url': 'libs/jquery/plugins/jquery.url',
	},
	shim: {
		'raphael': {
			exports: "Raphael"
		},

		'jquery-ui': ['jquery'],
		'jquery.download': ['jquery'],
		'jquery.form': ['jquery'],
		'jquery.impromptu': ['jquery'],
		'jquery.jstree': ['jquery'],
		'jquery.simplemodal': ['jquery'],
		'jquery.url': ['jquery'],
	}
});

define([
         "jquery",
		 "extensions_jquery",
		 "jquery-ui",
		 "jquery.impromptu",
		 "jquery.download",
		 "jquery.form",
		 "jquery.simplemodal",
		 "jquery.url",
		 "jquery.jstree",
		 "m_utils",
		 "m_communicationController",
		 "extensions",

		 "m_jsfViewManager",

		 "m_urlUtils",
		 "m_constants",
		 "m_user",
		 "m_outline"
], function() {
	var outline = require('m_outline');
	outline.init();
});

