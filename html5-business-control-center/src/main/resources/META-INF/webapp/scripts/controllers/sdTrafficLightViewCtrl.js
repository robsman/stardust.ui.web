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
 * @author Johnson.Quadras
 */

(function() {
	'use strict';

	angular.module("bcc-ui").controller('sdTrafficLightViewCtrl',
			['$q','sdActivityInstanceService','sdTafficLightService', 'sdLoggerService', '$filter','sgI18nService', 'sdLoggedInUserService', Controller]);

	var _sdActivityInstanceService = null;
	var _sdCommonViewUtilService = null;
	var _q = null;
	var _sdTafficLightService = null;
	var trace = null;
	var _filter = null;
	var _sgI18nService = null;

	/**
	 * 
	 */
	function Controller($q, sdActivityInstanceService, sdTafficLightService, sdLoggerService, $filter,
			sgI18nService, sdLoggedInUserService) {

		_sdTafficLightService = sdTafficLightService;
		trace = sdLoggerService.getLogger('bcc-ui.sdTrafficLightViewCtrl');
		_filter = $filter;
		_sgI18nService = sgI18nService;
		_q = $q;
		_sdActivityInstanceService = sdActivityInstanceService;

		this.columnSelector = sdLoggedInUserService.getUserInfo().isAdministrator ? 'admin': true;
		this.exportFileName = "Traffic Light view";
		this.columns = [];
		this.ready = false;
		this.dataTable = null;
		this.data = null;
		this.processes = [];
		this.selected = {
				process : '',
				category :'',
		};

		this.createTable = false;
		this.detailsTable = false;

		this.completedActivitiesTable = null;
		this.pendingActivitiesTable = null;

		this.getAllProcesses();

		this.detailsView = {
				activityName : "",
				completedOids :[],
				pendingOids : []
		};
	};

	/**
	 * 
	 */
	Controller.prototype.getAllProcesses = function() {
		trace.debug("Fetching the procceses configured with Traffic Light view.");
		var self = this;
		_sdTafficLightService.getProcesses()
		.then(
				function(result) {
					self.processes = result;
				});
	};

	/**
	 * 
	 */
	Controller.prototype.getCategories = function(processQId) {
		var self = this;
		trace.debug("Fetching the categories for the process : ",processQId);
		_sdTafficLightService.getCategories(processQId)
		.then(
				function(result) {
					self.categories = result;
				});
	};

	/**
	 * 
	 */
	Controller.prototype.onProcessSelect = function( ) {
		var self = this;
		if(!self.selected.process){
			return;
		}
		self.ready = false;
		this.getCategories(self.selected.process);
		this.getTrafficLightViewColumns(self.selected.process);
	};

	/**
	 * 
	 */
	Controller.prototype.getTrafficLightViewColumns = function( processQId ) {
		var self = this;
		if(!processQId){
			return;
		}
		this.createTable = false;
		_sdTafficLightService.getTrafficLightViewColumns(processQId)
		.then(
				function(result) {
					self.columns = result;
					self.ready = true;
					self.createTable = true;
				});
	};

	/**
	 * 
	 */
	Controller.prototype.fetchData = function( params ) {
		var result = {
				list : [],
				totalCount : 0,
		}
		var deferred = _q.defer();
		deferred.resolve(result);
		return deferred.promise;
	};

	/**
	 * 
	 */
	Controller.prototype.showDetails = function(/*activityName, completedActivities, pendingActivities */) {
		this.detailsTable = true;
		
		/*this.detailsView.completedOids = [completedActivities];
		this.detailsView.pendingOids = [pendingActivities];*/
		this.detailsView.activityName = "Activityy 11";
		this.detailsView.completedOids = [86];
		this.detailsView.pendingOids = [82];
		
		
	};

	/**
	 * 
	 */
	Controller.prototype.getCompletedActivitiesByOids = function( params ) {
		var self = this;
		var deferred = _q.defer();
		var activities = {};
		_sdActivityInstanceService.getByOids(params, self.detailsView.completedOids).then(function(data) {
			activities.list = data.list;
			activities.totalCount = data.totalCount;
			deferred.resolve(activities);
		}, function(error) {
			deferred.reject(error);
		});
		return deferred.promise;
	};

	/**
	 * 
	 */
	Controller.prototype.getPendingActivitiesByOids = function( params ) {
		var self = this;
		var deferred = _q.defer();
		var activities = {};
		_sdActivityInstanceService.getByOids(params, self.detailsView.pendingOids).then(function(data) {
			activities.list = data.list;
			activities.totalCount = data.totalCount;
			deferred.resolve(activities);
		}, function(error) {
			deferred.reject(error);
		});
		return deferred.promise;
	};


})();