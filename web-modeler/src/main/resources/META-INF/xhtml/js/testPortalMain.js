/**
 * @author Robert Sauer
 */

require.config({
	paths : {
		'jquery' : 'libs/jquery/jquery-1.7.2',
		'json' :'libs/json/json2',
		'raphael' : 'libs/raphael/2.0.1/raphael',

		'jquery-ui': 'libs/jquery/plugins/jquery-ui-1.8.19.custom.min',
		'jquery.download': 'libs/jquery/plugins/download.jQuery',
		'jquery.form': 'libs/jquery/plugins/jquery.form',
		'jquery.impromptu': 'libs/jquery/plugins/jquery-impromptu.3.1.min',
		'jquery.jstree': 'libs/jquery/plugins/jquery.jstree',
		'jquery.simplemodal': 'libs/jquery/plugins/jquery.simplemodal.1.4.1.min',
		'jquery.tablescroll': 'libs/jquery/plugins/jquery.tablescroll',
		'jquery.treeTable': 'libs/jquery/plugins/jquery.treeTable',
		'jquery.url': 'libs/jquery/plugins/jquery.url'
	},
	shim: {
		'json': {
			exports: "JSON"
		},
		'raphael': {
			exports: "Raphael"
		},

		'jquery-ui': ['jquery'],
		'jquery.download': ['jquery'],
		'jquery.form': ['jquery'],
		'jquery.impromptu': ['jquery'],
		'jquery.jstree': ['jquery'],
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
		 "jquery.jstree",
		 "jquery.simplemodal",
		 "jquery.tablescroll",
		 "jquery.treeTable",
		 "jquery.url",

		 "json",
		 "raphael",

		 "m_utils",
		 "m_communicationController",
		 "testExtensions",

		 // TODO Remove dependency
		 "m_testViewManager",
		 "m_roleView",
		 "m_organizationView",
		 "m_dataView",
		 "m_messageTransformationApplicationView",
		 "m_camelApplicationView",
		 "m_xsdStructuredDataTypeView",
		 "m_modelerViewLayoutManager",		 
		 "m_urlUtils",
		 "m_constants",
		 "m_user",
		 "m_outline"
], function(require) {
	require('m_outline').init();
});

