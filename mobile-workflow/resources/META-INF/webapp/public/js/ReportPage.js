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
			var page = new ReportPage();

			page.initialize(deck);

			return page;
		}
	};

	function ReportPage() {
		this.id = "reportPage";

		/**
		 * 
		 */
		ReportPage.prototype.initialize = function(deck) {
			this.deck = deck;
		};
		
		/**
		 * 
		 */
		ReportPage.prototype.show = function() {
			var deferred = jQuery.Deferred();

			deferred.resolve();

			return deferred.promise();
		};

		/**
		 * 
		 */
		ReportPage.prototype.back = function() {
			this.deck.popPage();
		};

		/**
		 * 
		 */
		ReportPage.prototype.getReportUri = function(e) {
			return WorkflowService.instance().getBaseUrl()
					+ "/plugins/bpm-reporting/views/reportPanel.html?path="
					+ this.report.path;
		};
	}
});
