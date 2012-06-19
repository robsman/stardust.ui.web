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
	        return {
	            subscribe: function(eventName, callback) {
	                events[eventName] = events[eventName] || [];
	                events[eventName].push(callback);
	            },
	            
	            unsubscribe: function(eventName, callback) {
	            	var callbacks = events[eventName];
	            	if (callbacks) {
	                    for (i = 0; i < callbacks.length; i++) {
	                    	if(callbacks[i] === callback) {
	                    		callbacks.splice(i, 1);
	                    		return; 
	                    	}
	                    }
	            	}
	            },
	            publish: function(eventName) {
	            	var callbacks = events[eventName];
	                if (callbacks) {
	                    var args = Array.prototype.slice.call(arguments, 1);
	                    for (var i = 0; i < callbacks.length; i++) {
	                        try {
	                        	callbacks[i].apply(null, args);
	                        } catch(err){}
	                    }
	                }
	            }
	        };
	    }()
};