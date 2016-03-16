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

  var app = angular.module('bpm-common.directives');

  /**
   * define service
   */
  function ProcessDocumentsService($http, $q, sdUtilService) {
    this.$http = $http;
    this.$q = $q;
    this.rootUrl = sdUtilService.getBaseUrl();
  }

  /**
   * @param processInstanceOid
   * @returns
   */
  ProcessDocumentsService.prototype.getProcessDocuments = function(processInstanceOid) {
    var url = this.rootUrl + "services/rest/portal/process-instances/" + processInstanceOid + "/documents";
    var deferred = this.$q.defer();

    /*
     * var searchCri = { "documentSearchCriteria": { "selectedFileTypes":
     * ["All"], "selectedDocumentTypes": ["All"], "selectedRepository": ["All"],
     * "showAll": false, "searchContent": true, "searchData": true,
     * "advancedFileType": "", "selectFileTypeAdvance": false, "documentName":
     * "", "documentId": "", "createDateTo": 1457516688730,
     * "modificationDateTo": 1457516688730, "author": "", "containingText": "" } };
     */

    this.$http.get(url).then(function(data) {
      deferred.resolve(data);
    }, function(error) {
      deferred.reject(error);
    })
    return deferred.promise;
  }

  /**
   * @param activityInstanceOid
   * @returns
   */
  ProcessDocumentsService.prototype.getActivityDocuments = function(activityInstanceOid) {
    var url = this.rootUrl + "services/rest/portal/notes/activity/" + activityInstanceOid + "?asc=true";
    var deferred = this.$q.defer();

    this.$http.get(url).then(function(data) {
      deferred.resolve(data);
    }, function(error) {
      deferred.reject(error);
    })
    return deferred.promise;
  }

  /**
   * @param activityInstanceOid
   * @returns
   */
  ProcessDocumentsService.prototype.updateDocument = function(data) {
    var url = this.rootUrl + "services/rest/portal/notes/save";
    var deferred = this.$q.defer();

    this.$http.post(url, data).then(function(data) {
      deferred.resolve(data);
    }, function(error) {
      deferred.reject(error);
    })
    return deferred.promise;
  }

  ProcessDocumentsService.prototype.uploadDocument = function(data) {
    var url = this.rootUrl + "services/rest/portal/notes/save";
    var deferred = this.$q.defer();

    this.$http.post(url, data).then(function(data) {
      deferred.resolve(data);
    }, function(error) {
      deferred.reject(error);
    })
    return deferred.promise;
  }

  // inject dependencies
  ProcessDocumentsService.$inject = ["$http", "$q", "sdUtilService"];

  // register service with Angular Module
  app.service("processDocumentsService", ProcessDocumentsService);

  // define controller
  /**
   * 
   */
  function ProcessDocumentsController(processDocumentsService, sdViewUtilService, sdUtilService, sgI18nService,
          sdMimeTypeService, $scope) {
    this.$scope = $scope;
    this.processDocumentsService = processDocumentsService;
    this.sdViewUtilService = sdViewUtilService;
    this.rootUrl = sdUtilService.getBaseUrl();
    this.sdMimeTypeService = sdMimeTypeService;
    this.sdI18n = $scope.$root.sdI18n;

    this.documentMenuPopupUrl = sdUtilService.getBaseUrl()
            + "plugins/html5-views-common/html5/partials/views/documentMenuPopover.html";
    this.documentActionControl = {};

    this.initialize();
  }

  /**
   * 
   */
  ProcessDocumentsController.prototype.initialize = function() {
    this.initializeDocuments();
    console.log("ProcessDocuments controller initialized...");
  }

  /**
   * 
   */
  ProcessDocumentsController.prototype.initializeDocuments = function() {
    var self = this;
    if (self.$scope.processInstanceOid) {
      self.showProcessDocuments = true;
    } else {
      self.showProcessDocuments = false;
    }
    if (self.$scope.activityInstanceOid) {
      self.showActivityDocuments = true;
    } else {
      self.showActivityDocuments = false;
    }

    if (self.showProcessDocuments || self.showActivityDocuments) {
      self.processDocumentsService.getProcessDocuments(self.$scope.processInstanceOid).then(function(data) {
        self.processDocuments = self.normalizeData(data.data);
        self.activityDocuments = self.normalizeData(data.data);
      });
    }
  }

  /**
   * 
   */
  ProcessDocumentsController.prototype.uploadProcessDocuments = function() {

  }

  /**
   * 
   */
  ProcessDocumentsController.prototype.uploadActivityDocuments = function() {
  }

  /**
   * 
   */
  ProcessDocumentsController.prototype.openDocumentView = function(documentId) {
    var viewKey = 'documentOID=' + encodeURIComponent(documentId);
    viewKey = window.btoa(viewKey);

    var parameters = {
      "documentId": documentId
    };
    this.sdViewUtilService.openView('documentView', viewKey, parameters, false);
  };

  /**
   * @returns
   */
  ProcessDocumentsController.prototype.getTotalCount = function() {
    var total = 0;
    if (this.showActivityDocuments && this.activityDocuments) {
      total = total + this.activityDocuments.totalCount;
    }

    if (this.showProcessDocuments && this.processDocuments) {
      total = total + this.processDocuments.totalCount;
    }
    return total;
  };

  /**
   * TODO: move sdMimeTypeService to html5common and then used it here
   * 
   * @param mimeType
   */
  ProcessDocumentsController.prototype.getGlyphiconClass = function(mimeType) {
    return this.sdMimeTypeService.getIcon(mimeType);
  };

  /**
   * @param document
   * @returns
   */
  ProcessDocumentsController.prototype.getDocumentLabel = function(document) {
    if (document.dataPathId != "PROCESS_ATTACHMENTS") {
      return document.dataPathName;
    } else {
      return document.name;
    }
  };

  /**
   * 
   */
  ProcessDocumentsController.prototype.showDocumentMenuPopover = function(selectedDocument) {
    this.documentActionControl.selectedDocument = selectedDocument;
  }

  ProcessDocumentsController.prototype.rename = function() {
    this.documentActionControl[this.documentActionControl.selectedDocument.uuid] = {
      edit: true,
      name: this.documentActionControl.selectedDocument.name
    };
  }

  ProcessDocumentsController.prototype.renameComplete = function() {

  }

  /**
   * @param data
   * @returns
   */
  ProcessDocumentsController.prototype.normalizeData = function(data) {
    var res = {
      processAttachments: [],
      activityAttachments: [],
      specificDocuments: []
    }, tempDataPath, tempDoc, i, j; // iterators

    for (i = 0; i < data.length; i++) {
      tempDataPath = data[i];

      var targetDocumentList = res.specificDocuments;

      if (tempDataPath.dataPath.id === "PROCESS_ATTACHMENTS") {
        targetDocumentList = res.processAttachments;
      }
      // PROCESS ATTACHMENT PROCESSING
      for (j = 0; j < tempDataPath.documents.length; j++) {
        // activity attachment or process attachments
        if (tempDataPath.documents[j].attachmentType == "activity") {
          targetDocumentList = res.activityAttachments;
        } else if (tempDataPath.documents[j].attachmentType == "process") {
          targetDocumentList = res.processAttachments;
        }

        tempDoc = tempDataPath.documents[j];
        tempDoc.dataPathId = tempDataPath.dataPath.id;
        tempDoc.dataPathName = tempDataPath.dataPath.name;
        targetDocumentList.push(tempDoc);
      }
    }
    return res;
  }

  // inject dependencies
  ProcessDocumentsController.$inject = ["processDocumentsService", "sdViewUtilService", "sdUtilService",
      "sgI18nService", 'sdMimeTypeService', "$scope"];

  // register controller
  app.controller('processDocumentsPanelCtrl', ProcessDocumentsController);

  // register directive
  app
          .directive(
                  "sdProcessDocumentsPanel",
                  [
                      "sdUtilService",
                      function(sdUtilService) {
                        return {
                          restrict: 'EA',
                          scope: {
                            activityInstanceOid: "@sdaActivityInstanceOid",
                            processInstanceOid: "@sdaProcessInstanceOid"
                          },
                          controller: "processDocumentsPanelCtrl",
                          controllerAs: "processDocumentsCtrl",
                          templateUrl: sdUtilService.getBaseUrl()
                                  + "plugins/html5-process-portal/scripts/directives/sdProcessDocumentsPanel/sdProcessDocumentsPanel.html"
                        };
                      }])
})();