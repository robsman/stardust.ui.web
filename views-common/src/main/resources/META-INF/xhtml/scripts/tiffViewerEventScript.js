/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/
var docWidth = 0, docHeight = 0;

/**
 * 
 * @param iframeId
 * @param anchorId
 */
function restoreTiffIframe(iframeId, anchorId) {
	var divObj = document.getElementById('tiffViewerIframe');
	var topValue = 0, leftValue = 0;
	while (divObj) {
		leftValue += divObj.offsetLeft;
		topValue += divObj.offsetTop;
		divObj = divObj.offsetParent;
	}
	var windowWidth = document.body.clientWidth;
	var windowHeight = document.body.clientHeight;
	
	docWidth = parseInt((document.body.clientWidth - leftValue)) - 30;
	docHeight = parseInt(docWidth * 1.2)+70;
	
	docHeight -= topValue;
	
	if (document.getElementById('tiffViewerIframe')) {
			document.getElementById('tiffViewerIframe').style.width = docWidth;
			+'px';
			document.getElementById('tiffViewerIframe').style.height = docHeight;
			+'px';

			window.parent.EventHub.events.publish('CANVAS_RESIZED', docWidth,
					docHeight);
		this.parent.window.BridgeUtils.FrameManager.resizeAndReposition(
				iframeId, {
					anchorId : anchorId,
					canvasWidth : docWidth,
					canvasHeight : docHeight,
					width : docWidth,
					height : docHeight
				});
	}
}

/**
 * 
 * @param iframeId
 * @param defaultPath
 */
function activateAndResizeIframe(iframeId, defaultPath) {

	var anchorId = "tiffViewerIframe";
	restoreTiffIframe(iframeId, anchorId);
	defaultPath += "&canvasWidth=" + docWidth + "&canvasHeight=" + docHeight;
	this.parent.window.BridgeUtils.FrameManager.createOrActivate(iframeId,
			defaultPath, {
				canvasWidth : docWidth,
				canvasHeight : docHeight,
				width : docWidth,
				height : docHeight,
				anchorId : anchorId
			});

}