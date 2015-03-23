/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * @author Nikhil.Gahlot
 */

(function(){
	'use strict';

	angular.module('bpm-common').directive('sdPopover', ['sdLoggerService','$parse', '$timeout', PopoverDirective]);

	var trace;
	
	/*
	 * Directive class sd-popover="someObject"
	 * Attributes supported:
	 * 		sda-on-open
	 * 		ng-disabled
	 * Usage:
	 * 	<span sd-popover sda-on-open="...">
	 *		<i class="glyphicon glyphicon-chevron-right glyphicon-rotate-90"></i>
	 *		<div class="popover-body">
	 *			popover body ...
	 *		</div>
	 *	</span>
	 *  
	 */
	function PopoverDirective(sdLoggerService, $parse, $timeout) {
		
		trace = sdLoggerService.getLogger('bpm-common.sdPopover');
		
		var directiveDefObject = {
				restrict : 'AE',
				scope: true,
				transclude: true,
				template: ' <span class="popover-btn"></span>'
						+ ' <div ng-show="showPopover" class="popover-body-container popup-dlg" style="cursor:auto; position:fixed;"></div>',
				compile: PopoverCompilerFn
			};
		
		/*
		 *Compiler Function class
		 */
		function PopoverCompilerFn(elem, attr, transcludeFn) {
			elem.addClass('sd-popover');
			
			var popoverBodyContainer = elem.find('.popover-body-container');
			var popoverBtn = elem.find('.popover-btn');
			
			return function (scope, element, attrs) { // Link Function
				
				var onOpenFn = $parse(attrs.sdaOnOpen);
				
				transcludeFn(scope, function(clone) {
					var popoverBody = clone.filter('.popover-body');
					popoverBody.detach();
					popoverBtn.append(clone);
					element.prepend(popoverBtn);
					popoverBodyContainer.append(popoverBody);
				});
				
				elem.bind('click', function(event) {
					handlePopoverClick(event);
				});
				
				$timeout(function() {
					scope.$apply(function() {
						exposeAPI(attrs.sdPopover, scope);
					});
				});
				
				function handlePopoverClick(clkEvent, clickElem) {
					// In case of ng-disabled, make sure the click is not activated
					var disabled = false;
					if (angular.isDefined(attrs.ngDisabled)) {
						disabled = parseAttribute(scope.$parent, attrs.ngDisabled);
					}
					
					if (disabled != true) {
						
						if (angular.isDefined(onOpenFn)) {
							onOpenFn(scope);
						}
						
						// Handle close by click on document event
						var popoverCloseEvent = function(event) {
							if (elem.find(event.target).length === 0) {
								scope.$apply(function() {
									scope.showPopover = false;
									// this is important since we want this to be called exactly once
									$(document).unbind('click', popoverCloseEvent);
								});
							}
						};
						$(document).bind('click', popoverCloseEvent);
						
						scope.$apply(function() {
							if (!angular.isDefined(clickElem)) {
								clickElem = popoverBtn;
							}
							if (clkEvent.target === clickElem || clickElem.find(clkEvent.target).length > 0) {
								scope.showPopover = !scope.showPopover;
								if (scope.showPopover === false) {
									// this is important since we want this to be called exactly once
									$(document).unbind('click', popoverCloseEvent);
								} else if (scope.showPopover === true) {
									popoverBodyContainer.css({'visibility': 'hidden'});
									$timeout(function() {
										var xPos = clkEvent.pageX - 5;
										var yPos = clkEvent.pageY + 5;
										
										var maxAllowedXPos = jQuery(window).width() - popoverBodyContainer.outerWidth();
										var maxAllowedYPos = jQuery(window).height() - popoverBodyContainer.outerHeight();
										
										if (xPos > maxAllowedXPos) {
											xPos = maxAllowedXPos;
										}
										if (yPos > maxAllowedYPos) {
											yPos = maxAllowedYPos;
										}
										popoverBodyContainer.css({left: xPos + 'px', top: yPos + 'px'});
										popoverBodyContainer.css({'visibility': 'visible'});
									});
								}
							}
						});
					}
				}
				
				// API with open & close functions
				function PopoverApi() {
					this.show = function(event) {
						$timeout(function() {
							handlePopoverClick(event, event.target);
						});
					}
				}
				
				// Expose the api to allow show event from other targets
				// Attribute value sd-popover='someObject' will be assigned the api object
				// A button with ng-click='someObject.show(event)' would show the popover at the target location
				function exposeAPI(popoverAttr, scope) {
					if (angular.isDefined(popoverAttr) && popoverAttr != '') {
						var popoverAttrAssignable = $parse(popoverAttr).assign;
						if (popoverAttrAssignable) {
							popoverAttrAssignable(scope, new PopoverApi());
						} else {
							trace.info('Could not expose API for: ' + popoverAttr + ', expression is not an assignable.');
						}
					}
				}
				
				function parseAttribute(scope, attr) {
		        	try {
		        		var evalAttr = $parse(attr)(scope);
		        		if (!angular.isDefined(evalAttr)) {
		        			evalAttr = attr;
		        		}
		        		return evalAttr;
		            } catch( err ) {
		                return attr;
		            }
		        }
			}
		}
		
		return directiveDefObject;
	}
})();
