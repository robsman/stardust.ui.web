/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

/*
 * @author Subodh.Godbole
 */

(function(){
	'use strict';

	angular.module('bpm-common.services').provider('sdPreferenceService', function () {
		this.$get = ['sdUtilService', function (sdUtilService) {
			var service = new PreferenceService(sdUtilService);
			return service;
		}];
	});

	/*
	 * 
	 */
	function PreferenceService(sdUtilService) {
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
		function PreferenceStorage(scope, module, preferenceId) {
			var url = "services/rest/portal/preference/:scope/:moduleId/:preferenceId";
			url = url.replace(':scope', scope);
			url = url.replace(':moduleId', module);
			url = url.replace(':preferenceId', preferenceId);

			var userScope = scope.toUpperCase() == 'USER';
			var store, parentStore;

			/*
			 * 
			 */
			PreferenceStorage.prototype.getValue = function(name) {
				if (store == undefined) {
					this.fetch();
				}

				var value = store[name];
				if (userScope && value == undefined) {
					 value = parentStore[name];
				}

				return value;
			};

			/*
			 * 
			 */
			PreferenceStorage.prototype.setValue = function(name, value) {
				if (store == undefined) {
					this.fetch();
				}

				if (value != undefined && value != null) {
					if (angular.isObject(value) || angular.isArray(value)) {
						value = angular.toJson(value);
					}
					store[name] = value;
				} else {
					delete store[name];
				}
			};

			/*
			 * 
			 */
			PreferenceStorage.prototype.fetch = function() {
				var prefData = sdUtilService.syncAjax(url);

				store = prefData[scope.toUpperCase()];
				if (!store) {
					store = {};
				}

				if (userScope) {
					parentStore = prefData['PARTITION'];
					if (!parentStore) {
						parentStore = {};
					}
				}
			};

			/*
			 * 
			 */
			PreferenceStorage.prototype.save = function() {
				return sdUtilService.syncAjaxSubmit(url, store);
			};
		}
	};
})();
