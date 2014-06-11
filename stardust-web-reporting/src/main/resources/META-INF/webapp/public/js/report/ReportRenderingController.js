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
		[ "bpm-reporting/public/js/report/AngularAdapter",
				"bpm-reporting/public/js/report/ReportingService", "bpm-reporting/public/js/report/I18NUtils" ],
		function(AngularAdapter, ReportingService, I18NUtils) {
			var angularCompile = null;
			return {
				create : function(angularCompile1) {
					var controller = new ReportRenderingController();
					angularCompile = angularCompile1;
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
				ReportRenderingController.prototype.renderReport = function(report) {
					this.initialize(report);
					var deferred = jQuery.Deferred();
					var self = this;

					if (this.report.layout.type == 'table') {
						this.createTable();
					} else if (this.report.layout.type == 'document') {
						self.getReportData(self.report, self.parameters)
								.done(
										function(data) {
											console.log("Data for Document");
											console.log(data);
											
											var html = "<html ng-app><head>"
													// TODO Read local
													+ "<script src='"
													+ self.reportingService
															.getRootUrl()
													+ "/plugins/bpm-reporting/public/js/libs/angular/angular-1.2.11.js'>"
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
			                location: 'ne',
			                placement: 'outside',
			                fontSize: '11px'
						},
						highlighter : {},
						cursor : {},
						zoom : {},
						seriesColors: [ "#4bb2c5", "#c5b47f", "#EAA228", "#579575", "#839557", "#958c12",
						                 "#953579", "#4b5de4", "#d8b83f", "#ff5800", "#0085cc"]
					};

					// Copy configuration from Report Definition

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
					chartOptions.seriesDefaults.trendline.show = this.report.layout.chart.options.seriesDefaults.trendline.show;
					chartOptions.seriesDefaults.rendererOptions = {
						animation : {
							speed : 2500
						}
					};

					// TODO There is more

					if (this.report.layout.chart.type === this.reportingService.metadata.chartTypes.xyPlot.id) {
						// Use default series renderer

						if (this.getFirstDimension().type == this.reportingService.metadata.timestampType) {
							chartOptions.axes.xaxis.renderer = jQuery.jqplot.DateAxisRenderer;
						} else if (this.getFirstDimension().type == this.reportingService.metadata.enumerationType) {
							chartOptions.axes.xaxis.renderer = jQuery.jqplot.CategoryAxisRenderer;
						}

						chartOptions.axes.xaxis.tickRenderer = jQuery.jqplot.CanvasAxisTickRenderer;
						
					} else if (this.report.layout.chart.type === this.reportingService.metadata.chartTypes.candlestickChart.id) {
						if (this.getFirstDimension().type == this.reportingService.metadata.timestampType) {
							chartOptions.axes.xaxis.renderer = jQuery.jqplot.DateAxisRenderer;
						} else {
							chartOptions.axes.xaxis.renderer = jQuery.jqplot.CategoryAxisRenderer;
						}
						chartOptions.series.push({
							renderer:$.jqplot.OHLCRenderer, rendererOptions:{candleStick:false }
						})
						
					} else if (this.report.layout.chart.type === this.reportingService.metadata.chartTypes.barChart.id) {
						chartOptions.seriesDefaults.renderer = $.jqplot.BarRenderer;
						chartOptions.seriesDefaults.rendererOptions = {
							animation : {
								speed : 2500
							},
							fillToZero : true
						};

						if (this.getFirstDimension().type == this.reportingService.metadata.timestampType) {
							chartOptions.axes.xaxis.renderer = jQuery.jqplot.DateAxisRenderer;
						} else {
							chartOptions.axes.xaxis.renderer = jQuery.jqplot.CategoryAxisRenderer;
						}

						chartOptions.axes.xaxis.tickRenderer = jQuery.jqplot.CanvasAxisTickRenderer;
						
						chartOptions.axes.yaxis.pad = 1.05;
					} else if (this.report.layout.chart.type === this.reportingService.metadata.chartTypes.bubbleChart.id) {
						chartOptions.seriesDefaults.renderer = $.jqplot.BubbleRenderer;
						chartOptions.seriesDefaults.rendererOptions = {
							bubbleGradients : true
						};
					}else if (this.report.layout.chart.type === this.reportingService.metadata.chartTypes.donutChart.id) {
						chartOptions.seriesDefaults.renderer = jQuery.jqplot.DonutRenderer;
						chartOptions.seriesDefaults.rendererOptions = {
					        // Donut's can be cut into slices like pies.
					        sliceMargin: 3,
					        // Pies and donuts can start at any arbitrary angle.
					        startAngle: -90,
					        showDataLabels: true,
					        // By default, data labels show the percentage of the donut/pie.
					        // You can show the data 'value' or data 'label' instead.
					        series: [{label: 'A1'},{label: 'A2'}],
					        highlighter: {
					            show: true,
					            showLabel: true,
					            formatString: '%s - %d. X, %d Y'
					        },
					        pointLabels: {
			                    show: true
			                },
						};
					} else if(this.report.layout.chart.type === this.reportingService.metadata.chartTypes.pieChart.id) {
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
				ReportRenderingController.prototype.performUII18n = function(inData, report){
					if((typeof report_data === 'undefined')){
						var primaryObject = this.reportingService.metadata.objects[report.dataSet.primaryObject];
						var dimension = primaryObject.dimensions[report.dataSet.groupBy];
						
						//if groupby is empty or none
						if(!dimension){
							Object.keys(inData).forEach(function(key) {
								inData[primaryObject.name] = inData[key];
		                        delete inData[key];	
							});
						}

						if (dimension && dimension.enumerationType) {
							var qualifier = dimension.enumerationType.split(":");
							var enums = this.reportingService.getEnumerators2(qualifier[0], qualifier[1]);
							if(!enums){
								return;
							}
							Object.keys(inData).forEach(function(key) {
								for ( var item in enums)
		                          {
		                             if (enums[item].id == key)
		                             {
		                            	 inData[enums[item].name] = inData[key];
		                                 delete inData[key];
		                                break;
		                             }
		                          }
							});
						}
					}
				};
				
				/**
				 * 
				 */
				ReportRenderingController.prototype.createChart = function() {
					var deferred = this.getReportData(this.report, this.parameters);
					var self = this;

					document.body.style.cursor = "wait";

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
										
										var data = {};
										data.seriesGroup = [];
										var seriesIds = [];
										for(var prop in inData){
											data.seriesGroup.push(inData[prop]); 
											seriesIds.push(prop);
										}
										
										if (self.report.dataSet.firstDimension === self.reportingService.metadata.objects.processInstance.dimensions.priority.id)
										{
										   var qualifier = [ "staticData", "priorityLevel" ];
										   var enumItems = self.reportingService.getEnumerators2(qualifier[0], qualifier[1]);

										   data.seriesGroup.forEach(function(group)
										   {
										      for ( var i = 0; i < group.length; i++)
										      {
										         for ( var item in enumItems)
										         {
										            if (enumItems[item].id == group[i][0])
										            {
										               group[i][0] = enumItems[item].name;
										               break;
										            }
										         }
										      }
										   })
										}
										
										if (self.report.dataSet.firstDimension === self.reportingService.metadata.objects.
										                                    processInstance.dimensions.priority.id)
                              {
										   var qualifier = ["staticData", "priorityLevel"];
										   var enumItems = self.reportingService.getEnumerators2(qualifier[0], qualifier[1]);
                              
   										data.seriesGroup.forEach(function(group) {
   										   for ( var i = 0; i < group.length; i++)
                                    {
   								            for ( var item in enumItems)
   								            {
   								               if (enumItems[item].id == group[i][0])
   								               {
   								                  group[i][0] = enumItems[item].name;
   								                  break;
   								               }
   								            }
                                    }
   										})
                              }
										
										console
												.log("Report Data before preprocessing");
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
												.createChartOptions(seriesIds);
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
																				"<p>Empty data set retrieved.</p>");

															}
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

                if(this.reportData){
                    deferred.resolve(this.reportData);
                }
                var self = this;
                self.reportingService.retrieveData(report, parameters)
                .done(
                      function(data) {
                        deferred.resolve(data);
                      }).fail(function(data) {
                   deferred.reject(data);
                });
                
                return deferred.promise();
                
             };
            
            //TABLE data processing
            /**
             * 
             */
            ReportRenderingController.prototype.refreshPreview = function(report, scopeController, parameters) {
            	if (report) {
					this.report = report;
				}

            	this.parameters = parameters;
            	
				var self = this;
				if(this.report.dataSet.type === 'seriesGroup' && this.report.layout.subType == this.reportingService.metadata.layoutSubTypes.table.id){
						this.getReportData(self.report, self.parameters).done(
								function(data) {
									self.refreshSeriesTable(data, scopeController);
								}).fail(function(err) {
							console.log("Failed getting Preview Date: showing dummy data" + err);
						});	
				}else if (this.report.dataSet.type === 'seriesGroup'
					&& this.report.layout.subType == this.reportingService.metadata.layoutSubTypes.chart.id) {
					var deferred = jQuery.Deferred();

					this.renderReport(self.report)
							.done(function() {
								deferred.resolve();
							}).fail(function() {
								deferred.reject();
							});

					return deferred.promise();
				} else {
					this.refreshRecordSet(scopeController);
				}
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
				   //apply ui terminologies
				   this.performUII18n(data, this.report);	
				
                   self= this;
                   var configurations = self.getCumulantsTableConfig();
                   var disableSorting = configurations.disableSorting;
                   var multi_headers = configurations.multi_headers;
                   var dimensionAsRow = false;
                   var cumulantsAsRow = false;
                   
                   if(self.report.layout.table.dimensionDisplay == self.reportingService.metadata.cumulantsDisplay.rows.id){
                	   var dimensionAsRow = true;  
                   }

                   if(self.report.layout.table.cumulantsDisplay == self.reportingService.metadata.cumulantsDisplay.rows.id){
                	   var cumulantsAsRow = true;
                	   var dimensionAsRow = true; //TODO:review later
                   }
                                          
                   var fact_count = (this.report.dataSet.fact == this.reportingService.metadata.objects.processInstance.facts.count.id);
                   var span = this.report.layout.table.selectedCumulants.length;
                   
                   //transform data
                   var inputArray = [];

                   //if fact != count
                   if (!fact_count) {

                	 //position of cumulants in response json
                	 var INDEX = {
                			maximum : 1,
							minimum : 2,
							average : 3,
							stdDeviation : 4,
							count : 5
					  };
                	 
                	 var PROP_KEY_PREFIX = "reporting.definitionView.layout.table.cumulant.";
                	 
                	 var CUMULANTS_MSG = {
                			maximum : this.getI18N(PROP_KEY_PREFIX + "maximum"),
                			average : this.getI18N(PROP_KEY_PREFIX + "average"),
							minimum : this.getI18N(PROP_KEY_PREFIX + "minimum"),
							stdDeviation : this.getI18N(PROP_KEY_PREFIX + "stdDeviation"),
							count : this.getI18N(PROP_KEY_PREFIX + "count"),
							total : this.getI18N(PROP_KEY_PREFIX + "total")
					  }; 
                     
                     
                     var dimensionArray = [];
                     inputArray.push(dimensionArray);

                     if(!dimensionAsRow){
                  	   dimensionArray.push('', 1);   
                     }

                     var seriesArray = [];
                     inputArray.push(seriesArray);
                     seriesArray.push("");
                     
                     var dimensionArrayComplete = false;

                     for (var prop in data) {
                       var dimensionIndex = 0;

                       for (var j = 0; j < data[prop].length; j++) {

                         var inputArrayIndex = 2 + dimensionIndex++ * span;

                         //prepare header1: cumulating interval header
                         if (!dimensionArrayComplete) {
                           dimensionArray.push(data[prop][j][0]);
                           dimensionArray.push(span);

                           for(var i in this.report.layout.table.selectedCumulants){
                        	   inputArray.push([CUMULANTS_MSG[this.report.layout.table.selectedCumulants[i]]]);
                           }
                         }

                         //populate cumulant data
                         for(var i in this.report.layout.table.selectedCumulants){
                        	 inputArray[inputArrayIndex++].push(data[prop][j][INDEX[this.report.layout.table.selectedCumulants[i]]]);
                         }  
                       }

                       dimensionArrayComplete = true;

                       //if groupby is selected
                       //prepare groupby header
                       seriesArray.push(prop);
                     }

                     //if display total is selected
                     if(this.report.layout.table.displayTotals){
                         var total_cols = seriesArray.length - 1;
                         if (seriesArray.length > 2) {
	                           inputArray[1].push("Total");
	                           total_cols = seriesArray.length - 2;
	                         }	
	
	                         //inputArray.push(["Total"]);
	
	                         for (var j = 0; j < total_cols; j++) {
	                           //inputArray[inputArray.length - 1].push(0); //set default value
	                         }
	                         

	                  for (var i = 2; i < inputArray.length; i++) {
							var sum = 0;

							for (var j = 1; j < inputArray[i].length; j++) {
								sum += inputArray[i][j];
								// if display total is selected
								// inputArray[inputArray.length - 1][j] += inputArray[i][j];
							}

							if (seriesArray.length > 2) {
								inputArray[i].push(sum);
							}
						}
						
						var sum = 0;
						
						for (var i = 2; i < inputArray.length-1; i++) {
							sum += inputArray[i][inputArray[i].length-1];
						}
						//inputArray[inputArray.length-1].push(sum);
                   }  

                   } else { // fact is count
                     var seriesArray = [];
                     inputArray.push(seriesArray);
                     seriesArray.push("");

                     var dimensionArrayComplete = false;

                     var seriesIndex = 0;

                     for (var prop in data) {

                       for (var j = 0; j < data[prop].length; j++) {

                         if (!dimensionArrayComplete) {
                           inputArray.push([data[prop][j][0]]);
                         }

                         inputArray[j + 1].push(data[prop][j][1]);
                       }

                       dimensionArrayComplete = true;

                       seriesArray.push(prop);
                       seriesIndex++;
                     }

                     //if display total is selected
                     if(this.report.layout.table.displayTotals){
                         var total_cols = seriesArray.length - 1;
                         if (seriesIndex > 1) {
                           inputArray[0].push("Total");
                           total_cols = seriesArray.length - 2;
                         }

                         inputArray.push(["Total"]);

                         for (var j = 0; j < total_cols; j++) {
                           inputArray[inputArray.length - 1].push(0); //set default value
                         }
                         
						for (var i = 1; i < inputArray.length-1; i++) {
							var sum = 0;
							
							for (var j = 1; j < inputArray[i].length; j++) {
								sum += inputArray[i][j];
								// if display total is selected
								inputArray[inputArray.length - 1][j] += inputArray[i][j];
							}

							if (seriesIndex > 1) {
								inputArray[i].push(sum);
							}
						}
						
						var sum = 0;
						
						for (var i = 1; i < inputArray.length-1; i++) {
							sum += inputArray[i][inputArray[i].length-1];
						}
						inputArray[inputArray.length-1].push(sum);
                     }
                   }
                   
                   
                   
					  // server data must be converted to following format - just for reference, is not used
			      var countgroupbyCumulantsCol1 = [
			        ['', 'A1', 'A2', 'A3', 'Total'], //header -> this and all rows below it should match
			        ['Jan', 22, 3, 4, 29],
			        ['Feb', 6, 7, 8, 21],
			        ['Total', 28, 10, 12, 50]
			      ];

			      var countCumulantsCol1 = [
			        ['', 'Activities'], //header -> this and all rows below it should match
			        ['Jan', 22, ],
			        ['Feb', 21],
			        ['Total', 41]
			      ];

			      var nonCountCumulantsCol1 = [
			        ['Jan', 5, 'Feb', 5], //header 1, it's a pair {title, span}
			        ['', 'Activities'], //header 2-> this and all rows below it should match
			        ['Average', 22, ],
			        ['Min', 21],
			        ['Max', 30],
			        ['Std Dev', 30],
			        ['Count', 30],
			        ['Average', 28, ],
			        ['Min', 22],
			        ['Max', 37],
			        ['Std Dev', 33],
			        ['Count', 31]
			      ];

			      var nonCountGroupbyCumulantsCol1 = [
				        ['', 1,'Jan', 5, 'Feb', 5], //header 1, it's a pair {title, span}
				        ['', 'A1', 'A2'],//header 2-> this and all rows below it should match
				        ['Average', 22, 12],
				        ['Min', 21, 2],
				        ['Max', 30, 4],
				        ['Std Dev', 30, 5],
				        ['Count', 30, 5],
				        ['Average', 28, 5],
				        ['Min', 22, 54],
				        ['Max', 37, 44],
				        ['Std Dev', 33, 45],
				        ['Count', 31, 56]
				      ];
			      
/*                       //TODO: Replace following with live report data in the give format, also conside the total flag
                   var tableArray = nonCountGroupbyCumulantsCol; //This data should come from Report-data result
                   if(this.report.dataSet.fact == this.reportingService.metadata.objects.processInstance.facts.count.id){
                	   tableArray = countCumulantsCol;
                	   if(this.report.dataSet.groupBy == 'activityName'){
                		   tableArray = countgroupbyCumulantsCol;   
                	   }
                   }else{
                	   tableArray = nonCountCumulantsCol;
                	   if(this.report.dataSet.groupBy == 'activityName'){
                		   tableArray = nonCountGroupbyCumulantsCol;   
                	   }
                   }
*/                       
                   tableArray = inputArray;
                   
                   //Process
                   var TEMPLATE = "<table cellpadding=\"0\" cellspacing=\"0\" class=\"dataTable\"><thead><tr>_HEADERS_</tr></thead><tbody><tr sd-table-data=\"row in rows\">_COLUMNS_</tr></tbody></table>";
                   var options = [];

                   if (multi_headers) {
                     if (!dimensionAsRow) {
                       TEMPLATE = "<table cellpadding=\"0\" cellspacing=\"0\" class=\"dataTable\"><thead><tr>_TOPHEADERS_</tr></thead><thead><tr>_HEADERS_</tr></thead><tbody><tr options=_OPTIONS_ sd-table-data=\"row in rows\">_COLUMNS_</tr></tbody></table>";
                     } 
                     else{
                       TEMPLATE = "<table cellpadding=\"0\" cellspacing=\"0\" class=\"dataTable\"><thead><tr>_HEADERS_</tr></thead><tbody><tr options=_OPTIONS_ sd-table-data=\"row in rows\">_COLUMNS_</tr></tbody></table>";
                     }
                     options = disableSorting;
                   }
                   var v1 = jQuery.extend({}, TEMPLATE);

                   var TEMPLATE_COPY = "";
                   for (v in v1) {
                     TEMPLATE_COPY += v1[v];
                   }

                   if (multi_headers) {
                     var topheaders = tableArray[0];
                     tableArray = tableArray.splice(1);

                     var topHeaders = "";

                     if (dimensionAsRow) {
                       var topHeaderArr = [];
                       topHeaderArr.push('');
                       for (i = 0; i < topheaders.length - 1; i = i + 2) {
                         var h = topheaders[i];
                         for (x = 0; x < topheaders[i + 1]; x++) {
                           topHeaderArr.push(h);
                           h = "";
                         }
                       }
                       for (i = 0; i < tableArray.length; i++) {
                         //insert column data
                         tableArray[i].splice(0, 0, topHeaderArr[i]);
                       }
                     } else {
                       for (i = 0; i < topheaders.length - 1; i = i + 2) {
                         topHeaders += "<th colspan=" + topheaders[i + 1] + ">" + topheaders[i] + "</th>";
                       }
                     }

                     if (!dimensionAsRow) {
                       TEMPLATE_COPY = TEMPLATE_COPY.replace("_TOPHEADERS_", topHeaders);
                     }
                   }

                   TEMPLATE_COPY = TEMPLATE_COPY.replace("_OPTIONS_", options);

                   //transform the array
                   if (!dimensionAsRow) {
                	   
                	   tableArray = transposeArray(tableArray);
                   }

                   var columns = tableArray[0];

                   var headers = "";
                   var cols = "";

                   if (multi_headers && dimensionAsRow) {
                     //for (i = 0; i < topheaders.length - 1; i = i + 2) {
                       //cols += "<td style=\"font-weight:bold; font-size:small\" rowspan=" + topheaders[i + 1] + ">" + topheaders[i] + "</td>";
                     //}
                   }

                   for (x in columns) {
                     var column = columns[x];
                     headers += "<th>" + column + "</th>";
                     if (x == 0) {
                       cols += "<td style=\"font-weight:bold; font-size:small\">{{row[" + x + "]}}</td>";
                     }else if(x == 1 && multi_headers && dimensionAsRow){
                    	 cols += "<td style=\"font-weight:bold; font-size:small\">{{row[" + x + "]}}</td>";
                     }else {
                       cols += "<td style=\"text-align:center\">{{row[" + x + "]}}</td>";
                     }
                   }

                   TEMPLATE_COPY = TEMPLATE_COPY.replace("_HEADERS_", headers);

                   TEMPLATE_COPY = TEMPLATE_COPY.replace("_COLUMNS_", cols);

                   //E
                   //create an angular element. (this is our "view")
   	               	
                   var el = angular.element(TEMPLATE_COPY);
                   
                   var compiled = angularCompile(el);
                   
                   var divElem = angular.element(".dynamicTable");
                   
                   //append our view to the element of the directive.
                   divElem.html(el);
                   
   	               compiled(divElem.scope());
                   
   	               scopeController.rows = tableArray.splice(1);
   	               
   	               scopeController.updateView();
                  };
		
		ReportRenderingController.prototype.refreshRecordSet = function(scopeController) {
			   var columns = this.reportingService.getColumnDimensions(this.report);
		
            var TEMPLATE = "<table cellpadding=\"0\" cellspacing=\"0\" class=\"dataTable\"><thead><tr>_HEADERS_</tr></thead><tbody><tr sd-table-data=\"row in rows\">_COLUMNS_</tr></tbody></table>";
               
            var v1 = jQuery.extend({}, TEMPLATE);
            var TEMPLATE_COPY = "";
            for (v in v1) {
               TEMPLATE_COPY += v1[v];
            }
               
            var headers = "";
            var cols = "";
               
            for (x in columns) {
               var column = columns[x];
               var columnDisplayName = column.name;
               if (this.report.dataSet.groupBy != null && this.report.dataSet.groupBy != 'None') {
                  var aggregations = this.reportingService.metadata.recordSetAggregationFunctions;
                  for ( var n in aggregations ) {
                     if (this.report.dataSet.columns[x].metaData.aggregationFunction === aggregations[n].id)
                     {
                        columnDisplayName += " (" + aggregations[n].name + ")";
                        break;
                     }
                  }
               }
               headers += "<th>" + columnDisplayName + "</th>";
                   var col = column.id;
                   console.log(col);
                   col = replaceSpecialChars(col);
                   var style = "";
                   if (column.type.id == this.reportingService.metadata.timestampType.id)
                   {
                      style = " style = \"text-align: right;\"";
                   }
                   cols += "<td" + style +">{{row." + col + "}}</td>";
            }
            TEMPLATE_COPY = TEMPLATE_COPY.replace("_HEADERS_", headers);
            TEMPLATE_COPY = TEMPLATE_COPY.replace("_COLUMNS_", cols);
              
            jQuery(".dynamicTable").html(TEMPLATE_COPY);
            
            var divElem = angular.element(".dynamicTable");
            angularCompile(divElem)(divElem.scope());
            
            
            if (columns.length != 0)
            {   
               var self = this;
               setTimeout(function () {
                 	   self.refreshPreviewData(scopeController);
                    }, 200);
            } 
     		};
     		
     		/**
          * 
          */
         ReportRenderingController.prototype.refreshPreviewData = function(scopeController) {
            var self = this;	
            
        	   this.getReportData(this.report, this.parameters).done(
     		function(data) {
     			// Format data before displaying the Results
             	  scopeController.rows = self.formatPreviewData(data.rows);
     			scopeController.updateView();
     		}).fail(function(err) {
     			console.log("Failed getting Preview Date: " + err);
     		});   
         };
		
	    
/**
 * 
 */
ReportRenderingController.prototype.formatPreviewData = function(data) {
   var self = this;
      
   var selectedColumns =  this.reportingService.getColumnDimensions(this.report);
   
   for ( var selColumn in selectedColumns)
   {
      if (selectedColumns[selColumn].id == this.
               reportingService.metadata.objects.processInstance.dimensions.priority.id)
      {// Formatting Priority to display priority levels as Low, medium etc
         var qualifier = ["staticData", "priorityLevel"];
         
         var enumItems = this.reportingService.getEnumerators2(qualifier[0], qualifier[1]);
           
         for ( var row in data)
         {
            var record = data[row];
            for ( var item in enumItems)
            {
               if (enumItems[item].id == record[selColumn])
               {
                  record[selColumn] = enumItems[item].name;
                  break;
               }
            }
         }
      } else if (selectedColumns[selColumn].id == this.
               reportingService.metadata.objects.processInstance.dimensions.state.id) {
      // Formatting Process State to display string states as Alive, completed etc 
         var qualifier = ["staticData", "processStates"];
         
         var enumItems = this.reportingService.getEnumerators2(qualifier[0], qualifier[1]);
           
         for ( var row in data)
         {
            var record = data[row];
            if(!record[selColumn]){
          	  record[selColumn] = enumItems[record[selColumn]].name;  
            }
         }
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

		ReportRenderingController.prototype.getI18N = function(key) {
			return I18NUtils.getProperty(key);
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
			}
			
			function transposeArray(aInput) {
			      return Object.keys(aInput[0]).map(
			        function(c) {
			          return aInput.map(function(r) {
			            return r[c];
			          });
			        }
			      );
			}			
			
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
		});