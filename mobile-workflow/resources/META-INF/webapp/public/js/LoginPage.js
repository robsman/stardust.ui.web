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

if (!window.bpm.mobile_workflow.LoginPage) {
	bpm.mobile_workflow.LoginPage = function LoginPage() {
		this.id = "loginPage";

		/**
		 * 
		 */
		LoginPage.prototype.initialize = function() {
			var deferred = jQuery.Deferred();

			$("#" + this.id + " #errorPopup").popup();
			$("#" + this.id + " #errorPopup").popup("close");

			deferred.resolve();

			return deferred.promise();
		};

		/**
		 * 
		 */
		LoginPage.prototype.login = function() {
			console.log("Account: " + this.account);
			console.log("Password: " + this.password);

			getWorkflowService().login(this.account, this.password).done(
					function() {
						getDeck().pushPage(
								new bpm.mobile_workflow.DashboardPage());
					}).fail(function() {
				$("#errorPopup").popup("open");
			});
		};
	};
}
