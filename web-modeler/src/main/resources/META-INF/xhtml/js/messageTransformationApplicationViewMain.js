/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Marc Gille
 * @author Robert Sauer
 */

require.config({
	baseUrl: "../../../",
	paths : {
		'jquery' : ['bpm-modeler/js/libs/jquery/jquery-1.7.2', '//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min'],
		'json' : ['bpm-modeler/js/libs/json/json2', '//cdnjs.cloudflare.com/ajax/libs/json2/20110223/json2'],
		'raphael' : ['bpm-modeler/js/libs/raphael/2.0.1/raphael', '//cdnjs.cloudflare.com/ajax/libs/raphael/2.0.1/raphael-min'],
		'angularjs' : ['bpm-modeler/js/libs/angular/angular-1.0.2', '//ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min'],
		'mustache': ['bpm-modeler/js/libs/mustache/mustache', 'https://raw.github.com/janl/mustache.js/6d1954cb5c125c40548c9952efe79a4534c6760a/mustache'],

		'jquery-ui': ['bpm-modeler/js/libs/jquery/plugins/jquery-ui-1.8.19.min', '//ajax.googleapis.com/ajax/libs/jqueryui/1.8.19/jquery-ui.min'],
		'jquery.atmosphere': ['bpm-modeler/js/libs/jquery/plugins/jquery.atmosphere', 'https://raw.github.com/Atmosphere/atmosphere/cc760abedaa3d1f8bd7952c9555f7f40b8f41e2e/modules/jquery/src/main/webapp/jquery/jquery.atmosphere'],
		'jquery.download': ['bpm-modeler/js/libs/jquery/plugins/download.jQuery', 'https://raw.github.com/filamentgroup/jQuery-File-Download/master/jQuery.download'],
		'jquery.jeditable': ['bpm-modeler/js/libs/jquery/plugins/jquery.jeditable', 'https://raw.github.com/tuupola/jquery_jeditable/bae12d99ab991cd915805667ef72b8c9445548e0/jquery.jeditable'],
		'jquery.form': ['bpm-modeler/js/libs/jquery/plugins/jquery.form', 'https://raw.github.com/malsup/form/5d413a0169b673c9ee81d5f458b1c955ff1b8027/jquery.form'],
		'jquery.jstree': ['bpm-modeler/js/libs/jquery/plugins/jquery.jstree', 'https://jstree.googlecode.com/svn-history/r191/trunk/jquery.jstree'],
		'jquery.simplemodal': ['bpm-modeler/js/libs/jquery/plugins/jquery.simplemodal.1.4.1.min', '//simplemodal.googlecode.com/files/jquery.simplemodal.1.4.1.min'],
		'jquery.tablescroll': ['bpm-modeler/js/libs/jquery/plugins/jquery.tablescroll', 'https://raw.github.com/farinspace/jquery.tableScroll/master/jquery.tablescroll'],
		'jquery.treeTable': ['bpm-modeler/js/libs/jquery/plugins/jquery.treeTable', 'https://raw.github.com/ludo/jquery-treetable/master/src/javascripts/jquery.treeTable'],
		'jquery.url': ['bpm-modeler/js/libs/jquery/plugins/jquery.url', 'https://raw.github.com/allmarkedup/jQuery-URL-Parser/4f5254f2519111ad7037d398b2efa61d3cda58d4/jquery.url'],

		'jslint': ['bpm-modeler/js/libs/jslint/jslint', 'https://raw.github.com/douglascrockford/JSLint/996246308b755df665bd6c4f3ae59d655ae0a97e/jslint'],

		'codemirror': ['bpm-modeler/js/libs/codemirror/codemirror-2.34', 'https://raw.github.com/marijnh/CodeMirror/fc17d2d418d50fba292bae4fdcdb8a5bf1102867/lib/codemirror'],
		'codemirror.mode.javascript': ['bpm-modeler/js/libs/codemirror/mode/javascript/javascript', 'https://raw.github.com/marijnh/CodeMirror/fc17d2d418d50fba292bae4fdcdb8a5bf1102867/mode/javascript/javascript'],
		'codemirror.util.dialog': ['bpm-modeler/js/libs/codemirror/util/dialog', 'https://raw.github.com/marijnh/CodeMirror/fc17d2d418d50fba292bae4fdcdb8a5bf1102867/lib/util/dialog'],
		'codemirror.util.javascript-hint': ['bpm-modeler/js/libs/codemirror/util/javascript-hint', 'https://raw.github.com/marijnh/CodeMirror/fc17d2d418d50fba292bae4fdcdb8a5bf1102867/lib/util/javascript-hint'],
		'codemirror.util.match-highlighter': ['bpm-modeler/js/libs/codemirror/util/match-highlighter', 'https://raw.github.com/marijnh/CodeMirror/fc17d2d418d50fba292bae4fdcdb8a5bf1102867/lib/util/match-highlighter'],
		'codemirror.util.search': ['bpm-modeler/js/libs/codemirror/util/search', 'https://raw.github.com/marijnh/CodeMirror/fc17d2d418d50fba292bae4fdcdb8a5bf1102867/lib/util/search'],
		'codemirror.util.searchcursor': ['bpm-modeler/js/libs/codemirror/util/searchcursor', 'https://raw.github.com/marijnh/CodeMirror/fc17d2d418d50fba292bae4fdcdb8a5bf1102867/lib/util/searchcursor'],
		'codemirror.util.simple-hint': ['bpm-modeler/js/libs/codemirror/util/simple-hint', 'https://raw.github.com/marijnh/CodeMirror/fc17d2d418d50fba292bae4fdcdb8a5bf1102867/lib/util/simple-hint'],

		'common-plugins': '../services/rest/bpm-modeler/config/ui/plugins/common-plugins',
		 'i18n' : 'common/InfinityBPMI18N',
		 'ace': ['bpm-modeler/js/libs/ace/ace', 'https://github.com/ajaxorg/ace-builds/blob/master/src/ace']
	},
	shim: {
	    'i18n' : {
			exports : "InfinityBPMI18N"
		 },
		'jquery-ui': ['jquery'],
		'jquery.form': ['jquery'],
		'jquery.jeditable': ['jquery'],
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
		 "jquery-ui",
		 "jquery.form",
		 "jquery.jeditable",
		 "jquery.simplemodal",
		 "jquery.tablescroll",
		 "jquery.treeTable",
		 "jquery.url",
		 "ace",

		 "jslint",

		 "codemirror",
		 "codemirror.mode.javascript",
		 "codemirror.util.dialog",
		 "codemirror.util.searchcursor",
		 "codemirror.util.search",
		 "codemirror.util.match-highlighter",
		 "codemirror.util.simple-hint",
		 "codemirror.util.javascript-hint",

		 "common-plugins",
		 "i18n",
         "bpm-modeler/js/m_messageTransformationApplicationView"],
		 function(require) {
	require("bpm-modeler/js/m_messageTransformationApplicationView").initialize(
			jQuery.url(window.location.search).param("fullId"));
});

