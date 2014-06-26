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
			      
			      var clientDateFormat = "mm/dd/yy";
			      var serverDateFormat = "yy/mm/dd";
			      
			      var self = this;
			      var deferred = jQuery.Deferred();
               this.reportingService.getDateFormats()
                  .done(function(data) {

                  console.log("Retrieved Date Formats: " + data.dateTimeFormat);
                  
                  self.clientDateFormat = clientDateFormat;
                  self.serverDateFormat = serverDateFormat;
                  
                  deferred.resolve();
               }).fail(function() {
                  deferred.reject();
               });

				
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
						this.createChart().done(function(){
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
						var enums = null;
			
						//model data must be added from server side
						if(qualifier[0] != 'modelData' || this.reportingService.modelData){
							enums = this.reportingService.getEnumerators2(qualifier[0], qualifier[1]);	
						}
						
						if(!enums){
							return;
						}
						Object.keys(inData).forEach(function(key) {
							for ( var item in enums)
	                          {
	                             if (enums[item].id == key)
	                             {
	                            	 if(enums[item].name != key){
	                            		 inData[enums[item].name] = inData[key];
		                                 delete inData[key];	 
		                                 break;
	                            	 }
	                             }
	                          }
						});
					}
				};
				
				/**
				 * 
				 */
				ReportRenderingController.prototype.createChart = function() {
					var localDeferred = jQuery.Deferred();
					
					
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
										localDeferred.resolve();
									}).fail(function() {
								document.body.style.cursor = "default";
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
            ReportRenderingController.prototype.refreshPreview = function(scopeController, report, parameters) {
				var deferred = jQuery.Deferred();
            	
            	if (report) {
					this.report = report;
				}
            	
            	if(parameters){
            		this.parameters = parameters;	
            	}
            	
            	//reset
            	this.tableOptions = null;
            	
				var self = this;
				if(this.report.dataSet.type === 'seriesGroup' && this.report.layout.subType == this.reportingService.metadata.layoutSubTypes.table.id){
						this.getReportData(self.report, self.parameters).done(
								function(data) {
									self.refreshSeriesTable(data, scopeController);
									deferred.resolve();
								}).fail(function(err) {
							console.log("Failed getting Preview Date: showing dummy data" + err);
							deferred.reject();
						});	
						
				}else if (this.report.dataSet.type === 'seriesGroup'
					&& this.report.layout.subType == this.reportingService.metadata.layoutSubTypes.chart.id) {

					this.renderReport(self.report)
							.done(function() {
								deferred.resolve();
							}).fail(function() {
								deferred.reject();
							});
				} else {
					this.refreshRecordSet(scopeController);
				}
				
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
			    //apply ui terminologies
			    this.performUII18n(data, this.report);
			
			    self = this;
			    var inData = data;
			
			    //detect Table drawing mode
			    var cumulantsAsRow = true;
			    var dimensionAsRow = true;
			
			    if (self.report.layout.table.dimensionDisplay != self.reportingService.metadata.cumulantsDisplay.rows.id) {
			        dimensionAsRow = false;
			    }
			
			    if (self.report.layout.table.cumulantsDisplay != self.reportingService.metadata.cumulantsDisplay.rows.id) {
			        cumulantsAsRow = false;
			    }
			    var fact_count = (this.report.dataSet.fact == this.reportingService.metadata.objects.processInstance.facts.count.id);
			
			    var tableDrawMode = 1; // 2, 3, 4, 5, 6 based on Dimensions as columns/Rows AND/OR Cumulants as columns/Rows
			
			    var tableParameters = {};
			    tableParameters.numberOfColHeaders = 1;
			    //tableParameters.numberOfRowHeaders = 1;
			    tableParameters.rowHeaderIndex = 0;
			    tableParameters.addLastRowAsFooter = false;
			
			
			    var addTotalRow = false;
			
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
			
			    if (this.tableDrawMode == 4 || this.tableDrawMode == 6) {
			        this.tableOptions = {
			            aoColumnDefs: [{
			                sDefaultContent: "-",
			                sClass: "",
			                aTargets: ["_all"]
			            }, {
			                bVisible: false,
			                aTargets: [0]
			            }],
			            "aLengthMenu": [
			                [5, 10, 25, 50, 100, 200, -1],
			                [5, 10, 25, 50, 100, 200, "All"]
			            ]
			        };
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
			        var dimension = primaryObject.dimensions[this.report.dataSet.groupBy];
			        if (dimension) {
			            dimensionName = dimension.name;
			        }
			
			        if (tableDrawMode == 3 || tableDrawMode == 4) {
			            var baseTableIndex = 0;
			            var h1 = [dimensionName]; // header line one
			            var h2 = ["Cumulants"]; // header line two
			
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
			            var h1 = ["Series"]; // header line one
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
			            var totalRow = ["Total"] // TODO: I18n
			            totalRow = totalRow.concat(getTotalRow(baseTable, 2, 1));
			            baseTable.push(totalRow);
			        }
			
			        if (tableDrawMode == 4 || tableDrawMode == 6) {
			            baseTable = transposeArray(baseTable);
			        }
			    } else { // fact is count
			        var baseTableIndex = 0;
			        var h1 = ["Series"]; // header line one
			
			        //for selected number of cumulants
			
			        baseTable.push(h1);
			        baseTableIndex++;
			
			        var rowHeaderAdded = false;
			
			        for (var seriesName in inData) {
			            h1.push(seriesName);
			
			            for (var j = 0; j < inData[seriesName].length; j++) {
			                if (!rowHeaderAdded) {
			                   inData[seriesName][j][0] = this.formatDate(inData[seriesName][j][0], self.serverDateFormat, self.clientDateFormat);
			                    baseTable.push(inData[seriesName][j]);
			                } else {
			                    baseTable[baseTableIndex + j] = baseTable[baseTableIndex + j].concat(inData[seriesName][j].slice(1));
			                }
			            }
			            rowHeaderAdded = true;
			        }
			
			        if (addTotalRow) {
			            var totalRow = ["Total"] // TODO: I18n
			            totalRow = totalRow.concat(getTotalRow(baseTable, 1, 1));
			            baseTable.push(totalRow);
			        }
			
			        if (tableDrawMode == 2) {
			            baseTable = transposeArray(baseTable);
			        }
			    }
			
			    tableArray = baseTable;
			
			    //set the data in parent scope
			    scopeController.tableArray = baseTable;
			    scopeController.tableParameters = tableParameters;
         };
		
         /**
          * 
          */
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
               if (column.type.id == this.reportingService.metadata.durationType.id) {
                  columnDisplayName += " (" + this.report.dataSet.columns[x].metaData.durationUnit + ")";
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
     			scopeController.rows = [];
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
      } else if (selectedColumns[selColumn].id == this.
               reportingService.metadata.objects.activityInstance.dimensions.criticality.id) {
         //Formatting Criticality data to display string values
         var qualifier = ["preferenceData", "criticality"];
         
         var enumItems = this.reportingService.getEnumerators2(qualifier[0], qualifier[1]);
         
         for ( var row in data)
         {
            var record = data[row];
            var criticality = this.getCriticalityName(record[selColumn], enumItems);
            record[selColumn] = criticality.name;  
         }
         
      } else if(selectedColumns[selColumn].type.id == this.
               reportingService.metadata.timestampType.id) {
         for ( var row in data)
         {
            var record = data[row];
            if (record[selColumn])
            {
               var dateStr = record[selColumn]
               var datePart = "";
               var timePart = "";
         
               var dateParts  = dateStr.split(" ");
               if (dateParts.length == 1) {
                  datePart = dateParts;
               } else if (dateParts.length >= 2) {
                  var timeParts = dateParts[1].split(":"); // Get 2 Parts, and stripoff seconds part
                  datePart = dateParts[0];
                  timePart = timeParts[0] + ":" + timeParts[1];
               }
               
               record[selColumn] = this.formatDate(datePart, self.serverDateFormat, self.clientDateFormat);
               
               record[selColumn] = record[selColumn] + " " + timePart;
               console.log("Final Date:" + record[selColumn]);
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
				
				/**
				 * 
				 */
				ReportRenderingController.prototype.getCriticalityName = function(criticalityRating, enumItems)
				{
				   criticalityRating *= 1000;
				   var self = this;//enumItems.forEach(function(item)
				   for ( var i = 0; i < enumItems.length; i++)
				   {
				      if (criticalityRating > enumItems[i].rangeFrom && criticalityRating <= enumItems[i].rangeTo)
                  {
                     return enumItems[i];
                  }
				   }
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
		});