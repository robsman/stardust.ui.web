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
				"bpm-reporting/public/js/report/ReportRenderingController" ],
		function(I18NUtils, AngularAdapter, ReportingService,
				ReportRenderingController) {
			var angularCompile;
			return {
				create : function(angular, name, path, options) {
					var controller = new ReportViewerController();

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

					controller.initialize(renderingController, name, path);

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
						renderingController, name, path) {
					var self = this;
					this.renderingController = renderingController;

					// fetch report definition
					this.loadOrCreateReportDefinition(name, path);

					jQuery.when(this.loadOrCreateReportDefinition(name, path))
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
						deferred.reject();
					}

					return deferred.promise();
				};

			}
		});
