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
 * @author Nikhil.Gahlot
 */

(function() {
	'use strict';

	angular.module('admin-ui').controller(
			'sdRealmManagementCtrl',
			[ 'sdLoggerService', 'sdRealmManagementService', 'sdDialogService', 'sgI18nService',
					'$scope', '$q', 'sdMessageService', 'sdLoggedInUserService', RealmManagementController ]);

	/*
	 * 
	 */
	function RealmManagementController(sdLoggerService, sdRealmManagementService, sdDialogService, sgI18nService,
			$scope, $q, sdMessageService, sdLoggedInUserService) {

		var trace = sdLoggerService.getLogger('admin-ui.sdRealmManagementCtrl');

		/*
		 * 
		 */
		RealmManagementController.prototype.initialize = function() {
			this.resetValues();
			this.dataTable = null; // This will be set to underline data
			
			this.columnSelector = sdLoggedInUserService.getUserInfo().isAdministrator ? 'admin' : true;
			this.exportFileNameForRealmManagement = "RealmManagement"

			this.realms = {
				list : [],
				totalCount : 0
			};
			this.fetchRealms();
		};

		/*
		 * 
		 */
		RealmManagementController.prototype.resetValues = function() {
			this.showCreateDlg = false;
			this.errorMessages = [];
			this.realmToCreate = {};
		};

		/**
		 * 
		 */
		RealmManagementController.prototype.fetchRealms = function() {
			var self = this;

			sdRealmManagementService.getRealms().then(function(result) {
				self.realms.list = result;
				self.realms.totalCount = result.length;

				self.refresh();
			}, function(error) {
				trace.error('Error occured while fetching Realms : ', error);
			});
		}

		/*
		 * 
		 */
		RealmManagementController.prototype.refresh = function() {
			if (angular.isDefined(this.dataTable) && this.dataTable != null) {
				this.dataTable.refresh(true);
			}
		};

		/*
		 * 
		 */
		RealmManagementController.prototype.createRealm = function() {
			var deferred = $q.defer();
			var self = this;
            self.submitted = true;
			var payload = {};
			payload = angular.extend({}, self.realmToCreate);

			if (self.validate(payload)) {
				sdRealmManagementService.createRealm(payload).then(
						function(result) {
							self.resetValues();
							self.fetchRealms();

							deferred.resolve();
						},
						function(error) {
							self.errorMsg = error.data;
							self.showErrorMsg = true;
							trace.error('Error occured while saving Realm : ', error);
							// show error to the user
							sdMessageService.showMessage(sgI18nService
									.translate('admin-portal-messages.views-realmMgmt-cannotCreateRealm'));
						});
			}

			return deferred.promise;
		};

		/*
		 * 
		 */
		RealmManagementController.prototype.validate = function(realm) {
			if (angular.isDefined(realm.id) && angular.isDefined(realm.name)) {
				return true;
			}

			return false;
		};

		/*
		 * 
		 */
		RealmManagementController.prototype.removeRealms = function() {
			var self = this;
			var options = {
				title : sgI18nService.translate('admin-portal-messages.common-confirmation'),
				dialogActionType : 'YES_NO'
			};
			var defer = sdDialogService.confirm($scope, sgI18nService
					.translate('admin-portal-messages.views-realmMgmt-confirmDelete-title'), options);

			var realmOids = [];
			angular.forEach(self.dataTable.getSelection(), function(item) {
				realmOids.push(item.id);
			});

			defer.then(function() {
				sdRealmManagementService.deleteRealms({
					ids : realmOids
				})
						.then(
								function(result) {
									self.resetValues();
									self.fetchRealms();
								},
								function(error) {
									self.errorMsg = error.data;
									self.showErrorMsg = true;
									trace.error('Error occured while deleting Realms : ', error);
									// show error to the user
									sdMessageService.showMessage(sgI18nService.translate(
											'admin-portal-messages.views-realmMgmt-cannotDeleteRealm',
											'Cannot delete realm.'));
								});
			});
		};

		/*
		 * 
		 */
		RealmManagementController.prototype.openCreateRealmDlg = function() {
			this.submitted = false;
			this.showCreateDlg = true;
		};

		this.initialize();
	}
})();