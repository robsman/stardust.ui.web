/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

/**
 * @author Yogesh.Manware
 */

(function() {
  'use strict';
  angular.module("bpm-common.directives").directive('ngRightClick',
          function($parse) {
            return function(scope, element, attrs) {
              var callBackFn = $parse(attrs.ngRightClick);
              element.bind('contextmenu', function(event) {
                scope.$apply(function() {
                  event.preventDefault();
                  callBackFn(scope);
                });
              });
            };
          });
})();