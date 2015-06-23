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

  angular.module('admin-ui.services').provider(
          'sdAuthorizationManagerService',
          function() {
            this.$get = [
                '$resource',
                'sdLoggerService',
                'sdUtilService',
                function($resource, sdLoggerService, sdUtilService) {
                  var service = new AMService($resource, sdLoggerService,
                          sdUtilService);
                  return service;
                }];
          });

  /*
   * 
   */
  function AMService($resource, sdLoggerService, sdUtilService) {
    var PART_REST_BASE_URL = sdUtilService.getBaseUrl()
            + "services/rest/portal/participantManagement";

    var PREF_REST_BASE_URL = sdUtilService.getBaseUrl()
            + "services/rest/portal/preference";

    var GRANT_REST_BASE_URL = sdUtilService.getBaseUrl()
            + "services/rest/portal/preference/permissions/grants";

    var grantResource = $resource(GRANT_REST_BASE_URL);

    var trace = sdLoggerService
            .getLogger('admin-ui.services.sdAuthorizationManagerService');

    // Search Participant
    AMService.prototype.searchParticipants = function(queryParams) {
      // Prepare URL
      var restUrl = PART_REST_BASE_URL + "/searchParticipants";
      var participantSearchResult = $resource(restUrl, queryParams);
      var query = participantSearchResult.query();

      return participantSearchResult.query().$promise;
    };

    // Clone Participant
    AMService.prototype.cloneParticipant = function(sourceParticipants,
            targetParticipants) {
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

      return participantSearchResult.query().$promise;
    };

    // return permissions and relevant participants
    AMService.prototype.getPermissions = function(sourceParticipants,
            targetParticipants) {
      return grantResource.get().$promise;
    }

    // update permissions with participants (allow/deny)
    AMService.prototype.savePermissions = function(postedData) {
      return grantResource.save({}, postedData).$promise;
    }

  }

})();
