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
function WorklistPage(activityInstanceOid) {
	this.id = "worklistPage";

	/**
	 * 
	 */
	WorklistPage.prototype.initialize = function() {
		$("#worklistPage #backLink").click(function() {
			getDeck().popPage();
		});

		getWorkflowService().getWorklist(function(worklist) {
			$("#workitemList li").remove();

			for ( var n in worklist) {
				var activityInstance = worklist[n];

				jQuery("#workitemList").append(
						"<li><a id=\"" + activityInstance.oid + "\" ref=\"#\">"
								+ "<h4>" + activityInstance.activityName
								+ "</h4>" + "<p>" + activityInstance.startTime
								+ " " + activityInstance.lastModificationTime
								+ "</p></a></li>");
				$("#workitemList #" + activityInstance.oid).click(
						{
							"activityInstance" : activityInstance
						},
						function(event) {
							getDeck().pushPage(new ActivityInstancePage(
									event.data.activityInstance));
						});
			}

			$("#workitemList").listview("refresh");
		});
	};
}
