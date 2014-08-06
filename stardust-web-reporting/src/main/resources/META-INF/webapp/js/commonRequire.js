if (!window.bpm) {
	bpm = {};
}

if (!window.bpm.portal) {
	bpm.portal = {};
}

if (!window.bpm.portal.reportingRequire) {
	bpm.portal.reportingRequire = {
		PATHS : {
			'jquery' : [ 'bpm-reporting/public/js/libs/jquery/jquery-1.7.2',
					'//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min' ],
			'jquery.url' : [
					'bpm-reporting/public/js/libs/jquery/plugins/jquery.url',
					'https://raw.github.com/allmarkedup/jQuery-URL-Parser/4f5254f2519111ad7037d398b2efa61d3cda58d4/jquery.url' ],
			'jquery-ui' : [
					'bpm-reporting/public/js/libs/jquery/plugins/jquery-ui-1.10.2.custom.min',
					'//ajax.googleapis.com/ajax/libs/jqueryui/1.10.2/jquery-ui.min' ],
			'json' : [ 'bpm-reporting/public/js/libs/json/json2',
					'//cdnjs.cloudflare.com/ajax/libs/json2/20110223/json2' ],
			'angularjs' : [
					'bpm-reporting/public/js/libs/angular/angular-1.2.11',
					'//ajax.googleapis.com/ajax/libs/angularjs/1.2.11/angular.min' ],
			'jquery.base64' : [
					'bpm-reporting/public/js/libs/jquery/plugins/jquery.base64',
					'' ],
			'jquery.jqplot' : [ 'bpm-reporting/public/js/libs/jqplot/jquery.jqplot.min' ],
			'jquery.jstree' : [
					'bpm-reporting/public/js/libs/jquery/plugins/jquery.jstree',
					'https://jstree.googlecode.com/svn-history/r191/trunk/jquery.jstree' ],
			'barRenderer' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.barRenderer.min' ],
			'bubbleRenderer' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.bubbleRenderer.min' ],
			'canvasTextRenderer' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.canvasTextRenderer.min' ],
			'canvasAxisLabelRenderer' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.canvasAxisLabelRenderer.min' ],
			'canvasAxisTickRenderer' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.canvasAxisTickRenderer.min' ],
			'categoryAxisRenderer' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.categoryAxisRenderer.min' ],
			'dateAxisRenderer' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.dateAxisRenderer.min' ],
			'cursor' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.cursor.min' ],
			'highlighter' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.highlighter.min' ],
			'trendline' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.trendline.min' ],
			'ohlcRenderer' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.ohlcRenderer.min' ],
			'pieRenderer' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.pieRenderer.min' ],
			'donutRenderer' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.donutRenderer.min' ],
			'enhancedLegendRenderer' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.enhancedLegendRenderer.min' ],
			'pointLabels' : [ 'bpm-reporting/public/js/libs/jqplot/plugins/jqplot.pointLabels.min' ],
			'dataTables' : [ 'bpm-reporting/public/js/libs/datatables/jquery.dataTables.min' ],
			'TableTools' : [ 'bpm-reporting/public/js/libs/datatables/extras/TableTools/TableTools.min' ],
			'ckeditor' : [ 'bpm-reporting/public/js/libs/ckeditor/ckeditor' ],
			'i18n' : 'common/InfinityBPMI18N',
			'ace' : [ 'bpm-reporting/public/js/libs/ace/ace',
					'https://github.com/ajaxorg/ace-builds/blob/master/src/ace' ]
		},
		SHIM : {
			'angularjs' : {
				require : "jquery",
				exports : "angular"
			},
			'json' : {
				exports : "JSON"
			},
			'i18n' : {
				exports : "InfinityBPMI18N"
			},
			'raphael' : {
				exports : 'Raphael'
			},
			'jquery-ui' : [ 'jquery' ],
			'jquery.download' : [ 'jquery' ],
			'jquery.jeditable' : [ 'jquery' ],
			'jquery.form' : [ 'jquery' ],
			'jquery.jstree' : [ 'jquery' ],
			'jquery.simplemodal' : [ 'jquery' ],
			'jquery.tablescroll' : [ 'jquery' ],
			'jquery.treeTable' : [ 'jquery' ],
			'jquery.url' : [ 'jquery' ],
			'jquery.jqprint' : [ 'jquery' ],
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
			'enhancedLegendRenderer' : [ 'jquery.jqplot' ],
			'pointLabels' : [ 'jquery.jqplot' ],
			'dataTables' : [ 'jquery' ],
			'TableTools' : [ 'dataTables' ]
		},
		/*
		 * config : waitSeconds,baseUrl
		 */
		config : function(config) {
			if(!config){
				config = {};
			}
			
			require.config({
				waitSeconds : config.waitSeconds ? config.waitSeconds : 0,
				baseUrl : config.baseUrl ? config.baseUrl : "plugins/",
				paths : this.PATHS,
				shim : this.SHIM
			});
		}
	};
}