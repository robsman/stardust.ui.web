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
 * @author Omkar.Patil
 */
define(["m_logger"], function(m_logger){
	return {
		init: function(frame, divId, cWidth, cHeight, bImage){
			//Raphael.setWindow(frame);
			canvas = Raphael(divId, cWidth, cHeight).initZoom();
			canvasWidth = cWidth;
			canvasHeight = cHeight;
			bgImage = bImage;
			setBGImage();
			addClickEventHandling(divId);
		},
		
		setWindow : function (frame) {
			Raphael.setWindow(frame);
		},
		
		getCanvas: function() {
			return canvas;
		},
		
		addImage: function(imageURL, width, height) {
			canvas.clear();
			canvas.initZoom();
			setBGImage();
			return canvas.image(imageURL, 0, 0, width, height).initZoom();
		},
		
		drawImageAt: function(imageURL, x, y, width, height) {
			return canvas.image(imageURL, x, y, width, height);
		},
		
		drawRectangle: function(x, y, width, height, attributes) {
			var rect = canvas.rect(x, y, width, height).initZoom();
			jQuery.each(attributes, function (name, value) {
				rect.attr(name, value);
			});
			return rect;
		},
		
		drawCircle: function(x, y, radius, attributes) {
			var circle = canvas.circle(x, y, radius).initZoom();
			jQuery.each(attributes, function (name, value) {
				circle.attr(name, value);
			});
			return circle;
		},
		
		drawTextNode : function (x, y, textContent, fontSize) {
			return canvas.text(x, y, textContent).attr('font-size', parseInt(fontSize)).initZoom();
		},
		
		getNewSet : function () {
			return canvas.set();
		},
		
		setZoomLevel : function(scaleBy) {
			canvas.setZoom(parseFloat(scaleBy));
		},
		
		setCanvasSize : function (width, height) {
			canvasWidth = width;
			canvasHeight = height;
			canvas.setSize(parseInt(width), parseInt(height));
		}
	};
	
	var canvas;
	var canvasWidth;
	var canvasHeight;
	var bgImage;
	
	function addClickEventHandling(divId) {
		jQuery('#' + divId).click(function(e) {
			var x = e.clientX - this.offsetLeft;
			var y = e.clientY - this.offsetTop;
			jQuery(document).trigger('CANVAS_CLICKED', {x:x, y:y});	
		});
	};
	
	
	function setBGImage()
	{
		if (bgImage)
		{
			canvas.image(bgImage, -10, -10, canvasWidth * 11, canvasHeight * 11);
		}	
	}
});