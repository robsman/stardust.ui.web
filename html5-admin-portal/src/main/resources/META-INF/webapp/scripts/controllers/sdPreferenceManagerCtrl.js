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
 * @author Johnson.Quadras
 */

(function() {
    'use strict';
    angular.module('admin-ui')
	    .controller(
		    'sdPreferenceManagerctrl',
		    [ '$scope', '$q', '$filter', 'sdLoggerService', 'sdPreferenceService', 'sdLoggedInUserService', Controller ]);

    var _sdPreferenceService = null;
    var _q = null;
    var _filter = null;
    var _sdLoggedInUserService = null;
    var trace = null;
    /*
     * 
     */
    function Controller($scope, $q, $filter, sdLoggerService, sdPreferenceService, sdLoggedInUserService) {
	var self = this;
	_sdPreferenceService = sdPreferenceService;
	_q = $q;
	_sdLoggedInUserService = sdLoggedInUserService;
	_filter = $filter;
	trace = sdLoggerService.getLogger('admin-ui.sdPreferenceManagerctrl');
	self.intialize();
    }

    /**
     * 
     */
    Controller.prototype.intialize = function() {
	var self = this;

	self.dataTable = null;
	self.data = {
	    list : [],
	    totalCount : 0
	};
	self.selectedPreference = {
	    value : "partition"
	}
	self.searchText = "";
	var loggedInUser = _sdLoggedInUserService.getUserInfo();
	self.selectedUser = [ {
	    name : loggedInUser.displayName,
	    type : 'USER',
	    realmId : loggedInUser.realmId,
	    id : loggedInUser.id
	} ];
	self.lastSearchedUser = {};
	self.useOldData = false;
	trace.debug("Intilaized with preference : " + self.selectedPreference.value);
    };

    /**
     * 
     */
    Controller.prototype.changeView = function() {
	var self = this;
	self.useOldData = false;
	self.dataTable.refresh();
    };

    /**
     * 
     */
    Controller.prototype.filterTable = function() {
	var self = this;
	// Check if the user is the same as the previous search
	if (self.selectedPreference.value == 'user' && _filter('filter')(self.selectedUser, {
	    name : self.lastSearchedUser.name
	}, true) < 1) {
	    self.useOldData = false;
	}
	self.dataTable.refresh();
    };

    /**
     * 
     */
    Controller.prototype.filterData = function(data) {
	var self = this;

	var filterData = {
	    list : [],
	    totalCount : self.data.totalCount
	};

	if (self.searchText && self.searchText != '') {
	    trace.debug("Filter data with search text : " + self.searchText);
	    filterData.list = _filter('filter')(data.list, self.searchText, false);
	} else {
	    filterData.list = data.list;
	}
	return filterData;
    };

    /**
     * 
     */
    Controller.prototype.fetchPreferences = function(params) {

	var self = this;
	var deferred = _q.defer();

	if (self.useOldData) {
	    trace.debug("Filtering the previously fetched data.");
	    var filteredData = self.filterData(self.data);
	    deferred.resolve(filteredData);
	} else {
	    trace.debug("Fetching fresh data.");
	    self.getPreferenceData().then(function(result) {
		self.data.totalCount = result.length;
		self.data.list = result;
		var filteredData = self.filterData(self.data);
		deferred.resolve(filteredData);
		self.useOldData = true;
	    }, function(error) {
		deferred.reject(error);
	    });
	}
	return deferred.promise;
    };

    /**
     * 
     */
    Controller.prototype.getPreferenceData = function() {
	var self = this;
	trace.debug("Fetching preferences for view : " + self.selectedPreference.value);
	if (self.selectedPreference.value == 'partition') {
	    self.lastSearchedUser = {};
	    return _sdPreferenceService.getTenantPreferences();
	} else {
	    var deferred = _q.defer();
	    var userData = {}
	    if (self.selectedUser.length > 0) {
		userData = self.selectedUser[0];
		self.lastSearchedUser = userData;
		trace.debug("User : ", userData.name);
	    }
	    _sdPreferenceService.getUserPreferences(userData.realmId, userData.id).then(function(result) {
		// Adding username to the rowData
		angular.forEach(result, function(data) {
		    data.userName = userData.name;
		});
		deferred.resolve(result);
	    });
	    return deferred.promise;
	}
    };

    /**
     * 
     */
    Controller.prototype.getExportDataForScope = function(rowData) {
	var self = this;
	var text = rowData.scope + "( "
	if (self.selectedPreference.value == 'user') {
	    text = text + rowData.userName;
	} else {
	    text = text + rowData.partitionId;
	}
	text = text + " ) "

	return text;
    };

})();