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
        	var ret = [];

            var allListeners = listeners[topic] || {};
    		for(var id in allListeners) {
    			ret.push(allListeners[id](payload));
        	}
    		
    		for(var i in ret) {
    			if (ret[i] !== 'object' && ret[i] == false) {
    				return false;
    			}
    		}

    		return true;
        };
    }]);
});