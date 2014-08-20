require
		.config({
			waitSeconds: 0,
			baseUrl : "../plugins/",
			paths : {
				'jquery' : [ 'bpm-reporting/js/libs/jquery/jquery-1.7.2',
						'//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min' ],
				'jquery.url' : [
						'bpm-reporting/js/libs/jquery/plugins/jquery.url',
						'https://raw.github.com/allmarkedup/jQuery-URL-Parser/4f5254f2519111ad7037d398b2efa61d3cda58d4/jquery.url' ],
				'jquery-ui' : [
						'bpm-reporting/js/libs/jquery/plugins/jquery-ui-1.10.2.custom.min',
						'//ajax.googleapis.com/ajax/libs/jqueryui/1.10.2/jquery-ui.min' ],
				'json' : [ 'bpm-reporting/js/libs/json/json2',
						'//cdnjs.cloudflare.com/ajax/libs/json2/20110223/json2' ],
				'angularjs' : [ 'bpm-reporting/js/libs/angular/angular-1.2.11',
						'//ajax.googleapis.com/ajax/libs/angularbpm-reporting/js/1.2.11/angular.min' ],
				'jquery.base64' : [
						'bpm-reporting/js/libs/jquery/plugins/jquery.base64',
						'' ],
				'jquery.jqplot' : [ 'bpm-reporting/js/libs/jqplot/jquery.jqplot.min' ],
				'barRenderer' : [ 'bpm-reporting/js/libs/jqplot/plugins/jqplot.barRenderer.min' ],
				'bubbleRenderer' : [ 'bpm-reporting/js/libs/jqplot/plugins/jqplot.bubbleRenderer.min' ],
				'canvasTextRenderer' : [ 'bpm-reporting/js/libs/jqplot/plugins/jqplot.canvasTextRenderer.min' ],
				'canvasAxisLabelRenderer' : [ 'bpm-reporting/js/libs/jqplot/plugins/jqplot.canvasAxisLabelRenderer.min' ],
				'canvasAxisTickRenderer' : [ 'bpm-reporting/js/libs/jqplot/plugins/jqplot.canvasAxisTickRenderer.min' ],
				'categoryAxisRenderer' : [ 'bpm-reporting/js/libs/jqplot/plugins/jqplot.categoryAxisRenderer.min' ],
				'dateAxisRenderer' : [ 'bpm-reporting/js/libs/jqplot/plugins/jqplot.dateAxisRenderer.min' ],
				'cursor' : [ 'bpm-reporting/js/libs/jqplot/plugins/jqplot.cursor.min' ],
				'highlighter' : [ 'bpm-reporting/js/libs/jqplot/plugins/jqplot.highlighter.min' ],
				'trendline' : [ 'bpm-reporting/js/libs/jqplot/plugins/jqplot.trendline.min' ],
				'ohlcRenderer' : [ 'bpm-reporting/js/libs/jqplot/plugins/jqplot.ohlcRenderer.min' ],
				'pieRenderer' : [ 'bpm-reporting/js/libs/jqplot/plugins/jqplot.pieRenderer.min' ],
				'donutRenderer' : [ 'bpm-reporting/js/libs/jqplot/plugins/jqplot.donutRenderer.min' ],
				'pointLabels' : [ 'bpm-reporting/js/libs/jqplot/plugins/jqplot.pointLabels.min' ],
				'dataTables' : [ 'bpm-reporting/js/libs/datatables/jquery.dataTables.min' ],
				'TableTools' : [ 'bpm-reporting/js/libs/datatables/extras/TableTools/TableTools.min' ],
				'ckeditor' : [ 'bpm-reporting/js/libs/ckeditor/ckeditor' ],
				'i18n' : '../plugins/common/InfinityBPMI18N',
		      'ace': ['bpm-reporting/js/libs/ace/ace', 'https://github.com/ajaxorg/ace-builds/blob/master/src/ace']
			},
			shim : {
				'jquery.url' : [ 'jquery' ],
				'jquery-ui' : [ 'jquery' ],
				'angularjs' : {
					require : "jquery",
					exports : "angular"
				},
				'i18n' : {
					exports : "InfinityBPMI18N"
				},
				'jquery.base64' : [ 'jquery' ],
				'jquery.jqplot' : [ 'jquery' ],
				'barRenderer' : [ 'jquery.jqplot' ],
				'bubbleRenderer' : [ 'jquery.jqplot' ],
				'canvasTextRenderer' : [ 'jquery.jqplot' ],
				'canvasAxisLabelRenderer' : [ 'jquery.jqplot' ],
				'canvasAxisTickRenderer' : [ 'jquery.jqplot' ],
				'categoryAxisRenderer' : [ 'jquery.jqplot' ],
				'dateAxisRenderer' : [ 'jquery.jqplot' ],
				'cursor' : [ 'jquery.jqplot' ],
				'highlighter' : [ 'jquery.jqplot' ],
				'trendline' : [ 'jquery.jqplot' ],
				'ohlcRenderer' : [ 'jquery.jqplot' ],
				'pieRenderer' : [ 'jquery.jqplot' ],
				'donutRenderer' : [ 'jquery.jqplot' ],
				'pointLabels' : [ 'jquery.jqplot' ],
				'dataTables' : [ 'jquery' ],
				'TableTools' : [ 'dataTables' ]
			}
		});

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
