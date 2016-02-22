/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
bpm.portal.reportingRequire.config({baseUrl: "../../../"});

require([ "require", "jquery", "angularjs",
		"bpm-reporting/js/ReportStorageController" ], function(require, jQuery,
		angularjs, ReportStorageController) {
	jQuery(document).ready(function() {
		ReportStorageController.create();
	});
});
