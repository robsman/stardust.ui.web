/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/**
 * @author Johnson.Quadras
 */
(function() {
   'use strict';

   angular.module('workflow-ui.services').provider('sdPriorityService', function() {
      this.$get = [ '$q', 'sgI18nService', '$resource', function($q, sgI18nService, $resource) {
         var service = new PriorityService( $q, sgI18nService, $resource);
         return service;
      } ];
   });
   
	var REST_BASE_URL = "services/rest/portal/process-instances/";
   /**
    * 
    */
   function PriorityService( $q, sgI18nService,  $resource) {

      this.priorities = [
               {
                  "value" : -1,
                  "label" : sgI18nService
                           .translate('views-common-messages.common-priorities-low'),
                  "name" : "low",
                  "order": 3,
               },
               {
                  "value" : 0,
                  "label" : sgI18nService
                           .translate('views-common-messages.common-priorities-normal'),
                  "name" : "normal",
                  "order": 2,
               },
               {
                  "value" : 1,
                  "label" : sgI18nService
                           .translate('views-common-messages.common-priorities-high'),
                  "name" : "high",
                  "order": 1,
               } ];
      
      /**
       * 
       */
      PriorityService.prototype.getAllPriorities = function() {
         var deferred = $q.defer();
         deferred.resolve(this.priorities);
         return deferred.promise;
      };
      
      /**
       * 
       */
      PriorityService.prototype.savePriorityChanges = function( value) {
          var restUrl = REST_BASE_URL + ":method";
          
          var priority = $resource(restUrl, {
        	  method : '@method'
           }, {
        	   updatePriorities : {
                 method : 'POST'
              }
           });
          var urlTemplateParams = {method : 'updatePriorities'};
          return priority.updatePriorities(urlTemplateParams, {'priorities': value}).$promise;
       };
   };
})();
