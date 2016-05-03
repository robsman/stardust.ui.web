/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/*
 * @author Subodh.Godbole
 */

(function() {
	'use strict';

	angular.module('bpm-common.services').provider('sdPreferenceService', function() {
		this.$get = ['sdUtilService', 'sdLoggerService', '$resource', function(sdUtilService, sdLoggerService, $resource) {
			var service = new PreferenceService(sdUtilService, sdLoggerService, $resource);
			return service;
		}];
	});

	/*
	 *
	 */
	function PreferenceService(sdUtilService, sdLoggerService, $resource) {
		var trace = sdLoggerService.getLogger('bpm-common.sdPreferenceService');

		/*
		 *
		 */
		PreferenceService.prototype.getStore = function(prefScope, module, preferenceId) {
			var prefStorage = new PreferenceStorage(prefScope, module, preferenceId);
			return prefStorage;
		};

		/*
		 *
		 */
		PreferenceService.prototype.getTenantPreferences = function() {
			var restUrl = sdUtilService.getBaseUrl() + "services/rest/portal/preference/partition";
			return $resource(restUrl).query().$promise;
		};

		/*
		 *
		 */
		PreferenceService.prototype.getUserPreferences = function(realmId, userId) {
			var restUrl = sdUtilService.getBaseUrl() + "services/rest/portal/preference/user";
			if (realmId && userId) {
				restUrl = restUrl + "?realmId=" + realmId + "&userId=" + userId;
			}
			return $resource(restUrl).query().$promise;
		};

		/*
		 *
		 */
		function PreferenceStorage(scope, module, preferenceId) {
			this.scope = scope;
			this.module = module;
			this.preferenceId = preferenceId;

			this.url = sdUtilService.getBaseUrl() + "services/rest/portal/preference/:scope/:moduleId/:preferenceId";
			this.url = this.url.replace(':scope', this.scope);
			this.url = this.url.replace(':moduleId', this.module);
			this.url = this.url.replace(':preferenceId', this.preferenceId);

			this.userScope = this.scope.toUpperCase() == 'USER';
			this.store = null;
			this.parentStore = null;



			/*
			 *
			 */
			PreferenceStorage.prototype.getValue = function(name, fromParent) {
				if (this.store == undefined) {
					throw "Store not intialized yet";
				}

				var value;
				if (fromParent) {
					value = this.parentStore[this.marshalName('PARTITION', name)];
					trace.log('Returning Partition Scope value for: ' + name, value);
				} else {
					value = this.store[this.marshalName(this.scope, name)];
					if (this.userScope && value == undefined) {
						value = this.parentStore[this.marshalName('PARTITION', name)];
						trace.log('Falling back to Partition Scope for: ' + name, value);
					}
				}
				return value;
			};


			/*
			 *
			 */

			PreferenceStorage.prototype.init = function() {
				var self = this;

				return $resource(self.url).get().$promise.then(function(prefData) {
					self.store = prefData[self.scope.toUpperCase()];
					if (!self.store) {
						self.store = {};
					}

					if (self.userScope) {
						self.parentStore = prefData['PARTITION'];
						if (!self.parentStore) {
							self.parentStore = {};
						}
					}
				});

			};


			/*
			 *
			 */
			PreferenceStorage.prototype.setValue = function(name, value) {
				if (this.store == undefined) {
					throw "Store not intialized yet";
				}

				if (value != undefined && value != null) {
					if (angular.isObject(value) || angular.isArray(value)) {
						value = angular.toJson(value);
					}
					this.store[this.marshalName(this.scope, name)] = value;
				} else {
					delete this.store[this.marshalName(this.scope, name)];
				}
			};

			
			/*
			 * 
			 */
			PreferenceStorage.prototype.save = function () {
				var self = this;
				if (this.store != undefined) {

					var preferences = $resource(self.url, null, {
						save : {
							method : 'POST'
						}
					});

					return preferences.save(null, self.store).$promise;
				} else {
					trace.error('Cannot save preferences, as its not yet fetched.');
				}
			};


			/*
			 *
			 */
			PreferenceStorage.prototype.marshalName = function(scope, name) {
				return name;
			};
		}
	};
})();
