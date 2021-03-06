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
		[ "bpm-reporting/js/report/I18NUtils", "bpm-reporting/js/report/AngularAdapter",
				"bpm-reporting/js/report/ReportingService",
				"bpm-reporting/js/report/ReportRenderingController",
				"bpm-reporting/js/SchedulingController",
				"bpm-reporting/js/ReportFilterController",
				"bpm-reporting/js/ReportHelper",
				"bpm-reporting/js/utils",
				"bpm-reporting/js/m_codeEditorAce",
				"bpm-reporting/js/m_autoCompleters"],
		function(I18NUtils, AngularAdapter, ReportingService,
				ReportRenderingController, SchedulingController, ReportFilterController, ReportHelper,
				utils, m_codeEditorAce, m_autoCompleters) { 
		      var angularServices = null;
			return {
				create : function(angular, reportUID, name, path, isClone, options) {
					var controller = new ReportDefinitionController();
					
			        var angularAdapter = new bpm.portal.AngularAdapter(options);

			        //initialize controller and services
			        var angularModule = angularAdapter.initializeModule(angular);
			        controller.initializeAutocompleteDir(angularModule);
			        
			        controller.initializeFilterDir(angularModule);

			        //bootstrap module
			        angularAdapter.initialize(angular);

					controller = angularAdapter
							.mergeControllerWithScope(controller);
					
					angularServices = angularAdapter.getAngularServices();
					
					var renderingController = ReportRenderingController.create(angularServices);

					controller.initialize(renderingController, reportUID, name, path, isClone);
					
					controller.loadCustomTheme();
					
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
						renderingController, reportUID, name, path, isClone) {
					
					//set toolbar
					this.showSaveInstanceBtn = true;
					this.showSaveBtn = true;
					this.showSaveMessage = false;
					this.saveMessage = "Some text";
					
					this.renderingController = renderingController;
					this.schedulingController = SchedulingController.create();
					this.reportHelper = ReportHelper.create(this.reportingService);
					
					this.path = path;
					this.isClone = isClone;
					this.reportUID = reportUID;

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
					
					this.scheduling = {
                        "nextExecutionDate" : null,
                        "nextExecutionDateDay" : null
               };
					
					this.factSelect = jQuery("#factSelect");
					this.chartTypeSelect = jQuery("#chartTypeSelect");
					this.layoutSubTypeSelect = jQuery("#layoutSubTypeSelect");
					this.dimensionDisplaySelect = jQuery("#dimensionDisplaySelect");
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
				      					
					this.expressionEditor.loadLanguageTools();
					this.expressionEditor.setSessionData("$keywordList",["test", "air", "word"]);
					this.expressionEditor.setValue("");
					this.expressionEditor.disable();
					
					$(this.expressionEditor).on("moduleLoaded",function(event,module){
                  var sessionCompleter;
                  if(module.name==="ace/ext/language_tools"){
                     sessionCompleter = m_autoCompleters.getSessionCompleter();
                     self.expressionEditor.addCompleter(sessionCompleter);
                  }
               });
					
					window.parent.EventHub.events.subscribe("BPM-REPORTING-REPORT-NAME-UPDATED", function(reportUID, newName, newPath) {
					   if (self.report.reportUID == reportUID)
					   {
					      self.report.name = newName;
					      self.report.storage.path = newPath; 
					      self.updateView();   
					   }
               }, false);
					
					var self = this;

					document.body.style.cursor = "wait";

					CKEDITOR.replace("documentTemplateEditor", {
						height : 450,
						extraPlugins : 'table,image,specialchar',
						allowedContent : true,
						enterMode : CKEDITOR.ENTER_BR,
						autoParagraph : false,
						fillEmptyBlocks: false,
						entities: false,
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
							self.modelParticipants = self.reportingService.getModelParticipants();

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

									if (!self.report.layout.table.selectedCumulants) {
										self.report.layout.table.selectedCumulants = [];
									}

								}

								self.populateChartTypes();
								self.updateView();
							});

							self.dimensionDisplaySelect.change(function () {
								self.report.layout.table.dimensionDisplay = self.dimensionDisplaySelect.val();
							});
							
							self.cumulantsDisplaySelect.change(function () {
								self.report.layout.table.cumulantsDisplay = self.cumulantsDisplaySelect.val();
							});

							self.chartTypeSelect
							.change(function () {
								if(self.report.layout.chart){
									self.report.layout.chart.type = self.chartTypeSelect
									.val();	
								}

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
										self.resetParamFilters();
										self.renderingController.showRetrieveAll = false;
										self.renderingController.refreshPreview(self, self.report, self.parameters).done(
												function(){
													//self.resetParamFilters();
													self.updateView();			
												});
									}
									if (ui.newPanel.selector === "#schedulingTab") {
										self.resetParamFilters();
										self.updateView();
									}
									
									if (ui.newPanel.selector === "#dataSetTab") {
										//refresh facts as they contain computed columns
										self.populateFacts();
										self.updateView();
									}
								}
							});

							self
							.loadOrCreateReportDefinition(
								reportUID, name, path, isClone)
							.done(
								function () {
								// TODO Need a
								// cleaner
								// initialization
								// function
								self.dataAvailable = true;
								self
								.changePrimaryObject(true);
								self.changeFact();
								self
								.changeFirstDimension();
								self.renderingController
								.initialize(self.report);

								self.layoutSubTypeSelect.val(self.report.layout.subType);
								jQuery("#layoutSubTypeSelect").change();

								if(self.report.layout.table){
									self.dimensionDisplaySelect.val(self.report.layout.table.dimensionDisplay);
									self.cumulantsDisplaySelect.val(self.report.layout.table.cumulantsDisplay);	
								}

								if(self.report.layout.chart){
									self.chartTypeSelect
									.val(self.report.layout.chart.type);	
								}

								self.schedulingController
								.initialize(self.report.scheduling);

								self.runInAngularContext(function (scope) {
									scope.$watch("report.scheduling", function (newValue, oldValue) {
									   if (angular.equals(newValue.delivery, oldValue.delivery)) {
									      self.getNextExecutionDate();                                 
                              } 
									}, true);
								});
								
								self.runInAngularContext(function (scope) {
									scope.$watch("report.layout.table.selectedCumulants", function (newValue, oldValue) {
									   if (!(angular.equals(newValue, oldValue))) {
									      self.processFactPolicies();
									   } 
									}, true);
								});
			
								self.factSelect.val(self.report.dataSet.fact);
								
								if (self.report.storage.state == "saved") {
									self.showFavoriteBtn = true;
								} else {
									self.showFavoriteBtn = false;
								}
								
								if(self.report.layout.type == 'document'){
									self.showSaveInstanceBtn = false;
								}
								
								if (!self.report.scheduling.delivery.participant)
		                  {
		                     self.schedulingParticipantsSelect.val(jQuery("#schedulingParticipantsSelect option:first").val());
		                     self.report.scheduling.delivery.participant = self.schedulingParticipantsSelect.val(); 
		                  } else {
								self.schedulingParticipantsSelect.val(self.report.scheduling.delivery.participant);
		                  }
								
								self.updateView();

								document.body.style.cursor = "default";

								if (self.report.layout.type == "document") {
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
							console.debug('refreshModelData preferencedata success...');
						}).fail(function () {
							console.debug('refreshModelData preferencedata falied...');
					});
				};
				
				/**
				 * 
				 */
				ReportDefinitionController.prototype.initializeFilterDir = function(
						angularModule) {
					var self = this;
					angularModule.directive('reportFilter', function() {
						return {
							restrict : 'E',
							scope : {
								reportFilterController : '=controller'
							},
							templateUrl : 'templates/reportFilters.html'
						};
					});
				};
				
				/**
				 * 
				 */
				ReportDefinitionController.prototype.reloadTable = function(previewRetrieveAll) {
					var self = this;

					this.renderingController.previewRetrieveAll = previewRetrieveAll;

					if (previewRetrieveAll) {
						self.renderingController.showRetrieveAll = true;
					}
					this.renderingController.refreshPreview(this, this.report, this.parameters).done(function(){
						self.updateView();	
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
						reportUID, name, path, isClone) {
					var deferred = jQuery.Deferred();

					var self = this;

					if (path) {
						this.reportingService.retrieveReportDefinition(path)
								.done(function(report) {
								   
								   if (self.isClone)
                           {
								      var clonedReport = self.cloneReportDefinition(report);
                              clonedReport.name = "Copy " + clonedReport.name;
                              clonedReport.reportUID = reportUID;
                              clonedReport.storage.path = null;
                              self.path = null;
                              report = clonedReport;
                           }

									self.report = report;
									
									console.log("Loaded report definition: ", self.report);

									self.initFilters();
									
									deferred.resolve();
								}).fail(function() {
									deferred.reject();
								});
					} else {
						// Initialize
						// defaults

						//get user locale
						
						self.reportingService.getUserLanguage()
						.done(function(userLanguage) {
						   
							// TODO Get chart options from central place
							self.report = {
							    userLanguage : userLanguage,
							    reportUID : reportUID,      
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
									factDurationUnit : "d",
									firstDimensionCumulationIntervalCount : 1,
									firstDimensionCumulationIntervalUnit : "d",
									firstDimensionDurationUnit: "s",
									filters: getDefaultFilterFor("processInstanceStartTimestamp"),
									factPolicies : []
								},
								parameters : {},
								layout : {
									type : "simpleReport",
									chart : {
										type : self.reportingService.metadata.chartTypes.xyPlot.id,
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
														angle : 0,
														showMark : true,
														showGridline : true
													},
													showTickMarks : true,
													showTicks : true
												},
												x2axis : {},
												yaxis : {
													tickOptions : {
														mark : "outside",
														markSize : 4,
														angle : 0,
														showMark : true,
														showGridline : true
													},
													showTickMarks : true,
													showTicks : true
												},
												y2axis : {}
											},
											legend : {
												show: true,
												location : "e"
											},
											highlighter : {},
											cursor : {
												show : true,
												followMouse : true,
												showTooltip : true
											},
											zoom : {}
										}
									},
									table : {
										options : {
										   showExportButtons : true,	
										   showSearchInput : true,
								           showVisibleRowCountSelector : true  
										}
									},
									document : {}
								},
								scheduling : self.schedulingController
										.createDefaultSettings()
							};

							self.report.storage.participant = "Administrator";
							
							self.report.dataSet.fact = self.getPrimaryObject().facts.count.id;
							self.setDefaultFirstDimension();
							
							self.initFilters();
							
							deferred.resolve();
						});
					}

					return deferred.promise();
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.initFilters = function() {
					var self = this;
					self.reportFilterController = ReportFilterController
					.create(self.report, self.report.dataSet.filters, self.reportingService, self.reportHelper, false);
					self.reportFilterController.baseUrl = "..";
					
					this.prepareParameters();
					
					self.reportParameterController = ReportFilterController
					.create(self.report, self.parameters, self.reportingService, self.reportHelper, true);
					self.reportParameterController.baseUrl = "..";
				};
				
				/**
				 * 
				 */
				ReportDefinitionController.prototype.resetFilters = function() {
					this.reportFilterController.resetReport(this.report, this.report.dataSet.filters);
				};
				
				/**
				 * 
				 */
				ReportDefinitionController.prototype.resetParamFilters = function() {
					this.prepareParameters();
					this.reportParameterController.resetReport(this.report, this.parameters);
				}
				
				/**
				 * 
				 */
				ReportDefinitionController.prototype.prepareParameters = function() {
					this.parameters = this.reportHelper.prepareParameters(this.report.dataSet.filters);
				}
				
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
				
				/**
				 * 
				 */
				ReportDefinitionController.prototype.primaryObjectEnumGroup = function(id) {
					return this.reportHelper.primaryObjectEnumGroup(id, this.report);
				};
				
				/**
				 * 
				 */
				ReportDefinitionController.prototype.getAvailableCumulantsEnum = function() {
					if (this.dataAvailable && this.report.layout) {
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
					} else {
						return null;
					}
						
				};
				
				ReportDefinitionController.prototype.getSelectedCumulantsEnum = function() {
					if (this.dataAvailable && this.report.layout) {
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
					} else {
						return null;
					}
				};
				
				/**
				 * 
				 */
				ReportDefinitionController.prototype.getFact = function() {
					return (this.dataAvailable && this.getPrimaryObject()) ? this.reportingService.getCumulatedFacts(this.report)[this.report.dataSet.fact] : null;
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.getDimension = function(id) {
				   var dimensions = this.reportingService.getCumulatedDimensions(this.report);
               for(var i = 0; i < dimensions.length; i++){
                  if(id == dimensions[i].id){
                     return dimensions[i];
                  }
               }
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.getFirstDimension = function() {
				   return this.getDimension([this.report.dataSet.firstDimension]);
				};

				

				/**
             * 
             */
				ReportDefinitionController.prototype.changeDataSetType = function() {
					if (this.report.dataSet.type === "processHistory") {
						this.report.layout.type = "processDiagram";
					} else if (this.report.dataSet.type === 'seriesGroup') {
						this.report.layout.type = "simpleReport";
						this.report.layout.subType = this.reportingService.metadata.layoutSubTypes.chart.id;
						jQuery("#layoutSubTypeSelect").change();
						this.report.dataSet.columns = [];
					} else if (this.report.dataSet.type === 'recordSet') {
						this.report.layout.type = "table";
					}
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.layoutChanged = function() {
					this.report.layout.type = 'document';
					this.report.layout.subType = null;
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.changePrimaryObject = function(
						initialize) {
					this.primaryObjectSelect
							.val(this.report.dataSet.primaryObject);

					if (!initialize) {
						this.reportFilterController.filterSelected = [];
						this.resetReportDefinitionProperties();
					}

					this.populateFacts();
					
					this.populatelayoutSubTypes();
					//this.populateChartTypes();
					
					this.populateDimensionDisplay();
					
					this.populateCumulantsDisplay();
					
					if(this.report.layout.chart){
						this.report.layout.chart.options.title = this
						.getPrimaryObject().name;	
					}

					this.updateView();
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.populateFacts = function() {
					
					this.factSelect.empty();

					var stdQuantities = "<optgroup label=\"" + this.getI18N("reporting.definitionView.stdQuantities") + "\">";
					
					var group = "<optgroup label=\"" + this.getI18N("reporting.definitionView.numericDescriptors") + "\">";
					
					var computedColGroup = "<optgroup label=\"" + this.getI18N("reporting.definitionView.computedColumns") + "\">";
					
					var areDiscriptorsAvailable = false;
					var areComputedColumnsAvailable = false;
					
					var cumulatedFacts = this.reportingService.getCumulatedFacts(this.report, true); 
					
					for ( var n in cumulatedFacts) {
						var fact = cumulatedFacts[n];

						if (this.isNumeric(fact) || this.isCount(fact) || this.isDuration(fact))
						{
						   if (fact.metadata && fact.metadata.isDescriptor) {
							group += "<option value='" + fact.id + "'>" + fact.name + "</option>";
							areDiscriptorsAvailable = true;
						   } else if (fact.metadata && fact.metadata.isComputedType) {
							computedColGroup += "<option value='" + fact.id + "'>" + fact.name + "</option>";
							areComputedColumnsAvailable = true;
						   } else {
							stdQuantities += "<option value='" + fact.id + "'>" + fact.name + "</option>";
						   }
						}
					}
					
					stdQuantities += "</optgroup>";
					this.factSelect.append(stdQuantities);
					
					group += "</optgroup>";
					computedColGroup += "</optgroup>";
					
					if (areDiscriptorsAvailable) {
					   this.factSelect.append(group);
					}
					
					if (areComputedColumnsAvailable) {
						this.factSelect.append(computedColGroup);
					}
					
					this.factSelect.val(this.report.dataSet.fact);
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
					if(this.report.layout.chart){
						if (this.getFact()) {
							this.report.layout.chart.options.axes.yaxis.label = this
							.getFact().name;	
						}
					}
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.changeFirstDimension = function() {
					console.log("First Dimension: ", this.getFirstDimension());
					this.populateChartTypes();
					if(this.report.layout.chart){
						this.report.layout.chart.options.axes.xaxis.label = this
						.getFirstDimension().name;	
					}

					this.report.dataSet.firstDimensionParameters = [];

					this.updateView();
				};
				
				/**
             * 
             */
            ReportDefinitionController.prototype.changeGroupBy = function() {
               this.populateChartTypes();
               this.report.dataSet.columns = [];
               this.updateView();
            };

				/**
				 * 
				 */
				ReportDefinitionController.prototype.getTestEnumerators = function() {
					return [ "Nase", "Wurst", "Propase" ];
				};


				/**
				 * Returns a consolidated list of possible dimensions excluding
				 * joined data from external data sources.
				 */
				ReportDefinitionController.prototype.getInternalDimensions = function() {
					var dimensions = [];

					var dims = this.getPrimaryObject().dimensions;

					for ( var m in dims) {
						var dimension = dims[m];

						if (!dimension.notSupportedAsAutoComplete) {
							dimensions.push({
								id : dimension.id,
								name : dimension.name
							});
						}
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
				ReportDefinitionController.prototype.getGroupDimensions = function() {
				    var dimensions = this.reportingService.getCumulatedDimensions(this.report);
				    for (var i = dimensions.length - 1; i >= 0; i--) {
				        // Only discrete types can be used as group criteria
				        if (!this.reportingService.isDiscreteType(dimensions[i].type)) {
				            dimensions.splice(i, 1);
				        } else if (!this.isValidGroupableDimension(dimensions[i])) {
				           dimensions.splice(i, 1);
				        }
				    }
				
				    return dimensions;
				};

				ReportDefinitionController.prototype.populatelayoutSubTypes = function() {
					this.layoutSubTypeSelect.empty();

					var option = "<option value='ID'>LABEL</option>";
			        
					var layoutSubTypes = this.reportingService.metadata.layoutSubTypes;
					
			        for(var i in layoutSubTypes){
			            this.layoutSubTypeSelect.append(option.replace("ID", layoutSubTypes[i].id).replace("LABEL", layoutSubTypes[i].name));
			        }
				};

				ReportDefinitionController.prototype.populateDimensionDisplay = function() {
					this.dimensionDisplaySelect.empty();

					var option = "<option value='ID'>LABEL</option>";
			        
					var cumulantsDisplay = this.reportingService.metadata.cumulantsDisplay;
					
			        for(var i in cumulantsDisplay){
			            this.dimensionDisplaySelect.append(option.replace("ID", cumulantsDisplay[i].id).replace("LABEL", cumulantsDisplay[i].name));
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
					.append("<option value='donutChart'>"
							+ this.reportingService.metadata.chartTypes.donutChart.name
							+ "</option>");
					
					if (!fact_count) {
						this.chartTypeSelect
								.append("<option value='candlestickChart'>"
										+ this.reportingService.metadata.chartTypes.candlestickChart.name
										+ "</option>");
					}

					if (!this.report.dataSet.groupBy || (this.report.dataSet.groupBy == 'None')) {
						this.chartTypeSelect
								.append("<option value='pieChart'>"
										+ this.reportingService.metadata.chartTypes.pieChart.name
										+ "</option>");
					}
					
					if(this.report.layout.chart){
						this.chartTypeSelect.val(this.report.layout.chart.type);	
					}
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.getChartType = function() {
					if (this.dataAvailable && this.report.layout) {
						if(this.report.layout.chart){
							return this.reportingService.metadata.chartTypes[this.report.layout.chart.type];	
						}
					}
					return null;
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
					console.log("Report Content: ", this.getJsonString());

					var self = this;
					
					if(self.report && self.report.storage.path != null){
						self.path = self.report.storage.path;
					}
					
					//update markup
					if (this.report.layout.type == "document") {
					    self.report.layout.document = {
					        markup: CKEDITOR.instances["documentTemplateEditor"].getData()
					    };
					}
					
					self.report.name = self.reportingService.validateReportName(self.report.name);
					
					//Check if Report name has been changed. If yes then first invoke rename and then save 
					if (self.path != null)
					{
					   var ext = self.path.substring(self.path.lastIndexOf('.'), self.path.length);
					   var id = self.path; 
					   var lastIndex = id.lastIndexOf("/");
	               id = id.substr( lastIndex + 1, id.length);
	               id = id.substr( 0, id.lastIndexOf("."));
	               
	               if (id !== self.report.name)
                  {
                     //report name has been changed
	                  console.log("report name has been changed");
	                  
	                  self.reportingService.renameAndSaveReportDefinition(self.report)
                     .done(
                           function(report) {
                              self.path = report.storage.path;
                              self.report.storage = report.storage;
                              window.parent.EventHub.events.publish("BPM-REPORTING-REPORT-UPDATED", 
                                       self.report.reportUID, self.report.name, self.path);
                              self.updateView();
                           });
	                  
	                  return;
                  }
               } 
               self.reportingService.saveReportDefinition(self.report)
                  .done(
                        function(report) {
                           self.report.storage = report.storage;
                           self.report.metadata = report.metadata;
                           if (self.path == null)
                           {// Create Case
                              window.parent.EventHub.events.publish("BPM-REPORTING-REPORT-CREATED", 
                                       self.report.reportUID, self.report.name, self.path);
                           }
                           self.showFavoriteBtn = true;
                           
                           self.showSaveMessage = true;
                           self.saveMessage = I18NUtils.getProperty('reporting.definitionView.saveReport.message').replace("{0}", self.report.name);
                           setTimeout(function () {
                               self.showSaveMessage = false;
                               self.updateView();
                           }, 5000);
                           
                           
                           self.updateView();
                        });
               document.body.style.cursor = "default";
					
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
            ReportDefinitionController.prototype.getParametersOfTypeTimestamp = function() {
               var parametersOfTypeTimestamp = {};
               
               for ( var parameter in this.parameters)
               {
            	   var dimension = this.getDimension(this.parameters[parameter].dimension);
                  if (dimension && dimension.type.id == this.reportingService.metadata.timestampType.id)
                  {
                	  parametersOfTypeTimestamp[dimension.id + ".from"] = dimension.id + " (" + this.getI18N("reporting.definitionView.from.label") + ")"; 
                	  parametersOfTypeTimestamp[dimension.id + ".to"] = dimension.id + " (" + this.getI18N("reporting.definitionView.to.label") + ")";
                  }
               }
               return parametersOfTypeTimestamp;
            };
            
            

				/**
				 * 
				 */
				ReportDefinitionController.prototype.hasParameters = function() {
					if(this.parameters && this.parameters.length > 0){
						return true;
					}
					
					return false;
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

					if (!parent.iDnD.getTransferObject()){
						parent.iDnD.hideIframe();
						return;
					}
					
					//simple
					var iframeTag = "<iframe allowtransparency='true' frameborder='0' "
							+ "sandbox='allow-same-origin allow-forms allow-scripts' "
							+ "scrolling='auto' style='border: none; width: 100%; height: 150%;' "
							+ "src='"
							+ "../../.."
							+ "/plugins/views-common/views/report/reportViewer.html?viewMode=embedded&qualifiedParameters=false&path="
							+ parent.iDnD.getTransferObject().path
							+ "'></iframe>";

					//angularized
					/*iframeTag = '<sd-report-frame path="'
						+ parent.iDnD.getTransferObject().path + '"'
						+ " parameters=></sd-report-frame>";*/
					
					console.log("Object dropped at : " ,parent.iDnD.getTransferObject());

					if (this.report.layout.type == "document") {
						CKEDITOR.instances["documentTemplateEditor"]
								.setData(CKEDITOR.instances["documentTemplateEditor"]
										.getData()
										+ iframeTag);
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
					

					console.log("Start Date: ", this.report.scheduling.recurrenceRange.startDate);

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
					console.log("Delete: ", field);

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

				ReportDefinitionController.prototype.deleteComputedColumn = function(
						column) {
					
					this.adjustReportDefinition(column);
					
					for ( var n = 0; n < this.report.dataSet.computedColumns.length; ++n) {
						if (this.report.dataSet.computedColumns[n].$$hashKey === column.$$hashKey) {
							this.report.dataSet.computedColumns.splice(n, 1);

							if (this.report.dataSet.computedColumns.length) {
								this.selectedComputedColumn = this.report.dataSet.computedColumns[0];
							} else {
								this.selectedComputedColumn = null;
							}
							this.expressionEditor.setValue("");
							return;
						}
					}
				};
				
				/**
				 * adjust report definition if computer column is being used elsewhere.
				 */
				ReportDefinitionController.prototype.adjustReportDefinition = function(
						column) {
					// check if the selected computed column is set as
					// Fact
					if (this.report.dataSet.fact == column.id) {
						this.report.dataSet.fact = this.getPrimaryObject().facts.count.id;
					}

					// Dimension
					if (this.report.dataSet.firstDimension == column.id) {
						if (this.report.dataSet.primaryObject == this.reportingService.metadata.objects.processInstance.id) {
							this.report.dataSet.firstDimension = this.reportingService.metadata.objects.processInstance.dimensions.priority.id;
						} else {
							this.report.dataSet.firstDimension = this.reportingService.metadata.objects.activityInstance.dimensions.activityInstanceDuration.id;
						}
					}

					// Filter
					if (this.report.dataSet.filters) {
						var filters = this.report.dataSet.filters;
						for (var int = 0; int < filters.length; int++) {
							var filter = filters[int];
							if (filter && filter.dimension == column.id) {
								this.reportFilterController
										.deleteFilter(filter);
							}
						}
					}

					// Group by
					if (this.report.dataSet.groupBy == column.id) {
						this.report.dataSet.groupBy = "None";
						this.changeGroupBy();
					}

					// Column in Record set
					if (this.report.dataSet.columns) {
						var columns = this.report.dataSet.columns;
						for (var int2 = 0; int2 < columns.length; int2++) {
							var column2 = columns[int2];
							if (column2.id == column.id) {
								this.removeItem(columns, column.id);
							}
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
             * 
             */
            ReportDefinitionController.prototype.byDefaultSelectFirstComputedColumn = function() {
               if (this.report.dataSet.computedColumns.length > 0)
               {
                  this.selectComputedColumn(this.report.dataSet.computedColumns[0]);
               }
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
                                 console.log("Failed next Execution Date: " , err);
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
                      dimensionNames[m] = replaceSpecialChars(dimension.id);
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
            		//delete list[list.indexOf(itemId)];
            		var index = list.indexOf(itemId);
                	if (index > -1) {
                		list.splice(index, 1);
                	}
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
	        * This function will return true if argument is of Integer Type
	        */
	      ReportDefinitionController.prototype.isNumeric = function(element) {
	    	  	if ((element.type.id === this.reportingService.metadata.integerType.id)
						|| (element.type.id === this.reportingService.metadata.decimalType.id)) {
					return true;
				}
				return false;
	      };
              
              /**
               * This function will return true if argument is of duration Type
				 */
             ReportDefinitionController.prototype.isDuration = function(element) {
            	 if (this.dataAvailable && element) {
            		 return (element.type.id === this.reportingService.metadata.durationType.id)? true : false;
            	 } else {
            		 return false;
            	 }
             };
             
             /**
              * This function will return true if argument is of Count Type
              */
            ReportDefinitionController.prototype.isCount = function(element) {
               return (element.type.id === this.reportingService.metadata.countType.id)? true : false;
            };
     
              /**
               * Adding order parameter to dimension object for displaying it on UI in specific order
               */
              ReportDefinitionController.prototype.getCumulatedDimensionsByGroup = function() {
                 var dimensions = this.reportingService.getCumulatedDimensions(this.report);
                 for ( var dimension in dimensions)
                 {
                    var group = this.primaryObjectEnumGroup(dimensions[dimension].id);
                    dimensions[dimension].order = this.reportHelper.getDimensionsDisplayOrder(dimensions[dimension].id, this.report); 
                 }
                 return dimensions
              };
              
            /* This function will return Report Definition with default values
            * 
            */
            ReportDefinitionController.prototype.resetReportDefinitionProperties = function() {
               this.report.dataSet.computedColumns = [];
               this.expressionEditor.setValue("");
               this.report.dataSet.columns = [];
               this.report.dataSet.factDurationUnit = "d";
               this.report.dataSet.firstDimensionCumulationIntervalCount = 1;
               this.report.dataSet.firstDimensionCumulationIntervalUnit = "d";
               this.report.dataSet.groupBy = "None";
               
               this.setDefaultFirstDimension();
               this.setDefaultFact();
           
               //set default filter
               var self = this;
               if (self.report.dataSet.primaryObject == self.reportingService.metadata.objects.processInstance.id) {
				    self.report.dataSet.filters = getDefaultFilterFor(self.reportingService.metadata.objects.processInstance.dimensions.processInstanceStartTimestamp.id);
				} else {
					self.report.dataSet.filters = getDefaultFilterFor(self.reportingService.metadata.objects.activityInstance.dimensions.startTimestamp.id);
				}
               
               this.resetFilters();
               this.resetParamFilters();
            };
            
            /**
             * Getting latest Record Set Available columns &
             * Adding order parameter to dimension object for displaying it on UI in specific order
             */
            ReportDefinitionController.prototype.getRecordSetAvailableColumns = function() {
               var dimensions = this.reportingService.getCumulatedDimensions(this.report);
               
               var self = this;
               
               dimensions = self.populateAggegateDimensions(dimensions);
               
               var enumerators = [];
               for ( var n in dimensions ) {
                  var add = true; 
                  
                  if(dimensions[n].notSupportedAsColumn){
                	  add = false;
                  }else if (this.report.dataSet.columns) {
                     this.report.dataSet.columns
                           .forEach(function(column) {
                              if (self.report.dataSet.groupBy && self.report.dataSet.groupBy != 'None') 
                              {
                                 var groupDimensions = self.getGroupDimensions();
                                 for ( var groupDim in groupDimensions)
                                 {
                                    if (groupDimensions[groupDim].id === dimensions[n].id && dimensions[n].id == column.id)
                                    {
                                       add = false;
                                    }
                                 }
                                 
                                 if ((column.id + "#" + column.metaData.aggregationFunction) === dimensions[n].id)
                                 {
                                    add = false;
                                 }
                              } else if (dimensions[n].id == column.id) 
                              {
                                 add = false;
                              }
                           });
                  }
                  if(add){
                     enumerators.push(dimensions[n]);  
                  }
               }
               
               dimensions = enumerators;
               
               for ( var dimension in dimensions)
               {
                  var group = this.primaryObjectEnumGroup(dimensions[dimension].id);
                  dimensions[dimension].order = this.reportHelper.getDimensionsDisplayOrder(dimensions[dimension].id, this.report);

                  var id = dimensions[dimension].id;
                  
                  var index = id.indexOf("#average");
                  if (index != -1) {
                	  dimensions[dimension].subOrder = 10;
                  }
                  
                  var index1 = id.indexOf("#sum");
                  if(index1 != -1) {
                	  dimensions[dimension].subOrder = 11;
                  }
                  
                  if (index == -1 && index1 == -1 && (id.indexOf("#") != -1)) {
                	  dimensions[dimension].subOrder = 12;
                  }
                  if (!dimensions[dimension].inOrder) {
                	  dimensions[dimension].inOrder = 0;
                  }
               }
               return dimensions;
            };
            
            /**
             * Getting latest Record Set Selected columns
             */
            ReportDefinitionController.prototype.getRecordSetSelectedColumns = function() {
               
               var enumerators = [];
               if (!this.report.dataSet.columns){
                  return enumerators;
               }  
               
               var availableColumns = this.reportingService.getCumulatedDimensions(this.report);
               var selColumns = this.report.dataSet.columns;
               
               var self = this;
               
               availableColumns = self.populateAggegateDimensions(availableColumns);
                
               for (var i in selColumns) {
                  for ( var k in availableColumns) {
                     if (this.report.dataSet.groupBy && this.report.dataSet.groupBy != 'None') 
                     {
                        var groupDimensions = self.getGroupDimensions();
                        for ( var groupDim in groupDimensions)
                        {
                           if (groupDimensions[groupDim].id === selColumns[i].id && selColumns[i].id === availableColumns[k].id)
                           {
                              enumerators.push(availableColumns[k]);
                              break;
                           }
                        }
                        
                        if ((selColumns[i].id + "#" + selColumns[i].metaData.aggregationFunction) === availableColumns[k].id)
                        {
                           enumerators.push(availableColumns[k]);
                           break;
                        }
                     } else if (selColumns[i].id === availableColumns[k].id)
                     {
                        enumerators.push(availableColumns[k]);
                        break;
                     }
                  };
               }
               return enumerators;
            };
            
            /**
             * Function to clone a JS object.
             */
            ReportDefinitionController.prototype.cloneReportDefinition = function(report) {
               var clonedReport = jQuery.extend(true, {}, report);               
               return clonedReport;
            };
            
            /**
             * 
             */
            ReportDefinitionController.prototype.saveReportInstance = function() {
            	var self = this;
            	self.renderingController.saveReportInstance(self.report, self.parameters).then(function(data) {
            		self.showSaveMessage = true;
                    self.saveMessage = I18NUtils.getProperty('reporting.definitionView.saveReport.message').replace("{0}", data.definition.instanceName);
                    setTimeout(function () {
                        self.showSaveMessage = false;
                        self.updateView();
                    }, 5000);
            	});
    		};
    		
    		/**
             * 
             */
            ReportDefinitionController.prototype.addToFavorites = function() {
            	var self = this;
            	this.reportingService.addToFavorites(this.report.name, this.report.metadata.documentId).done(function(){
            		self.updateView();	
            	});
    		};
    		
    		/**
    		 * 
    		 */
    		ReportDefinitionController.prototype.removeFromFavorites = function() {
    			var self = this;
            	this.reportingService.removeFromFavorites(this.report.metadata.documentId).done(function(){
            		self.updateView();
            	});
    		};
    		
    		/**
    		 * 
    		 */
    		ReportDefinitionController.prototype.isFavorite = function() {
				if (this.report && this.report.metadata
						&& this.report.metadata.documentId) {
					return this.reportingService
							.isFavoriteReport(this.report.metadata.documentId);
				}
				return false;
			};
			
			ReportDefinitionController.prototype.moveItemsRight = function (selectedItems, list) {
            var self = this;
            selectedItems.forEach(function(itemId){
               var index = self.getItemIndex(itemId, list);
               if (index == -1) {
                  self.addItem(list, itemId, "h");
               }
            });
         };
         
         ReportDefinitionController.prototype.moveItemsLeft = function (selectedItems, list) {
            var self = this;
            selectedItems.forEach(function(itemId){
               var index = self.getItemIndex(itemId, list);
               if (index != -1) {
                  self.removeItem(list, itemId);
               }
            });
         };
         
         ReportDefinitionController.prototype.moveAllItemsRight = function (selectedItems, list) {
            var self = this;
            selectedItems.forEach(function(item){
               var index = self.getItemIndex(item.id, list);
               if (index == -1) {
                  self.addItem(list, item.id, "h");
               }
            });
         };
         
         ReportDefinitionController.prototype.moveAllItemsLeft = function (list) {
            list.length = 0;
         };
         
         /**
          * 
          */
         ReportDefinitionController.prototype.moveItemUpWard = function(item, list) {
             if (this.isMultiSelected(item))
             {
                return;
             }
             
             var index = this.getItemIndex(item[0], list);
             if (index != -1) {
                list.splice(index - 1, 0, list.splice(index, 1)[0]);
             }
         };
         
         /**
          * 
          */
         ReportDefinitionController.prototype.moveItemDownWard = function(item, list) {
            if (this.isMultiSelected(item))
            {
               return;
            }
            
            var index = this.getItemIndex(item[0], list);
            if (index != -1) {
               list.splice(index + 1, 0, list.splice(index, 1)[0]);
            }
        };
        
        /**
         * 
         */
        ReportDefinitionController.prototype.createAggegateDimensions = function(dimension, enumerators) {
           var aggregations = this.reportingService.metadata.recordSetAggregationFunctions;
           
           var inOrder = enumerators.length + 1;
                      
           for ( var n in aggregations ) {
              var obj = this  .cloneReportDefinition(dimension);
              obj.id = obj.id + "#" + aggregations[n].id;
              obj.name = obj.name + " (" + aggregations[n].name + ")";
              obj.inOrder = inOrder;
              enumerators.push(obj);
           }
        };
        
        /**
         * Adds item to columnDurationUnit
         */
        ReportDefinitionController.prototype.addItem = function(list, itemId, colDurationUnit) {
           //Strip Aggregation Function from ID
           var colId = StripAggregationFromColumnId(itemId);
           var aggregationFunction = "None";
           
           var dimension = this.getMetadataObjectFromId(colId);
           
           var isDescriptor = (dimension && dimension.metadata && dimension.metadata.isDescriptor)? true : false;
           
           if (this.report.dataSet.groupBy != null && this.report.dataSet.groupBy != 'None') {
              var aggregationAndColId = getAggregationAndColumnId(itemId);
              aggregationFunction = (aggregationAndColId instanceof Array) ? aggregationAndColId[1] : 
                 aggregationFunction; 
           }
           
           this.addMetadata(list, dimension, isDescriptor, colDurationUnit, aggregationFunction);
        };
        
        /**
         * Removes item from columnDurationUnit
         */
        ReportDefinitionController.prototype.removeItem = function(list, itemId) {
           var aggregationFunction = getAggregationAndColumnId(itemId);
           (aggregationFunction instanceof Array) ? this.removeMetadata(list, aggregationFunction[0], aggregationFunction[1]) : 
              this.removeMetadata(list, aggregationFunction); 
        };
        
        /**
         * Adds item's metadata to columnDurationUnit
         */
        ReportDefinitionController.prototype.addMetadata = function(list, dimension, isDescriptor, 
                 durationUnit, aggregationFunction) {
           list.push({
              "id": dimension.id,
              "metaData": {
                 "descriptor": isDescriptor,
                 "durationUnit": durationUnit,
                 "aggregationFunction": aggregationFunction
              }
           });
        };
        
        /**
         * Removes item to columnDurationUnit
         */
        ReportDefinitionController.prototype.removeMetadata = function(list, dimensionId, aggregationFunction) {
           
           for ( var i = this.report.dataSet.columns.length - 1; i >= 0; i--)
           {
              if (this.report.dataSet.groupBy && (this.report.dataSet.groupBy != 'None')) {
                 if (aggregationFunction == null || aggregationFunction === "None")
                 {// For GroupBy selected value
                    var groupDimensions = this.getGroupDimensions();
                    for ( var groupDim in groupDimensions)
                    {
                       if (groupDimensions[groupDim].id === list[i].id && list[i].id === dimensionId)
                       {
                          list.splice(i, 1);
                          break;
                       }
                    }
                 }
                 if (list[i].id === dimensionId && 
                          list[i].metaData.aggregationFunction === aggregationFunction)
                 {
                    list.splice(i, 1);
                 }
              } else if (list[i].id === dimensionId) {
                 list.splice(i, 1);
              }
           }
        };
        
        /**
         * Get item index from list
         */
        ReportDefinitionController.prototype.getItemIndex = function(itemId, list) {
           
           var colId = StripAggregationFromColumnId(itemId);
           var aggregationFunction = "None";
           
           if (this.report.dataSet.groupBy != null && this.report.dataSet.groupBy != 'None') {
              var aggregationAndColId = getAggregationAndColumnId(itemId);
              aggregationFunction = (aggregationAndColId instanceof Array) ? aggregationAndColId[1] : 
                 aggregationFunction; 
           }
           for ( var i = 0; i < list.length; i++)
           {
              if (this.report.dataSet.groupBy && (this.report.dataSet.groupBy != 'None')) 
              {
                 if (aggregationFunction === "None")
                 {// For GroupBy selected value
                    var groupDimensions = this.getGroupDimensions();
                    for ( var groupDim in groupDimensions)
                    {
                       if (groupDimensions[groupDim].id === list[i].id && list[i].id === colId)
                       {
                          return i;
                       }
                    }
                 }
                 
                 if (list[i].id === colId && 
                          list[i].metaData.aggregationFunction === aggregationFunction)
                 {
                    return i;
                 }
              } else if (list[i].id === colId) 
                 {
                      return i;
                 }
           }
           return -1;
         };
         
        /**
         * Set firstDimension to default value
         */
        ReportDefinitionController.prototype.setDefaultFirstDimension = function() {
           if (this.getPrimaryObject().id === this.reportingService.metadata.objects.processInstance.id)
           {
              this.report.dataSet.firstDimension = this.reportingService.metadata.objects.processInstance.dimensions.priority.id;
           } else if (this.getPrimaryObject().id === this.reportingService.metadata.objects.activityInstance.id) {
              this.report.dataSet.firstDimension = this.reportingService.metadata.objects.activityInstance.dimensions.activityInstanceDuration.id;
           }
           this.changeFirstDimension();
        };
        
        /**
         * Get Fact to default value
         */
        ReportDefinitionController.prototype.setDefaultFact = function() {
           if (this.getPrimaryObject().id === this.reportingService.metadata.objects.processInstance.id || 
                    this.getPrimaryObject().id === this.reportingService.metadata.objects.activityInstance.id)
           {
              this.factSelect.val(this.reportingService.metadata.objects.processInstance.facts.count.id);
           }
           this.factSelect.change();
        };
        
        /**
         * Get Metadata Object from Id
         */
        ReportDefinitionController.prototype.getMetadataObjectFromId = function(objectId) {
           var dimensions = this.reportingService.getCumulatedDimensions(this.report);
           var self = this;
           for ( var index in dimensions)
           {
              if (dimensions[index].id === objectId)
              {
                 return dimensions[index];
              }
           }
        };
        
        /**
         * 
         */
        ReportDefinitionController.prototype.populateAggegateDimensions = function(dimensions) {
           var aggregatedDimensions = [];
           var self = this;
           if (self.report.dataSet.groupBy && self.report.dataSet.groupBy != 'None')
           {
              dimensions.forEach(function(dimension) {
                 if (self.isValidNumericAggegateDimension(dimension) || self.isDuration(dimension))
                 {
                    self.createAggegateDimensions(dimension, aggregatedDimensions);
                 } else if (self.report.dataSet.groupBy === dimension.id) {
                    aggregatedDimensions.push(self.getMetadataObjectFromId(self.report.dataSet.groupBy));
                 }
              });
              return aggregatedDimensions;
           }
           return dimensions;
        };
        
        /**
          * Removing the OIDs from the column selector is a little bit of a special case
          * since even though they are numeric, aggregate functions on OIDs do not make
          * sense
          */
        ReportDefinitionController.prototype.isValidNumericAggegateDimension = function(dimension) {
           var isValid = false;
           var inValidNumericAggegateDimensions = [this.reportingService.metadata.objects.activityInstance.dimensions.processOID, 
                    this.reportingService.metadata.objects.activityInstance.dimensions.activityOID];
           if (this.isNumeric(dimension))
           {
              for ( var dim in inValidNumericAggegateDimensions)
              {
                 if (inValidNumericAggegateDimensions[dim].id === dimension.id)
                 {
                    return isValid;
                 }
              }
              isValid = true;
           }
           return isValid;
        };
        
        /**
         * Process OID, Activity OID should not be a grouping criteria and it should be removed.
         */
        ReportDefinitionController.prototype.isValidGroupableDimension = function(dimension) {
          var isValid = false;
          var inValidGroupableDimensions = [this.reportingService.metadata.objects.activityInstance.dimensions.processOID, 
                   this.reportingService.metadata.objects.activityInstance.dimensions.activityOID];
          for ( var dim in inValidGroupableDimensions) {
            if (inValidGroupableDimensions[dim].id === dimension.id) 
            { 
              return isValid; 
            }
          }

          if (dimension.metadata && dimension.metadata.isCompositeOrLinkDescriptor) { 
            return false; 
          }
          
          isValid = true;
          return isValid;
        };

        /**
         * Manages layout Cursor settings
         */
        ReportDefinitionController.prototype.updateCursorSettings = function() {
           this.report.layout.chart.options.cursor.zoom = (this.report.layout.chart.options.cursor.show == false) ? false : 
              this.report.layout.chart.options.cursor.zoom;
           
           this.report.layout.chart.options.cursor.showTooltip = (this.report.layout.chart.options.cursor.show == false) ? false : 
              this.report.layout.chart.options.cursor.showTooltip;
              
        };
        
        /**
         * 
         */
        ReportDefinitionController.prototype.processFactPolicies = function() {
        	var self = this;
        	if (self.report.layout.table.selectedCumulants.indexOf(self.reportingService.metadata.cumulants.sum.id) != -1) {
        		var index = self.report.dataSet.factPolicies.indexOf(self.reportingService.metadata.cumulants.sum.id);
        		if (index == -1) {
        			self.report.dataSet.factPolicies.push(self.reportingService.metadata.cumulants.sum.id);
        		}
        	} else {
        		var index = self.report.dataSet.factPolicies.indexOf(self.reportingService.metadata.cumulants.sum.id);
        		if (index > -1) {
        			self.report.dataSet.factPolicies.splice(index, 1);
        		}
        	}
        };

		/*
		 * 
		 */
        ReportDefinitionController.prototype.loadCustomTheme = function() {
        	var self = this;

			jQuery.ajax({
				type : 'GET',
				url : this.getContextName() + "/services/rest/common/html5/api/themes/current/custom",
				async : true
			}).done(function(json){
				var head = document.getElementsByTagName('head')[0];
				
				for(var i in json.stylesheets) {
					var link = document.createElement('link');
					link.href = self.getContextName() + "/" + json.stylesheets[i];
					link.rel = 'stylesheet';
					link.type = 'text/css';
					head.appendChild(link);
				}
			}).fail(function(err){
				console.debug("Failed in loading custom theme");
			});
		};

		/**
		 *
		 */
		ReportDefinitionController.prototype.getContextName = function() {
			return location.pathname.substring(0, location.pathname.indexOf('/plugins', 1));
		}
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
		
		function StripAggregationFromColumnId (columnId) {
		   var index = columnId.indexOf("#");
         if(index != -1) { 
           columnId = columnId.substr( 0, index );
         }
         return columnId;
      }
      
      function getAggregationAndColumnId (columnId) {
         if(columnId.indexOf("#") != -1) { 
            columnId = columnId.split("#");
         }
         return columnId;
      }

      /**
       * This method provides defualt start time stamp filter for process and activity instances query
       */
      function getDefaultFilterFor(dimension) {
	  		//prepare default start date
	  		var curDt = new Date();
	  		var strDt = curDt.getFullYear() + "/" + (curDt.getMonth() + 1) + "/" + curDt.getDate() + " 12:00:00:000";
	  		
			var filters = new Array();
			filters.push({
				"value" : {
					"from" : strDt,
					"duration" : -1,
					"durationUnit" : "M"
				},
				"dimension" : dimension,
				"metadata" : {
					"fromTo" : false
				},
				"operator" : "E"
			} );	
			return filters;
      }
      
      
		});