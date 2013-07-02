/*
 *
 */

if (!window["Messaging"]) {
	Messaging = new function() {

		var extenssionListeners = [];

		/*
		 *
		 */
		function init() {
		try {
			var win = window;

				if (win.postMessage) {
				  if (win.addEventListener) {
					  win.addEventListener("message", handlePostMessage, true);
	              } else if (ippPortalWin.attachEvent) {
			  win.attachEvent("onmessage", handlePostMessage);
	              } else {
	                debug("This browser does not support safe cross iframe messaging.");
	              }
				} else {
	              debug("This browser does not support safe cross iframe messaging.");
				}
			} catch (e) {
				alert("Failed enabling safe cross domain iframe messaging: " + e.message);
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
						InfinityBpm.ProcessPortal.activityClosePanelCommand(message);
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
			var jsonStr;
			try {
				if (typeof input === 'string' || input instanceof String){
					// String. So it will be Stringified JSON, Validation is done at serverside
					jsonStr = input;
					proceed = true;
				} else if (typeof input === 'object') {
					jsonStr = JSON.stringify(input);
					proceed = true;
				}
			} catch(x) {}

			if (proceed) {
				BridgeUtils.View.doPartialSubmit("modelerLaunchPanels", "viewFormLP", "messageData", message);
			} else {
				//alert("Post Error");
			}
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