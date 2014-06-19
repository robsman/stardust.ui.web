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

	/*
	 * Copy of ngInclude, with addition to have onfail handler 
	 */
	bpmUiRMod.module.directive('sdInclude', ['$http', '$templateCache', '$anchorScroll', '$animate', '$sce',
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