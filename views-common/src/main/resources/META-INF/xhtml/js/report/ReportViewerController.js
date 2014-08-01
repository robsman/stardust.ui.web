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
define(
		[ "bpm-reporting/public/js/report/I18NUtils",
				"bpm-reporting/public/js/report/AngularAdapter",
				"bpm-reporting/public/js/report/ReportingService",
				"bpm-reporting/public/js/report/ReportRenderingController",
				"bpm-reporting/js/ReportFilterController",
				"bpm-reporting/js/ReportHelper" ],
		function(I18NUtils, AngularAdapter, ReportingService,
				ReportRenderingController, ReportFilterController, ReportHelper) {
			var angularCompile;
			return {
				create : function(angular, name, path, documentId, viewMode, options) {
					var controller = new ReportViewerController();
					controller.documentId =  documentId;
					
					var angularAdapter = new bpm.portal.AngularAdapter(null);

					// initialize controller and services
					var angularModule = angularAdapter
							.initializeModule(angular);
					controller.initializeAutocompleteDir(angularModule);
					controller.initializeFilterDir(angularModule);

					// bootstrap module
					angularAdapter.initialize(angular);

					controller = angularAdapter
							.mergeControllerWithScope(controller);

					angularCompile = angularAdapter.getCompiler();

					var renderingController = ReportRenderingController
							.create(angularCompile);

					controller.initialize(renderingController, name, path,
							viewMode);

					return controller;
				}
			};

			/**
			 * 
			 */
			function ReportViewerController() {
				this.reportingService = ReportingService.instance();

				/**
				 * 
				 */
				ReportViewerController.prototype.getI18N = function(key) {
					return I18NUtils.getProperty(key);
				};

				/**
				 * 
				 */
				ReportViewerController.prototype.initialize = function(
						renderingController, name, path, viewMode) {

					this.path = path;
					this.viewMode = viewMode;
					
					//set toolbar - report Definition Viewer
					this.showFavoriteBtn = true;
					this.showSaveInstanceBtn = true;
					this.showPopoutBtn = true;
					
					if (this.isInstance()) {
						this.instance = true;
						this.showRerunBtn = true;
						this.showSaveInstanceBtn = false;
					} else if(this.isReRun()) {
						this.instance = false;
						this.showFavoriteBtn = false;
					}

					var self = this;
					this.renderingController = renderingController;
					this.reportHelper = ReportHelper
							.create(this.reportingService);

					// fetch report definition
					jQuery.when(self.reportingService.refreshPreferenceData(),
							self.reportingService.refreshModelData(),
							self.loadOrCreateReportDefinition(name, path))
							.done(
									function() {
										// fetch and render report-data
										self.renderingController.refreshPreview(self, self.report, self.parameters).done(function(){
											self.updateView();
										});
									});

					jQuery("#reportDefinitionView")
							.css("visibility", "visible");
				};

				/**
				 * 
				 */
				ReportViewerController.prototype.loadOrCreateReportDefinition = function(
						name, path) {
					var deferred = jQuery.Deferred();
					var self = this;
					if (path) {
						this.reportingService
								.retrieveReportDefinition(path)
								.done(
										function(report) {
											if (self.isInstance()) {
												self.report = report.definition;
												self.renderingController.setReportData(report.data);
											} else if(self.isReRun()){
												self.report = report.definition;
											} else{
												self.report = report;
											}

											self.initFilters();

											console
													.log("Loaded report definition:");
											console.log(self.report);
											deferred.resolve();
										}).fail(function() {
									deferred.reject();
								});
					} else {
						deferred.reject();
					}

					return deferred.promise();
				};

				/**
				 * TODO: following is duplicate as in
				 * ReportDefinitionController.js
				 * 
				 * @param angularModule
				 */
				ReportViewerController.prototype.initializeFilterDir = function(
						angularModule) {
					var self = this;
					angularModule
							.directive(
									'reportFilter',
									function() {
										return {
											restrict : 'E',
											scope : {
												reportFilterController : '=controller'
											},
											templateUrl : 'bpm-reporting/views/templates/reportFilters.html'
										};
									});
				};
				
				/**
				 * 
				 */
				ReportViewerController.prototype.reRun = function() {
					// open new instance and get data there

					var msg = {};
					msg.type = "OpenView";
					msg.data = {};
					msg.data.viewId = "documentView";
					msg.data.viewKey = "documentOID=" + this.documentId
							+ "_instance";
					msg.data.viewKey = window.btoa(msg.data.viewKey);

					msg.data.params = {};
					msg.data.params.documentId = this.documentId;
					msg.data.params.viewMode = "reRun";
					parent.postMessage(JSON.stringify(msg), "*");
				};

				 /**
					 * 
					 */
				ReportViewerController.prototype.saveReportInstance = function() {
					this.report.storage.state = "created" //TODO: need to find some other way
					this.renderingController.saveReportInstance(this.report, this.parameters);
				};
				
	    		/**
	             * 
	             */
				ReportViewerController.prototype.addToFavorites = function() {
	            	var self = this;
	            	this.reportingService.addToFavorites(this.report.name, this.documentId).done(function(){
	            		self.updateView();	
	            	});
	    		};
	    		
	    		/**
	    		 * 
	    		 */
	    		ReportViewerController.prototype.removeFromFavorites = function() {
	    			var self = this;
	            	this.reportingService.removeFromFavorites(this.documentId).done(function(){
	            		self.updateView();
	            	});
	    		};
	    		
	    		/**
	    		 * 
	    		 */
	    		ReportViewerController.prototype.isFavorite = function() {
					if (this.documentId) {
						return this.reportingService.isFavoriteReport(this.documentId);
					}
					return false;
				};
				
				
				/**
				 * 
				 */
				ReportViewerController.prototype.reloadTable = function() {
					var self = this;
					this.renderingController.refreshPreview(this, this.report, this.parameters).done(function(){
						self.updateView();
					});
				};

				/**
				 * 
				 * @returns {Boolean}
				 */
				ReportViewerController.prototype.hasParameters = function() {
					if (this.parameters && this.parameters.length > 0) {
						return true;
					}

					return false;
				};

				/**
				 * 
				 */
				ReportViewerController.prototype.initFilters = function() {
					var self = this;

					this.parameters = this.reportHelper.prepareParameters(this.report.dataSet.filters);
					self.reportParameterController = ReportFilterController.create(self.report, self.parameters,
														self.reportingService,
														self.reportHelper, true);
					
					self.reportParameterController.baseUrl = "bpm-reporting";
					
					var qualifiedParameters = jQuery.url(window.location.search).param("qualifiedParameters");
									
					if(this.isEmbedded()){
						for (var param in this.parameters) {
							var columnName = this.parameters[param].dimension;
							if(!qualifiedParameters && columnName.lastIndexOf("}") != -1){
								  columnName = columnName.substring(columnName.lastIndexOf("}") + 1, columnName.length);
							}
							var value = jQuery.url(window.location.search).param(columnName);
							
							if(value){
								this.parameters[param].value = value;
								
								//TODO: temporary code since parameters are not working as on 17th July
								/*for (var filterInd in this.report.dataSet.filters) {
									if(this.parameters[param].dimension == this.report.dataSet.filters[filterInd].dimension){
										this.report.dataSet.filters[filterInd].value = value;
									}
								}*/
							}
						}
					}
				};

				/**
				 * TODO: following is duplicate as in
				 * ReportDefinitionController.js
				 * 
				 * @param angularModule
				 */
				ReportViewerController.prototype.initializeAutocompleteDir = function(
						angularModule) {
					var self = this;

					angularModule
							.controller(
									"AutocompleteCntr",
									function($scope) {

										$scope.data = [];
										$scope.dataSelected = [];
										$scope.matchVal = "";

										/* Retrieve data from our service */
										$scope.getMatches = function(
												serviceName, serachVal) {
											clearTimeout($scope.typingTimer);

											$scope.data = [];

											$scope.typingTimer = setTimeout(
													function() {
														self.reportingService
																.search(
																		serviceName,
																		serachVal)
																.done(
																		function(
																				data) {
																			$scope.data = JSON
																					.parse(data);
																			$scope
																					.$apply();
																		})
																.fail(
																		function() {
																			console
																					.debug("Error occurred while fetching user from server");
																		});
													}, 500);
										};
									});
				};

				/**
				 * 
				 */
				ReportViewerController.prototype.isInstance = function() {
					if (this.viewMode == "instance") {
						return true;
					}
					return false;
				};

				/**
				 * 
				 */
				ReportViewerController.prototype.isEmbedded = function() {
					if (this.viewMode == "embedded") {
						return true;
					}
					return false;
				};
				/**
				 * 
				 */
				ReportViewerController.prototype.isReRun = function() {
					if (this.viewMode == "reRun") {
						return true;
					}
					return false;
				};

				/**
				 * 
				 */
				ReportViewerController.prototype.isReportDefinition = function() {
					if (!isInstance() && !isReRun()) {
						return true;
					}
					return false;
				};
			};

		});
