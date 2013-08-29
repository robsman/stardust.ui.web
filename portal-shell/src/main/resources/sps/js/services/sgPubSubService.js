/*
 * 
 */
define(['sps/js/shell'], function (shell) {
    'use strict';

    shell.services.service('sgPubSubService', ['$q', function ($q) {
        var listeners = {};
        var count = 0;

        /*
         * 
         */        
        this.subscribe = function(topic, callback) {
        	if (!topic || !callback) {
        		return;
        	}

        	var id = count++;
        	listeners[topic] = listeners[topic] || {};
        	listeners[topic][id] = callback;

        	// Unsubscribe function
        	var ret = function() {
        		if (listeners[topic] && listeners[topic][id]) {
        			delete listeners[topic][id];
        		}
        	};
        };

        /*
         * TODO: Use Promises? 
         */
        this.publish = function(topic, payload) {
        	var deferred = $q.defer();
       	 	var retPromises = [];

            var allListeners = listeners[topic] || {};
    		for(id in listeners[topic]) {
    			retPromises.push(listeners[topic][id](payload));
        	}
    		
    		for(i in retPromises) {
    			if (retPromises[i] !== 'object' && retPromises[i] == false) {
    				return false;
    			}
    		}
        };
    }]);
});