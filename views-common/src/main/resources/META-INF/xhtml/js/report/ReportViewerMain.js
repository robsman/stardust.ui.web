bpm.portal.reportingRequire.config({baseUrl: "../plugins"});

require(
		[ "require", "jquery", "jquery.url", "jquery-ui", "json", "angularjs",
				"jquery.base64", "jquery.jqplot", "barRenderer",
				"bubbleRenderer", "canvasTextRenderer",
				"canvasAxisLabelRenderer", "canvasAxisTickRenderer",
				"categoryAxisRenderer", "dateAxisRenderer", "cursor",
				"highlighter", "trendline", "ohlcRenderer", "pieRenderer", "donutRenderer",
				"pointLabels", "views-common/js/report/ReportViewerController",
				"dataTables", "TableTools",
				"ckeditor", "ace"], function(require, jquery, jqueryUrl, jqueryUi,
				json, angularjs, jqueryBase64, jqueryJqPlot, barRenderer,
				bubbleRenderer, canvasTextRenderer, canvasAxisLabelRenderer,
				canvasAxisTickRenderer, categoryAxisRenderer, dateAxisRenderer,
				cursor, highlighter, trendline, ohlcRenderer, pieRenderer, donutRenderer,
				pointLabels, ReportViewerController, dataTables,
				TableTools, CkEditor, ace) {
					jQuery(document).ready(
						function() {
							ReportViewerController.create(angularjs, getParam("name"),
									getParam("path"), getParam("documentId"), getParam("viewMode"), {});
						});
						
						function getParam(name) {
							return jQuery.url(window.location.search).param(name);
						}
						
		});
