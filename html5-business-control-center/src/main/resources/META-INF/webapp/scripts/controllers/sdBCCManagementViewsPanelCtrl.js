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

	angular.module("bcc-ui").controller('sdBCCManagementViewsPanelCtrl',
			['sdLoggerService', 'sdViewUtilService', BCCManagementViewsPanelCtrl ]);

	var trace;
	var _sdViewUtilService;

	/**
	 * 
	 */
	function BCCManagementViewsPanelCtrl(sdLoggerService, sdViewUtilService) {
		trace = sdLoggerService.getLogger('bcc-ui.sdBCCManagementViewsPanelCtrl');
		_sdViewUtilService = sdViewUtilService;
		this.showPanelContent = true;
	}
	
	BCCManagementViewsPanelCtrl.prototype.openView= function(viewId,params) {		
		_sdViewUtilService.openView(viewId, null, params, false);
	}
	
	BCCManagementViewsPanelCtrl.prototype.openAllProcessManagement = function(){
		_sdViewUtilService.openView('processOverviewView', null, null, false);
		_sdViewUtilService.openView('processSearchView', null, null, false);
		_sdViewUtilService.openView('trafficLightView', null, null, false);
	}
	
	BCCManagementViewsPanelCtrl.prototype.openAllActivityManagement = function(){
		_sdViewUtilService.openView('activityCriticalityManagerView', null, null, false);
		_sdViewUtilService.openView('pendingActivities', null, null, false);
		_sdViewUtilService.openView('completedActivities', null, null, false);
		_sdViewUtilService.openView('postponedActivities', null, null, false);
		_sdViewUtilService.openView('strandedActivities', null, null, false);
	}
	
	BCCManagementViewsPanelCtrl.prototype.openAllResourceManagement = function(){
		_sdViewUtilService.openView('resourceAvailabilityView', null, null, false);
		_sdViewUtilService.openView('roleAssignmentView', null, null, false);
		_sdViewUtilService.openView('deputyTeamMemberView', null, null, false);
		_sdViewUtilService.openView('resourceLoginView', null, null, false);
		_sdViewUtilService.openView('resourcePerformance', null, null, false);
		_sdViewUtilService.openView('performanceTeamleader', null, null, false);
	}
	
})();