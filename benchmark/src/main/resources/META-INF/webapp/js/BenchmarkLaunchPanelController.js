/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

define(
		[ "benchmark/js/BenchmarkService" ],
		function(BenchmarkService) {
			return {
				create : function() {
					var controller = new BenchmarkLaunchPanelController();

					return controller;
				}
			};

			/**
			 * 
			 */
			function BenchmarkLaunchPanelController() {
				/**
				 * 
				 */
				BenchmarkLaunchPanelController.prototype.initialize = function() {
					return this;
				};

				/**
				 * 
				 */
				BenchmarkLaunchPanelController.prototype.openBenchmarkView = function() {
					this.openView("benchmarkView", "benchmarkName=Criticality",
							window.btoa("benchmarkName=Criticality"));
				};

				/**
				 * 
				 */
				BenchmarkLaunchPanelController.prototype.openTrafficLightView = function() {
					this.openView("trafficLightView", "viewId=trafficLightView", window.btoa("trafficLightView"));
				};

				/**
				 * 
				 */
				BenchmarkLaunchPanelController.prototype.openTrafficLightViewPreconfigured = function() {
					this.openView("trafficLightView", "viewId=trafficLightView&businessDate=TODAY&benchmark=Criticality&drilldown=BUSINESS_OBJECT&businessObject=Fund", window.btoa("viewId=trafficLightView&businessDate=TODAY&benchmark=Criticality&drilldown=BUSINESS_OBJECT&businessObject=Fund"));
				};

				/**
				 * 
				 */
				BenchmarkLaunchPanelController.prototype.openGanttChartView = function() {
					this.openView("ganttChartView", "viewId=ganttChartView&oid=596", window.btoa("viewId=ganttChartView&oid=596"));
				};

				/**
				 * TODO - re-use a Util from web-modeler
				 */
				BenchmarkLaunchPanelController.prototype.openView = function(
						viewId, viewParams, viewIdentity) {
					var portalWinDoc = this.getOutlineWindowAndDocument();
					var link = jQuery("a[id $= 'view_management_link']",
							portalWinDoc.doc);
					var linkId = link.attr('id');
					var form = link.parents('form:first');
					var formId = form.attr('id');

					link = portalWinDoc.doc.getElementById(linkId);

					var linkForm = portalWinDoc.win.formOf(link);

					linkForm[formId + ':_idcl'].value = linkId;
					linkForm['viewParams'].value = viewParams;
					linkForm['viewId'].value = viewId;
					linkForm['viewIdentity'].value = viewIdentity;

					portalWinDoc.win.iceSubmit(linkForm, link);
				};

				/**
				 * TODO - re-use a Util from web-modeler
				 */
				BenchmarkLaunchPanelController.prototype.updateView = function(
						viewId, viewParams, viewIdentity) {
					var portalWinDoc = this.getOutlineWindowAndDocument();
					var link = jQuery("a[id $= 'view_updater_link']",
							portalWinDoc.doc);
					var linkId = link.attr('id');
					var form = link.parents('form:first');
					var formId = form.attr('id');

					link = portalWinDoc.doc.getElementById(linkId);

					var linkForm = portalWinDoc.win.formOf(link);

					linkForm[formId + ':_idcl'].value = linkId;
					linkForm['viewParams'].value = viewParams;
					linkForm['viewId'].value = viewId;
					linkForm['viewIdentity'].value = viewIdentity;

					portalWinDoc.win.iceSubmit(linkForm, link);
				};

				/*
				 * 
				 */
				BenchmarkLaunchPanelController.prototype.getOutlineWindowAndDocument = function() {
					return {
						win : parent.document
								.getElementById("portalLaunchPanels").contentWindow,
						doc : parent.document
								.getElementById("portalLaunchPanels").contentDocument
					};
				};

				/**
				 * 
				 */
				BenchmarkLaunchPanelController.prototype.safeApply = function(
						fn) {
					var phase = this.$root.$$phase;

					if (phase == '$apply' || phase == '$digest') {
						if (fn && (typeof (fn) === 'function')) {
							fn();
						}
					} else {
						this.$apply(fn);
					}
				};
			}
		});
