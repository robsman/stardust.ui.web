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
      this.$get = [ '$rootScope', '$resource','sdDataTableHelperService', function($rootScope, $resource, sdDataTableHelperService) {
         var service = new WorklistService($rootScope, $resource, sdDataTableHelperService);
         return service;
      } ];
   });
   /*
    * 
    */
   function WorklistService($rootScope, $resource, sdDataTableHelperService) {
	   var REST_BASE_URL = "services/rest/portal/worklist/";
	   /*
	    * 
	    */
	   WorklistService.prototype.getWorklist = function(query) {

		   var restUrl = "";
		   
		   if(query.url) {
			   restUrl = query.url;
		   }else {
			   restUrl = REST_BASE_URL;
		   }
		   
		   // Prepare path Params
		   var urlTemplateParams = {};
		   if (query.criticality) {
		       	   restUrl = restUrl  + ":type/:id";
			   urlTemplateParams.type = "criticality";
			   urlTemplateParams.id = query.criticality;
		   }else  if (query.fromDate) {
			   restUrl = restUrl  + ":type/:id";
			   urlTemplateParams.type = "date";
			   urlTemplateParams.id = query.fromDate;
		   }else if (query.processQId) {
			   restUrl = restUrl  + ":type/:id";
			   urlTemplateParams.type = "process";
			   urlTemplateParams.id = query.processQId;
		   }else if (query.participantQId) {
			   restUrl = restUrl  + ":type/:id";
			   urlTemplateParams.type = "participant";
			   urlTemplateParams.id = query.participantQId;
		   }else if (query.userId) {
			   restUrl = restUrl  + ":type/:id";
			   urlTemplateParams.type = "user";
			   urlTemplateParams.id = query.userId;
		   }else{
			   if(!query.url){
				   throw "Illegal type passed to getWorklist : "+query;
			   }
		   }
		   
		   if(query.queryParams){
		       restUrl = restUrl+ '?'+ query.queryParams;
		   }
		   
		   var queryParams = sdDataTableHelperService.convertToQueryParams(query.options);

		   if (queryParams.length > 0) {
			   var separator = "?";
			   if(/[?]/.test(restUrl)){
				   separator =  "&";
			   }
			   restUrl = restUrl + separator + queryParams.substr(1);
		   }
		   var postData = sdDataTableHelperService.convertToPostParams(query.options);
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
