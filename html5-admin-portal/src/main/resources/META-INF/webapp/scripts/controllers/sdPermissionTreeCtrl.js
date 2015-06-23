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
          'sdPermissionTreeCtrl',
          ['$scope', '$q', 'sdLoggerService', 'sdUtilService', 'sdI18nService',
              'sdMessageService', 'sdAuthorizationManagementService', PTCtrl]);

  var trace;
  var _sdAuthorizationManagementService;
  var _$q;
  var _sdUtilService;
  var _sdMessageService;
  var i18n;

  /**
   * 
   */
  function PTCtrl($scope, $q, sdLoggerService, sdUtilService, sdI18nService,
          sdMessageService, sdAuthorizationManagementService) {
    trace = sdLoggerService.getLogger('admin-ui.sdPermissionTreeCtrl');
    _$q = $q;
    _sdUtilService = sdUtilService;
    _sdMessageService = sdMessageService;

    i18n = $scope.sdI18nHtml5Admin = sdI18nService
            .getInstance('html5-admin-portal').translate;

    _sdAuthorizationManagementService = sdAuthorizationManagementService;

    this.refreshPermissions();

    PTCtrl.prototype.safeApply = function() {
      sdUtilService.safeApply($scope);
    }
  }

  PTCtrl.prototype.refreshPermissions = function() {
    alert('Refresh Permissions tree');
  };

  PTCtrl.prototype.highlightParticipants = function() {
    alert('highlight participant');
  }

})();