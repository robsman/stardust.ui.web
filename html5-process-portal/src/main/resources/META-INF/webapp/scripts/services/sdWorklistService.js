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

   angular.module('workflow-ui.services').provider('sdWorklistService', function() {
      this.$get = [ '$rootScope', '$resource','sdDataTableHelperService', 'sdUtilService', function($rootScope, $resource, sdDataTableHelperService, sdUtilService) {
         var service = new WorklistService($rootScope, $resource, sdDataTableHelperService, sdUtilService);
         return service;
      } ];
   });
   /*
    *
    */
   function WorklistService($rootScope, $resource, sdDataTableHelperService, sdUtilService) {
	   var REST_BASE_URL = "services/rest/portal/worklist/";

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

	   	if (query.type) {

				if(query.type === "highCriticality") {
					 restUrl = restUrl  + ":type/:id";
			   	 urlTemplateParams.type = "criticality";
			  	 urlTemplateParams.id = "high";

				} else if(query.type === "allAssigned") {
						restUrl = restUrl + "allAssigned";

				}  else if(query.type === "personal") {
						restUrl = restUrl + "personalItems";

				} else if(query.type === "unified") {
						restUrl = restUrl + "unified/:type/:id";
						urlTemplateParams.type = "user";
			  	  urlTemplateParams.id = query.userId;

				} else if(query.type === "myWorkSince") {
					 restUrl = restUrl  + ":type/:id";
			 	   urlTemplateParams.type = "date";
			  	 urlTemplateParams.id = query.from;

				} else if(query.type === "resubmission") {
					 restUrl = restUrl  + "resubmissionActivities";

				} else if(query.type === "allActivable") {
					 restUrl = restUrl  + "allActivable";

				} else if(query.type === "processInstances"){
					 restUrl = restUrl  + ":type";
		  		 urlTemplateParams.type = "processInstance";
		  		 var params = "oids="+query.pInstanceOids;
		   		 restUrl = sdDataTableHelperService.appendQueryParamsToURL(restUrl, params);
				} else if (query.type === "allStates") {
						restUrl = restUrl + ":type/:id";
						urlTemplateParams.type = "user";
			  	  urlTemplateParams.id = query.userId;
			  	  var params = "fetchAllStates="+true;
		   		  restUrl = sdDataTableHelperService.appendQueryParamsToURL(restUrl, params);
				}
			} else if (query.participantQId) {
				 restUrl = restUrl  + ":type/:id";
			   urlTemplateParams.type = "participant";
			   urlTemplateParams.id = query.participantQId;

			} else if (query.processQId) {
				 restUrl = restUrl  + ":type/:id";
			   urlTemplateParams.type = "process";
			   urlTemplateParams.id = query.processQId;

			} else if(query.userId){
					restUrl = restUrl + ":type/:id";
					urlTemplateParams.type = "user";
		  	  urlTemplateParams.id = query.userId;
		  	  var params = "fetchAllStates="+true;
	   		  restUrl = sdDataTableHelperService.appendQueryParamsToURL(restUrl, params);

			} else {
				if(!query.url){
				   throw "Illegal type passed to getWorklist : "+query;
			   }
			}


		   if(query.queryParams){
			   restUrl = sdDataTableHelperService.appendQueryParamsToURL(restUrl, queryParams);
		   }


		   //Checking for deputy userId along with participant Id in case of deputy user
		   if( query.participantQId && query.userId ) {
			   restUrl = sdDataTableHelperService.appendQueryParamsToURL(restUrl,  'userId='+query.userId);
		   }


		   var queryParams = sdDataTableHelperService.convertToQueryParams(query.options);

		   if (queryParams.length > 0) {
			   restUrl = sdDataTableHelperService.appendQueryParamsToURL(restUrl, queryParams.substr(1));
		   }

		   var postData = sdDataTableHelperService.convertToPostParams(query.options);
		   postData['worklistId'] = query.id;

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
   }

})();
