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
 * @author Yogesh.Manware
 * 
 */
define([ "bpm-reporting/public/js/report/I18NUtils",
		"bpm-reporting/public/js/report/AngularAdapter",
		"bpm-reporting/public/js/report/ReportingService",
		"bpm-reporting/public/js/report/ReportRenderingController" ],
		function(I18NUtils, AngularAdapter, ReportingService,
				ReportRenderingController) {
			var angularCompile;
			return {
				create : function(angular) {
					var controller = new ReportDefinitionController();

					var angularAdapter = new bpm.portal.AngularAdapter(null);

					// initialize controller and services
					angularAdapter.initializeModule(angular);

					// bootstrap module
					angularAdapter.initialize(angular);

					controller = angularAdapter
							.mergeControllerWithScope(controller);

					angularCompile = angularAdapter.getCompiler();

					var renderingController = ReportRenderingController
							.create(angularCompile);

					controller.initialize(renderingController, "my Report 111",
							"Report111", "instance");

					controller.renderingController.refreshPreview(report_definition, controller);
					
					controller.report = report_definition;
					
					//controller.refreshPreview(report_definition);
					
					controller.updateView();
					
					return controller;
				}
			};

			/**
			 * 
			 */
			function ReportDefinitionController() {
				this.reportingService = ReportingService.instance();

				/**
				 * 
				 */
				ReportDefinitionController.prototype.getI18N = function(key) {
					return I18NUtils.getProperty(key);
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.initialize = function(
						renderingController, name, path, viewMode) {
					var self = this;
					this.renderingController = renderingController;

					
					//TODO: remove after testing
					//report_definition.layout.table.preview = true;
					
					report_data = this.reportingService
							.getTestSimpleReportData(report_definition); // TODO:
																	// this
																	// would be
																	// html
																	// based
																	// script
																	// variable


					
					jQuery("#reportDefinitionView").css("visibility", "visible");
				};
				
	            ReportDefinitionController.prototype.refreshPreview = function(report) {
	            	if (report) {
						this.report = report;
					}
					var self = this;
					if(this.report.dataSet.type === 'seriesGroup' && this.report.layout.subType == this.reportingService.metadata.layoutSubTypes.table.id){
							this.renderingController.getPreviewData(report_definition).done(
									function(data) {
										self.refreshPreview1(data);
									}).fail(function(err) {
								console.log("Failed getting Preview Date: showing dummy data" + err);
							});	
					}else if (this.report.dataSet.type === 'seriesGroup'
						&& this.report.layout.subType == this.reportingService.metadata.layoutSubTypes.chart.id) {
						var deferred = jQuery.Deferred();

						this.renderingController.renderReport(self.report)
								.done(function() {
									deferred.resolve();
								}).fail(function() {
									deferred.reject();
								});

						return deferred.promise();
					} else {
						this.refreshRecordSet();
					}
				};
			    
				/**
				 * 
				 */
				ReportDefinitionController.prototype.refreshPreview1 = function(data) {
					   //apply ui terminologies
					   //this.performUII18n(data, this.report);	
					
	                   self= this;
	                   var configurations = this.renderingController.getCumulantsTableConfig();
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
	                   
	   	               compiled(this);
	                   
	   	               this.rows = tableArray.splice(1);
	   	               
	   	               //this.updateView();
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
		});
