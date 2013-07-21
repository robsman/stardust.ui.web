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

bpm.mobile_workflow.ProcessPage = function ProcessPage(processInstanceOid) {
	this.id = "processPage";
	this.processInstanceOid = processInstanceOid;

	/**
	 * 
	 */
	ProcessPage.prototype.initialize = function() {
		var deferred = jQuery.Deferred();
		var self = this;

		getWorkflowService().getProcessInstance(this.processInstanceOid).done(
				function(processInstance) {
					self.processInstance = processInstance;

					console.log("Process Instance");
					console.log(self.processInstance);

					deferred.resolve();
				}).fail(function() {
			deferred.reject();
		});

		return deferred.promise();
	};

	/**
	 * 
	 */
	ProcessPage.prototype.openUserPage = function(x) {
		getDeck().pushPage(new bpm.mobile_workflow.UserPage(x));
	};

	/**
	 * 
	 */
	ProcessPage.prototype.back = function() {
		getDeck().popPage();
	};

};
