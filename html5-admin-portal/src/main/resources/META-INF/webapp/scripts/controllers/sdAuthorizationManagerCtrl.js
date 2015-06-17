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
              'sdParticipantManagementService', AMCtrl]);

  var trace;
  var _sdParticipantManagementService;
  /**
   * 
   */
  function AMCtrl($scope, $q, sdLoggerService, sdUtilService, sdI18nService,
          sdParticipantManagementService) {
    trace = sdLoggerService.getLogger('admin-ui.sdAuthorizationManagerCtrl');
    $scope.sdI18nHtml5Admin = sdI18nService.getInstance('html5-admin-portal').translate;
    _sdParticipantManagementService = sdParticipantManagementService;

    this.participants = {
      list: [],
      totalCount: 0
    };

    this.refreshParticipants();
  }

  /**
   * 
   */
  AMCtrl.prototype.refreshParticipants = function() {
    var self = this;
    _sdParticipantManagementService.searchParticipants({
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

  AMCtrl.prototype.refreshPermissionsTree = function() {
    alert('Refresh Permissions');
  };

  AMCtrl.prototype.cloneParticipant = function() {
    alert('clone participant');
  }

  AMCtrl.prototype.highlightParticipants = function() {
    alert('clone participant');
  }
})();