/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Shrikant.Gangal
 */
define( function() {
	return {
		/**
		 * @returns e.g. http://testhost:8080/pepper-test/plugins/
		 */
		getPlugsInRoot : function() {
			return location.href.substring(0, location.href.indexOf("/plugins")) + "/plugins/";
		},

		getContextName : function() {
			return location.pathname.substring(0, location.pathname.indexOf(
					'/', 1));
		},

		getQueryString : function() {
			return window.location.search.substring(1);
		},

		getQueryParams : function() {
			var queryString = this.getQueryString();
			var params = queryString.split('&');
			var qsParam = new Array();
			for ( var i = 0; i < params.length; i++) {
				var pos = params[i].indexOf('=');
				if (pos > 0) {
					var key = params[i].substring(0, pos);
					var val = params[i].substring(pos + 1);
					qsParam[key] = val;
				}
			}

			return qsParam;
		},

		getQueryParam : function(paramName) {
			var qsParams = this.getQueryParams();

			return qsParams[paramName];
		},

		getURL : function()
		{
			//return require('bpm-modeler/js/m_urlUtils').getContextName() + m_constants.ANNOTATIONS_RESTLET_PATH + documentId + m_constants.ANNOTATIONS_RESTLET_PATH_PAGE + m_pageController.getOriginalPageIndex() + "/" + m_pageController.getURLPostFix();
		},

		getUserServicesURL : function()
		{
			"/services/rest/views-common/documentRepoService/getUser"
		},

		getStampsQueryURL : function()
		{
			m_constants = require("bpm-modeler/js/m_constants");
			return this.getContextName() + m_constants.DMS_RESTLET_PATH + m_constants.STAMPS_GET_REQUEST_URL;
		},

		getDocDownloadTokenURL : function()
		{
			m_constants = require("bpm-modeler/js/m_constants");
			return this.getContextName() + m_constants.DMS_RESTLET_PATH + m_constants.DOCUMENT_DOWNLOAD_TOKEN_URL;
		},

		getModelerEndpointUrl : function()
		{
			return this.getContextName() + "/services/rest/bpm-modeler/modeler/" + new Date().getTime();
		}
	};
});