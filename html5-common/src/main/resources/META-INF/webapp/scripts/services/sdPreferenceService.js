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
		this.$get = ['$rootScope', function ($rootScope) {
			var service = new PreferenceService($rootScope);
			return service;
		}];
	});

	/*
	 * 
	 */
	function PreferenceService($rootScope) {
		/*
		 * 
		 */
		PreferenceService.prototype.getPreference = function(prefScope, module) {
			var prefStorage = new PreferenceStorage(prefScope, module);
			return prefStorage;
		};
	};

	// Implement in memory Store for now!
	// TODO: Connect to REST
	var inMemStore = {};

	/*
	 * 
	 */
	function PreferenceStorage(scope, module) {
		if (!inMemStore[scope]) {
			inMemStore[scope] = {};
		}

		/*
		 * 
		 */
		PreferenceStorage.prototype.getList = function(name) {
			return inMemStore[scope].name;
		};

		/*
		 * 
		 */
		PreferenceStorage.prototype.setList = function(name, list) {
			inMemStore[scope].name = list;
		};
	}
})();
