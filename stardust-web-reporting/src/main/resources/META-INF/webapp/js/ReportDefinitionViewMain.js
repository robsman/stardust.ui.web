require
		.config({
			baseUrl : "../../",
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
				'angularjs' : [ 'bpm-reporting/js/libs/angular/angular-1.0.2',
						'//ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min' ],
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
				'pointLabels' : [ 'bpm-reporting/js/libs/jqplot/plugins/jqplot.pointLabels.min' ],
				'dataTables' : [ 'bpm-reporting/js/libs/datatables/jquery.dataTables.min' ],
				'TableTools' : [ 'bpm-reporting/js/libs/datatables/extras/TableTools/TableTools.min' ],
				'ckeditor' : [ 'bpm-reporting/js/libs/ckeditor/ckeditor' ],
				'i18n' : 'common/InfinityBPMI18N'
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
				"highlighter", "trendline", "ohlcRenderer", "pieRenderer",
				"pointLabels", "bpm-reporting/js/ReportDefinitionController",
				"dataTables", "TableTools",
				"ckeditor" ], function(require, jquery, jqueryUrl, jqueryUi,
				json, angularjs, jqueryBase64, jqueryJqPlot, barRenderer,
				bubbleRenderer, canvasTextRenderer, canvasAxisLabelRenderer,
				canvasAxisTickRenderer, categoryAxisRenderer, dateAxisRenderer,
				cursor, highlighter, trendline, ohlcRenderer, pieRenderer,
				pointLabels, ReportDefinitionController, dataTables,
				TableTools, CkEditor) {
			jQuery(document).ready(
					function() {
						console.log("===> URL" + window.location);
						console.log("===> Name"
								+ jQuery.url(window.location.search).param(
										"name"));
						console.log("===> Path"
								+ jQuery.url(window.location.search).param(
										"path"));

						ReportDefinitionController.create(angularjs, jQuery
								.url(window.location.search).param("name"),
								jQuery.url(window.location.search)
										.param("path"));
					});
		});
