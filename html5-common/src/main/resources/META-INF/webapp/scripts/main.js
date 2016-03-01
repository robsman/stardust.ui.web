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
// Define Custom Modules
angular.module('bpm-common.services', []);

angular.module('bpm-common.directives', ['ui.bootstrap']);

angular.module('bpm-common.init', []).run(['$rootScope', 'sdUtilService', function($rootScope, sdUtilService) {
	$rootScope.bpmCommon = {
		stopEvent : sdUtilService.stopEvent
	};
}]);

angular.module('bpm-common', ['bpm-common.services', 'bpm-common.directives', 'bpm-common.init'])
.config(['$httpProvider', function ($httpProvider) {
	$httpProvider.interceptors.push('httpInterceptor');
	
	var msie = false; // For non-IE browser.
	
	try {
		var ua = window.navigator.userAgent;
		
	    var rv = ua.indexOf('MSIE ');
	    if (rv > 0) {
	    	msie = true;
	    } else {
	    	var trident = ua.indexOf('Trident/'); // IE 11
		    if (trident > 0) {
		    	msie = true;
		    } else {
		    	var edge = ua.indexOf('Edge/'); // IE 12
			    if (edge > 0) {
			    	msie = true;
			    }	
		    }
	    }
	   	} catch (e) {}
	   	
	// To disable cache for IE
	if(msie) {
		if (!$httpProvider.defaults.headers.get) {
	        $httpProvider.defaults.headers.get = {};    
	    }  
	  	// disable IE ajax request caching
	    $httpProvider.defaults.headers.get['If-Modified-Since'] = 'Mon, 26 Jul 1997 05:00:00 GMT';
	    // extra
	    $httpProvider.defaults.headers.get['Cache-Control'] = 'no-cache';
	    $httpProvider.defaults.headers.get['Pragma'] = 'no-cache';
	    // To disable JQuery cache for Ajax calls
	    jQuery.ajaxSetup({ cache: false });  
	 }
    
}]);

// Register top level module to Framework
portalApplication.registerModule('bpm-common');