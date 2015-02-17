/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * @author Marc.Gille
 * @author Yogesh.Manware
 * 
 */
define(
		[ "bpm-reporting/js/report/AngularAdapter",
				"bpm-reporting/js/report/ReportingService", "bpm-reporting/js/report/I18NUtils" ],
		function(AngularAdapter, ReportingService, I18NUtils) {
			
			var angularServices = null;
			
			return {
				create : function(angularServices1) {
					var controller = new ReportRenderingController();
					angularServices = angularServices1;
					return controller;
				}
			};

			/**
			 * 
			 */
			function ReportRenderingController() {
				this.reportingService = ReportingService.instance();

					this.nonCountTableConfig = {
					         disableSorting : [{
					                aaSorting: []
					              }, {
					                bSortable: false,
					                aTargets: ["_all"]
					              }],
					        multi_headers : true, //dont change this
					      };
		
			      this.countTableConfig = {
			        multi_headers : false, //dont change this
			      };

				/**
				 * 
				 */
				ReportRenderingController.prototype.initialize = function(
						report, masterController) {
					this.report = report;

					// TODO Angular may have better concepts for
					// modularization/nesting

					if (!masterController) {
						this.masterController = this;
					} else {
						this.masterController = masterController;
					}
					
					this.rows = [];

					// this.updateView();
				};

				/**
				 * 
				 */
				ReportRenderingController.prototype.setReportData = function(reportData) {
					this.reportData = reportData;
				}
				
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
					return this.reportingService.getCumulatedFacts(this.report)[this.report.dataSet.fact];
				};

				/**
				 * 
				 */
				ReportRenderingController.prototype.getDimension = function(id) {
					var dimensions = this.reportingService.getCumulatedDimensions(this.report);
					for(var i = 0; i < dimensions.length; i++){
						if(id == dimensions[i].id){
							return dimensions[i]
						}
					}
				};
				
				/**
				 * 
				 */
				ReportRenderingController.prototype.getFirstDimension = function() {
					return this.getDimension([this.report.dataSet.firstDimension]);
				};

				/**
				 * 
				 */
				ReportRenderingController.prototype.renderReport = function(report, scopeController) {
					this.initialize(report);
					var deferred = jQuery.Deferred();
					var self = this;

					if (this.report.layout.type == 'table') {
						this.createTable();
					} else {
						this.createChart(scopeController).done(function(){
							deferred.resolve();
						}).fail(function(){
							deferred.reject();
						});
					}

					return deferred.promise();
				};

				/**
				 * Creates the JQPlot drawing options from chart type defaults
				 * and settings stored with the report definition.
				 */
				ReportRenderingController.prototype.createChartOptions = function(
						groupIds, data) {
					var chartOptions = {
						series : [],
						seriesDefaults : {
							label : this.report.layout.chart.options.title,
							lineWidth : 1.5,
							markerOptions : {
								style : "filledCircle"
							},
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
						title: {
					        text: this.report.layout.chart.options.title,   // title for the plot,
					        show: true,
					        textAlign: "left"
					    },
						axes : {
							xaxis : {},
							x2axis : {},
							yaxis : {},
							y2axis : {}
						},
						legend : {
							show: true,
							    renderer: $.jqplot.EnhancedLegendRenderer,
			                location: 'e',
			                placement: 'outsideGrid',
			                fontSize: '11px'
						},
						highlighter : {},
						cursor : {show : true, followMouse : true},
						zoom : {},
						seriesColors: [ "#4bb2c5", "#c5b47f", "#EAA228", "#579575", "#839557", "#958c12",
						                 "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"]
					};

					// Copy configuration from Report Definition

					if (this.report.layout.chart.options.axes.xaxis.showTicks) {
						chartOptions.axes.xaxis.label = this.report.layout.chart.options.axes.xaxis.label;
					}
					chartOptions.axes.xaxis.min = this.report.layout.chart.options.axes.xaxis.min;
					if (!this.report.layout.chart.options.axes.xaxis.min && 
							(this.getFirstDimension().type == this.reportingService.metadata.countType ||
							this.getFirstDimension().type == this.reportingService.metadata.durationType)) {
						chartOptions.axes.xaxis.min = 0;
					}
					chartOptions.axes.xaxis.max = this.report.layout.chart.options.axes.xaxis.max;
					chartOptions.axes.xaxis.tickOptions = this.report.layout.chart.options.axes.xaxis.tickOptions;
					chartOptions.axes.xaxis.tickOptions.showMark = this.report.layout.chart.options.axes.xaxis.showTickMarks;
					chartOptions.axes.xaxis.showTickMarks = this.report.layout.chart.options.axes.xaxis.showTickMarks;
					chartOptions.axes.xaxis.showTicks = this.report.layout.chart.options.axes.xaxis.showTicks;
					
					if (this.report.layout.chart.options.axes.yaxis.showTicks) {
						chartOptions.axes.yaxis.label = this.report.layout.chart.options.axes.yaxis.label;
					}
					chartOptions.axes.yaxis.min = this.report.layout.chart.options.axes.yaxis.min;
					if (!this.report.layout.chart.options.axes.yaxis.min) {
						var cumulatedFacts = this.reportingService.getCumulatedFacts(this.report, true);
						for ( var n in cumulatedFacts) {
							var fact = cumulatedFacts[n];
							
							if (this.report.dataSet.fact == fact.id)
							{
								if (fact.type.id == this.reportingService.metadata.countType.id || 
										fact.type.id == this.reportingService.metadata.durationType.id) {
									chartOptions.axes.yaxis.min = 0;
								} else {
									chartOptions.axes.yaxis.min = this.report.layout.chart.options.axes.yaxis.min;
								}
								break;
							}
						}
					}
					chartOptions.axes.yaxis.max = this.report.layout.chart.options.axes.yaxis.max;
					chartOptions.axes.yaxis.tickOptions = this.report.layout.chart.options.axes.yaxis.tickOptions;
					chartOptions.axes.yaxis.tickOptions.showMark = this.report.layout.chart.options.axes.yaxis.showTickMarks;
					chartOptions.axes.yaxis.showTickMarks = this.report.layout.chart.options.axes.yaxis.showTickMarks;
					chartOptions.axes.yaxis.showTicks = this.report.layout.chart.options.axes.yaxis.showTicks;
					
					chartOptions.legend.show = this.report.layout.chart.options.legend.show;
					chartOptions.legend.location = this.report.layout.chart.options.legend.location;
					if (!this.report.layout.chart.options.legend.show) {
						//JQPLOT issue: If Legend is disabled(false) still jqplot tries to draw and fails, so initializing it to empty object 
						chartOptions.legend = {};
					} 
					chartOptions.highlighter.show = this.report.layout.chart.options.highlighter.show;
					chartOptions.cursor.showTooltip = this.report.layout.chart.options.cursor.showTooltip;
					chartOptions.cursor.show = this.report.layout.chart.options.cursor.show;
					chartOptions.cursor.zoom = this.report.layout.chart.options.cursor.zoom;
					chartOptions.seriesDefaults.showMarker = this.report.layout.chart.options.seriesDefaults.showMarker;
					chartOptions.seriesDefaults.markerOptions = this.report.layout.chart.options.seriesDefaults.markerOptions;
					chartOptions.seriesDefaults.shadow = this.report.layout.chart.options.seriesDefaults.shadow;
					chartOptions.seriesDefaults.pointLabels.show = this.report.layout.chart.options.seriesDefaults.pointLabels.show;
					chartOptions.seriesDefaults.trendline.show = this.report.layout.chart.options.seriesDefaults.trendline.show;
					chartOptions.seriesDefaults.rendererOptions = {
						animation : {
							speed : 2500
						}
					};
					chartOptions.animate = this.report.layout.chart.options.animate;
					chartOptions.animateReplot = this.report.layout.chart.options.animateReplot;
					
					if (this.report.layout.chart.options.seriesDefaults.lineWidth) {
					   chartOptions.seriesDefaults.lineWidth = this.report.layout.chart.options.seriesDefaults.lineWidth;
					}
					
					if (this.report.layout.chart.options.seriesDefaults.color) {
					   chartOptions.seriesDefaults.color = this.report.layout.chart.options.seriesDefaults.color;
					}
					

					//For Legend Positioning.
					if (this.report.dataSet.type === 'seriesGroup'
			         && this.report.layout.subType === this.reportingService.metadata.layoutSubTypes.chart.id
			         || this.report.layout.chart.type === this.reportingService.metadata.chartTypes.pieChart.id)
      			{
					   var northSide = [ "n" ];
					   var southSide = [ "s" ];
      			   var eastSide = [ "ne", "e", "se" ];
      			   var westSide = [ "w", "nw", "sw" ];
      			   
      			   var dataLength = data.seriesGroup.length;
      			   
      			   //TODO Donut chart might need more processing.
      			   if (this.report.layout.chart.type === this.reportingService.metadata.chartTypes.donutChart.id)
                  {
      			      dataLength = data.seriesGroup[0].length; 
                  }
      			   
      			   if (dataLength > 12)
                  {
      			      jQuery("#dataSetExceedWarning").text(this.getI18N('reporting.definitionView.preview.dataSetExceed.message'));
      			      jQuery("#dataSetExceedWarning").show();
                  }
      
      			   var defaultChartSize = 400;
      			   var adjustedChartSize = 500;
      			   jQuery('#chartView').css('height', defaultChartSize);
      			   if (northSide.indexOf(this.report.layout.chart.options.legend.location) != -1)
                  {
      			      chartOptions.legend.rendererOptions = {
                              numberRows : Math.ceil(data.seriesGroup.length / 14)
                           }
      			      jQuery('#chartView').css('height', adjustedChartSize);
                  } else if (southSide.indexOf(this.report.layout.chart.options.legend.location) != -1)
                  {
                     chartOptions.legend.rendererOptions = {
                        numberRows : Math.ceil( dataLength/ 14)
                     }
                     jQuery('#chartView').css('height', adjustedChartSize);
                  } else if (eastSide.indexOf(this.report.layout.chart.options.legend.location) != -1)
      			   {
      			      chartOptions.legend.rendererOptions = {
      			         numberColumns : Math.ceil(dataLength / 10)
      			      }
      			   } else if (westSide.indexOf(this.report.layout.chart.options.legend.location) != -1)
                  {
                     chartOptions.legend.rendererOptions = {
                        numberColumns : Math.ceil(data.seriesGroup.length / 10)
                     }
                  }  
      			}

					// TODO There is more

					if (this.report.layout.chart.type === this.reportingService.metadata.chartTypes.xyPlot.id) {
						// Use default series renderer

						if (this.getFirstDimension().type == this.reportingService.metadata.timestampType) {
							chartOptions.axes.xaxis.renderer = jQuery.jqplot.DateAxisRenderer;
							chartOptions.axes.xaxis.tickOptions.formatString = this.getDateFormatForDimension(true);
						} else {
							chartOptions.axes.xaxis.renderer = jQuery.jqplot.CategoryAxisRenderer;
						}
						
						chartOptions.axes.xaxis.tickRenderer = jQuery.jqplot.CanvasAxisTickRenderer;
						chartOptions.axes.yaxis.tickRenderer = jQuery.jqplot.CanvasAxisTickRenderer;
						
						chartOptions.highlighter = {
						   show: this.report.layout.chart.options.highlighter.show,
						   tooltipContentEditor: tooltipContentEditor
						};
						
					} else if (this.report.layout.chart.type === this.reportingService.metadata.chartTypes.candlestickChart.id) {
						if (this.getFirstDimension().type == this.reportingService.metadata.timestampType) {
							chartOptions.axes.xaxis.renderer = jQuery.jqplot.DateAxisRenderer;
							chartOptions.axes.xaxis.tickOptions.formatString = this.getDateFormatForDimension(true);
						} else {
							chartOptions.axes.xaxis.renderer = jQuery.jqplot.CategoryAxisRenderer;
						}
						chartOptions.series.push({
							renderer:$.jqplot.OHLCRenderer, rendererOptions:{candleStick:false }
						})
						chartOptions.axes.xaxis.tickRenderer = jQuery.jqplot.CanvasAxisTickRenderer;
                  chartOptions.axes.yaxis.tickRenderer = jQuery.jqplot.CanvasAxisTickRenderer;
                  
                  chartOptions.seriesDefaults.pointLabels.hideZeros = true;
                  
                  chartOptions.highlighter = {
                     show: this.report.layout.chart.options.highlighter.show,
                     tooltipContentEditor: tooltipContentEditor
                  };
						
					} else if (this.report.layout.chart.type === this.reportingService.metadata.chartTypes.barChart.id) {
						chartOptions.seriesDefaults.renderer = $.jqplot.BarRenderer;
						chartOptions.stackSeries = (this.report.layout.chart.options.stackSeries && 
						         (this.report.dataSet.groupBy != null && this.report.dataSet.groupBy != 'None'));

						chartOptions.seriesDefaults.pointLabels.hideZeros = true;
						chartOptions.seriesDefaults.rendererOptions = {
							animation : {
								speed : 2500
							},
							fillToZero : true,
							highlighter: {
							   show: this.report.layout.chart.options.highlighter.show,
							   tooltipContentEditor: tooltipContentEditor
							}
						};

						if (this.getFirstDimension().type == this.reportingService.metadata.timestampType) {
						   chartOptions.axes.xaxis.renderer = jQuery.jqplot.DateAxisRenderer;
						   chartOptions.axes.xaxis.tickOptions.formatString = this.getDateFormatForDimension(true);
						   chartOptions.axes.xaxis.tickRenderer = jQuery.jqplot.AxisTickRenderer;
						} else {
						   chartOptions.axes.xaxis.renderer = jQuery.jqplot.CategoryAxisRenderer;
						   chartOptions.axes.xaxis.tickRenderer = jQuery.jqplot.CanvasAxisTickRenderer;
						}
						
						chartOptions.axes.yaxis.pad = 1.05;
					} else if (this.report.layout.chart.type === this.reportingService.metadata.chartTypes.bubbleChart.id) {
						chartOptions.seriesDefaults.renderer = $.jqplot.BubbleRenderer;
						chartOptions.seriesDefaults.rendererOptions = {
							bubbleGradients : true
						};
					}else if (this.report.layout.chart.type === this.reportingService.metadata.chartTypes.donutChart.id) {
						chartOptions.seriesDefaults.renderer = jQuery.jqplot.DonutRenderer;
						chartOptions.seriesDefaults.rendererOptions = {
						     highlightMouseOver : this.report.layout.chart.options.highlighter.show,
					        // Donut's can be cut into slices like pies.
					        sliceMargin: 3,
					        // Pies and donuts can start at any arbitrary angle.
					        startAngle: -90,
					        showDataLabels: true,
					        // By default, data labels show the percentage of the donut/pie.
					        // You can show the data 'value' or data 'label' instead.
					        series: [{label: 'A1'},{label: 'A2'}],
					        highlighter: {
					            show: this.report.layout.chart.options.highlighter.show,
					            tooltipContentEditor: tooltipContentEditor,
					            useAxesFormatters: false
					        }
						};
					} else if(this.report.layout.chart.type === this.reportingService.metadata.chartTypes.pieChart.id) {
						chartOptions.seriesDefaults.renderer = jQuery.jqplot.PieRenderer;
						chartOptions.seriesDefaults.rendererOptions = {
						   highlightMouseOver : this.report.layout.chart.options.highlighter.show,
							fill : false,
							showDataLabels : true,
							sliceMargin : 4,
							lineWidth : 5
						};
						
						chartOptions.highlighter = {
		                     show: this.report.layout.chart.options.highlighter.show,
		                     tooltipContentEditor: tooltipContentEditor,
		                     useAxesFormatters: false
		            };
					}
					
					function tooltipContentEditor(str, seriesIndex, pointIndex, plot) {
					   // display series_label, x-axis_tick, y-axis value
					   if (plot.stackSeries)
					      return plot.series[seriesIndex]["label"] + ", " + plot.options.axes.xaxis.ticks[pointIndex] + 
					      " : " + plot.data[seriesIndex][pointIndex];
					   else
					      return plot.series[seriesIndex]["label"] + ", " + plot.data[seriesIndex][pointIndex][0] +
					      " : " + plot.data[seriesIndex][pointIndex][1];
					}
					
					if (chartOptions.stackSeries) {
					   var x_axis = [];
					   for ( var i = 0; i < data.seriesGroup.length; ++i) {
					      var tempData = [];
					      for ( var j = 0; j < data.seriesGroup[i].length; ++j) {
					         if (i == 0) {
					            x_axis.push(data.seriesGroup[i][j][0]);
					         }
					         tempData.push(data.seriesGroup[i][j][1]);
					         if (j == data.seriesGroup[i].length -1 ) {
					            data.seriesGroup[i] = tempData;
					         }
					      }
					   }
                  
					   
					   var result = getUniqueElementsCount(x_axis);
					   var intervals = result[1];
					   x_axis = result[0];
					   var max = [];
					   for ( var i = 0; i < data.seriesGroup.length; i++)
					   {
					      for ( var j = 0; j < data.seriesGroup[i].length; j++)
					      {
					         max = [];
					         for ( var k = 0; k < intervals.length; k++)
					         {
					            var tempArray = data.seriesGroup[i].splice(0, intervals[k]);
					            max[k] = Math.max.apply(Math, tempArray);
					         }
					      }
					      if (chartOptions.stackSeries) {
					         data.seriesGroup[i] = max;
					      } else {
					         for ( var z = 0; z < x_axis.length; z++) {
					            data.seriesGroup[i][z] = [x_axis[z],max[z]];
					         }
					      }
					   }
					   if (chartOptions.stackSeries) {
					      chartOptions.axes.xaxis.ticks = x_axis;
					   }
					}

					// Label series
					if (this.report.dataSet.groupBy) {
						if (groupIds) {
							chartOptions.series = [];
							for ( var i = 0; i < groupIds.length; ++i) {
								
								if (this.report.layout.chart.type === this.reportingService.metadata.chartTypes.candlestickChart.id){
									chartOptions.series.push({
										label : groupIds[i],
										renderer:$.jqplot.OHLCRenderer, rendererOptions:{candleStick:false }
									});	
								}else{
									chartOptions.series.push({
										label : groupIds[i]
									});	
								}
							}
						}
					}

					return chartOptions;
				};

				/**
				 * perform ui controlled I18n
				 */
				ReportRenderingController.prototype.performUII18n = function(inData, report, scopeController, dimensionAsRow){
					
					var tableOptions = {
							aoColumnDefs : []
						};
					
					tableOptions.bPaginate = this.report.layout.table.options.showVisibleRowCountSelector;
					tableOptions.bFilter = this.report.layout.table.options.showSearchInput;
					
					if (this.report.layout.subType == this.reportingService.metadata.layoutSubTypes.table.id)
					{
					   this.setLanguage(tableOptions);
					}  
					
					if(scopeController){
						scopeController.tableOptions = tableOptions;	
					}
					
					var primaryObject = this.reportingService.metadata.objects[report.dataSet.primaryObject];
					
					//format groupby
					var dimension = this.getDimension(report.dataSet.groupBy);
					//if groupby is empty or none
					if(!dimension){
						Object.keys(inData).forEach(function(key) {
							if("processInstance" == key || "activityInstance" == key){
								inData[primaryObject.name] = inData[key]; //to I18n processInstance and activityInstance
		                        delete inData[key];	
							}
						});
					}
				};
				
				/**
				 * 
				 */
				ReportRenderingController.prototype.createChart = function(scopeController) {
					var localDeferred = jQuery.Deferred();
					
					var deferred = this.getReportData(this.report, this.parameters);
					var self = this;

					deferred
							.done(
									function(inData) {
										var fact_count = (self.report.dataSet.fact == self.reportingService.metadata.objects.processInstance.facts.count.id);
										
										//apply ui terminologies
										self.performUII18n(inData, self.report);
										
										//Transform data values for Candlestick chart
										if (self.report.layout.chart.type === self.reportingService.metadata.chartTypes.candlestickChart.id) {
											for(var i in inData){
												  for(var j in inData[i]){
													//now max, min, avg													  
												    inData[i][j].length = 4;
												  }
											}
											
										} else if (!fact_count) {
											for ( var i in inData) {
												for ( var j in inData[i]) {
													//before swap max, min, avg, stddev, count 
												    var temp = inData[i][j][1];
												    inData[i][j][1] = inData[i][j][3];
												    inData[i][j][3] = temp;
													//after swap avg, min, max, stddev, count
													inData[i][j].length = 2; //consider only avg value
												}
											}
										}
										

										if (self.report.dataSet.firstDimension == self.reportingService.metadata.objects.activityInstance.dimensions.activeTimestamp.id) {
											for ( var i in inData) {
												for ( var j in inData[i]) {
													//For dimension, "activeTimestamp" date returned from engine is in wrong format like "2014/11/19 00:00:00:000".
													//In IE above Date fails.
													//It should be like "2014/11/19 00:00:00" or "2014/11/19".
													//Applying below logic to convert date to "2014/11/19"
													if (inData[i][j][0].length > 10) {
														inData[i][j][0] = inData[i][j][0].substring(0, 10); 
													}
												}
											}
										}
										
										var data = {};
										data.seriesGroup = [];
										var seriesIds = [];
										for(var prop in inData){
											data.seriesGroup.push(inData[prop]); 
											seriesIds.push(prop);
										}
										
										console.log("Report Data before preprocessing");
										console.log(data);

//										var chartOptions ={
//											    title: 'Concern vs. Occurrance',
//											    //series:[{renderer:$.jqplot.BarRenderer}],
//											    axesDefaults: {
//											        tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
//											        tickOptions: {
//											          angle: -30,
//											          fontSize: '10pt'
//											        }
//											    },
//											    axes: {
//											      xaxis: {
//											        renderer: $.jqplot.CategoryAxisRenderer
//											      }
//											    }
//										};
										var chartOptions = self
												.createChartOptions(seriesIds, data);
										// Clean Canvas
										if (self.chart) {
											self.chart.destroy();
										}

										// Clean rendering area

										jQuery("#chartView").empty();

										window
												.setTimeout(
														function() {
															console
																	.debug("Chart Data");
															console
																	.debug(data.seriesGroup);
															console
																	.debug("Chart Options");
															console
																	.debug(chartOptions);

															if (data.seriesGroup.length) {
																self.chart = jQuery
																		.jqplot(
																				'chartView',
																				data.seriesGroup,
																				chartOptions);
															} else {
																self.chart = null;

																jQuery(
																		"#chartView")
																		.append(
																				"<p>" + self.getI18N('reporting.definitionView.preview.emptyDataSet.message') + "</p>");

															}

														}, 1000);
										localDeferred.resolve();
										self.hideReportPreview = false;
									}).fail(function() {
								self.renderingFailed = self.getI18N("reporting.definitionView.retrievalFailed");
								scopeController.updateView();
								localDeferred.reject();
							});
					
					return localDeferred.promise();
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
					
					var deferred = this.getReportData(this.report, this.parameters);
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
										}
									}).fail(function() {

							});
				};

				/**
				 * 
				 */
				ReportRenderingController.prototype.getReportTableColumns = function() {
					return this.reportingService
							.getColumnDimensions(this.report);
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

				/**
				 * 
				 */
				ReportRenderingController.prototype.pad = function(number,
						characters) {
					return (1e15 + number + // combine with large number
					"" // convert to string
					).slice(-characters); // cut leading "1"
				};
				
				
            /**
             *  Use this method to retrieve to report data from service
             */
            ReportRenderingController.prototype.getReportData = function(report, parameters) {
                var deferred = jQuery.Deferred();

                document.body.style.cursor = "wait";
                
                if(this.reportData){
                	document.body.style.cursor = "default";
                   return deferred.resolve(this.reportData);
                }else{
                	var self = this;
                    self.reportingService.retrieveData(report, parameters)
                    .done(
                          function(data) {
                        	document.body.style.cursor = "default";
                            deferred.resolve(data);
                          }).fail(function(data) {
                       document.body.style.cursor = "default"; 	  
                       deferred.reject(data);
                    });	
                }
                return deferred.promise();
                
             };
            
            //TABLE data processing
            /**
             * 
             */
            ReportRenderingController.prototype.refreshPreview = function(scopeController, report, parameters) {
               jQuery("#dataSetExceedWarning").empty();
               jQuery("#dataSetExceedWarning").hide();
               
   				var self = this;
				
   				var deferred = jQuery.Deferred();
            	
            	if (report) {
					this.report = report;
				}
            	
            	if(parameters){
            		this.parameters = parameters;	
            	}
            	
            	//timeout is added so that the tab change before following code gets executed
            	this.hideReportPreview = true;
				this.renderingFailed = null;
				document.body.style.cursor = "wait";
				scopeController.updateView();
				
					setTimeout(
							function() {
								if (self.report.layout.type == 'document') {
									self.renderCompositeReport(scopeController);
								}else if(self.report.dataSet.type === 'seriesGroup' && self.report.layout.subType == self.reportingService.metadata.layoutSubTypes.table.id){
										self.getReportData(self.report, self.parameters).done(
												function(data) {
													self.refreshSeriesTable(data, scopeController);
													deferred.resolve();
												}).fail(function(err) {
											self.renderingFailed = self.getI18N("reporting.definitionView.retrievalFailed");		
											console.log("Failed getting Preview Date: showing dummy data" + err);
											deferred.reject();
										});	
										
								}else if (self.report.dataSet.type === 'seriesGroup'
									&& self.report.layout.subType == self.reportingService.metadata.layoutSubTypes.chart.id) {

									self.renderReport(self.report, scopeController)
											.done(function() {
												deferred.resolve();
											}).fail(function() {
												deferred.reject();
											});
								} else {
									self.refreshRecordSet(scopeController);
								}			
							}, 500);	
				
				
				
				
				return deferred.promise();
			};

			/**
			 * 
			 */
			ReportRenderingController.prototype.renderCompositeReport = function(scopeController) {
				var deferred = jQuery.Deferred();
			    var self = this;
			    
			    var isSeriesGroup = self.report.dataSet.type === 'seriesGroup';
			    
			    self.getReportData(self.report, self.parameters)
			        .done(function(data) {
			        console.log("Data for Document");
			        console.log(data);
			        
				    //show preview
				    self.hideReportPreview = false;
				    scopeController.updateView();

			
			        var html = "<html ng-app='STARTDUST_REPORTING'><head>"
			        	+ "<link rel='stylesheet' type='text/css' href='"
			        	+ self.reportingService.getRootUrl()
			          	 + "/plugins/bpm-reporting/js/libs/ckeditor/contents.css'/>"
			          	 // TODO Read local
			          	 + "<scr"
			          	 + "ipt"
			          	 + " src='"
			          	 + self.reportingService
			          	.getRootUrl()
			          	 + "/portal-shell/js/libs/angular/1.2.11/angular.js'>"
			          	 + "</scr"
			          	 + "ipt>"
			          	 + "<scr"
			          	 + "ipt"
			          	 + " src='"
			          	 + self.reportingService
			          	.getRootUrl()
			          	 + "/plugins/bpm-reporting/js/report/programmaticAccessHelper.js'>"
			          	 + "</scr"
			          	 + "ipt>"
			          	 + "<scr"
			          	 + "ipt>"
			          	 + "var __reportData="
			          	 + (data ? JSON.stringify(data)
			          		 : "null")
			          	 + ";"
			          	 + "var __isSeriesGroup="
			          	 + isSeriesGroup
			          	 + ";"
			          	 + "</scri"
			          	 + "pt></head><body><div ng-controller='Controller'>"
			          	 + self.report.layout.document.markup
			          	+ "</div></body></html>";
			        
			        console.log(html);
			
			        var documentFrame = document.getElementById('documentFrame');
			        var frameDocument = documentFrame.contentDocument || documentFrame.contentWindow.document;
			
			        frameDocument.write(html);
			        frameDocument.close();
				    
			        deferred.resolve();
			    }).fail(function() {
			    	self.renderingFailed = self.getI18N("reporting.definitionView.retrievalFailed");
			    	scopeController.updateView();
			        deferred.reject();
			    });
			    
			    return deferred.promise();
			};
			
			
			
			
			ReportRenderingController.prototype.getCumulantsTableConfig = function(){
		    	  if(this.report.dataSet.fact == this.reportingService.metadata.objects.processInstance.facts.count.id){
		    		  return this.countTableConfig;
		    	  }else{
		    		  return this.nonCountTableConfig;  
		    	  }
		    };
		    
			/**
			 * 
			 */
			ReportRenderingController.prototype.refreshSeriesTable = function(data, scopeController) {
			
			    var self = this;
			    var inData = data;
			
			    //detect Table drawing mode
			    var cumulantsAsRow = true;
			    var dimensionAsRow = true;
			
			    if (self.report.layout.table.dimensionDisplay == self.reportingService.metadata.cumulantsDisplay.columns.id) {
			        dimensionAsRow = false;
			    }
			
			    if (self.report.layout.table.cumulantsDisplay != self.reportingService.metadata.cumulantsDisplay.rows.id) {
			        cumulantsAsRow = false;
			    }
			    //apply ui terminologies
			    this.performUII18n(data, this.report, scopeController, dimensionAsRow);
			    var fact_count = (this.report.dataSet.fact == this.reportingService.metadata.objects.processInstance.facts.count.id);
			
			    var tableDrawMode = 1; // 2, 3, 4, 5, 6 based on Dimensions as columns/Rows AND/OR Cumulants as columns/Rows
			
			    var tableParameters = {};
			    tableParameters.numberOfColHeaders = 1;
			    //tableParameters.numberOfRowHeaders = 1;
			    tableParameters.rowHeaderIndex = 0;
			    tableParameters.addLastRowAsFooter = false;
			
			    var fileName = self.report.layout.title;
			    if(!fileName){
			    	fileName = self.report.name;
			    }
			    tableParameters.csv = fileName;
			    tableParameters.excel = fileName;
			    
			    this.initializeDataTableTableToolsOptions(this.report.layout.table.options.showExportButtons, tableParameters);
			
			    var addTotalRow = false;
			
			    var dimensionDateFormat = this.getDateFormatForDimension();
			    if(dimensionDateFormat){
			    	if(dimensionAsRow){
				    	tableParameters.colFilters = {0: this.reportingService.metadata.timestampType.id + ":'" + dimensionDateFormat + "'"};
				    }else{
				    	tableParameters.rowFilters = {0: this.reportingService.metadata.timestampType.id + ":'" + dimensionDateFormat + "'"};
				    }	
			    }
			    if (fact_count) {
			        if (dimensionAsRow) {
			            tableDrawMode = 1;
			        } else {
			            tableDrawMode = 2;
			        }
			    } else {
			
			        if (dimensionAsRow) {
			            if (cumulantsAsRow) {
			                tableDrawMode = 6;
			                tableParameters.rowHeaderIndex = 1;
			                tableParameters.groupByIndex = 0;
			            } else {
			                tableDrawMode = 3;
			                tableParameters.numberOfColHeaders = 2;
			            }
			        } else {
			            if (cumulantsAsRow) {
			                tableDrawMode = 4;
			                tableParameters.rowHeaderIndex = 1;
			                tableParameters.groupByIndex = 0;
			            } else {
			                tableDrawMode = 5;
			                tableParameters.numberOfColHeaders = 2;
			            }
			        }
			    }
			
			    this.tableDrawMode = tableDrawMode;
			
			    if (this.report.layout.table.displayTotals && this.report.dataSet.groupBy) {
			        addTotalRow = true;
			    }
			
			    if (addTotalRow) {
			        if (this.tableDrawMode == 1 || this.tableDrawMode == 3 || this.tableDrawMode == 5) {
			            tableParameters.addLastRowAsFooter = true;
			        }
			    }
			
			    // transform data
			    var baseTable = [];
			
			    // if fact != count
			    if (!fact_count) {
			
			        var span = this.report.layout.table.selectedCumulants.length;
			
			        //position of cumulants in response json
			        var INDEX = {
			            maximum: 1,
			            minimum: 2,
			            average: 3,
			            stdDeviation: 4,
			            count: 5
			        };
			
			        var PROP_KEY_PREFIX = "reporting.definitionView.layout.table.cumulant.";
			
			        var CUMULANTS_MSG = {
			            maximum: this.getI18N(PROP_KEY_PREFIX + "maximum"),
			            average: this.getI18N(PROP_KEY_PREFIX + "average"),
			            minimum: this.getI18N(PROP_KEY_PREFIX + "minimum"),
			            stdDeviation: this.getI18N(PROP_KEY_PREFIX + "stdDeviation"),
			            count: this.getI18N(PROP_KEY_PREFIX + "count"),
			            total: this.getI18N(PROP_KEY_PREFIX + "total")
			        };
			
			        var dimensionName = "";
			        var primaryObject = this.reportingService.metadata.objects[this.report.dataSet.primaryObject];
			        var dimension = this.getDimension(this.report.dataSet.groupBy);
			        if (dimension) {
			            dimensionName = dimension.name;
			        }
			
			        if (tableDrawMode == 3 || tableDrawMode == 4) {
			            var baseTableIndex = 0;
			            var h1 = [dimensionName]; // header line one
			            var h2 = [this.getI18N("reporting.definitionView.cumulants.title", "Cumulants")]; // header line two
			
			            baseTable.push(h1);
			            baseTableIndex++;
			            baseTable.push(h2);
			            baseTableIndex++;
			
			            var rowHeaderAdded = false;
			
			            for (var seriesName in inData) {
			
			                for (var c = 0; c < span; c++) {
			                    h1.push(seriesName);
			                }
			
			                // for selected number of cumulants
			                for (var i in this.report.layout.table.selectedCumulants) {
			                    h2.push(CUMULANTS_MSG[this.report.layout.table.selectedCumulants[i]]);
			                }
			
			                for (var j = 0; j < inData[seriesName].length; j++) {
			                    if (!rowHeaderAdded) {
			                        baseTable.push([inData[seriesName][j][0]]);
			                    }
			                    //populate cumulant data
			                    for (var i in this.report.layout.table.selectedCumulants) {
			                        baseTable[baseTableIndex + j].push(data[seriesName][j][INDEX[this.report.layout.table.selectedCumulants[i]]]);
			                    }
			                }
			                rowHeaderAdded = true;
			            }
			        } else if (tableDrawMode == 5 || tableDrawMode == 6) {
			
			            var baseTableIndex = 0;
			            var h1 = [this.getI18N("reporting.definitionView.series.title", "Series")]; // header line one
			            var h2 = [dimensionName]; // header line two
			
			            baseTable.push(h1);
			            baseTableIndex++;
			            baseTable.push(h2);
			            baseTableIndex++;
			
			            var rowHeaderAdded = false;
			
			            for (var seriesName in inData) {
			
			                baseTable[baseTableIndex] = [seriesName];
			                for (var j = 0; j < inData[seriesName].length; j++) {
			
			                    if (!rowHeaderAdded) {
			                        for (var c = 0; c < span; c++) {
			                            h1.push(inData[seriesName][j][0]);
			                        }
			                        // for selected number of cumulants
			                        for (var i in this.report.layout.table.selectedCumulants) {
			                            h2.push(CUMULANTS_MSG[this.report.layout.table.selectedCumulants[i]]);
			                        }
			                    }
			
			                    //populate cumulant data
			                    for (var i in this.report.layout.table.selectedCumulants) {
			                        baseTable[baseTableIndex].push(data[seriesName][j][INDEX[this.report.layout.table.selectedCumulants[i]]]);
			                    }
			                }
			                rowHeaderAdded = true;
			                baseTableIndex++;
			            }
			        }
			
			        if (addTotalRow) {
			            var totalRow = [this.getI18N("reporting.definitionView.total.title", "Total")] // TODO: I18n
			            totalRow = totalRow.concat(getTotalRow(baseTable, 2, 1));
			            baseTable.push(totalRow);
			        }
			
			        if (tableDrawMode == 4 || tableDrawMode == 6) {
			            baseTable = transposeArray(baseTable);
			        }
			    } else { // fact is count
			        var baseTableIndex = 0;
			        var h1 = [this.getI18N("reporting.definitionView.series.title", "Series")]; // header line one
			
			        //for selected number of cumulants
			
			        baseTable.push(h1);
			        baseTableIndex++;
			
			        var rowHeaderAdded = false;
			
			        for (var seriesName in inData) {
			            h1.push(seriesName);
			
			            for (var j = 0; j < inData[seriesName].length; j++) {
			                if (!rowHeaderAdded) {
			                   inData[seriesName][j][0] =  inData[seriesName][j][0];
			                    baseTable.push(inData[seriesName][j]);
			                } else {
			                    baseTable[baseTableIndex + j] = baseTable[baseTableIndex + j].concat(inData[seriesName][j].slice(1));
			                }
			            }
			            rowHeaderAdded = true;
			        }
			
			        if (addTotalRow) {
			            var totalRow = [this.getI18N("reporting.definitionView.total.title", "Total")] // TODO: I18n
			            totalRow = totalRow.concat(getTotalRow(baseTable, 1, 1));
			            baseTable.push(totalRow);
			        }
			
			        if (tableDrawMode == 2) {
			            baseTable = transposeArray(baseTable);
			        }
			    }
			
			    //show preview
			    this.hideReportPreview = false;
			    
			    //set the data in parent scope
			    scopeController.tableParameters = tableParameters;
			    scopeController.tableArray = baseTable;
			    
         };
		
         /**
          * 
          */
         ReportRenderingController.prototype.getDateFormatForDimension = function(jqPlotFormat) {
        	if (this.getFirstDimension().type != this.reportingService.metadata.timestampType){
        	 return null;
        	}
        	
        	var dateFormatObj = this.reportingService.dateFormats;
        	
        	if(jqPlotFormat){
        		dateFormatObj = this.reportingService.jqPlot.dateFormats;
        	}
        	
			if (this.report.dataSet.firstDimensionCumulationIntervalUnit == 's') {
				return dateFormatObj.seconds;
			} else if (this.report.dataSet.firstDimensionCumulationIntervalUnit == 'm') {
				return dateFormatObj.minutes;
			} else if (this.report.dataSet.firstDimensionCumulationIntervalUnit == 'h') {
				return dateFormatObj.hours;
			} else if (this.report.dataSet.firstDimensionCumulationIntervalUnit == 'd'
					|| this.report.dataSet.firstDimensionCumulationIntervalUnit == 'w') {
				return dateFormatObj.date;
			} else if (this.report.dataSet.firstDimensionCumulationIntervalUnit == 'M') {
				return dateFormatObj.months;
			}
			return null;
		};
         /**
			 * 
			 */
		ReportRenderingController.prototype.refreshRecordSet = function(scopeController) {
			   var columns = this.reportingService.getColumnDimensions(this.report);
		
            var headers = [];
               
            for (x in columns) {
               var column = columns[x];
               var columnDisplayName = column.name;
               if (this.report.dataSet.groupBy != null && this.report.dataSet.groupBy != 'None') {
                  var aggregations = this.reportingService.metadata.recordSetAggregationFunctions;
                  for ( var n in aggregations ) {
			         if (this.report.dataSet.columns[x].metaData.aggregationFunction === aggregations[n].id) {
                        columnDisplayName += " (" + aggregations[n].name + ")";
                        break;
                     }
                  }
               }
               if (column.type.id == this.reportingService.metadata.durationType.id) {
                  columnDisplayName += " (" + this.report.dataSet.columns[x].metaData.durationUnit + ")";
               }
               headers.push(columnDisplayName);
                   }
            
            if (columns.length != 0)
            {   
               var self = this;
               setTimeout(function () {
                 	   self.refreshPreviewData(scopeController, headers);
                    }, 200);
            } 
     		};
     		
     		/**
          * 
          */
         ReportRenderingController.prototype.refreshPreviewData = function(scopeController, headers) {
            var self = this;	
            var pos = 0;
            var element = {};
            
            // Filter out Group by column from selected columns in JSON before invoking 
            // the API
            if (self.report.dataSet.columns) {
               for ( var i = this.report.dataSet.columns.length - 1; i >= 0; i--) {
                  if (self.report.dataSet.groupBy && self.report.dataSet.groupBy != 'None') {
                     if (self.report.dataSet.groupBy === self.report.dataSet.columns[i].id) {
                        self.pos = i;
                        self.element = self.report.dataSet.columns.splice(i, 1);
                        break;
                     }
                  }
               }
            }
            
            
        	   this.getReportData(this.report, this.parameters).done(
     		function(data) {
     		   if (self.report.dataSet.groupBy && self.report.dataSet.groupBy != 'None') {
        		   // Add previously filtered out Group by column to selected columns in JSON
     		      if (self.element) {
        		      self.report.dataSet.columns.splice(self.pos, 0, self.element[0]);
        		      self.element = null;
   
        		      // API doesn't support re-ordering of groupBy column, so re-ordering it 
        		      // as per position in selected columns
        		      for ( var rowIndex in data.rows) {
        		         var row = data.rows[rowIndex];
        		         row.splice(self.pos + 1, 0, row[0]);
        		         row.splice(0, 1);
        		      }
   
        		   } else {
        		      headers.splice(0, 0, self.getDimension(self.report.dataSet.groupBy).name);
        		   }
        		}
     			// Format data before displaying the Results
     		   scopeController.rows = self.formatPreviewData(data.rows, scopeController);
     		   var baseTable = [headers];
     		   for ( var rowIndex in data.rows)
     		   {
     			  baseTable.push(data.rows[rowIndex]);
     		   }
			    //show preview
			    self.hideReportPreview = false;
			    scopeController.tableArray = baseTable;
     		   
     			scopeController.updateView();
     			
     		}).fail(function(err) {
     			self.renderingFailed = self.getI18N("reporting.definitionView.retrievalFailed");
     			scopeController.updateView();
     			console.log("Failed getting Preview Date: " + err);
     		});   
         };
		
	    
/**
 * 
 */
ReportRenderingController.prototype.formatPreviewData = function(data, scopeController) {
   var self = this;
   //add filter - for date formatting
	//add filters to table parameters
   var filters = {};
   scopeController.tableParameters = {};
   scopeController.tableParameters.colFilters = filters;
   var tableOptions = {
			aoColumnDefs : []
		};
   
   this.initializeDataTableTableToolsOptions(this.report.layout.table.options.showExportButtons, 
               scopeController.tableParameters);
      
   tableOptions.bPaginate = this.report.layout.table.options.showVisibleRowCountSelector;
   tableOptions.bFilter = this.report.layout.table.options.showSearchInput;
   
   this.setLanguage(tableOptions);
   
   scopeController.tableOptions = tableOptions;
      
   var selectedColumns =  this.reportingService.getColumnDimensions(this.report);
   var displayValueMapping = {};
   
   if (self.report.dataSet.groupBy && self.report.dataSet.groupBy != 'None') {
      // Showing the group by column as first column in Reports 
      var found  = false;
      for ( var selColumn in selectedColumns) {
         if (selectedColumns[selColumn].id == self.report.dataSet.groupBy) {
            found = true;
            break;
         }
      }
      if (!found) {
         selectedColumns.splice(0, 0, self.getDimension(self.report.dataSet.groupBy));
      }
   }
   
   for ( var selColumn in selectedColumns)
   {
	  if (selectedColumns[selColumn].type.id == this.reportingService.metadata.timestampType.id) 
      {
         tableOptions.aoColumnDefs.push(getColumnDefForDate(selColumn, this.reportingService.dateFormats.minutes));
      }
   }
		
   
   var a = [];
   
   for ( var row in data)
   {
      var record = data[row];
      record.splice(selectedColumns.length, record.length);
      var b = {};
      for ( var selColumn in selectedColumns) {
         var key = selectedColumns[selColumn].id;
         key = replaceSpecialChars(key);
         var value = record[selColumn];
         b[key] = value;
      }
      a.push(b);
   }
   return a;
};

		ReportRenderingController.prototype.getI18N = function(key, defaultValue) {
			return I18NUtils.getProperty(key, defaultValue);
		};
		
		//Report Instance
		   /**
         * 
         */
        ReportRenderingController.prototype.saveReportInstance = function(report, parameters) {
        	var self = this;
        	if (report) {
				this.report = report;
			}
        	this.parameters = parameters;

        	if (this.report.storage.state == "saved") {
        		this.getReportData(this.report, this.parameters)
                .done(
                      function(data) {
                    	// save report instance along with report definition
                    		self.saveReportInstance_(self.report, data, null);
                      }).fail(function(data) {
                
                      });
				return;
			}else{
				if (parent.iPopupDialog) {
					parent.iPopupDialog.openPopup(self.prepareSaveReportInstance());
				}
			}
		};
        
		/**
		 * @param reportInstanceMetadata - contains report Name and report Location if any
		 */
		ReportRenderingController.prototype.saveReportInstance_ = function(report, reportData, reportMetadata) {
			var reportDI =  {};
			reportDI.definition = report;
			reportDI.data = reportData;
			reportDI.metadata = reportMetadata;
			var self= this;
			var deferred = jQuery.Deferred();
			this.reportingService.saveReportInstance(reportDI)
            .done(
                  function(data) {
                    deferred.resolve(data);
                  }).fail(function(data) {
               deferred.reject(data);
            });
			
			return deferred.promise();
		};
		
		/**
		 * 
		 */
		ReportRenderingController.prototype.saveReportInstanceAdhoc = function(reportMetadata) {
			var self = this;
			this.getReportData(this.report, this.parameters)
            .done(
                  function(data) {
                	// save report instance along with report definition
                		self.saveReportInstance_(self.report, data, reportMetadata);
                  }).fail(function(data) {
                  
                  });
		};

		/**
         * 
         */
        ReportRenderingController.prototype.prepareSaveReportInstance = function() {
					var self = this;
					var popupData = {
						attributes : {
							width : "650px",
							height : "290px",
							src : this.reportingService.getRootUrl()
									+ "/plugins/bpm-reporting/views/templates/reportStoragePopup.html"
						},
						payload : {
							I18N : {},
							report : self.report,
							modelParticipants : self.reportingService
									.getModelParticipants(),
							acceptFunction : function(reportMD) {
								self.saveReportInstanceAdhoc(reportMD);
                            }
						}
					};

					return popupData;
			};
				
            /*
			 * 
			 */
            ReportRenderingController.prototype.formatDate = function(value, fromFormat, toFormat) {
               if (value != undefined && value != null && value != "") {
                  try {
                     var date = jQuery.datepicker.parseDate(fromFormat, value);
                     value = jQuery.datepicker.formatDate(toFormat, date);
                  } catch(e) {
                     console.log(e);
                  }
               }
               return value;
            };
            
            /**
             * 
             */
            ReportRenderingController.prototype.initializeDataTableTableToolsOptions = function(showExportButtons, tableParamaters) {
               if (!showExportButtons) {
                  tableParamaters.csv = false;
                  tableParamaters.excel = false;
               }
               (this.report.layout.table.options.showExportButtons || this.report.layout.table.options.showSearchInput ||
                        this.report.layout.table.options.showVisibleRowCountSelector) ? jQuery('div .heading').css({display:'block'}) :
                           jQuery('div .heading').css({display:'none'});
            };
            
            /**
             * 
             */
            ReportRenderingController.prototype.setLanguage = function (tableOptions) {
            	tableOptions.oLanguage = {
            		"sProcessing" : this.getI18N('datatables.sProcessing'),
            		"sSearch" : this.getI18N('datatables.sSearch'),
            		"sLengthMenu" : this.getI18N('datatables.sLengthMenu'),
            		"sInfo" : this.getI18N('datatables.sInfo'),
            		"sInfoEmpty" : this.getI18N('datatables.sInfoEmpty'),
            		"sInfoFiltered" : this.getI18N('datatables.sInfoFiltered'),
            		"sLoadingRecords" : this.getI18N('datatables.sLoadingRecords'),
            		"sZeroRecords" : this.getI18N('datatables.sZeroRecords'),
            		"sEmptyTable" : this.getI18N('datatables.sEmptyTable'),
            		"oPaginate" : {
            			"sFirst" : this.getI18N('datatables.oPaginate.sFirst'),
            			"sPrevious" : this.getI18N('datatables.oPaginate.sPrevious'),
            			"sNext" : this.getI18N('datatables.oPaginate.sNext'),
            			"sLast" : this.getI18N('datatables.oPaginate.sLast')
            		},
            		"oAria" : {
            			"sSortAscending" : this.getI18N('datatables.oAria.sSortAscending'),
            			"sSortDescending" : this.getI18N('datatables.oAria.sSortDescending')
            		}
            	};
             };	
           };
			
			function transposeArray(aInput) {
			      return Object.keys(aInput[0]).map(
			        function(c) {
			          return aInput.map(function(r) {
			            return r[c];
			          });
			        }
			      );
			}	
			
			function getTotalRow(aInputT, rowInd, colInd) {
				var totalRow = [];
				for (var i = rowInd; i < aInputT.length; i++) {
					for (var j = colInd; j < aInputT[i].length; j++) {
						if (!totalRow[j - colInd]) {
							totalRow[j - colInd] = 0;
						}
						totalRow[j - colInd] += aInputT[i][j];
					}
				}
				
				for(var i = 0; i < totalRow.length; i++){
					totalRow[i] = totalRow[i].toFixed(2);
				}
				
				return totalRow;
			}
			
			/**
			 * 
			 */
			function replaceSpecialChars(id){
				 // Logic to handle special characters like '{' in column id
	            // column.id is typically like {Model71}ChangeOfAddress:{Model71}ConfirmationNumber
	            // So Getting the last word i.e. ConfirmationNumber
	            if(id.indexOf("{") != -1) { 
	               var lastIndex = id.lastIndexOf("}");
	              id = id.substr( lastIndex + 1, id.length );
	            }
	            return id;
			}
			
			/**
			 * 
			 */
			function getColumnDefForDate(selColumn, dateFormat) {
				var col = parseInt(selColumn);
				return {
					"aTargets" : [col],
					"mData" : (function (dateFormat, col) {
						return function (source, type, val) {

							if (type === 'set') {
								//backup original date value
								source[col] = val;

								if (!source.display) {
									source.display = [];
								}

								try {
									//format date value
									var dateVal = val;

									if (!val) {
										source.display[col] = dateVal;
										return;
									}

									var matches = dateVal.match(/\:/g);
									// cannot handle millisecs at the moment
									if (matches.length > 2) {
										var lastIndex = dateVal.lastIndexOf(":");
										dateVal = dateVal.substring(0, lastIndex);
									}

									//get the date object
									var d = new Date(dateVal);
									if (isFinite(d)) {
										if (angularServices && angularServices.filter) {
											dateVal = angularServices.filter('date')(d, dateFormat);
										}
									}
								} catch (e) {
									console.debug("Error occurred while formatting date");
								}
								finally {
									source.display[col] = dateVal;
									return;
								}
							} else if (type === 'display' || type == 'filter') {
								return source.display[col];
							}

							// 'sort' and 'type' both just use the raw data
							return source[col];
						};
					})(dateFormat, col)
				};
			};
			
			 /**
			 * To get unique elements and their count.
			 * @param array
			 * @returns
			 * e.g. var arr = [ "Low", "Low", "Low", "Low", "Low", "Low", "Low", "Low", "Medium","Medium", "Medium", "High" ];
			 * Result: a = [Low,Medium,High], b = [8,3,1]
			 */
			function getUniqueElementsCount(arr) {
             var a = [], b = [], prev;

//           arr.sort(); Commneting the array sorting as it changes the order of elements in array. 
             for ( var i = 0; i < arr.length; i++) {
                if (arr[i] !== prev) {
                   a.push(arr[i]);
                   b.push(1);
                }
                else {
                   b[b.length - 1]++;
                }
                prev = arr[i];
             }

             return [ a, b ];
         }
			
		});