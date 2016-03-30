/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

/**
 * Provides an implementation for properties page containing Notes and
 * Attachment for both Activity and Process, it can be configured anywhere and
 * not just Activity Panel.
 * -----------------------------------------------------------------------------------
 * 
 * @sdaProcessInstanceOid - If it is not provided then Process level Notes and
 *                        Attachments section will not appear
 * @sdaActivityInstanceOid - If it is not provided then Activity level Notes and
 *                         Attachments section will not appear
 */

/**
 * @author Yogesh.Manware
 */

(function() {

  var app = angular.module('bpm-common.directives');

  // define controller
  function PropertiesPageController($scope, sgI18nService) {
    var self = this;
    this.activityInstanceOid = $scope.activityInstanceOid;
    this.processInstanceOid = $scope.processInstanceOid;
    self.$scope = $scope;
    this.sdI18n = $scope.$root.sdI18n;
    this.selectedPropertiesPage = "notes";
    this.totalNotes = 0;
    self.initialize();
  }

  PropertiesPageController.prototype.initialize = function() {
    var self = this;

    self.$scope.$on('TotalNotesNumberChanged', function(event, data) {
      self.totalNotes = data.totalNotes;
    })

    this.$scope.$on('TotalAttachmentsNumberChanged', function(event, data) {
      self.totalAttachments = data.totalAttachments;
    })

    this.propertiesPageVisible = true;
    
    console.log("sdActivityPanelPropertiesPage initialized...");
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
                                  + "plugins/html5-process-portal/scripts/directives/sdActivityPanelPropertiesPage/sdActivityPanelPropertiesPage.html"
                        };
                      }])
})();