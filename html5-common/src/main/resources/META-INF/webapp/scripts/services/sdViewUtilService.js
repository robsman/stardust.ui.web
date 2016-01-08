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
(function() {
	'use strict';

	angular.module('bpm-common.services').provider(
			'sdViewUtilService',
			function() {
				this.$get = [
						'$rootScope',
						'sgViewPanelService',
						'sgPubSubService',
						'sdLoggerService',
						function($rootScope, sgViewPanelService, sgPubSubService, sdLoggerService) {
							var service = new ViewUtilService($rootScope, sgViewPanelService, sgPubSubService,
									sdLoggerService);
							return service;
						} ];
			});

	/*
	 * 
	 */
	function ViewUtilService($rootScope, sgViewPanelService, sgPubSubService, sdLoggerService) {
		var trace = sdLoggerService.getLogger('bpm-common.sdViewUtilService');

		var viewHandlers = {};

		var self = this;
		sgPubSubService.subscribe('sgActiveViewPanelChanged', function() {
			var args = Array.prototype.slice.call(arguments, 0);
			self.viewChanged.apply(self, args);
		});

		/*
		 * 
		 */
		ViewUtilService.prototype.viewChanged = function(data) {
			var currentViewPath = data.currentNavItem.path;
			var beforeViewPath = data.before ? data.before.path : null;

			if (currentViewPath !== beforeViewPath) {
				if (beforeViewPath && viewHandlers[beforeViewPath]) {
					trace.log('Calling DEACTIVATE event on view ' , beforeViewPath);
					callHandlerFunction(viewHandlers[beforeViewPath], "DEACTIVATED");
				}

				if (viewHandlers[currentViewPath]) {
					trace.log('Calling ACTIVATE event on view ' , currentViewPath);
					callHandlerFunction(viewHandlers[currentViewPath], "ACTIVATED");
				}
			}
		}

		/*
		 * 
		 */
		ViewUtilService.prototype.getView = function(scope) {
			return scope.panel;
		};

		/*
		 * 
		 */
		ViewUtilService.prototype.getViewParams = function(scope) {
			return scope.panel.params.custom;
		};

		/*
		 * 
		 */
		ViewUtilService.prototype.getViewParam = function(scope, param) {
			return scope.panel.params.custom[param];
		};

		/*
		 * 
		 */
		ViewUtilService.prototype.openView = function(viewId, viewKey, params, nested) {
			var message = {
				"type" : "OpenView",
				"data" : {
					"viewId" : viewId,
					"viewKey" : viewKey,
					"params" : (params == null)? undefined : params,
					"nested" : (nested != undefined && nested === true) ? true : false
				}
			};

			window.postMessage(JSON.stringify(message), "*");
		};
		
		
		/*
		 * 
		 */
		ViewUtilService.prototype.updateViewInfo = function(viewId, viewKey, params) {
			var message = {
				"type" : "UpdateViewInfo",
				"data" : {
					"viewId" : viewId,
					"viewKey" : viewKey,
					"params" : params
				}
			};

			window.postMessage(JSON.stringify(message), "*");
		};


		/*
		 * 
		 */
		ViewUtilService.prototype.closeView = function(viewId, viewKey) {
			var message = {
				"type" : "CloseView",
				"data" : {
					"viewId" : viewId,
					"viewKey" : viewKey
				}
			};

			window.postMessage(JSON.stringify(message), "*");
		};
		/*
		 * 
		 */
		ViewUtilService.prototype.changePerspective = function(perspectiveId, params) {
			var message = {
				"type" : "ChangePerspective",
				"data" : {
					"perspectiveId" : perspectiveId,
					"params" : params
				}
			};

			window.postMessage(JSON.stringify(message), "*");
		};

		/*
		 * 
		 */
		ViewUtilService.prototype.registerForViewEvents = function(scope, handlerFunc, ownerObject) {
			if (angular.isFunction(handlerFunc)) {
				var path = scope.panel.path;

				viewHandlers[path] = {};
				viewHandlers[path].func = handlerFunc;
				viewHandlers[path].owner = ownerObject;

				var self = this;
				scope.$on("$destroy", function() {
					if (viewHandlers[path]) {
						delete viewHandlers[path];
					}
				});
			} else {
				throw "Handler should be a function.";
			}
		};

		ViewUtilService.prototype.syncLaunchPanels = function() {
			BridgeUtils.View.syncLaunchPanels();
		};

		/*
		 * 
		 */
		function callHandlerFunction(handler, type) {
			try {
				if (handler.func) {
					if (handler.owner) {
						handler.func.call(handler.owner, {
							type : type
						});
					} else {
						handler.func({
							type : type
						});
					}
				}
			} catch (e) {
				if (console) {
					trace.error(e);
				}
			}
		}
	}
	;
})();
