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

  var app = angular.module('bpm-common.directives');
  app.directive('sdStateDisplay', function() {
    var DEFAULT_COLORS = ['#F79F81', '#FFBF00', '#00FFFF', '#BEF781', '#A9E2F3', '#E1F5A9', '#A9F5E1', '#E3F6CE',
        '#F8E1FF', '#F2F2F2'];

    return {
      link: function(scope, el, attrs) {
        var colors = scope.$eval(attrs['sdaStateDisplayColors'])
        // var borderBottom = attrs['sdaStateDisplayBorderBottom'];

        if (!colors) {
          colors = DEFAULT_COLORS;
        }
        var totalN = colors.length;

        scope.$watch(attrs['sdStateDisplay'], function(newVal) {
          var index = attrs['sdStateDisplay'] % totalN;
          el.css('background-color', colors[index]);
          /*
           * if(borderBottom){ el.css('border-bottom', "'" + borderBottom + "'" +
           * 'solid ' + colors[index]); }
           */
        });
      }
    }
  });

  app.directive('sdUserImage', function() {
    return {
      template: "<img" + " ng-if='isImagePathAvailable()'" + " ng-src='{{getImage()}}'"
              + " width='24px' height='24px' style='padding:3px'/> <i" + " class='{{getImage()}} pi-1x'"
              + " ng-if='!isImagePathAvailable()'" + " style='margin-top: 3px; margin-left: 3px;'></i>",
      scope: {

      },
      restrict: "E",
      link: function($scope, el, attrs) {
        $scope.imagePath = attrs['sdaPath'];
      },
      controller: ['$scope', 'sdUtilService', function($scope, sdUtilService) {
        $scope.rootUrl = sdUtilService.getBaseUrl();
        $scope.isImagePathAvailable = function() {
          if (!$scope.imagePath) { return false; }
          return ($scope.imagePath.indexOf('/') > -1)
        }
        $scope.getImage = function() {
          if (!$scope.imagePath) { return "fa fa-user-avatar"; }
          var rootUrl = $scope.rootUrl.slice(0, this.rootUrl.length - 1);
          return ($scope.imagePath.indexOf("/") > -1) ? rootUrl + $scope.imagePath : $scope.imagePath;
        }
      }]
    }
  });

  // to display html contents appropriately
  app.filter("sdSanitize", ['$sce', function($sce) {
    return function(htmlCode) {
      return $sce.trustAsHtml(htmlCode);
    }
  }]);

})();
