/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * Fix for IE/ANgular rendering issues related to Select DOM elements.
 * In instances where IE refuses to render the angular expressions in the select 
 * box options, and instead displays the unevaluated angular expression, "{{someExpression}}",
 * this directive will inject and remove a null option thus forcing IE to render correctly.
 */

(function(){
	
	var app = angular.module('bpm-common.directives');
	
	app.directive('ieSelectFix', [
	  function() {
	
	      return {
	          restrict: 'A',
	          require: 'ngModel',
	          link: function(scope, element, attributes, ngModelCtrl) {
	              var isIE = document.attachEvent;
	              if (!isIE) return;
	              
	              var control = element[0];
	              //to fix IE issue with parent and detail controller, we need to depend on the parent controller
	              scope.$watch(attributes.ieSelectFix, function() {
	                  //this will add and remove the options to trigger the rendering in IE
	                  var option = document.createElement("option");
	                      control.add(option,null);
	                      control.remove(control.options.length-1);
	                  });
	              }
	          }
	      }
	  ]);
	
})();