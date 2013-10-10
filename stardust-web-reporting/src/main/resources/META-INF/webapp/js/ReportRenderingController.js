define(
		[ "js/AngularAdapter", "js/ReportingService" ],
		function(AngularAdapter, ReportingService) {
			return {
				create : function(angular) {
					var controller = new ReportRenderingController();

					if (angular) {
						var angularAdapter = new bpm.portal.AngularAdapter();

						angularAdapter.initialize(angular);

						controller = angularAdapter
								.mergeControllerWithScope(controller);
					}

					return controller;
				}
			};

			/**
			 * 
			 */
			function ReportRenderingController() {
				this.reportingService = ReportingService.instance();

				/**
				 * 
				 */
				ReportRenderingController.prototype.initialize = function(
						report, masterController) {
					this.report = report;

					for ( var id in this.report.parameters) {
						var parameter = this.report.parameters[id];
						var value = jQuery.url().param("parameter_" + id);

						if (value) {
							console.log("Replacing parameter [" 
									+ id + "] = "
									+ this.report.parameters[id]
									+ " by " + value);
						}
						
						parameter.value = value;
					}

					// TODO Angular may have better concepts for
					// modularization/nesting

					if (!masterController) {
						this.masterController = this;
					} else {
						this.masterController = masterController;
					}

					// this.updateView();
				};

				/**
				 * 
				 */
				ReportRenderingController.prototype.getPrimaryObject = function() {
					return this.reportingService.metadata.objects[this.report.dataSet.primaryObject];
				};

				/**
				 * 
				 */
				ReportRenderingController.prototype.getFact = function() {
					return this.getPrimaryObject().facts[this.report.dataSet.fact];
				};

				/**
				 * 
				 */
				ReportRenderingController.prototype.getFirstDimension = function() {
					return this.getPrimaryObject().dimensions[this.report.dataSet.firstDimension];
				};

				/**
				 * 
				 */
				ReportRenderingController.prototype.renderReport = function() {
					var deferred = jQuery.Deferred();
					var self = this;

					if (this.report.layout.type == 'table') {
						this.createTable();
					} else if (this.report.layout.type == 'document') {
						self.reportingService
								.retrieveData(self.report)
								.done(
										function(data) {
											console.log("Data for Document");
											console.log(data);

											var html = "<html ng-app><head>"
													// TODO Read local
													+ "<script src='"
													+ self.reportingService
															.getRootUrl()
													+ "/plugins/bpm-reporting/js/libs/angular/angular-1.0.2.js'>"
													+ "</script>"
													+ "<script>"
													+ "function Controller($scope) {$scope.seriesGroup = "
													+ (data.seriesGroup ? JSON
															.stringify(data.seriesGroup)
															: "null")
													+ ";"
													+ "$scope.recordSet = "
													+ (data.recordSet ? JSON
															.stringify(data.recordSet)
															: "null")
													+ ";"
													+ "$scope.groupIds = "
													+ JSON
															.stringify(data.groupIds)
													+ ";}"
													+ "</script></head><body><div ng-controller='Controller'>"
													+ self.report.layout.document.markup
													+ "</div></body></html>";

											console.log(html);

											var documentFrame = document
													.getElementById('documentFrame');
											var frameDocument = documentFrame.contentDocument
													|| documentFrame.contentWindow.document;

											frameDocument.write(html);
											frameDocument.close();
											deferred.resolve();
										}).fail(function() {
									deferred.reject();
								});
					} else {
						this.createChart();
					}

					return deferred.promise();
				};

				/**
				 * Creates the JQPlot drawing options from chart type defaults
				 * and settings stored with the report definition.
				 */
				ReportRenderingController.prototype.createChartOptions = function(
						groupIds) {
					var chartOptions = {
						series : [],
						seriesDefaults : {
							pointLabels : {},
							trendline : {
								color : '#666666',
								label : '',
								type : 'linear',
								shadow : true,
								lineWidth : 1.5,
								shadowAngle : 45,
								shadowOffset : 1.5,
								shadowDepth : 3,
								shadowAlpha : 0.07
							}
						},
						axes : {
							xaxis : {},
							x2axis : {},
							yaxis : {},
							y2axis : {}
						},
						legend : {
							location : "w"
						},
						highlighter : {},
						cursor : {},
						zoom : {}
					};

					// Copy configuration from Report Definition

					chartOptions.title = this.report.layout.chart.options.title;
					chartOptions.axes.xaxis.label = this.report.layout.chart.options.axes.xaxis.label;
					chartOptions.axes.xaxis.tickOptions = this.report.layout.chart.options.axes.xaxis.tickOptions;
					chartOptions.axes.yaxis.label = this.report.layout.chart.options.axes.yaxis.label;
					chartOptions.axes.yaxis.tickOptions = this.report.layout.chart.options.axes.yaxis.tickOptions;
					chartOptions.legend.show = this.report.layout.chart.options.legend.show;
					chartOptions.legend.location = this.report.layout.chart.options.legend.location;
					chartOptions.highlighter.show = this.report.layout.chart.options.highlighter.show;
					chartOptions.cursor.show = this.report.layout.chart.options.cursor.show;
					chartOptions.cursor.zoom = this.report.layout.chart.options.cursor.zoom;
					chartOptions.seriesDefaults.showMarker = this.report.layout.chart.options.seriesDefaults.showMarker;
					chartOptions.seriesDefaults.markerOptions = this.report.layout.chart.options.seriesDefaults.markerOptions;
					chartOptions.seriesDefaults.shadow = this.report.layout.chart.options.seriesDefaults.shadow;
					chartOptions.seriesDefaults.pointLabels.show = this.report.layout.chart.options.seriesDefaults.pointLabels.show;
					chartOptions.seriesDefaults.trendline.show = this.report.layout.chart.options.seriesDefaults.trendline.show;

					// TODO There is more

					if (this.report.layout.chart.type === this.reportingService.metadata.chartTypes.xyPlot.id) {
						console.log(this.getFirstDimension().type);
						console
								.log(this.getFirstDimension().type == this.reportingService.metadata.timestampType);

						// Use default series renderer

						if (this.getFirstDimension().type == this.reportingService.metadata.timestampType) {
							chartOptions.axes.xaxis.renderer = jQuery.jqplot.DateAxisRenderer;
						} else if (this.getFirstDimension().type == this.reportingService.metadata.enumerationType) {
							chartOptions.axes.xaxis.renderer = jQuery.jqplot.CategoryAxisRenderer;
						}

						chartOptions.axes.xaxis.tickRenderer = jQuery.jqplot.CanvasAxisTickRenderer;
						chartOptions.axes.yaxis.tickRenderer = jQuery.jqplot.CanvasAxisTickRenderer;
					} else if (this.report.layout.chart.type === this.reportingService.metadata.chartTypes.candlestickChart.id) {
						chartOptions.axes.xaxis.renderer = $.jqplot.DateAxisRenderer;
						chartOptions.series = [ {
							renderer : $.jqplot.OHLCRenderer,
							rendererOptions : {
								candleStick : true
							}
						} ];

						chartOptions.highlighter = {
							show : true,
							showMarker : false,
							tooltipAxes : 'xy',
							yvalues : 4,
							formatString : '<table class="jqplot-highlighter"> \
		      <tr><td>date:</td><td>%s</td></tr> \
		      <tr><td>open:</td><td>%s</td></tr> \
		      <tr><td>hi:</td><td>%s</td></tr> \
		      <tr><td>low:</td><td>%s</td></tr> \
		      <tr><td>close:</td><td>%s</td></tr></table>'
						};
					} else if (this.report.layout.chart.type === this.reportingService.metadata.chartTypes.barChart.id) {
						chartOptions.seriesDefaults.renderer = $.jqplot.BarRenderer;
						chartOptions.seriesDefaults.rendererOptions = {
							fillToZero : true
						};

						if (this.getFirstDimension().type == this.reportingService.metadata.timestampType) {
							chartOptions.axes.xaxis.renderer = jQuery.jqplot.DateAxisRenderer;
						} else {
							chartOptions.axes.xaxis.renderer = jQuery.jqplot.CategoryAxisRenderer;
						}

						chartOptions.axes.xaxis.tickRenderer = jQuery.jqplot.CanvasAxisTickRenderer;
						chartOptions.axes.yaxis.tickRenderer = jQuery.jqplot.CanvasAxisTickRenderer;

						chartOptions.axes.yaxis.pad = 1.05;
					} else if (this.report.layout.chart.type === this.reportingService.metadata.chartTypes.bubbleChart.id) {
						chartOptions.seriesDefaults.renderer = $.jqplot.BubbleRenderer;
						chartOptions.seriesDefaults.rendererOptions = {
							bubbleGradients : true
						};
					} else {
						chartOptions.seriesDefaults.renderer = jQuery.jqplot.PieRenderer;
						chartOptions.seriesDefaults.rendererOptions = {
							fill : false,
							showDataLabels : true,
							sliceMargin : 4,
							lineWidth : 5
						};
					}

					// Label series

					if (this.report.dataSet.groupBy) {
						if (groupIds) {
							for ( var i = 0; i < groupIds.length; ++i) {

								chartOptions.series.push({
									label : groupIds[i]
								});
							}
						}
					}

					return chartOptions;
				};

				/**
				 * 
				 */
				ReportRenderingController.prototype.createChart = function() {
					var deferred = this.reportingService
							.retrieveData(this.report);
					var self = this;

					document.body.style.cursor = "wait";

					deferred
							.done(
									function(data) {
										console
												.log("Report Data before preprocessing");
										console.log(data);

										var chartOptions = self
												.createChartOptions(data.groupIds);

										// Perform preprocessing of data

										data.seriesGroup = self
												.preprocessData(data.seriesGroup);

										// Perform chart-specific data
										// processing

										if (self.report.layout.chartType === self.reportingService.metadata.chartTypes.barChart.id) {
											var seriesGroup = [];
											var ticks = [];

											for ( var n = 0; n < data.seriesGroup.length; ++n) {
												var series = [];

												seriesGroup.push(series);

												for ( var m = 0; m < data.seriesGroup[n].length; ++m) {
													series
															.push(data.seriesGroup[n][m][1]);

													if (n == 0) {
														ticks
																.push(data.seriesGroup[n][m][0]);
													}
												}
											}

											chartOptions.axes.xaxis.ticks = ticks;

											data.data = seriesGroup;
										} else if (self.report.layout.chartType === self.reportingService.metadata.chartTypes.bubbleChart.id) {
											var arr = [
													[ 11, 123, 1236, "Acura" ],
													[ 45, 92, 1067,
															"Alfa Romeo" ],
													[ 24, 104, 1176,
															"AM General" ],
													[ 50, 23, 610,
															"Aston Martin Lagonda" ],
													[ 18, 17, 539, "Audi" ],
													[ 7, 89, 864, "BMW" ],
													[ 2, 13, 1026, "Bugatti" ] ];

											data = [ arr ];
										}

										// Clean Canvas

										if (self.chart) {
											self.chart.destroy();
										}

										window
												.setTimeout(
														function() {
															self.chart = jQuery
																	.jqplot(
																			'chartView',
																			data.seriesGroup,
																			chartOptions);
															document.body.style.cursor = "default";
														}, 1000);
									}).fail(function() {
								document.body.style.cursor = "default";
							});
				};

				/**
				 * 
				 */
				ReportRenderingController.prototype.preprocessData = function(
						seriesGroup) {
					if (this.getFact().type == this.reportingService.metadata.durationType) {
						var secondsPerUnit = {
							s : 1000,
							m : 1000 * 60,
							h : 1000 * 60 * 60,
							d : 1000 * 60 * 60 * 24,
							w : 1000 * 60 * 60 * 24 * 7,
							M : 1000 * 60 * 60 * 24 * 30, // TODO
							// Consider
							// calendar
							y : 1000 * 60 * 60 * 24 * 256, // TODO
						// Consider
						// calendar
						};

						// TODO Remove, just added for
						// robustness

						if (!this.report.dataSet.factDurationUnit) {
							this.report.dataSet.factDurationUnit = "s";
						}

						for ( var n = 0; n < seriesGroup.length; ++n) {
							for ( var m = 0; m < seriesGroup[n].length; ++m) {
								seriesGroup[n][m][1] = seriesGroup[n][m][1]
										/ secondsPerUnit[this.report.dataSet.factDurationUnit];
							}
						}
					}

					// TODO Check whether dimension is discrete/string

					for ( var n = 0; n < seriesGroup.length; ++n) {
						for ( var m = 0; m < seriesGroup[n].length; ++m) {
							if (seriesGroup[n][m][0] === "__UNDEFINED") {
								seriesGroup[n][m][0] = "(Undefined)"; // TODO
								// I18N
							}
						}
					}

					return seriesGroup;
				};

				/**
				 * 
				 */
				ReportRenderingController.prototype.createTable = function() {
					var dataTableHeaderRow = jQuery("#dataTableHeaderRow");
					var dataTableBody = jQuery("#dataTableBody");
					var columns = this.getReportTableColumns();

					dataTableHeaderRow.empty();

					for (x in columns) {
						var column = columns[x];

						dataTableHeaderRow.append("<th>" + column.name
								+ "</th>");
					}

					document.body.style.cursor = "wait";

					var deferred = this.reportingService
							.retrieveData(this.report);
					var self = this;

					deferred
							.done(
									function(data) {
										var rows = data.recordSet;
										console.log("Record Set");
										console.log(rows);

										dataTableBody.empty();

										for ( var n = 0; n < rows.length; ++n) {
											var row = rows[n];
											var rowString = null;

											if ((n + 1) % 2 == 0) {
												rowString = "<tr class='odd gradeA'>";
											} else {
												rowString = "<tr class='even gradeA'>";
											}

											for (y in columns) {
												var column = columns[y];

												if (!row[column.id]) {
													rowString += "<td></td>";
												} else if (column.type == self.reportingService.metadata.timestampType) {
													var date = new Date(
															row[column.id]);

													rowString += "<td>"
															+ self
																	.formatDateTime(date)
															+ "</td>";

												} else {
													rowString += "<td>"
															+ row[column.id]
															+ "</td>";
												}
											}

											rowString += "</tr>";

											dataTableBody.append(rowString);

											jQuery('#reportTable')
													.dataTable(
															{
																"bDestroy" : true,
																"sDom" : 'T<"clear">lfrtip',
																"oTableTools" : {
																	"aButtons" : [
																			"copy",
																			"print",
																			{
																				"sExtends" : "collection",
																				"sButtonText" : "Save",
																				"aButtons" : [
																						"csv",
																						"xls",
																						"pdf" ]
																			} ]
																}
															});

											document.body.style.cursor = "default";
										}
									}).fail(function() {
								document.body.style.cursor = "default";
							});
				};

				/**
				 * 
				 */
				ReportRenderingController.prototype.getReportTableColumns = function() {
					var columns = [];

					for ( var k = 0; k < this.report.dataSet.columns.length; ++k) {
						var dimension = this.getPrimaryObject().dimensions[this.report.dataSet.columns[k]];

						columns.push(dimension);
					}

					// TODO Remove?
					// if (this.getPrimaryObject().supportsDescriptors) {
					// for ( var l in
					// this.reportingService.modelData.descriptors) {
					// var descriptor =
					// this.reportingService.modelData.descriptors[l];
					//
					// columns.push({
					// id : l,
					// name : descriptor.name
					// });
					// }
					// }

					return columns;
				};

				/**
				 * 
				 */
				ReportRenderingController.prototype.formatDateTime = function(
						dateTime) {
					return this.pad(dateTime.getUTCDate(), 2) + "."
							+ this.pad(dateTime.getUTCMonth() + 1, 2) + "."
							+ dateTime.getUTCFullYear() + " "
							+ this.pad(dateTime.getUTCHours(), 2) + ":"
							+ this.pad(dateTime.getUTCMinutes(), 2);
				};

				ReportRenderingController.prototype.pad = function(number,
						characters) {
					return (1e15 + number + // combine with large number
					"" // convert to string
					).slice(-characters); // cut leading "1"
				};
			}
		});