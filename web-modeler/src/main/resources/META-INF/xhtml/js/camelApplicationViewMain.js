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
	baseUrl: "plugins/",
	paths : {
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
		'ace': ['bpm-modeler/js/libs/ace/ace', 'https://github.com/ajaxorg/ace-builds/blob/master/src/ace'],
		'ckeditor': ['bpm-modeler/js/libs/ckeditor/ckeditor'],
		'common-plugins': '../services/rest/bpm-modeler/config/ui/plugins/common-plugins',
		'i18n' : 'common/InfinityBPMI18N'
	},
	shim: {
		'i18n': {
			exports: "InfinityBPMI18N"
		},
		'jquery-ui': ['jquery'],
		'jquery.form': ['jquery'],
		'jquery.jeditable': ['jquery'],
		'jquery.simplemodal': ['jquery'],
		'jquery.tablescroll': ['jquery'],
		'jquery.treeTable': ['jquery'],
		'jquery.url': ['jquery']
	}
});

require(["require",
         "json",
         "jquery",
         "jquery-ui",
         "jquery.form",
		 "jquery.jeditable",
		 "jquery.simplemodal",
		 "jquery.tablescroll",
		 "jquery.treeTable",
		 "jquery.url",
		 "ace",
		 "ckeditor",
		 "i18n",
		 "common-plugins",
		 "bpm-modeler/js/m_camelApplicationView"],
		 function(require) {

//		require("bpm-modeler/js/m_camelApplicationView").initialize(
//				jQuery.url(window.location.search).param("fullId"));

		require("bpm-modeler/js/m_camelApplicationView").initialize(BridgeUtils.View.getActiveViewParams().param("fullId"));
});

