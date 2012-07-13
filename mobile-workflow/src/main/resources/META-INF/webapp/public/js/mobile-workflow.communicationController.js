/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

var endPointUrl = getContextName()
		+ "/services/rest/mobile-workflow/" + new Date().getTime();

/**
 * 
 * @returns
 */
function getContextName() {
	return location.pathname.substring(0, location.pathname.indexOf('/', 1));
}

/**
 * 
 * @param url
 * @param data
 * @param successCallback
 * @param errorCallback
 * @param options
 */
function postData(url, data, successCallback, errorCallback, options) {
	var result = jQuery
			.ajax({
				"type" : "POST",
				"url" : endPointUrl + url,
				"contentType" : options != null && options.contentType != null ? options.contentType
						: "application/json",
				"data" : JSON.stringify(data),
				"success" : successCallback,
				"error" : errorCallback
			});
}

/**
 * 
 * @param url
 * @param callback
 * @param options
 */
function getData(url, callback, options) {
	jQuery.getJSON(endPointUrl + url, callback);
}
