/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Abhay.Thappan
 */

(function() {
	'use strict';

	angular.module("bcc-ui").controller(
			'sdDeputyManagementCtrl',
			[ '$q', '$timeout', 'sdDeputyManagementService', 'sdLoggerService','sdUtilService',
			     DeputyManagementCtrl ]);

	var _q;
	var _sdDeputyManagementService;
	var trace;
	var _timeout;
	var _sdUtilService;
	var rootURL;

	/**
	 * 
	 */
	function DeputyManagementCtrl($q, $timeout, sdDeputyManagementService, sdLoggerService, sdUtilService) {
		trace = sdLoggerService.getLogger('bcc-ui.sdDeputyManagementCtrl');
		_q = $q;
		_sdDeputyManagementService = sdDeputyManagementService;
		_timeout = $timeout
		_sdUtilService = sdUtilService;
		
		rootURL = _sdUtilService.getRootUrl();

		this.usersTable = null;
		this.userDeputiesTable = null;
		//this.showUsersTable = true;
		this.showUserDeputiesTable= true
		this.deputies = [];
		//this.columnSelector = 'admin';
		//this.exportFileNameForRoleAssignment = "RoleAssignment"
		this.rowSelectionForUsers = null;
		this.showHideNonDepParticipant = true;
		this.getUsers();
		//this.getRoleAssignments();
	}

	/**
	 * 
	 * @returns
	 */
	DeputyManagementCtrl.prototype.getUsers = function() {
		//var deferred = _q.defer();
		var self=this;
		_sdDeputyManagementService.loadUsers().then(function(data) {
			self.allUsers = data.list;
			
			self.usersWithDeputies = [];
			
			angular.forEach(data.list, function(user) {
				if(user.hasDeputies){
					self.usersWithDeputies.push(user);
				}
			});	
			self.showUsersTable = true;
			//deferred.resolve(data.list);
		}, function(error) {
			trace.log(error);
			//deferred.reject(error);
		});
		
		//return deferred.promise;
	};
	
	/**
	 * 
	 */
	DeputyManagementCtrl.prototype.getUsersData = function() {
		var self = this;
		var deferred = _q.defer();
		if(self.showHideNonDepParticipant){
			deferred.resolve(self.allUsers);
		}else{
			deferred.resolve(self.usersWithDeputies);
		}
		
		return deferred.promise;
	};
	
	/**
	 * 
	 * @param userImageURI
	 * @returns
	 */
	DeputyManagementCtrl.prototype.getUserImageURL = function(userImageURI){
		return rootURL + userImageURI;
	};
	
	DeputyManagementCtrl.prototype.getDeputiesForUser = function(userOID){
		var self = this;
		_sdDeputyManagementService.loadDeputiesForUser(userOID).then(function(data) {
			self.deputies = data.list;
			if(self.userDeputiesTable != undefined){
				self.userDeputiesTable.refresh();
			}
		}, function(error) {
			trace.log(error);			
		});
	}
	
	/**
	 * 
	 * @param info
	 */
	DeputyManagementCtrl.prototype.onSelect = function(info){
		var self = this;
		if(info.action =="select"){
			self.getDeputiesForUser(info.current.userOID);
		}else{
			self.deputies = [];
			self.userDeputiesTable.refresh();
		}
	};
	
	/**
	 * 
	 * @param showHideIndicator
	 */
	DeputyManagementCtrl.prototype.showHideNonDeputyParticipant = function(showHideIndicator){
		var self = this;
		self.showHideNonDepParticipant = showHideIndicator;
		self.usersTable.refresh();
		self.deputies = [];
		self.userDeputiesTable.refresh();
	}

})();