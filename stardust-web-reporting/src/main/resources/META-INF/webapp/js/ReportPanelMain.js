console.log("Initialize");
bpm.portal.reportingRequire.config({baseUrl: "../../"});

console.log("Before require");

require(
		[ "require", "jquery", "jquery.url", "jquery-ui", "json", "angularjs",
				"jquery.base64", "jquery.jqplot", "barRenderer",
				"bubbleRenderer", "canvasTextRenderer",
				"canvasAxisLabelRenderer", "canvasAxisTickRenderer",
				"categoryAxisRenderer", "dateAxisRenderer", "highlighter",
				"ohlcRenderer", "pieRenderer", "enhancedLegendRenderer", "pointLabels",
				"bpm-reporting/public/js/report/ReportRenderingController", "bpm-reporting/public/js/report/ReportingService",
				"dataTables", "TableTools" ],
		function(require, jquery, jqueryUrl, jqueryUi, json, angularjs,
				jqueryBase62, jqueryJqPlot, barRenderer, bubbleRenderer,
				canvasTextRenderer, canvasAxisLabelRenderer,
				canvasAxisTickRenderer, categoryAxisRenderer, dateAxisRenderer,
				highlighter, ohlcRenderer, pieRenderer, enhancedLegendRenderer, pointLabels,
				ReportRenderingController, ReportingService, dataTables,
				TableTools) {
			jQuery(document)
					.ready(
							function() {
								var controller = ReportRenderingController
										.create(angularjs);
								var path = jQuery.url().param('path');

								console.log("Path: " + path);

								var reportingService = ReportingService
										.instance();

								reportingService
										.refreshModelData()
										.done(
												function() {
													reportingService
															.retrieveReportDefinition(
																	path)
															.done(
																	function(
																			reportDefinition) {
																		controller
																				.initialize(reportDefinition);
																		controller
																				.renderReport();
																		controller
																				.updateView();
																	}).fail(
																	function() {
																	});
												});
							});
		});
