/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others. All rights reserved. This
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

  angular.module('shell').controller('sd.common.controller',
          ['$scope', 'sdI18nService', function($scope, sdI18nService) {
            $scope.sdI18n = sdI18nService.translate;
          }])
})();
