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
 * @author Johnson.Quadras
 */

(function() {
    'use strict';
    
   /*Date Time filter*/
    angular.module('bpm-common').filter('sdDateTimeFilter',
	    [ '$filter', 'sdLocalizationService', function($filter, sdLocalizationService) {

		return function(input) {
		    if (input == null) {
			return "";
		    }
		    var angularDateFilter = $filter('date');
		    var format = sdLocalizationService.getInfo().dateTimeFormat;
		    return angularDateFilter(input, format);
		};

	    } ]);

    /*Date Filter*/
    angular.module('bpm-common').filter('sdDateFilter',
	    [ '$filter', 'sdLocalizationService', function($filter, sdLocalizationService) {

		return function(input) {
		    if (input == null) {
			return "";
		    }
		    var angularDateFilter = $filter('date');
		    var format = sdLocalizationService.getInfo().dateFormat;
		    return angularDateFilter(input, format);
		};

	    } ]);

   /* Time filter*/
    angular.module('bpm-common').filter('sdTimeFilter',
	    [ '$filter', 'sdLocalizationService', function($filter, sdLocalizationService) {

		return function(input) {
		    if (input == null) {
			return "";
		    }
		    var angularDateFilter = $filter('date');
		    var format = sdLocalizationService.getInfo().timeFormat;
		    return angularDateFilter(input, format);
		};

	    } ]);

})();
