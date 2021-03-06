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
bpm.portal.reportingRequire.config({baseUrl: "../../"});

require(
		[ "require", "jquery", "jquery.url", "jquery-ui", "json", "angularjs",
				"jquery.base64", "jquery.jqplot", "barRenderer",
				"bubbleRenderer", "canvasTextRenderer",
				"canvasAxisLabelRenderer", "canvasAxisTickRenderer",
				"categoryAxisRenderer", "dateAxisRenderer", "cursor",
				"highlighter", "trendline", "ohlcRenderer", "pieRenderer", "donutRenderer", "enhancedLegendRenderer",
				"pointLabels", "bpm-reporting/js/ReportDefinitionController",
				"dataTables", "TableTools",
				"ckeditor", "ace", "bpm-reporting/js/autocomplete/autocomplete"], function(require, jquery, jqueryUrl, jqueryUi,
				json, angularjs, jqueryBase64, jqueryJqPlot, barRenderer,
				bubbleRenderer, canvasTextRenderer, canvasAxisLabelRenderer,
				canvasAxisTickRenderer, categoryAxisRenderer, dateAxisRenderer,
				cursor, highlighter, trendline, ohlcRenderer, pieRenderer, donutRenderer, enhancedLegendRenderer, 
				pointLabels, ReportDefinitionController, dataTables,
				TableTools, CkEditor, ace, autocomplete) {
			jQuery(document).ready(
					function() {
						console.log("URL", window.location);
						console.log("Name", jQuery.url(window.location.search).param(
										"name"));
						console.log("Path", jQuery.url(window.location.search).param(
										"path"));

						 //initialize Options
				        var options = {};
				        // directive modules to be loaded 
				        options.directives = [autocomplete];
				        
						ReportDefinitionController.create(angularjs, 
						      jQuery.url(window.location.search)
                           .param("reportUID"), jQuery
								.url(window.location.search).param("name"),
								jQuery.url(window.location.search)
										.param("path"), jQuery.url(window.location.search)
                              .param("isClone"), options);
					});
		});
