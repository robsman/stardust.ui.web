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

  angular.module('admin-ui.services').provider('sdAuthorizationManagerService', function() {
    this.$get = ['$resource', 'sdLoggerService', 'sdUtilService', function($resource, sdLoggerService, sdUtilService) {
      var service = new AMService($resource, sdLoggerService, sdUtilService);
      return service;
    }];
  });

  /*
   * 
   */
  function AMService($resource, sdLoggerService, sdUtilService) {
    var PART_REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/participants";
    var PREF_REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/preference";
    var GRANT_REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/preference/permissions/grants";

    var grantResource = $resource(GRANT_REST_BASE_URL);

    var trace = sdLoggerService.getLogger('admin-ui.services.sdAuthorizationManagerService');

    // Search Participant
    AMService.prototype.searchParticipants = function() {
      // Prepare URL
    var participantSearchResult = $resource(PART_REST_BASE_URL
					+ "?filterScopedParticipant=false");

      return participantSearchResult.query().$promise;
    };

    // Clone Participant
    AMService.prototype.cloneParticipant = function(sourceParticipants, targetParticipants) {
      if (sourceParticipants.constructor === Array) {
        sourceParticipants = sourceParticipants.join();
      }
      if (targetParticipants.constructor === Array) {
        targetParticipants = targetParticipants.join();
      }

      var queryParams = {
        sourceParticipantIds: sourceParticipants,
        targetParticipantIds: targetParticipants
      }

      // Prepare URL
      var restUrl = PREF_REST_BASE_URL + "/participants/clone";

      var participantSearchResult = $resource(restUrl, queryParams);

      return participantSearchResult.get().$promise;
    };

    // return permissions and relevant participants
    AMService.prototype.getPermissions = function(sourceParticipants, targetParticipants) {
      return grantResource.get().$promise;
    }

    // update permissions with participants (allow/deny)
    AMService.prototype.savePermissions = function(participants, allow, deny, overwrite) {
      // format data
      var data = {};
      data.participants = [];
      data.allow = null;
      data.deny = null;
      data.overwrite = overwrite;
      for (var i = 0; i < participants.length; i++) {
        if (participants[i].qualifiedId) {
          data.participants.push(participants[i].qualifiedId);
        } else {
          data.participants.push(participants[i].participantQualifiedId);
        }
      }
      if (allow != null) {
        data.allow = [];
        for (var j = 0; allow != null && j < allow.length; j++) {
          if (allow[j].id) {
            data.allow.push(allow[j].id);
          } else {
            data.allow.push(allow[j]);
          }
        }
      }

      if (deny != null) {
        data.deny = [];
        for (var k = 0; deny != null && k < deny.length; k++) {
          if (deny[k].id) {
            data.deny.push(deny[k].id);
          } else {
            data.deny.push(deny[k]);
          }
        }
      }
      return grantResource.save({}, data).$promise;
    }

    // Reset participant
    AMService.prototype.resetParticipants = function(participants) {
      var restUrl = PREF_REST_BASE_URL + "/participants/restore";
      var participantIds = [];
      for (var i = 0; i < participants.length; i++) {
        if (participants[i].qualifiedId) {
          participantIds.push(participants[i].qualifiedId);
        } else {
          participantIds.push(participants[i].participantQualifiedId);
        }
      }

      var participantIdsStr = participantIds.join(",");

      return $resource(restUrl, {
        participantIds: participantIdsStr
      }).get().$promise;
    }
  }

})();
