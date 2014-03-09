/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
var EventHub = {
	events: function() {
	        var events = {};
	        var allEventSubscribers = [];
	        return {
	            subscribe: function(eventName, callback) {
	            	if (eventName == "*") {
	            		allEventSubscribers.push(callback);
	            	} else {
		                events[eventName] = events[eventName] || [];
		                events[eventName].push(callback);
	            	}
	            },
	            unsubscribe: function(eventName, callback) {
	            	if (eventName == "*") {
		            	for (var i = 0; i < allEventSubscribers.length; i++) {
	                    	if(allEventSubscribers[i] === callback) {
	                    		allEventSubscribers.splice(i, 1);
	                    		return; 
	                    	}
	                    }	            		
	            	} else {
		            	var callbacks = events[eventName];
		            	if (callbacks) {
		                    for (i = 0; i < callbacks.length; i++) {
		                    	if(callbacks[i] === callback) {
		                    		callbacks.splice(i, 1);
		                    		return; 
		                    	}
		                    }
		            	}
	            	}
	            },
	            publish: function(eventName) {
                    var args = Array.prototype.slice.call(arguments, 1);
	            	var callbacks = events[eventName];
	                if (callbacks) {
	                    for (var i = 0; i < callbacks.length; i++) {
	                        try {
	                        	callbacks[i].apply(null, args);
	                        } catch(err){}
	                    }
	                }

	                for (var i = 0; i < allEventSubscribers.length; i++) {
                    	try {
                    		allEventSubscribers[i].apply(null, [eventName].concat(args));
                        } catch(err){}
                    }
	            }
	        };
	    }()
};