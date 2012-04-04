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

if ( !InfinityBpm.Core) {

  InfinityBpm.Core = new function() {

    // define module private state
    var mainIppFrame = getIppWindow();
    var pageUnloadHandlerRegistered = false;
    var iceHandlerRegistered = false;
    var logoutUri = ""; 
    
    // define module private functions

    function debug(msg) {
      //alert(msg);
    }

    function doWithContentFrame(contentId, callback) {
      var ippMainDom = mainIppFrame.document;
      var frameContainer = ippMainDom.getElementById('ippProcessPortalContentFrameContainer');
      if (frameContainer) {
        var frames = frameContainer.getElementsByTagName('iframe');
        for ( var i = 0; i < frames.length; i++) {
          var frame = frames[i];
          debug('Found content frame: ' + frame);
          if ( !contentId || (contentId == frame.getAttribute('name'))) {
            // invoke callback function
            callback(frame);
            if (contentId != null) {
            	break;
            }
          }
        }
      }
    }
    
    function registerPageUnloadHandler() {
      if ( !pageUnloadHandlerRegistered) {
        debug("Registering onUnload handler: " + window.location);

        var currentBeforeUnload = window.onbeforeunload;
        window.onbeforeunload = function(event) {
          debug('<before unload>: ' + event);

          var messages = new Array();
          try {
            var message = onPageUnload(window, event, 'before');
            if (message) {
              messages.push(message);
            }
          } catch (e) {
        	  alert(getMessage("portal.common.js.beforeUnloadEvent.handling.failed", "Failed handling before unload event: ") + e.message);
          }
          
          if (currentBeforeUnload) {
            var message = currentBeforeUnload(event);
            if (message) {
              messages.push(message);
            }
          }
          
          if (0 < messages.length) {
            if (event) {
              event.returnValue = messages.join('\n');
            }
            return messages.join('\n');
          }
        }
        var currentUnload = window.onunload;
        window.onunload = function(event) {
          debug('<unload>: ' + event);

          try {
            onPageUnload(window, event, 'after');
          } catch (e) {
        	  alert(getMessage("portal.common.js.afterUnloadEvent.handling.failed", "Failed handling after unload event: ") + e.message);
          }
          
          if (currentUnload) {
            currentUnload(event);
          }
        }
        
        pageUnloadHandlerRegistered = true;
      }
    }
    
    function onPageUnload(window, event, phaseId) {
      debug("Handling unload event (" + phaseId + ").");
      
      var openContentFrames = new Array();
      doWithContentFrame(null, function(contentFrame) {
     	 if ('true' != contentFrame.getAttribute('noUnloadWarning')) {
    		 openContentFrames.push(contentFrame.getAttribute('name'));
    	 }
      });
      
      debug("Open content frames at page unload (" + phaseId + "): " + openContentFrames);

      var messages = new Array();
      if (0 < openContentFrames.length) {
        if ('before' == phaseId) {
          messages.push(1 == openContentFrames.length //
                  ? getMessage("portal.common.js.oneIframeOpen.warning", 'Currently there is 1 open content frame. If you proceed to load another page any unsaved content will be lost.')
                    : getMessage("portal.common.js.multipleIframesOpen.warning.prefix", 'Currently there are ') + openContentFrames.length + getMessage("portal.common.js.multipleIframesOpen.warning.postfix", ' open content frames. If you proceed to load another page any unsaved content will be lost.'));
        } else if ('after' == phaseId) {
        	alert(getMessage("portal.common.js.unsavedContent.lost.prefix", 'Unsaved content of ') + openContentFrames.length + getMessage("portal.common.js.unsavedContent.lost.postfix", ' content frame(s) will be lost.'));
        }
      }
      
      if (0 < messages.length) {
        debug('Leaving was not permitted.');
        return messages.join('\n');
      }
    }

    function registerIceHandlers() {
    	if (!iceHandlerRegistered) {
	        debug("Registering Ice handlers");
	
	        Ice.onSessionExpired('document:body', function() {
	        	handleServerDisconnect("SessionExpired");
	        });

	        iceHandlerRegistered = true;

	        debug("Registered Ice handlers");
    	}
    }
    
    function handleServerDisconnect(type) {
    	debug("Handling Server Disconnect for " + type);
    	
    	//Close All iFrames
    	if (closeIframes(2)) {
        	if (logoutUri != null) {
        		var ippWin = getIppWindow();
        		var baseLocation = String(ippWin.document.location);
                if (-1 == baseLocation.indexOf("main.iface")) {
                	alert(getMessage("portal.common.js.incorrectMainPage.redirect.failed", "Unexpected main page, should be .../plugins/common/main.iface. Cannot redirect."));
                	return;
                }

                // determine original URI of portalMain.iface
                var fullLogoutUri = baseLocation.substr(0, baseLocation.indexOf("/plugins/common/main.iface")) + logoutUri;
                ippWin.location = fullLogoutUri;
        	} else {
        		alert(getMessage("portal.common.js.logoutURI.cannotRedirect", "Logout URI not available. Cannot redirect."));
        	}
    	}
    }
    
    function closeIframes(tryCount) {
    	tryCount--;
    	
    	var iFrames = new Array();
    	doWithContentFrame(null, function(contentFrame) {
    		iFrames.push(contentFrame);
    	});

    	var errors = new Array();
    	for (var index = 0; index < iFrames.length; index++) {
    		try {
        		debug("Closing iFrame = " + iFrames[index]);
        		iFrames[index].parentNode.removeChild(iFrames[index]);
    		}catch (e) {
    			errors.push(e);
			}
    	}
    	
    	if (0 < errors.length) {
    		var msg = getMessage("portal.common.js.iframe.close.failed", "Could not Close below iFrames. Redirect will not properly work");
    		for (var i = 0; i < errors.length; i++) {
    			msg += "\n\t" + errors[i].message;
    		}
    		alert(msg);
    		return false;
    	}
    	
    	// Confirm iFrames Count
    	iFrames = new Array();
    	doWithContentFrame(null, function(contentFrame) {
    		iFrames.push(contentFrame);
    	});

    	if (iFrames.length > 0) {
        	if (tryCount > 0) {
        		return closeIframes(tryCount);
        	} else {
        		alert(getMessage("portal.common.js.iframe.close.failed.prefix", "Could not Close ") + iFrames.length + getMessage("portal.common.js.iframe.close.failed.postfix", " iFrames. You will not be redirected back to login page."));
        		return false;
        	}
    	}
    	
    	return true;
    }

    function onPortalMainResized() {
      // resize outer window accordingly
      var portalMainWnd = mainIppFrame["ippPortalMain"];
      var portalMainBody = portalMainWnd.document.getElementsByTagName("body")[0];
      if (portalMainBody) {
        debug("portalMain size is (" + portalMainBody.offsetWidth + ", " + portalMainBody.offsetHeight + ")");

        var mainBody = mainIppFrame.document.getElementsByTagName("body")[0];
        
        var portalMainContainer = mainIppFrame.document.getElementById("ippPortalMainContainer");
        if (portalMainContainer) {
          //portalMainContainer.style.width = portalMainBody.offsetWidth + "px";
          if (mainBody.offsetHeight > portalMainBody.offsetHeight) {
            portalMainContainer.style.height = "100%"; 
          } else if (mainBody.offsetHeight < portalMainBody.offsetHeight) {
         	// Add some margin height, so that there is no possibility of 
        	// vertical scroll bar getting added to iFrame
            portalMainContainer.style.maxHeight = (portalMainBody.offsetHeight + 30) + "px";
          }
        }
      }
    }
    
    function onPortalMainLoaded(event) {

      registerPageUnloadHandler();
      
      registerIceHandlers();
      
      var baseLocation = String(mainIppFrame.document.location);
      if (-1 == baseLocation.indexOf("main.iface")) {
    	  alert(getMessage("portal.common.js.incorrectMainPage.redirect.wontWork", "Unexpected main page, should be .../plugins/common/main.iface. Redirects will not properly work."));
        return;
      }

      // determine original URI of portalMain.iface
      var portalMainUri = baseLocation.substr(0, baseLocation.indexOf("main.iface")) + "portalMain.iface";
      debug("main.iface location:\n  " + baseLocation + "\nexpected portalMain.iface location:\n  " + portalMainUri);
      
      var portalMainWnd = mainIppFrame["ippPortalMain"];
      
      var onPortalMain = false;
      var innerLocation;
      try {
        innerLocation = String(portalMainWnd.location);
      } catch (x) {
    	  alert(getMessage("portal.common.js.innerPageLocation.determine.failed", "Failed determining inner page location: ") + x.message);
        innerLocation = undefined;
      }

      try {
        if (innerLocation) {
          // if the load is just a refresh of the portalMain page, do nothing
          if (innerLocation.length >= portalMainUri.length) {
            var prefix = innerLocation.slice(0, portalMainUri.length);
            
            onPortalMain = (prefix == portalMainUri);
            debug("On portalMain page: " + onPortalMain + "\n" + prefix + "\n" + portalMainUri);
          }
        }
      } catch (x) {
    	  alert(getMessage("portal.common.js.innerPageLocation.portalMain.determine.failed", "Failed determining if inner page is portalMain.iface: ") + x.message);
      }

      if (onPortalMain) {
    	mainIppFrame.onresize = function () {
    		resizePortalMainWindow();
    		onPortalMainResized();
    	}
        portalMainWnd.onresize = onPortalMainResized;
        portalMainWnd.document.body.onresize = onPortalMainResized;
        
        onPortalMainResized();
      } else {
        if ( !innerLocation) {
          // location of iframe not readable, possible do to access restrictions
          var baseUri = portalMainUri.slice(0, 1 - "portalMain.iframe".length);
          innerLocation = baseUri + "main.iface";
        }
        
        debug("Navigating to inner frame target: " + innerLocation);
        mainIppFrame.location = innerLocation;
      }
    }

    function setLogoutUri(uri) {
    	logoutUri = uri;
    }

    function closeSession() {

      var baseLocation = String(mainIppFrame.document.location);
      if (-1 == baseLocation.indexOf("main.iface")) {
    	  alert(getMessage("portal.common.js.incorrectMainPage.redirect.wontWork", "Unexpected main page, should be .../plugins/common/main.iface. Redirects will not properly work."));
        return;
      }

      // determine original URI of portalMain.iface
      var logoutUri = baseLocation.substr(0, baseLocation.indexOf("/plugins/common/main.iface")) + "/ipp/common/ippPortalLogout.jsp";
      debug("main.iface location:\n  " + baseLocation + "\nexpected ippPortalLogout.jsp location:\n  " + logoutUri);
      
      debug("Navigating to logout URL: " + logoutUri);
      mainIppFrame.location = logoutUri;
    }

    function repositionViewToolbar() {
    	var ippDocument = mainIppFrame["ippPortalMain"].document;
    	var viewToolbarAnchor = ippDocument.getElementById('ippViewToolbarAnchor');
    	if(viewToolbarAnchor) {
    		var toolbar = ippDocument.getElementById('ippViewToolbar');
    		if(toolbar) {
    			var toolbarStart = ippDocument.getElementById('ippViewToolbarStart');
    			var toolbarEnd = ippDocument.getElementById('ippViewToolbarEnd');
    			var toolbarWidth = findPosition(toolbarEnd).x - findPosition(toolbarStart).x;

    			var posToolbarAnchor = findPosition(viewToolbarAnchor);

    			var widthAdjustment = 2;
    			var heightAdjustment = -18;
    			
        		toolbar.style.left = (posToolbarAnchor.x - toolbarWidth + widthAdjustment) + 'px';
        		toolbar.style.top = (posToolbarAnchor.y + heightAdjustment) + 'px';
        		toolbar.style.visibility = "visible";
    		}
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

    function getIppWindow() {
    	try {
    		var ippWindow = findIppWindow(top);
    		return ippWindow;
    	} catch (x) {
    		alert(getMessage("portal.common.js.ippMainWindow.notFound", "Error getting IPP Main Window. Portal will not properly work.") + "\n" + x);
    		return null;
    	}
    }
    
    function resizePortalMainWindow() {
  	  var portalMainContainer = mainIppFrame.document.getElementById("ippPortalMainContainer");
  	  var scrollPos = getWindowScrollPosition(mainIppFrame);
  	  portalMainContainer.style.width = '98%'; // This is required so that all sizes gets recalculated to fit the content

	  //Set portalMainContainer width
	  var widthEndDivOffsetLeft = getOffsetleft(mainIppFrame["ippPortalMain"].document.getElementById("ippPortalContentWidthEnd"));
	  var windowSize = getBrowserDimensions();
	  var width = (widthEndDivOffsetLeft > windowSize.width) ? widthEndDivOffsetLeft : windowSize.width;
	  if (isIE()) {
		  width -= 25;
	  }
	  portalMainContainer.style.width = (width+ 'px');
	  

	  //Set portalMainContainer height	  
	  var endDivContent = mainIppFrame["ippPortalMain"].document.getElementById("ippPortalEndContent");
	  var endDivLP = mainIppFrame["ippPortalMain"].document.getElementById("ippPortalEndLP");
	  var height = getOffsetTop(endDivContent);
	  if (endDivLP) {
		  var endDivLPOffsetTop = getOffsetTop(endDivLP);
		  height = endDivLPOffsetTop > height ? endDivLPOffsetTop : height;
	  }
	  
	  if (isIE7()) {
		  var heightOffset = (30 * height / screen.height);
		  height = height + heightOffset;
	  }
	  
	  var minHeight = screen.height * 80 / 100;
	  height = (height < minHeight) ? minHeight : height;		  

	  portalMainContainer.style.height = (height + 'px');
	  setWindowScrollPosition(mainIppFrame, scrollPos);

	  // Reposition View Specific Toolbar
	  repositionViewToolbar();
    }
    
    function getWindowScrollPosition(targetWin) {
    	var scrollX = 0;
    	var scrollY = 0;
		if (navigator.appName == 'Netscape') {
			scrollX = targetWin.pageXOffset;
			scrollY = targetWin.pageYOffset;
		} else if (navigator.appName == 'Microsoft Internet Explorer') {
			scrollX = targetWin.document.body.scrollLeft;
			scrollY = targetWin.document.body.scrollTop;
		}
		
		return {'x' : scrollX, 'y' : scrollY};
    }
    
    function setWindowScrollPosition(targetWin, scrollPos) {
    	targetWin.scrollTo(scrollPos.x, scrollPos.y);
    }
    
    function getBrowserDimensions() {
    	var winW = screen.availWidth ? screen.availWidth : screen.width;
	  	var winH = screen.availWidth ? screen.availHeight : screen.height;
	  	var mainWin = mainIppFrame.parent;
	  	var mainDoc = mainWin.document;
	  	if (mainDoc.body && mainDoc.body.offsetWidth) {
	  		winW = mainDoc.body.offsetWidth;
	  		winH = mainDoc.body.offsetHeight;
	    }
	  	if (mainDoc.compatMode=='CSS1Compat' && mainDoc.documentElement && mainDoc.documentElement.offsetWidth ) {
	  		winW = mainDoc.documentElement.offsetWidth;
	  		winH = mainDoc.documentElement.offsetHeight;
	    }
	  	if (mainWin.innerWidth && mainWin.innerHeight) {
	  		winW = mainWin.innerWidth;
	  		winH = mainWin.innerHeight;
	    }
	    	
	  	return {'width' : winW, 'height' : winH};
    }
    
    function positionMessageDialog(divId) {
    	var windowSize = getBrowserDimensions();
    	var scrollPos = getWindowScrollPosition(mainIppFrame);
    	var popupDivs = getElementsWithIDLike('div', divId);
    	if (popupDivs && (popupDivs.length > 0)) {
    		try {
	    		var popupDiv = popupDivs[0];
	    		var widthOffset = (popupDiv.offsetWidth < windowSize.width) ? popupDiv.offsetWidth : 0;
	    		var heightOffset = (popupDiv.offsetHeight < windowSize.height) ? popupDiv.offsetHeight : 0;
	    		popupDiv.style.left = (((windowSize.width - widthOffset)/ 2) + scrollPos.x) + 'px';
	    		popupDiv.style.top = (((windowSize.height - heightOffset)/ 2) + scrollPos.y) + 'px';
    		} catch (e) {
	    		popupDiv.style.left = (scrollPos.x + 200) + 'px';
	    		popupDiv.style.top = (scrollPos.y + 200) + 'px';
    		}
    	}
    }
    
    function getElementsWithIDLike(tagName, elementId) {
    	var allElems = document.getElementsByTagName(tagName);
    	var selectedElems = [];    	
    	if (allElems) {
    		for (var i = 0; i < allElems.length; i++) {    			
    			if (allElems[i].id && (allElems[i].id.indexOf(elementId) >= 0)) {
    				selectedElems.push(allElems[i]);
    			}
    		}    			
    	}
    	
    	return selectedElems;
    }
    
    function isIE7() {
    	if (navigator.appVersion && (-1 != navigator.appVersion.indexOf("MSIE 7.0"))) {
    		return true;
    	}
    	
    	return false;
    }
    
    function isIE() {
    	if (navigator.appVersion && (-1 != navigator.appVersion.indexOf("MSIE"))) {
    		return true;
    	}
    	
    	return false;
    }

    function isFF() {
    	if (navigator.userAgent && (-1 != navigator.userAgent.indexOf("Firefox"))) {
    		return true;
    	}
    	
    	return false;
    }
    
    function getOffsetleft(element)
    {
    	var offsetLeft = 0;
    	while (element.tagName != 'BODY') {
    		offsetLeft += element.offsetLeft;
    		element = element.parentNode;
    	}
    	
    	return offsetLeft;
    }
    
    function getOffsetTop(element)
    {
    	var offsetTop = 0;
    	while (element.tagName != 'BODY') {
    		offsetTop += element.offsetTop;
    		element = element.parentNode;
    	}
    	
    	return offsetTop;
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
    }

    // // interface

    return {
      
      onPortalMainLoaded : function(event) {
        onPortalMainLoaded(event);
      },
      
      onPortalMainResized : function() {
        onPortalMainResized();
      },
      
      closeSession : function() {
    	  window.setTimeout(function() {
    		  closeSession();
    	  }, CONTENT_FRAME_CLOSE_DELAY);
      },

      repositionViewToolbar : function() {
    	  repositionViewToolbar();
      },
      
      getIppWindow : function() {
    	  return getIppWindow();
      },
      
      resizePortalMainWindow : function(rdm) {
    	  resizePortalMainWindow(rdm);
      },
      
      registerIceHandlers : function() {
    	  registerIceHandlers();
      },
      
      setLogoutUri : function(uri) {
    	  setLogoutUri(uri);
      },
      
      positionMessageDialog : function(divId) {
    	  positionMessageDialog(divId);
      },
      
      isFF : function() {
    	  return isFF();
      }
    };
  };

} // !InfinityBpm.Core

if ( !InfinityBpm.ProcessPortal) {

  // provide activity panel API at top frame
  InfinityBpm.ProcessPortal = new function() {

	var mainIppFrame = InfinityBpm.Core.getIppWindow();
	  
    return {
  
      isFullApi : function() {
        return false;
      },
      
      completeActivity: function() {
        // alert("Delegating complete() to portal frame.");
        var portalMainWnd = mainIppFrame["ippPortalMain"];
        if (portalMainWnd.InfinityBpm.ProcessPortal) {
          portalMainWnd.InfinityBpm.ProcessPortal.completeActivity();
        } else {
        	alert(getMessage("portal.common.js.processPortal.api.notAvailable", "The Process Portal API is not available."));
        }
      },
  
      qaPassActivity: function() {
          // alert("Delegating qaPassActivity() to portal frame.");
          var portalMainWnd = mainIppFrame["ippPortalMain"];
          if (portalMainWnd.InfinityBpm.ProcessPortal) {
            portalMainWnd.InfinityBpm.ProcessPortal.qaPassActivity();
          } else {
          	alert(getMessage("portal.common.js.processPortal.api.notAvailable", "The Process Portal API is not available."));
          }
      },
        
	  qaFailActivity: function() {
	      // alert("Delegating qaFailActivity() to portal frame.");
	      var portalMainWnd = mainIppFrame["ippPortalMain"];
	      if (portalMainWnd.InfinityBpm.ProcessPortal) {
	        portalMainWnd.InfinityBpm.ProcessPortal.qaFailActivity();
	      } else {
	      	alert(getMessage("portal.common.js.processPortal.api.notAvailable", "The Process Portal API is not available."));
	      }
	   },
  
      suspendActivity: function(saveOutParams) {
        // alert("Delegating suspend() to portal frame.");
        var portalMainWnd = mainIppFrame["ippPortalMain"];
        if (portalMainWnd.InfinityBpm.ProcessPortal) {
          portalMainWnd.InfinityBpm.ProcessPortal.suspendActivity(saveOutParams);
        } else {
        	alert(getMessage("portal.common.js.processPortal.api.notAvailable", "The Process Portal API is not available."));
        }
      },
  
      abortActivity: function() {
        // alert("Delegating abort() to portal frame.");
        var portalMainWnd = mainIppFrame["ippPortalMain"];
        if (portalMainWnd.InfinityBpm.ProcessPortal) {
          portalMainWnd.InfinityBpm.ProcessPortal.abortActivity();
        } else {
        	alert(getMessage("portal.common.js.processPortal.api.notAvailable", "The Process Portal API is not available."));
        }
      }
    };
  };
} // !InfinityBpm.ProcessPortal