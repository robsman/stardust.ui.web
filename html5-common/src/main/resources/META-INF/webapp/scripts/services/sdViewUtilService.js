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
						'sdEnvConfigService',
						'sdLoggerService',
						function($rootScope, sgViewPanelService, sgPubSubService, sdEnvConfigService, sdLoggerService) {
							var service = new ViewUtilService($rootScope, sgViewPanelService, sgPubSubService, sdEnvConfigService,
									sdLoggerService);
							return service;
						} ];
			});

	/*
	 * 
	 */
	function ViewUtilService($rootScope, sgViewPanelService, sgPubSubService, sdEnvConfigService, sdLoggerService) {
		var trace = sdLoggerService.getLogger('bpm-common.sdViewUtilService');

		var viewHandlers = {};

		var self = this;
		sgPubSubService.subscribe('sgActiveViewPanelChanged', function() {
			var args = Array.prototype.slice.call(arguments, 0);
			self.viewChanged.apply(self, args);
		});
		
		var ippWindow = window.BridgeUtils ? window : window.parent;

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
			var eventParams = null;

			if (params && params.eventParams) {
				eventParams = params.eventParams;
				delete params.eventParams;
			}

			var eventInterceptor = sdEnvConfigService.getEventInterceptor();
			if (eventInterceptor && eventInterceptor.openView) {
				var config = {
					viewId: viewId,
					viewKey: viewKey,
					params: eventParams,
					nested: nested
				};

				var continueOperation = eventInterceptor.openView(config);
				if (continueOperation) {
					// Check if View is available
					var hml5ViewId = sdEnvConfigService.getNavPath(viewId);
					if (hml5ViewId) {
						var html5ViewParams = jQuery.extend({}, params);
						html5ViewParams.custom = jQuery.extend({}, params);
		
						sgViewPanelService.open(hml5ViewId, true, html5ViewParams);
						return true;
					}
				}

				return false;
			}

			var message = {
				"type" : "OpenView",
				"data" : {
					"viewId" : viewId,
					"viewKey" : viewKey,
					"params" : (params == null)? undefined : params,
					"nested" : (nested != undefined && nested === true) ? true : false
				}
			};

			ippWindow.postMessage(JSON.stringify(message), "*")
			return true;
		};
		
		/**
		 * This method will return the count of all open view.
		 */
		ViewUtilService.prototype.getOpenViewCount = function(){
			return BridgeUtils.View.getOpenViewCount();
		}
		  
		/**
		 * This method will log out.
		 */
		ViewUtilService.prototype.logout = function(){
			return BridgeUtils.logout(true);
		}
		
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

			ippWindow.postMessage(JSON.stringify(message), "*");
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

			ippWindow.postMessage(JSON.stringify(message), "*");
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

			ippWindow.postMessage(JSON.stringify(message), "*");
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
			if(window.BridgeUtils) {
				BridgeUtils.View.syncLaunchPanels();
			}
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
