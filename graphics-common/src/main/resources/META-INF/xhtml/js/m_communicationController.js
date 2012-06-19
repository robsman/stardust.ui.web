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
define(function(){
	return {
		postData : function(options, dataToBePosted, callbacks) {
			jQuery.ajax({
				type: 'POST',
				url: options.url,
				contentType: options.hasOwnProperty('contentType') ? options.contentType : 'application/json',
				data: dataToBePosted,
				success: callbacks.hasOwnProperty('success') ? callbacks.success : null,
				error: callbacks.hasOwnProperty('error') ? callbacks.error : null
			});
		},

		syncPostData : function(options, dataToBePosted, callbacks) {
			jQuery.ajax({
				type: 'POST',
				url: options.url,
				async: false,
				contentType: options.hasOwnProperty('contentType') ? options.contentType : 'application/json',
				data: dataToBePosted,
				success: callbacks.hasOwnProperty('success') ? callbacks.success : null,
				error: callbacks.hasOwnProperty('error') ? callbacks.error : null
			});
		},
		
		getData : function(url, callback) {
			jQuery.getJSON(url, callback);
		},
		
		syncGetData : function(options, callbacks) {
			jQuery.ajax({
				type: 'GET',
				url: options.url,
				async: false,
				success: callbacks.hasOwnProperty('success') ? callbacks.success : null,
				error: callbacks.hasOwnProperty('error') ? callbacks.error : null
			});
		}
	}
});