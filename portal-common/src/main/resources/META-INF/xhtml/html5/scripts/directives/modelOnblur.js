/**
 * @author Subodh.Godbole
 */

'use strict';

// Taken From - http://jsfiddle.net/cn8VF/
// This is to delay model updates till element is in focus
angular.module('bpm-ui').directive('ngModelOnblur', function() {
    return {
        restrict: 'A',
        require: 'ngModel',
        priority: 1,
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