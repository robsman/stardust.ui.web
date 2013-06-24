/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Subodh.Godbole
 */
'use strict';

// Main App
define(['angularjs'],
	function CommonApp(angular) {
		var angularModule = angular.module('angularApp', []);

		// Taken From - http://jsfiddle.net/cn8VF/
		// This is to delay model updates till element is in focus
		angularModule.directive('ngModelOnblur', function() {
		    return {
		        restrict: 'A',
		        require: 'ngModel',
		        link: function(scope, elm, attr, ngModelCtrl) {
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

		var app = {};

		app.init = function(div) {
			if (!div) {
				angular.bootstrap(document, ['angularApp']);
			} else if (!jQuery(div).hasClass("ng-scope")) {
				angular.bootstrap(div, ['angularApp']);
			}
		};

		app.__defineGetter__('angularApp', function() {
			return angularModule;
		});

		return app;
	});
