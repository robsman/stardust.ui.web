/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
if (!window.bpm) {
	bpm = {};
}

if (!window.bpm.portal) {
	bpm.portal = {};
}

(function() {
	  if (!window.console) {
	    window.console = {};
	  }
	  // union of Chrome, FF, IE, and Safari console methods
	  var m = [
	    "log", "info", "warn", "error", "debug", "trace", "dir", "group",
	    "groupCollapsed", "groupEnd", "time", "timeEnd", "profile", "profileEnd",
	    "dirxml", "assert", "count", "markTimeline", "timeStamp", "clear"
	  ];
	  // define undefined methods as noops to prevent errors
	  for (var i = 0; i < m.length; i++) {
	    if (!window.console[m[i]]) {
	      window.console[m[i]] = function() {};
	    }    
	  } 
	})();

if (!window.bpm.portal.reportingRequire) {
	bpm.portal.reportingRequire = {
		PATHS : {
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
			'angularjs' : [
					'../portal-shell/js/libs/angular/1.2.11/angular',
					'//ajax.googleapis.com/ajax/libs/angularjs/1.2.11/angular.min' ],
			'jquery.base64' : [
					'bpm-reporting/js/libs/jquery/plugins/jquery.base64',
					'' ],
			'jquery.jqplot' : [ 'bpm-reporting/js/libs/jqplot/jquery.jqplot.min' ],
			'jquery.jstree' : [
					'bpm-reporting/js/libs/jquery/plugins/jquery.jstree',
					'https://jstree.googlecode.com/svn-history/r191/trunk/jquery.jstree' ],
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
			'enhancedLegendRenderer' : [ 'bpm-reporting/js/libs/jqplot/plugins/jqplot.enhancedLegendRenderer.min' ],
			'pointLabels' : [ 'bpm-reporting/js/libs/jqplot/plugins/jqplot.pointLabels.min' ],
			'dataTables' : [ 'bpm-reporting/js/libs/datatables/jquery.dataTables.min' ],
			'TableTools' : [ 'bpm-reporting/js/libs/datatables/extras/TableTools/TableTools.min' ],
			'ckeditor' : [ 'bpm-reporting/js/libs/ckeditor/ckeditor' ],
			'i18n' : 'common/InfinityBPMI18N',
			'ace' : [ 'bpm-reporting/js/libs/ace/ace',
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