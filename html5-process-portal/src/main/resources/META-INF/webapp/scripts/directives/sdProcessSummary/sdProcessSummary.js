/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/
/**
 * Provides implementation of Process Summary
 * -----------------------------------------------------------------------------------
 * 
 * @sdaProcessInstanceOid - Process Oid to request process notes for. If it is
 *                        not provided then Process Notes section will not
 *                        appear
 */

/**
 * @author Yogesh.Manware
 */

(function() {

  var app = angular.module('bpm-common.directives');

  // register a directive
  app.directive("sdProcessSummary", [
      "sdUtilService",
      function(sdUtilService) {
        return {
          restrict: 'E',
          scope: {
            processInstanceOid: "@sdaProcessInstanceOid"
          },
          controller: "sdProcessSummaryCtrl",
          controllerAs: "sdProcessSummaryCtrl",
          templateUrl: sdUtilService.getBaseUrl()
                  + "plugins/html5-process-portal/scripts/directives/sdProcessSummary/sdProcessSummary.html"
        };
      }])

  // inject dependencies
  ProcessSummaryController.$inject = ["$scope", "$q", "sdUtilService", 'sdMimeTypeService', "sdProcessSummaryService"];
  // register a controller
  app.controller('sdProcessSummaryCtrl', ProcessSummaryController);

  function ProcessSummaryController($scope, $q, sdUtilService, sdMimeTypeService, sdProcessSummaryService) {
    this.$scope = $scope;
    this.$q = $q;
    this.sdUtilService = sdUtilService;
    this.sdProcessSummaryService = sdProcessSummaryService;
    this.rootUrl = sdUtilService.getBaseUrl();
    this.sdMimeTypeService = sdMimeTypeService;

    this.expandedProcessOids = [];

    // fetch data from server, then call this method
    this.initProcessInstances();

  }

  ProcessSummaryController.prototype.isProcessExpanded = function(processOid) {
    if (this.expandedProcessOids.indexOf(processOid) > -1) {
      return true;
    } else {
      return false;
    }
  }

  ProcessSummaryController.prototype.toggleProcessSelection = function(oid) {
    var index = this.expandedProcessOids.indexOf(oid);
    if (index > -1) {
      this.expandedProcessOids.splice(index, 1);
    } else {
      this.expandedProcessOids.push(oid);
    }
  }

  ProcessSummaryController.prototype.expandAll = function() {
    this.expandedProcessOids = [];

    this.expandedProcessOids.push(this.processInstance.oid);

    for ( var prop in this.flowElements) {
      if (this.flowElements[prop].type_ == 'Activity') {
        this.flowElements[prop].expanded_ = true;
      } else {
        if (this.expandedProcessOids.indexOf(this.flowElements[prop].processOid_)) {
          this.expandedProcessOids.push(this.flowElements[prop].processOid_);
        }
      }
    }
    this.allExpanded = true;
  }

  ProcessSummaryController.prototype.collapseAll = function() {
    this.allExpanded = false;
    this.expandedProcessOids = [];

    this.expandedProcessOids.push(this.processInstance.oid);

    for ( var prop in this.flowElements) {
      if (this.flowElements[prop].type_ == 'Activity') {
        this.flowElements[prop].expanded_ = false;
      }
    }
  }

  /**
   * 
   */
  ProcessSummaryController.prototype.refresh = function() {

    this.showNotes = true;
    this.showDocuments = true;
    this.flowElements = [];

    // stratify documents
    // this.documents = this.normalizeData(activityInstance.attachments);
    this.documents = {};
    this.processInstance.type_ = "Process";
    this.processInstance.name_ = this.processInstance.processName;
    this.processInstance.processOid_ = this.processInstance.oid;
    this.expandedProcessOids.push(this.processInstance.oid);
    // this.flowElements.push(this.processInstance);
    this.addActivityInstances(this.processInstance);
  }

  ProcessSummaryController.prototype.addActivityInstances = function(processInstance) {
    var activityInstances = processInstance.activityInstances;
    delete processInstance.activityInstances;

    this.normalizeAttachments(processInstance.attachments);

    for ( var index in activityInstances) {
      var activityInstance = activityInstances[index];

      if (activityInstance.startingProcessInstance) {
        activityInstance.type_ = "Process";
        activityInstance.processOid_ = activityInstance.startingProcessInstance.oid;
        this.flowElements.push(activityInstance);
        this.addActivityInstances(activityInstance.startingProcessInstance);
      } else {
        // insert activities
        activityInstance.type_ = "Activity";
        activityInstance.expanded_ = false;
        activityInstance.processOid_ = processInstance.oid;

        // add notes
        if (activityInstance.notes && activityInstance.notes.list.length > 0) {
          activityInstance.notes_ = activityInstance.notes.list;
        }

        // add Documents
        activityInstance.documents = this.documents[activityInstance.activityOID];

        this.flowElements.push(activityInstance);
      }
    }

  }

  /**
   * 
   */
  ProcessSummaryController.prototype.initProcessInstances = function() {
    var self = this;
    this.sdProcessSummaryService.getProcessInstances(self.$scope.processInstanceOid).then(function(data) {
      self.processInstance = data;
      self.refresh();
    });
  }

  ProcessSummaryController.prototype.getGlyphiconClass = function(mimeType) {
    return this.sdMimeTypeService.getIcon(mimeType);
  };

  /**
   * @param data
   * @returns
   */
  ProcessSummaryController.prototype.normalizeAttachments = function(data) {
    var res = this.documents, i;
    if (!data) { return; }

    for (i = 0; i < data.length; i++) {
      if (data[i].contextKind === "activity") {
        if (!res[data[i].oid]) {
          res[data[i].oid] = [];
        }
        res[data[i].oid].push(data[i]);
      }
    }
  }

  // inject dependencies
  ProcessSummaryService.$inject = ["$http", "$q", "sdUtilService"];
  // register service with Angular Module
  app.service("sdProcessSummaryService", ProcessSummaryService);

  /**
   * define service
   */
  function ProcessSummaryService($http, $q, sdUtilService) {
    this.$http = $http;
    this.$q = $q;
    this.rootUrl = sdUtilService.getBaseUrl();
  }

  /**
   * @param processInstanceOid
   * @returns
   */
  ProcessSummaryService.prototype.getProcessInstances = function(processInstanceOid) {
    var url = this.rootUrl + "services/rest/portal/process-instances/" + processInstanceOid + "/activity-instances";
    var deferred = this.$q.defer();

    this.$http.get(url).then(function(data) {
      deferred.resolve(data.data);
    }, function(error) {
      deferred.reject(error);
    })
    return deferred.promise;
  }

})();