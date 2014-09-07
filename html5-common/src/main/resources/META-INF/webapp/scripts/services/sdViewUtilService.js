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
 * @author Subodh.Godbole
 */

'use strict';

angular.module('bpm-common.services').provider('sdViewUtilService', function () {
	var self = this;
	
	self.$get = ['$rootScope', function ($rootScope) {

		var service = {};

		/*
		 * 
		 */
		service.getViewParams = function(scope) {
			return scope.panel.params.custom;
		};

		/*
		 * 
		 */
		service.getViewParam = function(scope, param) {
			return scope.panel.params.custom[param];
		};

		/*
		 *
		 */
		service.openView = function(viewId, viewKey, params, nested) {
			var message = {
				"type": "OpenView",
				"data": {
					"viewId": viewId,
					"viewKey": viewKey,
					"params": params,
					"nested" : (nested != undefined && nested === true) ? true : false
				}
			};

			window.postMessage(JSON.stringify(message), "*");
		};

		/*
		 *
		 */
		service.changePerspective = function(perspectiveId, params) {
			var message = {
				"type": "ChangePerspective",
				"data": {
					"perspectiveId": perspectiveId,
					"params": params
				}
			};

			window.postMessage(JSON.stringify(message), "*");
		};

		return service;
	}];
});
