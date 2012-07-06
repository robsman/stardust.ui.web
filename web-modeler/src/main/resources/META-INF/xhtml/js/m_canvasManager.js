/**
 * Low-level drawing functions.
 * 
 * @author Omkar.Patil, Marc.Gille
 */

define(["m_utils"], function(m_utils){
	return {
		init: function(frame, divId, cWidth, cHeight, bImage){
			//Raphael.setWindow(frame);
			canvas = Raphael(divId, cWidth, cHeight);
			canvasWidth = cWidth;
			canvasHeight = cHeight;
			bgImage = bImage;
		},
		
		setWindow : function (frame) {
			Raphael.setWindow(frame);
		},
		
		getCanvas: function() {
			return canvas;
		},
		
		getCanvasWidth: function() {
			return canvasWidth;
		},

		getCanvasHeight: function() {
			return canvasHeight;
		},

		addImage: function(imageURL, width, height) {
			canvas.clear();
			setBGImage();
			return canvas.image(imageURL, 0, 0, width, height);
		},
		
		drawImageAt: function(imageURL, x, y, width, height) {
			return canvas.image(imageURL, x, y, width, height);
		},
		
		drawRectangle: function(x, y, width, height, attributes) {
			var rect = canvas.rect(x, y, width, height);
			jQuery.each(attributes, function (name, value) {
				rect.attr(name, value);
			});

			return rect;
		},
		
		drawPath: function(svgPathString, attributes) {
			var path = canvas.path(svgPathString);
			jQuery.each(attributes, function (name, value) {
				path.attr(name, value);
			});

			return path;
		},

		drawCircle: function(x, y, radius, attributes) {
			var circle = canvas.circle(x, y, radius);
			jQuery.each(attributes, function (name, value) {
				circle.attr(name, value);
			});
			return circle;
		},
		
		drawTextNode : function (x, y, textContent, fontSize) {
			return canvas.text(x, y, textContent).attr('font-size', parseInt(fontSize));
		},
		
		getNewSet : function () {
			return canvas.set();
		},
		
		setViewBox : function(panX, panY, zoomFactor) {
			canvas.setViewBox(panX, panY, canvasWidth * zoomFactor, canvasHeight * zoomFactor);
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

	function setBGImage()
	{
		if (bgImage)
		{
			canvas.image(bgImage, -10, -10, canvasWidth * 11, canvasHeight * 11);
		}	
	}
});