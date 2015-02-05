/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
/**
 * @author Johnson.Quadras
 */
(function()
{
   'use strict';

   angular.module('workflow-ui.services').provider('sdPriorityService', function()
   {
      this.$get = [ '$q', 'sgI18nService', function($q, sgI18nService)
      {
         var service = new PriorityService($q, sgI18nService);
         return service;
      } ];
   });

   /**
    * 
    */
   function PriorityService($q, sgI18nService)
   {

      this.priorities = [
               {
                  "value" : "-1",
                  "label" : sgI18nService
                           .translate('views-common-messages.common-priorities-low'),
                  "name" : "low"
               },
               {
                  "value" : "0",
                  "label" : sgI18nService
                           .translate('views-common-messages.common-priorities-normal'),
                  "name" : "normal"
               },
               {
                  "value" : "1",
                  "label" : sgI18nService
                           .translate('views-common-messages.common-priorities-high'),
                  "name" : "high"
               } ];

      PriorityService.prototype.getAllPriorities = function()
      {
         var deferred = $q.defer();
         deferred.resolve(this.priorities);
         return deferred.promise;
      };

   }

})();
