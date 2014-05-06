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
		[ "bpm-reporting/js/I18NUtils", "bpm-reporting/js/AngularAdapter",
				"bpm-reporting/js/ReportingService",
				"bpm-reporting/js/ReportRenderingController",
				"bpm-reporting/js/SchedulingController",
				"bpm-reporting/js/utils",
				"bpm-reporting/js/m_codeEditorAce",
				"bpm-reporting/js/m_autoCompleters"],
		function(I18NUtils, AngularAdapter, ReportingService,
				ReportRenderingController, SchedulingController, utils, 
				m_codeEditorAce, m_autoCompleters) {
		      var angularAdapter = null; 
		      var angularCompile = null;
			return {
				create : function(angular, name, path, options) {
					var controller = new ReportDefinitionController();
					var renderingController = ReportRenderingController.create();
					
			        var angularAdapter = new bpm.portal.AngularAdapter(options);

			        //initialize controller and services
			        var angularModule = angularAdapter.initializeModule(angular);
			        controller.initializeAutocompleteDir(angularModule);

			        //bootstrap module
			        angularAdapter.initialize(angular);

					controller = angularAdapter
							.mergeControllerWithScope(controller);

					controller.initialize(renderingController, name, path);
					
					angularCompile = angularAdapter.getCompiler();

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
						renderingController, name, path) {
					this.constants = {
							ALL_PROCESSES : {id: "allProcesses", name: this.getI18N("reporting.definitionView.additionalFiltering.allprocesses")},
							ALL_ACTIVITIES : {id: "allActivities", name: this.getI18N("reporting.definitionView.additionalFiltering.allactivities")}
					};

					
					this.renderingController = renderingController;
					this.schedulingController = SchedulingController.create();

					this.path = path;

					this.dataSetPanel = jQuery("#dataSetPanel");
					this.layoutPanel = jQuery("#layoutPanel");
					this.dataViewPanel = jQuery("#reportViewPanel");
					this.templatePanel = jQuery("#templatePanel");

					this.firstDimensionTimeBoundariesPanel = jQuery("#firstDimensionTimeBoundariesPanel");
					this.firstDimensionEnumerationSelectionPanel = jQuery("#firstDimensionEnumerationSelectionPanel");

					this.wizardPanels = [];

					this.wizardPanels.push(this.dataSetPanel);
					this.wizardPanels.push(this.layoutPanel);
					this.wizardPanels.push(this.dataViewPanel);
					this.wizardPanels.push(this.templatePanel);

					this.primaryObjectSelect = jQuery("#primaryObjectSelect");
					this.participantsSelect = jQuery("#participantsSelect");
					this.schedulingParticipantsSelect = jQuery("#schedulingParticipantsSelect");
					this.startDateId = jQuery("#startDateId");
					this.endDateId = jQuery("#endDateId");
					this.cumulatedDimensions = [];
					this.selectedColumns = [];
					this.filterSelected = [];
					
					this.scheduling = {
                        "nextExecutionDate" : null,
                        "nextExecutionDateDay" : null
               };
					
					this.factSelect = jQuery("#factSelect");
					this.chartTypeSelect = jQuery("#chartTypeSelect");
					this.layoutSubTypeSelect = jQuery("#layoutSubTypeSelect");
					this.cumulantsDisplaySelect = jQuery("#cumulantsDisplaySelect");
					
					this.editorAnchor = utils.jQuerySelect("#expressionTextDiv").get(0);
					this.expressionEditor = m_codeEditorAce.getJSCodeEditor(this.editorAnchor);
					this.expressionEditor.getEditor().on('blur', function(event)
					         {
					               if (self.selectedComputedColumn != null)
					               {
					                  self.selectedComputedColumn.formula = self.expressionEditor.getValue();
					               }
					         });
					
					this.startDateId.on('blur', function(event)
                        {
                              setTimeout(function () {
                                 self.getNextExecutionDate();;
                             }, 100);
                              
                        });
					
					this.endDateId.on('blur', function(event)
                        {
                              setTimeout(function () {
                                 // Timer set for Date picker value to be updated in
                                 // underlying text control.
                                 self.getNextExecutionDate();;
                             }, 100);
                              
                        });
					
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
					
					this.expressionEditor.loadLanguageTools();
					this.expressionEditor.setSessionData("$keywordList",["test", "air", "word"]);
					this.expressionEditor.disable();
					
					$(this.expressionEditor).on("moduleLoaded",function(event,module){
                  var sessionCompleter;
                  if(module.name==="ace/ext/language_tools"){
                     sessionCompleter = m_autoCompleters.getSessionCompleter();
                     self.expressionEditor.addCompleter(sessionCompleter);
                  }
               });
					
					var self = this;

					document.body.style.cursor = "wait";

					CKEDITOR.replace("documentTemplateEditor", {
						allowedContent : true,
						toolbarGroups : [
								{
									name : 'clipboard',
									groups : [ 'clipboard', 'undo' ]
								},
								{
									name : 'editing',
									groups : [ 'find', 'selection',
											'spellchecker' ]
								},
								{
									name : 'links'
								},
								{
									name : 'insert'
								},
								{
									name : 'forms'
								},
								{
									name : 'tools'
								},
								{
									name : 'document',
									groups : [ 'mode', 'document', 'doctools' ]
								},
								{
									name : 'others'
								},
								'/',
								{
									name : 'basicstyles',
									groups : [ 'basicstyles', 'cleanup' ]
								},
								{
									name : 'paragraph',
									groups : [ 'list', 'indent', 'blocks',
											'align', 'bidi' ]
								}, {
									name : 'styles'
								}, {
									name : 'colors'
								} ]
					});
					
					CKEDITOR.instances["documentTemplateEditor"]
							.on(
									'blur',
									function(e) {
										self.report.layout.document = {
											markup : CKEDITOR.instances["documentTemplateEditor"]
													.getData()
										};
									});

					this.initializeDragAndDrop();

					jQuery.when(self.reportingService
							.refreshPreferenceData(), self.reportingService
							.refreshModelData()).done( function() {
							self.primaryObjectSelect.empty();

							for (var n in self.reportingService.metadata.objects) {
								self.primaryObjectSelect
								.append("<option value='" + n + "'>"
									 + self.reportingService.metadata.objects[n].name
									 + "</option>");
							}

							self.primaryObjectSelect
							.change(function () {
								self.report.dataSet.primaryObject = self.primaryObjectSelect
									.val();

								self
								.changePrimaryObject(false);

								self.populateAutoCompleteKeywordList();

								self.updateView();
							});

							//Participants Select
							self.participantsSelect.empty();
							var modelParticipants = self.reportingService.modelData.participants;
							for (var n in modelParticipants) {
								self.participantsSelect
								.append("<option value='"
									 + modelParticipants[n].id
									 + "'>"
									 + modelParticipants[n].name
									 + "</option>");
							}

							self.participantsSelect.change(function () {
								self.report.storage.location = "participantFolder";
								self.report.storage.participant = self.participantsSelect.val();
								self.updateView();
							});

							//Participants Select
							self.schedulingParticipantsSelect.empty();
							var modelParticipants = self.reportingService.modelData.participants;
							for (var n in modelParticipants) {
								self.schedulingParticipantsSelect
								.append("<option value='"
									 + modelParticipants[n].id
									 + "'>"
									 + modelParticipants[n].name
									 + "</option>");
							}

							self.schedulingParticipantsSelect.change(function () {
								self.report.scheduling.delivery.participant = self.schedulingParticipantsSelect.val();
								self.updateView();
							});

							self.layoutSubTypeSelect.change(function (val) {
								self.report.layout.subType = self.layoutSubTypeSelect.val();

								if (self.reportingService.metadata.layoutSubTypes.table.id == self.layoutSubTypeSelect.val()) {

									if (!self.report.layout) {
										self.report.layout = {};
									}

									if (!self.report.layout.table) {
										self.report.layout.table = {};
									}

									if (!self.report.layout.selectedCumulants) {
										self.report.layout.table.selectedCumulants = [];
									}

								}

								self.populateChartTypes();
								self.updateView();
							});

							self.cumulantsDisplaySelect.change(function () {
								self.report.layout.table.cumulantsDisplay = self.cumulantsDisplaySelect.val();
							});

							self.chartTypeSelect
							.change(function () {
								self.report.layout.chart.type = self.chartTypeSelect
									.val();

								self.updateView();
							});

							self.factSelect
							.change(function () {
								self.report.dataSet.fact = self.factSelect
									.val();

								self.changeFact();
								self.updateView();
							});

							jQuery("#factProcessDataSelect")
							.change(
								function () {
								self.report.dataSet.factProcessData = jQuery(
										this).val();

								self.updateView();
							});

							jQuery("#groupBySelect")
							.change(
								function () {
								self.report.dataSet.groupBy = jQuery(
										this).val();
							});

							jQuery("#propertiesTabs")
							.tabs({
								beforeActivate : function (
									event, ui) {
									if (self.report.layout.type == "document"
										 && ui.newPanel.selector === "#previewTab") {
										// TODO
										// Workaround
										// to make
										// sure that
										// changes
										// are
										// written
										// to markup

										self.report.layout.document = {
											markup : CKEDITOR.instances["documentTemplateEditor"]
											.getData()
										};
									}
								},
								activate : function (
									event, ui) {

									if (ui.newPanel.selector === "#previewTab") {
										self
										.refreshPreview();
									}
								}
							});

							self
							.loadOrCreateReportDefinition(
								name, path)
							.done(
								function () {
								// TODO Need a
								// cleaner
								// initialization
								// function

								self
								.changePrimaryObject(true);
								self.changeFact();
								self
								.changeFirstDimension();
								self.renderingController
								.initialize(self.report);

								self.layoutSubTypeSelect.val(self.report.layout.subType);
								jQuery("#layoutSubTypeSelect").change();

								self.cumulantsDisplaySelect.val(self.report.layout.table.cumulantsDisplay);

								self.chartTypeSelect
								.val(self.report.layout.chart.type);

								self.schedulingController
								.initialize(self.report.scheduling);

								self.runInAngularContext(function (scope) {
									scope.$watch("report.scheduling", function (newValue, oldValue) {
										self.getNextExecutionDate();
									}, true);
								});

								jQuery("#groupBySelect").val(self.report.dataSet.groupBy);
								
								self.factSelect.val(self.report.dataSet.fact);
								
								self.updateView();

								document.body.style.cursor = "default";

								if (self.report.document) {
									CKEDITOR.instances["documentTemplateEditor"]
									.setData(self.report.layout.document.markup);
								}

								jQuery(
									"#reportDefinitionView")
								.css(
									"visibility",
									"visible");

								self.populateAutoCompleteKeywordList();

							}).fail(function ()	{
										console.debug(error);
										document.body.style.cursor = "default";
										jQuery("#reportDefinitionView").css("visibility", "visible");
								});
							console.debug('refreshModelData preferencedata success.............');
						}).fail(function () {
							console.debug('refreshModelData preferencedata falied.............');
					});
				};

				ReportDefinitionController.prototype.initializeAutocompleteDir = function(angularModule) {
					var self = this; 
	
					angularModule.controller("AutocompleteCntr", function($scope) {

			          $scope.data = [];
			          $scope.dataSelected = [];
			          $scope.matchVal = "";

			          /*Retrieve data from our service*/
			          $scope.getMatches = function(serviceName, serachVal) {
			        	  clearTimeout($scope.typingTimer);
			        	  
			        	  $scope.data = [];
			        	  
			        	  $scope.typingTimer = setTimeout(
				              function(){
				            	  self.reportingService.search(serviceName, serachVal).done(function(data) {
					        		  $scope.data = JSON.parse(data);  
					        		  $scope.$apply();
								}).fail(function() {
									console.debug("Error occurred while fetching user from server");
								});
				              },
				              500
			              );
			          };
			        });
			      };
				
				/**
				 * 
				 */
				ReportDefinitionController.prototype.getDefaultValueForEmptyString = function(
						value, defaultValue) {

					if (value == null || value.trim().length == 0) {
						return defaultValue;
					}

					return value;
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.loadOrCreateReportDefinition = function(
						name, path) {
					var deferred = jQuery.Deferred();

					var self = this;

					if (path) {
						this.reportingService.retrieveReportDefinition(path)
								.done(function(report) {

									self.report = report;
									
									self.loadDataSetRecordSet();
									console.log("Loaded report definition:");
									console.log(self.report);

									deferred.resolve();
								}).fail(function() {
									deferred.reject();
								});
					} else {
						// Initialize
						// defaults

						// TODO Get chart options from central place

						self.report = {
							name : name,
							description : "",
							storage : {
								location : "publicFolder",
								state : "created"
							},
							dataSet : {
								type : "seriesGroup",
								primaryObject : "processInstance",
								joinExternalData : false,
								externalJoins : [ {
									joinType : "outer",
									restUri : "http://127.0.0.1:1337/",
									fields : []
								} ],
								computedColumns : [],
								columns : [],
								filters : [],
								factDurationUnit : "d",
								firstDimensionCumulationIntervalCount : 1,
								firstDimensionCumulationIntervalUnit : "d"
							},
							parameters : {},
							layout : {
								type : "simpleReport",
								chart : {
									type : this.reportingService.metadata.chartTypes.xyPlot.id,
									options : {
										animate : true,
										animateReplot : true,
										series : {},
										seriesDefaults : {
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
											},
										},
										axes : {
											xaxis : {
												tickOptions : {
													mark : "outside",
													markSize : 4,
													angle : 0
												}
											},
											x2axis : {},
											yaxis : {
												tickOptions : {
													mark : "outside",
													markSize : 4,
													angle : 0
												}
											},
											y2axis : {}
										},
										legend : {
											location : "w"
										},
										highlighter : {},
										cursor : {
											show : true
										},
										zoom : {}
									}
								},
								table : {
									options : {}
								},
								document : {}
							},
							scheduling : self.schedulingController
									.createDefaultSettings()
						};

						this.report.dataSet.fact = this.getPrimaryObject().facts.count.id;
						
						this.cumulatedDimensions = this.getCumulatedDimensions();

						deferred.resolve();
					}

					return deferred.promise();

				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.getMetadata = function() {
					return this.reportingService.metadata;
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.getPrimaryObject = function() {
					return this.reportingService.metadata.objects[this.report.dataSet.primaryObject];
				};
				
				ReportDefinitionController.prototype.primaryObjectEnumGroup = function(id) {
					var dimension = this.getDimension(id);
					
					if(!dimension){
						dimension = this.getComputedColumnAsDimensions()[id];
					}
					
					if(!dimension){
						return;
					}
					
					if(dimension.metadata && dimension.metadata.isDescriptor){
						return this.getI18N("reporting.definitionView.descriptors");
					}else if(dimension.metadata && dimension.metadata.isComputedType){
						return this.getI18N("reporting.definitionView.computedColumns");
					}
					return this.getI18N("reporting.definitionView." + this.report.dataSet.primaryObject);
				};
				
				ReportDefinitionController.prototype.getPrimaryObjectEnum = function() {
					var dimensionsObj = this.reportingService.metadata.objects[this.report.dataSet.primaryObject].dimensions;
					var enumerators = [];
					for ( var n in dimensionsObj) {
						enumerators.push(dimensionsObj[n]);
					}
					
					dimensionsObj = this.getComputedColumnAsDimensions();
					
					for ( var n in dimensionsObj) {
						enumerators.push(dimensionsObj[n]);
					}

					enumerators.sort(function(object1, object2){
						return object1.name.localeCompare(object2.name);
					});
					
					return enumerators;
				};
				
				ReportDefinitionController.prototype.getAvailableCumulantsEnum = function() {
					var cumulants = this.reportingService.metadata.cumulants;
					var enumerators = [];
					for ( var n in cumulants ) {
						var add = true; 
						if (this.report.layout.table && this.report.layout.table.selectedCumulants) {
							this.report.layout.table.selectedCumulants
									.forEach(function(cumulantId) {
										if (cumulants[n].id == cumulantId) {
											add = false;
										}
									});
						}
						if(add){
							enumerators.push(cumulants[n]);	
						}
					}
					return enumerators;
				};
				
				ReportDefinitionController.prototype.getSelectedCumulantsEnum = function() {
					var enumerators = [];
					if (!this.report.layout.table || !this.report.layout.table.selectedCumulants){
						return enumerators;
					}	
					var cumulants = this.report.layout.table.selectedCumulants;
					var self = this;
					cumulants.forEach(function(cumulantId){
						enumerators.push(self.reportingService.metadata.cumulants[cumulantId]);
					});
					
					return enumerators;
				};
				
				
				ReportDefinitionController.prototype.toggleFilter = function(filter, property) {
					filter.metadata[property] = !filter.metadata[property]; 
					//this.updateView();
				};
				
				ReportDefinitionController.prototype.selectedProcessChanged = function(filter) {
					var self = this;
					if(filter.metadata.selectedProcesses.some(function (id) {return self.constants.ALL_PROCESSES.id == id;})){
						filter.metadata.selectedProcesses = [this.constants.ALL_PROCESSES.id];
					}
				};
				
				/**
				 * 
				 */
				ReportDefinitionController.prototype.getFact = function() {
					return this.getPrimaryObject().facts[this.report.dataSet.fact];
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.getDimension = function(id) {
					return this.getPrimaryObject().dimensions[id];
				};

				ReportDefinitionController.prototype.getComputedColumnAsDimensions = function() {
					var dimensions = {};
					for ( var n in this.report.dataSet.computedColumns) {
						var column = this.report.dataSet.computedColumns[n];

						dimensions[column.id] = {
							id : column.id,
							name : column.name,
							type : this.reportingService.metadata[column.type],
							metadata : {
								isComputedType : true
							}
						};
					}
					
					return dimensions;
				};
				/**
				 * 
				 */
				ReportDefinitionController.prototype.getFirstDimension = function() {
					return this.getPrimaryObject().dimensions[this.report.dataSet.firstDimension];
				};

				ReportDefinitionController.prototype.refreshPreview = function() {
					var self = this;
					if(this.report.layout.subType == this.reportingService.metadata.layoutSubTypes.table.id){
						this.renderingController.getPreviewData().done(
								function(data) {
									self.refreshPreview1(data);
								}).fail(function(err) {
									if(self.report.layout.table.preview){
										self.refreshPreview1();
									}
							console.log("Failed getting Preview Date: showing dummy data" + err);
						});
					}else{
						this.refreshPreview2();
					}
				};

				ReportDefinitionController.prototype.getCumulantsTableConfig = function(){
			    	  if(this.report.dataSet.fact == this.reportingService.metadata.objects.processInstance.facts.count.id){
			    		  return this.countTableConfig;
			    	  }else{
			    		  return this.nonCountTableConfig;  
			    	  }
			    };
			    
				/**
				 * 
				 */
				ReportDefinitionController.prototype.refreshPreview1 = function(data) {
					//delete later 
					//TODO: remove this temporary data post-development
					var countCumulantsCol = {
						  "activity_instances": [
		                         [
		                           "2014_01",
		                           1
		                         ],
		                         [
		                           "2014_04",
		                           3
		                         ]
		                       ]
							};
					
					var countgroupbyCumulantsCol = {
							  "A1": [
							         [
							           "2014_01",
							           0
							         ],
							         [
							           "2014_04",
							           2
							         ]
							       ],
							     "A2": [
							         [
							           "2014_01",
							           1
							         ],
							         [
							           "2014_04",
							           1
							         ]
							       ]
							     }
						
					var nonCountCumulantsCol = {
							  "activity_instances": [
				                         [
				                           "2014_01",
				                           6,
				                           6,
				                           6,
				                           0,
				                           1
				                         ],
				                         [
				                           "2014_04",
				                           15,
				                           7,
				                           10,
				                           3.559,
				                           3
				                         ]
				                       ]
							    }
					
					var nonCountGroupbyCumulantsCol = {
							  "A1": [
							         [
							           "2014_01",
							           0,
							           0,
							           0,
							           0,
							           0
							         ],
							         [
							           "2014_04",
							           15,
							           7,
							           11,
							           4,
							           2
							         ]
							       ],
							       "A2": [
							         [
							           "2014_01",
							           6,
							           6,
							           6,
							           0,
							           1
							         ],
							         [
							           "2014_04",
							           8,
							           8,
							           8,
							           0,
							           1
							         ]
							       ]
							     }
					
						if(this.report.layout.table.preview){
							//data = countCumulantsCol;
							data = countgroupbyCumulantsCol;
						
		                    if(this.report.dataSet.fact == this.reportingService.metadata.objects.processInstance.facts.count.id){
		                    	data = countCumulantsCol;
		                 	   if(this.report.dataSet.groupBy == 'activityName'){
		                 		  data = countgroupbyCumulantsCol;   
		                 	   }
		                    }else{
		                    	data = nonCountCumulantsCol;
		                 	   if(this.report.dataSet.groupBy == 'activityName'){
		                 		  data = nonCountGroupbyCumulantsCol;   
		                 	   }
		                    }
						}

                       self= this;
                       var configurations = self.getCumulantsTableConfig();
                       var disableSorting = configurations.disableSorting;
                       var multi_headers = configurations.multi_headers;
                       var cumulantsAsRow = false;
                       
                       if(self.report.layout.table.cumulantsDisplay == self.reportingService.metadata.cumulantsDisplay.rows.id){
                    	   var cumulantsAsRow = true;   
                       }
                       
                       var fact_count = (this.report.dataSet.fact == this.reportingService.metadata.objects.processInstance.facts.count.id);
                       var span = this.report.layout.table.selectedCumulants.length;
                       
                       //transform data
                       var inputArray = [];

                       //if fact != count
                       if (!fact_count) {
                         //position of cumulants in response json
                         var AVG_I = 1,
                           MIN_I = 2,
                           MAX_I = 3,
                           STD_DEV_I = 4,
                           COUNT_I = 5;

                         var cumulatingIntHeader = [];
                         inputArray.push(cumulatingIntHeader);

                         if(!cumulantsAsRow){
                      	   cumulatingIntHeader.push('', 1);   
                         }
                         
                         var cumulatingIntHeaderComplete = false;

                         var groupByArray = [];
                         inputArray.push(groupByArray);
                         groupByArray.push("");

                         for (var prop in data) {
                           var groupbyIndex = 0;

                           for (var j = 0; j < data[prop].length; j++) {

                             var inputArrayIndex = 2 + groupbyIndex++ * span;

                             //prepare header1: cumulating interval header
                             if (!cumulatingIntHeaderComplete) {
                               cumulatingIntHeader.push(data[prop][j][0]);
                               cumulatingIntHeader.push(span);

                               //if fact != count
                               if(this.report.layout.table.selectedCumulants.indexOf('average') != -1){
                            	   inputArray.push(['average']); //I18n   
                               }
                               if(this.report.layout.table.selectedCumulants.indexOf('minimum') != -1){
                            	   inputArray.push(['minimum']); //I18n   
                               }
                               if(this.report.layout.table.selectedCumulants.indexOf('maximum') != -1){
                            	   inputArray.push(['maximum']); //I18n   
                               }	   
                               if(this.report.layout.table.selectedCumulants.indexOf('stdDeviation') != -1){
                            	   inputArray.push(['stdDeviation']); //I18n   
                               }
                               if(this.report.layout.table.selectedCumulants.indexOf('count') != -1){
                            	   inputArray.push(['count']); //I18n   
                               }
                             }

                             //if fact != count  
                             //populate cumulant data
                             if(this.report.layout.table.selectedCumulants.indexOf('average') != -1){
                            	 inputArray[inputArrayIndex++].push(data[prop][j][AVG_I]);	 
                             }
                             if(this.report.layout.table.selectedCumulants.indexOf('minimum') != -1){
                            	 inputArray[inputArrayIndex++].push(data[prop][j][MIN_I]);	 
                             }
                             if(this.report.layout.table.selectedCumulants.indexOf('maximum') != -1){
                            	 inputArray[inputArrayIndex++].push(data[prop][j][MAX_I]);	 
                             }
                             if(this.report.layout.table.selectedCumulants.indexOf('stdDeviation') != -1){
                            	 inputArray[inputArrayIndex++].push(data[prop][j][STD_DEV_I]);	 
                             }
                             if(this.report.layout.table.selectedCumulants.indexOf('count') != -1){
                            	 inputArray[inputArrayIndex++].push(data[prop][j][COUNT_I]);	 
                             }
                           }

                           cumulatingIntHeaderComplete = true;

                           //if groupby is selected
                           //prepare groupby header
                           groupByArray.push(prop);
                         }

                         //if display total is selected
                         if(this.report.layout.table.displayTotals){
	                         var total_cols = groupByArray.length - 1;
	                         if (groupByArray.length > 2) {
	                           inputArray[1].push("Total");
	                           total_cols = groupByArray.length - 2;
	                         }
	
	                         inputArray.push(["Total"]);
	
	                         for (var j = 0; j < total_cols; j++) {
	                           inputArray[inputArray.length - 1].push(0); //set default value
	                         }
	                         
							for (var i = 2; i < inputArray.length-1; i++) {
								var sum = 0;
								
								for (var j = 1; j < inputArray[i].length; j++) {
									sum += inputArray[i][j];
									// if display total is selected
									inputArray[inputArray.length - 1][j] += inputArray[i][j];
								}
	
								if (groupByArray.length > 2) {
									inputArray[i].push(sum);
								}
							}
							
							var sum = 0;
							
							for (var i = 2; i < inputArray.length-1; i++) {
								sum += inputArray[i][inputArray[i].length-1];
							}
							inputArray[inputArray.length-1].push(sum);
	                         
	                         
                       }  

                       } else { // fact is count
                         var groupByArray = [];
                         inputArray.push(groupByArray);
                         groupByArray.push("");

                         var cumulatingIntHeaderComplete = false;

                         var groupbyIndex = 0;

                         for (var prop in data) {

                           for (var j = 0; j < data[prop].length; j++) {

                             if (!cumulatingIntHeaderComplete) {
                               inputArray.push([data[prop][j][0]]);
                             }

                             inputArray[j + 1].push(data[prop][j][1]);
                           }

                           cumulatingIntHeaderComplete = true;

                           groupByArray.push(prop);
                           groupbyIndex++;
                         }

                         //if display total is selected
                         if(this.report.layout.table.displayTotals){
	                         var total_cols = groupByArray.length - 1;
	                         if (groupbyIndex > 1) {
	                           inputArray[0].push("Total");
	                           total_cols = groupByArray.length - 2;
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
	
								if (groupbyIndex > 1) {
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
	                   
	                   
                       
   					// TODO: remove post-development - server data must converted to following format
					   // input data
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
                         if (!cumulantsAsRow) {
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

                         var headers2 = "";

                         if (cumulantsAsRow) {
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
                             headers2 += "<th colspan=" + topheaders[i + 1] + ">" + topheaders[i] + "</th>";
                           }
                         }

                         if (!cumulantsAsRow) {
                           TEMPLATE_COPY = TEMPLATE_COPY.replace("_TOPHEADERS_", headers2);
                         }
                       }

                       TEMPLATE_COPY = TEMPLATE_COPY.replace("_OPTIONS_", options);

                       //transform the array
                       if (!cumulantsAsRow) {
                    	   
                    	   tableArray = transposeArray(tableArray);
                       }

                       var columns = tableArray[0];

                       var headers = "";
                       var cols = "";

                       if (multi_headers && cumulantsAsRow) {
                         //for (i = 0; i < topheaders.length - 1; i = i + 2) {
                           //cols += "<td style=\"font-weight:bold; font-size:small\" rowspan=" + topheaders[i + 1] + ">" + topheaders[i] + "</td>";
                         //}
                       }

                       for (x in columns) {
                         var column = columns[x];
                         headers += "<th>" + column + "</th>";
                         if (x == 0) {
                           cols += "<td style=\"font-weight:bold; font-size:small\">{{row[" + x + "]}}</td>";
                         }else if(x == 1 && multi_headers && cumulantsAsRow){
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
	                   
	   	               self.rows = tableArray.splice(1);
	   	               self.updateView();
	   	               
                      };
			
			ReportDefinitionController.prototype.refreshPreview2 = function() {
				console.log("refreshPreview");
				
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
                   headers += "<th>" + column.name + "</th>";
	                   var col = column.id;
	                   console.log(col);
	                   // Logic to handle special characters like '{' in column id
	                   // column.id is typically like {Model71}ChangeOfAddress:{Model71}ConfirmationNumber
	                   // So Getting the last word i.e. ConfirmationNumber
	                   if(col.indexOf("{") != -1) { 
	                      var lastIndex = col.lastIndexOf("}");
                         col = col.substr( lastIndex + 1, col.length );
	                   }
	                   cols += "<td>{{row." + col + "}}</td>";
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
                self.refreshPreviewData();
	               }, 200);
           } else {
   				var self = this;
   				var deferred = jQuery.Deferred();
   
   				this.renderingController.renderReport(self.report).done(function() {
   					deferred.resolve();
   				}).fail(function() {
   					deferred.reject();
   				});
   				
   				return deferred.promise();
   			}
			};
			
			/**
         * 
         */
        ReportDefinitionController.prototype.refreshPreviewData = function() {
           var self = this;
           
           this.renderingController.getPreviewData().done(
               function(data) {
                //Format data before displaying the Results
                  
                  self.rows = self.formatPreviewData(data.rows);
                  
                  self.updateView();
               }).fail(function(err){
                  console.log("Failed getting Preview Date: " + err);
               });
        }	

				/**
             * 
             */
				ReportDefinitionController.prototype.changeDataSetType = function() {
					if (this.report.dataSet.type === "processHistory") {
						this.report.layout.type = "processDiagram";
					} else if (this.report.dataSet.type === 'seriesGroup') {
						this.report.layout.type = "simpleReport";
						this.report.layout.subType = "chart";
					} else if (this.report.dataSet.type === 'recordSet') {
						this.report.layout.type = "table";
					}
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.changePrimaryObject = function(
						initialize) {
					this.primaryObjectSelect
							.val(this.report.dataSet.primaryObject);

					if (!initialize) {
						this.report.dataSet.filters = [];
						this.filterSelected = [];
						this.populateCumulatedDimensions();
					}

					this.factSelect.empty();

					var stdQuantities = "<optgroup label=\"" + this.getI18N("reporting.definitionView.stdQuantities") + "\">";
					
					var group = "<optgroup label=\"" + this.getI18N("reporting.definitionView.numericDescriptors") + "\">";
					var areDiscriptorsAvailable = false;
					
					for ( var n in this.getPrimaryObject().facts) {
						var fact = this.getPrimaryObject().facts[n];

						if (this.isNumeric(fact) || this.isCount(fact) || this.isDuration(fact))
						{
						   if(fact.metadata && fact.metadata.isDescriptor) 
						   {
						      group += "<option value='" + n + "'>" + fact.name + "</option>";
						      areDiscriptorsAvailable = true;
						   } else {
							   stdQuantities += "<option value='" + n + "'>"  + fact.name + "</option>";
						   }
						}
					}
					
					stdQuantities += "</optgroup>";
					this.factSelect.append(stdQuantities);
					
					group += "</optgroup>";
					
					if (areDiscriptorsAvailable)
					{
					   this.factSelect.append(group);
					}

					this.populatelayoutSubTypes();
					//this.populateChartTypes();
					this.populateGroupBy();
					
					this.populateCumulantsDisplay();
					
					this.report.layout.chart.options.title = this
							.getPrimaryObject().name;
					
					this.updateView();
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.changeFact = function() {
					this.populateChartTypes();

					// TODO Check for data

					if (true) {
						this.populateFactProcessDataSelect();
					}

					this.report.layout.chart.options.axes.yaxis.label = this
							.getFact().name;
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.isFactDuration = function() {
					return this.getFact().type == this.reportingService.metadata.durationType;
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.changeFirstDimension = function() {
					console.log("First Dimension");
					console.log(this.getFirstDimension());

					if (this.getFirstDimension().type == this.reportingService.metadata.enumerationType) {
						this.firstDimensionEnumerators = this
								.getEnumerators(this.getFirstDimension());
					}

					this.populateChartTypes();

					this.report.layout.chart.options.axes.xaxis.label = this
							.getFirstDimension().name;

					this.populateGroupBy();
					
					//Remove the First Dimension parameters from the list
					for ( var parameter in this.report.parameters)
               {
                  if (this.report.parameters[parameter].id.indexOf("filters.") === -1)
                  {
                     delete this.report.parameters[parameter];
                  }
               }
					
					this.report.dataSet.firstDimensionParameters = [];

					this.updateView();
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.getTestEnumerators = function() {
					return [ "Nase", "Wurst", "Propase" ];
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.getEnumerators = function(
						dimension) {
					if (!dimension || !dimension.enumerationType) {
						return null;
					}

					var qualifier = dimension.enumerationType.split(":");

					return this.reportingService.getEnumerators(qualifier[0],
							qualifier[1], qualifier[2]);
				};
				
				ReportDefinitionController.prototype.getOperatorsEnum = function(dimension){
					var operators = [];
					
					if(!dimension){
						return operators; 
					}
					
					//operators can be data type specific or filter specific
					var dimensionOperator = dimension.operators;
					if(!dimensionOperator && dimension.type){
						dimensionOperator = dimension.type.operators;
					}
					
					if(dimensionOperator){
						for (var i in dimensionOperator){
							operators.push({"id" : dimensionOperator[i], 
									"label" : this.getI18N("reporting.definitionView.additionalFiltering.operator." + dimensionOperator[i])});
						}
					}else{
						// return default list
						operators.push({"id" : "E", "label" : this.getI18N("reporting.definitionView.additionalFiltering.operator.E")});
						operators.push({"id" : "LE", "label" : this.getI18N("reporting.definitionView.additionalFiltering.operator.LE")});
						operators.push({"id" : "GE", "label" : this.getI18N("reporting.definitionView.additionalFiltering.operator.GE")});
						operators.push({"id" : "NE", "label" : this.getI18N("reporting.definitionView.additionalFiltering.operator.NE")});
					}
					return operators;
				}
						
				
				ReportDefinitionController.prototype.getEnumerators2 = function(
						dimension, filter) {
					if (!dimension || !dimension.enumerationType) {
						return null;
					}

					var qualifier = dimension.enumerationType.split(":");

					var enumItems = this.reportingService.getEnumerators2(qualifier[0], qualifier[1]);
					
					var filteredEnumItems = enumItems;
					
					if(filter && (filter.dimension == "processName" || filter.dimension == "activityName")){
						//processes
						if ((dimension.id == "processName" || dimension.id == "activityName")) {
							filteredEnumItems = [];
							filteredEnumItems.push(this.constants.ALL_PROCESSES);
							for (var i = 0; i < enumItems.length; i++) {
								var process = enumItems[i];
								if (!filter.metadata.process_filter_auxiliary || !process.auxiliary) {
									filteredEnumItems.push(process);
								}
							}
						} 
						
						//activities
						if (dimension.id == "activityName") {
							var selectedProcesses = [];
							self = this;
							
							if (!filter.metadata.selectedProcesses
									|| filter.metadata.selectedProcesses.length < 1) {
								selectedProcesses = selectedProcesses.concat(filteredEnumItems);
							}else if(filter.metadata.selectedProcesses.some(function (id) {return self.constants.ALL_PROCESSES.id == id;})){
								selectedProcesses = selectedProcesses.concat(filteredEnumItems);
							}else{
								filteredEnumItems.forEach(function(item){
									if(filter.metadata.selectedProcesses.some(function (id) {return  item.id == id;})){
										selectedProcesses.push(item);
									}
								});
							}
							
							filteredEnumItems = [];
							filteredEnumItems.push(this.constants.ALL_ACTIVITIES);
							
							for (var i = 0; i < selectedProcesses.length; i++) {
								var process = selectedProcesses[i];
								if(process == this.constants.ALL_PROCESSES){
									continue;
								}
								for (var j = 0; j < process.activities.length; j++){
									var activity = process.activities[j];
									if (!filter.metadata.activity_filter_auxiliary || !activity.auxiliary) {
										if(!filter.metadata.activity_filter_interactive || !activity.interactive){
											if(!filter.metadata.activity_filter_nonInteractive || activity.interactive){
												filteredEnumItems.push(activity);	
											}
										}
									}	
								}
							}
							
						} 
					}
					
					return filteredEnumItems;
				};

				
				ReportDefinitionController.prototype.selectionChanged = function(dimension, filter) {
					var self = this;
					if(dimension.id == "processName" && filter.value.some(function (id) {return self.constants.ALL_PROCESSES.id == id;})){
						filter.value = [this.constants.ALL_PROCESSES.id];
					}
					
					if(dimension.id == "activityName" && filter.value.some(function (id) {return self.constants.ALL_ACTIVITIES.id == id;})){
						filter.value = [this.constants.ALL_ACTIVITIES.id];
					}
					
					if(dimension.id == "criticality"){
						filter.metadata = this.getCriticalityForName(filter.value); 
					}
				};
				
				/**
				 * 
				 */
				ReportDefinitionController.prototype.addFilter = function() {
					var index = this.report.dataSet.filters.length;

					this.report.dataSet.filters.push({
						index : index,
						value : null,

						// TODO: Operator only for respective types

						operator : "equal"
					});
					
					this.filterSelected.push({
					         index : index,
					         value : []
					      });
				};

				ReportDefinitionController.prototype.getCriticalityForName = function(name) {
					var criticality;
					this.reportingService.preferenceData.criticality.forEach(function(item){
						if(item.name == name){
							criticality = item;
						}
					});
					
					return criticality;
				};
				
				/**
				 * 
				 */
				ReportDefinitionController.prototype.deleteFilter = function(
						index) {
					var newFilters = [];

					for ( var n = 0; n < this.report.dataSet.filters.length; ++n) {
						if (n == index) {
							continue;
						}

						newFilters.push(this.report.dataSet.filters[n]);
					}

					this.report.dataSet.filters = newFilters;
					
					//Remove parameters from parameter
					this.removeParametersFromParameterList(index);
					
				};

				/**
				 * Filters are stored in an array. Retrieves a filter by ID from
				 * that array.
				 */
				ReportDefinitionController.prototype.getFilterByDimension = function(
						dimension) {
					for ( var n = 0; n < this.report.dataSet.filters.length; ++n) {
						if (this.report.dataSet.filters[n].dimension == dimension) {
							return this.report.dataSet.filters[n];
						}
						//TODO: this is always thrown commenting temporarily
						/*throw "No filter found with dimension " + dimension
								+ ".";*/
					}
				};

				/**
				 * Reinitializes filter values and operator.
				 */
				ReportDefinitionController.prototype.onFilterDimensionChange = function(
						index) {
					this.report.dataSet.filters[index].value = null;
					this.report.dataSet.filters[index].metadata = null;
					this.report.dataSet.filters[index].operator = null;
					
					if(this.report.dataSet.filters[index].dimension == 'processName'){
						this.report.dataSet.filters[index].metadata = { process_filter_auxiliary : true };
						this.report.dataSet.filters[index].value = [this.constants.ALL_PROCESSES.id];
					}else if(this.report.dataSet.filters[index].dimension == 'activityName'){
						this.report.dataSet.filters[index].metadata = activityFilterTemplate();
						this.report.dataSet.filters[index].metadata.selectedProcesses.push(this.constants.ALL_PROCESSES.id);
						this.report.dataSet.filters[index].value = [this.constants.ALL_ACTIVITIES.id];
					}else{
						var dimenison = this
								.getDimension(this.report.dataSet.filters[index].dimension);
						
						if (dimenison && dimenison.metadata
								&& dimenison.metadata.isDescriptor) {
							this.report.dataSet.filters[index].metadata = dimenison.metadata;
						}
						
						if(dimenison && (dimenison.type == this.reportingService.metadata.autocompleteType)){
							this.report.dataSet.filters[index].value = [];
						}
						
						if(dimenison && (dimenison.type == this.reportingService.metadata.timestampType)){
							this.report.dataSet.filters[index].value = {
									from :"",
									to : "",
									duration: "",
									durationUnit: ""
							};
							if(!this.report.dataSet.filters[index].metadata){
								this.report.dataSet.filters[index].metadata = {};
							}
							this.report.dataSet.filters[index].metadata.fromTo = true;
 						}
					}

					// TODO: Operator only for respective types
					this.report.dataSet.filters[index].operator = "equal";
					
					this.removeParametersFromParameterList(index);
				};

				/**
				 * Returns a consolidated list of possible dimensions excluding
				 * joined data from external data sources.
				 */
				ReportDefinitionController.prototype.getInternalDimensions = function() {
					var dimensions = [];

					for ( var m in this.getPrimaryObject().dimensions) {
						var dimension = this.getPrimaryObject().dimensions[m];

						dimensions.push({
							id : dimension.id,
							name : dimension.name
						});
					}

					return dimensions;
				};

				/**
				 * Returns a consolidated list of possible dimensions including
				 * joined data from external data sources.
				 */
				ReportDefinitionController.prototype.getCumulatedDimensions = function() {
					return this.reportingService.getCumulatedDimensions(this.report);
				};

				/**
				 * Get dimension objects for report columns.
				 */
				ReportDefinitionController.prototype.getColumnDimensions = function() {
					return this.reportingService
							.getColumnDimensions(this.report);
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.populateFactProcessDataSelect = function() {
					var factProcessDataSelect = jQuery("#factProcessDataSelect");

					factProcessDataSelect.empty();

					for ( var i in this.reportingService.processData) {
						var processData = this.reportingService.processData[i];

						if (i == 0) {
							this.report.dataSet.factProcessData = i;
						}

						factProcessDataSelect.append("<option value='" + i
								+ "'>" + processData.name + "</option>");
					}
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.populateGroupBy = function() {
					var groupBySelect = jQuery("#groupBySelect");

					groupBySelect.empty();

					groupBySelect
							.append("<option style='font-style: italic;'>None</option>"); // TODO
					// I18N

					for ( var i in this.getPrimaryObject().dimensions) {
						var dimension = this.getPrimaryObject().dimensions[i];

						// Only discrete types can be used as group criteria
						if (this.reportingService
								.isDiscreteType(dimension.type)) {
							groupBySelect.append("<option value='" + i + "'>"
									+ dimension.name + "</option>");
						}
					}
				};

				ReportDefinitionController.prototype.populatelayoutSubTypes = function() {
					this.layoutSubTypeSelect.empty();

					var option = "<option value='ID'>LABEL</option>";
			        
					var layoutSubTypes = this.reportingService.metadata.layoutSubTypes;
					
			        for(var i in layoutSubTypes){
			            this.layoutSubTypeSelect.append(option.replace("ID", layoutSubTypes[i].id).replace("LABEL", layoutSubTypes[i].name));
			        }
				};

				ReportDefinitionController.prototype.populateCumulantsDisplay = function() {
					this.cumulantsDisplaySelect.empty();

					var option = "<option value='ID'>LABEL</option>";
			        
					var cumulantsDisplay = this.reportingService.metadata.cumulantsDisplay;
					
			        for(var i in cumulantsDisplay){
			            this.cumulantsDisplaySelect.append(option.replace("ID", cumulantsDisplay[i].id).replace("LABEL", cumulantsDisplay[i].name));
			        }
				};

				
				/**
				 * Selects the useful tables and charts for the selected facts
				 * and dimensions.
				 */
				ReportDefinitionController.prototype.populateChartTypes = function() {
					this.chartTypeSelect.empty();

					var fact_count = (this.report.dataSet.fact == this.reportingService.metadata.objects.processInstance.facts.count.id);
					
					this.chartTypeSelect
							.append("<option value='xyPlot'>"
									+ this.reportingService.metadata.chartTypes.xyPlot.name
									+ "</option>");
					this.chartTypeSelect
							.append("<option value='barChart'>"
									+ this.reportingService.metadata.chartTypes.barChart.name
									+ "</option>");
					this.chartTypeSelect
							.append("<option value='bubbleChart'>"
									+ this.reportingService.metadata.chartTypes.bubbleChart.name
									+ "</option>");

					this.chartTypeSelect
					.append("<option value='donutChart'>"
							+ this.reportingService.metadata.chartTypes.donutChart.name
							+ "</option>");
					
					if (!fact_count) {
						this.chartTypeSelect
								.append("<option value='candlestickChart'>"
										+ this.reportingService.metadata.chartTypes.candlestickChart.name
										+ "</option>");
					}

					if (this.getFirstDimension()
							&& this.getFirstDimension().type == this.reportingService.metadata.enumerationType) {
						this.chartTypeSelect
								.append("<option value='pieChart'>"
										+ this.reportingService.metadata.chartTypes.pieChart.name
										+ "</option>");
					}
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.getChartType = function() {
					return this.reportingService.metadata.chartTypes[this.report.layout.chart.type];
				};
				
				/**
				 * 
				 */
				ReportDefinitionController.prototype.selectAllDimensionsForColumns = function() {
					for ( var k in this.cumulatedDimensions) {
						this.report.dataSet.columns.push(this.cumulatedDimensions[k].id);
						this.selectedColumns.push(this.cumulatedDimensions[k]);
					}
					this.cumulatedDimensions = [];
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.deselectAllDimensionsForColumns = function() {
				   this.report.dataSet.columns = [];
               for ( var k in this.selectedColumns) {
                  this.cumulatedDimensions.push(this.selectedColumns[k]);
               }
               this.selectedColumns = [];
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.printChart = function() {
					jQuery("#chartView").printElement();
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.saveReport = function() {
					console.log("*** Save Report ***\n");
					console.log("Report Content:");
					console.log(this.getJsonString());

					var self = this;

					this.reportingService.saveReportDefinition(this.report)
							.done(
									function(report) {
										self.report = report;
										window.parent.EventHub.events.publish("BPM-REPORTING-REPORT-CREATED");
										self.updateView();
									});
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.getJsonString = function() {
					return JSON.stringify(this.report, null, 3);
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.addParameter = function(
						id, name, type, value, operator) {
					
					var currentFilter = this.report.dataSet.filters;
					
					for ( var int = 0; int < this.report.dataSet.filters.length; int++)
               {
                  if (id.indexOf(this.report.dataSet.filters[int].dimension) != -1)
                  {
                     this.filterSelected[int].value.push({
                              id : id,
                              name : name,
                              type : type,
                              value : value,
                              operator : operator
                     });
                  }
               }
					
					if (id != null && id.length != 0)
               {
					   this.report.parameters[id] = {
			                  id : id,
			                  name : name,
			                  type : type,
			                  value : value,
			                  operator : operator
			               };
               }
				};

				/**
				 * Expects an array of parameters.
				 */
				ReportDefinitionController.prototype.addParameters = function(
						parameters) {
					for ( var n = 0; n < parameters.length; ++n) {
						this.report.parameters[parameters[n].id] = parameters[n];
					}
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.removeParameter = function(
						id) {
					delete this.report.parameters[id];
				};

				/**
				 * Expects an array of parameter IDs.
				 */
				ReportDefinitionController.prototype.removeParameters = function(
						ids) {
					for ( var n = 0; n < ids.length; ++n) {
						this.removeParameter(ids[n]);
					}
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.existsParameter = function(
						id) {
					return this.report.parameters[id] != null;
				};
				
				/**
             * 
             */
            ReportDefinitionController.prototype.existsDimension = function(
                  dimensionName) {
               for ( var parameter in this.report.parameters)
               {
                  if (this.report.parameters[parameter].name == dimensionName)
                  {
                     return true;
                  }
               }
               return false;
            };

				/**
				 * 
				 */
				ReportDefinitionController.prototype.getParameters = function() {
					return this.report.parameters;
				};
				
				/**
             * 
             */
            ReportDefinitionController.prototype.getParametersOfTypeTimestamp = function() {
               var parametersOfTypeTimestamp = {};
               for ( var parameter in this.report.parameters)
               {
                  if (this.report.parameters[parameter].type == this.reportingService.metadata.timestampType.id)
                  {
                     parametersOfTypeTimestamp[this.report.parameters[parameter].id] = this.report.parameters[parameter];
                  }
               }
               return parametersOfTypeTimestamp;
            };

				/**
				 * 
				 */
				ReportDefinitionController.prototype.hasParameters = function() {
					var key;

					for (key in this.report.parameters) {
						if (this.report.parameters.hasOwnProperty(key)) {
							return true;
						}
					}

					return false;
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.getParameterDefaultValue = function(
						id) {
					if (id.indexOf("firstDimension") >= 0) {
						return this.report.dataSet[id];
					} else if (id.indexOf("firstDimension") >= 0) {
						return this.report.dataSet[id];
					} else if (id.indexOf("filters.") >= 0) {
						var path = id.split(".");

						if (this.getFilterByDimension(path[1])) {
							return this.getFilterByDimension(path[1]).value;
						}
						return null;
					}
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.getProcesses = function() {
					// TODO Make objects

					return this.reportingService.processNames;
				};

				ReportDefinitionController.prototype.initializeDragAndDrop = function() {
					var self = this;
					var IE = document.all ? true : false;

					if (!IE)
						document.captureEvents(Event.MOUSEMOVE);
					document.onmousemove = function(e) {

						// De-select any selected elements in the canvas
						// to avoid they getting dragged inadvertently

						if (parent.iDnD.dragMode) {
						}

						if (e) {
							parent.iDnD.setIframeXY(e, window.name);
						} else {
							parent.iDnD.setIframeXY(window.event, window.name);
						}
					};

					document.onmouseup = function(event) {
						self.dropElement(event);
					};
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.dropElement = function(e) {
					var eve = e;

					if (!eve) {
						eve = window.event;
					}

					parent.iDnD.dragMode = false;

					if (parent.iDnD.getTransferObject()) {
						console.log("Object dropped at");
						console.log(parent.iDnD.getTransferObject());

						if (this.report.layout.type == "document") {
							CKEDITOR.instances["documentTemplateEditor"]
									.setData(CKEDITOR.instances["documentTemplateEditor"]
											.getData()
											+ "<iframe allowtransparency='true' frameborder='0' "
											+ "sandbox='allow-same-origin allow-forms allow-scripts' "
											+ "scrolling='auto' style='border: none; width: 100%; height: 100%;' "
											+ "src='"
											+ this.reportingService
													.getRootUrl()
											+ "/plugins/bpm-reporting/views/reportPanel.html?path="
											+ parent.iDnD.getTransferObject().path
											+ "'></iframe>");
						}
					}

					parent.iDnD.hideIframe();
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.getReportUri = function(e) {
					return this.reportingService.getRootUrl()
							+ "/plugins/bpm-reporting/views/reportPanel.html?path="
							+ this.path;
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.getNextExecutionTime = function(
						e) {
					var date = new Date(); // Now
					

					console.log("Start Date");
					console
							.log(this.report.scheduling.recurrenceRange.startDate);

					if (this.report.scheduling.recurrenceRange.startDate) {
						date = new Date(
								this.report.scheduling.recurrenceRange.startDate);
					}

					if (this.report.scheduling.recurrenceInterval === "weekly") {
						var weekdays = [
								this.report.scheduling.weeklyRecurrenceOptions.mondays,
								this.report.scheduling.weeklyRecurrenceOptions.tuesdays,
								this.report.scheduling.weeklyRecurrenceOptions.wednesdays,
								this.report.scheduling.weeklyRecurrenceOptions.thursdays,
								this.report.scheduling.weeklyRecurrenceOptions.fridays,
								this.report.scheduling.weeklyRecurrenceOptions.saturdays,
								this.report.scheduling.weeklyRecurrenceOptions.sundays ];
						var weekday = date.getDay();

						for ( var n = 0; n < weekdays.length; ++n) {
							if (weekdays[weekday]) {
								date.setDate(date.getDate() + n);

								break;
							}

							weekday = (weekday + 1) % 7;
						}
					}

					return date;
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.getWeekdayName = function(
						date) {
					if (date.getDay() == 0) {
						return "Monday";
					} else if (date.getDay() == 1) {
						return "Tuesday";
					} else if (date.getDay() == 2) {
						return "Wednesday";
					} else if (date.getDay() == 3) {
						return "Thursday";
					} else if (date.getDay() == 4) {
						return "Friday";
					} else if (date.getDay() == 5) {
						return "Saturday";
					} else if (date.getDay() == 6) {
						return "Sunday";
					} else {
						return "(Unknown)";
					}
				};
				
				
				/**
				 * 
				 */
				ReportDefinitionController.prototype.addExternalJoinField = function() {
					this.report.dataSet.externalJoins[0].fields.push({
						id : null,
						name : null,
						useAs : null,
						type : this.reportingService.metadata.stringType.id
					});
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.deleteExternalJoinField = function(
						field) {
					console.log("Delete");
					console.log(field);

					for ( var n = 0; n < this.report.dataSet.externalJoins[0].fields.length; ++n) {
						if (this.report.dataSet.externalJoins[0].fields[n].$$hashKey === field.$$hashKey) {
							this.report.dataSet.externalJoins[0].fields.splice(
									n, 1);

							return;
						}
					}
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.evaluateExternalTestdata = function(
						externalJoin) {
					var self = this;

					document.body.style.cursor = "wait";

					this.reportingService
							.retrieveExternalData(externalJoin.restUri)
							.done(
									function(records) {

										if (records && records[0]) {
											externalJoin.fields = [];

											var start = true;

											for ( var x in records[0]) {
												externalJoin.fields
														.push({
															id : x,
															name : x,
															useAs : x,
															type : self.reportingService.metadata.stringType.id,
														});

												if (start) {
													externalJoin.externalKey = x;
													start = false;
												}
											}
										}

										document.body.style.cursor = "default";
									}).fail(function() {
								document.body.style.cursor = "default";
							});
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.addComputedColumn = function() {
					this.selectedComputedColumn = {
						id : null,
						name : null,
						type : this.reportingService.metadata.stringType.id,
						formula : null
					};

					this.report.dataSet.computedColumns
							.push(this.selectedComputedColumn);
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.deleteComputedColumn = function(
						column) {
					for ( var n = 0; n < this.report.dataSet.computedColumns.length; ++n) {
						if (this.report.dataSet.computedColumns[n].$$hashKey === column.$$hashKey) {
							this.report.dataSet.computedColumns.splice(n, 1);

							if (this.report.dataSet.computedColumns.length) {
								this.selectedComputedColumn = this.report.dataSet.computedColumns[0];
							} else {
								this.selectedComputedColumn = null;
							}

							return;
						}
					}
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.selectComputedColumn = function(
						column) {
					this.selectedComputedColumn = column;
					
					this.expressionEditor.enable();
					this.expressionEditor.setValue(this.selectedComputedColumn.formula);
					
				};

				/**
				 * Auxiliary method because Angular does not support ternary
				 * operators.
				 */
				ReportDefinitionController.prototype.getComputedColumnStyleClass = function(
						column) {
					if (this.selectedComputedColumn
							&& this.selectedComputedColumn.$$hashKey === column.$$hashKey) {
						return "selected";
					}

					return "";
				};
				
				ReportDefinitionController.prototype.getExecutionTime = function() {
               return {
                  '01' : I18NUtils.getProperty('reporting.definitionView.executionTime.1200AM.label'),
                  '02' : I18NUtils.getProperty('reporting.definitionView.executionTime.1230AM.label'),
                  '03' : I18NUtils.getProperty('reporting.definitionView.executionTime.0100AM.label'),
                  '04' : I18NUtils.getProperty('reporting.definitionView.executionTime.0130AM.label'),
                  '05' : I18NUtils.getProperty('reporting.definitionView.executionTime.0200AM.label'),
                  '06' : I18NUtils.getProperty('reporting.definitionView.executionTime.0230AM.label'),
                  '07' : I18NUtils.getProperty('reporting.definitionView.executionTime.0300AM.label'),
                  '08' : I18NUtils.getProperty('reporting.definitionView.executionTime.0330AM.label'),
                  '09' : I18NUtils.getProperty('reporting.definitionView.executionTime.0400AM.label'),
                  '10': I18NUtils.getProperty('reporting.definitionView.executionTime.0430AM.label'),
                  '11': I18NUtils.getProperty('reporting.definitionView.executionTime.0500AM.label'),
                  '12': I18NUtils.getProperty('reporting.definitionView.executionTime.0530AM.label'),
                  '13': I18NUtils.getProperty('reporting.definitionView.executionTime.0600AM.label'),
                  '14': I18NUtils.getProperty('reporting.definitionView.executionTime.0630AM.label'),
                  '15': I18NUtils.getProperty('reporting.definitionView.executionTime.0700AM.label'),
                  '16': I18NUtils.getProperty('reporting.definitionView.executionTime.0730AM.label'),
                  '17': I18NUtils.getProperty('reporting.definitionView.executionTime.0800AM.label'),
                  '18': I18NUtils.getProperty('reporting.definitionView.executionTime.0830AM.label'),
                  '19': I18NUtils.getProperty('reporting.definitionView.executionTime.0900AM.label'),
                  '20': I18NUtils.getProperty('reporting.definitionView.executionTime.0930AM.label'),
                  '21': I18NUtils.getProperty('reporting.definitionView.executionTime.1000AM.label'),
                  '22': I18NUtils.getProperty('reporting.definitionView.executionTime.1030AM.label'),
                  '23': I18NUtils.getProperty('reporting.definitionView.executionTime.1100AM.label'),
                  '24': I18NUtils.getProperty('reporting.definitionView.executionTime.1130AM.label'),
                  '25': I18NUtils.getProperty('reporting.definitionView.executionTime.1200PM.label'),
                  '26': I18NUtils.getProperty('reporting.definitionView.executionTime.1230PM.label'),
                  '27': I18NUtils.getProperty('reporting.definitionView.executionTime.0100PM.label'),
                  '28': I18NUtils.getProperty('reporting.definitionView.executionTime.0130PM.label'),
                  '29': I18NUtils.getProperty('reporting.definitionView.executionTime.0200PM.label'),
                  '30': I18NUtils.getProperty('reporting.definitionView.executionTime.0230PM.label'),
                  '31': I18NUtils.getProperty('reporting.definitionView.executionTime.0300PM.label'),
                  '32': I18NUtils.getProperty('reporting.definitionView.executionTime.0330PM.label'),
                  '33': I18NUtils.getProperty('reporting.definitionView.executionTime.0400PM.label'),
                  '34': I18NUtils.getProperty('reporting.definitionView.executionTime.0430PM.label'),
                  '35': I18NUtils.getProperty('reporting.definitionView.executionTime.0500PM.label'),
                  '36': I18NUtils.getProperty('reporting.definitionView.executionTime.0530PM.label'),
                  '37': I18NUtils.getProperty('reporting.definitionView.executionTime.0600PM.label'),
                  '38': I18NUtils.getProperty('reporting.definitionView.executionTime.0630PM.label'),
                  '39': I18NUtils.getProperty('reporting.definitionView.executionTime.0700PM.label'),
                  '40': I18NUtils.getProperty('reporting.definitionView.executionTime.0730PM.label'),
                  '41': I18NUtils.getProperty('reporting.definitionView.executionTime.0800PM.label'),
                  '42': I18NUtils.getProperty('reporting.definitionView.executionTime.0830PM.label'),
                  '43': I18NUtils.getProperty('reporting.definitionView.executionTime.0900PM.label'),
                  '44': I18NUtils.getProperty('reporting.definitionView.executionTime.0930PM.label'),
                  '45': I18NUtils.getProperty('reporting.definitionView.executionTime.1000PM.label'),
                  '46': I18NUtils.getProperty('reporting.definitionView.executionTime.1030PM.label'),
                  '47': I18NUtils.getProperty('reporting.definitionView.executionTime.1100PM.label'),
                  '48': I18NUtils.getProperty('reporting.definitionView.executionTime.1130PM.label')
                     }; 
            };
            
            /**
             * 
             */
            ReportDefinitionController.prototype.getNextExecutionDate = function() {
                  var self = this;
                  if (this.report.scheduling.active)
                  {
                     this.reportingService.getNextExecutionDate(this.report.scheduling).done(
                              function(date) {
                                 self.scheduling.nextExecutionDate = date;
                                 self.scheduling.nextExecutionDateDay = utils.getWeekdayName(date);
                                 self.updateView();
                              }).fail(function(err){
                                 console.log("Failed next Execution Date: " + err);
                              });
                  }
            };
            
            /**
             * 
             */
            ReportDefinitionController.prototype.populateAutoCompleteKeywordList = function() {
                  var self = this;
                  var dimensions = self.getInternalDimensions();
                  var dimensionNames = [];
                  for ( var m in dimensions) {
                     var dimension = dimensions[m];
                     dimensionNames[m] = dimension.id;
                  }
                  self.expressionEditor.setSessionData("$keywordList", dimensionNames);
            };
            
            ReportDefinitionController.prototype.addItems = function (selectedItems, list) {
            	var self = this;
            	selectedItems.forEach(function(itemId){
            		if(list.indexOf(itemId) < 0){
            			list.push(itemId);	
            		}
            	});
            };
            
            ReportDefinitionController.prototype.removeItems = function (selectedItems, list) {
            	selectedItems.forEach(function(itemId){
            		delete list[list.indexOf(itemId)];
            	});
            };
            
            ReportDefinitionController.prototype.addAllItems = function (selectedItems, list) {
            	selectedItems.forEach(function(item){
            		if(list.indexOf(item.id) < 0){
            			list.push(item.id);	
            		}
            	});
            };
            
            ReportDefinitionController.prototype.removeAllItems = function (list) {
            	list.length = 0;
            };
            
            /**
             * 
             */
            ReportDefinitionController.prototype.moveItem = function(items, from, to) {
               if (items == undefined)
               {
                  return;
               }
               for ( var int = 0; int < items.length; int++)
               {
                  var item = items[int];
                  for ( var k in from) {
                     
                     if (item === from[k].id)
                     {
                        to.push(from[k]);
                        from.splice(k, 1);
                        break;
                     }
                  }
               }
               
               this.populateSelectedArrayinJson();

            };
            
            ReportDefinitionController.prototype.moveItemUp = function(item, list) {
                if (this.isMultiSelected(item))
                {
                   return;
                }
                
                var idx = list.indexOf(item[0]);
                if (idx != -1) {
                    list.splice(idx - 1, 0, list.splice(idx, 1)[0]);
                }
            };
            
            /**
             * 
             */
            ReportDefinitionController.prototype.moveItemDown = function(item, list) {
               if (this.isMultiSelected(item))
               {
                  return;
               }  
               
               var idx = list.indexOf(item[0]);
               if (idx != -1) {
                   list.splice(idx + 1, 0, list.splice(idx, 1)[0]);
               }
           };
            
            /**
             * 
             */
            ReportDefinitionController.prototype.moveUp = function(item, list) {
               if (this.isMultiSelected(item))
               {
                  return;
               }
               
               var listIds = [];
               
               for ( var l in list)
               {
                  listIds[l] = list[l].id;
               }
               
               var idx = listIds.indexOf(item[0]);
               if (idx != -1 && idx != 0) {
                   list.splice(idx - 1, 0, list.splice(idx, 1)[0]);
               }
               this.populateSelectedArrayinJson();
           };
           
           /**
            * 
            */
           ReportDefinitionController.prototype.moveDown = function(item, list) {
              if (this.isMultiSelected(item))
              {
                 return;
              }  
              
              var listIds = [];
              
              for ( var l in list)
              {
                 listIds[l] = list[l].id;
              }
              
              var idx = listIds.indexOf(item[0]);
              if (idx != -1 && idx != (list.lenght -1)) {
                  list.splice(idx + 1, 0, list.splice(idx, 1)[0]);
              }
              this.populateSelectedArrayinJson();
          };
          
          /**
             * This function updates the underlying variables when any changes happens to
             * Data Set -> Record Set -> multi select box options
             */
          ReportDefinitionController.prototype.populateSelectedArrayinJson = function() {
             this.report.dataSet.columns = [];
             for ( var item in this.selectedColumns)
             {
                this.report.dataSet.columns.push(this.selectedColumns[item].id);
             }
         };
         
         /**
          *  This function returns true if more than 1 options are selected in
          *  Data Set -> Record Set -> multi select box options 
          */
         ReportDefinitionController.prototype.isMultiSelected = function(item) {
            if (item.length > 1)
            {
               alert("Please select single item to move");// i18n
               return true;
            }
            return false;
        };
        
        /**
          * This function will populte the variables while loading previously saved Data
          * Set -> Record Set -> multi select box options
          */
        ReportDefinitionController.prototype.loadDataSetRecordSet = function() {
           this.cumulatedDimensions = this.getCumulatedDimensions();
           var selColumns = this.report.dataSet.columns;
            
           for (var i in selColumns) {
              for ( var k in this.cumulatedDimensions) {
                 if (selColumns[i] === this.cumulatedDimensions[k].id)
                 {
                    this.selectedColumns.push(this.cumulatedDimensions[k]);
                    this.cumulatedDimensions.splice(k, 1);
                    break;
                 }
              }
            }
        };
          
        /**
         * This function will remove parameters from parameter list
         */
       ReportDefinitionController.prototype.removeParametersFromParameterList = function(index) {
          if (this.filterSelected[index].value != null)
          {
             var params = this.filterSelected[index].value;
             for ( var param in params)
             {
                this.removeParameter(this.filterSelected[index].value[param].id);
			}
          }
       };
      
      
               /**
                * This function will return true if argument is of Integer Type
                */
              ReportDefinitionController.prototype.isNumeric = function(element) {
                 return (element.type.id === this.reportingService.metadata.integerType.id)? true : false;
              };
              
              /**
               * This function will return true if argument is of duration Type
               */
             ReportDefinitionController.prototype.isDuration = function(element) {
                return (element.type.id === this.reportingService.metadata.durationType.id)? true : false;
             };
             
             /**
              * This function will return true if argument is of Count Type
              */
            ReportDefinitionController.prototype.isCount = function(element) {
               return (element.type.id === this.reportingService.metadata.countType.id)? true : false;
            };
     
                 
              /**
               * 
               */
              ReportDefinitionController.prototype.formatPreviewData = function(data) {
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
                          record[selColumn] = enumItems[record[selColumn]].name;
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
                       var value = record[selColumn];
                       b[key] = value;
                    }
                    a.push(b);
                 }
                 return a;
              }
              
              
              ReportDefinitionController.prototype.populateCumulatedDimensions = function() {
                 this.cumulatedDimensions = [];
                 this.cumulatedDimensions = this.getCumulatedDimensions();
                 this.selectedColumns = [];
              };
              
              /**
               * Adding order parameter to dimension object for displaying it on UI in specific order
               */
              ReportDefinitionController.prototype.getCumulatedDimensionsByGroup = function() {
                 var dimensions = this.reportingService.getCumulatedDimensions(this.report);
                 for ( var dimension in dimensions)
                 {
                    var group = this.primaryObjectEnumGroup(dimensions[dimension].id);
                    dimensions[dimension].order = this.getDimensionsDisplayOrder(dimensions[dimension].id); 
                 }
                 return dimensions
              };
              
              /**
               * Returns the order index of a dimension depending on its group .
               */
              ReportDefinitionController.prototype.getDimensionsDisplayOrder = function(id) {
                 var dimension = this.getDimension(id);
                 
                 if(!dimension){
                    dimension = this.getComputedColumnAsDimensions()[id];
                    return;
                 }
                 
                 if(dimension.metadata && dimension.metadata.isDescriptor){
                    return 2;
                 }else if(dimension.metadata && dimension.metadata.isComputedType){
                    return 3;
                 }
                 return 1;
              };

         
          
			}
			

		function activityFilterTemplate(){
				return {
					// Processes
					process_filter_auxiliary : true,
					selectedProcesses : [],

					// Activities
					activity_filter_auxiliary : true,
					activity_filter_interactive : false,
					activity_filter_nonInteractive : true
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