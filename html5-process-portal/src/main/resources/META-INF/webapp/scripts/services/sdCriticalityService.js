/*****************************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ****************************************************************************************/
(function() {
   'use strict';

   angular.module('workflow-ui.services').provider('sdCriticalityService', function() {
      this.$get = [ '$q', '$resource', function($q, $resource) {
         var service = new CriticalityService($q, $resource);
         return service;
      } ];
   });

   /**
    * 
    */
   function CriticalityService($q, $resource) {
      var REST_URL = "services/rest/portal/activity-instances/availableCriticalities";

      CriticalityService.prototype.getAllCriticalities = function() {
         return $resource(REST_URL).query().$promise;
      };

   }

})();
