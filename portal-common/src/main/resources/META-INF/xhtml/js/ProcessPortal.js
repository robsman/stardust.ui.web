/*
 * IppProcessPortal.js APIs which are needed in new HTML5 based portal
 */

if (!window["InfinityBpm"]) {
	InfinityBpm = new function() {
	}
} // !InfinityBpm

if (!window["InfinityBpm.ProcessPortal"]) {
	InfinityBpm.ProcessPortal = new function() {

		/*
		 * 
		 */
		function sendIppAiClosePanelCommand(contentId, commandId, fallback) {
	    	BridgeUtils.FrameManager.doWithContentFrame(contentId, function(contentFrame) {
	    		var wndEmbeddedWebApp = contentFrame.contentWindow;
		        if (wndEmbeddedWebApp) {
		        	if (wndEmbeddedWebApp.performIppAiClosePanelCommand) {
		        		// function is present, so proceed synchronously
		        		invokeIppAiClosePanelCommand(wndEmbeddedWebApp, commandId);
		        	} else if ( !wndEmbeddedWebApp.document || ('loading' == wndEmbeddedWebApp.document.readyState)) {
		        		// if function is not present, this typically means the iFrame content is currently being loaded, ..
		        		try {
		        			// ... so proceed asynchronously
		        			debug("Asynchronously sending close command: " + commandId);
		        			contentFrame.onload = function(event) {
		        				// unregister self to fire event only once
		        				debug("Loaded external Web App: " + event);
		        				event.target.onload = undefined;
				                var wndEmbeddedWebApp = event.target.contentWindow;
				                if (wndEmbeddedWebApp.performIppAiClosePanelCommand) {
				                	invokeIppAiClosePanelCommand(wndEmbeddedWebApp, commandId);
				                } else {
				                	if (fallback) {
				                		window.setTimeout(function() {
				                			confirmCloseCommandFromExternalWebApp(commandId);
				                		});
				                	} else {
				                		alert("Did not find performIppAiClosePanelCommand() method in embedded AI panel's iFrame.");
				                	}
				                }
		        			};
		        			return;
		        		} catch (e) {
		        			alert('Failed registering <onload> handler: ' + e.message);
		        		}
		        	} else {
		        		if (fallback) {
	                		window.setTimeout(function() {
	                			confirmCloseCommandFromExternalWebApp(commandId);
	                		});
	                	} else {
	                		alert("Did not find performIppAiClosePanelCommand() method in embedded AI panel's iFrame.");
	                	}
		        	}
		        } else {
		        	alert('Failed resolving content window of external Web App iFrame.');
		        }
	    	});
	    }

		/*
		 * 
		 */
		function confirmCloseCommandFromExternalWebApp(commandId) {
			// Operate on Active Tab
			var tabWindow = BridgeUtils.FrameManager.getTabWindowAndDocument(true);
			if (tabWindow) {
				var ippPortalDom = tabWindow.doc;
				var divRemoteControl = ippPortalDom.getElementById('ippProcessPortalActivityPanelRemoteControl');
				if ( !divRemoteControl) {
					alert('Could not find the IPP Process Portal Remote Control infrastructure.');
					return;
				}

				var fldCommandId = divRemoteControl.getElementsByTagName('input')[0];
				if (fldCommandId) {
				    fldCommandId.value = commandId;

				    try {
				    	if ('function' === typeof tabWindow.win.submitForm) {
				    		// Trinidad
				    		tabWindow.win.submitForm(fldCommandId.form, 1, {source: fldCommandId.id});
				    	} else if ('function' === typeof tabWindow.win.contentWindow.iceSubmitPartial) {
				    		// ICEfaces
				    		tabWindow.win.contentWindow.iceSubmitPartial(fldCommandId.form, fldCommandId, null);
				    	}
				    	return;
				    }
				    catch (x) {
				    	alert('Failed submitting form: ' + x);
				    }
				}
				else {
					alert('Could not find the command field.');
				}
			} else {
				alert('Could not find iFrame for active tab/view.');
			}
		}

	    /*
	     * Private
	     */
	    function invokeIppAiClosePanelCommand(wndEmbeddedWebApp, commandId) {
	        try {
	          //alert("Found embedded AI panel notification function: " + wndEmbeddedWebApp.performIppAiClosePanelCommand);
	        	wndEmbeddedWebApp.performIppAiClosePanelCommand(commandId);
	        	return;
	        } catch (x) {
	          // probably forbidden to access location, assuming other page
	        	alert('Failed invoking performIppAiClosePanelCommand() function in target iFrame: ' + x.message);
	        }
	    }

	    /*
	     * 
	     */
	    function debug(msg) {
	    	parent.BridgeUtils.log(msg);
	    }

		return {
	        sendCloseCommandToExternalWebApp: function(contentId, commandId, fallback) {
	            try {
	            	sendIppAiClosePanelCommand(contentId, commandId, fallback);
	            } catch (e) {
	            	alert('Failed notifying external Web App: ' + e.message);
	            }
	        },
	        
	        confirmCloseCommandFromExternalWebApp : function(commandId) {
	            try {
	            	confirmCloseCommandFromExternalWebApp(commandId);
	            } catch (e) {
	            	alert('Failed notifying external Web App: ' + e.message);
	            }	        	
	        },

			completeActivity : function() {
				confirmCloseCommandFromExternalWebApp('complete');
			},

			qaPassActivity : function() {
				confirmCloseCommandFromExternalWebApp('qaPass');
			},
			
			qaFailActivity : function() {
				confirmCloseCommandFromExternalWebApp('qaFail');
			},

			suspendActivity : function(saveOutParams) {
				confirmCloseCommandFromExternalWebApp(saveOutParams ? 'suspendAndSave' : 'suspend');
			},

			abortActivity : function() {
				confirmCloseCommandFromExternalWebApp('abort');
			}
		}
	};
}