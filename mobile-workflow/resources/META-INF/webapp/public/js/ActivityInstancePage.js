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

bpm.mobile_workflow.ActivityInstancePage = function ActivityInstancePage(
		activityInstance) {
	this.id = "activityInstancePage";
	this.activityInstance = activityInstance;

	/**
	 * 
	 */
	ActivityInstancePage.prototype.initialize = function() {
		var deferred = jQuery.Deferred();

		$("#" + this.id + " #titleHeader").empty();
		$("#" + this.id + " #titleHeader").append(
				this.activityInstance.activityName + " ("
						+ this.activityInstance.oid + ")");
		$("#suspendConfirmationDialog").popup();

		var self = this;

		getWorkflowService()
				.activateActivity(this.activityInstance)
				.done(
						function(activityInstance) {
							self.activityInstance = activityInstance;

							console.log("Activity Instance");
							console.log(self.activityInstance);

							self.externalWebAppUrl = activityInstance.activity.contexts.externalWebApp['carnot:engine:ui:externalWebApp:uri']
									+ "?ippDevice=mobile&ippInteractionUri="
									+ activityInstance.activity.contexts.externalWebApp.interactionId;

							// activityInstance.html = "<form\">"
							// + "<label for=\"firstNameInput\">First
							// Name</label>"
							// + "<input type=\"text\" name=\"firstNameInput\"
							// id=\"firstNameInput\" value=\"\"/>"
							// + "<label for=\"lastNameInput\">Last
							// Name</label>"
							// + "<input type=\"text\" name=\"lastNameInput\"
							// id=\"lastNameInput\" value=\"\"/>"
							// + "<label for=\"dateOfBirthInput\">Date of
							// Birt</label>"
							// + "<input type=\"date\" name=\"dateOfBirthInput\"
							// id=\"dateOfBirthInput\" value=\"\"/>"
							// + "<label for=\"countrySelect\"
							// class=\"select\">Country</label> "
							// + "<select name=\"countrySelect\"
							// id=\"countrySelect\">"
							// + "<option value=\"USA\">USA</option>"
							// + "<option value=\"GER\">Germany</option>"
							// + "<option value=\"SA\">South Africa</option>"
							// + "</select>"
							// + "<input type=\"checkbox\"
							// name=\"approvedCheckbox\"
							// id=\"approvedCheckbox\" class=\"custom\" />"
							// + "<label
							// for=\"approvedCheckbox\">Approved</label>"
							// + "<label for=\"textarea\">Description</label>"
							// + "<textarea cols=\"40\" rows=\"8\"
							// name=\"description\"
							// id=\"description\"></textarea>"
							// + "</form>";
							// $("#activityForm").empty();
							// $("#activityForm").append(activityInstance.html);
							deferred.resolve();
						}).fail(function() {
					deferred.reject();
				});

		// $("#activityForm textarea").textinput();
		// $("#activityForm input:text").textinput();
		// $("#activityForm input:checkbox").checkboxradio();
		// $("#activityForm select").selectmenu();

		return deferred.promise();
	};

	/**
	 * 
	 */
	ActivityInstancePage.prototype.complete = function() {
		console.log("ActivityInstancePage.prototype.complete");

		getWorkflowService().completeActivity(this.activityInstance).done(
				function(activityInstance) {
					getDeck().popPage();

					if (activityInstance != null) {
						getDeck().pushPage(
								new ActivityInstancePage(activityInstance));
						console.log("New Activity Instance set");
					} else {
						console.log("Processing completed");
					}
				});
	};

	/**
	 * 
	 */
	ActivityInstancePage.prototype.openSuspendDialog = function() {
		$("#suspendConfirmationDialog").popup("open");
	};

	/**
	 * 
	 */
	ActivityInstancePage.prototype.suspendActivityInstance = function() {
		getWorkflowService().suspendActivity(this.activityInstance).done(
				function() {
					getDeck().popPage();
				});
	};

	/**
	 * 
	 */
	ActivityInstancePage.prototype.closeSuspendDialog = function() {
		$("#suspendConfirmationDialog").popup("close");
	};

	/**
	 * 
	 */
	ActivityInstancePage.prototype.openNotesPage = function() {
		getDeck().pushPage(new bpm.mobile_workflow.NotesPage());
	};

	/**
	 * 
	 */
	ActivityInstancePage.prototype.openProcessPage = function() {
		getDeck().pushPage(
				new bpm.mobile_workflow.ProcessPage(
						this.activityInstance.processInstanceOid));
	};
};
