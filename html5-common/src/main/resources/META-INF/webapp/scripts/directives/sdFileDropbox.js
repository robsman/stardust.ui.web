/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

/**
 * @author Yogesh.Manware
 * @description - adds wrapper div around the content to make a complete content
 *              area a drop box. It support multiple file drag drop. It accepts
 *              following parameters
 * @param "=sdaParams"
 *          parameters to be sent with each file,
 * @param "@sdaPlaceholder"
 *          message to be displayed when mouse enters the drop area during drag
 *          operation
 * @param "@sdaUrl" -
 *          File will be posted to this URL
 * @param "&sdaHandler -
 *          callback function to handle events - dropped, success, error
 * @param "@sdaOverlayClass" -
 *          style class prefix for the content and overlay
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
            placeHolder_: "@sdaPlaceholder",
            url: "@sdaUrl",
            eventHandlerCallback: "&sdaHandler",
            overlayClass_: "@sdaOverlayClass"
          },
          template: '<div ng-class="boxContentClass">' + '<ng-transclude></ng-transclude>' + '</div>'
                  + '<div ng-show="showOverlay" ng-class="overlayClass"> <span">{{placeHolder}}</span></div>',

          link: function(scope, element, attrs) {

            if (!sdI18n) {
              // for iframe, specially, activity panel
              sdI18n = scope.$root.sdI18n;
            }

            if (!scope.placeHolder_) {
              scope.placeHolder_ = sdI18n("views-common-messages.fileDropbox.drophere");
            }
            scope.placeHolder = scope.placeHolder_;

            if (!scope.overlayClass_) {
              scope.overlayClass_ = "filedropbox";
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
              if (counter == 0) {
                scope.overlayClass = scope.overlayClass_ + "-overlay";
                scope.placeHolder = scope.placeHolder_;
              }
              counter++;
              event.preventDefault();
              event.stopPropagation();
              scope.$apply(function() {
                scope.boxContentClass = scope.overlayClass_ + "-content";
                scope.showOverlay = true;
              })
            });

            element.on('drop', function(event) {
              event.preventDefault();
              event.stopPropagation();
              counter = 0;

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
                      files: dataTransfer.files
                    }
                  })
                  scope.$apply(function() {
                    scope.overlayClass = scope.overlayClass_ + "-progress";
                    scope.placeHolder = sdI18n("views-common-messages.fileDropbox.inProgress",
                            "Transmitting {0} files", [dataTransfer.files.length]);
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
                if (scope.params) {
                  for ( var param in scope.params) {
                    formData.append(param, scope.params[param]);
                  }
                }
              });

              

              $http({
                method: 'POST',
                url: scope.url,
                data: formData,
                headers: {
                  "Content-Type": "multipart/form-data"
                }
              }).success(
                      function(data) {
                        scope.placeHolder = sdI18n("views-common-messages.fileDropbox.success",
                                "File(s) Successfully Transmitted");
                        scope.overlayClass = scope.overlayClass_ + "-success";

                        setTimeout(function() {
                          scope.$apply(function() {
                            scope.boxContentClass = "";
                            scope.showOverlay = false;
                          });
                        }, 2000);

                        scope.eventHandlerCallback({
                          event: {
                            type: "success",
                            data: data
                          }
                        })
                      }).error(
                      function(error) {
                        scope.placeHolder = sdI18n("views-common-messages.fileDropbox.error",
                                "Error occurred while transmitting files. Please check logs");
                        scope.overlayClass = scope.overlayClass_ + "-error";

                        setTimeout(function() {
                          scope.$apply(function() {
                            scope.boxContentClass = "";
                            scope.showOverlay = false;
                          });
                        }, 2000);

                        scope.eventHandlerCallback({
                          event: {
                            type: "error",
                            error: error
                          }
                        })
                      });
            };
          }
        };
      }])
})();