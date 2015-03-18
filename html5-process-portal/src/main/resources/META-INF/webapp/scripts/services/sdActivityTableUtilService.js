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

   angular.module('workflow-ui.services').provider('sdActivityTableUtilService', function()
   {
      this.$get = [ '$filter', function( $filter)
      {
         var service = new UtilService( $filter);
         return service;
      } ];
   });

   /*
    * 
    */
   function UtilService( $filter) {
	   
	   /**
	    * Construct the query params from the options
	    */
	   UtilService.prototype.getQueryParamsFromOptions = function(options){
		   		
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
	   UtilService.prototype.getPostParamsFromOptions = function(options){

		   var postData = {
				   filters : options.filters,
				   descriptors : {
					   fetchAll : false,
					   visbleColumns : []
				   }
		   };

		   var found = $filter('filter')(options.columns, {
			   field : 'descriptors'
		   }, true);

		   if (found && found.length > 0) {
			   postData.descriptors.fetchAll = true;
		   }

		   var descriptorColumns = $filter('filter')(options.columns, {
			   name : 'descriptorValues'
		   });

		   if (descriptorColumns) {
			   angular.forEach(descriptorColumns, function(column) {
				   postData.descriptors.visbleColumns.push(column.name);
			   });
		   }
		   
		   return postData;
	   };

   };
   
 
   
   
})();
