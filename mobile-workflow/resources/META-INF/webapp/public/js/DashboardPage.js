/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

if (!window.bpm) {
	bpm = {};
}

if (!window.bpm.mobile_workflow) {
	bpm.mobile_workflow = {};
}

bpm.mobile_workflow.DashboardPage = function DashboardPage() {
	this.id = "dashboardPage";

	/**
	 * 
	 */
	DashboardPage.prototype.initialize = function() {
		var deferred = jQuery.Deferred();

		deferred.resolve();

		return deferred.promise();		
	};

	/**
	 * 
	 */
	DashboardPage.prototype.logout = function() {
		getWorkflowService().logout().done(function() {
			getDeck().popPage();
		});
	};

	/**
	 * 
	 */
	DashboardPage.prototype.openWorklistPage = function() {
		getDeck().pushPage(new bpm.mobile_workflow.WorklistPage());
	};

	/**
	 * 
	 */
	DashboardPage.prototype.openStartableProcessesPage = function() {
		getDeck().pushPage(new bpm.mobile_workflow.StartableProcessesPage());
	};

	/**
	 * 
	 */
	DashboardPage.prototype.openReportsPage = function() {
		getDeck().pushPage(new bpm.mobile_workflow.ReportsPage());
	};

	/**
	 * 
	 */
	DashboardPage.prototype.openSearchPage = function() {
		getDeck().pushPage(new bpm.mobile_workflow.SearchPage());
	};
	
	/**
	 * 
	 */
	DashboardPage.prototype.openFolderPage = function() {
		getDeck().pushPage(new bpm.mobile_workflow.FolderPage());
	};
};
