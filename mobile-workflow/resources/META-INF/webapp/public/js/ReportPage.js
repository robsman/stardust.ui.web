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

bpm.mobile_workflow.ReportPage = function ReportPage(report) {
	this.id = "reportPage";
	this.report = report;

	console.log("Report");
	console.log(this.report);

	/**
	 * 
	 */
	ReportPage.prototype.initialize = function() {
		var deferred = jQuery.Deferred();

		deferred.resolve();

		return deferred.promise();
	};

	/**
	 * 
	 */
	ReportPage.prototype.back = function() {
		getDeck().popPage();
	};

	/**
	 * 
	 */
	ReportPage.prototype.getReportUri = function(e) {
		return getWorkflowService().getBaseUrl()
				+ "/plugins/bpm-reporting/views/reportPanel.html?path="
				+ this.report.path;
	};
};
