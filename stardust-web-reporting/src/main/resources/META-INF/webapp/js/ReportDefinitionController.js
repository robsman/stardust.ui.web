define(
		[ "js/AngularAdapter", "js/ReportingService",
				"js/ReportRenderingController", "js/SchedulingController" ],
		function(AngularAdapter, ReportingService, ReportRenderingController,
				SchedulingController) {
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
				ReportDefinitionController.prototype.initialize = function(
						renderingController, name, path) {
					this.renderingController = renderingController;
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
					this.factSelect = jQuery("#factSelect");
					this.firstDimensionSelect = jQuery("#firstDimensionSelect");
					this.secondDimensionSelect = jQuery("#secondDimensionSelect");
					this.chartTypeSelect = jQuery("#chartTypeSelect");

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

										self.firstDimensionSelect
												.change(function() {
													self.report.dataSet.firstDimension = self.firstDimensionSelect
															.val();

													self.changeFirstDimension();
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

															self.schedulingController = SchedulingController
																	.create(self.report.scheduling);

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
								location : "publicFolder"
							},
							dataSet : {
								type : "seriesGroup",
								primaryObject : "processInstance",
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
											}
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
							scheduling : {
								delivery : {
									mode : "personalFolder"
								}
							}
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

					this.populateFirstDimensionSelect();

					this.secondDimensionSelect.empty();

					this.secondDimensionSelect.append("<option value='" + -1
							+ "'>None</option>");

					for ( var i in this.getPrimaryObject().dimensions) {
						var dimension = this.getPrimaryObject().dimensions[i];

						this.secondDimensionSelect.append("<option value='" + i
								+ "'>" + dimension.name + "</option>");
					}

					jQuery("#availableDimensionsSelect").empty();

					for ( var k in this.getPrimaryObject().dimensions) {
						var dimension = this.getPrimaryObject().dimensions[k];

						jQuery("#availableDimensionsSelect").append(
								"<option value='" + k + "'>" + dimension.name
										+ "</option>");
					}

					jQuery("#selectedDimensionsSelect").empty();

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
					if (!dimension) {
						return null;
					}

					var qualifier = dimension.enumerationType.split(":");

					return this.reportingService.getEnumerators(qualifier[0],
							qualifier[1], qualifier[2]);
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

						throw "No filter found with dimension " + dimension
								+ ".";
					}
				};

				/**
				 * Reinitializes filter values and operator.
				 */
				ReportDefinitionController.prototype.onFilterDimensionChange = function(
						index) {
					this.report.dataSet.filters[index].value = null;

					// TODO: Operator only for respective types

					this.report.dataSet.filters[index].operator = "equal";
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.populateFirstDimensionSelect = function() {
					this.firstDimensionSelect.empty();

					for ( var m in this.getPrimaryObject().dimensions) {
						var dimension = this.getPrimaryObject().dimensions[m];

						this.firstDimensionSelect.append("<option value='" + m
								+ "'>" + dimension.name + "</option>");
					}
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
					jQuery("#availableDimensionsSelect").empty();
					jQuery("#selectedDimensionsSelect").empty();

					this.report.dataSet.columns = [];

					for ( var k in this.getPrimaryObject().dimensions) {
						var dimension = this.getPrimaryObject().dimensions[k];

						this.report.dataSet.columns.push(k);

						jQuery("#selectedDimensionsSelect").append(
								"<option value='" + k + "'>" + dimension.name
										+ "</option>");
					}
				};

				/**
				 * 
				 */
				ReportDefinitionController.prototype.deselectAllDimensionsForColumns = function() {
					jQuery("#availableDimensionsSelect").empty();
					jQuery("#selectedDimensionsSelect").empty();

					this.report.dataSet.columns = [];

					for ( var k in this.getPrimaryObject().dimensions) {
						var dimension = this.getPrimaryObject().dimensions[k];

						jQuery("#availableDimensionsSelect").append(
								"<option value='" + k + "'>" + dimension.name
										+ "</option>");
					}
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
									function() {
										window.top.postMessage(
												"BPM-REPORTING-REPORT-CREATED",
												self.reportingService
														.getRootUrl());
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
						id, name, type) {
					this.report.parameters[id] = {
						id : id,
						name : name,
						type : type,
						value : null
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

						return this.getFilterByDimension(path[1]).value;
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
			}
		});