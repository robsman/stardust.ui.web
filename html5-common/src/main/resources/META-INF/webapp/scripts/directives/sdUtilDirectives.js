/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

/**
 * @author Yogesh.Manware
 */

(function() {

  var app = angular.module('bpm-common.directives');

  app.directive('sdAutoTabTo', [function() {
    return {
      restrict: "A",
      link: function(scope, el, attrs) {
        el.bind('keydown', function(e) {
          if (9 === e.keyCode) {
            var element = document.getElementById(attrs.autoTabTo);
            if (element) element.focus();
          }
        });
      }
    }
  }]);

})();
