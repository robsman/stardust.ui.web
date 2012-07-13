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
function WorkflowService() {
	this.user = null;

	/**
	 * 
	 */
	WorkflowService.prototype.login = function(account, password,
			successCallback, errorCallback) {
		postData("/login", {
			"account" : account,
			"password" : password
		}, successCallback, errorCallback);
	};

	/**
	 * 
	 */
	WorkflowService.prototype.logout = function(successCallback, errorCallback) {
		successCallback();
	};

	/**
	 * 
	 */
	WorkflowService.prototype.getStartableProcesses = function(successCallback,
			errorCallback) {
		getData("/workflow/getStartableProcesses", successCallback,
				errorCallback);
	};

	/**
	 * 
	 */
	WorkflowService.prototype.startProcess = function(processId,
			successCallback, errorCallback) {
		postData("/workflow/startProcess", {
			"processId" : processId
		}, successCallback, errorCallback);
	};

	/**
	 * 
	 */
	WorkflowService.prototype.getWorklist = function(successCallback,
			errorCallback) {
		getData("/workflow/getWorklist", successCallback, errorCallback);
	};

	/**
	 * 
	 */
	WorkflowService.prototype.activateActivity = function(activityInstance,
			successCallback, errorCallback) {
		postData("/workflow/activateActivity", activityInstance, successCallback, errorCallback);
	};

	/**
	 * 
	 */
	WorkflowService.prototype.completeActivity = function(activityInstance,
			successCallback, errorCallback) {
		successCallback();
	};

	/**
	 * 
	 */
	WorkflowService.prototype.suspendActivity = function(activityInstance,
			successCallback, errorCallback) {
		successCallback();
	};
}