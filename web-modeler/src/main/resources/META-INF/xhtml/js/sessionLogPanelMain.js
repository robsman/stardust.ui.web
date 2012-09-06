/**
 * @author Omkar.Patil
 */

require.config({
	paths : {
		'jquery' : 'libs/jquery/jquery-1.7.2',

		'jquery-ui': 'libs/jquery/plugins/jquery-ui-1.8.19.custom.min',
		'jquery.atmosphere': 'libs/jquery/plugins/jquery.atmosphere',
		'jquery.download': 'libs/jquery/plugins/download.jQuery',
		'jquery.form': 'libs/jquery/plugins/jquery.form',
		'jquery.impromptu': 'libs/jquery/plugins/jquery-impromptu.3.1.min',
		'jquery.simplemodal': 'libs/jquery/plugins/jquery.simplemodal.1.4.1.min',
		'jquery.url': 'libs/jquery/plugins/jquery.url',
		'mustache': 'libs/mustache/mustache'
	},
	shim: {
		'jquery-ui': ['jquery'],
		'jquery.atmosphere': ['jquery'],
		'jquery.download': ['jquery'],
		'jquery.form': ['jquery'],
		'jquery.impromptu': ['jquery'],
		'jquery.simplemodal': ['jquery'],
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
		 "jquery.simplemodal",
		 "jquery.url",
		 "m_utils",
		 "m_communicationController",
		 "m_urlUtils",
		 "m_constants",
		 "m_user",
		 "m_session",
		 "m_sessionLogPanel",
		 "m_websocketModel",
		 "m_websocketInvite"
], function(require) {
	require('m_sessionLogPanel').initialize();
});
