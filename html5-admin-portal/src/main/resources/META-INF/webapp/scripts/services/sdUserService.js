/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/*
 * @author Subodh.Godbole
 */

'use strict';

angular.module('admin-ui.services').provider('sdUserServiceMock', function () {
	var self = this;
	
	self.$get = ['$rootScope', '$q', '$http', function ($rootScope, $q, $http) {

		var service = {};

		/*
		 * 
		 */
		service.getParticipants = function(){
			
			var deferred = $q.defer(),
				data;
			
			data=[
			      {name: 'Administrator', users:['motu2','motu3']},
			      {name: 'Fund Manager', users:['motu2','motu4']},
			      {name: 'Document Indexing Operator', users:['motu2']},
			      {name: 'Claims Processor', users:[]},
			      {name: 'Bunny Fluffer', users:['motu2','motu3','motu4']}
			];
			
			deferred.resolve(data);
			
			return deferred.promise;
		};
		
		service.getPeople = function(){
			
			var delay=(Math.random()*5)|0,
			    deferred = $q.defer();
			    
		    $http({
		      method:'POST',
		      url:'http://schematic-ipsum.herokuapp.com/?n=10',
		      data :{ "type": "object", "properties": { "id": { "type": "string" }, "name": { "type": "string" }, "email": { "type": "string", "format": "email" }, "bio": { "type": "string" }, "age": { "type": "integer" } } }
		    })
		    .success(function(data){
		    	deferred.resolve(data);
		    })
		    .error(function(){
		    	deferred.reject(data);
		    });
		    
		    return deferred.promise;
		};
		
		/**
		 * Get All Users or alternately
		 */
		service.getUsers = function(userId){
			var deferred = $q.defer(),
				data;
			
			//TODO: ZZM - Remove once enpoint is wired up.
			data=[
			      {	name:{last:'Of the Universe',first:'Captain'},
		    	    oid: 2,
		    	    account:'motu2',
		    	    validFrom:'1/1/2000',
		    	    validTo:'1/1/2100',
		    	    email: 'motu2@universe.org'},
			      {	name:{last:'Of the Universe',first:'Colonel'},
		    	    oid: 2,
		    	    account:'motu3',
		    	    validFrom:'1/1/2000',
		    	    validTo:'1/1/2100',
		    	    email: 'motu3@universe.org'},	
			      {	name:{last:'Of the Universe',first:'Major'},
		    	    oid: 2,
		    	    account:'motu4',
		    	    validFrom:'1/1/2000',
		    	    validTo:'1/1/2100',
		    	    email: 'motu4@universe.org'}
			];
			
			//TODO: ZZM - remove hard coded resolve
			deferred.resolve(data);
			
			//TODO: ZZM - find actual endpoint and wireup
			/*
			userId = userId || '';
			
			$http({
				method: 'GET',
				url: 'services/rest/portal/users/' + userId
			})
			.success(function(data){
				deferred.resolve(data);
			})
			.error(function(err){
				deferred.reject(err);
			});*/
			
			return deferred.promise;
		};
		

		return service;
	}];
});
