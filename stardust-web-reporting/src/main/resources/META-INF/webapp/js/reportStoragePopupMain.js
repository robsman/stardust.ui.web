require
		.config({
			baseUrl : "../../../",
			paths : {
				'jquery' : [
						'bpm-reporting/public/js/libs/jquery/jquery-1.7.2',
						'//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min' ],
				'angularjs' : [
						'bpm-reporting/public/js/libs/angular/angular-1.2.11',
						'//ajax.googleapis.com/ajax/libs/angularjs/1.2.11/angular.min' ],
				'i18n' : 'common/InfinityBPMI18N',
			},
			shim : {
				'angularjs' : {
					require : "jquery",
					exports : "angular"
				}
			}
		});

require([ "require", "jquery", "angularjs",
		"bpm-reporting/js/ReportStorageController" ], function(require, jquery,
		angularjs, ReportStorageController) {
	jQuery(document).ready(function() {
		ReportStorageController.create();
	});
});
