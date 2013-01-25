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
if ( !window["InfinityBpm"]) {

	InfinityBpm = new function() {

	};

} // !InfinityBpm

var InfinityBpm = window["InfinityBpm"];

var CONTENT_FRAME_CLOSE_DELAY  = 100;

if ( !InfinityBpm.ProcessPortal || !InfinityBpm.ProcessPortal.isFullApi()) {

InfinityBpm.ProcessPortal = new function() {

	// Contains custom object like {id:'frmId1', posX: 20, posY: 30}
	var iFrames = new Array();

	var mainIppWindow = null;

	// define module private functions

	function debug(msg) {
		//alert(msg);
	}

	/*
	 * Called on potalMain.xhtml body onload
	 */
	function init() {
		var portalWin = ippPortalWindow();
		portalWin.onscroll = function(event){
			handleScroll();
		};

	    registerMessageHandler();
	}

	/*
	 * Registers message handler for postMessage() API
	 */
    function registerMessageHandler() {
    	try {
    		var ippPortalWin = ippPortalWindow();
    		var ippMainWind = ippMainWindow();

			if (ippPortalWin.postMessage) {
			  if (ippPortalWin.addEventListener) {
			    //alert('Subscribing for postMessage ..');
				  ippPortalWin.addEventListener("message", handleRemoteControlMessage, true);

			    // main window will just forward to portal window
				  ippMainWind.addEventListener("message", handleRemoteControlMessage, true);
              } else if (ippPortalWin.attachEvent) {
                //alert('Attaching to onmessage event ..');
            	  ippPortalWin.attachEvent("onmessage", handleRemoteControlMessage);

                // main window will just forward to portal window
            	  ippMainWind.attachEvent("onmessage", handleRemoteControlMessage);
              } else {
                debug("This browser does not support safe cross iframe messaging.");
              }
			} else {
              debug("This browser does not support safe cross iframe messaging.");
			}
		} catch (e) {
			alert(getMessage("portal.common.js.safeCrossDomainMessaging.enable.failed", 'Failed enabling safe cross domain iframe messaging: ') + e.message);
		}
    }

	/*
	 * Adjusts all active iFrames as per current browser scroll position
	 */
	function handleScroll() {
		doWithContentFrame(null, function(contentFrame) {
			if (contentFrame.style.display == 'inline') {
				try {
					var scrollX = 0;
					var scrollY = 0;
					var portalWin = ippPortalWindow();
					if (navigator.appName == 'Netscape') {
						scrollX = portalWin.pageXOffset;
						scrollY = portalWin.pageYOffset;
					} else if (navigator.appName == 'Microsoft Internet Explorer') {
						scrollX = portalWin.document.documentElement.scrollLeft;
						scrollY = portalWin.document.documentElement.scrollTop;
					}

					var iFrame = getIframe(contentFrame.getAttribute('name'));
					if (iFrame != null) {
						var newX = iFrame.posX - scrollX;
						var newY = iFrame.posY - scrollY;

						var diffX = getAbsoluteSize(contentFrame.style.left) - newX;
						if (diffX < 0) {
							diffX = -diffX;
						}

						var diffY = getAbsoluteSize(contentFrame.style.top) - newY;
						if (diffY < 0) {
							diffY = -diffY;
						}

						// It's observed that sometimes scrollX/scrollY is 1 when there is no scroll on UI.
						// Also, 1px is very small, can be ignored. So check if it's greater than 1
						if (diffX > 1 || diffY > 1) {
							contentFrame.style.left = newX + "px";
							contentFrame.style.top = newY + "px";
						}
					}
				} catch(e) {
					alert(getMessage("portal.common.js.scroll.error", "Error in handling browser scroll - ") + e.message);
				}
			}
		});
	}

	function getAbsoluteSize(size) {
		if (size.indexOf('px') != -1) {
			return size.substr(0, size.indexOf('px'));
		} else {
			return size;
		}
	}

	function getIframe (id) {
		for (var i = 0; i < iFrames.length; i++) {
			if (iFrames[i].id == id) {
				return iFrames[i];
			}
		}
		return null;
	}

	function getIframeIndex (id) {
		for (var i = 0; i < iFrames.length; i++) {
			if (iFrames[i].id == id) {
				return i;
			}
		}
		return -1;
	}

	function addIframe (id, posX, posY) {
		var iFrame = {id: id, posX: posX, posY: posY};
		var index = getIframeIndex(id);
		if (index == -1) {
			iFrames.push(iFrame);
		} else {
			iFrames.splice(index, 1, iFrame);
		}
	}

	function removeIframe (id) {
		var index = getIframeIndex(id);
		if (index != -1) {
			iFrames.splice(index, 1);
			return true;
		}

		return false;
	}

	function logIframes(txt) {
		var msg = "iFRAMES ";
		msg += (txt != undefined) ? txt : "";
		console.log("%s: Frames Count = %d", msg, iFrames.length);

		for (var i = 0; i < iFrames.length; i++) {
			console.log("%s: Frames[%d] = %s %d %d", msg, i, iFrames[i].id, iFrames[i].posX, iFrames[i].posY );
		}
	}

	// >>> Copied from InfinityBpm_Core - START
    function isThisIppWindow(win) {
        var baseLocation = String(win.document.location);

        // Remove Query Params
        if (-1 != baseLocation.indexOf("?")) {
        	baseLocation = baseLocation.substr(0, baseLocation.indexOf("?"));
        }

        // Check url, it should either read main.iface or login.iface
        if (-1 != baseLocation.indexOf("main.iface") || -1 != baseLocation.indexOf("login.iface")) {
        	return true;
    	}
        else{
        	return false;
        }
    }

    function findIppWindow(win){
        if (!isThisIppWindow(win)) {
        	var frames = win.frames;
        	for(var i=0; i< frames.length; i++) {
        		var ippWindow = findIppWindow(frames[i]);
        		if (ippWindow != null) {
        			return ippWindow;
        		}
        	}
        	return null;
    	}
        else{
        	return win;
        }
    }

    function findIppWindowBottomUp(win){
        if (!isThisIppWindow(win)) {
        	if (win.parent != null && win.parent != win) {
        		return findIppWindowBottomUp(win.parent);
        	}
    	}
        else{
        	return win;
        }
    }

    function getIppWindow() {
    	try {
    		var ippWindow = findIppWindowBottomUp(window);
    		if (ippWindow == null && window.opener != null) {
    			ippWindow = findIppWindowBottomUp(window.opener);
    		}
    		return ippWindow;
    	} catch (x) {
    		alert(getMessage("portal.common.js.ippMainWindow.notFound", "Error getting IPP Main Window. Portal will not properly work.") + "\n" + x);
    		return null;
    	}
    }
    // >>> Copied from InfinityBpm_Core - END

    function ippMainWindow() {
      if (null != mainIppWindow) {
      	return mainIppWindow;
      }

      var ippWindow;
      if (InfinityBpm && InfinityBpm.Core) {
    	ippWindow = InfinityBpm.Core.getIppWindow();
      } else {
    	ippWindow = getIppWindow();
      }
      mainIppWindow = ippWindow;
      return ippWindow;
    }

    function ippPortalWindow() {
      return ippMainWindow()["ippPortalMain"];
    }

    function isIppWindow(window) {
      return (window == ippMainWindow()) || (window == ippPortalWindow());
    }

	function closeEmbeddedActivityPanel(targetWindow, commandId) {

	    //alert('Current window: ' + window.location);
		if (isIppWindow(window)) {
			//alert('In main window: ' + window.location);
			handleIppAiClosePanelCommandConfirmation(commandId);
			return;
		}

		if (targetWindow) {
			try {

				if (targetWindow.InfinityBpm.ProcessPortal) {
					//alert('Using direct invocation ... ');

					if ('complete' === commandId) {
						targetWindow.InfinityBpm.ProcessPortal.completeActivity();
					} else if ('qaPass' === commandId) {
						targetWindow.InfinityBpm.ProcessPortal.qaPassActivity();
					} else if ('qaFail' === commandId) {
						targetWindow.InfinityBpm.ProcessPortal.qaFailActivity();
					} else if ('suspendAndSave' === commandId) {
						targetWindow.InfinityBpm.ProcessPortal.suspendActivity(true);
					} else if ('suspend' === commandId) {
						targetWindow.InfinityBpm.ProcessPortal.suspendActivity(false);
					} else if ('abort' === commandId) {
						targetWindow.InfinityBpm.ProcessPortal.abortActivity();
					}

					return;
				} else {
					alert(getMessage("portal.common.js.infinityBpm.processPortal.notFound", 'Did not find InfinityBpm.ProcessPortal module in main page') + typeof targetWindow.InfinityBpm.ProcessPortal);
				}
			} catch (x1) {
				// probably forbidden to access location, assuming other page
				//alert('Failed invoking top level IPP function: ' + x1);

			}

			// trying postMessage
			try {
				if (targetWindow.postMessage) {
					//alert('Using post message ... ');

					targetWindow.postMessage(commandId, "*");

					return;
				}
			} catch (x2) {
				// failed using postMessage, fall back to FIM
				//alert('Failed invoking postMessage: ' + x2);
			}

			try {
				alert(getMessage("portal.common.js.browser.notSupported", 'Unfortunately this browser is currently not yet supported.'));
				return;

				ifrm = document.createElement("IFRAME");
				ifrm.setAttribute('style', 'display: none; width: 0px; height: 0px;');
				// TODO replace with dynamic URL determination
				ifrm.setAttribute("src", "http://localhost:9090/ipp/ipp/process/remoteControl/" + commandId + "EmbeddedActivityPanel.html");
				document.body.appendChild(ifrm);
			} catch (x3) {
				alert(getMessage("portal.common.js.crossDomainPanel.close.failed", 'Failed triggering cross domain panel close: ') + x3.description)
			}
		}
	}

	function handleIppAiClosePanelCommandConfirmation(commandId) {
	  //alert("In IPP frame: " + window.location);

	  var ippPortalDom = ippPortalWindow().document;
	  var divRemoteControl = ippPortalDom.getElementById('ippProcessPortalActivityPanelRemoteControl');
	  if ( !divRemoteControl) {
		  alert(getMessage("portal.common.js.remoteControlInfrastructure.notFound", 'Could not find the IPP Process Portal Remote Control infrastructure.'));
		  return
	  }

	  var fldCommandId = divRemoteControl.getElementsByTagName('input')[0];
	  if (fldCommandId)
	  {
	    //alert("Setting commandId to " + commandId);
	    fldCommandId.value = commandId;

	    try
	    {
	      //alert('Blanked screen ...');

	      if ('function' === typeof ippPortalWindow().submitForm) {
	        // Trinidad
	        ippPortalWindow().submitForm(fldCommandId.form, 1, {source: fldCommandId.id});
	      } else if ('function' === typeof ippPortalWindow().iceSubmitPartial) {
	        // ICEfaces
	        ippPortalWindow().iceSubmitPartial(fldCommandId.form, fldCommandId, null);
	      }

	      return;
	    }
	    catch (x) {
	    	alert(getMessage("portal.common.js.submitForm.failed", 'Failed submitting form: ') + x);
	    }
	  }
	  else {
		  alert(getMessage("portal.common.js.commandField.notFound", 'Could not find the command field.'));
	  }
	}

	function handleRemoteControlMessage(e) {
		//alert("Received event '" + e.data + "' from " + e.origin);
		// TODO: Check origin (e.origin) for Security
		var message = e.data;
		if ((typeof message === 'string' || message instanceof String)) {
			message = trim(message);
			if (message.startsWith("{") || message.startsWith("[")) {
				postMessageReceived(message);
			} else {
				// Backward compatible
				handleIppAiClosePanelCommandConfirmation(message);
			}
		} else if (typeof message === 'object') {
			postMessageReceived(message);
		}
	}

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
			var messageDataInput = document.getElementById("msgFrm:messageData");
			messageDataInput.value = jsonStr;
			iceSubmitPartial(document.getElementById("msgFrm"), messageDataInput);
		} else {
			//alert("Post Error");
		}
	}

	function trim(str)
	{
		return str.replace(/^\s+|\s+$/g,'');
	}

	function doInstallRemoteControlApi()
	{
		if ( !isIppWindow(window)) {
			//debug('Not in main IPP frame');
			return;
		}

		if ( !ippMainWindow().InfinityBpm.ProcessPortal) {

		  debug("Installing IPP activity panel browser API in main window.");

		  // provide activity panel API at top frame
		  ippMainWindow().InfinityBpm.ProcessPortal = new function() {
		    return {
              completeActivity: function() {
		        debug("Delegating complete() to portal frame.");
		        ippPortalWindow().InfinityBpm.ProcessPortal.completeActivity();
              },

              qaPassActivity: function() {
    		    debug("Delegating qaPassActivity() to portal frame.");
    		    ippPortalWindow().InfinityBpm.ProcessPortal.qaPassActivity();
              },

	          qaFailActivity: function() {
	            debug("Delegating qaFailActivity() to portal frame.");
		        ippPortalWindow().InfinityBpm.ProcessPortal.qaFailActivity();
	          },

              suspendActivity: function(saveOutParams) {
                debug("Delegating suspend() to portal frame.");
                ippPortalWindow().InfinityBpm.ProcessPortal.suspendActivity(saveOutParams);
              },

              abortActivity: function() {
                debug("Delegating abort() to portal frame.");
                ippPortalWindow().InfinityBpm.ProcessPortal.abortActivity();
              }
		    };
          };
		}

		//debug("Ready ..");

		var ippPortalDom = ippPortalWindow().document;
		var parentDiv = ippPortalDom.getElementById('ippProcessPortalActivityPanelRemoteControl');

		try {

		  //debug("Parent DIV: " + parentDiv);

		  if (parentDiv) {

			var clientApiContainer = parentDiv.getElementsByTagName('div');
			if ((null != clientApiContainer) && (0 < clientApiContainer.length)) {
				parentDiv.removeChild(clientApiContainer[0]);
			}

			// Nop : postMessage() API handles are already in place
		  }
		} catch (e) {
			alert(getMessage("portal.common.js.remoteControlInfrastructure.enable.failed", 'Failed enabling IPP remote control infrastructure: ') + e.message);
		}
	}

	function findPosition(node) {
	  var curleft = curtop = 0;
	  do {
	    curleft += node.offsetLeft;
	    curtop += node.offsetTop;
	  } while (node = node.offsetParent);

	  var pos = new Object();
	  pos.x = curleft;
	  pos.y = curtop;
	  return pos;
	}

    function doWithContentFrame(contentId, action) {
      var ippMainDom = ippMainWindow().document;
      var frameContainer = ippMainDom.getElementById('ippProcessPortalContentFrameContainer');
      if (frameContainer) {
        var frames = frameContainer.getElementsByTagName('iframe');
        for ( var i = 0; i < frames.length; i++) {
          var frame = frames[i];
          debug('Found content frame: ' + frame);
          if ( !contentId || (contentId == frame.getAttribute('name'))) {
            // invoke callback function
            action(frame);
            if (contentId != null) {
                break;
            }
          }
        }
      }
    }

    /*
     * advanceArgs Example:
     *    {anchorId:'ippProcessAttachmentsAnchor', width:100, height:30, maxWidth:500, maxHeight:550,
     *    "openOnRight:false, anchorXAdjustment:30, anchorYAdjustment:2, zIndex:200, border:'1px solid black'}
     */
    function activateContentFrame(contentId, advanceArgs) {
      debug("About to activate content frame with ID " + contentId);

      if (advanceArgs != undefined)
      {
	      var anchorId = advanceArgs.anchorId;
	      var width = advanceArgs.width;
	      var height = advanceArgs.height;
	      var openOnRight = advanceArgs.openOnRight;
	      var anchorXAdjustment = advanceArgs.anchorXAdjustment;
	      var anchorYAdjustment = advanceArgs.anchorYAdjustment;
	      var zIndex = advanceArgs.zIndex;
	      var border = advanceArgs.border;
      }

      doWithContentFrame(contentId, function(contentFrame) {
        debug("Activating content frame: " + contentFrame);
        var ippPortalDom = ippPortalWindow().document;

        var anchor = anchorId == undefined ? 'ippActivityPanelAnchor' : anchorId;
        var contentPanelAnchor = ippPortalDom.getElementById(anchor);
        if (contentPanelAnchor) {
          debug('Repositioning content frame: ' + contentId + ' (using anchor: ' + contentPanelAnchor + ')');
          var pos = findPosition(contentPanelAnchor);
          debug('Moving to (' + pos.x + ', ' + pos.y + ')');

          var iFrameWith = (width == undefined) ? contentPanelAnchor.offsetWidth : width;
          var iFrameHeight = (height == undefined) ? contentPanelAnchor.offsetHeight : height;

          openOnRight = (openOnRight == undefined) ?  true : openOnRight;
          anchorXAdjustment = (anchorXAdjustment == undefined) ? 0 : anchorXAdjustment;
          anchorYAdjustment = (anchorYAdjustment == undefined) ? 0 : anchorYAdjustment;

          var posX = openOnRight ? pos.x : (pos.x - iFrameWith);
          posX += anchorXAdjustment;
          var posY = pos.y + anchorYAdjustment;

          contentFrame.style.position = 'absolute';
          contentFrame.style.left = posX + 'px';
          contentFrame.style.top = posY + 'px';
          contentFrame.style.width = iFrameWith + 'px';
          contentFrame.style.height = iFrameHeight + 'px';

          if (border != undefined) {
          	contentFrame.style.border = border;
          }

          if (zIndex != undefined) {
        	  contentFrame.style.zIndex = zIndex;
          }

          addIframe(contentId, posX, posY);
          // This is needed because if page is scrolled at the time of iFrame activation
          // Then it has to be readjusted for scroll position.
          handleScroll();
        }

        debug('Displaying content frame: ' + contentId);
        contentFrame.style.display = 'inline';
      });
    }

    function resizeAndRepositionContentFrame(contentId, advanceArgs) {
    	debug("About to resize and reposition content frame with ID " + contentId);
    	doWithContentFrame(contentId, function(contentFrame) {
    		activateContentFrame(contentId, advanceArgs);

    		if (advanceArgs.width != undefined) {
    			var newWidth = (advanceArgs.maxWidth != undefined && advanceArgs.maxWidth < advanceArgs.width) ? advanceArgs.maxWidth : advanceArgs.width;
    			contentFrame.style.width = newWidth + 'px';
    		}

    		if (advanceArgs.height != undefined) {
    			var newHeight = (advanceArgs.maxHeight != undefined && advanceArgs.maxHeight < advanceArgs.height) ? advanceArgs.maxHeight : advanceArgs.height;
    			contentFrame.style.height = newHeight + 'px';
    		}
    	});
    }

    function deactivateContentFrame(contentId) {
      debug('About to hide content frame: ' + contentId);
      doWithContentFrame(contentId, function(contentFrame) {
        debug('Hiding content frame: ' + contentId);
        contentFrame.style.display = 'none';
        removeIframe(contentId);
      });
    }

    /**
     * resizes modeler Outline Iframe and adjust associated div
     */
    function resizeModelerOutlineIFrame(elementId){
    	if (InfinityBpm && InfinityBpm.Core){
        	var heightDivOffsetTop = InfinityBpm.Core.getOffsetTop(ippPortalWindow().document.getElementById("outlineAnchor"));
      	  	var windowSize = InfinityBpm.Core.getBrowserDimensions();

        	// set hieght and width
      	  	var dimensions = {};
      	    dimensions.height = windowSize.height - heightDivOffsetTop - 80;
      	    dimensions.width = 280;

    		resizeAndRepositionContentFrame(elementId, dimensions);

    		var div = InfinityBpm.Core.getElementsWithIDLike('div', "outlineAnchor", ippPortalWindow().document);
      		div[0].style.height = (dimensions.height + 5) + "px";
    		div[0].style.width = (dimensions.width + 5) + "px";
    	}
	 }

    /**
     * resizes Process Definition Iframe and adjust associated div
     */
    function resizeProcessDefinitionIFrame(elementId, event) {
		if (InfinityBpm && InfinityBpm.Core){
	    	var divOffsetTop = InfinityBpm.Core.getOffsetTop(ippPortalWindow().document.getElementById("processDefinitionFrameAnchor"));
	       	var divOffsetLeft = InfinityBpm.Core.getOffsetLeft(ippPortalWindow().document.getElementById("processDefinitionFrameAnchor"));

	  	  	var windowSize = InfinityBpm.Core.getBrowserDimensions();

	  	  	// set height and width
	  	  	var dimensions = {};
	  	  	dimensions.height = windowSize.height - divOffsetTop - 80;
	  	  	dimensions.width = windowSize.width - divOffsetLeft - 20;

	  	  	resizeAndRepositionContentFrame(elementId, dimensions);

			window.parent.EventHub.events.publish('PROCESS_IFRAME_RESIZED', dimensions);

			var div = InfinityBpm.Core.getElementsWithIDLike('div', "processDefinitionFrameAnchor", ippPortalWindow().document);
			div[0].style.height = (dimensions.height + 5) + "px";
			div[0].style.width = (dimensions.width + 5) + "px";
		}
	 }

    function closeContentFrame(contentId) {
      debug('About to close content frame: ' + contentId);
      doWithContentFrame(contentId, function(contentFrame) {
        debug('Closing content frame: ' + contentId);
        contentFrame.style.display = 'none';

        // Delayed removal of iFrame, This is a workaround for ICEfaces concurrent dom view issue with iFrames
        contentFrame.src = "about:blank";
        window.setTimeout(function() {
        	if (contentFrame) {
        		if (contentFrame.parentNode) {
        			contentFrame.parentNode.removeChild(contentFrame);
        		}
        	}
        }, CONTENT_FRAME_CLOSE_DELAY);

        removeIframe(contentId);
      });
    }

    function invokeIppAiClosePanelCommand(wndEmbeddedWebApp, commandId) {
      try {
        //alert("Found embedded AI panel notification function: " + wndEmbeddedWebApp.performIppAiClosePanelCommand);

        wndEmbeddedWebApp.performIppAiClosePanelCommand(commandId);
        return;
      } catch (x) {
        // probably forbidden to access location, assuming other page
    	  alert(getMessage("portal.common.js.performIppAiClosePanelCommand.invoke.failed", 'Failed invoking performIppAiClosePanelCommand() function in target iFrame: ') + x.message);
      }
    }

    function sendIppAiClosePanelCommand(contentId, commandId) {
      doWithContentFrame(contentId, function(contentFrame) {
        // debug('Sending close command (' + commandId + ') to content frame: ' + contentId);

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
                	alert(getMessage("portal.common.js.performIppAiClosePanelCommand.notFound", "Did not find performIppAiClosePanelCommand() method in embedded AI panel's iFrame."));
                }
              };
              return;
            } catch (e) {
            	alert(getMessage("portal.common.js.onloadHandler.register.failed", 'Failed registering <onload> handler: ') + e.message);
            }
          } else {
        	  alert(getMessage("portal.common.js.performIppAiClosePanelCommand.notFound", "Did not find performIppAiClosePanelCommand() method in embedded AI panel's iFrame."));
          }
        } else {
        	alert(getMessage("portal.common.js.externalWebApp.iframe.resolve.failed", 'Failed resolving content window of external Web App iFrame.'));
        }
      });
    }

    function createOrActivateContentFrame(contentId, contentUrl, advanceArgs) {

      var ippMainDom = ippMainWindow().document;
      var frameContainer = ippMainDom.getElementById('ippProcessPortalContentFrameContainer');
      if ( !frameContainer) {
        frameContainer = ippMainDom.createElement('div');
        debug("Created content frame container:" + frameContainer);
        frameContainer.setAttribute('id', 'ippProcessPortalContentFrameContainer');
        ippMainDom.getElementById('ippPortalOverlays').appendChild(frameContainer);
      }
      debug("Resolved content frame container: " + frameContainer);

      var contentFrame;
      var frames = frameContainer.getElementsByTagName('iframe');
      for ( var i = 0; i < frames.length; i++) {
        var frame = frames[i];
        debug('Found content frame: ' + frame);
        if (contentId == frame.getAttribute('name')) {
          contentFrame = frame;
        } else {
          //frame.style.display = 'none';
        }
      }

      if ( !contentFrame) {
        // create content frame
        debug('Creating new content frame: ' + contentId);

        contentFrame = ippMainDom.createElement('iframe');
        contentFrame.setAttribute('id', contentId);
        contentFrame.setAttribute('name', contentId);
        contentFrame.setAttribute('frameBorder', '0');
        contentFrame.setAttribute('marginWidth', '0');
        contentFrame.setAttribute('marginHeight', '0');
        contentFrame.setAttribute('scrolling', 'auto');
        contentFrame.setAttribute('style', 'display: none; z-index:100; position: relative; top: 450px; left: 100px; width: 400px; height: 300px;');
        if (advanceArgs != undefined) {
        	contentFrame.setAttribute('noUnloadWarning', advanceArgs.noUnloadWarning);
        }

        contentFrame.setAttribute('src', contentUrl);

        frameContainer.appendChild(contentFrame);
      }

      activateContentFrame(contentId, advanceArgs);
    }

    function getMessage(messageProp, defaultMsg) {
		if (InfinityBPMI18N && InfinityBPMI18N.common)
		{
			var propVal = InfinityBPMI18N.common.getProperty(messageProp, defaultMsg);
			if (propVal && propVal != "")
			{
				return propVal;
			}
		}

		return defaultMsg;
    }	//// interface

	return {

	    isFullApi : function() {
	      return true;
	    },

	    init : function() {
	    	init();
	    },

		enableRemoteControlApi: function() {
			if (ippPortalWindow() == window) {
				doInstallRemoteControlApi();
			}
		},

		completeActivity: function() {
		  try {
			if (ippPortalWindow() == window) {
				handleIppAiClosePanelCommandConfirmation('complete');
			} else {
				closeEmbeddedActivityPanel(ippPortalWindow(), 'complete');
			}
		  } catch (e) {
			  alert(getMessage("portal.common.js.activity.complete.failed", 'Failed completing activity: ') + e.message);
		  }
		},

		qaPassActivity: function() {
		  try {
			if (ippPortalWindow() == window) {
				handleIppAiClosePanelCommandConfirmation('qaPass');
			} else {
				closeEmbeddedActivityPanel(ippPortalWindow(), 'qaPass');
			}
		  } catch (e) {
			  alert(getMessage("portal.common.js.activity.qaPass.failed", 'Exception occurred while Quality Assurance Pass activity: ') + e.message);
		  }
		},

		qaFailActivity: function() {
		  try {
			if (ippPortalWindow() == window) {
				handleIppAiClosePanelCommandConfirmation('qaFail');
			} else {
				closeEmbeddedActivityPanel(ippPortalWindow(), 'qaFail');
			}
		  } catch (e) {
			  alert(getMessage("portal.common.js.activity.qaFail.failed", 'Exception occurred while Quality Assurance Fail activity: ') + e.message);
		  }
		},

		suspendActivity: function(saveOutParams) {
		  try {
			if (ippPortalWindow() == window) {
				handleIppAiClosePanelCommandConfirmation(saveOutParams ? 'suspendAndSave' : 'suspend');
			} else {
				closeEmbeddedActivityPanel(ippPortalWindow(), saveOutParams ? 'suspendAndSave' : 'suspend');
			}
          } catch (e) {
        	  alert(getMessage("portal.common.js.activity.suspend.failed", 'Failed suspending activity: ') + e.message);
          }
		},

		abortActivity: function() {
		  try {
			if (ippPortalWindow() == window) {
				handleIppAiClosePanelCommandConfirmation('abort');
			} else {
				closeEmbeddedActivityPanel(ippPortalWindow(), 'abort');
			}
          } catch (e) {
        	  alert(getMessage("portal.common.js.activity.abort.failed", 'Failed aborting activity: ') + e.message);
          }
		},

		createOrActivateContentFrame: function(contentId, contentUrl, advanceArgs) {
		  try {
		    createOrActivateContentFrame(contentId, contentUrl, advanceArgs);
		  } catch (e) {
			  alert(getMessage("portal.common.js.contentFrame.activate.failed", 'Failed during content frame activation: ') + e.message);
		  }
		},

        deactivateContentFrame: function(contentId) {
          try {
            deactivateContentFrame(contentId);
          } catch (e) {
        	  alert(getMessage("portal.common.js.contentFrame.deactivate.failed", 'Failed during content frame deactivation: ') + e.message);
          }
        },

        closeContentFrame: function(contentId) {
          try {
            closeContentFrame(contentId);
          } catch (e) {
        	  alert(getMessage("portal.common.js.contentFrame.close.failed", 'Failed during content frame close: ') + e.message);
          }
        },

        resizeContentFrame: function(contentId, advanceArgs) {
          try {
        	  activateContentFrame(contentId, advanceArgs);
          } catch (e) {
        	  alert(getMessage("portal.common.js.contentFrame.resize.failed", 'Failed during content frame resize: ') + e.message);
          }
        },

        resizeAndRepositionContentFrame: function(contentId, advanceArgs) {
            try {
            	resizeAndRepositionContentFrame(contentId, advanceArgs);
            } catch (e) {
            	alert(getMessage("portal.common.js.contentFrame.resize.failed", 'Failed during content frame resize: ') + e.message);
            }
        },

        sendCloseCommandToExternalWebApp: function(contentId, commandId) {
          try {
            sendIppAiClosePanelCommand(contentId, commandId);
          } catch (e) {
        	  alert(getMessage("portal.common.js.externalWebApp.notify.failed", 'Failed notifying external Web App: ') + e.message);
          }
        },

        postMessage: function(input) {
        	postMessageReceived(input);
        },

        resizeProcessDefinitionIFrame: function(elementId, event) {
        	resizeProcessDefinitionIFrame(elementId, event);
		},

		resizeModelerOutlineIFrame : function(elementId) {
		   	resizeModelerOutlineIFrame(elementId);
		}
	};
};

} // !InfinityBpm.ProcessPortal