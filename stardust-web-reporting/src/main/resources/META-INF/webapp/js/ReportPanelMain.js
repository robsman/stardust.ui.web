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
console.log("Initialize");
bpm.portal.reportingRequire.config({baseUrl: "../../"});

console.log("Before require");

require(
		[ "require", "jquery", "jquery.url", "jquery-ui", "json", "angularjs",
				"jquery.base64", "jquery.jqplot", "barRenderer",
				"bubbleRenderer", "canvasTextRenderer",
				"canvasAxisLabelRenderer", "canvasAxisTickRenderer",
				"categoryAxisRenderer", "dateAxisRenderer", "highlighter",
				"ohlcRenderer", "pieRenderer", "enhancedLegendRenderer", "pointLabels",
				"bpm-reporting/js/report/ReportRenderingController", "bpm-reporting/js/report/ReportingService",
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

								console.log("Path: " , path);

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
