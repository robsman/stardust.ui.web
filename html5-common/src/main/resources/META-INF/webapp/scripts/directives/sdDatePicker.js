/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

angular.module('bpm-common.directives')
	.directive('sdDatePicker', function() {
	    return {
	        restrict: 'A',
	        require: 'ngModel',
	        link: function(scope, element, attrs, ngModelCtrl) {
	          
	            var months = "January,February,March,April,May,June,July," +
	                         "August,September,October,November,December",
	                monthsShort = "Jan,Feb,Mar,Apr,May,Jun,Jul,Aug," +
	                                 "Sep,Oct,Nov,Dec",
	                days = "Sunday,Monday,Tuesday,Wednesday," +
	                       "Thursday,Friday,Saturday",
	                daysMin = "Su,Mo,Tu,We,Th,Fr,Sa,Su",
	                daysShort = "Sun,Mon,Tue,Wed,Thu,Fri,Sat",
	                dateFormat = attrs.sdaDateFormat || 'yy.mm.dd',
	                firstDay = 1*(attrs.sdaFirstDay || 1),
	                numberOfMonths = 1*(attrs.sdaNumberMonths || 1),
	                changeMonth = (attrs.sdaChangeMonth === 'true')?true:false,
	                changeYear = (attrs.sdaChangeYear === 'true')?true:false,
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
	                dayNames = (attrs.sdaDayNames)?attrs.sdaDayNames.split(","):days.split(","),
	                dayNamesMin =(attrs.sdaDayNamesMin)?attrs.sdaDayNamesMin.split(","):daysMin.split(","),
	                dayNamesShort =(attrs.sdaDayNamesShort)?attrs.sdaDayNamesShort.split(","):daysShort.split(","),
	                monthNames = (attrs.sdaMonthNames)?attrs.sdaMonthNames.split(","):months.split(","),
	                monthNamesShort = (attrs.sdaMonthNamesShort)?attrs.sdaMonthNamesShort.split(","):monthsShort.split(","),
	                yearRange = attrs.sdaYearRange || 'c-10:c+10',
	                buttonImageOnly = (attrs.sdaButtonImageOnly==='true')?true:false ,
	                buttonText = attrs.sdaButtonText || "Select Date",
	                milliseconds = attrs.sdaMilliseconds === 'true' ? true : false;

	            if (milliseconds) {
		            ngModelCtrl.$parsers.push(function(value) {
		            	if (value == undefined || value == null || value == '') {
		            		return value;
		            	}

		            	var date = jQuery.datepicker.parseDate(dateFormat, value);
                		return date.getTime();
		            });
	
		            ngModelCtrl.$formatters.push(function(value) {
		            	if (value == undefined || value == null || value == '') {
		            		return value;
		            	}

		            	var date = new Date(value);
                		return jQuery.datepicker.formatDate(dateFormat, date);
		            });
	            }

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
	                dayNames: dayNames,
	                dayNamesMin : dayNamesMin,
	                dayNamesShort: dayNamesShort,
	                monthNames: monthNames,
	                monthNamesShort : monthNamesShort,
	                
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
	});