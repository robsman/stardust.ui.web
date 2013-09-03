/*
 * 
 */
define(['sps/js/shell'], function (shell) {
	'use strict';
	shell.services.provider('sgNavigationService', function () {
		var self = this;
		
		// Reference to angular's $routeProvider. This is set from Shell.js
		self.$routeProvider = null;

		self.$get = ['$resource', '$route', '$rootScope', '$log', '$q', 'sgConfigService',
		             function ($resource, $route, $rootScope, $log, $q, sgConfigService) {

			var nav = null;
			var path2Item;
			var service = {};

			/*
			 * 
			 */
			function traverse(items, cb, navPath) {
				var path = navPath || [];
				angular.forEach(items, function (itm) {
					var ret = cb.apply(this, [itm, path]);
					if (ret === false) {
						return false;
					}
					if (itm.children) {
						path.push(itm);
						traverse(itm.children, cb, path);
						path.pop();
					}
				}, items);
			}

			/*
			 * 
			 */
			function normalizeLabel(label) {
				return label.toLowerCase().replace(/ /g, '-');
			}

			/*
			 * 
			 */
			function normalizeModel(model) {
				angular.forEach(model, function (navItems) {
					traverse(navItems, function (itm, path) {
						if (!itm.id) {
							itm.id = normalizeLabel(itm.label);
						}
						if (!itm.path) {
							var a = [];
							for (var i = 0; i < path.length; i++) {
								a.push(path[i].id);
							}
							a.push(itm.id);
							itm.path = '/' + a.join('/');
							path2Item[itm.path] = itm;
						}
					});
				});
			}

			/*
			 * 
			 */
			function createRoutes(model) {
				angular.forEach(model, function (navItems, key) {
					traverse(navItems, function (itm) {
						if (itm.partial) {
							var opts = {
								templateUrl: itm.partial + '?' + stardust.cacheQueryParameter()
							};
							if (itm.controller) {
								opts.controller = itm.controller;
							}
							if (itm.args) {
								opts.resolve = {
									args: function () {
										return itm.args;
									}
								};
							}
							self.$routeProvider.when(itm.path, opts);
						} else if (!itm.children && !itm.action) {
							$log.warn('Leaf nav item does not have a partial: ' + itm.label);
						}
					});
				});

			}

			/*
			 * 
			 */
			function processMenu(model, params) {
				normalizeModel(model);
				createRoutes(model);
				return model;
			}

			/*
			 * 
			 */
			function fetchNavigation(endpoint, params) {
				var deferred = $q.defer();
				$resource(endpoint).get(params,
					function(results) {
						nav = processMenu(results, params);
						deferred.resolve(nav);
					},
					function() {
						deferred.reject('Could not load navigation from ' + endpoint);
					}
				);
				return deferred.promise;
			}

			/*
			 * 
			 */
			service.load = function(params) {
				params = params || {};
				path2Item = {};

				return sgConfigService.then(function(config) {
					return fetchNavigation(config.endpoints && config.endpoints.navigation, params);
				}, function () {
					alert("Faild in loading Navigation Endpoint");
				});
			};

			/*
			 * 
			 */
			service.findNavItem = function (navPath) {
				var item;
				if (navPath.indexOf("/") !== 0) {
					item = getItemFromPath2Item(navPath);
				} else {
					item = path2Item[navPath];
				}
				return item;
			};

			/*
			 * 
			 */
			function getItemFromPath2Item(navPath) {
				var navItem = null;
				for(var path in path2Item){
					var item = path2Item[path];
					if(path.indexOf(navPath) > -1) {
						navItem = item;
						break;
					}
				}
				return navItem;
			}

			return service;
		}];
	});
});