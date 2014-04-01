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
			return {
				create : function(angular, name, path) {
					var controller = new ReportDefinitionController();
					var renderingController = ReportRenderingController
							.create();
					var angularAdapter = new bpm.portal.AngularAdapter();

					angularAdapter.initialize(angular);

					controller = angularAdapter
							.mergeControllerWithScope(controller);

					controller.initialize(renderingController, name, path);

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
					
					this.factSelect = jQuery("#factSelect");
					this.chartTypeSelect = jQuery("#chartTypeSelect");
					
					//TODO: have better way to do this??
					this.ALL_PROCESSES = {id: "allProcesses", name: "All Processes"};
					this.ALL_ACTIVITIES = {id: "allActivities", name: "All Activities"};
					
					this.editorAnchor = utils.jQuerySelect("#expressionTextDiv").get(0);
					this.expressionEditor = m_codeEditorAce.getJSCodeEditor(this.editorAnchor);
					this.expressionEditor.getEditor().on('blur', function(event)
					         {
					               if (self.selectedComputedColumn != null)
					               {
					                  self.selectedComputedColumn.formula = self.expressionEditor.getValue();
					               }
					         });
					this.expressionEditor.loadLanguageTools();
					this.expressionEditor.setSessionData("$keywordList",["test", "air", "word"]);
					
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

					this.reportingService
							.refreshModelData()
							.done(
									function() {
										self.primaryObjectSelect.empty();

										for ( var n in self.reportingService.metadata.objects) {
											self.primaryObjectSelect
													.append("<option value='"
															+ n
															+ "'>"
															+ self.reportingService.metadata.objects[n].name
															+ "</option>");
										}

										self.primaryObjectSelect
												.change(function() {
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
										for ( var n in modelParticipants) {
											self.participantsSelect
													.append("<option value='"
															+ modelParticipants[n].id
															+ "'>"
															+ modelParticipants[n].name
															+ "</option>");
										}

										self.participantsSelect.change(function() {
											self.report.storage.location = "participantFolder";
											self.report.storage.participant = self.participantsSelect.val();
											self.updateView();
										});
										
										//Participants Select
                              self.schedulingParticipantsSelect.empty();
                              var modelParticipants = self.reportingService.modelData.participants;
                              for ( var n in modelParticipants) {
                                 self.schedulingParticipantsSelect
                                       .append("<option value='"
                                             + modelParticipants[n].id
                                             + "'>"
                                             + modelParticipants[n].name
                                             + "</option>");
                              }

                              self.schedulingParticipantsSelect.change(function() {
                                 self.report.scheduling.delivery.participant = self.schedulingParticipantsSelect.val();
                                 self.updateView();
                              });
										
										self.chartTypeSelect
												.change(function() {
													self.report.layout.chart.type = self.chartTypeSelect
															.val();

													self.updateView();
												});

										self.factSelect
												.change(function() {
													self.report.dataSet.fact = self.factSelect
															.val();

													self.changeFact();
													self.updateView();
												});

										jQuery("#factProcessDataSelect")
												.change(
														function() {
															self.report.dataSet.factProcessData = jQuery(
																	this).val();

															self.updateView();
														});

										jQuery("#groupBySelect")
												.change(
														function() {
															self.report.dataSet.groupBy = jQuery(
																	this).val();
														});

										jQuery("#propertiesTabs")
												.tabs(
														{
															beforeActivate : function(
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
															activate : function(
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
														function() {
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

															self.chartTypeSelect
																	.val(self.report.layout.chart.type);

															self.schedulingController
																	.initialize(self.report.scheduling);

															self.runInAngularContext(function(scope){
															   scope.$watch("report.scheduling", function(newValue, oldValue) {
															            self.getNextExecutionDate();
	                                             }, true);
															});

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
															
														})
												.fail(
														function() {
															console
																	.debug("Failed to initialize Report Definition Controller");

															document.body.style.cursor = "default";
															jQuery(
																	"#reportDefinitionView")
																	.css(
																			"visibility",
																			"visible");
														});
									})
							.fail(
									function() {
										console
												.debug("Failed to initialize Report Definition Controller");

										document.body.style.cursor = "default";
										jQuery("#reportDefinitionView").css(
												"visibility", "visible");
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
								type : "chart",
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
						this.report.dataSet.firstDimension = this
								.getPrimaryObject().dimensions.startTimestamp.id;

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
				
				ReportDefinitionController.prototype.getPrimaryObjectEnum = function() {
					var dimensionsObj = this.reportingService.metadata.objects[this.report.dataSet.primaryObject].dimensions;
					var enumerators = [];
					for ( var n in dimensionsObj) {
						enumerators.push(dimensionsObj[n]);
					}
					return enumerators;
				};
				
				ReportDefinitionController.prototype.getPrimaryObjectEnum = function() {
					var dimensionsObj = this.reportingService.metadata.objects[this.report.dataSet.primaryObject].dimensions;
					var enumerators = [];
					for ( var n in dimensionsObj) {
						enumerators.push(dimensionsObj[n]);
					}
					return enumerators;
				};
				
				ReportDefinitionController.prototype.toggleFilter = function(filter, property) {
					filter.metadata[property] = !filter.metadata[property]; 
					this.updateView();
				};
				
				ReportDefinitionController.prototype.selectedProcessChanged = function(filter) {
					if(containsObj(filter.metadata.selectedProcesses, this.ALL_PROCESSES, "id")){
						filter.metadata.selectedProcesses = [this.ALL_PROCESSES];
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

				/**
				 * 
				 */
				ReportDefinitionController.prototype.getFirstDimension = function() {
					return this.getPrimaryObject().dimensions[this.report.dataSet.firstDimension];
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.refreshPreview = function() {
					console.log("refreshPreview");

					var deferred = jQuery.Deferred();
					var self = this;

					this.renderingController.renderReport().done(function() {
						deferred.resolve();
					}).fail(function() {
						deferred.reject();
					});

					return deferred.promise();
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.changeDataSetType = function() {
					if (this.report.dataSet.type === "processHistory") {
						this.report.layout.type = "processDiagram";
					} else if (this.report.dataSet.type === 'seriesGroup') {
						this.report.layout.type = "chart";
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
					}

					this.factSelect.empty();

					for ( var n in this.getPrimaryObject().facts) {
						var fact = this.getPrimaryObject().facts[n];

						this.factSelect.append("<option value='" + n + "'>"
								+ fact.name + "</option>");
					}

					this.populateChartTypes();
					this.populateGroupBy();

					this.report.layout.chart.options.title = this
							.getPrimaryObject().name;
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
				
				ReportDefinitionController.prototype.getEnumerators2 = function(
						dimension, filter) {
					if (!dimension || !dimension.enumerationType) {
						return null;
					}

					var qualifier = dimension.enumerationType.split(":");

					var enumItems = this.reportingService.getEnumerators2(qualifier[0], qualifier[1]);
					
					var filteredEnumItems = enumItems;
					
					//processes
					if ((dimension.id == "processName" || dimension.id == "activityName")) {
						filteredEnumItems = [];
						filteredEnumItems.push(this.ALL_PROCESSES);
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
						if (!filter.metadata.selectedProcesses
								|| filter.metadata.selectedProcesses.length < 1) {
							selectedProcesses = selectedProcesses.concat(filteredEnumItems);
						}else if(containsObj(filter.metadata.selectedProcesses, this.ALL_PROCESSES, "id")){
							selectedProcesses = selectedProcesses.concat(filteredEnumItems);
						}else{
							selectedProcesses = filter.metadata.selectedProcesses;
						}
						
						filteredEnumItems = [];
						filteredEnumItems.push(this.ALL_ACTIVITIES);
						
						for (var i = 0; i < selectedProcesses.length; i++) {
							var process = selectedProcesses[i];
							if(process == this.ALL_PROCESSES){
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
					
					return filteredEnumItems;
				};

				
				ReportDefinitionController.prototype.selectionChanged = function(dimension, filter) {
					if(dimension.id == "processName" && containsObj(filter.value, this.ALL_PROCESSES, "id")){
						filter.value = [this.ALL_PROCESSES];
					}
					
					if(dimension.id == "activityName" && containsObj(filter.value, this.ALL_ACTIVITIES, "id")){
						filter.value = [this.ALL_ACTIVITIES];
					}
				}
				
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
					
					if(this.report.dataSet.filters[index].dimension == 'processName'){
						this.report.dataSet.filters[index].metadata = { process_filter_auxiliary : true };
						this.report.dataSet.filters[index].value = [this.ALL_PROCESSES];
					}else if(this.report.dataSet.filters[index].dimension == 'activityName'){
						this.report.dataSet.filters[index].metadata = activityFilterTemplate();
						this.report.dataSet.filters[index].metadata.selectedProcesses.push(this.ALL_PROCESSES);
						this.report.dataSet.filters[index].value = [this.ALL_ACTIVITIES];
					}

					// TODO: Operator only for respective types
					this.report.dataSet.filters[index].operator = "equal";
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
					return this.reportingService
							.getCumulatedDimensions(this.report);
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

				/**
				 * Selects the useful tables and charts for the selected facts
				 * and dimensions.
				 */
				ReportDefinitionController.prototype.populateChartTypes = function() {
					this.chartTypeSelect.empty();

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

					if (true) {
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
					this.report.dataSet.columns = [];

					var cumulatedDimensions = this.getCumulatedDimensions();

					for ( var k in cumulatedDimensions) {
						console.log(cumulatedDimensions[k]);

						this.report.dataSet.columns
								.push(cumulatedDimensions[k].id);
					}
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.deselectAllDimensionsForColumns = function() {
					this.report.dataSet.columns = [];
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
					
					this.report.parameters[id] = {
						id : id,
						name : name,
						type : type,
						value : value,
						operator : operator
					};
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
					for ( var n = 0; n < parameters.length; ++n) {
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
				ReportDefinitionController.prototype.getParameters = function() {
					return this.report.parameters;
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
					
					this.expressionEditor.setValue(this.selectedComputedColumn.formula);
					
					console.log("Selected Column");
					console.log(this.selectedComputedColumn);
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
                  //FIXME temporary hack to update the values as sd-date directive not updating model
                  this.report.scheduling.recurrenceRange.startDate = document.getElementById("startDateId").value;
                  this.report.scheduling.recurrenceRange.endDate = document.getElementById("endDateId").value;
                  this.reportingService.getNextExecutionDate(this.report.scheduling).done(
                     function(date) {
                        self.report.scheduling.nextExecutionDate = date;
                        self.report.scheduling.nextExecutionDateDay = utils.getWeekdayName(date);
                        self.updateView();
                     }).fail(function(err){
                        console.log("Failed next Execution Date: " + err);
                     });
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
                     dimensionNames[m] = dimension.name;
                  }
                  self.expressionEditor.setSessionData("$keywordList", dimensionNames);
            };

			}
			

		function containsObj(arrayObj, element, property){
			if(!arrayObj || arrayObj.length < 1){
				return false;
			}
			
			for(var i = 0;  i < arrayObj.length; i++){
				if(element[property] == arrayObj[i][property]){
					return true;
				}
			}
			return false;
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
			
		});