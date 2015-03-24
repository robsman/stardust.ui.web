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

   angular.module('bpm-common').directive( 'sdParticipantFilter',[ 'sdUtilService','sdActivityInstanceService','$q', ParticipantFilter]);

		/*
		*/
   function ParticipantFilter( sdUtilService , sdActivityInstanceService , $q ) {
	   return {
		   restrict : 'A',
		   template : '<div sd-participant-selector '+
				 		'sda-selected-data="selectedParticipants" '+
				 		'sda-multiple="true" sd-data="participantFilterCtrl.fetchParticipants(params)">'+
				 	  '</div>',
		   controller : [ '$scope', 'sdActivityInstanceService','$q',  ParticipantFilterCtrl ],
		   link : function( scope, element, attr, ctrl) {
            /*
             * 
             */
        	 scope.handlers.applyFilter = function() {
        		 var filterValues = [];
        		 var displayText = [];
        		 angular.forEach(scope.selectedParticipants, function( participant ) {
        			displayText.push(participant.name);
        			filterValues.push(participant)
        		 });
        		 var title = displayText.join(',');
        		 scope.setFilterTitle(sdUtilService.truncateTitle(title));
        		 scope.filterData.participants = filterValues;
        		 return true;
        	 };
         }
      };
   }
   
   
   /*
    * 
    */
   var ParticipantFilterCtrl = function( $scope , sdActivityInstanceService, $q) {

      $scope.participantFilterCtrl = this;
      $scope.selectedParticipants = [];
      
      /*
       * 
       */
      ParticipantFilterCtrl.prototype.fetchParticipants = function(options) {

  		var query = {};
  		var deferred = $q.defer();
  		self.participants = {};
  		var matchVal = options;

  		sdActivityInstanceService.getMatchingParticpants( matchVal, 15).then(function(data) {

  			self.participants.list = data;
  			self.participants.totalCount = data.length;
  			deferred.resolve(data);
  		}, function(result) {
  			// Error occurred
  			trace.log('An error occurred while fetching participants.\n Caused by: ' + result);
  			deferred.reject(result);
  		});

  		return deferred.promise;
  	};

   };
   
	
 
})();
