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
				create : function(angular, name, path, viewMode, options) {
					var controller = new ReportViewerController();

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

					if (viewMode == "instance") {
						this.instance = true;
					} else {
						this.instance = false;
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
										self.renderingController
												.refreshPreview(self.report,
														self);
										self.updateView();
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
											if (self.instance) {
												self.report = report.report_definition;
												self.reportingService
														.setReportData(report.report_data);
											} else {
												self.report = report;
											}

											self.parameters = angular
													.copy(self.report.dataSet.filters);
											self.reportParameterController = ReportFilterController
													.create(
															self.report,
															self.report.dataSet.filters,
															self.reportingService,
															self.reportHelper,
															false);
											self.reportParameterController.baseUrl = "bpm-reporting";
											self.reportParameterController
													.loadFilters();

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
			}

		});
