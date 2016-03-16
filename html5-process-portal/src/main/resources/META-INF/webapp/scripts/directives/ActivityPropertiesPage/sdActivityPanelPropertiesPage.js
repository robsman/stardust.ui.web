/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

/**
 * Provides implementation of Documents Panel which includes both Activity and
 * Process level documents ATTRIBUTES:
 * -----------------------------------------------------------------------------------
 * 
 * @sdaProcessInstanceOid - Process Oid to request process documents for. If it
 *                        is not provided then Process Documents section will
 *                        not appear
 * @sdaActivityInstanceOid - Activity Oid to request activity documents for. If
 *                         it is not provided then Activity Documents section
 *                         will not appear
 */

/**
 * @author Yogesh.Manware
 */


(function() {

  console.log("Initializing Activities Properties pages...")

  var app = angular.module('bpm-common.directives');

  // define controller
  function PropertiesPageController($scope, sgI18nService) {
    var self = this;
    this.activityInstanceOid = $scope.activityInstanceOid;
    this.processInstanceOid = $scope.processInstanceOid;
    self.$scope = $scope;
    this.sdI18n = $scope.$root.sdI18n;
    this.selectedPropertiesPage = "notes";
    self.initialize();
  }

  PropertiesPageController.prototype.initialize = function() {
    this.propertiesPageVisible = true;
    console.log("sdActivityPanelPropertiesPageController initialized...");
  }

  PropertiesPageController.prototype.expand = function() {
    this.propertiesPageVisible = true;
    this.$scope.eventCallback({
      event: {
        type: "properties-panel-expanded"
      }
    });
  }

  PropertiesPageController.prototype.collapse = function() {
    this.propertiesPageVisible = false;
    this.$scope.eventCallback({
      event: {
        type: "properties-panel-collapsed"
      }
    });
  }

  PropertiesPageController.prototype.getNotesTotalCount = function() {
    return 10;
  }

  PropertiesPageController.prototype.getDocumentsTotalCount = function() {
    return 12;
  }

  // inject dependencies
  PropertiesPageController.$inject = ["$scope", "sgI18nService"];

  // register controller
  app.controller('sdActivityPanelPropertiesPageController', PropertiesPageController);

  // register directive
  app
          .directive(
                  "sdActivityPanelPropertiesPage",
                  [
                      "sdUtilService",
                      function(sdUtilService) {
                        return {
                          restrict: 'EA',
                          scope: {
                            activityInstanceOid: "@sdaActivityInstanceOid",
                            processInstanceOid: "@sdaProcessInstanceOid",
                            eventCallback: "&sdaEventCallback"
                          },
                          controller: "sdActivityPanelPropertiesPageController",
                          controllerAs: "propertiesPageController",
                          templateUrl: sdUtilService.getBaseUrl()
                                  + "plugins/html5-process-portal/scripts/directives/ActivityPropertiesPage/activityPanelPropertiesPage.html"
                        };
                      }])
})();