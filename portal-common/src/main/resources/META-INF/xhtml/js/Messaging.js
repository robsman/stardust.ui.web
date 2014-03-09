/**
 * @author Subodh.Godbole 
 */

if (!window["Messaging"]) {
	Messaging = new function() {

		var extenssionListeners = [];

		var parentSubscriber = false;
		var parentSubscriberEventNames = [];

		/*
		 *
		 */
		function init() {
		try {
			var win = window;

				if (win.postMessage) {
				  if (win.addEventListener) {
					  win.addEventListener("message", handlePostMessage, true);
	              } else if (win.attachEvent) {
			  win.attachEvent("onmessage", handlePostMessage);
	              } else {
	                debug("This browser does not support safe cross iframe messaging.");
	              }
				} else {
	              debug("This browser does not support safe cross iframe messaging.");
				}
				
				subscribeToEventHub();
			} catch (e) {
				alert("Failed enabling safe cross domain iframe messaging: " + e.message);
			}
		}

		/*
		 *
		 */
		function subscribeToEventHub() {
			if (window.EventHub != null) {
				window.EventHub.events.subscribe("*", eventHubSubscriber);
			} else {
				window.setTimeout(subscribeToEventHub, 200);
			}
		}
		
		/*
		 * 
		 */
		function eventHubSubscriber(eventName) {
			if (parentSubscriber) {
				BridgeUtils.log("Event to sent to parent - " + eventName);
				if (parentSubscriberEventNames.indexOf(eventName) > -1) {
					var args = null;
					if (arguments.length > 1) {
						args = Array.prototype.slice.call(arguments, 1);				
					}
		
					var message = {}
					message.eventName = eventName;
					if (null != args) {
						message.args = args;
					}
					
					message = JSON.stringify(message);
					BridgeUtils.log("Message to post: " + message);
					try {
						if (opener) {
							opener.postMessage(message, "*");
						} else if (parent) {
							parent.postMessage(message, "*");				
						}
					} catch(e) {
						BridgeUtils.log(e);
					}
				}
			} else {
				// NOP
			}
		}

		/*
		 * 
		 */
		function register(expr, func) {
			// If exists it overrides, One listener for one expr
			extenssionListeners[expr] = func;
		}

		/*
		 *
		 */
		function unregister(expr) {
			if (extenssionListeners[expr]) {
				var tempExtenssionListeners = [];
				for (var exp in extenssionListeners) {
					if(exp != expr) {
						tempExtenssionListeners[exp] = extenssionListeners[exp];
					}
				}

				extenssionListeners = tempExtenssionListeners;
			}
		}

		/*
		 * Private
		 */
		function handlePostMessage(e) {
			// TODO: Check origin (e.origin) for Security
			try {
				var message = e.data;
				if ((typeof message === 'string' || message instanceof String)) {
					message = trim(message);
					if (message.indexOf("{") == 0 || message.indexOf("[") == 0) {
						postMessageReceived(message);
					} else {
						// Backward compatible
						// TODO Use - extenssionListeners
						InfinityBpm.ProcessPortal.confirmCloseCommandFromExternalWebApp(message);
					}
				} else if (typeof message === 'object') {
					postMessageReceived(message);
				}
			} catch(ex){
				alert("something is wrong in postMessage(): " + ex);
			}
		}

		/*
		 * Private
		 */
		function postMessageReceived(input) {
			var proceed = false;
			var jsonObj;
			try {
				if (typeof input === 'string' || input instanceof String){
					jsonObj = JSON.parse(input);
					proceed = true;
				} else if (typeof input === 'object') {
					jsonObj = input;
					proceed = true;
				}
			} catch(x) {}

			if (proceed) {
				var map = sortCommandsByType(jsonObj);
				if (map.SYSTEM != undefined) {
					var jsonStr = JSON.stringify(map.SYSTEM);
				BridgeUtils.View.doPartialSubmit("portalLaunchPanels", "viewFormLP", "messageData", jsonStr);
				} 
				
				if (map.EVENTHUB_SUBSCRIPTION != undefined) {
					// TODO: Accept Event Names
					for(var i in map.EVENTHUB_SUBSCRIPTION) {
						if (map.EVENTHUB_SUBSCRIPTION[i].data.target == "parent" || 
								map.EVENTHUB_SUBSCRIPTION[i].data.target == "opener") {
							parentSubscriber = true;
							parentSubscriberEventNames = map.EVENTHUB_SUBSCRIPTION[i].data.eventNames;
							BridgeUtils.log("Event Subscribtion set for parent");
						}
					}
				}

				if (map.EVENTHUB != undefined) {
					if (window.EventHub) {
						for(var i in map.EVENTHUB) {
							var args = [map.EVENTHUB[i].eventName].concat(map.EVENTHUB[i].args);
							window.EventHub.events.publish.apply(null, args);
						}
					}
				}
			} else {
				//alert("Post Error");
			}
		}

		/*
		 * 
		 */
		function sortCommandsByType(jsonObj) {
			var ret = {};
			
			var jsonArr;
			if (Object.prototype.toString.call(jsonObj) === "[object Array]") {
				jsonArr = jsonObj;
			} else {
				jsonArr = [];
				jsonArr.push(jsonObj);
			}
			
			for (var i in jsonArr) {
				if (isSystemCommand(jsonArr[i])) {
					if (isEventSubscriberCommand(jsonArr[i])) {
						addToMap(ret, "EVENTHUB_SUBSCRIPTION", jsonArr[i]);
					} else {
						addToMap(ret, "SYSTEM", jsonArr[i]);
					}
				} else if (isEventHubCommand(jsonArr[i])) {
					addToMap(ret, "EVENTHUB", jsonArr[i])
				}
			}
			
			return ret;
		}

		/*
		 * 
		 */
		function addToMap(map, key, elm) {
			if (map[key] == undefined) {
				map[key] = [];
			}
			map[key].push(elm);
		}

		/*
		 * 
		 */
		function isSystemCommand(jsonObj) {
			return jsonObj.type && jsonObj.data;
		}

		/*
		 * 
		 */
		function isEventSubscriberCommand(jsonObj) {
			return jsonObj.type == "subscribeEvents";
		}

		/*
		 * 
		 */
		function isEventHubCommand(jsonObj) {
			return jsonObj.eventName;
		}

		/*
		 * private
		 */
		function trim(str)
		{
			return str.replace(/^\s+|\s+$/g,'');
		}

		/*
		 * private
		 */
		function debug(msg) {
			//alert(msg);
		}

		return {
			init : init,
			register : register,
			unregister : unregister
		}
	};

	Messaging.init();
}