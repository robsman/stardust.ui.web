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
 * @sdaProcessInstanceOid - Process Oid to request process attachments for. If
 *                        it is not provided then Process Attachments section
 *                        will not appear
 * @sdaActivityInstanceOid - Activity Oid to request activity attachments for.
 *                         If it is not provided then Activity Attachments
 *                         section will not appear
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
   *  TODO: not required at the moment
   */
  ProcessDocumentsService.prototype.detach = function(uuid) {
    var url = this.rootUrl + "services/rest/portal/documents/" + uuid;
    var deferred = this.$q.defer();

    this.$http['delete'](url).then(function(data) {
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
          sdMimeTypeService, $scope, sdDialogService, documentRepositoryService) {
    this.$scope = $scope;
    this.processDocumentsService = processDocumentsService;
    this.sdViewUtilService = sdViewUtilService;
    this.rootUrl = sdUtilService.getBaseUrl();
    this.sdMimeTypeService = sdMimeTypeService;
    this.sdI18n = $scope.$root.sdI18n;
    this.sdUtilService = sdUtilService;
    this.sdDialogService = sdDialogService;
    this.documentRepositoryService = documentRepositoryService;

    this.documentMenuPopupUrl = sdUtilService.getBaseUrl()
            + "plugins/html5-process-portal/scripts/directives/sdProcessDocumentsPanel/documentMenuPopover.html";

    this.processAttachmentUrl_ = sdUtilService.getBaseUrl()
            + "services/rest/portal/process-instances/{{OID}}/documents";

    this.activityAttachmentUrl_ = sdUtilService.getBaseUrl()
            + "services/rest/portal/activity-instances/{{OID}}/documents";
    this.versionUploadUrl = sdUtilService.getBaseUrl() + "services/rest/portal/documents/upload"

    this.params = {
      createVersion: false
    }

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
      self.showProcessAttachments = true;
      self.processAttachmentUrl = this.processAttachmentUrl_.replace("{{OID}}", self.$scope.processInstanceOid);
    } else {
      self.showProcessAttachments = false;
    }
    if (self.$scope.activityInstanceOid) {
      self.showActivityAttachments = true;
      self.activityAttachmentUrl = this.activityAttachmentUrl_.replace("{{OID}}", self.$scope.activityInstanceOid);
    } else {
      self.showActivityAttachments = false;
    }

    // to get activity and process labels
    if (self.showProcessAttachments || self.showActivityAttachments) {
      self.processDocumentsService.getActivityInstance(self.$scope.activityInstanceOid).then(function(data) {
        self.activityInstance = data.data;
        self.processInstance = data.data.processInstance;
        self.activityLabel = self.activityInstance.activityName;
        self.processLabel = self.processInstance.processName;
      });
    }

    if (self.showProcessAttachments || self.showActivityAttachments) {
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
   * @param api
   */
  ProcessDocumentsController.prototype.initializeActivityAttUploadDialog = function(api) {
    this.uploadActivityAttDialogApi = api;
  };

  /**
   * 
   */
  ProcessDocumentsController.prototype.uploadNewActivityAttachment = function() {
    var self = this;
    this.uploadActivityAttDialogApi.open().then(function(files) {
      if (files.length > 0) {
        self.initializeDocuments();
      }
    });
  };

  /**
   * @param api
   */
  ProcessDocumentsController.prototype.initializeProcessAttUploadDialog = function(api) {
    this.uploadProcessAttDialogApi = api;
  };

  /**
   * 
   */
  ProcessDocumentsController.prototype.uploadNewProcessAttachment = function() {
    var self = this;
    this.uploadProcessAttDialogApi.open().then(function(files) {
      if (files.length > 0) {
        self.initializeDocuments();
      }
    });
  };

  /**
   * @param api
   */
  ProcessDocumentsController.prototype.initializeVersionUploadDialog = function(api) {
    this.versionUploadDialogApi = api;
  };

  /**
   * 
   */
  ProcessDocumentsController.prototype.uploadNewVersion = function() {
    var self = this;
    this.versionUploadDialogApi.open().then(function(files) {
      if (files.length > 0) {
        self.replaceDocumentOnUI(files[0]);
      }
    })["catch"](function(err) {
      self.sdDialogService.error(self.$scope, err, {});
    })
  };

  /**
   * @param doc
   */
  ProcessDocumentsController.prototype.download = function() {
    this.sdUtilService.downloadDocument(this.selectedDocument.uuid, this.selectedDocument.name);
  };

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

    if (this.showProcessAttachments && this.processAttachments) {
      total = total + this.processAttachments.length;
    }

    this.$scope.$emit('TotalAttachmentsNumberChanged', {
      totalAttachments: total
    });
  };

  /**
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
      this.documentRepositoryService.renameDocument(this.selectedDocument.uuid, newName).then(function(data) {
        self.replaceDocumentOnUI(data);
      }, function(error) {
        self.sdDialogService.error(self.$scope, error.data.message, {});
      })
    }
  }

  /**
   * @param newFile
   */
  ProcessDocumentsController.prototype.replaceDocumentOnUI = function(newFile) {
    var self = this;
    for (var int = 0; int < self.processAttachments.length; int++) {
      if (self.processAttachments[int].uuid === newFile.uuid) {
        self.processAttachments[int] = newFile;
        self.selectedDocument = newFile;
      }
    }
    for (var int = 0; int < self.activityAttachments.length; int++) {
      if (self.activityAttachments[int].uuid === newFile.uuid) {
        self.activityAttachments[int] = newFile;
        self.selectedDocument = newFile;
      }
    }

  }

  /**
   * 
   */
  ProcessDocumentsController.prototype.deleteDocument = function() {
    var self = this;
    this.documentRepositoryService.deleteDocument(this.selectedDocument.uuid).then(function() {
      self.initializeDocuments();
    }, function(result) {
      self.sdDialogService.error(self.$scope, result.data.messages, {});
    })
    self.selectedDocument = null;
  }

  /**
   * 
   */
  ProcessDocumentsController.prototype.detach = function() {
    var self = this;
    this.processDocumentsService.detach(this.selectedDocument.uuid).then(function() {
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
    this.versionHistoryDocId = this.selectedDocument.uuid;
    this.showVersionHistoryDialog = true;
  }

  /**
   * @param event
   */
  ProcessDocumentsController.prototype.attachmentDropHandler = function(event) {
    if (event.type === "success") {
      if (event.data) {
        if (event.data.failures && event.data.failures.length > 0) {
          var messages = [];
          event.data.failures.forEach(function(failure) {
            messages.push(failure.message);
          })
          // display error
          this.sdDialogService.error(this.$scope, messages, {});
        }
      }
      this.initializeDocuments();
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
    }, tempDataPath, tempDoc, i, j;

    for (i = 0; i < data.length; i++) {
      tempDataPath = data[i];

      var targetDocumentList = res.specificDocuments;

      if (tempDataPath.dataPath.id === "PROCESS_ATTACHMENTS") {
        targetDocumentList = res.processAttachments;
      }
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
      "sgI18nService", 'sdMimeTypeService', "$scope", "sdDialogService", "documentRepositoryService"];

  // register a controller
  app.controller('processDocumentsPanelCtrl', ProcessDocumentsController);

  // register a directive
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