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

bpm.mobile_workflow.StartableProcessesPage = function StartableProcessesPage() {
	this.id = "startableProcessesPage";

	/**
	 * 
	 */
	StartableProcessesPage.prototype.initialize = function() {
		var deferred = jQuery.Deferred();
		var self = this;

		getWorkflowService().getStartableProcesses().done(
				function(startableProcesses) {
					$("#notificationDialog").popup();

					self.startableProcesses = startableProcesses;

					//jQuery("#startableProcessList").appendTo(".ui-page").trigger("refresh");
					
					deferred.resolve();
				}).fail(function() {
			deferred.reject();
		});

		return deferred.promise();
	};

	/**
	 * 
	 */
	StartableProcessesPage.prototype.startProcess = function(process) {
		var self = this;

		getWorkflowService()
				.startProcess(process)
				.done(
						function(activityInstance) {
							if (activityInstance != null) {
								getDeck().pushPage(
										new ActivityInstancePage(
												activityInstance));
							} else {
								self
										.openNotificationDialog("Process has been started but no activity is assigned to you.");
							}
						});
	};

	/**
	 * 
	 */
	StartableProcessesPage.prototype.back = function(process) {
		getDeck().popPage();
	};

	/**
	 * 
	 */
	StartableProcessesPage.prototype.openNotificationDialog = function(message) {
		$("#notificationDialog #message").empty();
		$("#notificationDialog #message").append(message);
		$("#notificationDialog").popup("open");
	};

	/**
	 * 
	 */
	StartableProcessesPage.prototype.closeNotificationDialog = function() {
		$("#notificationDialog").popup("close");
	};
};
