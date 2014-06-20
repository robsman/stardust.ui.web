/**
 * @author Subodh.Godbole
 */

define(['bpm-ui/js/bpm-ui'], function (bpmUi) {
	'use strict';

	// Taken From - http://jsfiddle.net/cn8VF/
	// This is to delay model updates till element is in focus
	bpmUi.module.directive('ngModelOnblur', function() {
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
});