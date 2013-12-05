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
/**
 * 
 * @param divId
 */
function resizeMessageDialog(divId) {
	var scrollX = 0;
	var scrollY = 0;
	var mainWin = window;
	if (navigator.appName == 'Netscape') {
		scrollX = mainWin.pageXOffset;
		scrollY = mainWin.pageYOffset;
	} else if (navigator.appName == 'Microsoft Internet Explorer') {
		scrollX = mainWin.document.documentElement.scrollLeft;
		scrollY = mainWin.document.documentElement.scrollTop;
	}
	var windowSize = getBrowserDimensions(mainWin);
	var popupDivs = getElementsWithIDLike('div', divId, document);
	if (popupDivs && (popupDivs.length > 0)) {
		for ( var i = 0; i < popupDivs.length; i++) {
			try {
				var popupDiv = popupDivs[i];
				var widthOffset = (popupDiv.offsetWidth < windowSize.width) ? popupDiv.offsetWidth
						: 0;
				var heightOffset = (popupDiv.offsetHeight < windowSize.height) ? popupDiv.offsetHeight
						: 0;
				popupDiv.style.left = (((windowSize.width - widthOffset) / 2) + scrollX)
						+ 'px';
				popupDiv.style.top = (((windowSize.height - heightOffset) / 2) + scrollY)
						+ 'px';
			} catch (e) {
				popupDiv.style.left = (scrollX + 200) + 'px';
				popupDiv.style.top = (scrollY + 200) + 'px';
			}
		}
	}
}

/**
 * 
 * @param tagName
 * @param elementId
 * @param doc
 * @returns
 */
function getElementsWithIDLike(tagName, elementId, doc) {
	var allElems;
	if (doc) {
		allElems = doc.getElementsByTagName(tagName);
	} else {
		allElems = document.getElementsByTagName(tagName);
	}
	var selectedElems = [];
	if (allElems) {
		for ( var i = 0; i < allElems.length; i++) {
			if (allElems[i].id && (allElems[i].id.indexOf(elementId) >= 0)) {
				selectedElems.push(allElems[i]);
			}
		}
	}

	return selectedElems;
}

/**
 * 
 * @returns {___anonymous2479_2521}
 */
function getBrowserDimensions(mainWin) {
	var winW = screen.availWidth ? screen.availWidth : screen.width;
	var winH = screen.availWidth ? screen.availHeight : screen.height;
	var mainDoc = mainWin.document;
	if (mainDoc.body && mainDoc.body.offsetWidth) {
		winW = mainDoc.body.offsetWidth;
		winH = mainDoc.body.offsetHeight;
	}
	if (mainDoc.compatMode == 'CSS1Compat' && mainDoc.documentElement
			&& mainDoc.documentElement.offsetWidth) {
		winW = mainDoc.documentElement.offsetWidth;
		winH = mainDoc.documentElement.offsetHeight;
	}
	if (mainWin.innerWidth && mainWin.innerHeight) {
		winW = mainWin.innerWidth;
		winH = mainWin.innerHeight;
	}

	return {
		'width' : winW,
		'height' : winH
	};
}

