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

    this.$http.get(url).then(function(data) {
      deferred.resolve(data);
    }, function(error) {
      deferred.reject(error);
    })
    return deferred.promise;
  }

  /**
   * 
   */
  ProcessDocumentsService.prototype.getActivityInstance = function(activityInstanceOid) {
    var url = this.rootUrl + "services/rest/portal/activity-instances/" + activityInstanceOid;
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
  ProcessDocumentsService.prototype.rename = function(document, newName) {
    var url = this.rootUrl + "services/rest/portal/documents/" + document.uuid;
    var deferred = this.$q.defer();

    this.$http.put(url, {
      name: newName
    }).then(function(data) {
      deferred.resolve(data);
    }, function(error) {
      deferred.reject(error);
    })
    return deferred.promise;
  }

  /**
   * 
   */
  ProcessDocumentsService.prototype.deleteDocument = function(document) {
    var url = this.rootUrl + "services/rest/portal/documents/" + document.uuid;
    var deferred = this.$q.defer();

    this.$http['delete'](url).then(function(data) {
      deferred.resolve(data);
    }, function(error) {
      deferred.reject(error);
    })
    return deferred.promise;
  }

  /**
   * 
   */
  ProcessDocumentsService.prototype.detach = function(document) {
    var url = this.rootUrl + "services/rest/portal/documents/" + document.uuid;
    var deferred = this.$q.defer();

    this.$http['delete'](url).then(function(data) {
      deferred.resolve(data);
    }, function(error) {
      deferred.reject(error);
    })
    return deferred.promise;
  }

  ProcessDocumentsService.prototype.getHistory = function(document) {
    var url = this.rootUrl + "services/rest/portal/documents/history/" + document.uuid;
    var deferred = this.$q.defer();

    this.$http.get(url).then(function(data) {
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
          sdMimeTypeService, $scope, sdPropertiesPageService) {
    this.$scope = $scope;
    this.processDocumentsService = processDocumentsService;
    this.sdViewUtilService = sdViewUtilService;
    this.rootUrl = sdUtilService.getBaseUrl();
    this.sdMimeTypeService = sdMimeTypeService;
    this.sdI18n = $scope.$root.sdI18n;
    this.propertiesPageService = sdPropertiesPageService;

    this.documentMenuPopupUrl = sdUtilService.getBaseUrl()
            + "plugins/html5-views-common/html5/partials/views/documentMenuPopover.html";

    this.documentHistoryPopupUrl = sdUtilService.getBaseUrl()
            + "plugins/html5-views-common/html5/partials/views/documentHistory.html";

    this.processAttachmentUrl_ = sdUtilService.getBaseUrl()
            + "services/rest/portal/process-instances/{{OID}}/documents";

    this.activityAttachmentUrl_ = sdUtilService.getBaseUrl()
            + "services/rest/portal/activity-instances/{{OID}}/documents";

    this.initialize();
  }

  /**
   * 
   */
  ProcessDocumentsController.prototype.initialize = function() {
    this.documentHistoryDialog = {};
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
      self.processAttachmentUrl = this.processAttachmentUrl_.replace("{{OID}}", self.$scope.processInstanceOid);
    } else {
      self.showProcessDocuments = false;
    }
    if (self.$scope.activityInstanceOid) {
      self.showActivityAttachments = true;
      self.activityAttachmentUrl = this.activityAttachmentUrl_.replace("{{OID}}", self.$scope.activityInstanceOid);
    } else {
      self.showActivityAttachments = false;
    }

    // to get activity and process labels
    if (self.showProcessDocuments || self.showActivityAttachments) {
      self.processDocumentsService.getActivityInstance(self.$scope.activityInstanceOid).then(function(data) {
        self.activityInstance = data.data;
        self.processInstance = data.data.processInstance;
        self.activityLabel = self.activityInstance.activityName;
        self.processLabel = self.processInstance.processName;
      });
    }

    if (self.showProcessDocuments || self.showActivityAttachments) {
      self.processDocumentsService.getProcessDocuments(self.$scope.processInstanceOid).then(function(data) {
        var allDocs = self.normalizeData(data.data);
        self.processAttachments = allDocs.processAttachments;
        self.activityAttachments = allDocs.activityAttachments;

        self.documentActionControl = {};
        for (var int = 0; int < self.processAttachments.length; int++) {
          self.documentActionControl[self.processAttachments[int].uuid] = {};
        }
        for (var int = 0; int < self.activityAttachments.length; int++) {
          self.documentActionControl[self.activityAttachments[int].uuid] = {};
        }

        self.publishTotalCount();
      });
    }
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
  ProcessDocumentsController.prototype.publishTotalCount = function() {
    var total = 0;
    if (this.showActivityAttachments && this.activityAttachments) {
      total = total + this.activityAttachments.length;
    }

    if (this.showProcessDocuments && this.processAttachments) {
      total = total + this.processAttachments.length;
    }

    this.propertiesPageService.setTotalDocuments(total);
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
    this.selectedDocument = selectedDocument;
    if (!this.documentActionControl[selectedDocument.uuid].popover) {
      this.documentActionControl[selectedDocument.uuid].popover = true;
    } else {
      this.documentActionControl[selectedDocument.uuid].popover = false;
    }
    for ( var uuid in this.documentActionControl) {
      if (uuid != selectedDocument.uuid) {
        this.documentActionControl[uuid].popover = false;
      }
    }
    event.preventDefault();
  }

  /**
   * 
   */
  ProcessDocumentsController.prototype.rename = function() {
    this.documentActionControl[this.selectedDocument.uuid].edit = true;
    this.documentActionControl[this.selectedDocument.uuid].name = this.selectedDocument.name;
  }

  /**
   * 
   */
  ProcessDocumentsController.prototype.renameComplete = function() {
    this.documentActionControl[this.selectedDocument.uuid].edit = false;
    var self = this;
    var newName = this.documentActionControl[this.selectedDocument.uuid].name;

    if (newName && (newName != this.selectedDocument.name)) {
      this.processDocumentsService.rename(this.selectedDocument, newName).then(function(result) {
        for (var int = 0; int < self.processAttachments.length; int++) {
          if (self.processAttachments[int].uuid === result.data.uuid) {
            self.processAttachments[int] = result.data;
            self.selectedDocument = result.data;
          }
        }
        for (var int = 0; int < self.activityAttachments.length; int++) {
          if (self.activityAttachments[int].uuid === result.data.uuid) {
            self.activityAttachments[int] = result.data;
            self.selectedDocument = result.data;
          }
        }
      }, function(error) {
        console.error(error);
      })
    }
  }

  /**
   * 
   */
  ProcessDocumentsController.prototype.deleteDocument = function() {
    var self = this;
    this.processDocumentsService.deleteDocument(this.selectedDocument).then(function() {
      self.initializeDocuments();
    }, function(error) {
      console.error(error);
    })

    self.selectedDocument = null;
  }

  /**
   * 
   */
  ProcessDocumentsController.prototype.detach = function() {
    var self = this;
    this.processDocumentsService.deleteDocument(this.selectedDocument).then(function() {
      self.initializeDocuments();
    }, function(error) {
      console.error(error);
    })

    self.selectedDocument = null;
  }

  /**
   * 
   */
  ProcessDocumentsController.prototype.viewHistory = function() {
    var self = this;
    this.processDocumentsService.getHistory(this.selectedDocument).then(function(result) {
      self.currentDocumentHistory = result.data;
      self.documentHistoryDialog.open();
      self.isReady = true;
    }, function(error) {
      console.error(error);
    })
  }

  ProcessDocumentsController.prototype.getCurrentDocumentHistory = function(params) {
    return this.currentDocumentHistory;
  }

  /**
   * 
   */
  ProcessDocumentsController.prototype.closeHistory = function() {
    this.documentHistoryDialog.close();
  }

  /**
   * @param event
   */
  ProcessDocumentsController.prototype.attachmentDropHandler = function(event) {
    if (event.type === "error") {
      console.log("ERROR!");
    }
    if (event.type === "success") {
      this.initializeDocuments();
    }
    if (event.type === "dropped") {
      console.log("dropped");
    }
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
      "sgI18nService", 'sdMimeTypeService', "$scope", "sdPropertiesPageService"];

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