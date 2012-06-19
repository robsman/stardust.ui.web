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
function default_portalCommon_loginuser(evt)
{
	evt = (evt) ? evt : (window.event) ? window.event : "";
	var shiftPressed = (evt.modifiers) ? evt.modifiers & Event.SHIFT_MASK : evt.shiftKey;
	var keycode = evt.which ? evt.which : (evt.keyCode) ? evt.keyCode : window.event.keyCode;

	if(keycode == 119 && shiftPressed)
	{
		window.document.getElementById("loginForm:j_username").value = "motu";
		window.document.getElementById("loginForm:j_password").value = "motu";
		return false;
	}
}

function submitOnEnter(evt){
    if( evt.keyCode == 13 || evt.keyCode == 13 ){
        var inputs = document.getElementsByTagName('input');	
        var submit;			
        for( var i=0;i<=inputs.length;i+=1){
            if( inputs[i].id.indexOf('submit') > -1 ){
                submit = inputs[i];
                submit.click();
                break;
            }
        }
    }
    else
        return true;
}

function onLoginPageLoad() {
    Ice.onSessionExpired('document:body', function() {
    	var ippWin = InfinityBpm.Core.getIppWindow();
    	if (ippWin != null) {
    		ippWin.location.reload();
    	}
    });
}