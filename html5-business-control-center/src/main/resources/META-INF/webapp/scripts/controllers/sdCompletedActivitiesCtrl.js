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

	angular.module("bcc-ui").controller('sdCompletedActivitiesCtrl',
			['sdActivityInstanceService', 'sdCommonViewUtilService', '$q', 'sdProcessInstanceService', 
			 'sdLoggerService', '$filter', 'sgI18nService', 'sdLoggedInUserService','sdPreferenceService', 'sdDataTableHelperService',CompletedActivitiesCtrl ]);


	var _sdActivityInstanceService = null;
	var _sdCommonViewUtilService = null;
	var _q = null;
	var _sdProcessInstanceService = null;
	var trace = null;
	var _filter = null;
	var _sgI18nService = null;
	var _sdPreferenceService = null;
	var _sdDataTableHelperService = null;
	
	/**
	 * 
	 */
	function CompletedActivitiesCtrl( sdActivityInstanceService, sdCommonViewUtilService, $q, sdProcessInstanceService, 
									  sdLoggerService, $filter, sgI18nService, sdLoggedInUserService, sdPreferenceService, sdDataTableHelperService) {
	
		_sdActivityInstanceService = sdActivityInstanceService;
		_sdCommonViewUtilService = sdCommonViewUtilService;
		_q = $q;
		_sdProcessInstanceService = sdProcessInstanceService;
		trace = sdLoggerService.getLogger('bcc-ui.sdCompletedActivitiesCtrl');
		_filter = $filter; 
		_sgI18nService =sgI18nService;
		_sdPreferenceService = sdPreferenceService;
		_sdDataTableHelperService   = sdDataTableHelperService
		
		this.completedActivities = {
				totalCount : 0,
		 		list : []
		}
	
		this.columnSelector = sdLoggedInUserService.getUserInfo().isAdministrator ?  'admin' : true;
		this.exportFileName = "Completed Activities";
		this.columns = [];
		this.ready = false;
		this.dataTable = null;
		
		// Getting columns for the data table
		this.getColumns( );
		this.getStatsForCompletedActivities();
	};
	
	/**
	 * 
	 */
	CompletedActivitiesCtrl.prototype.getColumns = function( ) {
		var self = this;
		_sdProcessInstanceService.getProcessColumns().then(function(result){
			self.columns = result;
			self.ready = true;
			trace.log('Columns retrieved :' + self.columns);
		});
	};
	
	
	/**
	 * 
	 */
	CompletedActivitiesCtrl.prototype.getStatsForCompletedActivities = function( ) {
		var self = this;
		_sdActivityInstanceService.getStatsForCompletedActivities( ).then(function( result ){
			trace.log('Completed activities retreived successfully.');
			self.completedActivities.list = result;
			self.completedActivities.totalCount = result.length;
			if(self.dataTable)
				self.dataTable.refresh();
		})
	};
	
	
	/**
	 * 
	 */
	CompletedActivitiesCtrl.prototype.fetchData = function( options ) {
		var self = this;
		var deferred = _q.defer();
		var result = {
				list : self.completedActivities.list,
				totalCount : self.completedActivities.totalCount
		}
			
		deferred.resolve(result);
		return deferred.promise;
	};
	
	/**
	 * 
	 */
	CompletedActivitiesCtrl.prototype.openUserManagerDetails = function(user) {
		trace.log('Opening user management details view.');
		_sdCommonViewUtilService.openUserManagerDetailView( user.oid, user.id, true);
	};
	
	/**
	 * 
	 */
	CompletedActivitiesCtrl.prototype.getExportValue = function(data) {
		return (	_sgI18nService.translate('business-control-center-messages.views-common-column-today')+": "+data.day+
					" "+_sgI18nService.translate('business-control-center-messages.views-common-column-week')+": "+data.week+
					" "+_sgI18nService.translate('business-control-center-messages.views-common-column-month')+": "+data.month );
	};
	
	/**
	 * 
	 */
	CompletedActivitiesCtrl.prototype.refresh = function( ) {
		trace.log('Refreshing view.');
		this.getStatsForCompletedActivities();
	};
	
	/**
	 * 
	 */
	CompletedActivitiesCtrl.prototype.preferenceDelegate = function(prefInfo) {
		var preferenceStore = _sdPreferenceService.getStore( prefInfo.scope, 'ipp-business-control-center',
				'preference');
		// Override
		preferenceStore.marshalName = function(scope) {
			return 'ipp-business-control-center.CompletedActivity.selectedColumns';
		}

		return preferenceStore;
	};

})();