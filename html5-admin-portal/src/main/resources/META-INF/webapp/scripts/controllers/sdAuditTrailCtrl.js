/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

/**
 * @author Abhay.Thappan
 */

(function() {
	'use strict';

	angular.module("admin-ui").controller(
			'sdAuditTrailCtrl',
			[ '$scope', 'sdAuditTrailService', 'sdLoggerService', 'sdViewUtilService', 'sdDialogService', 'sgI18nService',
					AuditTrailCtrl ]);

	var trace;
	var _sdViewUtilService;
	var _sdAuditTrailService;
	var _sdDialogService;
	var _sgI18nService;
	var _scope;

	/**
	 * 
	 */
	function AuditTrailCtrl($scope, sdAuditTrailService, sdLoggerService, sdViewUtilService, sdDialogService, sgI18nService) {
		trace = sdLoggerService.getLogger('admin-ui.sdAuditTrailCtrl');
		_sdViewUtilService = sdViewUtilService;
		_sdAuditTrailService = sdAuditTrailService;
		_sdDialogService = sdDialogService;
		_sgI18nService = sgI18nService;
		_scope = $scope;
	}

	/**
	 * 
	 */
	AuditTrailCtrl.prototype.openRecoveryDialog = function() {
		var self = this;
		var options = {
			title : _sgI18nService
					.translate('admin-portal-messages.launchPanels-ippAdmAdministrativeActions-recovery-title'),
			dialogActionType : 'YES_NO'
		};
		var defer = _sdDialogService
				.confirm(
						_scope,
						_sgI18nService
								.translate('admin-portal-messages.launchPanels-ippAdmAdministrativeActions-auditTrail-recoverWorkflowEngineText'),
						options);

		defer.then(function() {
			 _sdAuditTrailService.recoverWorkflowEngine().then(
						function(result) {
							_sdDialogService.info(_scope, 
									_sgI18nService.translate('admin-portal-messages.launchPanels-ippAdmAdministrativeActions-auditTrail-recoveringCompleted'),{});
					    }, function(error) {
							self.errorMsg = error.data.message;
							_sdDialogService.error(_scope, self.errorMsg,{});
					 });
				});

	}
	/**
	 * 
	 */
	AuditTrailCtrl.prototype.openCleanupATDConfirm = function(){
		var self = this;
		self.retainUsersAndDepts = true;
		self.retainBOInstances = true;
		var openViewCount = _sdViewUtilService.getOpenViewCount();
		if(openViewCount > 0){
			_sdDialogService.error(_scope, _sgI18nService.translate('admin-portal-messages.launchPanels-ippAdmAdministrativeActions-auditTrail-viewsOpen-errorMessage'),{});
		}else{
			self.showCleanupATDConfirmation = true;
		}
	}
	/**
	 * 
	 * @param res
	 */
	AuditTrailCtrl.prototype.cleanupATD = function(res){
		var self = this;
		var params = {
				retainUsersAndDepts	: self.retainUsersAndDepts,
				retainBOInstances:	self.retainBOInstances
		};
		_sdAuditTrailService.cleanupAuditTrailDatabase(params).then(
				function(result) {
					if(result.status){
						_sdViewUtilService.logout();
					}
					
			    }, function(error) {
					self.errorMsg = error.data.message;
					_sdDialogService.error(_scope, self.errorMsg,{});
			 });
	}
	
	/**
	 * 
	 */
	AuditTrailCtrl.prototype.openCleanupATMDConfirm = function(){
		var self = this;
		var openViewCount = _sdViewUtilService.getOpenViewCount();
		if(openViewCount > 0){
			_sdDialogService.error(_scope, _sgI18nService.translate('admin-portal-messages.launchPanels-ippAdmAdministrativeActions-auditTrail-viewsOpen-errorMessage'),{});
		}else{
			self.showCleanupATMDConfirmation = true;
		}
	}
	
	/**
	 * 
	 * @param res
	 */
	AuditTrailCtrl.prototype.cleanupATMD = function(res){
		var self = this;
		_sdAuditTrailService.cleanupAuditTrailDatabaseWithModel().then(
				function(result) {
					if(result.status){
						_sdViewUtilService.logout();
					}
					
			    }, function(error) {
					self.errorMsg = error.data.message;
					_sdDialogService.error(_scope, self.errorMsg,{});
			 });
	}
	
	
})();
