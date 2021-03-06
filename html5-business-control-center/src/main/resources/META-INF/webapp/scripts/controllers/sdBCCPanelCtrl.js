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

	angular.module("bcc-ui")
			.controller(
					'sdBCCPanelCtrl',
					[ 'sdLoggerService', 'sdViewUtilService', 'sdFavoriteViewService', 'sdFavoriteReportsService', 'sdCommonViewUtilService','eventBus','$scope',
							BCCPanelCtrl ]);

	var trace;
	var _sdViewUtilService;
	var _sdFavoriteViewService;
	var _sdFavoriteReportsService;
	var _sdCommonViewUtilService;
	var _eventBus;
	var _self;
	/**
	 * 
	 */
	function BCCPanelCtrl(sdLoggerService, sdViewUtilService, sdFavoriteViewService, sdFavoriteReportsService, sdCommonViewUtilService, eventBus, $scope) {
		trace = sdLoggerService.getLogger('bcc-ui.sdBCCPanelCtrl');
		_sdViewUtilService = sdViewUtilService;
		_sdFavoriteViewService = sdFavoriteViewService;
		_sdFavoriteReportsService = sdFavoriteReportsService;
		_sdCommonViewUtilService = sdCommonViewUtilService;
		_eventBus	= eventBus;

		//Only add the eventBus listener for our fav reports ctrl alias instance
		if($scope.myFavRptsCtrl){
			_eventBus.onMsg("myReportsView.file.delete",this.removeFavoriteReport,$scope);
		}


	}


	/**
	 * Handle emitted report file deletion events from other views.
	 * Be aware that even though this fx is on a prototpye, this will not
	 * refer to our controller, in fact it will be null when invoked by the
	 * eventBus. FOr this reason self references must be obtained globally from
	 * our _self variable. 
	 * @param  {[type]} e [description]
	 * @param  {[type]} m [description]
	 * @return {[type]}   [description]
	 */
	BCCPanelCtrl.prototype.removeFavoriteReport = function(e, m, originScope){

		var isPresent = originScope.myFavRptsCtrl.favoriteReports.some(function(rpt){
			return rpt.documentId === m.id; 
		});

		if(!isPresent){
			return;
		}

		_sdFavoriteReportsService.removeFromFavoriteReports(m.id)
		.then(function(){
			return originScope.myFavRptsCtrl.refreshMyReportsPanel();
		})
		["catch"](function(err){
			trace.log(err);
		});

	};

    /**
     * 
     * @param viewId
     */
	BCCPanelCtrl.prototype.openView = function(viewId) {
		_sdViewUtilService.openView(viewId, null, null, false);
	}
    
	/**
	 * 
	 */
	BCCPanelCtrl.prototype.openAllProcessManagement = function() {
		_sdViewUtilService.openView('processOverviewView', null, null, false);
		_sdViewUtilService.openView('processSearchView', null, null, false);
		_sdViewUtilService.openView('trafficLightView', null, null, false);
	}
    /**
     * 
     */
	BCCPanelCtrl.prototype.openAllActivityManagement = function() {
		_sdViewUtilService.openView('activityCriticalityManagerView', null, null, false);
		_sdViewUtilService.openView('pendingActivities', null, null, false);
		_sdViewUtilService.openView('completedActivities', null, null, false);
		_sdViewUtilService.openView('postponedActivities', null, null, false);
		_sdViewUtilService.openView('strandedActivities', null, null, false);
	}
    /**
     * 
     */
	BCCPanelCtrl.prototype.openAllResourceManagement = function() {
		_sdViewUtilService.openView('resourceAvailabilityView', null, null, false);
		_sdViewUtilService.openView('roleAssignmentView', null, null, false);
		_sdViewUtilService.openView('deputyTeamMemberView', null, null, false);
		_sdViewUtilService.openView('resourceLoginView', null, null, false);
		_sdViewUtilService.openView('resourcePerformance', null, null, false);
		_sdViewUtilService.openView('performanceTeamleader', null, null, false);
	}

	/**
	 * 
	 */
	BCCPanelCtrl.prototype.getAllFavorites = function() {
		var self = this;
		_sdFavoriteViewService.getAllFavorite().then(function(data) {
			self.favorites = data;
		}, function(error) {
			trace.log(error);
		});
	};
    /**
     * 
     */
	BCCPanelCtrl.prototype.refreshMyFavoritesPanel = function() {
		var self = this;
		self.getAllFavorites();
	}
    /**
     * 
     * @param preferenceId
     * @param preferenceName
     */
	BCCPanelCtrl.prototype.openTLVView = function(preferenceId, preferenceName) {
		_sdViewUtilService.openView(preferenceId, "id=" + preferenceName, {
			"preferenceId" : "" + preferenceId,
			"preferenceName" : "" + preferenceName
		}, false);
	}

	/**
	 * 
	 */
	BCCPanelCtrl.prototype.getAllFavoriteReports = function() {
		var self = this;
		_sdFavoriteReportsService.getAllFavoriteReports().then(function(data) {
			self.favoriteReports = data;
		}, function(error) {
			trace.log(error);
		});
	};
    /**
     * 
     * @param documentId
     */
	BCCPanelCtrl.prototype.removeFromFavoriteReports = function(documentId) {
		var self = this;
		_sdFavoriteReportsService.removeFromFavoriteReports(documentId).then(function(data) {
			self.refreshMyReportsPanel();
		}, function(error) {
			trace.log(error);
		});
	};
    /**
     * 
     */
	BCCPanelCtrl.prototype.refreshMyReportsPanel = function() {
		var self = this;
		self.getAllFavoriteReports();
	}
    /**
     * 
     * @param documentId
     */
	BCCPanelCtrl.prototype.openReportView = function(documentId) {
		_sdCommonViewUtilService.openDocumentView(documentId, false);
	}
})();