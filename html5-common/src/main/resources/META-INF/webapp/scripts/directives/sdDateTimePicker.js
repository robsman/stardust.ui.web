/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * @author Johnson.Quadras
 */

(function() {
    'use strict';

    angular.module('bpm-common').directive('sdDateTimePicker', [ Directive ]);

    /*
     */
    function Directive() {

	return {
	    restrict : 'A',
	    scope : {
		selectedDateTime : '=ngModel'
	    },
	    require : 'ngModel',
	    link : function(scope, elem, attr, ngModelCtrl) {

		ngModelCtrl.$parsers.push(function(value) {

		    if (value != undefined && isNaN(value)) {
			ngModelCtrl.$setValidity('validate', false);
			return undefined;
		    }
		    ngModelCtrl.$setValidity('validate', true);
		    return value;

		});

		scope.ctrl.onChange = function() {

		    var date = null;
		    if (scope.ctrl.selectedDate.date == '') {
			date = getDate();
		    } else {
			date = getDate(scope.ctrl.selectedDate);
		    }
		    scope.selectedDateTime = date;
		    ngModelCtrl.$setViewValue(date);
		    ngModelCtrl.$render();
		    if (angular.isFunction(ngModelCtrl.$apply)) {
			ngModelCtrl.$apply();
		    }
		}
	    },
	    controller : [ '$scope', Controller ],
	    template : '<input  type="text" sd-date-picker sda-change-year="true" sda-milliseconds="true" style="width:100px"' + 
	    				' id="selectDate" name="selectDate"   ng-change="ctrl.onChange()" ng-model="ctrl.selectedDate.date"  / > '+
	    		'<select  ng-change="ctrl.onChange()" style = "width : 55px"  ng-model="ctrl.selectedDate.hours" '+
	    				' ng-options="option as option for option in ctrl.hoursOptions"></select> : '+
		        '<select  ng-change="ctrl.onChange()"  style = "width : 55px"  ng-model="ctrl.selectedDate.mins" '+
	    				' ng-options="option as option for option in ctrl.minsOptions"></select>'
	}
    }

    var hoursOptions = getArrayWithNumber(0, 23);
    var minsOptions = getArrayWithNumber(0, 59);

    /**
     * 
     */
    function Controller($scope) {
	var self = this;
	var currentDate = new Date();
	if ($scope.selectedDateTime) {
	    self.selectedDate = getDateTimeObj(new Date($scope.selectedDateTime), true);
	} else {
	    self.selectedDate = getDateTimeObj(currentDate, false);
	}
	self.hoursOptions = hoursOptions;
	self.minsOptions = minsOptions;
	$scope.ctrl = this;
    }

    /**
     * 
     */
    function getDateTimeObj(date, isDateSelected) {
	var dateTime = {
	    hours : date.getHours(),
	    mins : date.getMinutes()
	}

	if (isDateSelected) {
	    dateTime.date = date.getTime();
	} else {
	    dateTime.date = '';
	}
	return dateTime;
    }

    /**
     * 
     */
    function getArrayWithNumber(from, to) {
	var array = [];
	for (var i = from; i <= to; i++) {
	    array.push(i);
	}
	return array;
    }


    /**
     * 
     */
    function getDate(input) {
	if (!input) {
	    return undefined;
	}
	var date = new Date(input.date);
	date.setHours(input.hours);
	date.setMinutes(input.mins);
	return date.getTime();
    }

})();
