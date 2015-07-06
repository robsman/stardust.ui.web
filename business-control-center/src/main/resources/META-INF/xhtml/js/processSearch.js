/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

var archivePortalWindow;

/*
 * 
 */
function openArchiveSearch(url, criteria) {
	var message = '{"type": "OpenView", "data": {"viewId": "processSearchView", "params": ' + criteria + '}}';
	
	if (archivePortalWindow && !archivePortalWindow.closed) {
		if (window.console) {
			console.log('Archive Search Message', message);
		}
		archivePortalWindow.postMessage(message, '*');
	} else {
		url += "?uicommand=" + message;
		if (window.console) {
			console.log('Archive Search URL', url);
		}
		archivePortalWindow = window.open(url, 'ArchivePortal');
	}
	
	archivePortalWindow.focus();
}