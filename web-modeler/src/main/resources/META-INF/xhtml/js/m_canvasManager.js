/**
 * Low-level drawing functions.
 *
 * @author Omkar.Patil, Marc.Gille
 */

define(["bpm-modeler/js/m_utils"], function(m_utils){
	return {
		initialize: function(divId, cWidth, cHeight, bImage){
			return new CanvasManager(divId, cWidth, cHeight, bImage);
		}
	};
	
	function CanvasManager(divId, cWidth, cHeight, bImage) {
		this.canvas = new Raphael(divId, cWidth, cHeight);
		this.canvas.parentDivId = divId;
		var canvasWidth = cWidth;
		var canvasHeight = cHeight;
		var bgImage = bImage;
		
		// TODO delete
		this.aTempId = Math.floor(Math.random() * 1000);
		
		// TODO Stille needed?
		CanvasManager.prototype.setWindow = function (frame) {
			Raphael.setWindow(frame);
		};

		CanvasManager.prototype.getCanvas = function() {
			return this.canvas;
		};

		CanvasManager.prototype.getCanvasWidth = function() {
			return canvasWidth;
		};

		CanvasManager.prototype.getCanvasHeight = function() {
			return canvasHeight;
		};

		CanvasManager.prototype.addImage = function(imageURL, width, height) {
			this.canvas.clear();
			this.setBGImage();
			return this.canvas.image(imageURL, 0, 0, width, height);
		};

		CanvasManager.prototype.drawImageAt = function(imageURL, x, y, width, height) {
			return this.canvas.image(imageURL, x, y, width, height);
		};

		CanvasManager.prototype.drawRectangle = function(x, y, width, height, attributes) {
			var rect = this.canvas.rect(x, y, width, height);
			jQuery.each(attributes, function (name, value) {
				rect.attr(name, value);
			});

			return rect;
		};

		CanvasManager.prototype.drawPath = function(svgPathString, attributes) {
			var path = this.canvas.path(svgPathString);
			jQuery.each(attributes, function (name, value) {
				path.attr(name, value);
			});

			return path;
		};

		CanvasManager.prototype.drawCircle = function(x, y, radius, attributes) {
			var circle = this.canvas.circle(x, y, radius);
			jQuery.each(attributes, function (name, value) {
				circle.attr(name, value);
			});
			return circle;
		};

		CanvasManager.prototype.drawTextNode = function (x, y, textContent, fontSize) {
			return this.canvas.text(x, y, textContent).attr('font-size', parseInt(fontSize));
		};

		CanvasManager.prototype.getNewSet = function () {
			return this.canvas.set();
		};

		CanvasManager.prototype.setViewBox = function(panX, panY, zoomFactor) {
			this.canvas.setViewBox(panX, panY, canvasWidth * zoomFactor, canvasHeight * zoomFactor);
		};

		CanvasManager.prototype.setCanvasSize = function (width, height) {
			canvasWidth = width * 1.25;
			canvasHeight = height * 1.25;
			this.canvas.setSize(parseInt(canvasWidth), parseInt(canvasHeight));
		};
		
		CanvasManager.prototype.setBGImage = function()
		{
			if (bgImage)
			{
				this.canvas.image(bgImage, -10, -10, canvasWidth * 11, canvasHeight * 11);
			}
		}
	}
});