commonRequire.config({baseUrl: "../../../"});

require([ "require", "jquery", "angularjs",
		"bpm-reporting/js/ReportStorageController" ], function(require, jquery,
		angularjs, ReportStorageController) {
	jQuery(document).ready(function() {
		ReportStorageController.create();
	});
});
