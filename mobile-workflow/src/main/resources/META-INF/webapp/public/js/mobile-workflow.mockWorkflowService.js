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
function MockWorkflowService() {
	this.user = null;

	/**
	 * 
	 */
	MockWorkflowService.prototype.login = function(account, password,
			successCallback, errorCallback) {
		if (account == "ep" && password == "ep") {
			this.user = {
				"firstName" : "Ellie",
				"lastName" : "Propelli"
			};

			successCallback(this.user);
		} else {
			errorCallback();
		}
	};

	/**
	 * 
	 */
	MockWorkflowService.prototype.logout = function(successCallback,
			errorCallback) {
		successCallback();
	};

	MockWorkflowService.prototype.getStartableProcesses = function(
			successCallback, errorCallback) {
		successCallback([ {
			"id" : "CustomerOnboarding",
			"name" : "Customer Onboarding",
			"description" : "Initiates onboarding for a new customer."
		}, {
			"id" : "ContractChange",
			"name" : "Contract Change",
			"description" : "Initiates changes of a customer contract."
		} ]);
	};

	/**
	 * 
	 */
	MockWorkflowService.prototype.startProcess = function(process,
			successCallback, errorCallback) {
		if (process.id == "CustomerOnboarding") {
			successCallback({
				"oid" : 4,
				"activityName" : "Customer Data Entry",
				"activityId" : "CustomerDataEntry",
				"processName" : "Customer Onboarding",
				"processId" : "CustomerOnboarding",
				"startTime" : "01.04.2012 13:09:35",
				"lastModificationTime" : "14.12.2012 17:00:24"
			});
		} else {
			successCallback(null);
		}
	};

	MockWorkflowService.prototype.getWorklist = function(successCallback,
			errorCallback) {
		successCallback([ {
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
	};

	/**
	 * 
	 */
	MockWorkflowService.prototype.activateActivity = function(activityInstance,
			successCallback, errorCallback) {
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

		successCallback(activityInstance);
	};

	/**
	 * 
	 */
	MockWorkflowService.prototype.completeActivity = function(activityInstance,
			successCallback, errorCallback) {
		if (activityInstance.activityId == "ReviewClientRequest") {
			successCallback(null);
		} else {
			successCallback({
				"oid" : 5,
				"activityName" : "Review Client Request",
				"activityId" : "ReviewClientRequest",
				"processName" : "Customer Onboarding",
				"processId" : "CustomerOnboarding",
				"startTime" : "12.12.2012 02:09:35",
				"lastModificationTime" : "14.12.2012 17:00:24"
			});
		}
	};

	/**
	 * 
	 */
	MockWorkflowService.prototype.suspendActivity = function(activityInstance,
			successCallback, errorCallback) {
		successCallback();
	};
}
