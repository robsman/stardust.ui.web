/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Zach McCain
 */

'use strict';

(function(){
	
	//Closures for our dependencies injected by the DI subsystem.
	var _sdViewUtilService,
		_sdUserService,
		_$q,
		_eventBus,
		_ngDialog;
	
	var controller = function(sdViewUtilService,sdUserService,$q,eventBus,ngDialog){
		_sdViewUtilService=sdViewUtilService;
		_sdUserService = sdUserService;
		_$q=$q;
		_eventBus=eventBus;
		_ngDialog=ngDialog;
	}
	
	controller.prototype.init = function(){
		var that = this;
		this.data={};
		
		_$q.all([_sdUserService.getUsers(),
		         _sdUserService.getParticipants()])
		.then(function(values) {  
			that.data.users = values[0];
			that.data.participants = values[1];
        })
        .catch(function(err){
        	//TODO: handle errors.
        });
	};
	
	controller.prototype.onOpen = function(p){
		alert(p);
	};
	
	controller.prototype.getPeople = function(){
		var that = this;
		_sdUserService.getPeople()
		.then(function(data){
			that.people = data;
		});
	};
	
	controller.prototype.generateError = function(){
		x.message ="error";
	};
	
	angular.module('admin-ui')
	.controller('sdParticipantManagementViewController', 
			['sdViewUtilService', 'sdUserService', '$q', 'eventBus', 'ngDialog',controller]);
	
})();
