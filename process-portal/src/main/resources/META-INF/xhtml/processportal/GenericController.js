/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

if (!window.bpm) {
	bpm = {};
}

if (!window.bpm.portal) {
	bpm.portal = {};
}

if (!window.bpm.portal.GenericController) {
	/**
	 * Manages all input and output data for UIMashup Application via AngularJS.
	 *
	 */
	bpm.portal.GenericController = function GenericController() {

		/**
		 *
		 */
		GenericController.prototype.bind = function(angular, interaction,
				options) {
			this.angular = angular;
			this.interaction = interaction;
			this.options = options;

			if (!this.options) {
				this.options = {};
			}

			this.validationErrorMessageDialog = jQuery("#validationErrorMessageDialog");

			this.validationErrorMessageDialog.dialog({
				modal : true,
				autoOpen : false,
				closeOnEscape : true,
				buttons : {
					Ok : function() {
						jQuery(this).dialog("close");
					}
				}
			});

			this.angularModule = angular.module('angularApp', []);

			// Required to bind images in UI

			this.baseUrl = this.interaction.baseUrl;

			this.errors = [];

			this.initalizeFormatConstraints();

			var self = this;

			this.angularModule
					.directive(
							'ngModel',
							function() {
								return {
									link : function(scope, elm, attr,
											ngModelCtrl) {
										self.initalizePath(attr.ngModel);

										elm
												.bind(
														'blur',
														function(event) {
															scope
																	.$apply(function() {
																		var key = attr.ngModel
																				.split(".")[0];

																		if (key === "$tableIterator" || key === "$listIterator") {
																			// Change
																			// is
																			// coming
																			// from
																			// a
																			// table
																			// iterator

																			var attributes;
																			if (key === "$tableIterator") {
																				attributes = event.target.parentNode.parentNode.attributes;
																			} else {
																				attributes = event.target.parentNode.parentNode.parentNode.attributes;
																			}

																			for (n in attributes) {
																				if (attributes[n].name === "ng-repeat") {
																					var tokens = attributes[n].value
																							.split(" ");

																					self
																							.postSingle(tokens[2]
																									.split(".")[0]);

																					break;
																				}
																			}
																		} else {
																			self
																					.postSingle(attr.ngModel
																							.split(".")[0]);
																		}
																	});
														});
									}
								};
							});

			// Taken From - http://jsfiddle.net/cn8VF/
			// This is to delay model updates till element is in focus

			this.angularModule.directive('ngModelOnblur', function() {
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

			this.angular.bootstrap(document, [ 'angularApp' ]);

			// Is this correct?

			this.__defineGetter__('angularApp', function() {
				return this.angularModule;
			});

			// Copy data from Interaction object

			for (x in this.interaction.transfer) {
				console.log("Copying " + x);
				console.log(this.interaction.transfer[x]);

				this[x] = this.interaction.transfer[x];
				if (!isPrimitive(this[x])) {
					if (isValidObject(this[x])) {
						processTransferObject(this[x]);
					} else {
						this[x] = ""; // This is not valid Object, then it must be empty primitive
					}
				}
			}

			this.mergeControllerWithScope();
			this.updateView();
		};

		/*
		 * Blank out empty primitives, so that on UI it won't show [object object]
		 * 
		 */
		function processTransferObject(obj) {
			for(var key in obj) {
				var vp = isValidParam(key);
				if (vp) {
					if (!isPrimitive(obj[key])) {
						if (obj[key].length) { // Array
							for(var i in obj[key]) {
								processTransferObject(obj[key][i]);
							}
						} else { // Object
							if (isValidObject(obj[key])) {
								processTransferObject(obj[key]);
							} else {
								obj[key] = ""; // This is not valid Object, then it must be empty primitive
							}
						}
					}
				}
			}
		};

		/*
		 * 
		 */
		function isValidParam(attr) {
			return (attr.indexOf("_") != 0 && !endsWith(attr, "_asArray"));
		}

		/*
		 * 
		 */
		function endsWith(str, subStr) {
			return str.lastIndexOf(subStr) == str.length - 8;
		}

		/*
		 * 
		 */
		function isPrimitive(obj) {
			if (typeof obj != "object" || typeof obj == "string") {
				return true;
			} else {
				for(var key in obj) {
					if (key == "toString" && typeof obj[key] == "function") {
						return true;
					}
				}
			}
			return false;
		}

		/*
		 * 
		 */
		function isValidObject(obj) {
			for(var key in obj) {
				if (isValidParam(key)) {
					return true;
				}
			}
			return false;
		}

		/**
		 *
		 */
		GenericController.prototype.initalizePath = function(path) {
			path = path.split(".");

			var object = this;

			for ( var n = 0; n < path.length - 1; ++n) {
				if (object[path[n]] == null) {
					console.log("Creating " + path[n]);

					object[path[n]] = {};

					// if (n == 0) {
					// scope = this.angular.element(document.body).scope()[path]
					// = object[path];
					// }
				}

				object = object[path[n]];
			}
		};

		/**
		 *
		 */
		GenericController.prototype.generateLabelForPath = function(path) {
			path = path.split(".");

			var label = "";

			for ( var i = 0; i < path.length; ++i) {
				if (i > 0) {
					label += "/";
				}

				var identifier = path[i];
				var previousCharacter = identifier.charAt(0);

				label += previousCharacter.toUpperCase();

				identifier = identifier.slice(1);

				for ( var n = 0; n < identifier.length; ++n) {
					if (isLowerCase(previousCharacter)
							&& isUpperCase(identifier.charAt(n))) {
						label += " ";
					}

					label += identifier.charAt(n);
					previousCharacter = identifier.charAt(n);
				}
			}

			return label;
		};

		/**
		 *
		 */
		GenericController.prototype.initalizeFormatConstraints = function() {
			var self = this;

			this.angularModule
					.directive(
							'required',
							function() {
								return {
									require : 'ngModel',
									link : function(scope, elm, attrs, ctrl) {
										ctrl.$parsers
												.unshift(function(viewValue) {
													self.removeError(
															attrs.ngModel,
															"required");

													if (viewValue == null
															|| viewValue == "") {
														self.errors
																.push({
																	path : attrs.ngModel,
																	type : "required",
																	message : "Value required ("
																			+ self
																					.generateLabelForPath(attrs.ngModel)
																			+ ")."
																});

														ctrl.$setValidity(
																'required',
																false);

														return undefined;
													} else {
														ctrl.$setValidity(
																'required',
																true);

														return viewValue;
													}
												});
									}
								};
							});

			var INTEGER_REGEXP = /^\-?\d*$/;

			this.angularModule
					.directive(
							'sdInteger',
							function() {
								return {
									require : 'ngModel',
									link : function(scope, elm, attrs, ctrl) {
										ctrl.$parsers
												.unshift(function(viewValue) {
													self.removeError(
															attrs.ngModel,
															"sdInteger");

													if (INTEGER_REGEXP
															.test(viewValue)) {
														ctrl.$setValidity(
																'sdInteger',
																true);

														return viewValue;
													} else {
														ctrl.$setValidity(
																'sdInteger',
																false);

														self.errors
																.push({
																	path : attrs.ngModel,
																	type : "sdInteger",
																	message : "Invalid Integer Format ("
																			+ self
																					.generateLabelForPath(attrs.ngModel)
																			+ ")."
																});

														return undefined;
													}
												});
									}
								};
							});

			var DECIMAL_REGEXP = /^\-?\d+((\.|\,)\d+)?$/;

			this.angularModule
					.directive(
							'sdDecimal',
							function() {
								return {
									require : 'ngModel',
									link : function(scope, elm, attrs, ctrl) {
										ctrl.$parsers
												.unshift(function(viewValue) {
													self.removeError(
															attrs.ngModel,
															"sdDecimal");

													if (DECIMAL_REGEXP
															.test(viewValue)) {
														ctrl.$setValidity(
																'sdDecimal',
																true);

														return parseFloat(viewValue
																.replace(',',
																		'.'));
													} else {
														ctrl.$setValidity(
																'sdDecimal',
																false);

														self.errors
																.push({
																	path : attrs.ngModel,
																	type : "sdDecimal",
																	message : "Invalid Decimal Format ("
																			+ self
																					.generateLabelForPath(attrs.ngModel)
																			+ ")."
																});

														return undefined;
													}
												});
									}
								};
							});

			// Date Picker
			this.angularModule.directive('sdDate', function($parse) {
				return {
					require : 'ngModel',
					link : function(scope, element, attrs, controller) {
						var ngModel = $parse(attrs.ngModel);
						$(function() {
							element.datepicker({
								inline : true,
								dateFormat : 'yy-mm-dd',
								onSelect : function(dateText, inst) {
									scope.$apply(function(scope) {
										// Change binded variable
										ngModel.assign(scope, dateText);
										self
												.postSingle(attrs.ngModel
														.split(".")[0]);
									});
								}
							});
						});
						
						controller.$formatters.unshift(function(viewValue) {
							// Strip off Time Part
							// TODO: Support Time
							if (viewValue && viewValue != null && viewValue != "") {
								var dateParts = getPrimitiveValue(viewValue).split("T");
								if (dateParts.length >= 1) {
									viewValue = dateParts[0];
								}
							}
							
							return viewValue;
						});
					}
				};
			});

			// Data Table
			this.angularModule.directive('dataTable', function() {
				return function(scope, element, attrs) {
					console.log("Initialize data table");

					options = {
						"bStateSave" : true,
						"iCookieDuration" : 2419200, /* 1 month */
						"bJQueryUI" : true,
						"bPaginate" : false,
						"bLengthChange" : false,
						"bFilter" : false,
						"bInfo" : false,
						"bDestroy" : true
					};

					var dataTable = element.dataTable(options);

					scope.$watch(attrs.rowData, function(value) {
						var val = value || null;

						if (val) {
							dataTable.fnClearTable();
							dataTable.fnAddData(scope.$eval(attrs.rowData));
						}
					});
				};
			});

			this.angularModule.directive('sdValidate', function() {
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
										if (validate === "byte" || validate === "short" || validate === "integer") {
											val = new Number(value);
											if (!isNaN(val)) {
												if (validate === "byte") {
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
												}
											}
										} else if (validate === "duration") {
											var parts = value.split(":");
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
		};

		/*
		 * 
		 */
		function getPrimitiveValue(value) {
			if (typeof value == "object" && value.toString) {
				return value.toString();
			}
			return value;
		}
		
		/**
		 * Bind all fields of GenericController to Angular scope.
		 */
		GenericController.prototype.removeError = function(path, type) {
			for ( var n = 0; n < this.errors.length; ++n) {
				if (this.errors[n].path === path
						&& this.errors[n].path === path) {

					this.errors.splice(n, 1);
				}
			}
		};

		/**
		 * Bind all fields of GenericController to Angular scope.
		 */
		GenericController.prototype.mergeControllerWithScope = function() {
			var scope = this.angular.element(document.body).scope();

			jQuery.extend(scope, this);

			console.log("Scope (without methods)");
			console.log(scope);

			this.inheritMethods(scope, this);
		};

		/**
		 * Force Angular to update the HTML View.
		 */
		GenericController.prototype.updateView = function() {
			this.angular.element(document.body).scope().$apply();
		};

		/*
		 * 
		 */
		GenericController.prototype.addToList = function(list) {
			if (list != undefined) {
				list.push({});
			}
		};

		/*
		 * 
		 */
		GenericController.prototype.selectListItem = function(event, obj) {
			// Select if target is Row/Column 
			if (event.target.localName.toLowerCase() == "td" || event.target.localName.toLowerCase() == "tr") {
				if (obj.$$selected == undefined || obj.$$selected == false) {
					obj.$$selected = true;
				} else {
					obj.$$selected = false;
				}
			}
		};

		/*
		 * 
		 */
		GenericController.prototype.removeFromList = function(list) {
			if (list) {
				removeSelectedElements(list);
				if (list.length == 0) {
					list.push({});
				}
			}
		};

		/*
		 * 
		 */
		function removeSelectedElements(arr) {
			for(var i = 0 ; i < arr.length; i++) {
				if (arr[i].$$selected) {
					arr.splice(i, 1);
					removeSelectedElements(arr);
					break;
				}
			}
		};
		
		/**
		 * Add row to "to many"-table.
		 */
		GenericController.prototype.addRow = function(event) {
			var path = this.getArrayPath(event);
			var array = this.getArrayForPath(path);

			if (array) {
				array.push({});
			}

			console.log("Array path " + path);

			this.postSingle(path.split(".")[0]);
		};

		/**
		 * Delete row from "to many"-table.
		 */
		GenericController.prototype.deleteRow = function(event, index) {
			var path = this.getArrayPath(event);
			var array = this.getArrayForPath(path);

			if (array) {
				array.splice(index, 1);
			}

			console.log("Array path " + path);

			this.postSingle(path.split(".")[0]);
		};

		/**
		 *
		 */
		GenericController.prototype.getArrayPath = function(event) {
			var attributes = event.target.parentNode.attributes;
			var path = null;

			for (n in attributes) {
				if (attributes[n].name === "path") {
					return attributes[n].nodeValue;
				}
			}
		};

		/**
		 *
		 */
		GenericController.prototype.getArrayForPath = function(path) {
			path = path.split(".");

			var object = this;

			for ( var i = 0; i < path.length; ++i) {
				if (object[path[i]] == null) {
					// If only the last element - the array - is null, create it

					if (i == path.length - 1) {
						object[path[i]] = [];
					} else {
						object[path[i]] = {};
					}
				}

				object = object[path[i]];
			}

			return object;
		};

		/**
		 * Auxiliary method to copy all methods from the parentObject to the
		 * childObject.
		 */
		GenericController.prototype.inheritMethods = function(childObject,
				parentObject) {
			for ( var member in parentObject) {
				if (parentObject[member] instanceof Function) {
					childObject[member] = parentObject[member];
				}
			}
		};

		/**
		 * Copy all top-level fields of the controller which are not internal
		 * back to the Interaction object.
		 *
		 * TODO: Currently not used as all posts are initiated on single objects
		 */
		GenericController.prototype.post = function() {
			for (x in this) {
				if (!(this[x] instanceof Function) && x.indexOf("$") != 0
						&& this[x] != this && x !== "interaction"
						&& x !== "options" && x != "angular"
						&& x != "angularModule" && x != "angularApp"
						&& x != "baseUrl" && x != "errors"
						&& x != "validationErrorMessageDialog") {
					this.interaction.transfer[x] = this[x];
				}
			}
		};

		/**
		 *
		 */
		GenericController.prototype.postSingle = function(key) {
			console.log("Posting element " + key);
			console.log(this[key]);

			this.interaction.transfer = {};

			var toPost;
			if (isPrimitive(this[key])) {
				toPost = this[key];
			} else {
				// Need a deep copy in order to be able to remove internal variables
				toPost = jQuery.extend(true, {}, this[key]);
			}
				
			this.interaction.transfer[key] = toPost;

			// Cleanup internal ($) variables

			this.removeInternalVariables(this.interaction.transfer[key]);

			// TODO Add comparison to eliminate post if nothing has changed

			this.interaction.post();
		};

		/**
		 * Removes all internal Angular variables ($.*) e.g. used for table
		 * iteration.
		 */
		GenericController.prototype.removeInternalVariables = function(object) {
			for (key in object) {
				if (key.indexOf("$") == 0) {
					delete object[key];
				} else if (object[key] != null) {
					// http://perfectionkills.com/instanceof-considered-harmful-or-how-to-write-a-robust-isarray/

					if (Object.prototype.toString.call(object[key]) === "[object Array]") {
						var arr = object[key];
						for ( var n in arr) {
							this.removeInternalVariables(arr[n]);
						}
					} else if (typeof object[key] == "object") {
						this.removeInternalVariables(object[key]);
					}
				}
			}
		};

		/**
		 *
		 */
		GenericController.prototype.completeActivity = function() {
			if (this.customValidation) {
				this.customValidation();
			}

			if (this.errors.length > 0) {
				this.validationErrorMessageDialog.dialog("open");

				return;
			}

			this.interaction.completeActivity();
		};

		/**
		 *
		 */
		GenericController.prototype.suspendActivity = function() {
			this.interaction.suspendActivity();
		};

		/**
		 *
		 */
		GenericController.prototype.abortActivity = function() {
			this.interaction.abortActivity();
		};

		/**
		 *
		 */
		GenericController.prototype.qaPassActivity = function() {
			this.interaction.qaPassActivity();
		};

		/**
		 *
		 */
		GenericController.prototype.qaFailActivity = function() {
			this.interaction.qaFailActivity();
		};
	};

	/**
	 *
	 */
	function isLowerCase(c) {
		if (c >= 'a' && c <= 'z') {
			return true;
		}

		return false;
	}

	/**
	 *
	 */
	function isUpperCase(c) {
		if (c >= 'A' && c <= 'Z') {
			return true;
		}

		return false;
	}
}