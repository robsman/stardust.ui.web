require
		.config({
			waitSeconds: 0,
			baseUrl : "../../",
			paths : {
				'jquery' : [ 'bpm-reporting/js/libs/jquery/jquery-1.7.2',
						'//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min' ],
				'jquery-ui' : [
						'bpm-reporting/js/libs/jquery/plugins/jquery-ui-1.10.2.custom.min',
						'//ajax.googleapis.com/ajax/libs/jqueryui/1.10.2/jquery-ui.min' ],
				'jquery-ui-timepicker' : ['bpm-reporting/js/libs/jquery/plugins/jquery-ui-timepicker/jquery-ui-timepicker-addon.min'],
				'json' : [ 'bpm-reporting/js/libs/json/json2',
						'//cdnjs.cloudflare.com/ajax/libs/json2/20110223/json2' ],
				'angularjs' : [ 'bpm-reporting/js/libs/angular/angular-1.2.11',
						'//ajax.googleapis.com/ajax/libs/angularjs/1.2.11/angular.min' ],
				'jquery.base64' : [
						'bpm-reporting/js/libs/jquery/plugins/jquery.base64',
						'' ],
				'jquery.jstree' : [
						'bpm-reporting/js/libs/jquery/plugins/jquery.jstree',
						'https://jstree.googlecode.com/svn-history/r191/trunk/jquery.jstree' ],
				'i18n' : 'common/InfinityBPMI18N'
			},
			shim : {
				'jquery-ui' : [ 'jquery' ],
				'jquery-ui-timepicker' : ['jquery-ui'],
				'angularjs' : {
					require : "jquery",
					exports : "angular"
				},
				'i18n' : {
					exports : "InfinityBPMI18N"
				},
				'jquery.base64' : [ 'jquery' ],
				'jquery.jstree' : [ 'jquery' ]
			}
		});

require([ "require", "jquery", "jquery-ui", "jquery-ui-timepicker", "json", "angularjs",
		"jquery.base64", "jquery.jstree",
		"bpm-reporting/js/ReportManagementController" ], function(require,
		jquery, jqueryUi, jqueryUiTimepicker, json, angularjs, jqueryBase64, jqueryJsTree,
		ReportManagementController) {
	jQuery(document).ready(function() {
		ReportManagementController.create(angularjs);
	});
});
