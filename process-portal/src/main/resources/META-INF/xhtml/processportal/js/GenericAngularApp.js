/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/*
 * @author subodh.godbole
 */

if (!window.bpm) {
	bpm = {};
}

if (!window.bpm.portal) {
	bpm.portal = {};
}

if (!window.bpm.portal.GenericAngularApp) {

	bpm.portal.GenericAngularApp = function GenericAngularApp(options) {
		
		var SERVER_DATE_FORMAT = "yy-mm-dd";
		var SERVER_DATE_TIME_FORMAT_SEPARATOR = "T";

		var angularCompile;

		/*
		 *
		 */
		GenericAngularApp.prototype.initialize = function() {
			var angularModule = angular.module(options.module, []);
			
			// Taken From - http://jsfiddle.net/cn8VF/
			// This is to delay model updates till element is in focus
			angularModule.directive('ngModelOnblur', function() {
				return {
					restrict : 'A',
					require : 'ngModel',
					link : function(scope, elm, attr, ngModelCtrl) {
						if (attr.type === 'radio' || attr.type === 'checkbox') {
							return;
						}
						elm.unbind('input').unbind('keydown').unbind('change');
						elm.bind('blur', function() {
							scope.$apply(function() {
								ngModelCtrl.$setViewValue(elm.val());
							});
						});
					}
				};
			});

			angularModule.directive('sdDate', function($parse) {
				return {
					require : 'ngModel',
					link : function(scope, elm, attr, ctrl) {
						var ngModel = $parse(attr.ngModel);
						jQuery(function() {
								elm.datepicker({
									dateFormat : options.dateFormat,
									onSelect : function(dateText, inst) {
										scope.$apply(function(scope) {
											ngModel.assign(scope, dateText);
											ctrl.$setValidity('date', true);
										});
									}
							});
						});

						ctrl.$parsers.unshift(function(value) {
							var success = false;
							try {
								jQuery.datepicker.parseDate(options.dateFormat, value);
								success = true;
							} catch(e) {
							}

							if (success) {
								ctrl.$setValidity('date', true);
								return value;
							} else {
								ctrl.$setValidity('date', false);
								return value;
							}
						});
					}
				};
			});

			angularModule.directive('sdValidate', function() {
				return {
					require : 'ngModel',
					link : function(scope, elm, attr, ctrl) {
						var validate = attr.sdValidate;
						if (validate) {
							var validatorFunc = function(value, toUI) {
								var success = false;
								var val;

								if (value == undefined || value == null || value == "") {
									success = true;
								} else {
									try {
										var _value = "" + value; // Convert to String. Below code expects it to be String 

										if (validate === "byte" || validate === "short" || validate === "integer" || validate === "long") {
											val = new Number(_value);
											if (!isNaN(val)) {
												if (_value.indexOf(".") >= 0) {
													// Decimal no. Invalid
												} else if (validate === "byte") {
													if (val >= -128 && val <= 127) {
														success = true;
													}
												} else if (validate === "short") {
													if (val >= -32768 && val <= 32767) {
														success = true;
													}
												} else if (validate === "integer") {
													if (val >= -2147483648 && val <= 2147483647) {
														success = true;
													}
												} else if (validate === "long") {
													if (val >= -9223372036854775808 && val <= 9223372036854775807) {
														success = true;
													}
												}
											}
										} else if (validate === "duration") {
											var parts = _value.split(":");
											if (parts.length == 6) {
												success = true;
												for(var i in parts) {
													val = new Number(parts[i]);
													if (!isNaN(val)) {
														if (val < -32768 || val > 32767) {
															success = false;
															break;
														}
													} else {
														success = false;
														break;
													}
												}
											}
										} else {
											success = true;
										}
									} catch(e) {
									}
								}

								if (success) {
									ctrl.$setValidity('validate', true);
									return value;
								} else {
									ctrl.$setValidity('validate', false);
									return undefined;
								}
							};

							ctrl.$formatters.unshift(function(value) {
								return validatorFunc(value, true);
							});
							ctrl.$parsers.unshift(function(value) {
								return validatorFunc(value, false);
							});
						}
					}
				};
			});

			angularModule.filter('sdFilterDate', function() {
				return function(value) {
					// Convert to Client Format
					if (value && value != null && value != "") {
						var datePart;
						var dateParts = value.split(SERVER_DATE_TIME_FORMAT_SEPARATOR); // Get 2 Parts
						if (dateParts.length >= 1) {
							datePart = formatDate(dateParts[0], SERVER_DATE_FORMAT, options.dateFormat);
						}

						value = datePart;
					}
					return value;
				};
			});

			angularModule.filter('sdFilterDateTime', function() {
				return function(value) {
					// Convert to Client Format
					if (value && value != null && value != "") {
						var datePart;
						var timePart;
						var dateParts = value.split(SERVER_DATE_TIME_FORMAT_SEPARATOR); // Get 2 Parts
						if (dateParts.length >= 1) {
							datePart = formatDate(dateParts[0], SERVER_DATE_FORMAT, options.dateFormat);
						}
						if (dateParts.length >= 2) {
							var timeParts = dateParts[1].split(":"); // Get 3 Parts, and stripoff seconds part
							timePart = timeParts[0] + ":" + timeParts[1];
						}

						value = datePart + " " + timePart;
					}
					return value;
				};
			});

			angularModule.filter('sdFilterTime', function() {
				return function(value) {
					// Convert to Client Format
					if (value && value != null && value != "") {
						var timePart;
						var dateParts = value.split(SERVER_DATE_TIME_FORMAT_SEPARATOR); // Get 2 Parts
						if (dateParts.length >= 2) {
							var timeParts = dateParts[1].split(":"); // Get 3 Parts, and stripoff seconds part
							timePart = timeParts[0] + ":" + timeParts[1];
						}

						value = timePart;
					}
					return value;
				};
			});

			angularModule.controller(options.ctrl, function($compile) {
				angularCompile = $compile;
			});

			angularModule.directive('ngRightClick', function($parse) {
			    return function(scope, element, attrs) {
			        var fn = $parse(attrs.ngRightClick);
			        element.bind('contextmenu', function(event) {
			            scope.$apply(function() {
			                event.preventDefault();
			                fn(scope, {$event:event});
			            });
			        });
			    };
			});	
			
			angular.bootstrap(document, [options.module]);				
		};

		/*
		 * 
		 */
		GenericAngularApp.prototype.getCompiler = function() {
			return angularCompile;
		};

		/*
		 * 
		 */
		function formatDate(value, fromFormat, toFormat) {
			if (value != undefined && value != null && value != "") {
				try {
					var date = jQuery.datepicker.parseDate(fromFormat, value);
					value = jQuery.datepicker.formatDate(toFormat, date);
				} catch(e) {
					log(e);
				}
			}
			return value;
		}
	};
}
