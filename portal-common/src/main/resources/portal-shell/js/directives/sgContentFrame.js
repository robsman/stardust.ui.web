/*
 * 
 */
'use strict';

angular.module('shell').directive('sgStretchToBottom',['$window', '$timeout', function($window, $timeout) {
    return {
        restrict : 'A',
        replace : false,
        link: function(scope, element, attrs, sgReinit) {
            var shellSizes = {};

            function calc() {
                var elemOffset = element.offset();
                if(elemOffset.top < shellSizes.windowHeight) {
                	var height = shellSizes.windowHeight - elemOffset.top - shellSizes.footerHeight;
                	if (height > 0) {
                		element.outerHeight(height);
                	} else {
                		setTimeout(calc, 200); // If height is zero or less retry
                	}
                }
            }

            scope.$watch('shell.sizes', function(sizes) {
                shellSizes = sizes;
                if(shellSizes !== {}){
                    calc();
                }
            }, true);
        }
    };
}]);

angular.module('shell').directive('sgContentFrame', function () {
    return {
        template : '<iframe ng-src="{{getContentFrameURL()}}" sg-stretch-to-bottom></iframe>',
        restrict: 'A',
        replace: true,
        link: function (scope, element, attrs) {
            element.addClass('content-frame');
            scope.getContentFrameURL = function(){
              return attrs.sgSrc;
          };
        }
    };
});
