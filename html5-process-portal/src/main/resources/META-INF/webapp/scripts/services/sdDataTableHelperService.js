/*****************************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ****************************************************************************************/

/**
 * @author johnson.quadras
 */
(function() {
	'use strict';
	angular.module('workflow-ui.services').provider('sdDataTableHelperService',function() {
		this.$get = ['$filter', function($filter) {
			var service = new TableHelperService($filter);
			return service;
		}];
	});

	/*
	 * 
	 */
   function TableHelperService( $filter) {
	   /**
	    * Construct the query params from the options
	    */
	   TableHelperService.prototype.convertToQueryParams = function(options){
		   		
		   	var queryParams = "";
	       
		   	if (options.skip != undefined) {
	        	 queryParams += "&skip=" + options.skip;
	         }
	        
		   	if (options.pageSize != undefined) {
	        	 queryParams += "&pageSize=" + options.pageSize;
	         }
	        
		   	if (options.order != undefined) {
	            // Supports only single column sort
	            var index = options.order.length - 1;
	            queryParams += "&orderBy=" + options.order[index].name;
	            queryParams += "&orderByDir=" + options.order[index].dir;
	         }
	         return queryParams
	   };
	   
	   /**
	    * Construct the post params from the options
	    */
	   TableHelperService.prototype.convertToPostParams = function(options){

		   var postData = {
				   filters : options.filters,
				   descriptors : {
					   fetchAll : false,
					   visibleColumns : []
				   }
		   };

		   var found = $filter('filter')(options.columns, {
			   field : 'descriptors'
		   }, true);

		   if (found && found.length > 0) {
			   postData.descriptors.fetchAll = true;
		   }

		   var visibleDescriptors = [];
		   
		   angular.forEach(options.descriptorColumns,function(descriptor){
		      var found =  $filter('filter')(options.columns, { name : descriptor.key
		       },true);
		      
		      if(found && found.length > 0){
			  visibleDescriptors.push(descriptor.key);
		      }
		   });

		   if (visibleDescriptors) {
		       postData.descriptors.visibleColumns = visibleDescriptors;
		   }
		   return postData;
	   };
   };
   
})();
