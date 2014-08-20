bpm.portal.reportingRequire.config();

require(
		[ "require", "jquery", "jquery.url", "jquery-ui", "json", "angularjs",
				"jquery.base64", "jquery.jqplot", "barRenderer",
				"bubbleRenderer", "canvasTextRenderer",
				"canvasAxisLabelRenderer", "canvasAxisTickRenderer",
				"categoryAxisRenderer", "dateAxisRenderer", "cursor",
				"highlighter", "trendline", "ohlcRenderer", "pieRenderer", "donutRenderer",
				"pointLabels", "bpm-reporting/js/report/ReportDefinitionController",
				"dataTables", "TableTools",
				"ckeditor", "ace"], function(require, jquery, jqueryUrl, jqueryUi,
				json, angularjs, jqueryBase64, jqueryJqPlot, barRenderer,
				bubbleRenderer, canvasTextRenderer, canvasAxisLabelRenderer,
				canvasAxisTickRenderer, categoryAxisRenderer, dateAxisRenderer,
				cursor, highlighter, trendline, ohlcRenderer, pieRenderer, donutRenderer,
				pointLabels, ReportDefinitionController, dataTables,
				TableTools, CkEditor, ace) {
			jQuery(document).ready(
					function() {
						ReportDefinitionController.create(angularjs);
					});
		});
