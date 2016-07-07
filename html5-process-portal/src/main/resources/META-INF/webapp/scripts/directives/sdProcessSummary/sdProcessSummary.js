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
 *  @sdaActivityInstanceOid - it is optional and used to highlight the given activity 
 */

/**
 * @author Yogesh.Manware
 */

(function() {

  var app = angular.module('workflow-ui');

  // register a directive
  app.directive("sdProcessSummary", [
      "sdUtilService",
      function(sdUtilService) {
        return {
          restrict: 'E',
          scope: {
            processInstanceOid: "@sdaProcessInstanceOid",
            activityInstanceOid: "@sdaActivityInstanceOid"
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
    this.sdI18n = $scope.$root.sdI18n;
    this.currentActivityInstanceOid = $scope.activityInstanceOid; //to highlight the background

    // fetch data from server, then call this method
    this.refresh();

  }

  ProcessSummaryController.prototype.isProcessExpanded = function(processOid) {
    if (this.expandedProcessOids.indexOf(processOid) > -1) {
      return true;
    } else {
      return false;
    }
  }
  
  /**
   * 
   * @param key
   * @returns
   */
  ProcessSummaryController.prototype.getI18n = function(key) {
    if (this.$scope.$root.sdI18n) {
      return this.$scope.$root.sdI18n(key);
    } else {
      return key;
    }
  }  

  ProcessSummaryController.prototype.toggleProcessSelection = function(flowElement) {
    var index = this.expandedProcessOids.indexOf(flowElement.processOid_);
    if (index > -1) {
      this.expandedProcessOids.splice(index, 1);
      // parent process oid color
      flowElement.colorIndex_ = flowElement.parentProcessOid_;   
    } else {
      // own process oid color
      flowElement.colorIndex_ = flowElement.startingProcessInstance.oid;
      this.expandedProcessOids.push(flowElement.processOid_);
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
          this.flowElements[prop].colorIndex_ = this.flowElements[prop].startingProcessInstance.oid;
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
      } else {
        this.flowElements[prop].colorIndex_ = this.flowElements[prop].parentProcessOid_;
      }
    }
  }

  /**
   * 
   */
  ProcessSummaryController.prototype.initialize = function() {
    this.expandedProcessOids = [];
    this.showNotes = true;
    this.showDocuments = true;
    this.flowElements = [];

    // stratify documents
    // this.documents = this.normalizeData(activityInstance.attachments);
    this.documents = {};
    this.procDocuments = {};
    this.historicalData = {};

    this.processInstance.type_ = "Activity";
    this.processInstance.isRoot_ = true;
    this.processInstance.name_ = this.processInstance.processName;
    this.processInstance.oid_ = this.processInstance.oid;
    this.processInstance.processOid_= this.processInstance.oid;
    this.processInstance.colorIndex_ = this.processInstance.oid;
    
    this.expandedProcessOids.push(this.processInstance.oid);
    this.flowElements.push(this.processInstance);
    this.addActivityInstances(this.processInstance);
  }

  ProcessSummaryController.prototype.addActivityInstances = function(processInstance) {
    var activityInstances = processInstance.activityInstances;
    delete processInstance.activityInstances;

    this.normalizeAttachments(processInstance.attachments);
    this.normalizeHistoricalData(processInstance.historicalData);
    
    // add notes
    if (processInstance.notes && processInstance.notes.list.length > 0) {
      processInstance.notes_ = processInstance.notes.list;
    }
    
    // add documents
    processInstance.documents = this.procDocuments[processInstance.oid];

    for ( var index in activityInstances) {
      var activityInstance = activityInstances[index];

      if (activityInstance.startingProcessInstance) {
        activityInstance.type_ = "Process";
        activityInstance.parentProcessOid_ = activityInstance.processInstance.oid;
        activityInstance.colorIndex_ = activityInstance.processInstance.oid;
        activityInstance.processOid_= activityInstance.startingProcessInstance.oid;
        activityInstance.name_ = activityInstance.activity.name;
        activityInstance.oid_ = activityInstance.activityOID;
        this.flowElements.push(activityInstance);
        this.addActivityInstances(activityInstance.startingProcessInstance);
      } else {
        // insert activities
        activityInstance.type_ = "Activity";
        activityInstance.expanded_ = false;
        activityInstance.colorIndex_ = processInstance.oid;
        activityInstance.processOid_ = processInstance.oid;
        activityInstance.name_ = activityInstance.activity.name;
        activityInstance.oid_ = activityInstance.activityOID;
        
        // add notes
        if (activityInstance.notes && activityInstance.notes.list.length > 0) {
          activityInstance.notes_ = activityInstance.notes.list;
        }

        // add documents
        activityInstance.documents = this.documents[activityInstance.activityOID];
        
        //add historical data
        if (activityInstance.isChecklistActivity) {
          activityInstance.historicalData = this.historicalData[activityInstance.activityOID];
          if (activityInstance.historicalData && activityInstance.historicalData.length > 0) {
            activityInstance.dataModifiedBy = activityInstance.historicalData[0].modifiedBy;
          }
        }
        this.flowElements.push(activityInstance);
      }
    }

  }

  /**
   * 
   */
  ProcessSummaryController.prototype.refresh = function() {
    var self = this;
    this.sdProcessSummaryService.getProcessInstances(self.$scope.processInstanceOid).then(function(data) {
      self.processInstance = data;
      self.initialize();
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
      if (data[i].contextKind === "AI") {
        res = this.documents;
      } else if (data[i].contextKind === "PI") {
        res = this.procDocuments;
      }
      if (!res[data[i].contextOID]) {
        res[data[i].contextOID] = [];
      }
      var doc_exist = false;
      for ( var docInd in res[data[i].contextOID]) {
        if (data[i].uuid == res[data[i].contextOID][docInd].uuid) {
          doc_exist = true;
          break;
        }
      }
      if (!doc_exist) {
        res[data[i].contextOID].push(data[i]);
      }
    }
  }

  /**
   * 
   */
  ProcessSummaryController.prototype.normalizeHistoricalData = function(data) {
    var res = this.historicalData, i;
    if (!data) { return; }

    for (i = 0; i < data.length; i++) {
      if (!res[data[i].contextAIOID]) {
        res[data[i].contextAIOID] = [];
      }
      res[data[i].contextAIOID].push(data[i]);
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