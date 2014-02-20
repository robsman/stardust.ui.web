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
				            	    '//ajax.googleapis.com/ajax/libs/angularjs/1.2.11/angular.min' ]
			},
			shim : {
				'jquery-mobile' : [ 'jquery' ],
				'jquery-ui' : [ 'jquery' ],
				'angularjs' : {
					require : "jquery",
					exports : "angular"
				}
			}
		});

require(
		
		[ "require", 
		  "jquery", 
		  "angularjs", 
		  "jquery-mobile",
		  "js/app",
		  "jquery-ui"],
		  
		function(require, jquery, angularjs, jqueryMobile,app,jqueryui) {
			app.init();
		});