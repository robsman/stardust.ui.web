bpm.portal.reportingRequire.config({baseUrl: "../../../"});

require([ "require", "jquery", "angularjs",
		"bpm-reporting/js/ReportStorageController" ], function(require, jQuery,
		angularjs, ReportStorageController) {
	jQuery(document).ready(function() {
		ReportStorageController.create();
	});
});
