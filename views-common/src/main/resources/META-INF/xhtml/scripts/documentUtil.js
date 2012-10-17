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
var popupWindow;
function openWindow(contextPath) 
{
	parameters = "";
	height1 = getCookie("scrH");
	width1 = getCookie("scrW");
	screenX1 = getCookie("scrX");
	screenY1 = getCookie("scrY");

	if(isIE()){
		parameters = 'left=' + screenX1 + ',top=' + screenY1 + ',width=' + width1  + ',height=' + height1 + ',';
	}else if(isNetscape()){
		parameters = 'screenX=' + screenX1 + ',screenY=' + screenY1 + ',outerWidth=' + width1 + ',outerHeight=' + height1 + ',';
	}else{
		parameters = 'screenX=' + screenX1 + ',screenY=' + screenY1 + ',width=' + width1 + ',height=' + height1 + ',';
	}

	parameters = parameters + 'toolbar=no, location=no, resizable=yes, scrollbars=yes';	
	
	var completeUrl = window.location.protocol + "//" + window.location.host
			+ contextPath;
	
	popupWindow = window.open(completeUrl, 'ippExternalDocumentWindow',
			parameters);
	//parent.popupWindow = popupWindow;
	
	var ippWindow = InfinityBpm.Core.getIppWindow();
	if (ippWindow != null) {
		ippWindow.popupWindow = popupWindow;
	}
}

//called from outside popup
function closeWindow() 
{
	if(!popupWindow)
	{
		var ippWindow = InfinityBpm.Core.getIppWindow();
		if(ippWindow && ippWindow.popupWindow)
		{
			ippWindow.popupWindow.close();
		}
	}else{
		if (false == popupWindow.closed) 
		{
			popupWindow.close();
		} else 
		{
			alert(getMessage("portal.common.js.documentPopout.windowAlreadyClosed", 'Window already closed!'));
		}
	}
}

//called from within popup
function closeThisWindow() {
	popupWindow = self;
	saveWindowProperties();	
	popupWindow.close();
}

function onWindowUnload() {
	// do not execute if user has clicked pop-in button from within popup
	if(!popupWindow){
		popupWindow = self;

		saveWindowProperties();	

		document.getElementById("externalViewer:windowUnloaded").value = "true";
		
		iceSubmitPartial(document.getElementById("externalViewer"), document
				.getElementById("externalViewer:windowUnloaded"));	
	}
}

function setCookie(c_name, value, expiredays) {
	var exdate = new Date();
	exdate.setDate(exdate.getDate() + expiredays);
	myDocument = popupWindow.opener.document;
	myDocument.cookie = c_name + "=" + escape(value)+ ((expiredays == null) ? "" : ";expires=" + exdate.toUTCString());
	}

function getCookie(c_name) {
	if (document.cookie.length > 0) {
		c_start = document.cookie.indexOf(c_name + "=");
		if (c_start != -1) {
			c_start = c_start + c_name.length + 1;
			c_end = document.cookie.indexOf(";", c_start);
			if (c_end == -1)
				c_end = document.cookie.length;
			return unescape(document.cookie.substring(c_start, c_end));
		}
	}
	return "";
}

function saveWindowProperties(){
	setCookie('scrX', getScreenX(), 365);
	setCookie('scrY', getScreenY(), 365);
	setCookie('scrH', getPageHeight(), 365);
	setCookie('scrW', getPageWidth(), 365);
}

function getPageWidth() {
	pageWidth = 600;
	if(window.outerWidth){
		pageWidth = window.outerWidth;
		pageWidth = Math.round(pageWidth + (pageWidth * 0.22) + 2);
	}else if(document.documentElement && document.documentElement.clientWidth){
		pageWidth = document.documentElement.clientWidth;
	}else if(document.body && document.body.clientWidth){
		pageWidth = document.body.clientWidth;
	}
	return pageWidth;
}

function getPageHeight() {
	pageHeight = 500;
	if(window.outerHeight){
		pageHeight = window.outerHeight;
		pageHeight = Math.round(pageHeight + (pageHeight * 0.22) + 2);
	}else if(document.documentElement && document.documentElement.clientHeight){
		pageHeight = document.documentElement.clientHeight;
	}else if(document.body && document.body.clientHeight){
		pageHeight = document.body.clientHeight;
	}
	return pageHeight;
}

function getScreenX(){
	screenPosX = 100;
	if(window.screenX){
		screenPosX = window.screenX;
		screenPosX = Math.round(screenPosX + (screenPosX * 0.22));
	}else if (window.screenLeft) {
		screenPosX = window.screenLeft - 3;
	}
	return screenPosX;
}

function getScreenY(){
	screenPosY = 100;
	if(window.screenY){
		screenPosY = window.screenY;
		screenPosY = Math.round(screenPosY + (screenPosY * 0.22));
	}else if (window.screenTop) {
		screenPosY = window.screenTop - 23;
	}
	return screenPosY;
}

function isIE() {
	if (navigator.appName == "Microsoft Internet Explorer") {
		return true;
	}
	return false;
}

function isNetscape() {
	if (navigator.appName == "Netscape") {
		return true;
	}
	return false;
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