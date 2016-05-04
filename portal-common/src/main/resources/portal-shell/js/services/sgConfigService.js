/*
 * 
 */
"use strict";

angular.module('shell.services').provider('sgConfigService', function() {
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

	self.$get = ['$injector', '$resource', '$q', '$log', function($injector, $resource, $q, $log) {
		var deferred = $q.defer();
		if( localCfg ) {
			deferred.resolve(localCfg);
		} else {
			if ($injector.has('sgConfig')) {
				localCfg = $injector.get('sgConfig');
				deferred.resolve(localCfg);
			} else {
				$log.error('sgConfig required, but not defined.');
				deferred.reject('Framework config required!');
			}
		}
		return deferred.promise;
	}];
});