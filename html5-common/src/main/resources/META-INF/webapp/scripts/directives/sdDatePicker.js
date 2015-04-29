/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

(function(){
	'use strict';
var  jQueryDateFormats = {
		'MM/dd/yy' :  'm/d/y',
		'dd.MM.yy' :  'dd.m.y',
		'yyyy-MM-dd' : 'yy-m-d'
	};

angular.module('bpm-common.directives')
	.directive('sdDatePicker', [ 'sdLocalizationService', 'sgI18nService',function( sdLocalizationService, sgI18nService) {
	    return {
	        restrict: 'A',
	        require: 'ngModel',
	        link: function(scope, element, attrs, ngModelCtrl) {
	            
	            var months =sgI18nService.translate('html5-common.date-picker-meridian-months',
			'January,February,March,April,May,June,July,August,September,October,November,December'),
	                daysMin = sgI18nService.translate('html5-common.date-picker-meridian-days',
			'Su,Mo,Tu,We,Th,Fr,Sa,Su'),
	                dateFormat = attrs.sdaDateFormat || jQueryDateFormats[sdLocalizationService.getInfo().dateFormat],
	                firstDay = 1*(attrs.sdaFirstDay || 1),
	                numberOfMonths = 1*(attrs.sdaNumberMonths || 1),
	                changeMonth = (attrs.sdaChangeMonth === 'true')?true:false,
	                changeYear = (attrs.sdaChangeYear === 'false')?false:true,
	                showButtonPanel = (attrs.sdaShowButtons==='true')?true:false,
	                showAnim = attrs.sdaAnimationStyle || 'show',
	                showWeek = (attrs.sdaShowWeek==='true')?true:false,
	                minDate = attrs.sdaMinDate || "-10Y",
	                maxDate = attrs.sdaMaxDate || "10Y",
	                gotoCurrent = (attrs.sdaGotoCurrent==="true")?true:false,
	                showOn = attrs.sdaShowOn || 'both', //focus|button|both
	                buttonImage = attrs.sdaButtonImage || '',
	                constrainInput = (attrs.sdaConstrainInput==="true")?true:false,
	                defaultDate = attrs.sdaDefaultDate || 0,
	                currentText = attrs.sdaCurrentText || 'Today',
	                closeText = attrs.sdaCloseText || 'Close',
	                dayNamesMin =(attrs.sdaDayNamesMin)?attrs.sdaDayNamesMin.split(","):daysMin.split(","),
	                monthNames = (attrs.sdaMonthNames)?attrs.sdaMonthNames.split(","):months.split(","),
	                yearRange = attrs.sdaYearRange || 'c-10:c+10',
	                buttonImageOnly = (attrs.sdaButtonImageOnly==='true')?true:false ,
	                buttonText = attrs.sdaButtonText || "Select Date",
	                milliseconds = attrs.sdaMilliseconds === 'true' ? true : false;

		            ngModelCtrl.$parsers.push(function(value) {
		               ngModelCtrl.$setValidity('validate', true);
		            	if (value == undefined || value == null || value == '') {
		            		return value;
		            	}
		            	try{
		            	var date = jQuery.datepicker.parseDate(dateFormat, value);
		            	}catch (e) {
		            	   ngModelCtrl.$setValidity('validate', false);
	                     return undefined;
                     }
		            	
                		return milliseconds ? date.getTime():value;
		            });
	
		            ngModelCtrl.$formatters.push(function(value) {
		            	if (value == undefined || value == null || value == '') {
		            		return value;
		            	}

		            	var date = new Date(value);
                		return  milliseconds ? jQuery.datepicker.formatDate(dateFormat, date):vaue;
		            });

	                $(element).datepicker({
	                numberOfMonths : numberOfMonths,
	                dateFormat: dateFormat,
	                firstDay : firstDay,
	                changeMonth : changeMonth,
	                changeYear : changeYear,
	                showButtonPanel : showButtonPanel,
	                showAnim : showAnim,
	                showWeek : showWeek,
	                minDate: minDate,
	                maxDate: maxDate,
	                gotoCurrent: gotoCurrent,
	                currentText: currentText,
	                closeText : closeText,
	                showOn : showOn,
	                defaultDate: defaultDate,
	                buttonImage : buttonImage,
	                buttonImageOnly : buttonImageOnly,
	                yearRange: yearRange,
	                dayNamesMin : dayNamesMin,
	                monthNames: monthNames,
	                
	                onSelect: function(date) {
	                    ngModelCtrl.$setViewValue(date);
	                    ngModelCtrl.$render();
	                    if (angular.isFunction(ngModelCtrl.$apply)) {
	                    	ngModelCtrl.$apply();
	                    }
	                }
	            });
	        }
	    };
	}]);
})();