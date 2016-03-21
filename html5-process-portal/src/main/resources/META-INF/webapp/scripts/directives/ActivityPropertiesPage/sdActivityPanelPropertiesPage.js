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
  // define service

  function PropertiesPageService() {
    var totalNotes = 0;
    var totalDocuments = 0;
  }

  angular.extend(PropertiesPageService.prototype, {
    getTotalNotes: function() {
      return this.totalNotes;
    },
    setTotalNotes: function(notesCount) {
      this.totalNotes = notesCount;
    }
  });

  angular.extend(PropertiesPageService.prototype, {
    getTotalDocuments: function() {
      return this.totalDocuments;
    },
    setTotalDocuments: function(documentsCount) {
      this.totalDocuments = documentsCount;
    }
  });

  // register service
  app.service('sdPropertiesPageService', PropertiesPageService);

  // define controller
  function PropertiesPageController($scope, sgI18nService, sdPropertiesPageService) {
    var self = this;
    this.activityInstanceOid = $scope.activityInstanceOid;
    this.processInstanceOid = $scope.processInstanceOid;
    self.$scope = $scope;
    this.sdI18n = $scope.$root.sdI18n;
    this.selectedPropertiesPage = "notes";
    this.propertiesPageService = sdPropertiesPageService;
    self.initialize();
  }

  Object.defineProperty(PropertiesPageController.prototype, 'totalNotes', {
    enumerable: true, // indicate that it supports enumerations
    configurable: false, // disable delete operation
    get: function() {
      return this.propertiesPageService.getTotalNotes();
    }
  });

  Object.defineProperty(PropertiesPageController.prototype, 'totalDocuments', {
    enumerable: true, // indicate that it supports enumerations
    configurable: false, // disable delete operation
    get: function() {
      return this.propertiesPageService.getTotalDocuments();
    }
  });

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
  
  // inject dependencies
  PropertiesPageController.$inject = ["$scope", "sgI18nService", "sdPropertiesPageService"];

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