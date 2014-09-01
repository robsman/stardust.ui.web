/*
 * 
 */
'use strict';

angular.module('shell.services').service('sgPubSubService', ['$q', function ($q) {
    var listeners = {};

    /*
     * 
     */
    function getCount(topicListeners) {
    	var count = 0;
    	for(var id in topicListeners) {
			count++;
    	}
    	return count;
    }
    
    /*
     * 
     */        
    this.subscribe = function(topic, callback) {
    	if (!topic || !callback) {
    		return;
    	}

    	listeners[topic] = listeners[topic] || {};
    	var id = getCount(listeners[topic]);
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