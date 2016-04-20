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

    for ( var processInd in this.processInstances.list) {
      var process = this.processInstances.list[processInd];
      process.type_ = "Process";
      process.processOid_ = process.oid;
      this.flowElements.push(this.processInstances.list[processInd]);

      // stratify documents
      this.documents = this.normalizeData(this.processInstances.list[processInd].attachments);

      // insert activities
      var activityInstances = this.processInstances.list[processInd].activityInstances;
      var processOid = this.processInstances.list[processInd].oid;

      if (activityInstances && activityInstances.list) {
        for ( var ind in activityInstances.list) {
          var activity = activityInstances.list[ind];
          activity.type_ = "Activity";
          activity.expanded_ = false;
          activity.processOid_ = processOid;

          // add notes
          if (activity.notes && activity.notes.totalCount > 0) {
            activity.notes_ = activity.notes.list;
          }

          // add Documents
          activity.documents = this.documents[activity.activityOID];

          this.flowElements.push(activity);
        }
      }
    }
  }

  /**
   * 
   */
  ProcessSummaryController.prototype.initProcessInstances = function() {
    var self = this;
    this.sdProcessSummaryService.getProcessInstances(self.$scope.processInstanceOid).then(function(data) {
      self.processInstances = data;
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
  ProcessSummaryController.prototype.normalizeData = function(data) {
    var res = {}, tempDataPath, tempDoc, i, j;
    if (!data) { return res; }
    
    for (i = 0; i < data.length; i++) {
      if (data[i].attachmentType === "activity") {
        if (!res[data[i].oid]) {
          res[data[i].oid] = [];
        }
        res[data[i].oid].push(data[i]);
      }
    }
    return res;
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
    var url = this.rootUrl + "services/rest/portal/process-instances/" + processInstanceOid + "?hierarchy=true";
    var deferred = this.$q.defer();

    this.$http.get(url).then(function(data) {
      deferred.resolve(data.data);
    }, function(error) {
      deferred.reject(error);
    })
    return deferred.promise;
  }

})();