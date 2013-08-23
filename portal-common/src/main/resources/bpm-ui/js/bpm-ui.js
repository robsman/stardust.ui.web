/**
 * @author Subodh.Godbole
 */

define(function () {
	'use strict';

	var bpmUiRMod = {};

	var bpmUiAMod = angular.module('bpm-ui', ['bpm-ui.services', 'shell', 'bpm-ui.init']);
	bpmUiRMod.module = bpmUiAMod;

	var bpmUiServicesAMod = angular.module('bpm-ui.services', []);
	bpmUiRMod.services = bpmUiServicesAMod;

	bpmUiAMod.config(['sgViewPanelServiceProvider', function(sgViewPanelServiceProvider) {
		sgViewPanelServiceProvider.setViewOpeningStrategy('mdi');
	}]);

	// Taken From - http://jsfiddle.net/cn8VF/
	// This is to delay model updates till element is in focus
	bpmUiRMod.module.directive('ngModelOnblur', function() {
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
	
	angular.module('bpm-ui.init', []) .
		run(['$log', '$rootScope', '$window', 'sgPubSubService', function ($log, $rootScope, $window, sgPubSubService) {
			// execute here the code that needs to be executed before the app starts.
			$log.log('- BPM UI started!');
			$rootScope.$on('sgLoginRequired', function (event) {
				var href = $window.location.href.substr(0, window.location.href.indexOf('#'));
				$window.location.replace(href);
			});

			// sgPubSubService is required in BridgeUtils. But BridgeUtils is not loaded at this point
			// Also BridgeUtils runs is non Angular Context, so save data in root
			$rootScope.sgPubSubService = sgPubSubService;
		}]);

	return bpmUiRMod;
});