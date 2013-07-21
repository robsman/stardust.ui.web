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

if (!window.bpm) {
	bpm = {};
}

if (!window.bpm.mobile_workflow) {
	bpm.mobile_workflow = {};
}

bpm.mobile_workflow.ReportsPage = function ReportsPage() {
	this.id = "reportsPage";

	/**
	 * 
	 */
	ReportsPage.prototype.initialize = function() {
		var deferred = jQuery.Deferred();
		var self = this;
		
		getWorkflowService()
				.getReportDefinitions()
				.done(
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
		getDeck().pushPage(new bpm.mobile_workflow.ReportPage(report));
	};

	/**
	 * 
	 */
	ReportsPage.prototype.back = function() {
		getDeck().popPage();				
	};
};
