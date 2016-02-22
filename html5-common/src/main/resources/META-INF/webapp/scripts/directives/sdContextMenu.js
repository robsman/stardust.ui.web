/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

(function() {

  'use strict';

  function dropDownMenu($q) {

    // Linking function for our menu
    function dropDownLinkingFx(scope, elem, attrs) {

      // controls menu visibility
      scope.showMenu = false;

      scope.openMenu = function() {
        if (!scope.showMenu) {
          scope.showMenu = true;
          $(document).bind('click', popoverCloseEvent);
          $(document).bind('contextmenu', popoverCloseEvent);
        }
      }
      
      scope.closeMenu = function() {
        if (scope.showMenu) {
          scope.showMenu = false;
          $(document).unbind('click', popoverCloseEvent);
          $(document).unbind('contextmenu', popoverCloseEvent);
        }
      }

      // callback function from our template which wraps the users callback
      scope.invoke = function(item, e) {
        var deferred = $q.defer(), menuItem = {}, parentLi;

        // handle FireFox-IE-Chrome
        var element = e.target || e.srcElement;

        // Clicks can occur on the LI or the nested span
        if (element.nodeName === 'SPAN') {
          parentLi = angular.element(element.parentElement);
        } else {
          parentLi = angular.element(element.srcElement);
        }

        // user can implement this css class as desired (or not)
        parentLi.addClass("deferred");

        // build up our object we will pass back
        menuItem.item = item;
        menuItem.deferred = deferred;
        menuItem.menuEvent = "menuItem.clicked";

        // invoke
        this.callback({
          "menuItem": menuItem,
          "e": e
        });

        // wait for deferred to resolve.
        deferred.promise.then(function(fx) {

          // then remove the deferred class
          parentLi.removeClass("deferred");

          // on resolve hide menu
          scope.showMenu = false;
          // this is important since we want this to be called exactly once
          $(document).unbind('click', popoverCloseEvent);
          $(document).unbind('contextmenu', popoverCloseEvent);

          // let user muck about with the scope however they want by
          // invoking their function passed through as resolved data
          if (angular.isFunction(fx)) {
            fx(scope);
          }

        });
      };// scope.invoke ends.

      function popoverCloseEvent(event) {
        if (!event || elem.find(event.target).length == 0) {
          scope.$apply(function() {
            scope.showMenu = false;
            // this is important since we want this to be called exactly once
            $(document).unbind('click', popoverCloseEvent);
            $(document).unbind('contextmenu', popoverCloseEvent);
          });
        }
      }
    }// Linking function ends.

    return {
      template: "<div class='sd-drop-down-menu'>"
              + "<div class='dd-header' ng-right-click='openMenu()' ng-transclude></div>"
              + "<ul style='position:absolute;' ng-show='showMenu'>" + "<span ng-click='closeMenu()'>x</span>"
              + "<li ng-click='invoke(item,$event)'" + "ng-class='item.class'" + "ng-repeat='item in menuItems'>"
              + "<span>{{item.value}}</span>" + "</li>" + "</ul>" + "</div>",
      restrict: "EA",
      transclude: true,
      scope: {
        "menuItems": "=sdaMenuItems",
        "callback": "&sdaMenuCallback"
      },
      link: dropDownLinkingFx
    };

  }

  dropDownMenu.inject = ["$q"];

  angular.module("bpm-common.directives").directive("sdContextMenu", dropDownMenu);

})();