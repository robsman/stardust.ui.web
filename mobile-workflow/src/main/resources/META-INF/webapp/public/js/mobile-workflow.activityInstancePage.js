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
function ActivityInstancePage(activityInstance) {
	this.id = "activityInstancePage";
	this.activityInstance = activityInstance;

	/**
	 * 
	 */
	ActivityInstancePage.prototype.initialize = function() {
		$("#activityInstancePage #completeLink").click(function() {
			getDeck().getTopPage().complete();
		});
		$("#activityInstancePage #suspendLink").click(function() {
			getDeck().getTopPage().suspend();
		});
		$("#activityInstancePage #notesLink").click(function() {
			getDeck().pushPage(new NotesPage());
		});

		$("#activityInstancePage #delegateLink").click(function() {
		});

		$("#activityInstancePage #processLink").click(function() {
			getDeck().pushPage(new ProcessPage());
		});

		$("#" + this.id + " #titleHeader").empty();
		$("#" + this.id + " #titleHeader").append(
				this.activityInstance.activityName + " ("
						+ this.activityInstance.oid + ")");
		$("#suspendConfirmationDialog").popup();

		getWorkflowService()
				.activateActivity(
						this.activityInstance,
						function(activityInstance) {
							activityInstance.html = "<div data-role=\"fieldcontain\">"
									+ "<label for=\"firstNameInput\">First Name</label>"
									+ "<input type=\"text\" name=\"firstNameInput\" id=\"firstNameInput\" value=\"\" data-mini=\"true\" />"
									+ "</div>"
									+ "<div data-role=\"fieldcontain\">"
									+ "<label for=\"lastNameInput\">Last Name</label>"
									+ "<input type=\"text\" name=\"lastNameInput\" id=\"lastNameInput\" value=\"\" data-mini=\"true\" />"
									+ "</div>"
									+ "<div data-role=\"fieldcontain\">"
									+ "<label for=\"countrySelect\" class=\"select\">Country</label> "
									+ "<select name=\"countrySelect\" id=\"countrySelect\" data-mini=\"true\">"
									+ "<option value=\"USA\">USA</option>"
									+ "<option value=\"GER\">Germany</option>"
									+ "<option value=\"SA\">South Africa</option>"
									+ "</select>"
									+ "</div>"
									+ "<div data-role=\"fieldcontain\">"
									+ "<input type=\"checkbox\" name=\"approvedCheckbox\" id=\"approvedCheckbox\" class=\"custom\" data-mini=\"true\" />"
									+ "<label for=\"approvedCheckbox\">Approved</label>"
									+ "</div>"
									+ "<div data-role=\"fieldcontain\">"
									+ "<label for=\"textarea\">Description</label>"
									+ "<textarea cols=\"40\" rows=\"8\" name=\"description\" id=\"description\" data-mini=\"true\"></textarea>"
									+ "</div>";
							$("#activityForm").empty();
							$("#activityForm").append(activityInstance.html);
						}, function() {
						});

		$("#activityForm textarea").textinput();
		$("#activityForm input:text").textinput();
		$("#activityForm input:checkbox").checkboxradio();
		$("#activityForm select").selectmenu();
	};

	/**
	 * 
	 */
	ActivityInstancePage.prototype.complete = function() {
		debug("ActivityInstancePage.prototype.complete");

		getWorkflowService().completeActivity(
				this.activityInstance,
				function(activityInstance) {
					getDeck().popPage();

					if (activityInstance != null) {
						getDeck().pushPage(
								new ActivityInstancePage(activityInstance));
						debug("New Activity Instance set");
					} else {
						debug("Processing completed");
					}
				});
	};

	/**
	 * 
	 */
	ActivityInstancePage.prototype.suspend = function() {
		getWorkflowService().suspendActivity(this.activityInstance, function() {

			$("#suspendConfirmationDialog").popup("open");

			// getDeck().popPage();
		});
	};
}
