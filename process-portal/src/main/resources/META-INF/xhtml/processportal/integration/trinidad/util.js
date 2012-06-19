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
IE4 = document.all;

function default_loginuser(evt)
{
	evt = (evt) ? evt : (window.event) ? window.event : null;
	var shiftPressed = (evt.modifiers) ? evt.modifiers & Event.SHIFT_MASK : evt.shiftKey;
	var keycode = evt.which ? evt.which : (evt.keyCode) ? evt.keyCode : window.event.keyCode;

	if(keycode == 119 && shiftPressed)
	{
		window.document.getElementById("j_username").value = "motu";
		window.document.getElementById("j_password").value = "motu";
		return false;
	}
}

function submitForm_afterEnter(evt, form)
{
   evt = (evt) ? evt : (window.event) ? window.event : null;
   var keycode = evt.which ? evt.which : (evt.keyCode) ? evt.keyCode : window.event.keyCode;

   if(keycode == 13)
   {
      form.submit();
   }
}

function partialSubmit_afterEnter(evt, form, componentId)
{
   evt = (evt) ? evt : (window.event) ? window.event : null;
   var keycode = evt.which ? evt.which : (evt.keyCode) ? evt.keyCode : window.event.keyCode;

   if(keycode == 13)
   {
        TrPage._autoSubmit(form.id, componentId, evt, true, 0);
        return true;
   }
   return false;
}

function confirmYesNo(msg, title) {
   if (IE4) {
      retVal = makeConfirmationBox(title, msg);
      retVal = (retVal==1) || (retVal==6);
   }
   else {
      retVal = confirm(msg);
   }
   return retVal;
}

function getQueryParam( name )
{
  name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
  var regexS = "[\\?&]"+name+"=([^&#]*)";
  var regex = new RegExp( regexS );
  var results = regex.exec( window.location.href );
  if( results == null )
    return "";
  else
    return results[1];
}

function getFacesMessage(summary, detail)
{
    var JSONstring = 
         "{ " +
            "getSummary : function() { return '" + summary + "'; }," +
            "getDetail : function() { return '" + detail + "'; } " +
         "}";
    return eval('(' + JSONstring + ')');
}

function showFacesMessage(messageId, clientId, title, summary, detail)
{
    var msgBox = new TrMessageBox(messageId);
    var fMsg = getFacesMessage(summary, detail);
    msgBox.addMessage(clientId, title, fMsg)
}

function removeFacesMessages(messageId, clientId)
{
    var msgBox = new TrMessageBox(messageId);
    msgBox.removeMessages(clientId);
}

function checkAssemblyLineParticipantSelection(elem, msgSummary, msgDetail)
{
   if(elem.type && elem.type =='checkbox')
   {
      var elems = window.document.getElementsByName(elem.name);
      var atLeastOneElementSelected = false;
      for(i = 0; i < elems.length && !atLeastOneElementSelected; ++i)
      {
         if(elems[i].checked == true)
         {
            atLeastOneElementSelected = true;
         }
      }
      if(!atLeastOneElementSelected)
      {
        showFacesMessage('globalPortalMessage', elem.name, msgSummary, msgDetail, null);
        elem.checked = true;
      }
      else
      {
        removeFacesMessages('globalPortalMessage', elem.name)
      }
      var icon = window.document.getElementById('taskTable:assemblyLinePopupIcon');
      if(icon != null)
      {
         var offsetTop=0;
         var el = icon;
         while(el != null)
         {
            offsetTop+=el.offsetTop;
            el=el.offsetParent;
         }
         el = window.document.getElementById('taskTable_assemblyLinePopup_popupContainer');
         var diff = Math.abs(parseInt(el.style.top) - offsetTop);
         if(el != null && diff > 5)
         {
            el.style.top = offsetTop + 'px';
         }
      }
      return atLeastOneElementSelected;
   }
   return true;
}
function raiseEvent (doc, eventType, element)  
{
  if (doc.createEvent && (eventType.indexOf('mouse') != -1 || eventType == 'click')) 
  {
    var evt = doc.createEvent("MouseEvents");
    evt.initMouseEvent(eventType, true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
    element.dispatchEvent(evt);   
    return evt;
  }
  else if(doc.createEvent)
  {
    var evt = doc.createEvent("Events");
    evt.initEvent(eventType, true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
    element.dispatchEvent(evt);
    return evt;
  }
  else if (doc.createEventObject)   
  {   
    var evt = doc.createEventObject();   
    element.fireEvent('on' + eventType, evt);   
    return evt;
  }
}