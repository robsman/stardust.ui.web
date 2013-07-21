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

bpm.mobile_workflow.WorklistPage = function WorklistPage(activityInstanceOid) {
	this.id = "worklistPage";

	/**
	 * 
	 */
	WorklistPage.prototype.initialize = function() {
		var deferred = jQuery.Deferred();

		getWorkflowService()
				.getWorklist()
				.done(
						function(worklist) {
							$("#workitemList li").remove();

							for ( var n in worklist) {
								var activityInstance = worklist[n];

								var descriptors = "";
								var start = true;

								for ( var x in activityInstance.descriptors) {
									if (start) {
										start = false;
									} else {
										descriptors += ", ";
									}

									descriptors += x;
									descriptors += ": ";
									descriptors += activityInstance.descriptors[x] == null ? " -"
											: activityInstance.descriptors[x];
									descriptors += " ";
								}

								jQuery("#workitemList")
										.append(
												"<li><a id=\""
														+ activityInstance.oid
														+ "\" ref=\"#\">"
														+ "<h4>"
														+ activityInstance.activityName
														+ "</h4>"
														+ "<p>"
														+ formatDateTime(new Date(
																activityInstance.startTime))
														+ " "
														+ (activityInstance.lastModificationTime == 0 ? "-"
																: formatDateTime(new Date(
																		activityInstance.lastModificationTime)))
														+ "<br>" + descriptors
														+ "</p></a></li>");
								$("#workitemList #" + activityInstance.oid)
										.click(
												{
													"activityInstance" : activityInstance
												},
												function(event) {
													getDeck()
															.pushPage(
																	new bpm.mobile_workflow.ActivityInstancePage(
																			event.data.activityInstance));
												});
							}

							// $("#workitemList").listview("refresh");

							deferred.resolve();
						}).fail(function() {
					deferred.reject();
				});

		return deferred.promise();
	};

	/**
	 * 
	 */
	WorklistPage.prototype.back = function() {
		getDeck().popPage();
	};
};
