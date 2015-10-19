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

	angular.module('bpm-common').directive('sdDateTimePicker', ['sdLocalizationService', 'sgI18nService', Directive]);

	/*
	 */
	function Directive(sdLocalizationService, sgI18nService) {

		return {
			restrict : 'A',
			scope : {
				selectedDateTime : '=ngModel',
				autoIdPrefix : '@sdaAidPrefix'
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
						date = getDate(scope.ctrl.selectedDate, scope.ctrl.is24HourClock);
					}
					scope.selectedDateTime = date;
					ngModelCtrl.$setViewValue(date);
					ngModelCtrl.$render();
					if (angular.isFunction(ngModelCtrl.$apply)) {
						ngModelCtrl.$apply();
					}
				}
			},
			controller : ['$scope', 'sdLocalizationService', 'sgI18nService', Controller],
			template : '<span id="date">'
					+ '<input  type="text" sd-date-picker ng-model-onblur sda-milliseconds="true" style="width:100px"'
					+ ' id="selectDate" name="selectDate" ng-change="ctrl.onChange()" ng-model="ctrl.selectedDate.date" aid="{{ctrl.aidDate}}" / >'
					+ '<span>'
					+ '<span id="time">'
					+ ' <select  ng-change="ctrl.onChange()" style = "width : 55px;min-width : 55px;"  ng-model="ctrl.selectedDate.hours" '
					+ ' ng-options="option as option for option in ctrl.hoursOptions" aid="{{ctrl.aidHours}}"></select> : '
					+ '<select  ng-change="ctrl.onChange()"  style = "width : 55px;min-width : 55px"  ng-model="ctrl.selectedDate.mins" '
					+ ' ng-options="option as option for option in ctrl.minsOptions" aid="{{ctrl.aidMins}}"></select>'
					+ ' <select ng-show ="!ctrl.is24HourClock" ng-change="ctrl.onChange()"  style = "width : 60px;min-width : 60px"'
					+ ' ng-model="ctrl.selectedDate.meridian" '
					+ ' ng-options="option as option for option in ctrl.meridianOptions" aid="{{ctrl.aidMeridian}}"></select>'
					+ '<span>'
		}
	}

	var OPTIONS = {
		clock24 : {
			hours : getArrayWithNumber(0, 23)
		},
		clock12 : {
			hours : getArrayWithNumber(1, 12)
		},
		minutes : getArrayWithNumber(0, 59),
		AM : '',
		PM : ''
	}

	/**
	 * 
	 */
	function Controller($scope, sdLocalizationService, sgI18nService) {

		var self = this;
		var currentDate = new Date();
		var dateTimeFormat = sdLocalizationService.getInfo().dateTimeFormat;
		OPTIONS.AM = sgI18nService.translate('html5-common.date-picker-meridian-am', 'am');
		OPTIONS.PM = sgI18nService.translate('html5-common.date-picker-meridian-pm', 'pm');

		this.is24HourClock = true;
		if (dateTimeFormat.indexOf('a') > -1) {
			this.is24HourClock = false;
		}

		if (this.is24HourClock) {
			self.hoursOptions = OPTIONS.clock24.hours;
		} else {
			self.hoursOptions = OPTIONS.clock12.hours;
		}
		self.minsOptions = OPTIONS.minutes;
		self.meridianOptions = [OPTIONS.AM, OPTIONS.PM]

		if ($scope.selectedDateTime) {
			self.selectedDate = getDateTimeObj(new Date($scope.selectedDateTime), true, self.is24HourClock);
		} else {
			self.selectedDate = getDateTimeObj(currentDate, false, self.is24HourClock);
		}

		$scope.$watch('selectedDateTime', function() {
			if ($scope.selectedDateTime != undefined) {
				if ($scope.selectedDateTime) {
					self.selectedDate = getDateTimeObj(new Date($scope.selectedDateTime), true, self.is24HourClock);
				} else {
					self.selectedDate = getDateTimeObj(currentDate, false, self.is24HourClock);
				}
				
				self.onChange();
			}
		});

		if ($scope.autoIdPrefix) {
			self.aidDate = $scope.autoIdPrefix + "-Date";
			self.aidHours = $scope.autoIdPrefix + "-Hours";
			self.aidMins = $scope.autoIdPrefix + "-Mins";
			self.aidMeridian = $scope.autoIdPrefix + "-Meridian";
		}

		$scope.ctrl = this;
	}

	/**
	 * 
	 */
	function getDateTimeObj(date, isDateSelected, is24HourClock) {

		var dateTime = {
			mins : date.getMinutes()
		}

		if (is24HourClock) {
			dateTime.hours = date.getHours()
		} else {
			var postHour = date.getHours();
			if (postHour == 12) { // At 00 hours we need to show 12 AM
				dateTime.meridian = OPTIONS.PM;
				dateTime.hours = postHour;
			} else if (postHour > 12) {
				dateTime.meridian = OPTIONS.PM;
				dateTime.hours = postHour - 12;
			} else if (postHour == 0) {
				dateTime.hours = 12;
				dateTime.meridian = OPTIONS.AM;
			} else {
				dateTime.hours = postHour;
				dateTime.meridian = OPTIONS.AM;
			}
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
	function getDate(input, is24HourClock) {
		if (!input) {
			return undefined;
		}
		var date = new Date(input.date);
		date.setMinutes(input.mins);
		if (!is24HourClock) {
			if (input.meridian == OPTIONS.PM && input.hours < 12) {
				date.setHours(input.hours + 12);
			} else if (input.meridian == OPTIONS.AM && input.hours == 12) {
				date.setHours(input.hours - 12);
			} else {
				date.setHours(input.hours);
			}

		} else {
			date.setHours(input.hours);
		}
		return date.getTime();
	}

})();
