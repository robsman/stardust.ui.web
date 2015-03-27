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

   angular.module('bpm-common').directive( 'sdUserFilter',[ 'sdUtilService', UserFilterDirective]);
   /*
    * 
    */
   function UserFilterDirective( sdUtilService ) {
	   return {
		   restrict : 'A',
		   template : '<div sd-user-selector sda-selected-data="filterData.participants"'+
			   				'sda-active-only="true" sda-multiple="false" sda-max="20">'+
		   			  '</div>',
		   link : function( scope, element, attr, ctrl) {
            /*
             * 
             */
        	 scope.handlers.applyFilter = function() {
        		 var displayText = [];
        		 angular.forEach(scope.filterData.participants, function( participant ) {
        			displayText.push(participant.name);
        		 });
        		 var title = displayText.join(',');
        		 scope.setFilterTitle(sdUtilService.truncateTitle(title));
        		 return true;
        	 };
         }
      };
   };
})();
