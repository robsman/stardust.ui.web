
require(
		[ "require", "jquery", "jquery-ui", "json", "angularjs","jquery.jqplot", "barRenderer",
				"bubbleRenderer", "canvasTextRenderer",
				"canvasAxisLabelRenderer", "canvasAxisTickRenderer",
				"categoryAxisRenderer", "dateAxisRenderer", "cursor",
				"highlighter", "trendline", "ohlcRenderer", "pieRenderer", "donutRenderer",
				"enhancedLegendRenderer", "pointLabels", "../report/ReportDefinitionController",
				"dataTables"], function(require, jquery, jqueryUi,
				json, angularjs, jqueryJqPlot, barRenderer,
				bubbleRenderer, canvasTextRenderer, canvasAxisLabelRenderer,
				canvasAxisTickRenderer, categoryAxisRenderer, dateAxisRenderer,
				cursor, highlighter, trendline, ohlcRenderer, pieRenderer, donutRenderer,
				enhancedLegendRenderer, pointLabels, ReportDefinitionController, dataTables) {
			jQuery(document).ready(
					function() {
						ReportDefinitionController.create(angularjs);
					});
		});
