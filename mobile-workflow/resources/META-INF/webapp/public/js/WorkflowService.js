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
	WorkflowService.prototype.login = function(account, password) {
		var deferred = jQuery.Deferred();
		var self = this;

		jQuery.ajax({
			type : "POST",
			url : this.getMobileWorkflowRestServicesUrl() + "/login",
			contentType : "application/json",
			data : JSON.stringify({
				account : account,
				password : password
			})
		}).done(function(user) {
			console.debug("User:");
			console.debug(user);

			deferred.resolve(user);
		}).fail(function() {
			deferred.reject();
		});

		return deferred.promise();
	};

	/**
	 * 
	 */
	WorkflowService.prototype.logout = function() {
		var deferred = jQuery.Deferred();

		deferred.resolve();

		return deferred.promise();
	};

	/**
	 * 
	 */
	WorkflowService.prototype.getStartableProcesses = function() {
		var deferred = jQuery.Deferred();

		var self = this;

		jQuery.ajax(
				{
					type : "GET",
					beforeSend : function(request) {
						request.setRequestHeader("Authentication", self
								.getBasicAuthenticationHeader());
					},
					url : this.getMobileWorkflowRestServicesUrl()
							+ "/startable-processes"
				}).done(function(data) {
			console.debug("Return Value:");
			console.debug(data);

			deferred.resolve(data.processDefinitions);
		}).fail(function() {
			deferred.reject();
		});

		return deferred.promise();
	};

	/**
	 * 
	 */
	WorkflowService.prototype.startProcess = function(processId) {
		// postData("/workflow/startProcess", {
		// "processId" : processId
		// }, successCallback, errorCallback);
		var deferred = jQuery.Deferred();

		deferred.resolve();

		return deferred.promise();
	};

	/**
	 * 
	 */
	WorkflowService.prototype.getWorklist = function() {
		var deferred = jQuery.Deferred();

		var self = this;

		jQuery.ajax(
				{
					type : "GET",
					beforeSend : function(request) {
						request.setRequestHeader("Authentication", self
								.getBasicAuthenticationHeader());
					},
					url : this.getMobileWorkflowRestServicesUrl() + "/worklist"
				}).done(function(data) {
			console.debug("Return Value:");
			console.debug(data);

			deferred.resolve(data.worklist);
		}).fail(function() {
			deferred.reject();
		});

		return deferred.promise();
	};

	/**
	 * 
	 */
	WorkflowService.prototype.activateActivity = function(activityInstance) {
		var deferred = jQuery.Deferred();
		var self = this;

		jQuery.ajax(
				{
					type : "POST",
					url : this.getMobileWorkflowRestServicesUrl()
							+ "/activity/activate",
					contentType : "application/json",
					data : JSON.stringify(activityInstance)
				}).done(function(data) {
			console.debug("Return Value:");
			console.debug(data);

			deferred.resolve(data);
		}).fail(function() {
			deferred.reject();
		});

		return deferred.promise();
	};

	/**
	 * 
	 */
	WorkflowService.prototype.completeActivity = function(activityInstance) {
		var deferred = jQuery.Deferred();

		deferred.resolve();

		return deferred.promise();
	};

	/**
	 * 
	 */
	WorkflowService.prototype.suspendActivity = function(activityInstance) {
		var deferred = jQuery.Deferred();

		deferred.resolve();

		return deferred.promise();
	};

	/**
	 * 
	 */
	WorkflowService.prototype.getProcessInstance = function(processInstanceOid) {
		var deferred = jQuery.Deferred();

		var self = this;

		jQuery.ajax(
				{
					type : "GET",
					beforeSend : function(request) {
						request.setRequestHeader("Authentication", self
								.getBasicAuthenticationHeader());
					},
					url : this.getMobileWorkflowRestServicesUrl()
							+ "/process-instances/" + processInstanceOid,
				}).done(function(processInstance) {
			console.debug("Return Value:");
			console.debug(processInstance);

			deferred.resolve(processInstance);
		}).fail(function() {
			deferred.reject();
		});

		return deferred.promise();
	};

	/**
	 * 
	 */
	WorkflowService.prototype.getNotes = function(processInstanceOid) {
		var deferred = jQuery.Deferred();
		var self = this;

		jQuery.ajax(
				{
					type : "GET",
					url : this.getMobileWorkflowRestServicesUrl()
							+ "/process-instances/" + processInstanceOid
							+ "/notes",
				}).done(function(result) {
			console.debug("Return Value:");
			console.debug(result);

			deferred.resolve(result.notes);
		}).fail(function() {
			deferred.reject();
		});

		return deferred.promise();
	};

	/**
	 * 
	 */
	WorkflowService.prototype.createNote = function(processInstanceOid, content) {
		var deferred = jQuery.Deferred();

		jQuery.ajax(
				{
					type : "POST",
					url : this.getMobileWorkflowRestServicesUrl()
							+ "/process-instances/" + processInstanceOid
							+ "/notes/create",
					contentType : "application/json",
					data : JSON.stringify({
						processInstanceOid : processInstanceOid,
						content : content
					})
				}).done(function(note) {
			console.debug("Note:");
			console.debug(note);

			deferred.resolve(note);
		}).fail(function() {
			deferred.reject();
		});

		return deferred.promise();
	};

	/**
	 * 
	 */
	WorkflowService.prototype.getReportDefinitions = function() {
		var deferred = jQuery.Deferred();

		var self = this;

		jQuery
				.ajax(
						{
							type : "GET",
							beforeSend : function(request) {
								request.setRequestHeader("Authentication", self
										.getBasicAuthenticationHeader());
							},
							url : self.getBaseUrl()
									+ "/services/rest/bpm-reporting/report-definitions",
						}).done(function(data) {
					deferred.resolve(data);
				}).fail(function(data) {
					deferred.reject(data);
				});

		return deferred.promise();
	};

	/**
	 * 
	 */
	WorkflowService.prototype.getFolders = function(folder) {
		var deferred = jQuery.Deferred();
		var self = this;
		var folderId = folder ? folder.id : "null";

		jQuery.ajax(
				{
					type : "GET",
					url : self.getBaseUrl()
							+ "/services/rest/mobile-workflow/folders/"
							+ folderId,
				}).done(function(data) {
			deferred.resolve(data);
		}).fail(function(data) {
			deferred.reject(data);
		});

		return deferred.promise();
	};

	/**
	 * 
	 */
	WorkflowService.prototype.getBasicAuthenticationHeader = function() {
		// TODO Inherit security context

		return "Basic " + jQuery.base64.encode("motu" + ":" + "motu");
	};

	/**
	 * 
	 */
	WorkflowService.prototype.getBaseUrl = function(html) {
		return location.href.substring(0, location.href.indexOf("/plugins"));

	};

	/**
	 * 
	 */
	WorkflowService.prototype.getMobileWorkflowRestServicesUrl = function(html) {
		return location.href.substring(0, location.href.indexOf("/plugins"))
				+ "/services/rest/mobile-workflow";

	};
}