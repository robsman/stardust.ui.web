/**
 * @author Marc Gille
 * @author Robert Sauer
 */

require.config({
	paths : {
		'jquery' : ['libs/jquery/jquery-1.7.2', '//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min'],
		'json' : ['libs/json/json2', '//cdnjs.cloudflare.com/ajax/libs/json2/20110223/json2'],
		'raphael' : ['libs/raphael/2.0.1/raphael', '//cdnjs.cloudflare.com/ajax/libs/raphael/2.0.1/raphael-min'],
		'angularjs' : ['libs/angular/angular-1.0.2', '//ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min'],
		'mustache': ['libs/mustache/mustache', 'https://raw.github.com/janl/mustache.js/6d1954cb5c125c40548c9952efe79a4534c6760a/mustache'],

		'jquery-ui': ['libs/jquery/plugins/jquery-ui-1.8.19.custom.min', '//ajax.googleapis.com/ajax/libs/jqueryui/1.8.19/jquery-ui.min'],
		'jquery.atmosphere': ['libs/jquery/plugins/jquery.atmosphere', 'https://raw.github.com/Atmosphere/atmosphere/cc760abedaa3d1f8bd7952c9555f7f40b8f41e2e/modules/jquery/src/main/webapp/jquery/jquery.atmosphere'],
		'jquery.download': ['libs/jquery/plugins/download.jQuery', 'https://raw.github.com/filamentgroup/jQuery-File-Download/master/jQuery.download'],
		'jquery.jeditable': ['libs/jquery/plugins/jquery.jeditable', 'https://raw.github.com/tuupola/jquery_jeditable/bae12d99ab991cd915805667ef72b8c9445548e0/jquery.jeditable'],
		'jquery.form': ['libs/jquery/plugins/jquery.form', 'https://raw.github.com/malsup/form/5d413a0169b673c9ee81d5f458b1c955ff1b8027/jquery.form'],
		'jquery.impromptu': ['libs/jquery/plugins/jquery-impromptu.3.1.min', 'https://raw.github.com/trentrichardson/jQuery-Impromptu/5a7daa5af8fb56a2e07f2eeced396171a20e9bc9/jquery-impromptu'],
		'jquery.jstree': ['libs/jquery/plugins/jquery.jstree', 'https://jstree.googlecode.com/svn-history/r191/trunk/jquery.jstree'],
		'jquery.simplemodal': ['libs/jquery/plugins/jquery.simplemodal.1.4.1.min', '//simplemodal.googlecode.com/files/jquery.simplemodal.1.4.1.min'],
		'jquery.tablescroll': ['libs/jquery/plugins/jquery.tablescroll', 'https://raw.github.com/farinspace/jquery.tableScroll/master/jquery.tablescroll'],
		'jquery.treeTable': ['libs/jquery/plugins/jquery.treeTable', 'https://raw.github.com/ludo/jquery-treetable/f98c6d07a02cb48052e9d4e033ce7dcdf64218e1/src/javascripts/jquery.treeTable'],
		'jquery.url': ['libs/jquery/plugins/jquery.url', 'https://raw.github.com/allmarkedup/jQuery-URL-Parser/472315f02afbfd7193184300cc381163e19b4a16/jquery.url'],

		'common-plugins': '../../../services/rest/bpm-modeler/config/ui/plugins/common-plugins',
	},
	shim: {
		'jquery-ui': ['jquery'],
		'jquery.download': ['jquery'],
		'jquery.form': ['jquery'],
		'jquery.impromptu': ['jquery'],
		'jquery.jeditable': ['jquery'],
		'jquery.simplemodal': ['jquery'],
		'jquery.tablescroll': ['jquery'],
		'jquery.treeTable': ['jquery'],
		'jquery.url': ['jquery'],
	}
});

require(["require",
         "jquery",
         "extensions_jquery",

         "jquery-ui",
         "jquery.download",
         "jquery.form",
         "jquery.impromptu",
         "jquery.jeditable",
         "jquery.simplemodal",
         "jquery.tablescroll",
         "jquery.treeTable",
         "jquery.url",

         "common-plugins",
         "m_utils",
         "m_communicationController",
         "m_urlUtils",
         "m_constants",
         "m_command",
         "m_commandsController",
         "m_view",
         "m_organizationView"],
         function (require) {
	require("m_organizationView").initialize(
			jQuery.url.setUrl(window.location.search).param("fullId"));
});
