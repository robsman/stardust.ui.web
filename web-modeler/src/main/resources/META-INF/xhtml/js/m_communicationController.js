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

define([ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_urlUtils" ], function(m_utils, m_constants, m_urlUtils) {

	var endPointUrl = m_urlUtils.getContextName()
	+ "/services/rest/bpm-modeler/modeler/" + new Date().getTime();
	
	return {
		getEndpointUrl : function() {
			return endPointUrl;
		},
		
		postData : function(options, dataToBePosted, callbacks) {
			var transferObject = null;
			var result = jQuery.ajax({
				type: 'POST',
				url: options.url,
				async: options.sync ? false : true, //default :true
				contentType: options.hasOwnProperty('contentType') ? options.contentType : 'application/json',
				data: dataToBePosted,
				success: callbacks.hasOwnProperty('success') ? callbacks.success : null,
				error: callbacks.hasOwnProperty('error') ? callbacks.error : null
			});
		},

		deleteData : function(options, dataToBePosted, callbacks) {
			jQuery.ajax({
				type: 'DELETE',
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
				callbackScope: options.callbackScope,
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
		
		getHead : function(options, callbacks) {
			jQuery.ajax({
				type: 'HEAD',
				url: options.url,
				callbackScope: options.callbackScope,
				async: false,
				success: callbacks.hasOwnProperty('success') ? callbacks.success : null,
				error: callbacks.hasOwnProperty('error') ? callbacks.error : null
			});
		},
		
		syncGetData : function(options, callbacks) {
			jQuery.ajax({
				type: 'GET',
				url: options.url,
				callbackScope: options.callbackScope,
				async: false,
				success: callbacks.hasOwnProperty('success') ? callbacks.success : null,
				error: callbacks.hasOwnProperty('error') ? callbacks.error : null
			});
		}
	}
});