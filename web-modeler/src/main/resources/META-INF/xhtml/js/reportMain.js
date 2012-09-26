'use strict';

/**
 * @author Robert Sauer
 */

require.config({
	paths : {
		'jquery' : 'libs/jquery/jquery-1.7.2',
		'angularjs' : 'libs/angular/angular-1.0.2',
		'jquery-ui': 'libs/jquery/plugins/jquery-ui-1.8.19.custom.min',
		'jquery.download': 'libs/jquery/plugins/download.jQuery',
		'jquery.form': 'libs/jquery/plugins/jquery.form',
		'jquery.impromptu': 'libs/jquery/plugins/jquery-impromptu.3.1.min',
		'jquery.jstree': 'libs/jquery/plugins/jquery.jstree',
		'jquery.simplemodal': 'libs/jquery/plugins/jquery.simplemodal.1.4.1.min',
		'jquery.url': 'libs/jquery/plugins/jquery.url',

		'extensions': '../../../services/rest/bpm-modeler/config/ui/extensions.js?p=extensions',
	},
	shim: {
		'angularjs': {
			require: "jquery",
			exports: "angular",
		},
		'jquery-ui': ['jquery'],
		'jquery.download': ['jquery'],
		'jquery.form': ['jquery'],
		'jquery.impromptu': ['jquery'],
		'jquery.jstree': ['jquery'],
		'jquery.simplemodal': ['jquery'],
		'jquery.url': ['jquery']
	}
});

define('reportMain',[
		 "reportApp",
		 "jquery",
		 "reportControllers"
], function(reportApp, jQuery) {
	jQuery(document).ready(function() {
		reportApp.init();
	});
});

