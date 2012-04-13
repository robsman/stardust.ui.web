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
var iDnD = function () {
	var dragContentIFrame;
	var imageToDrag;
	var imgDragCallback;
	var ifDrag = false;
	var transferObject;
	
	var IE = document.all ? true:false;
	//if (!IE) document.captureEvents(Event.MOUSEMOVE)
	
	var resetImageToDrag = function()
	{
		imageToDrag = undefined;
		transferObject = undefined;
		resetDrag();
	}
	
	var hideIframe = function()
	{
		dragContentIFrame.width = "0px";
		dragContentIFrame.height = "0px";
		dragContentIFrame.style.visibility = "hidden";
	}
	
	function initDnD()
	{
		var dragOverlayDiv = document.createElement('div');
		dragOverlayDiv.id = "dragOverlay";
		document.getElementsByTagName('body') [0].appendChild(dragOverlayDiv);
		dragOverlayDiv.innerHTML = '<iframe id="dragContentIFrame" width="0px" height="0px"  style="z-index:10000; overflow : hidden;" src="dndContent.html" frameborder="0"></iframe>';
		dragContentIFrame = document.getElementById('dragContentIFrame');
		dragContentIFrame.style.visibility = "hidden";
	}
	
	document.onmouseup = hideIframe;
		
	return {
		setImageToDrag : function(imgSrc, txt) {
			imageToDrag = imgSrc;
			imgDragCallback(imgSrc, txt);
		},
		
		resetImageToDrag : function()
		{
			resetImageToDrag();
		},
		
		drawIframeAt : function(e, container) {
			 if(e.preventDefault)
			 {
			  	e.preventDefault();
			 }
			var coordinates = getMouseCoordinates(e, container)
			dragContentIFrame.width = "170px";
			dragContentIFrame.height = "50px";
			dragContentIFrame.style.visibility = "visible";
			this.setIframeXY(e, container);
		},
		
		hideIframe : function() {
			//console.log('hide iframe called');
			resetImageToDrag();
			hideIframe();			
		},
		
		setIframeXY : function(e, container) {
			var coordinates = getMouseCoordinates(e, container);
			
			if ((true == ifDrag) && (dragContentIFrame.style.visibility == "visible"))
			{
				dragContentIFrame.style.position = 'absolute';
				dragContentIFrame.style.left = (coordinates.x + 10);
				dragContentIFrame.style.top = (coordinates.y + 10);
			}
			else if (true == ifDrag)
			{
				this.drawIframeAt(e, container);
			}
				
			return true;
		},
		
		setImageDragCallback : function(func)
		{
			imgDragCallback = func;
		},
		
		init : function () {
			initDnD();
		},
		
		setDrag : function() {			
			ifDrag = true;
		},
		
		setTransferObject : function(tObj) {
			transferObject = tObj;
		},
		
		getTransferObject : function(tObj) {
			return transferObject;
		},
		
		getMouseCoordinates : function(e) {
			return getMouseCoordinates(e);
		}
	};
	
	function resetDrag() {
		ifDrag = false;
	}
	
	function getMouseCoordinates(e, container)
	{
		var tempX = 0;
		var tempY = 0;
		if (IE) { // grab the x-y pos.s if browser is IE
			tempX = e.clientX + document.body.scrollLeft;
			tempY = e.clientY + document.body.scrollTop;
		}
		else {  // grab the x-y pos.s if browser is NS
			tempX = e.pageX;
			tempY = e.pageY;
		}

		if (container)
		{
			var cont = document.getElementById(container);
			if (cont)
			{
				var pos = getPos(cont)
				tempX += pos.x;
				tempY += pos.y;
			}
		}
	
		if (tempX < 0){tempX = 0;}
		if (tempY < 0){tempY = 0;}
	
		return {x : tempX, y : tempY};
	}
	
	function getPos(el) {
		for (var lx=0, ly=0;
			 el != null;
			 lx += el.offsetLeft, ly += el.offsetTop, el = el.offsetParent);
		return {x: lx,y: ly};
	}
}();