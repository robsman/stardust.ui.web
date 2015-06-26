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

	angular.module("bcc-ui").controller('sdPerformanceTeamLeaderCtrl',
			['sdActivityInstanceService', 'sdCommonViewUtilService', '$q', 'sdProcessInstanceService',
			 'sdLoggerService', '$filter','sgI18nService', 'sdLoggedInUserService','sdPreferenceService', Controller ]);


	var _sdActivityInstanceService = null;
	var _sdCommonViewUtilService = null;
	var _q = null;
	var _sdProcessInstanceService = null;
	var trace = null;
	var _filter = null;
	var _sgI18nService = null;
	var _sdPreferenceService = null;

	/**
	 * 
	 */
	function Controller( sdActivityInstanceService, sdCommonViewUtilService, $q, sdProcessInstanceService, 
			sdLoggerService, $filter, sgI18nService, sdLoggedInUserService, sdPreferenceService) {

		_sdActivityInstanceService = sdActivityInstanceService;
		_sdCommonViewUtilService = sdCommonViewUtilService;
		_q = $q;
		_sdProcessInstanceService = sdProcessInstanceService;
		trace = sdLoggerService.getLogger('bcc-ui.sdController');
		_filter = $filter; 
		_sgI18nService =sgI18nService;
		_sdPreferenceService = sdPreferenceService;

		this.statistics = {
				totalCount : 0,
				list : []
		}

		this.columnSelector = sdLoggedInUserService.getUserInfo().isAdministrator ?  'admin' : true;
		this.exportFileName = "Performance Team Leader";
		this.columns = [];
		this.ready = false;
		this.dataTable = null;

		// Getting columns for the data table
		this.getColumns( );
		this.fetchStatistics();
	};

	/**
	 * 
	 */
	Controller.prototype.getColumns = function( ) {
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
	Controller.prototype.fetchStatistics = function( ) {
		var self = this;
		_sdActivityInstanceService.getCompletedActivityStatsByTeamLead( ).then(function( result ){
			trace.log('Completed activities retreived successfully.');
			self.statistics.list = result;
			self.statistics.totalCount = result.length;
			if(self.dataTable)
				self.dataTable.refresh();
		})
	};
	/**
	 * 
	 */
	Controller.prototype.getStatistics = function( options ) {
		var self = this;
		var deferred = _q.defer();
		var result = {
				list : [],
				totalCount : self.statistics.totalCount
		}
		if(options.filters && options.filters.TeamMember && options.filters.TeamMember.textSearch !=''){
			trace.log("Applying filter with team member",options.filters.TeamMember.textSearch );
			result.list = _filter('filter')( self.statistics.list, {'teamMember' : {'displayName': options.filters.TeamMember.textSearch }},false)
		}else{
			result.list = self.statistics.list;
		}
		deferred.resolve(result);
		return deferred.promise;
	};
	/**
	 * 
	 */
	Controller.prototype.openUserManagerDetails = function(user) {
		trace.debug('Opening user management details view');
		_sdCommonViewUtilService.openUserManagerDetailView( user.oid, user.id, true);
	};
	/**
	 * 
	 */
	Controller.prototype.getExportValue = function(data) {
		return (	_sgI18nService.translate('business-control-center-messages.views-common-column-today')+": "+data.day+
				" "+_sgI18nService.translate('business-control-center-messages.views-common-column-week')+": "+data.week+
				" "+_sgI18nService.translate('business-control-center-messages.views-common-column-month')+": "+data.month );
	};
	/**
	 * 
	 */
	Controller.prototype.refresh = function( ) {
		this.fetchStatistics();

	};
	/**
	 * 
	 */
	Controller.prototype.preferenceDelegate = function(prefInfo) {
		var preferenceStore = _sdPreferenceService.getStore('USER', 'ipp-business-control-center',
		'preference');
		// Override
		preferenceStore.marshalName = function(scope) {
			return 'ipp-business-control-center.performanceTeamLeader.selectedColumns';
		}
		return preferenceStore;
	};

})();