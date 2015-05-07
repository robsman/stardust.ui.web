/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/*
 * @author Nikhil.Gahlot
 */

(function(){
	'use strict';

	angular.module('admin-ui.services').provider('sdRealmManagementService', function () {
		this.$get = ['$resource', function ($resource) {
			var service = new RealmManagementService($resource);
			return service;
		}];
	});

	/*
	 *
	 */
	function RealmManagementService($resource) {
		var REST_BASE_URL = "services/rest/portal/realm/";

		/*
		 *
		 */
		RealmManagementService.prototype.getRealms = function() {
			var restUrl = REST_BASE_URL + "fetch";
			var realm = $resource(restUrl);
			return realm.query().$promise;
		};

		/**
		 * 
		 */
		RealmManagementService.prototype.createRealm = function(realm) {
			var restUrl = REST_BASE_URL + "save";
			var saveRes = $resource(restUrl, {}, {
				save : {
					method : 'POST'
				}
			});

			return saveRes.save({}, realm).$promise;
		};
		
		/**
		 * 
		 */
		RealmManagementService.prototype.deleteRealms = function(realmIds) {
			var restUrl = REST_BASE_URL + "delete";
			var realm = $resource(restUrl, {}, {
				remove : {
					method : 'POST'
				}
			});

			return realm.remove({}, realmIds).$promise;
		};
	};
})();
