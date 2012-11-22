/**
 * @author Omkar.Patil
 * @author Robert Sauer
 */

require.config({
		baseUrl: "../../",
		paths : {
		'jquery' : ['bpm-modeler/js/libs/jquery/jquery-1.7.2', '//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min'],
		'json' : ['bpm-modeler/js/libs/json/json2', '//cdnjs.cloudflare.com/ajax/libs/json2/20110223/json2'],
		'raphael' : ['bpm-modeler/js/libs/raphael/2.0.1/raphael', '//cdnjs.cloudflare.com/ajax/libs/raphael/2.0.1/raphael-min'],
		'angularjs' : ['bpm-modeler/js/libs/angular/angular-1.0.2', '//ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min'],
		'mustache': ['bpm-modeler/js/libs/mustache/mustache', 'https://raw.github.com/janl/mustache.js/6d1954cb5c125c40548c9952efe79a4534c6760a/mustache'],

		'jquery-ui': ['bpm-modeler/js/libs/jquery/plugins/jquery-ui-1.8.19.min', '//ajax.googleapis.com/ajax/libs/jqueryui/1.8.19/jquery-ui.min'],
		'jquery.atmosphere': ['bpm-modeler/js/libs/jquery/plugins/jquery.atmosphere', 'https://raw.github.com/Atmosphere/atmosphere/e6b2a1730d5c94e5d699f7656a76d1439c85889c/modules/jquery/src/main/webapp/jquery/jquery.atmosphere'],
		'jquery.download': ['bpm-modeler/js/libs/jquery/plugins/download.jQuery', 'https://raw.github.com/filamentgroup/jQuery-File-Download/master/jQuery.download'],
		'jquery.jeditable': ['bpm-modeler/js/libs/jquery/plugins/jquery.jeditable', 'https://raw.github.com/tuupola/jquery_jeditable/bae12d99ab991cd915805667ef72b8c9445548e0/jquery.jeditable'],
		'jquery.form': ['bpm-modeler/js/libs/jquery/plugins/jquery.form', 'https://raw.github.com/malsup/form/5d413a0169b673c9ee81d5f458b1c955ff1b8027/jquery.form'],
		'jquery.impromptu': ['bpm-modeler/js/libs/jquery/plugins/jquery-impromptu.3.1.min', 'https://raw.github.com/trentrichardson/jQuery-Impromptu/5a7daa5af8fb56a2e07f2eeced396171a20e9bc9/jquery-impromptu'],
		'jquery.jstree': ['bpm-modeler/js/libs/jquery/plugins/jquery.jstree', 'https://jstree.googlecode.com/svn-history/r191/trunk/jquery.jstree'],
		'jquery.simplemodal': ['bpm-modeler/js/libs/jquery/plugins/jquery.simplemodal.1.4.1.min', '//simplemodal.googlecode.com/files/jquery.simplemodal.1.4.1.min'],
		'jquery.tablescroll': ['bpm-modeler/js/libs/jquery/plugins/jquery.tablescroll', 'https://raw.github.com/farinspace/jquery.tableScroll/master/jquery.tablescroll'],
		'jquery.treeTable': ['bpm-modeler/js/libs/jquery/plugins/jquery.treeTable', 'https://raw.github.com/ludo/jquery-treetable/f98c6d07a02cb48052e9d4e033ce7dcdf64218e1/src/javascripts/jquery.treeTable'],
		'jquery.url': ['bpm-modeler/js/libs/jquery/plugins/jquery.url', 'https://raw.github.com/allmarkedup/jQuery-URL-Parser/472315f02afbfd7193184300cc381163e19b4a16/jquery.url'],

		'outline-plugins': '../services/rest/bpm-modeler/config/ui/plugins/outline-plugins',
		'i18n' : 'common/InfinityBPMI18N'
	},
	shim: {
		'raphael': {
			exports: "Raphael"
		},
		'i18n': {
			exports: "InfinityBPMI18N"
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

define([
         "jquery",
		 "jquery-ui",
		 "jquery.impromptu",
		 "jquery.download",
		 "jquery.form",
		 "jquery.simplemodal",
		 "jquery.url",
		 "jquery.jstree",
		 "outline-plugins",
		 "i18n",
		 "bpm-modeler/js/m_outline"
], function() {
	var outline = require('bpm-modeler/js/m_outline');
	outline.init();
});

