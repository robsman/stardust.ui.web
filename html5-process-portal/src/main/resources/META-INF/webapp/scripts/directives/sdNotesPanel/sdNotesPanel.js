/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

/**
 * Provides implementation of Notes Panel which includes both Activity and
 * Process level notes ATTRIBUTES:
 * -----------------------------------------------------------------------------------
 * 
 * @sdaProcessInstanceOid - Process Oid to request process notes for. If it is
 *                        not provided then Process Notes section will not
 *                        appear
 * @sdaActivityInstanceOid - Activity Oid to request activity notes for. If it
 *                         is not provided then Activity Notes section will not
 *                         appear
 */

/**
 * @author Yogesh.Manware
 */
(function() {

  var app = angular.module('bpm-common.directives');

  /**
   * define service
   */
  function NotesService($http, $q, sdUtilService) {
    this.$http = $http;
    this.$q = $q;
    this.rootUrl = sdUtilService.getBaseUrl();
  }

  /**
   * @param processInstanceOid
   * @returns
   */
  NotesService.prototype.getProcessNotes = function(processInstanceOid) {
    var url = this.rootUrl + "services/rest/portal/notes/process/" + processInstanceOid + "?asc=true";
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
  NotesService.prototype.getActivityNotes = function(activityInstanceOid) {
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
  NotesService.prototype.saveNotes = function(data) {
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
  NotesService.$inject = ["$http", "$q", "sdUtilService"];

  // register service with Angular Module
  app.service("notesService", NotesService);

  // define controller
  /**
   * 
   */
  function NotesController(notesService, sdViewUtilService, sdUtilService, sgI18nService, $scope,
          sdPropertiesPageService) {
    this.$scope = $scope;
    this.notesService = notesService;
    this.sdViewUtilService = sdViewUtilService;
    this.rootUrl = sdUtilService.getBaseUrl();
    this.sdI18n = $scope.$root.sdI18n;
    this.propertiesPageService = sdPropertiesPageService;
    this.initialize();

  }

  /**
   * 
   */
  NotesController.prototype.initialize = function() {
    this.initializeProcessNotes();
    this.initializeActivityNotes();
    console.log("Notes controller initialized...");
  }

  /**
   * 
   */
  NotesController.prototype.initializeProcessNotes = function() {
    var self = this;
    if (self.$scope.processInstanceOid) {
      self.showProcessNotes = true;
    } else {
      self.showProcessNotes = false;
    }

    if (self.showProcessNotes) {
      self.notesService.getProcessNotes(self.$scope.processInstanceOid).then(function(data) {
        self.processNotes = data.data;
        self.publishTotalCount();
      });
    }
  }

  /**
   * 
   */
  NotesController.prototype.initializeActivityNotes = function() {
    var self = this;
    if (self.$scope.activityInstanceOid) {
      self.showActivityNotes = true;
    } else {
      self.showActivityNotes = false;
    }

    if (self.showActivityNotes) {
      self.notesService.getActivityNotes(self.$scope.activityInstanceOid).then(function(data) {
        self.activityNotes = data.data;
        self.publishTotalCount();
      });
    }
  }

  /**
   * 
   */
  NotesController.prototype.addProcessNotes = function() {
    var self = this;
    if (this.processNote) {
      this.notesService.saveNotes({
        processInstanceOid: this.$scope.processInstanceOid,
        noteText: this.processNote
      }).then(function() {
        self.processNote = undefined;
        self.initializeProcessNotes();
      });
    }
  }

  /**
   * 
   */
  NotesController.prototype.addActivityNotes = function() {
    var self = this;
    this.notesService.saveNotes({
      activityInstanceOid: this.$scope.activityInstanceOid,
      noteText: this.activityNote
    }).then(function() {
      self.activityNote = undefined;
      self.initializeActivityNotes();
    });
  }

  /**
   * @param userImageURI
   * @returns
   */
  NotesController.prototype.getUserImageURL = function(userImageURI) {
    // remove a trailing slash from url
    var rootUrl = this.rootUrl.slice(0, this.rootUrl.length - 1);

    return (userImageURI.indexOf("/") > -1) ? rootUrl + userImageURI : userImageURI;
  };

  /**
   * 
   */
  NotesController.prototype.openNotesView = function() {
    var processInstanceOid = this.$scope.processInstanceOid;

    this.sdViewUtilService.openView("notesPanel", "oid=" + processInstanceOid, {
      "oid": "" + processInstanceOid,
      "processName": this.processNotes.label
    }, false);
  };

  /**
   * @param userImageURI
   * @returns {Boolean}
   */
  NotesController.prototype.isUserImageURLAvailable = function(userImageURI) {
    return (userImageURI.indexOf('/') > -1);
  };

  /**
   * @returns
   */
  NotesController.prototype.publishTotalCount = function() {
    var total = 0;
    if (this.showActivityNotes && this.activityNotes) {
      total = total + this.activityNotes.totalCount;
    }

    if (this.showProcessNotes && this.processNotes) {
      total = total + this.processNotes.totalCount;
    }

    this.propertiesPageService.setTotalNotes(total);
  };

  // inject dependencies
  NotesController.$inject = ["notesService", "sdViewUtilService", "sdUtilService", "sgI18nService", "$scope",
      "sdPropertiesPageService"];

  // register controller
  app.controller('notesPanelCtrl', NotesController);

  // register directive
  app.directive("sdNotesPanel", [
      "sdUtilService",
      function(sdUtilService) {
        return {
          restrict: 'EA',
          scope: {
            activityInstanceOid: "@sdaActivityInstanceOid",
            processInstanceOid: "@sdaProcessInstanceOid"
          },
          controller: "notesPanelCtrl",
          controllerAs: "notesCtrl",
          templateUrl: sdUtilService.getBaseUrl()
                  + "plugins/html5-process-portal/scripts/directives/sdNotesPanel/sdNotesPanel.html"
        };
      }])
})();