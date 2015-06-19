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

	angular.module("bcc-ui").controller('sdPostponedActivitiesCtrl',
			['sdActivityInstanceService', 'sdCommonViewUtilService', '$q', 'sdLoggerService', '$filter', 
			 'sgI18nService', 'sdLoggedInUserService','sdPreferenceService', PostponedActivitiesCtrl ]);


	var _sdActivityInstanceService = null;
	var _sdCommonViewUtilService = null;
	var _q = null;
	var trace = null;
	var _filter = null;
	var _sgI18nService = null;
	var _sdPreferenceService = null;
	
	/**
	 * 
	 */
	function PostponedActivitiesCtrl( sdActivityInstanceService, sdCommonViewUtilService, $q, 
									  sdLoggerService, $filter, sgI18nService, sdLoggedInUserService, sdPreferenceService) {
	
		_sdActivityInstanceService = sdActivityInstanceService;
		_sdCommonViewUtilService = sdCommonViewUtilService;
		_q = $q;
		trace = sdLoggerService.getLogger('bcc-ui.sdPostponedActivitiesCtrl');
		_filter = $filter; 
		_sgI18nService =sgI18nService;
		_sdPreferenceService = sdPreferenceService;
		
		this.postponedActivities = {
				totalCount : 0,
		 		list : []
		}
		
		this.activities = {
				totalCount : 0,
				list : [],
				isTableVisible : false
		}
	
		this.columnSelector = sdLoggedInUserService.getUserInfo().isAdministrator ?  'admin' : true;
		this.exportFileName = "Postponed Activities";
		this.columns = [];
		this.ready = false;
		this.dataTable = null;
		this.activityTable = null;
		
		
		this.selectedOids = [];
		// Getting columns for the data table
		this.getColumns( );
		
	};
	
	/**
	 * 
	 */
	PostponedActivitiesCtrl.prototype.getColumns = function( ) {
		var self = this;
		_sdActivityInstanceService.getParticipantColumns().then(function(result){
			self.columns = result;
			trace.debug("Columns retreived : ",self.columns);
			self.ready = true;
		});
	};
	/**
	 * 
	 */
	PostponedActivitiesCtrl.prototype.getPostponedActivities = function( options ) {
		
		var self = this;
	    var deferred = _q.defer();
		_sdActivityInstanceService.getPostponedActivities( ).then(function( result ){
			trace.log('Postponed activities retreived successfully.');
			
			if(options.filters && options.filters.TeamMember && options.filters.TeamMember.textSearch !=''){
				trace.log("Applying filter with team member : ",options.filters.TeamMember.textSearch );
				result = _filter('filter')(result, {'teamMember' : {'displayName': options.filters.TeamMember.textSearch }},false)
			}
			
			self.postponedActivities.list = result;
			self.postponedActivities.totalCount = result.length;
			deferred.resolve(self.postponedActivities);
			console.log(self.completedActivities)
		}).then(function(failure){
			trace.log('Failed to retrive postponed activities.');
			deferred.reject(self.postponedActivities);
		});
		return deferred.promise;
		
	};
	
	/**
	 * 
	 */
	PostponedActivitiesCtrl.prototype.openUserManagerDetails = function(user) {
		trace.log('Opening user management details view.');
		_sdCommonViewUtilService.openUserManagerDetailView( user.oid, user.id, true);
	};
	
	/**
	 * 
	 */
	PostponedActivitiesCtrl.prototype.getExportValue = function(data) {
		return (	_sgI18nService.translate('business-control-center-messages.views-common-column-totalCount')+": "+data.totalCount+
					" "+_sgI18nService.translate('business-control-center-messages.views-postponedActivities-column-duration')+": "+data.avgDuration +
					" "+_sgI18nService.translate('business-control-center-messages.views-postponedActivities-column-durationExceed')+": "+data.exceededDurationCount);
	};
	
	/**
	 * 
	 */
	PostponedActivitiesCtrl.prototype.refresh = function( ) {
		this.dataTable.refresh();
	};
	
	/**
	 * 
	 */
	PostponedActivitiesCtrl.prototype.preferenceDelegate = function(prefInfo) {
		var preferenceStore = _sdPreferenceService.getStore('USER',
				'ipp-business-control-center', 'preference');
		// Override
		preferenceStore.marshalName = function(scope) {
			return 'ipp-business-control-center.postponedActivities.selectedColumns';
		}

		return preferenceStore;
	};
	
	/**
	 * 
	 */
	PostponedActivitiesCtrl.prototype.showAllActivities = function( rowData ) {
		if(!rowData.allActivityOIDs || rowData.allActivityOIDs.length < 1){
			return;
		}
		var self = this;
		self.showActivityTable(rowData.allActivityOIDs);
	};
	
	/**
	 * 
	 */
	PostponedActivitiesCtrl.prototype.showExceededActivities = function( rowData ) {
		
		if(!rowData.exceededActivityOIDs || rowData.exceededActivityOIDs.length < 1){
			return;
		}
		var self = this;
		self.showActivityTable(rowData.exceededActivityOIDs);
	};
	
	/**
	 * 
	 */
	PostponedActivitiesCtrl.prototype.showActivityTable = function( oids ) {
		var self = this;
		self.selectedOids = oids;
		self.activities.isTableVisible = true;
		if(self.activityTable){
			self.activityTable.refresh();
		} 
	};
	
	/**
	 * 
	 */
	PostponedActivitiesCtrl.prototype.getActivitiesByOids = function(params) {
		var self = this;
		var deferred = _q.defer();
		
		_sdActivityInstanceService.getByOids(params, self.selectedOids).then(function(data) {
			self.activities.list = data.list;
			self.activities.totalCount = data.totalCount;
			deferred.resolve(self.activities);
		}, function(error) {
			deferred.reject(error);
		});

		return deferred.promise;
	};
	
})();