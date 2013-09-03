/*
 * 
 */
define(['portal-shell/js/shell'], function (shell) {
	"use strict";

	shell.module.provider('sgConfigService', function() {
		var self = this;
		var localCfg;

		/*
		 * 
		 */
		self.config = function(cfg) {
			if( cfg !== undefined ) {
				localCfg = cfg;
			}
			return localCfg;
		};

		self.$get = ['$resource', '$q', '$log', function($resource, $q, $log) {
			var deferred = $q.defer();
			if( localCfg ) {
				deferred.resolve(localCfg);
			} else {
				$resource(stardust.initParams().configEndpoint).get({},
					function(conf) {
						$log.log('- Shell config loaded');
						localCfg = conf;
						deferred.resolve(conf);
					},
					function() {
						deferred.reject('Framework config required!');
						$log.error('Framework config required!');
					}
				);
			}
			return deferred.promise;
		}];
	});
});