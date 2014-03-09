/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

var bpm = null;

require
		.config({
			baseUrl : "./",
			paths : {
				'jquery' 		: [ 'js/libs/jquery/jquery-1.7.2.min',
				         		    '//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min' ],
				'jquery-mobile' : [ 'js/libs/jquery/plugins/jquery.mobile-1.4.0.min',
				                    '//code.jquery.com/mobile/1.4.0/jquery.mobile-1.4.0.min'],
				'jquery-ui'		: ['js/libs/jquery/plugins/jquery-ui-1.10.2.custom.min'],
				'angularjs' 	: [ 'js/libs/angular/angular.1.2.11.min',
				            	    '//ajax.googleapis.com/ajax/libs/angularjs/1.2.11/angular.min' ],
				 // SG
                'json' 			: [ '../../../views-common/js/libs/json/json2',
                       			    '//cdnjs.cloudflare.com/ajax/libs/json2/20110223/json2' ],
                'jquery.url' 	: [ 'js/libs/jquery/plugins/jquery.url',
                             	    'https://raw.github.com/allmarkedup/jQuery-URL-Parser/4f5254f2519111ad7037d398b2efa61d3cda58d4/jquery.url' ],
	            'xml2json' 		: [ '../../../processportal/xml2js' ],
	            'bpm.portal.Interaction' : [ '../../../processportal/Interaction' ],
	            'bpm.portal.GenericController' : [ '../../../processportal/GenericController' ]
			},
			shim : {
				'jquery-mobile' : [ 'jquery' ],
				'jquery-ui' : [ 'jquery' ],
				'angularjs' : {
					require : "jquery",
					exports : "angular"
				},
				// SG
				'jquery.url' : [ 'jquery' ], 
                'bpm.portal.Interaction' : [ 'jquery' ],
                'bpm.portal.GenericController' : [ 'jquery' ]
			}
		});

require(
		
		[ "require", 
		  "jquery", 
		  "angularjs", 
		  "jquery-mobile",
		  "js/app",
		  // SG
		  "jquery-ui", 'json', 'jquery.url', 'xml2json', 'bpm.portal.Interaction', 'bpm.portal.GenericController'],
		  
		function(require, jquery, angularjs, jqueryMobile,app,jqueryui) {
			app.init();
		});