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
