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
 * @author Subodh.Godbole
 */

(function() {
	'use strict';

	angular.module('workflow-ui').controller('sdWorklistViewCtrl',
			['$scope', '$parse', 'sdUtilService', 'sdViewUtilService', '$interval',
			'sdWorklistViewConfigService', 'sdLoggerService', 'sdI18nService', WorklistViewCtrl]);

	var  _$interval = null ;
	var _sdWorklistViewConfigService = null;
	var _parse = null;
	var trace = null;
	/*
	 *
	 */
	function WorklistViewCtrl($scope, $parse, sdUtilService,
											sdViewUtilService, $interval, sdWorklistViewConfigService,
											sdLoggerService, sdI18nService) {
		sdViewUtilService.registerForViewEvents($scope, this.handleViewEvents, this);

		var i18n;

		_$interval = $interval;
		_sdWorklistViewConfigService = sdWorklistViewConfigService;
		_parse = $parse;
		trace  = sdLoggerService.getLogger('workflow-ui.sdWorklistViewCtrl');

		this.initialize();

		i18n = sdI18nService.getInstance('views-common-messages');
		this.header = i18n.translate("views.worklistViewPanel.header");

		this.registerForAutoRefresh();

		this.refreshRequired = false;

		this.query = this.generateQuery($scope);

		/*
		 * This needs to be defined here as it requires access to $scope
		 */
		WorklistViewCtrl.prototype.safeApply = function() {
			sdUtilService.safeApply($scope);
		};
	}

	/**
	 *
	 */
	WorklistViewCtrl.prototype.autoRefresh = function() {
		this.refreshRequired = true;
	};

	/*
	 *
	 */
	WorklistViewCtrl.prototype.registerForAutoRefresh = function() {
	    var self = this;
	    
	    _sdWorklistViewConfigService.loadConfig().then(function(){
	    	 var refreshInterval = _sdWorklistViewConfigService.getRefreshIntervalInMillis();
	 	    if (refreshInterval > 0) {
	 	    	this.timer = _$interval(function() {
	 	    		if (self.dataTable) {
	 	    			self.dataTable.refresh(true);
	 	    		}
	 	    	}, refreshInterval);
	 	    }
	    })
	};


	/*
	 *
	 */
	WorklistViewCtrl.prototype.handleViewEvents = function(event) {
		if (event.type == "ACTIVATED") {
			if(this.refreshRequired) {
				this.refresh();
				this.refreshRequired = false;
			}
			this.registerForAutoRefresh();
		} else if (event.type == "DEACTIVATED") {
		    if(this.timer){
			_$interval.cancel(this.timer);
		    }
		}
	};

	/*
	 *
	 */
	WorklistViewCtrl.prototype.initialize = function() {
		this.dataTable = null; // This will be set to underline data table instance automatically
	};

	/*
	 *
	 */
	WorklistViewCtrl.prototype.generateQuery = function($scope) {

		var queryGetter = _parse("panel.params.custom");
		var params = queryGetter($scope);

		var query = {};

		if(params.participantQId) {
				query.participantQId = params.participantQId;
				query.departmentQId = params.departmentQId;
		} else if (params.processQId) {
				query.processQId = params.processQId;
		} else if (params.pInstanceOids) {
				query.pInstanceOids = params.pInstanceOids;
		} else if(params.type) {
				query.type = params.type;
				query.userId =	params.userId;
				query.from =	params.from;
		}

		query.id = params.id;
		return query;
	};

	/*
	 *
	 */
	WorklistViewCtrl.prototype.refresh = function() {
		this.dataTable.refresh(true);
	};
})();
