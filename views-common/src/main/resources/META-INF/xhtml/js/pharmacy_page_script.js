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
	require({baseUrl: "../../../graphics-common/js"},["m_pageController", "m_pharmacyToolbarController", "m_pharmacyCanvasController", "m_urlUtils"], function() {
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
	});
});

function configurei18n()
{	
	var lang;
	
	jQuery.ajax({ 
	    url: require('m_urlUtils').getContextName() + "/services/rest/documents/DUMMU_DOC_ID/pages/0/" + getNextRandom() + "/language",		    
		async: false,
	    success: function(l) {	    	
	        lang = l;
	    }
	});
	
  	InfinityBPMI18N.initPluginProps({
  		pluginName : "graphicscommon",
	    propFilePath : "../../../graphics-common/bundle/",
		propFileBaseName : "graphics-common-messages",
		locale : lang
	});
}

function getNextRandom()
{
	return new Date().getTime();
}