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
function LoginPage() {
	this.id = "loginPage";

	/**
	 * 
	 */
	LoginPage.prototype.initialize = function() {
		$("#" + this.id + " #loginButton").click({
			"page" : this
		}, function(event) {
			event.data.page.login();
		});

		$("#" + this.id + " #accountTextInput").val("");
		$("#" + this.id + " #accountPasswordInput").val("");
		$("#" + this.id + " #errorPopup").popup();
		$("#" + this.id + " #errorPopup").popup("close");
	};

	/**
	 * 
	 */
	LoginPage.prototype.login = function() {
		getWorkflowService().login($("#" + this.id + " #accountTextInput").val(), 
				$("#" + this.id + " #accountPasswordInput").val(), function() {
			getDeck().pushPage(new DashboardPage());
		}, function() {
			$("#errorPopup").popup("open");			
		});
	};
}
