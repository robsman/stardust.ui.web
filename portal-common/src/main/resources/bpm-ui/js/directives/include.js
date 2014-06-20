/**
 * @author Subodh.Godbole
 */

define(['bpm-ui/js/bpm-ui'], function (bpmUi) {
	'use strict';

	/*
	 * Copy of ngInclude, with addition to have onfail handler 
	 */
	bpmUi.module.directive('sdInclude', ['$http', '$templateCache', '$anchorScroll', '$animate', '$sce',
		function($http, $templateCache, $anchorScroll, $animate, $sce) {
			return {
				restrict: 'ECA',
				priority: 400,
				terminal: true,
				transclude: 'element',
				controller: angular.noop,
				compile: function(element, attr) {
				  var srcExp = attr.sdInclude || attr.src,
				      onloadExp = attr.onload || '',
				      onfailExp = attr.onfail || '',
				      autoScrollExp = attr.autoscroll;
				
				  return function(scope, $element, $attr, ctrl, $transclude) {
				    var changeCounter = 0,
				        currentScope,
				        previousElement,
				        currentElement;
				
				    var cleanupLastIncludeContent = function() {
				      if(previousElement) {
				        previousElement.remove();
				        previousElement = null;
				      }
				      if(currentScope) {
				        currentScope.$destroy();
				        currentScope = null;
				      }
				      if(currentElement) {
				        $animate.leave(currentElement, function() {
				          previousElement = null;
				        });
				        previousElement = currentElement;
				        currentElement = null;
				      }
				    };
				
				    scope.$watch($sce.parseAsResourceUrl(srcExp), function sdIncludeWatchAction(src) {
				      var afterAnimation = function() {
				        if (angular.isDefined(autoScrollExp) && (!autoScrollExp || scope.$eval(autoScrollExp))) {
				          $anchorScroll();
				        }
				      };
				      var thisChangeId = ++changeCounter;
				
				      if (src) {
				        $http.get(src, {cache: $templateCache}).success(function(response) {
				          if (thisChangeId !== changeCounter) return;
				          var newScope = scope.$new();
				          ctrl.template = response;
				
				          // Note: This will also link all children of ng-include that were contained in the original
				          // html. If that content contains controllers, ... they could pollute/change the scope.
				          // However, using ng-include on an element with additional content does not make sense...
				          // Note: We can't remove them in the cloneAttchFn of $transclude as that
				          // function is called before linking the content, which would apply child
				          // directives to non existing elements.
				          var clone = $transclude(newScope, function(clone) {
				            cleanupLastIncludeContent();
				            $animate.enter(clone, null, $element, afterAnimation);
				          });
				
				          currentScope = newScope;
				          currentElement = clone;
				
				          currentScope.$emit('$includeContentLoaded');
				          scope.$eval(onloadExp);
				        }).error(function(data, status, headers, config) {
				          if (thisChangeId === changeCounter) cleanupLastIncludeContent();
				          scope.$eval(onfailExp);
				        });
				        scope.$emit('$includeContentRequested');
				      } else {
				        cleanupLastIncludeContent();
				        ctrl.template = null;
				      }
				    });
				  };
			}
		};
	}]).directive('sdInclude', ['$compile', function($compile) {
	    return {
	      restrict: 'ECA',
	      priority: -400,
	      require: 'sdInclude',
	      link: function(scope, $element, $attr, ctrl) {
	        $element.html(ctrl.template);
	        $compile($element.contents())(scope);
	      }
	    };
	}]);
});