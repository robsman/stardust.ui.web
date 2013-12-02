/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * 
 */

define([ "js/Utils", "js/WorkflowService" ], function(Utils, WorkflowService) {
	return {
		create : function(deck) {
			var page = new ReportsPage();

			page.initialize(deck);

			return page;
		}
	};

	function ReportsPage() {
		this.id = "reportsPage";

		/**
		 * 
		 */
		ReportsPage.prototype.initialize = function(deck) {
			this.deck = deck;
		};
		
		/**
		 * 
		 */
		ReportsPage.prototype.show = function() {
			var deferred = jQuery.Deferred();
			var self = this;

			WorkflowService.instance().getReportDefinitions().done(
					function(reportFolders) {
						self.reportFolders = reportFolders;

						console.log("Report Folders");
						console.log(self.reportFolders);

						deferred.resolve();
					}).fail(function() {
				deferred.reject();
			});

			return deferred.promise();
		};

		/**
		 * 
		 */
		ReportsPage.prototype.openReportPage = function(report) {
			this.deck.reportPage.report = report; 
			this.deck.pushPage(this.deck.reportPage);
		};

		/**
		 * 
		 */
		ReportsPage.prototype.back = function() {
			this.deck.popPage();
		};
	}
});
