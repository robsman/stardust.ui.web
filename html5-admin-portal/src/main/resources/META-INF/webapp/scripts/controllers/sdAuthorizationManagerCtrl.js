/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

/**
 * @author Yogesh.Manware
 */

(function() {
  'use strict';

  angular.module("admin-ui").controller(
          'sdAuthorizationManagerCtrl',
          ['$scope', '$q', 'sdLoggerService', 'sdUtilService', 'sdI18nService',
              'sdMessageService', 'sdAuthorizationManagementService', AMCtrl]);

  var trace;
  var _sdAuthorizationManagementService;
  var _$q;
  var _sdUtilService;
  var _sdMessageService;
  var i18n;
  var ParticipantType = {
    ORGANIZATION: "ORGANIZATION",
    ROLE: "ROLE"
  }

  /**
   * 
   */
  function AMCtrl($scope, $q, sdLoggerService, sdUtilService, sdI18nService,
          sdMessageService, sdAuthorizationManagementService) {
    trace = sdLoggerService.getLogger('admin-ui.sdAuthorizationManagerCtrl');
    _$q = $q;
    _sdUtilService = sdUtilService;
    _sdMessageService = sdMessageService;

    i18n = $scope.sdI18nHtml5Admin = sdI18nService
            .getInstance('html5-admin-portal').translate;

    _sdAuthorizationManagementService = sdAuthorizationManagementService;
    this.cloneTemplateUrl = sdUtilService.getBaseUrl()
            + 'plugins/html5-admin-portal/partials/views/cloneParticipantDialog.html';
    this.participants = {
      list: [],
      totalCount: 0
    };

    this.refreshParticipants();

    AMCtrl.prototype.safeApply = function() {
      sdUtilService.safeApply($scope);
    }
  }

  /**
   * 
   */
  AMCtrl.prototype.onParticipantSelect = function(selectInfo) {
    this.showMessage = true;    
    // count organizations and roles
    var orgs = 0, roles = 0;
    for (var i = 0; i < selectInfo.all.length; i++) {
      if (selectInfo.all[i].type == ParticipantType.ORGANIZATION) {
        orgs++;
      } else if (selectInfo.all[i].type == ParticipantType.ROLE) {
        roles++;
      }
    }

    this.participantsMsg = {};
    this.participantsMsg.message = i18n(
            "views.authorizationManagerViewHtml5.selectedParticipantInfo",
            "Selected Participants", [orgs, roles]);
    this.participantsMsg.type = "ok";
    _sdMessageService.showMessage(this.participantsMsg);
  }

  /**
   * 
   */
  AMCtrl.prototype.refreshParticipants = function() {
    this.showMessage = false;
    var self = this;
    _sdAuthorizationManagementService.searchParticipants({
      type: 3
    }).then(function(result) {
      self.participants.list = result;
      self.participants.totalCount = result.length;
      if (angular.isDefined(self.dataTable)) {
        self.dataTable.refresh(true);
      }
    }, function(error) {
      trace.error("Error occured while fetching Participants!")
    })
  };

  /**
   * @param matchStr
   * @returns
   */
  AMCtrl.prototype.fetchParticipants = function(matchStr) {
    var self = this;
    var deferred = _$q.defer();

    setTimeout(function() {
      var filteredList = [];
      if (!matchStr || matchStr == "") {
        filteredList = self.participants.list;
      }
      for (var i = 0; i < self.participants.list.length; i++) {
        if (self.participants.list[i].name.search(new RegExp(matchStr + '+',
                "gi")) > -1
                || self.participants.list[i].id.search(new RegExp(matchStr
                        + '+', "gi")) > -1) {
          filteredList.push(self.participants.list[i]);
        }
      }
      deferred.resolve(filteredList);
    }, 0);

    return deferred.promise;
  };

  AMCtrl.prototype.refreshPermissionsTree = function() {
    alert('Refresh Permissions tree');
  };

  AMCtrl.prototype.cloneParticipant = function() {
    if (!this.selection || this.selection.length < 1) {
      _sdMessageService
              .showMessage({
                message: i18n("views.authorizationManagerViewHtml5.selectedParticipant.clone.warning"),
                type: "error"
              });
      this.showMessage = true;
    } else {
      this.showMessage = false;
      this.showCloneParticipantDialog = true;
    }
  }

  /**
   * 
   */
  AMCtrl.prototype.cloneParticipantConfirmed = function() {
    if (!this.selectedTargetParticipants || this.selectedTargetParticipants.length == 0) {
      _sdMessageService
              .showMessage({
                message: i18n("views.authorizationManagerViewHtml5.cloneParticipant.targetNotSelected"),
                type: "error"
              });
      this.showMessage = true;
      return;
    }

    var sourceParticipantsIds = [];
    for (var i = 0; i < this.selection.length; i++) {
      sourceParticipantsIds.push(this.selection[i].qualifiedId);
    }

    var targetParticipantsIds = [];
    for (var j = 0; j < this.selectedTargetParticipants.length; j++) {
      targetParticipantsIds
              .push(this.selectedTargetParticipants[j].qualifiedId);
    }
    this.selectedTargetParticipants = [];

    _sdAuthorizationManagementService.cloneParticipant(sourceParticipantsIds,
            targetParticipantsIds);
  }

  /**
   * 
   */
  AMCtrl.prototype.closeCloneParticipantDialog = function() {
    this.showCloneParticipantDialog = false;
  }

  AMCtrl.prototype.highlightParticipants = function() {
    alert('highlight participant');
  }
})();