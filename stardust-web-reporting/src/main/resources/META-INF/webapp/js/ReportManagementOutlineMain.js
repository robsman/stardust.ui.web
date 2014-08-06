bpm.portal.reportingRequire.config({baseUrl: "../../"});

require([ "require", "jquery", "jquery-ui", "json", "angularjs",
		"jquery.base64", "jquery.jstree",
		"bpm-reporting/js/ReportManagementController" ], function(require,
		jquery, jqueryUi, json, angularjs, jqueryBase64, jqueryJsTree,
		ReportManagementController) {
	jQuery(document).ready(function() {
		ReportManagementController.create(angularjs);
	});
});
