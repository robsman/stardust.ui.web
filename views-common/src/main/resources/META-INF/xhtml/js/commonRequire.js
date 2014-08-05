var commonRequire = {
	PATHS : {
		'jquery' : [ 'views-common/js/libs/jquery/jquery-1.7.2.min',
				'//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min' ],
		'jquery-ui' : [
				'views-common/js/libs/jquery/plugins/jquery-ui-1.10.2.min',
				'//ajax.googleapis.com/ajax/libs/jqueryui/1.10.2/jquery-ui.min' ],
		'jquery-base64' : [ 'common/js/thirdparty/jquery.base64' ],
		'jquery-mobile' : [
				"mobile-workflow/public/js/libs/jquery/plugins/jquery.mobile-1.4.0",
				"//code.jquery.com/mobile/1.4.0/jquery.mobile-1.4.0" ],
		'angularjs' : [ 'views-common/js/libs/angular/angular-1.0.2',
				'//ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min' ],
		'json' : [ 'views-common/js/libs/json/json2',
				'//cdnjs.cloudflare.com/ajax/libs/json2/20110223/json2' ],
		'jquery.form' : [
				'views-common/js/libs/jquery/plugins/jquery.form',
				'https://raw.github.com/malsup/form/5d413a0169b673c9ee81d5f458b1c955ff1b8027/jquery.form' ],
		'bpm.portal.GenericAngularApp' : [ 'processportal/js/GenericAngularApp' ],
		'bpm.portal.GenericController' : [ 'processportal/js/GenericController' ],
		'i18n' : 'common/InfinityBPMI18N',
		'portalSupport' : 'common/js/portalSupport'
	},
	SHIM : {
		'angularjs' : {
			require : "jquery",
			exports : "angular"
		},
		'jquery-ui' : [ 'jquery' ],
		'jquery-base64' : [ 'jquery' ],
		'jquery-mobile' : [ 'jquery' ],
		'jquery.form' : [ 'jquery' ],
		'portalSupport' : [ 'jquery' ],
		'bpm.portal.GenericAngularApp' : [ 'jquery' ],
		'bpm.portal.GenericController' : [ 'jquery' ]
	},
	/*
	 * config : waitSeconds,baseUrl
	 */
	config : function(config) {

		require.config({
			waitSeconds : config.waitSeconds ? config.waitSeconds : 0,
			baseUrl : config.baseUrl ? config.baseUrl : "plugins/",
			paths : this.PATHS,
			shim : this.SHIM
		});
	}
};