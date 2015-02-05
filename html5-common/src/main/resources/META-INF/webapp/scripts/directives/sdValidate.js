/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Subodh.Godbole (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * @author Subodh.Godbole
 */

(function(){
	'use strict';

	angular.module('bpm-common').directive('sdValidate', [ValidateDirective]);

	/*
	 * 
	 */
	function ValidateDirective() {
		return {
			require : 'ngModel',
			link : function(scope, elm, attr, ctrl) {
				var validate = attr.sdValidate;
				if (validate) {
					var validatorFunc = function(value, toUI) {
						var success = false;

						if (value == undefined || value == null || value == "") {
							success = true;
						} else {
							success = performValidation(validate, value);
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

		/*
		 * 
		 */
		function performValidation(dataType, value) {
			var success = false;

			try {
				var val;
				var _value = "" + value; // Convert to String. Below code expects it to be String 

				if (dataType === "byte" || dataType === "short" || dataType === "integer" || dataType === "long") {
					val = new Number(_value);
					if (!isNaN(val)) {
						if (_value.indexOf(".") >= 0) {
							// Decimal no. Invalid
						} else if (dataType === "byte") {
							if (val >= -128 && val <= 127) {
								success = true;
							}
						} else if (dataType === "short") {
							if (val >= -32768 && val <= 32767) {
								success = true;
							}
						} else if (dataType === "integer") {
							if (val >= -2147483648 && val <= 2147483647) {
								success = true;
							}
						} else if (dataType === "long") {
							if (val >= -9223372036854775808 && val <= 9223372036854775807) {
								success = true;
							}
						}
					}
				} else if (dataType === "duration") {
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

			return success;
		}
	}
})();