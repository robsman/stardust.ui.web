/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

if (!window.bpm) {
	bpm = {};
}

if (!window.bpm.portal) {
	bpm.portal = {};
}

if (!window.bpm.portal.AngularAdapter) {
	bpm.portal.AngularAdapter = function AngularAdapter(options) {
		/**
		 * 
		 */
		AngularAdapter.prototype.initialize = function(angular) {
			this.angular = angular;
			this.angularModule = angular.module('angularApp', []);

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

			this.initalizeFormatConstraints();

			this.angular.bootstrap(document, [ 'angularApp' ]);

			// Is this correct?

			this.__defineGetter__('angularApp', function() {
				return this.angularModule;
			});
		};

		/**
		 * 
		 */
		AngularAdapter.prototype.mergeControllerWithScope = function(controller) {
			var scope = this.angular.element(document.body).scope();

			jQuery.extend(scope, controller);
			this.inheritMethods(scope, controller);

			scope.updateView = function() {
				this.$apply();
			};

			return scope;
		};

		/**
		 * Auxiliary method to copy all methods from the parentObject to the
		 * childObject.
		 */
		AngularAdapter.prototype.inheritMethods = function(childObject,
				parentObject) {
			for ( var member in parentObject) {
				if (parentObject[member] instanceof Function) {
					childObject[member] = parentObject[member];
				}
			}
		};

		/**
		 * 
		 */
		AngularAdapter.prototype.initalizeFormatConstraints = function() {
			console.debug("Format constraints initialized");

			var self = this;

			this.angularModule
					.directive(
							'required',
							function() {
								console.log("required parsed");
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

			console.log("Checking sd-date");

			this.angularModule.directive('sdDate', function($parse) {
				console.log("sd-date parsed");
				return function(scope, element, attrs, controller) {
					var ngModel = $parse(attrs.ngModel);
					$(function() {
						element.datepicker({
							inline : true,
							dateFormat : 'dd.mm.yy',
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
		};
	}
}