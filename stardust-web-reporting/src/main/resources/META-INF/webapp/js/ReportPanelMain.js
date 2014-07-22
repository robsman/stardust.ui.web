console.log("Initialize");

require
		.config({
			waitSeconds: 0,
			baseUrl : "../../",
			paths : {
				'jquery' : [ 'bpm-reporting/public/js/libs/jquery/jquery-1.7.2',
						'//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min' ],
				'jquery.url' : [
						'bpm-reporting/public/js/libs/jquery/plugins/jquery.url',
						'https://raw.github.com/allmarkedup/jQuery-URL-Parser/4f5254f2519111ad7037d398b2efa61d3cda58d4/jquery.url' ],
				'jquery-ui' : [
						'bpm-reporting/public/js/libs/jquery/plugins/jquery-ui-1.10.2.custom.min',
						'//ajax.googleapis.com/ajax/libs/jqueryui/1.10.2/jquery-ui.min' ],
				'jquery.base64' : [ 'bpm-reporting/public/js/libs/jquery/plugins/jquery.base64', '' ],
				'json' : [ 'bpm-reporting/public/js/libs/json/json2',
						'//cdnjs.cloudflare.com/ajax/libs/json2/20110223/json2' ],
				'angularjs' : [ 'bpm-reporting/public/js/libs/angular/angular-1.2.11',
						'//ajax.googleapis.com/ajax/libs/angularjs/1.2.11/angular.min' ],
				'jquery.jqplot' : [ 'bpm-reporting/public/js/libs/jqplot/jquery.jqplot.min' ],
				'barRenderer' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.barRenderer.min' ],
				'bubbleRenderer' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.bubbleRenderer.min' ],
				'canvasTextRenderer' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.canvasTextRenderer.min' ],
				'canvasAxisLabelRenderer' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.canvasAxisLabelRenderer.min' ],
				'canvasAxisTickRenderer' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.canvasAxisTickRenderer.min' ],
				'categoryAxisRenderer' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.categoryAxisRenderer.min' ],
				'dateAxisRenderer' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.dateAxisRenderer.min' ],
				'highlighter' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.highlighter.min' ],
				'ohlcRenderer' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.ohlcRenderer.min' ],
				'pieRenderer' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.pieRenderer.min' ],
				'enhancedLegendRenderer' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.enhancedLegendRenderer.min' ],  
				'pointLabels' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.pointLabels.min' ],
				'dataTables' : [ 'bpm-reporting/public/js/libs/datatables/jquery.dataTables.min' ],
				'TableTools' : [ 'bpm-reporting/public/js/libs/datatables/extras/TableTools/TableTools.min' ],
				'i18n' : 'common/InfinityBPMI18N'
			},
			shim : {
				'jquery-ui' : [ 'jquery' ],
				'jquery.url' : [ 'jquery' ],
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
				'highlighter' : [ 'jquery.jqplot' ],
				'ohlcRenderer' : [ 'jquery.jqplot' ],
				'pieRenderer' : [ 'jquery.jqplot' ],
				'enhancedLegendRenderer' : [ 'jquery.jqplot' ],
				'pointLabels' : [ 'jquery.jqplot' ],
				'dataTables' : [ 'jquery' ],
				'TableTools' : [ 'dataTables' ]
			}
		});

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
