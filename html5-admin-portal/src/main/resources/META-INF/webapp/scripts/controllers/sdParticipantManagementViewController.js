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
		_eventBus;
	
	var controller = function(sdViewUtilService,sdUserServiceMock,$q,eventBus){
		_sdViewUtilService=sdViewUtilService;
		_sdUserService = sdUserServiceMock;
		_$q=$q;
		_eventBus=eventBus;
		
		this.dataSelected=[];
		this.matchVal="";
		this.data={};
		this.matchData=[];
		this.message = "Hello World";
		this.testShow = false;
	}
	
	
	controller.prototype.getMatches = function(val){
		var results=[],
		    data = [
		            {"id": "1", "name" : "Alabama", "category" : "Part1"},
		            {"id": "2", "name" : "Alaska", "category" : "Part1"},
		            {"id": "3", "name" : "Arizona", "category" : "Part1"},
		            {"id": "4", "name" : "Arkansas", "category" : "Part2"},
		            {"id": "5", "name" : "California", "category" : "Part2"},
		            {"id": "6", "name" : "Conneticut", "category" : "Part2"},
		            {"id": "7", "name" : "Colorado", "category" : "Part3"},
		            {"id": "8", "name" : "Delaware", "category" : "Part3"},
		            {"id": "9", "name" : "Florida", "category" : "Part3"},
		            {"id": "10", "name" : "Georgia", "category" : "Part4"},
		            {"id": "11", "name" : "Idaho", "category" : "Part4"},
		            {"id": "12", "name" : "Iowa", "category" : "Part2"},
		            {"id": "13", "name" : "Illinois", "category" : "Part4"},
		            {"id": "14", "name" : "Indiana", "category" : "Part1"},
		            {"id": "15", "name" : "Kansas", "category" : "Part1"},
		            {"id": "16", "name" : "Louisiana", "category" : "Part2"},
		            {"id": "17", "name" : "Maine", "category" : "Part2"},
		            {"id": "18", "name" : "Montana", "category" : "Part2"},
		            {"id": "19", "name" : "Michigan", "category" : "Part3"},
		            {"id": "20", "name" : "New York", "category" : "Part3"},
		            {"id": "21", "name" : "Mississippi", "category" : "Part4"}
		          ];
		
		data.forEach(function(v){
			if(v.name.indexOf(val) > -1){
				results.push(v);
			}
		});
			
		this.matchData= results;
	};
	/**
	 * Initialization routine for the controller
	 * @this {controller}
	 */
	controller.prototype.init = function(){
		var that = this;
		
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
	
	/**
	 * Call back function for our info dialogs
	 * @this {controller}
	 * @param {Object} res - promise returned from sd-dialog
	 */
	controller.prototype.onInfoOpen = function(res){
		var promise = res.promise;
		promise.then(function(){
			console.log("Dialog closed...");
		});
	};
	
	/**
	 * Call back function for our confirm dialogs
	 * @this {controller}
	 * @param {Object} res - promise returned from sd-dialog
	 */
	controller.prototype.onConfirmOpen = function(res){
		var promise = res.promise;
		promise.then(function(data){
			console.log("dialog state: confirmed");
		})
		.catch(function(){
			console.log("dialog state: rejected");
		});
	};
	
	controller.prototype.getPeople = function(){
		var that = this;
		_sdUserService.getPeople()
		.then(function(data){
			that.people = data;
			that.message = "Retrieved Data";
			that.testShow=true;
		});
	};
	
	controller.prototype.generateError = function(){
		x.message ="error";
	};
	
	angular.module('admin-ui')
	.controller('sdParticipantManagementViewController', 
			['sdViewUtilService', 'sdUserServiceMock', '$q', 'eventBus',controller]);
	
})();
