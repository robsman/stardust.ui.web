/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

/**
 * @author Abhay.Thappan
 * @author Yogesh.Manware
 */

(function() {
  'use strict';

  angular.module('admin-ui.services').provider(
          'sdParticipantManagementService',
          function() {
            this.$get = [
                '$resource',
                'sdLoggerService',
                'sdDataTableHelperService',
                'sdUtilService',
                function($resource, sdLoggerService, sdDataTableHelperService, sdUtilService) {
                  var service = new ParticipantManagementService($resource, sdLoggerService, sdDataTableHelperService,
                          sdUtilService);
                  return service;
                }];
          });

  /*
   * 
   */
  function ParticipantManagementService($resource, sdLoggerService, sdDataTableHelperService, sdUtilService) {
    var REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/participantManagement";
    var trace = sdLoggerService.getLogger('admin-ui.services.sdParticipantManagementService');

    /**
     * 
     */
    ParticipantManagementService.prototype.getAllUsers = function(query) {
      // Prepare URL
      var restUrl = REST_BASE_URL + "/:type";

      var options = sdDataTableHelperService.convertToQueryParams(query.options);

      if (options.length > 0) {
        restUrl = restUrl + "?" + options.substr(1);
      }

      var postData = {
        filters: query.options.filters,
        hideInvalidatedUsers: query.hideInvalidatedUsers
      };

      var allUsers = $resource(restUrl, {
        type: '@type'
      }, {
        fetch: {
          method: 'POST'
        }
      });

      var urlTemplateParams = {};
      urlTemplateParams.type = "allUsers";

      return allUsers.fetch(urlTemplateParams, postData).$promise;

    };

    ParticipantManagementService.prototype.openCreateCopyModifyUser = function(mode, oid) {
      var restUrl = REST_BASE_URL + "/:type/:mode/:oid";

      var urlTemplateParams = {};
      urlTemplateParams.type = "openCreateCopyModifyUser";
      urlTemplateParams.mode = mode;
      if (oid != undefined) {
        urlTemplateParams.oid = oid;
      } else {
        urlTemplateParams.oid = -1;
      }

      return $resource(restUrl).get(urlTemplateParams).$promise;
    };

    /**
     * 
     */
    ParticipantManagementService.prototype.createCopyModifyUser = function(user, mode) {
      // Prepare URL
      var restUrl = REST_BASE_URL + "/:type/:mode";

      var postData = {
        user: user
      };

      var createCopyModifyUser = $resource(restUrl, {
        type: '@type',
        mode: '@mode'
      }, {
        fetch: {
          method: 'POST'
        }
      });

      var urlTemplateParams = {};
      urlTemplateParams.type = "createCopyModifyUser";
      urlTemplateParams.mode = mode;

      return createCopyModifyUser.fetch(urlTemplateParams, postData).$promise;

    };

    /**
     * 
     */
    ParticipantManagementService.prototype.invalidateUsers = function(userOids) {
      // Prepare URL
      var restUrl = REST_BASE_URL + "/:type";

      var postData = {
        userOids: userOids
      };

      var invalidateUsers = $resource(restUrl, {
        type: '@type'
      }, {
        fetch: {
          method: 'POST'
        }
      });

      var urlTemplateParams = {};
      urlTemplateParams.type = "invalidateUsers";

      return invalidateUsers.fetch(urlTemplateParams, postData).$promise;

    };

    /**
     * 
     */
    ParticipantManagementService.prototype.delegateToDefaultPerformer = function(activityInstanceOids, userOids) {
      // Prepare URL
      var restUrl = REST_BASE_URL + "/:type";

      var postData = {
        userOids: userOids,
        activityInstanceOids: activityInstanceOids
      };

      var delegateToDefaultPerformer = $resource(restUrl, {
        type: '@type'
      }, {
        fetch: {
          method: 'POST'
        }
      });

      var urlTemplateParams = {};
      urlTemplateParams.type = "delegateToDefaultPerformer";

      return delegateToDefaultPerformer.fetch(urlTemplateParams, postData).$promise;

    };

    // create guid
    ParticipantManagementService.prototype.createGuid = function() {
      var val = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
      });

      return val;
    };

    // get Models which also contains top level Organizations and top level
    // Roles
    ParticipantManagementService.prototype.getModels = function() {
      var restUrl = sdUtilService.getBaseUrl() + "services/rest/portal/models?includePredefinedModel=true";
      var participantSearchResult = $resource(restUrl);

      return participantSearchResult.query().$promise;
    };

    // get Sub-Participants for provided participants
    ParticipantManagementService.prototype.getSubParticipants = function(participant) {
      var participantId = participant.qualifiedId;
      if (participant.type == "DEPARTMENT") {
        participantId = participant.parentQualifiedId + "[" + participant.id + "]";
      } else if ("DEPARTMENT_DEFAULT" == participant.type) {
        participantId = participant.qualifiedId + "[]";
      } else if (participant.parentQualifiedId) {
        participantId = participant.parentQualifiedId + participant.qualifiedId;
      }

      participantId = encodeURIComponent(participantId);

      var restUrl = sdUtilService.getBaseUrl() + "services/rest/portal/participants/" + participantId;

      var participantSearchResult = $resource(restUrl);

      return participantSearchResult.query().$promise;
    };
  }
  ;
})();
