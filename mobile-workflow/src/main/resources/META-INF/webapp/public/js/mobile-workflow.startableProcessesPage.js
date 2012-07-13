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
function StartableProcessesPage() {
	this.id = "startableProcessesPage";

	/**
	 * 
	 */
	StartableProcessesPage.prototype.initialize = function() {
		$("#startableProcessesPage #backLink").click(function() {
			getDeck().popPage();
		});

		getWorkflowService().getStartableProcesses(function(startableProcessesList) {
			jQuery("#startableProcessesList li").remove();

			for ( var n in startableProcessesList) {
				var process = startableProcessesList[n];

				jQuery("#startableProcessesList").append(
						"<li><a id=\"" + process.id + "\" ref=\"#\">" + "<h4>"
								+ process.name + "</h4>" + "<p>"
								+ process.description + "</p></a></li>");
				$("#startableProcessesList #" + process.id).click({
					"page" : getDeck().getTopPage(),
					"process" : process
				}, function(event) {
					event.data.page.startProcess(event.data.process);
				});
			}

			jQuery("#startableProcessesList").listview("refresh");
		});
	};

	/**
	 * 
	 */
	StartableProcessesPage.prototype.startProcess = function(process) {
		getWorkflowService().startProcess(process, function(activityInstance) {
			if (activityInstance != null) {
				getDeck().pushPage(new ActivityInstancePage(activityInstance));
			} else {
			}
		});
	};
}
