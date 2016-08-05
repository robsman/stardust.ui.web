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
 * DateTime control which leverages the JQuery datePicker for manipulation of the datePart, and leverages a second
 * input field to allow the user to manipuate the time components of the ngModel value. The value bound to the ngModel
 * should preferabbly be the long representation of a date as computed by the Date.getTime function. If a date object is 
 * bound then the directive will convert the date to its long representation. If no valid value is bound then the datePicker
 * will use the current date time.
 *
 * DIRECTIVE ATTRIBUTES
 * ------------------------------------
 * ngModel : {long} Value bound to the control representing the date and time of interest.
 * 			  Dev note: internally this is referenced as selectedDateTime. 
 *
 * sdaDefaultAdjustment : {string} When present this will result in two things occuring upon directive initialization.
 * 						  1. Directive will use the current date and time as the default value...
 * 						  2. The directive will then adjust the current time by the value specified in the attribute.
 * 						  
 * 						  Format: [0-9]+(m|h|d|M|y) where m = minutes, h = hours, d = days, M = Months, y= Years.
 * 						          e.g.  '30m','12h','7d' ...etc
 *
 * 
 * @return {[type]} [description]
 */
(function() {
	'use strict';

	angular.module('bpm-common').directive('sdDateTimeCondensed', ['sdLocalizationService', 'sgI18nService', Directive]);

	/*
	 */
	function Directive(sdLocalizationService, sgI18nService) {

		return {
			restrict : 'A',
			scope : {
				selectedDateTime : '=ngModel',
				defaultAdj : '@sdaDefaultAdjustment',
				autoIdPrefix : '@sdaAidPrefix'
			},
			require : 'ngModel',
			link : function(scope, elem, attr, ngModelCtrl) {

				scope.ctrl.ngModelCtrl = ngModelCtrl;
				scope.ctrl.dateElem = $("input[sd-date-picker]",elem);

				//This is required otherwise we dont keep our state synched with our date part
				//We will also be using an explicit call to a setValidity function in our 
				//controller in order to close this circle completely.
				ngModelCtrl.$parsers.push(function(value) {

					var dateValid = scope.ctrl.isDateValid(scope.ctrl.selectedDateTime);
					var timeValid = scope.ctrl.isTimeValid(scope.ctrl.timeComponent);

					//if both values are undefined then assume valid as it corresponds to both elements being pristine
					if(scope.ctrl.selectedDateTime === undefined && scope.ctrl.timeComponent === undefined){
						ngModelCtrl.$setValidity('validate', true);
						return value;
					}
					else if(dateValid && timeValid){
						ngModelCtrl.$setValidity('validate', true);
						return value;
					}
					else{
						ngModelCtrl.$setValidity('validate', false);
						return undefined;
					}

				});

			},
			controllerAs: 'ctrl',
			controller : ['$scope', 'sdLocalizationService', 'sgI18nService', '$timeout', Controller],
			template : 	'<div class="sda-date-time-condensed" id="date">'+
							'<input name="dpart" type="text" style="display:none;">' +
							'<input name="dpart" class="date-part" placeholder="{{ctrl.formatObj.dateFormat}}"  ' +
								'ng-class="{invalid:!ctrl.isDateValid(ctrl.selectedDate) && ctrl.isDirty}" ' +
								'ng-blur=" ctrl.isDirty = true" ' +
								'type="text" sd-date-picker ng-model-onblur sda-milliseconds="true" '+
								'id="selectDate" name="selectDate" ng-change="ctrl.updateView(ctrl.selectedDate,ctrl.timeComponent)" ' +
								'ng-model="ctrl.selectedDate" aid="{{ctrl.aidDate}}" / >'+
							'<input class="time-part" ' +
								'ng-blur="ctrl.updateView(ctrl.selectedDate,ctrl.timeComponent);ctrl.isDirty = true" ' +
								'ng-keydown="ctrl.keyMonitor($event)" ' +
								'ng-model="ctrl.timeComponent" ' +
								'ng-class="{invalid:!ctrl.isTimeValid(ctrl.timeComponent) && ctrl.isDirty}" ' +
								'placeholder="{{ctrl.formatObj.timeFormat}}"></input>' +
						'</div>'
		}
	}

	var OPTIONS = {
		AM : '',
		PM : ''
	}
	
	/**
	 * Constructor for our controller.
	 * @param {[type]} $scope                [description]
	 * @param {[type]} sdLocalizationService [description]
	 * @param {[type]} sgI18nService         [description]
	 * @param {[type]} $timeout              [description]
	 */
	function Controller($scope, sdLocalizationService, sgI18nService, $timeout) {

		var self = this;
		var currentDate = new Date();
		var meridianRxPart = "";

		//retrieve date and time format strings from the server.
		this.formatObj = sdLocalizationService.getInfo();

		//Place injected depenendencies on our object instance so we can reference them in prototyped scopes.
		this.$timeout = $timeout;
		this.$scope = $scope;

		//Track dirty state for our combined input fields
		this.isDirty = false;

		//set up valid i18n terms for our meridian options.
		this.meridianOptions = {
			"AM" : sgI18nService.translate('html5-common.date-picker-meridian-am', 'am'),
			"PM" : sgI18nService.translate('html5-common.date-picker-meridian-pm', 'pm'),
		}

		//Calculate the RX part for our meridians based on our i18n values. Be forgiving and ignore case
		meridianRxPart = this.meridianOptions.AM.toUpperCase() + "|" +  this.meridianOptions.PM.toUpperCase();
		meridianRxPart +=  "|" + this.meridianOptions.AM.toLowerCase() + "|" +  this.meridianOptions.PM.toLowerCase();

		//char fishing for whether or not we should use a 24 hour clock. seems fragile.
		this.is24HourClock = true;
		if (this.formatObj.dateTimeFormat.indexOf('a') > -1) {
			this.is24HourClock = false;
		}

		//Set up our regular expressions to validate our timeComponent string.
		if (this.is24HourClock) {
			self.timePartRX =new RegExp ("(((0|1)?[0-9])|(2[0-3])):[0-5][0-9]"); //no meridian for 24 hour clocks
		} else {
			self.timePartRX =new RegExp ("((^1[0-2]{1})|(^0?[0-9]{1})):[0-5][0-9] (" + meridianRxPart + ")");
		}

		$scope.ctrl = this;

		//initialize our sub structure, sd-date-picker , input field for time component.
		this.init();

		
	}

	/**
	 * Provide spinner like capabilities via up down arrows for our
	 * time component.
	 * @param  {[type]} $event [description]
	 * @return {[type]}        [description]
	 */
	Controller.prototype.keyMonitor = function($event){

		var keyCode = $event.keyCode;
		var caretPos; 
		var subStr;
		var calculatedVal;
		var calculatedTimeStr;
		var that = this;
		var modifier;
		var mode;

		if(keyCode !== 38 && keyCode !==40){return;}

		if(!this.isTimeValid(this.timeComponent) || !this.isDateValid(this.selectedDateTime)){
			return;
		}

		modifier = (keyCode===38)?1:-1;
		calculatedVal = this.selectedDateTime.getTime();
		caretPos = $event.target.selectionStart;
		subStr = this.timeComponent.substr(caretPos);

		//we are before the colon thus adjust hours
		if(subStr.indexOf(":") > -1){
			mode="h"
			calculatedVal += modifier*(1000*60*60);
		}
		//we are before the space between minutes and meridian, so adjust minutes
		else if(subStr.indexOf(" ") > -1){
			mode="m"
			calculatedVal += modifier*(1000*60);
		}
		//adjust meridian, arrow direction does not matter here, more of a toggle
		else{
			mode="a";
			modifier = (subStr.indexOf(this.meridianOptions.AM)>-1)?1:-1;
			calculatedVal += modifier*(1000*60*60*12);
		}
		calculatedTimeStr = this.buildTimeString(calculatedVal);
		this.timeComponent=calculatedTimeStr;

		this.updateView(calculatedVal,calculatedTimeStr);

		//reset cursor position after view updates
		this.$timeout(function(){
			if(mode==="m"){
				$event.target.selectionStart = that.timeComponent.indexOf(":") + 1;
				$event.target.selectionEnd = that.timeComponent.indexOf(" "); 
			}
			else if(mode==="h"){
				$event.target.selectionStart = 0;
				$event.target.selectionEnd = that.timeComponent.indexOf(":");
			}
			else if(mode==="a"){
				$event.target.selectionStart = that.timeComponent.indexOf(" ")+1;
				$event.target.selectionEnd = that.timeComponent.length;
			}
		},0);

		$event.preventDefault();
		$event.stopPropagation();
		$event.stopImmediatePropagation();
	};

	/**
	 * Do some basic adjustments on dates. Won't handle edge cases such as adding
	 * a month to Oct 31st etc -> will calculate DEc 1st in that case.
	 * @param  {[type]} dateLong [description]
	 * @param  {[type]} adjStr   [description]
	 * @return {[type]}          [description]
	 */
	Controller.prototype.calculateAdjustment = function(dateLong, adjStr){

		var num;
		var utilDate;

		if(/^-?[0-9]+m$/.test(adjStr)){
			num = adjStr.replace(/[A-z]/,'')*1;
			num = 60000 * num;
			dateLong = dateLong + num;
		}
		else if(/^-?[0-9]+h$/.test(adjStr)){
			num = adjStr.replace(/[A-z]/,'')*1;
			num = 60000*60 * num;
			dateLong = dateLong + num;
		}
		else if(/^-?[0-9]+d$/.test(adjStr)){
			num = adjStr.replace(/[A-z]/,'')*1;
			num = 60000*60*24* num;
			dateLong = dateLong + num;
		}
		else if(/^-?[0-9]+M$/.test(adjStr)){
			utilDate = new Date(dateLong);
			num = adjStr.replace(/[A-z]/,'')*1;
			utilDate.setMonth(utilDate.getMonth() + num);
			dateLong = utilDate.getTime();
			
		}
		else if(/^-?[0-9]+y$/.test(adjStr)){
			utilDate = new Date(dateLong);
			num = adjStr.replace(/[A-z]/,'')*1;
			utilDate.setFullYear(utilDate.getFullYear() + num);
			dateLong = utilDate.getTime();
			
		}

		return dateLong;

	}
	/**
	 * Set valid state on the dom element of our directive.
	 * For backwards compatibality look for
	 * ng-valid-validate | ng-invalid-validate
	 * @param {[type]} dateLong [description]
	 * @param {[type]} timeStr  [description]
	 */
	Controller.prototype.setValidity = function(dateLong,timeStr){

		if(this.isDateValid(dateLong) && this.isTimeValid(timeStr)){
			this.ngModelCtrl.$setValidity('validate', true);
		}
		else{
			this.ngModelCtrl.$setValidity('validate', false);
		}
		
	};

	/**
	 * Initialization function to seed all of our values based on the selectedDateTime reference
	 * the user has assigned on our scope (this is the ngModel reference on the outer exposed directive).
	 * Specifically, we need to take the ngModel value and build our internal values which will be used
	 * as the base for our ctrl.selectedDate and ctrl.timeComponent. If the user doesnt pass in a valid
	 * long as our value then we will build one from the current date.
	 * @return {[type]} [description]
	 */
	Controller.prototype.init = function(){
		var that = this;
		var currentDateLong;

		this.$timeout(function(){

			//if user fails to supply a value then init from current date time.
			if(!that.ngModelCtrl.$modelValue){

				if(that.$scope.defaultAdj){
					currentDateLong = (new Date()).getTime();
					currentDateLong = that.calculateAdjustment(currentDateLong,that.$scope.defaultAdj);
					that.ngModelCtrl.$modelValue = currentDateLong;
				}
				else{
					return;
				}
				
			}
			//if user references a date then convert to that dates long time value.
			else if(angular.isDate(that.ngModelCtrl.$modelValue)){
				that.ngModelCtrl.$modelValue = that.ngModelCtrl.$modelValue.getTime();
			}

			that.timeComponent = that.buildTimeString(that.ngModelCtrl.$modelValue,that.is24HourClock);
			that.updateView(that.ngModelCtrl.$modelValue, that.timeComponent);
		});
	}

	/**
	 * Helper function to build a valid time component string based on the dateLong value
	 * passed into the directive. 
	 * @param  {long} dateLong 
	 * @param  {boolean} is24Hour 
	 * @return {string}          
	 */
	Controller.prototype.buildTimeString = function(dateLong,is24Hour){

		var date = new Date(dateLong);
		var hours = date.getHours();
		var minutes = date.getMinutes();
		var result;
		var meridian = "";

		if(!is24Hour){
			if(hours > 12){
				hours = hours -12;
				meridian = this.meridianOptions.PM;
			}
			else{
				meridian = this.meridianOptions.AM;
			}
		}

		if(minutes < 10){
			minutes = "0" + minutes
		}

		result = hours + ":" + minutes + " " + meridian;
		return result;

	};

	/**
	 * This is the fx that tries to keep our ngModel value (selectedDateTime),
	 * synced with the changes occuring in our underlying values, timeComponent and selectedDate.
	 * Both sub components must be valid before the ngModel value will update. Important behavior to
	 * be aware of is that on any invalid dateLong or timeStr we will set our view value to null.
	 * @param  {[type]} dateLong [description]
	 * @param  {[type]} timeStr  [description]
	 * @return {[type]}          [description]
	 */
	Controller.prototype.updateView = function(dateLong,timeStr){

		var timeParts;
		var now; 
		
		//Special case where user just opens and closes the datePicker without selecting anything or
		//where the user has blanked out both values in the control. We will treat this like a reset
		//to the control and explicitly set validity to true and our view value to null.
		if(!dateLong && !timeStr){
			this.ngModelCtrl.$setViewValue(null);
			this.ngModelCtrl.$setValidity('validate', true);
			return
		};

		//Do we have a number we can use?
		if(!angular.isNumber(dateLong) || !isFinite(dateLong)){
			this.ngModelCtrl.$setViewValue(null);
			this.setValidity(dateLong,timeStr);
			return;
		}

		//Handle case where we have a dateLong but no timeStr by defaulting to the current time.
		if(!timeStr){
			now = (new Date()).getTime();
			this.timeComponent = this.buildTimeString(now,this.is24HourClock);
			timeStr = this.timeComponent;
		}

		this.setValidity(dateLong,timeStr);

		if(this.isDateValid(dateLong) && this.isTimeValid(timeStr)){
			timeParts = this.parseTimeString(timeStr);
			this.selectedDateTime = new Date(dateLong);
			this.selectedDateTime.setHours(timeParts.hour);
			this.selectedDateTime.setMinutes(timeParts.minutes);
			this.selectedDate = this.selectedDateTime.getTime();
			this.ngModelCtrl.$setViewValue(this.selectedDateTime.getTime() );
			this.ngModelCtrl.$render();
			if (angular.isFunction(this.ngModelCtrl.$apply)) {
				this.ngModelCtrl.$apply();
			}
		}
		else{
			this.ngModelCtrl.$setViewValue(null);
		}

	};


	/**
	 * Given a valid timeComponent string this will parse the string
	 * into a object containing the valid number of hours and minutes that
	 * the string represents. This is always computed as 24 hours, thus a
	 * valid string of "06:00 PM" will calculate hours as 18. 
	 * @param  {[type]} str [description]
	 * @return {[type]}     [description]
	 */
	Controller.prototype.parseTimeString = function(str){

		var result ={
			"hour" : 0,
			"minutes" : 0,
			"meridian" : undefined
		};
		var strParts;

		if(this.isTimeValid(str)){
			// assumes ':' always separates hours and minutes
			strParts = str.split(/\s|:/g);
			result.hour = 1*strParts[0];
			result.minutes = 1*strParts[1];

			if(this.is24HourClock===false && strParts.length === 3 ){
				result.meridian  = strParts[2];

				//handle case where 12AM needs to be converted to 0
				if(result.meridian === this.meridianOptions.AM && result.hour===12){
					result.hour = 0;
				}
				//handle the case where result is midday and no adjustment needed
				if(result.meridian === this.meridianOptions.PM && result.hour===12){
					result.hour = 12;
				}
				//now handle all other PM cases where we need to add 12 hours
				else if(result.meridian === this.meridianOptions.PM){
					result.hour = result.hour + 12;
				}
			}
		}

		result.hour = result.hour;

		return result;

	};

	/**
	 * Cheap but hopefully all we need to determine if we have a valid
	 * long representation of a date.
	 * @param  {[type]}  dateLong [description]
	 * @return {Boolean}          [description]
	 */
	Controller.prototype.isDateValid = function(dateLong){
		var result = (dateLong !=="" && isFinite(dateLong));
		return result;
	};

	/**
	 * Test a string representing a timeComponent to determine if 
	 * we have a valid representation as defined by our RX.
	 * @param  {[type]}  str [description]
	 * @return {Boolean}     [description]
	 */
	Controller.prototype.isTimeValid = function(str){
		var result = this.timePartRX.test(str);
		return result;
	};


})();
