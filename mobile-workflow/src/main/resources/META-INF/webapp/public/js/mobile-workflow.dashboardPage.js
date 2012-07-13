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
function DashboardPage() {
	this.id = "dashboardPage";

	/**
	 * 
	 */
	DashboardPage.prototype.initialize = function() {
		$("#" + this.id + " #logoutLink").click(function() {
			getWorkflowService().logout(function() {
				getDeck().popPage();
			});
		});

		$("#" + this.id + " #worklistLink").click(function() {
			getDeck().pushPage(new WorklistPage());
		});

		$("#" + this.id + " #startableProcessesLink").click(function() {
			getDeck().pushPage(new StartableProcessesPage());
		});
	};
}
