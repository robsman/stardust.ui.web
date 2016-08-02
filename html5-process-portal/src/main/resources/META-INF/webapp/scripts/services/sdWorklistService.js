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

(function() {
   'use strict';

   angular.module('workflow-ui.services').constant('sdWorklistConstants', {
    Types: {
        HIGH_CRITICALITY : 'highCriticality',
        TOTAL : 'total',
        PERSONAL : 'personal',
        UNIFIED : 'unified',
        WORK_SINCE : 'myWorkSince',
        RESUBMISSION : 'resubmission',
        ALIVE : 'alive',
        USER : 'user'
    }});

   angular.module('workflow-ui.services').provider('sdWorklistService', function() {
      this.$get = [ '$rootScope', '$resource','sdDataTableHelperService', 'sdUtilService','sdWorklistConstants','sdLoggerService','$q',
       function($rootScope, $resource, sdDataTableHelperService, sdUtilService, sdWorklistConstants, sdLoggerService, $q) {
         var service = new WorklistService($rootScope, $resource, sdDataTableHelperService, sdUtilService, sdWorklistConstants, sdLoggerService, $q);
         return service;
      } ];
   });
   /*
    *
    */
   function WorklistService($rootScope, $resource, sdDataTableHelperService, sdUtilService, sdWorklistConstants, sdLoggerService, $q) {
	   var REST_BASE_URL = "services/rest/portal/worklist/";
	   var trace = sdLoggerService.getLogger('workflow-ui.services.sdWorklistService')
	   /*
	    *
	    */
	   WorklistService.prototype.getWorklist = function(query) {


		   var restUrl = sdUtilService.getBaseUrl();
		   var urlTemplateParams = {};

		   if(query.url) {
			   restUrl = restUrl + query.url;
		   }else {
			   restUrl = restUrl + REST_BASE_URL;
		   }

		   var postData = sdDataTableHelperService.convertToPostParams(query.options);
		   postData.worklistId = query.id;

		   if (query.type) {

			   if(query.type === sdWorklistConstants.Types.HIGH_CRITICALITY) {
				   restUrl = restUrl  + ":type/:id";
				   urlTemplateParams.type = "criticality";
				   urlTemplateParams.id = "high";

			   } else if(query.type === sdWorklistConstants.Types.TOTAL) {
				   restUrl = restUrl + "allAssigned";

			   }  else if(query.type === sdWorklistConstants.Types.PERSONAL) {
				   restUrl = restUrl + "personalItems";

			   } else if(query.type === sdWorklistConstants.Types.UNIFIED) {
				   if( query.userId ) {
					   restUrl = restUrl + "unified/:type/:id";
					   urlTemplateParams.type = "user";
					   urlTemplateParams.id = query.userId;
				   }else {
					   restUrl = restUrl + "unified";
				   }
			   } else if(query.type === sdWorklistConstants.Types.WORK_SINCE) {
				   restUrl = restUrl  + ":type/:id";
				   urlTemplateParams.type = "date";
				   urlTemplateParams.id = query.from;

			   } else if(query.type ===  sdWorklistConstants.Types.RESUBMISSION) {
				   restUrl = restUrl  + "resubmissionActivities";

			   } else if(query.type ===  sdWorklistConstants.Types.ALIVE) {
				   restUrl = restUrl  + "alive";

			   } else if (query.type === sdWorklistConstants.Types.USER) {
				   restUrl = restUrl + ":type/:id";
				   urlTemplateParams.type = "user";
				   urlTemplateParams.id = query.userId;
				   var fetchParams = "fetchAllStates="+true;
				   restUrl = sdDataTableHelperService.appendQueryParamsToURL(restUrl, fetchParams);
			   }
		   } else if (query.participantQId) {
			   restUrl = restUrl  + ":type";
			 
			   var userParams = "userId="+query.userId;
			   restUrl = sdDataTableHelperService.appendQueryParamsToURL(restUrl, userParams);
			   
			   urlTemplateParams.type = "participant";
			   postData.participantQId = query.participantQId;
			   
			   if(query.departmentQId) {
				   postData.departmentQId =  query.departmentQId;
			   }
		   } else if (query.processQId) {
			   restUrl = restUrl  + ":type/:id";
			   urlTemplateParams.type = "process";
			   urlTemplateParams.id = query.processQId;

		   } else if(query.pInstanceOids ){
			   restUrl = restUrl  + ":type";
			   urlTemplateParams.type = "processInstance";
			   var oidParam = "oids="+query.pInstanceOids;
			   restUrl = sdDataTableHelperService.appendQueryParamsToURL(restUrl, oidParam);
		   }  else if(query.userId){
			   restUrl = restUrl + ":type/:id";
			   urlTemplateParams.type = "user";
			   urlTemplateParams.id = query.userId;

		   } else {
			   if(!query.url){
				   trace.error("Illegeal Query attribute", query);
				   var def = $q.defer();
				   def.reject("Illegal query passed to getWorklist");
				   return def.promise;
			   }
		   }


		   if(query.queryParams){
			   restUrl = sdDataTableHelperService.appendQueryParamsToURL(restUrl, queryParams);
		   }


		   //Checking for deputy userId along with participant Id in case of deputy user
		   if( query.participantQId && query.userId ) {
			   restUrl = sdDataTableHelperService.appendQueryParamsToURL(restUrl,  'userId='+query.userId);
		   }

		   addCustomizationParameters(postData, query);

		   var queryParams = sdDataTableHelperService.convertToQueryParams(query.options);
		   if (queryParams.length > 0) {
			   restUrl = sdDataTableHelperService.appendQueryParamsToURL(restUrl, queryParams.substr(1));
		   }

		   var worklist = $resource(restUrl, {
			   type : '@type',
			   id : '@id'
		   }, {
			   fetch : {
				   method : 'POST'
			   }
		   });

		   return worklist.fetch(urlTemplateParams, postData).$promise;
	   };
	   
	   /**
	    * 
	    */
	   function addCustomizationParameters(postData, query) {
		   //adding descriptor values
		   if(query.fetchDescriptors) {
			   if( typeof(query.fetchDescriptors) === "boolean") {
				   if(query.fetchDescriptors) {
					   postData.descriptors.fetchAll = query.fetchDescriptors;
				   }
			   }else  if( angular.isArray(query.fetchDescriptors)) {
				   if(query.fetchDescriptors) {
					   postData.descriptors.visibleColumns = postData.descriptors.visibleColumns.concat(query.fetchDescriptors);
				   }
			   }
		   }

		   //Adding sort Criteria
		   if(query.sortBy) { 
			   query.options.order = query.sortBy;
		   }
		   
		   //Adding filter Criteria
		   if(query.filterBy) {
			   if(!postData.filters) {
				   postData.filters = {};
			   }
			   angular.merge(postData.filters, query.filterBy);
		   }
	   }
   }

})();
