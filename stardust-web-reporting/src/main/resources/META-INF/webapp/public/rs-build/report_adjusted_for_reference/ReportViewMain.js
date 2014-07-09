/*require
		.config({
			waitSeconds: 0,
			baseUrl : "plugins/",
			paths : {
				'jquery' : [ '../libs/jquery/jquery-1.7.2-min',
						'//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min' ],
				'jquery-ui' : [
						'../libs/jquery/plugins/jquery-ui-1.10.2.custom.min',
						'//ajax.googleapis.com/ajax/libs/jqueryui/1.10.2/jquery-ui.min' ],
				'json' : [ '../libs/json/json2',
						'//cdnjs.cloudflare.com/ajax/libs/json2/20110223/json2' ],
				'angularjs' : [ '../libs/angular/angular-1.2.11-min',
						'//ajax.googleapis.com/ajax/libs/angular../1.2.11/angular.min' ],
				'jquery.jqplot' : [ '../libs/jqplot/jquery.jqplot.min' ],
				'barRenderer' : [ '../libs/jqplot/plugins/jqplot.barRenderer.min' ],
				'bubbleRenderer' : [ '../libs/jqplot/plugins/jqplot.bubbleRenderer.min' ],
				'canvasTextRenderer' : [ '../libs/jqplot/plugins/jqplot.canvasTextRenderer.min' ],
				'canvasAxisLabelRenderer' : [ '../libs/jqplot/plugins/jqplot.canvasAxisLabelRenderer.min' ],
				'canvasAxisTickRenderer' : [ '../libs/jqplot/plugins/jqplot.canvasAxisTickRenderer.min' ],
				'categoryAxisRenderer' : [ '../libs/jqplot/plugins/jqplot.categoryAxisRenderer.min' ],
				'dateAxisRenderer' : [ '../libs/jqplot/plugins/jqplot.dateAxisRenderer.min' ],
				'cursor' : [ '../libs/jqplot/plugins/jqplot.cursor.min' ],
				'highlighter' : [ '../libs/jqplot/plugins/jqplot.highlighter.min' ],
				'trendline' : [ '../libs/jqplot/plugins/jqplot.trendline.min' ],
				'ohlcRenderer' : [ '../libs/jqplot/plugins/jqplot.ohlcRenderer.min' ],
				'pieRenderer' : [ '../libs/jqplot/plugins/jqplot.pieRenderer.min' ],
				'donutRenderer' : [ '../libs/jqplot/plugins/jqplot.donutRenderer.min' ],
				'pointLabels' : [ '../libs/jqplot/plugins/jqplot.pointLabels.min' ],
				'dataTables' : [ '../libs/datatables/jquery.dataTables.min' ],
				'TableTools' : [ '../libs/datatables/extras/TableTools/TableTools.min' ],
				'i18n' : '../plugins/common/InfinityBPMI18N',
			},
			shim : {
				'jquery-ui' : [ 'jquery' ],
				'angularjs' : {
					require : "jquery",
					exports : "angular"
				},
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
*/
require(
		[ "require", "jquery", "jquery-ui", "json", "angularjs","jquery.jqplot", "barRenderer",
				"bubbleRenderer", "canvasTextRenderer",
				"canvasAxisLabelRenderer", "canvasAxisTickRenderer",
				"categoryAxisRenderer", "dateAxisRenderer", "cursor",
				"highlighter", "trendline", "ohlcRenderer", "pieRenderer", "donutRenderer",
				"pointLabels", "../report/ReportDefinitionController",
				"dataTables"], function(require, jquery, jqueryUi,
				json, angularjs, jqueryJqPlot, barRenderer,
				bubbleRenderer, canvasTextRenderer, canvasAxisLabelRenderer,
				canvasAxisTickRenderer, categoryAxisRenderer, dateAxisRenderer,
				cursor, highlighter, trendline, ohlcRenderer, pieRenderer, donutRenderer,
				pointLabels, ReportDefinitionController, dataTables) {
				jQuery(document).ready(
					function() {
						ReportDefinitionController.create(angularjs);
					});
		});
