/**
 * @author Marc Gille
 * @author Robert Sauer
 */
require.config({
	paths : {
		'jquery': 'libs/jquery/jquery-1.7.2',

		'jquery-ui': 'libs/jquery/plugins/jquery-ui-1.8.19.custom.min',
		'jquery.tablescroll': 'libs/jquery/plugins/jquery.tablescroll',
		'jquery.treeTable': 'libs/jquery/plugins/jquery.treeTable',
		'jquery.url': 'libs/jquery/plugins/jquery.url',

		'extensions': '../../../services/rest/bpm-modeler/config/ui/extensions.js?p=extensions',
	},
	shim: {
		'jquery-ui': ['jquery'],
		'jquery.tablescroll': ['jquery'],
		'jquery.treeTable': ['jquery'],
		'jquery.url': ['jquery'],
	}
});

require(["require",
         "jquery",
         "jquery.tablescroll",
         "jquery.treeTable",
		 "jquery.url",
		 "m_utils",
		 "extensions",
		 "m_extensionManager",
		 "m_communicationController",
		 "m_urlUtils",
		 "m_constants",
		 "m_command",
		 "m_commandsController",
		 "m_view",
		 "m_dataView"], function(require) {
	require("m_dataView").initialize(
			jQuery.url.setUrl(window.location.search).param("fullId"));

});
