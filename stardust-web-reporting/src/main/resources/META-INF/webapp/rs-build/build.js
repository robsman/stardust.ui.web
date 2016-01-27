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
({
	baseUrl : '../js/libs',
	paths : {
				'jquery' : '../../../../../../../../portal-common/src/main/resources/portal-shell/js/libs/jquery/1.9.1/jquery.min',
				'jquery-ui' : 'jquery/plugins/jquery-ui-1.10.2.custom.min',
				'json' :  'json/json2',
				'angularjs' : '../../../../../../../../portal-common/src/main/resources/portal-shell/js/libs/angular/1.2.11/angular.min',
				'jquery.jqplot' :  'jqplot/jquery.jqplot.min' ,
				'barRenderer' :  'jqplot/plugins/jqplot.barRenderer.min' ,
				'bubbleRenderer' :  'jqplot/plugins/jqplot.bubbleRenderer.min' ,
				'canvasTextRenderer' :  'jqplot/plugins/jqplot.canvasTextRenderer.min' ,
				'canvasAxisLabelRenderer' :  'jqplot/plugins/jqplot.canvasAxisLabelRenderer.min' ,
				'canvasAxisTickRenderer' :  'jqplot/plugins/jqplot.canvasAxisTickRenderer.min' ,
				'categoryAxisRenderer' :  'jqplot/plugins/jqplot.categoryAxisRenderer.min' ,
				'dateAxisRenderer' :  'jqplot/plugins/jqplot.dateAxisRenderer.min' ,
				'cursor' :  'jqplot/plugins/jqplot.cursor.min' ,
				'highlighter' :  'jqplot/plugins/jqplot.highlighter.min' ,
				'trendline' :  'jqplot/plugins/jqplot.trendline.min' ,
				'ohlcRenderer' :  'jqplot/plugins/jqplot.ohlcRenderer.min' ,
				'pieRenderer' :  'jqplot/plugins/jqplot.pieRenderer.min' ,
				'donutRenderer' :  'jqplot/plugins/jqplot.donutRenderer.min' ,
				'enhancedLegendRenderer' : 'jqplot/plugins/jqplot.enhancedLegendRenderer.min',				
				'pointLabels' :  'jqplot/plugins/jqplot.pointLabels.min' ,
				'dataTables' :  'datatables/jquery.dataTables.min' ,
				'I18NUtils' : '../report/I18NUtils'
			},
			shim : {
				'jquery-ui' : [ 'jquery' ],
				'angularjs' : {
					deps : ["jquery"],
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
				'enhancedLegendRenderer' : [ 'jquery.jqplot' ],
				'pointLabels' : [ 'jquery.jqplot' ],
				'dataTables' : [ 'jquery' ],
			},
	name : '../report/ReportViewMain',
	out : "main-built.js"
})