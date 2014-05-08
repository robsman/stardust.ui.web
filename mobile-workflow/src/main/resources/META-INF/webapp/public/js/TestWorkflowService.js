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
function TestWorkflowService() {
	this.user = null;

	/**
	 * 
	 */
	TestWorkflowService.prototype.login = function(account, password) {
		var deferred = jQuery.Deferred();

		if (account == "ep" && password == "ep") {
			deferred.resolve({
				"firstName" : "Ellie",
				"lastName" : "Propelli"
			});
		} else {
			deferred.reject();
		}

		return deferred.promise();
	};

	/**
	 * 
	 */
	TestWorkflowService.prototype.logout = function() {
		var deferred = jQuery.Deferred();

		deferred.resolve();

		return deferred.promise();
	};

	/**
	 * 
	 */
	TestWorkflowService.prototype.getStartableProcesses = function() {
		var deferred = jQuery.Deferred();

		deferred.resolve([ {
			"id" : "CustomerOnboarding",
			"name" : "Customer Onboarding",
			"description" : "Initiates onboarding for a new customer."
		}, {
			"id" : "ContractChange",
			"name" : "Contract Change",
			"description" : "Initiates changes of a customer contract."
		} ]);

		return deferred.promise();
	};

	/**
	 * 
	 */
	TestWorkflowService.prototype.startProcess = function(process) {
		var deferred = jQuery.Deferred();

		if (process.id == "CustomerOnboarding") {
			deferred.resolve({
				"oid" : 4,
				"activityName" : "Customer Data Entry",
				"activityId" : "CustomerDataEntry",
				"processName" : "Customer Onboarding",
				"processId" : "CustomerOnboarding",
				"startTime" : "01.04.2012 13:09:35",
				"lastModificationTime" : "14.12.2012 17:00:24"
			});
		} else {
			deferred.resolve(null);
		}

		return deferred.promise();
	};

	TestWorkflowService.prototype.getWorklist = function() {
		var deferred = jQuery.Deferred();

		deferred.resolve([ {
			"oid" : 1,
			"activityName" : "Review Client Request",
			"activityId" : "ReviewClientRequest",
			"processName" : "Customer Onboarding",
			"processId" : "CustomerOnboarding",
			"startTime" : "12.12.2012 02:09:35",
			"lastModificationTime" : "14.12.2012 17:00:24"
		}, {
			"oid" : 2,
			"activityName" : "Approve Payment",
			"activityId" : "ApprovePayment",
			"processName" : "Reimbursement",
			"processId" : "Reimbursement",
			"startTime" : "01.04.2012 13:09:35",
			"lastModificationTime" : "14.12.2012 17:00:24"
		}, {
			"oid" : 3,
			"activityName" : "Review Client Request",
			"activityId" : "ReviewClientRequest",
			"processName" : "Customer Onboarding",
			"processId" : "CustomerOnboarding",
			"startTime" : "12.12.2012 02:09:35",
			"lastModificationTime" : "14.12.2012 17:00:24"
		}, {
			"oid" : 4,
			"activityName" : "Customer Data Entry",
			"activityId" : "CustomerDataEntry",
			"processName" : "Customer Onboarding",
			"processId" : "CustomerOnboarding",
			"startTime" : "01.04.2012 13:09:35",
			"lastModificationTime" : "14.12.2012 17:00:24"
		} ]);

		return deferred.promise();
	};

	/**
	 * 
	 */
	TestWorkflowService.prototype.activateActivity = function(activityInstance) {
		var deferred = jQuery.Deferred();

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

		deferred.resolve(activityInstance);

		return deferred.promise();
	};

	/**
	 * 
	 */
	TestWorkflowService.prototype.completeActivity = function(activityInstance) {
		var deferred = jQuery.Deferred();

		if (activityInstance.activityId == "ReviewClientRequest") {
			deferred.resolve(null);
		} else {
			deferred.resolve({
				"oid" : 5,
				"activityName" : "Review Client Request",
				"activityId" : "ReviewClientRequest",
				"processName" : "Customer Onboarding",
				"processId" : "CustomerOnboarding",
				"startTime" : "12.12.2012 02:09:35",
				"lastModificationTime" : "14.12.2012 17:00:24"
			});
		}

		return deferred.promise();
	};

	/**
	 * 
	 */
	TestWorkflowService.prototype.suspendActivity = function(activityInstance) {
		var deferred = jQuery.Deferred();

		deferred.resolve();

		return deferred.promise();
	};
}
