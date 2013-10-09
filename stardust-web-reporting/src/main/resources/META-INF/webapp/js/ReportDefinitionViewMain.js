require
		.config({
			baseUrl : "../",
			paths : {
				'jquery' : [ 'js/libs/jquery/jquery-1.7.2',
						'//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min' ],
				'jquery.url' : [
						'js/libs/jquery/plugins/jquery.url',
						'https://raw.github.com/allmarkedup/jQuery-URL-Parser/4f5254f2519111ad7037d398b2efa61d3cda58d4/jquery.url' ],
				'jquery-ui' : [
						'js/libs/jquery/plugins/jquery-ui-1.10.2.custom.min',
						'//ajax.googleapis.com/ajax/libs/jqueryui/1.10.2/jquery-ui.min' ],
				'json' : [ 'js/libs/json/json2',
						'//cdnjs.cloudflare.com/ajax/libs/json2/20110223/json2' ],
				'angularjs' : [ 'js/libs/angular/angular-1.0.2',
						'//ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min' ],
				'jquery.base64' : [ 'js/libs/jquery/plugins/jquery.base64', '' ],
				'jquery.jqplot' : [ 'js/libs/jqplot/jquery.jqplot.min' ],
				'barRenderer' : [ 'js/libs/jqplot/plugins/jqplot.barRenderer.min' ],
				'bubbleRenderer' : [ 'js/libs/jqplot/plugins/jqplot.bubbleRenderer.min' ],
				'canvasTextRenderer' : [ 'js/libs/jqplot/plugins/jqplot.canvasTextRenderer.min' ],
				'canvasAxisLabelRenderer' : [ 'js/libs/jqplot/plugins/jqplot.canvasAxisLabelRenderer.min' ],
				'canvasAxisTickRenderer' : [ 'js/libs/jqplot/plugins/jqplot.canvasAxisTickRenderer.min' ],
				'categoryAxisRenderer' : [ 'js/libs/jqplot/plugins/jqplot.categoryAxisRenderer.min' ],
				'dateAxisRenderer' : [ 'js/libs/jqplot/plugins/jqplot.dateAxisRenderer.min' ],
				'cursor' : [ 'js/libs/jqplot/plugins/jqplot.cursor.min' ],
				'highlighter' : [ 'js/libs/jqplot/plugins/jqplot.highlighter.min' ],
				'trendline' : [ 'js/libs/jqplot/plugins/jqplot.trendline.min' ],
				'ohlcRenderer' : [ 'js/libs/jqplot/plugins/jqplot.ohlcRenderer.min' ],
				'pieRenderer' : [ 'js/libs/jqplot/plugins/jqplot.pieRenderer.min' ],
				'pointLabels' : [ 'js/libs/jqplot/plugins/jqplot.pointLabels.min' ],
				'dataTables' : [ 'js/libs/datatables/jquery.dataTables.min' ],
				'TableTools' : [ 'js/libs/datatables/extras/TableTools/TableTools.min' ],
				'ckeditor' : [ 'js/libs/ckeditor/ckeditor' ]
			},
			shim : {
				'jquery.url' : [ 'jquery' ],
				'jquery-ui' : [ 'jquery' ],
				'angularjs' : {
					require : "jquery",
					exports : "angular"
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
				"canvasAxisLabelRenderer", "canvasAxisTickRenderer", "categoryAxisRenderer",
				"dateAxisRenderer", "cursor", "highlighter", "trendline",
				"ohlcRenderer", "pieRenderer", "pointLabels",
				"js/ReportDefinitionController", "dataTables", "TableTools",
				"ckeditor" ], function(require, jquery, jqueryUrl, jqueryUi, json,
				angularjs, jqueryBase64, jqueryJqPlot, barRenderer,
				bubbleRenderer, canvasTextRenderer, canvasAxisLabelRenderer, canvasAxisTickRenderer,
				categoryAxisRenderer, dateAxisRenderer, cursor, highlighter,
				trendline, ohlcRenderer, pieRenderer, pointLabels,
				ReportDefinitionController, dataTables, TableTools, ChkEditor) {
			jQuery(document).ready(
					function() {
						console.log("===> URL" + window.location);
						
						ReportDefinitionController.create(angularjs, jQuery
								.url(window.location.search).param("name"), jQuery
								.url(window.location.search).param("path"));
					});
		});
