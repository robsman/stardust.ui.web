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
 * @author Subodh.Godbole
 */

(function()
{
   'use strict';

   angular.module('workflow-ui.services').provider('sdWorklistService', function()
   {
      this.$get = [ '$rootScope', '$resource','$filter', function($rootScope, $resource, $filter)
      {
         var service = new WorklistService($rootScope, $resource, $filter);
         return service;
      } ];
   });

   /*
    *
    */
   function WorklistService($rootScope, $resource, $filter)
   {
      var REST_BASE_URL = "services/rest/portal/worklist/";
      /*
       *
       */
      WorklistService.prototype.getWorklist = function(query)
      {
         // Prepare URL
         var restUrl = REST_BASE_URL + ":type/:id";

      // Add Query String Params. TODO: Can this be sent as stringified JSON?
         var options = "";
         if (query.options.skip != undefined) {
            options += "&skip=" + query.options.skip;
         }
         if (query.options.pageSize != undefined) {
            options += "&pageSize=" + query.options.pageSize;
         }
         if (query.options.order != undefined) {
            // Supports only single column sort
            var index = query.options.order.length - 1;
            options += "&orderBy=" + query.options.order[index].name;
            options += "&orderByDir=" + query.options.order[index].dir;
         }

         if (options.length > 0) {
            restUrl = restUrl + "?" + options.substr(1);
         }
         
         var postData ={
                  filters : query.options.filters,
                  descriptors : {
                     fetchAll : false,
                     visbleColumns : []
                  }
          };
         

         var found = $filter('filter')(query.options.columns, {
            field : 'descriptors'
         }, true);

         if (found && found.length > 0)
         {
            postData.descriptors.fetchAll = true;
         }
         
          var descriptorColumns = $filter('filter')(query.options.columns, {
            name : 'descriptorValues'
         });

         if (descriptorColumns)
         {
            angular.forEach(descriptorColumns,function(column){
               postData.descriptors.visbleColumns.push(column.name);
            });
         }
         
        
         var worklist = $resource(restUrl, {
            type : '@type',
            id : '@id'
         }, {
            fetch : {
               method : 'POST'
            }
         });

         // Prepare Query Params
         var urlTemplateParams = {};
         if (query.participantQId)
         {
            urlTemplateParams.type = "participant";
            urlTemplateParams.id = query.participantQId;
         }
         else if (query.userId)
         {
            urlTemplateParams.type = "user";
            urlTemplateParams.id = query.userId;
         }

         return worklist.fetch(urlTemplateParams, postData).$promise;
         ;
      };
   }
   ;
})();
