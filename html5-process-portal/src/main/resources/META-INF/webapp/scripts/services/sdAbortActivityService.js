/*****************************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ****************************************************************************************/

/*
 * @author Johnson.Quadras
 */

(function() {
   'use strict';

   angular.module('workflow-ui.services').provider('sdAbortActivityService', function() {
      this.$get = [ '$q', '$http', function( $q, $http) {
         var service = new AbortActivityService( $q, $http);
         return service;
      } ];
   });

   /**
    * 
    */
   function AbortActivityService($q, $http) {
      var REST_URL = "services/rest/portal/activity-instances/abort";

      /**
       * 
       */
      AbortActivityService.prototype.abortActivities = function( scope, activities) {

         var deferred = $q.defer();
         var requestObj = {
            scope : scope,
            activities : activities
         };

         var httpResponse = $http.post(REST_URL, requestObj);

         httpResponse.success(function(data) {
            deferred.resolve(data);
         }).error(function(data) {
            deferred.reject(data);
         });

         return deferred.promise;
      };
   };

})();
