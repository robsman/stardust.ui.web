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
jQuery(function () {
	require({baseUrl: "../../../graphics-common/js"},["m_pageController", "m_pharmacyToolbarController", "m_pharmacyCanvasController", "m_urlUtils", "extensions_jquery"], function() {
		var canvasController = require('m_pharmacyCanvasController');
		var urlUtils = require('m_urlUtils');
		var tiffIframe = parent.frames['tiff_frame' + urlUtils
				.getQueryParam('docId')];
		configurei18n();	
		canvasController.init(tiffIframe, 'canvas',
				urlUtils.getQueryParam('docId'), urlUtils
						.getQueryParam('noOfPages'),
						urlUtils.getQueryParam('isEditable'),
						parseFloat(urlUtils.getQueryParam('canvasWidth')),
						parseFloat(urlUtils.getQueryParam('canvasHeight')),
						'toolbar');
		loadCustomTheme();
	});
});

function configurei18n()
{	
	var lang;
	
	jQuery.ajax({ 
	    url: require('m_urlUtils').getContextName() + "/services/rest/graphics-common/documents/DUMMU_DOC_ID/pages/0/" + getNextRandom() + "/language",		    
		async: false,
	    success: function(l) {	    	
	        lang = l;
	    }
	});
	
  	InfinityBPMI18N.initPluginProps({
  		pluginName : "graphicscommon",
  		singleEndPoint : require('m_urlUtils').getContextName() + "/services/rest/common/properties/graphics-common-client-messages/" + lang
	});
}

function getNextRandom()
{
	return new Date().getTime();
}

function loadCustomTheme() {
	var m_urlUtils = require('m_urlUtils');
	jQuery.ajax({
		type : 'GET',
		url : m_urlUtils.getContextName() + "/services/rest/common/html5/api/themes/current/custom",
		async : true
	}).done(function(json){
		var head = document.getElementsByTagName('head')[0];
		for(var i in json.stylesheets) {
			var link = document.createElement('link');
			link.href = m_urlUtils.getContextName() + "/" + json.stylesheets[i];
			link.rel = 'stylesheet';
			link.type = 'text/css';
			head.appendChild(link);
		}
	}).fail(function(err){
		if(console && console.error){
			console.error("Failed in loading custom theme");
		}
	});
}