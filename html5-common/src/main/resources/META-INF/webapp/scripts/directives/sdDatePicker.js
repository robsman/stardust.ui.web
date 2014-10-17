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
	                dateFormat = attrs.dateFormat || 'yy.mm.dd',
	                firstDay = 1*(attrs.firstDay || 1),
	                numberOfMonths = 1*(attrs.numberMonths || 1),
	                changeMonth = (attrs.changeMonth === 'true')?true:false,
	                changeYear = (attrs.changeYear === 'true')?true:false,
	                showButtonPanel = (attrs.showButtons==='true')?true:false,
	                showAnim = attrs.animationStyle || 'show',
	                showWeek = (attrs.showWeek==='true')?true:false,
	                minDate = attrs.minDate || "-10Y",
	                maxDate = attrs.maxDate || "10Y",
	                gotoCurrent = (attrs.gotoCurrent==="true")?true:false,
	                showOn = attrs.showOn || 'both', //focus|button|both
	                buttonImage = attrs.buttonImage || '',
	                constrainInput = (attrs.constrainInput==="true")?true:false,
	                defaultDate = attrs.defaultDate || 0,
	                currentText = attrs.currentText || 'Today',
	                closeText = attrs.closeText || 'Close',
	                dayNames = (attrs.dayNames)?attrs.dayNames.split(","):days.split(","),
	                dayNamesMin =(attrs.dayNamesMin)?attrs.dayNamesMin.split(","):daysMin.split(","),
	                dayNamesShort =(attrs.dayNamesShort)?attrs.dayNamesShort.split(","):daysShort.split(","),
	                monthNames = (attrs.monthNames)?attrs.monthNames.split(","):months.split(","),
	                monthNamesShort = (attrs.monthNamesShort)?attrs.monthNamesShort.split(","):monthsShort.split(","),
	                yearRange = attrs.yearRange || 'c-10:c+10',
	                buttonImageOnly = (attrs.buttonImageOnly==='true')?true:false ,
	                buttonText = attrs.buttonText || "Select Date";
	            
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
	                    ngModelCtrl.$apply();
	                }
	                
	            });
	        }
	    };
	});