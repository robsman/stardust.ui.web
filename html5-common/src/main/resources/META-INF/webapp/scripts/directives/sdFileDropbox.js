/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

/**
 * adds wrapper div around the content to make a complete content area a drop
 * box.
 * 
 * @author Yogesh.Manware
 */
(function() {

  var app = angular.module('bpm-common.directives');

  app.directive('sdFileDropbox', [
      '$http',
      'sdI18nService',
      function($http, sdI18nService) {
        var sdI18n;

        if (sdI18nService && sdI18nService.getInstance) {
          sdI18n = sdI18nService.translate;
        }

        return {
          restrict: 'EA',
          transclude: true,
          scope: {
            params: "=sdaParams",
            placeHolder: "@sdaPlaceholder",
            url: "@sdaUrl",
            eventHandlerCallback: "&sdaHandler"
          },
          template: '<div ng-class="boxContentClass">' + '<ng-transclude></ng-transclude>' + '</div>'
                  + '<div ng-show="showOverlay" class="filedropbox-overlay"> <span">{{placeHolder}}</span></div>',

          link: function(scope, element, attrs) {

            if (!sdI18n) {
              // for iframe, specially, activity panel
              sdI18n = scope.$root.sdI18n;
            }

            if (!scope.placeHolder) {
              scope.placeHolder = sdI18n("views-common-messages.fileDropbox.drophere");
            }

            element.on('dragover', function(event) {
              event.preventDefault();
              event.stopPropagation();
            });

            var counter = 0;
            element.on('dragleave', function(event) {
              event.preventDefault();
              event.stopPropagation();

              counter--;
              if (counter === 0) {
                scope.$apply(function() {
                  scope.boxContentClass = "";
                  scope.showOverlay = false;
                })
              }
            });

            element.on('dragenter', function(event) {
              counter++;
              event.preventDefault();
              event.stopPropagation();
              scope.$apply(function() {
                scope.boxContentClass = "filedropbox-content";
                scope.showOverlay = true;
              })
            });

            element.on('drop', function(event) {
              event.preventDefault();
              event.stopPropagation();
              counter = 0;
              scope.$apply(function() {
                scope.boxContentClass = "";
                scope.showOverlay = false;
              })

              var dataTransfer;
              if (event.dataTransfer) {
                dataTransfer = event.dataTransfer;
              } else if (event.originalEvent.dataTransfer) {
                dataTransfer = event.originalEvent.dataTransfer;
              }

              if (dataTransfer) {
                if (dataTransfer.files.length > 0) {
                  scope.eventHandlerCallback({
                    event: {
                      type: "dropped",
                      file: dataTransfer.files[0]
                    }
                  })
                  postFile(dataTransfer.files);
                }
              }
              return false;
            });

            // post file to provided url
            var postFile = function(files) {
              var formData = new FormData();
              angular.forEach(files, function(value) {
                formData.append("files[]", value);
              });

              if (scope.params) {
                for ( var param in scope.params) {
                  formData.append(param, scope.params[param]);
                }
              }

              $http({
                method: 'POST',
                url: scope.url,
                data: formData,
                headers: {
                  "Content-Type": "multipart/form-data"
                }
              }).success(function() {
                scope.eventHandlerCallback({
                  event: {
                    type: "success"
                  }
                })
              }).error(function() {
                scope.eventHandlerCallback({
                  event: {
                    type: "error"
                  }
                })
              });
            };
          }
        };
      }])
})();