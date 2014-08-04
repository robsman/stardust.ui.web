var commonRequire = {
	PATHS : {
			'jquery' : ['bpm-modeler/js/libs/jquery/jquery-1.7.2', '//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min'],
			'json' : ['bpm-modeler/js/libs/json/json2', '//cdnjs.cloudflare.com/ajax/libs/json2/20110223/json2'],
			'raphael' : ['bpm-modeler/js/libs/raphael/2.0.1/raphael', '//cdnjs.cloudflare.com/ajax/libs/raphael/2.0.1/raphael-min'],
			'angularjs' : ['bpm-modeler/js/libs/angular/angular-1.0.2', '//ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min'],

			'jquery-ui': ['bpm-modeler/js/libs/jquery/plugins/jquery-ui-1.10.2.min', '//ajax.googleapis.com/ajax/libs/jqueryui/1.10.2/jquery-ui.min'],
			'jquery.download': ['bpm-modeler/js/libs/jquery/plugins/download.jQuery', 'https://raw.github.com/filamentgroup/jQuery-File-Download/master/jQuery.download'],
			'jquery.jeditable': ['bpm-modeler/js/libs/jquery/plugins/jquery.jeditable', 'https://raw.github.com/tuupola/jquery_jeditable/bae12d99ab991cd915805667ef72b8c9445548e0/jquery.jeditable'],
			'jquery.form': ['bpm-modeler/js/libs/jquery/plugins/jquery.form', 'https://raw.github.com/malsup/form/5d413a0169b673c9ee81d5f458b1c955ff1b8027/jquery.form'],
			'jquery.jstree': ['bpm-modeler/js/libs/jquery/plugins/jquery.jstree', 'https://jstree.googlecode.com/svn-history/r191/trunk/jquery.jstree'],
			'jquery.simplemodal': ['bpm-modeler/js/libs/jquery/plugins/jquery.simplemodal.1.4.1.min', '//simplemodal.googlecode.com/files/jquery.simplemodal.1.4.1.min'],
			'jquery.tablescroll': ['bpm-modeler/js/libs/jquery/plugins/jquery.tablescroll', 'https://raw.github.com/farinspace/jquery.tableScroll/master/jquery.tablescroll'],
			'jquery.treeTable': ['bpm-modeler/js/libs/jquery/plugins/jquery.treeTable', 'https://raw.github.com/ludo/jquery-treetable/master/src/javascripts/jquery.treeTable'],
			'jquery.url': ['bpm-modeler/js/libs/jquery/plugins/jquery.url', 'https://raw.github.com/allmarkedup/jQuery-URL-Parser/4f5254f2519111ad7037d398b2efa61d3cda58d4/jquery.url'],
			'jquery.jqprint': ['bpm-modeler/js/libs/jquery/plugins/jquery.jqprint-0.3', 'https://raw.github.com/tanathos/jquery.jqprint/master/jquery.jqprint-0.3'],
			
			'jslint': ['bpm-modeler/js/libs/jslint/jslint', 'https://raw.github.com/douglascrockford/JSLint/996246308b755df665bd6c4f3ae59d655ae0a97e/jslint'],
			'ace': ['bpm-modeler/js/libs/ace/ace', 'https://github.com/ajaxorg/ace-builds/blob/master/src/ace'],
			'ckeditor': ['bpm-modeler/js/libs/ckeditor/ckeditor'],
			
			'modeler-plugins': '../services/rest/bpm-modeler/config/ui/plugins/modeler-plugins',
			'common-plugins': '../services/rest/bpm-modeler/config/ui/plugins/common-plugins',
			'outline-plugins': '../services/rest/bpm-modeler/config/ui/plugins/outline-plugins',
			
			'i18n' : 'common/InfinityBPMI18N'
		},
	SHIM : {
			'angularjs': {
				require: "jquery",
				exports: "angular"
			},
			'json': {
				exports: "JSON"
			},
			'i18n': {
				exports: "InfinityBPMI18N"
			},
			'raphael': {
				exports: 'Raphael'
			},
			'jquery-ui': ['jquery'],
			'jquery.download': ['jquery'],
			'jquery.jeditable': ['jquery'],
			'jquery.form': ['jquery'],
			'jquery.jstree': ['jquery'],
			'jquery.simplemodal': ['jquery'],
			'jquery.tablescroll': ['jquery'],
			'jquery.treeTable': ['jquery'],
			'jquery.url': ['jquery'],
			'jquery.jqprint': ['jquery']
		},
	/*
	 * config : waitSeconds,baseUrl
	 */
	config : function(config) {

		require.config({
			waitSeconds: config.waitSeconds ? config.waitSeconds : 0,
			baseUrl: config.baseUrl? config.baseUrl : "plugins/",
			paths : this.PATHS,
			shim: this.SHIM
		});
	}
};