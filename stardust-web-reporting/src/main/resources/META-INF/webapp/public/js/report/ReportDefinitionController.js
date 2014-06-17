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

					controller.initialize(renderingController);

					controller.report = report_definition;
					
					controller.reloadTable();
					
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
				ReportDefinitionController.prototype.initialize = function(renderingController) {
					var self = this;
					this.renderingController = renderingController;
					jQuery("#reportDefinitionView").css("visibility", "visible");
				};
				
				/**
				 * 
				 */
				ReportDefinitionController.prototype.reloadTable = function() {
					var self = this;
					this.renderingController.refreshPreview(this, this.report).done(function(){
						self.updateView();	
					});	
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
