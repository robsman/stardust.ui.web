'use strict';

define('reportControllers',
		[
		 "reportApp",
		 "m_model",
		 "m_utils"
		],
		function(app, m_model, m_utils) {

        // modelId is injected by angular, see reportApp.js
	var reportController = app.modelReport.controller('ModelReportCtrl', function ModelReportCtrl($scope, modelId) {

		m_model.loadModels();

        var model = m_model.findModel(modelId);

        // copy immediately accessible attributes
		jQuery.extend($scope, model);

        // add additional, report relevant attributes
		$scope.modelId = modelId;
	});

    // modelId and processId are injected by angular, see reportApp.js
	var processReportController = app.modelReport.controller('ProcessReportCtrl', function ProcessReportCtrl($scope, modelId, processId) {

		m_model.loadModels();

		var process = m_model.findModel(modelId).processes[processId];

        // copy immediately accessible attributes
		jQuery.extend($scope, process);

        // add additional, report relevant attributes

		$scope.modelId = modelId;
		$scope.processId = processId;

		$scope.annotations = [];
		if (process.attributes["documentation:annotations"]) {
			var annotations = jQuery.parseJSON(process.attributes["documentation:annotations"]);
			for ( var n = 0; n < annotations.length; ++n) {
				var annotation = annotations[n];

				$scope.annotations.push({
					timestamp: m_utils.formatDate(annotations.timestamp, "n/j/Y  H:i:s"),
					userFirstName: annotation.userFirstName,
					userLastName: annotation.userLastName,
					content: anotation.content,
				});
			}
		}
	});

	return {
		modelReportCtrl: reportController,
		processReportCtrl: processReportController,
	};
});
