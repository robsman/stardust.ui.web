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
 * @author Shrikant.Gangal
 */
define([ "m_pharmacyToolbarController", "m_canvasManager", "m_communicationController", "m_constants", "m_logger", "m_pageController", "m_commandsController" ], function(m_pharmacyToolbarController, m_canvasManager, m_communicationController, m_constants, m_logger, m_pageController, m_commandsController) {
	var documentId;
	var isDocEditable;
	var currentEditableAnnotation;
	var canvasWidth;
	var canvasHeight;
	var imageWidth;
	var imageHeight;
	var currentZoomLevel = 1;
	var currentRotationFactor = 0;
	var currentlySelectedAnnotation;
	var allAnnotationsList = [];
	var currentImage;
	var zoomTranslationFactorX = 0;
	var zoomTranslationFactorY = 0;
	var annotationHighlighter = undefined;
	var _toolActionsMap;
	var _annotationMovedFlag = false; 
	var bgImage = "../../images/annotations/gray.jpg";
	var imageViewerConfigOptions;
	var sharedViewerState = {};
	var annotationWithContextMenu;
	
	/* The following two variables capture the current deltas (dx, dy) of an image. This is an aggregation of 
	 * all the panning that takes place with the image. This is needed to calculate the exact position on canvas
	 * of an annotation using it's image relative position. */
	var pannedByDXAggregated = 0;
	var pannedByDYAggregated = 0;
	
		return {
			init : function(frame, divId, docId, numOfPages, isEditable, cWidth, cHeight, toolbarDiv) {
				canvasWidth = cWidth;
				canvasHeight = cHeight;
				documentId = docId;
				isDocEditable = isEditable;
				initImageViewerConfigOptions();
				initializeToolActionsMap();
				m_canvasManager.init(frame, divId, canvasWidth, canvasHeight, bgImage);
				m_pageController.init(docId, 1);
				m_pharmacyToolbarController.init(toolbarDiv, numOfPages, isDocEditable, imageViewerConfigOptions);
				m_commandsController.init(true, false);
				setupEventHandling();
				initializePage();			
				internationalizeAllElements();
			}
		};
		
		function initializeToolActionsMap()
		{
			_toolActionsMap = {
				highlighterToolAction : function(args)
				{
					var drawableOptions = getDrawableOptions(args.x, args.y);
					if (drawableOptions.isDrawable)
					{
						args.action = 'Create';
						args.width = (drawableOptions.availableWidth >= m_constants.HIGHLIGHTER_DEFAULT_WIDTH) ?  m_constants.HIGHLIGHTER_DEFAULT_WIDTH : drawableOptions.availableWidth;
						args.height = (drawableOptions.availableHeight >= m_constants.HIGHLIGHTER_DEFAULT_HEIGHT) ?  m_constants.HIGHLIGHTER_DEFAULT_HEIGHT : drawableOptions.availableWidth;
						
						drawHighlighter(args);
					}
				},
				
				stickyNoteToolAction : function(args)
				{
					var drawableOptions = getDrawableOptions(args.x, args.y);
					if (drawableOptions.isDrawable)
					{
						args.action = 'Create';
						args.width = (drawableOptions.availableWidth >= m_constants.STICKY_NOTE_DEFAULT_WIDTH) ?  m_constants.STICKY_NOTE_DEFAULT_WIDTH : drawableOptions.availableWidth;
						args.height = (drawableOptions.availableHeight >= m_constants.STICKY_NOTE_DEFAULT_HEIGHT) ?  m_constants.STICKY_NOTE_DEFAULT_HEIGHT : drawableOptions.availableWidth;
						args.text = InfinityBPMI18N.graphicscommon.getProperty('tiffViewer.annotation.defaultText');
						args.completeText = InfinityBPMI18N.graphicscommon.getProperty('tiffViewer.annotation.defaultText');
						
						drawStickyNote(args);
					}
				},
				
				stampToolAction : function(args)
				{
					var drawableOptions = getDrawableOptions(args.x, args.y);
					if (drawableOptions.isDrawable)
					{
						args.action = 'Create';
						args.stampDocId = m_pharmacyToolbarController.getSelectedStampDocId();
						args.imageURL = m_pharmacyToolbarController.getSelectedStamp();
						if (args.imageURL)
						{
							args.width = (drawableOptions.availableWidth >= m_constants.STAMP_DEFAULT_WIDTH) ?  m_constants.STAMP_DEFAULT_WIDTH : drawableOptions.availableWidth;
							args.height = (drawableOptions.availableHeight >= m_constants.STAMP_DEFAULT_HEIGHT) ?  m_constants.STAMP_DEFAULT_HEIGHT : drawableOptions.availableWidth;
							drawStamp(args);
						}
					}
				},
				
				fitToWindowToolAction : function(args)
				{
					fitTo(m_pharmacyToolbarController.getFitToSize());
				},
				
				fitToHeightToolAction : function(args)
				{
					fitTo(m_pharmacyToolbarController.getFitToSize());
				},
				
				fitToWidthToolAction : function(args)
				{
					fitTo(m_pharmacyToolbarController.getFitToSize());
				},
				
				zoomOutToolAction : function(args)
				{
					effectZoomOut();
				},
				
				zoomInToolAction : function(args)
				{
					effectZoomIn();
				},
				
				rotateRightToolAction : function(args)
				{
					effectRotation(90);
					m_commandsController.handleCommand(createRotateCommandObject(currentRotationFactor));
				},
				
				rotateLeftToolAction : function(args)
				{
					effectRotation(-90);
					m_commandsController.handleCommand(createRotateCommandObject(currentRotationFactor));
				},
				
				showHideAnnotationsToolAction : function(args)
				{
					if (args.show)
					{
						jQuery.each(allAnnotationsList, function(id, value) {
							value.fn.showAnnotation();
						});
					}
					else
					{
						jQuery.each(allAnnotationsList, function(index, value) {
							value.fn.hideAnnotation();
						});
					}
				},
				
				grayScaleToolAction : function(args) {
					doGrayScale(args.dsatStatus);
				},
				
				invertToolAction : function(args) {
					doInvert(args.invertStatus);					
				}
			};
		}
		
		function setupEventHandling() {
			//Subscribe to canvas resize events. These are triggered when the containing view is resized.
			window.parent.EventHub.events.subscribe("CANVAS_RESIZED", setCanvasSize);
			window.parent.EventHub.events.subscribe("VIEW_CLOSING", cleanCache);
			window.parent.EventHub.events.subscribe("RE_INITIALIZE_VIEWER", initializePage);
			jQuery(document).bind(m_constants.CANVAS_CLICKED_EVENT, function(event, data) {
				deHighlightAnnotation();
				hideExistingAnnotContextMenu();
				currentlySelectedAnnotation = null; //Reset any previous selection of annotation.

				var divOverflowXCoordinateOffset = 5;
				var divOverflowYCoordinateOffset = 5;
				if (m_pharmacyToolbarController.getCurrentSelection())
				{
					if (typeof _toolActionsMap[m_pharmacyToolbarController.getCurrentSelection() + "ToolAction"] == 'function')
					{
						_toolActionsMap[m_pharmacyToolbarController.getCurrentSelection() + "ToolAction"]({x : (data.x - divOverflowXCoordinateOffset), y : (data.y - divOverflowYCoordinateOffset)});
					}
				}
				
				saveAndExitEditMode();
			});
			
			jQuery(document).bind("TOOL_CLICKED_EVENT", function(event, data) {
				if (typeof _toolActionsMap[data.toolId + "ToolAction"] == 'function')
				{
					_toolActionsMap[data.toolId + "ToolAction"](data);
				}
			});
			
			jQuery("#editable").editable(function(value, settings) {
				jQuery(this).hide();
				currentEditableAnnotation.text.attr({text : formatText(value, getNumberOfRows(currentEditableAnnotation), getNumberOfCharactersPerRow(currentEditableAnnotation))});
				positionTextVertically(currentEditableAnnotation);
				currentEditableAnnotation.text.customProps.completetext = value;
				//raise annotation modified event.
				currentEditableAnnotation.customProps.commandObj.action = 'Modify';
				currentEditableAnnotation.customProps.commandObj.props.text = currentEditableAnnotation.text.attr('text');
				currentEditableAnnotation.customProps.commandObj.props.completetext = currentEditableAnnotation.text.customProps.completetext;
				
				// Update the 'annotation modified' information
				updateAnnotationModifiedInfo(currentEditableAnnotation);
				
				m_commandsController.handleCommand(currentEditableAnnotation.customProps.commandObj);
				currentEditableAnnotation.fn.showAnnotation();
				currentEditableAnnotation = null;
				return(value);
				}, {
					type    : 'textarea',
					width : function() {return getCurrentEditablesWidth();},
					height : function() {return getCurrentEditablesHeight();},
					event : "dblclick",
					onblur : "submit"
			});
			
			jQuery(document).bind(m_constants.PAGE_CHANGE_EVENT, function() {
				deHighlightAnnotation();
				hideExistingAnnotContextMenu();
				currentlySelectedAnnotation = null;
				var imageDimensions = getPageDimensions(documentId, m_pageController.getCurrentPageIndex());
				imageWidth = imageDimensions.width;
				imageHeight = imageDimensions.height;
				currentRotationFactor = 0;
				var image = m_canvasManager.addImage(require('m_urlUtils').getContextName() + m_constants.TIFF_RENDERER_SERVLET_PATH + "?docId=" + documentId + "&pageNo=" + m_pageController.getCurrentPageIndex() + "&randomPostfix=" + m_pageController.getURLPostFix(), imageWidth, imageHeight);
				sharedViewerState.pageNo = m_pageController.getCurrentPageIndex();
				updateSharedViewerState();
				image.customProps = {};
				currentImage = image;
				enablePanning(image);
				
				/* Set the aggregated panning factors to 0 when a new
				 * image is loaded. */
				pannedByDXAggregated = 0;
				pannedByDYAggregated = 0;
				
				allAnnotationsList = [];
				applyInvertAndGrayScale();
				zoomTranslationFactorX = 0;
				zoomTranslationFactorY = 0;
					
				/* If fitToSize is called to re-calculate the zoom factor.
				 * This is needed as two pages with different dimensions will have
				 * different zoom factor for a fit to size event.
				 * If variable 'fitToSize' is set to 'none' then the zoom factor from previous page
				 * will be retained.
				 */
				initializeZoom();
				
				applyAnnotations();
			});
			
			jQuery(document).bind('ANNOTATIONS_APPLIED_EVENT', function() {				
				
				_toolActionsMap.showHideAnnotationsToolAction({show : m_pharmacyToolbarController.getAnnotationsShowHideStatus()});
			})
			
			jQuery(document).bind('ANNOTATION_MOVED', function(event, data) {
				m_communicationController.postData({url : data.url}, data.jsonData, new function() {
					return {
						success : function() {},				
						failure : function() {}
					}
				});
			});
			
			jQuery(document).bind("ANNOTATION_ADDED", function(event, data) {
				m_communicationController.postData({url : data.url}, data.jsonData, new function() {
					return {
						success : function() {},				
						failure : function() {}
					}
				});
				m_pharmacyToolbarController.resetCurrentSelection();
			});
			
			jQuery(document).bind("ROTATION_APPLIED", function(event, data) {
				m_communicationController.postData({url : data.url}, data.jsonData, new function() {
					return {
						success : function() {},
						failure : function() {}
					}
				});
			});
			
			jQuery(document).bind(m_constants.ZOOM_LEVEL_CHANGE_EVENT, function(event, data) {
				if (data.val != NaN) {
					currentZoomLevel = parseFloat(data.value) / 100;
					m_pharmacyToolbarController.resetFitToSize();
					effectZoom();
				}
			});
			
			jQuery(document).delKeydown(function() {
				  if (currentlySelectedAnnotation != null) {
					  deleteAnnotation(currentlySelectedAnnotation);
				  }
			});
			
			jQuery(document).rightArrowKeydown(function() {
				  if (currentlySelectedAnnotation != null) {
					  currentlySelectedAnnotation.fn.moveStart();
					  currentlySelectedAnnotation.fn.moveAnnotation(1, 0);
					  currentlySelectedAnnotation.fn.moveStop();
					  highlightAnnotation(currentlySelectedAnnotation);
				  }
			});
			
			jQuery(document).leftArrowKeydown(function() {
				  if (currentlySelectedAnnotation != null) {
					  currentlySelectedAnnotation.fn.moveStart();
					  currentlySelectedAnnotation.fn.moveAnnotation(-1, 0);
					  currentlySelectedAnnotation.fn.moveStop();
					  highlightAnnotation(currentlySelectedAnnotation);
				  }
			});
			
			jQuery(document).upArrowKeydown(function() {
				  if (currentlySelectedAnnotation != null) {
					  currentlySelectedAnnotation.fn.moveStart();
					  currentlySelectedAnnotation.fn.moveAnnotation(0, -1);
					  currentlySelectedAnnotation.fn.moveStop();
					  highlightAnnotation(currentlySelectedAnnotation);
				  }
			});
			
			jQuery(document).downArrowKeydown(function() {
				  if (currentlySelectedAnnotation != null) {
					  currentlySelectedAnnotation.fn.moveStart();
					  currentlySelectedAnnotation.fn.moveAnnotation(0, 1);
					  currentlySelectedAnnotation.fn.moveStop();
					  highlightAnnotation(currentlySelectedAnnotation);
				  }
			});
			
			function extractDelta(e) {
			    if (e.wheelDelta) {
			        return e.wheelDelta;
			    }

			    if (e.originalEvent.detail) {
			        return e.originalEvent.detail * -1;
			    }

			    if (e.originalEvent && e.originalEvent.wheelDelta) {
			        return e.originalEvent.wheelDelta;
			    }
			}
			
			// Bind to mouse wheel event.
			jQuery('#canvas').bind('mousewheel', function(event, delta) {
				if(delta == 0){
					// TODO : remove when jquery.mousewheel.js is upgraded
					// to 3.1.4 or higher
					delta= extractDelta(event);	
				}
				
			    if (parseInt(delta) > 0)
			    {
			    	effectZoomIn();
			    	event.preventDefault();
			    }
			    else
			    {
			    	effectZoomOut();
			    	event.preventDefault();
			    }
			});
			
			jQuery('#slider').slider({
				min : 10,
				max : 300,
				animate : true,
				slide : function(event, ui) {
							jQuery(document).trigger(m_constants.ZOOM_LEVEL_CHANGE_EVENT, {value : ui.value});
						}
			}).slider('value', 100);
		};
		
		/* 
		 * This is a workaround for FF
		 * Ideally an editable element should exit edit mode on blur but this is not working in FF.
		 * Hence invoking the submit function on form element in the event when we want to exit edit mode.
		 * */
		function saveAndExitEditMode()
		{
			if (currentEditableAnnotation)
			{
				jQuery("form", "#editable").submit();
			}		
		}
		
		function moveAnnotationTo(annotation, x, y)
		{
			moveByX = x - annotation.baseElement.attrs.x;
			moveByY = y - annotation.baseElement.attrs.y;
			
			annotation.baseElement.translate(moveByX, moveByY);
			annotation.stretchHandle.translate(moveByX, moveByY);
			if (annotation.text)
			{
				/* TODO - investigate and get rid of try /catch.
				 * This try catch is to catch a problem with FF.
				 * whenever a document is saved and the page is re-drawn , in FF, it throws the following error here.
				 * Ignoring this erro does no harm but his this will need to be looked into in detail.
				 * Error - "doc.defaultView.getComputedStyle(node.firstChild, E) is null" */
				try {
					annotation.text.attr('x', parseInt(parseInt(annotation.baseElement.attrs.x) + (parseInt(annotation.baseElement.attrs.width) / 2)));
					annotation.text.attr('y', parseInt(parseInt(annotation.baseElement.attrs.y) + (parseInt(annotation.baseElement.attrs.height) / 2)));
				}catch(e) {
				}
			}
		}
		
		function getDrawableOptions(x, y)
		{
			var boundary = getRotationQualifiedImageBoundary();
			if (x >= boundary.minX
					&& x < (boundary.maxX - 20)
					&& y >= boundary.minY
					&& y < (boundary.maxY - 20))
			{
				return {
					isDrawable : true,
					availableWidth : boundary.maxX - x,
					availableHeight : boundary.maxY - y
				};
			}
			
			return {
				isDrawable : false,
				availableWidth : 0,
				availableHeight : 0
			};
		}
		
		function getRotationQualifiedImageBoundaryForAnnotation(annotation)
		{
			var boundary = getRotationQualifiedImageBoundary();
			var annotOrientation = convertDegreesToRotationFactor(getAnnotationOrientation(annotation));
			switch(annotOrientation)
			{
				case 1:
					boundary.minX += annotation.baseElement.attrs.height;
					boundary.maxY -= annotation.baseElement.attrs.width;
					break;
				case 2:
					boundary.minX += annotation.baseElement.attrs.width;
					boundary.minY += annotation.baseElement.attrs.height;
					break;
				case 3:
					boundary.maxX -= annotation.baseElement.attrs.height;
					boundary.minY += annotation.baseElement.attrs.width;
					break;
				default:
					boundary.maxX -= annotation.baseElement.attrs.width;
					boundary.maxY -= annotation.baseElement.attrs.height;					
			}
			
			return boundary;
		}
		
		function getRotationQualifiedMaxStretchLimits(annotation)
		{
			var deltaMax = {};
			var boundary = getRotationQualifiedImageBoundary();
			var annotOrientation = convertDegreesToRotationFactor(getAnnotationOrientation(annotation));
			switch(annotOrientation)
			{
				case 1:
					deltaMax.x = boundary.maxY - (annotation.baseElement.attrs.y + annotation.baseElement.attrs.width);
					deltaMax.y = (annotation.baseElement.attrs.x - annotation.baseElement.attrs.height) - boundary.minX;
					break;
				case 2:
					deltaMax.x = (annotation.baseElement.attrs.x - annotation.baseElement.attrs.width) - boundary.minX;
					deltaMax.y = (annotation.baseElement.attrs.y - annotation.baseElement.attrs.height) - boundary.minY;
					break;
				case 3:
					deltaMax.x = (annotation.baseElement.attrs.y - annotation.baseElement.attrs.width) - boundary.minY;
					deltaMax.y = boundary.maxX - (annotation.baseElement.attrs.x + annotation.baseElement.attrs.height);
					break;
				default:
					deltaMax.x = boundary.maxX - (annotation.baseElement.attrs.x + annotation.baseElement.attrs.width);
					deltaMax.y = boundary.maxY - (annotation.baseElement.attrs.y + annotation.baseElement.attrs.height);
			}
			
			return deltaMax;
		}
		
		function getRotationQualifiedImageBoundary()
		{
			var boundary = {};
			switch(getCurrentImageOrientation())
			{			
				case 1:
					var coord = getRotationQualifiedCoordinates(currentImage.attrs.x, currentImage.attrs.y);
					boundary.minX = coord.x - currentImage.attrs.height;
					boundary.maxX = coord.x;
					boundary.minY = coord.y;
					boundary.maxY = coord.y + currentImage.attrs.width;
					break;
				case 2:
					var coord = getRotationQualifiedCoordinates(currentImage.attrs.x, currentImage.attrs.y);
					boundary.minX = coord.x - currentImage.attrs.width;
					boundary.maxX = coord.x;
					boundary.minY = coord.y - currentImage.attrs.height;
					boundary.maxY = coord.y;
					break;
				case 3:
					var coord = getRotationQualifiedCoordinates(currentImage.attrs.x, currentImage.attrs.y);
					boundary.minX = coord.x;
					boundary.maxX = coord.x + currentImage.attrs.height;
					boundary.minY = coord.y - currentImage.attrs.width;
					boundary.maxY = coord.y;
					break;
				default:
					boundary.minX = currentImage.attrs.x;
					boundary.maxX = currentImage.attrs.x + currentImage.attrs.width;
					boundary.minY = currentImage.attrs.y;
					boundary.maxY = currentImage.attrs.y + currentImage.attrs.height;
			}
			
			return boundary;
		}
		
		function deleteAnnotation(annotation)
		{
			annotation.fn.remove();
			annotation.customProps.commandObj.action = 'Delete';
			m_commandsController.handleCommand(currentlySelectedAnnotation.customProps.commandObj);
			currentlySelectedAnnotation = null;
		}
		
		function setCanvasSize(width, height)
		{
			m_canvasManager.setCanvasSize(width, height);
			canvasWidth = parseInt(width);
			canvasHeight = parseInt(height);
			jQuery(document).trigger(m_constants.PAGE_CHANGE_EVENT);
		}
		
		function cleanCache(docId)
		{
			if (documentId == docId)
			{
				m_communicationController.postData({url : getURL() + "/cleanCache"}, {}, new function() {
					return {
						success : function() {},				
						failure : function() {}
					}
				});				
			}				
		}
		
		function getRotationUnQualifiedCoordinates(x, y)
		{
			var coordinates = {};
			coordinates.x = x;
			coordinates.y = y;
			if (getCurrentImageOrientation() != 0)
			{
				var canvasCentreX = canvasWidth / 2;
				var canvasCentreY = canvasHeight / 2;				
				switch(getCurrentImageOrientation())
				{						
					case 1:
						coordinates.x = canvasCentreX - (canvasCentreY - y);
						coordinates.y = canvasCentreY - (x - canvasCentreX);
						break;
					case 2:
						coordinates.x = canvasCentreX + (canvasCentreX - x);
						coordinates.y = canvasCentreY + (canvasCentreY - y);
						break;
					case 3:
						coordinates.x = canvasCentreX - (y - canvasCentreY);
						coordinates.y = canvasCentreY + (x - canvasCentreX);
						break;
				}
			}
			
			return coordinates;
		}
		
		function getRotationQualifiedCoordinates(x, y)
		{
			var coordinates = {};
			coordinates.x = x;
			coordinates.y = y;
			coordinates.rotFactor = 0;
			if (getCurrentImageOrientation() != 0)
			{
				var canvasCentreX = canvasWidth / 2;
				var canvasCentreY = canvasHeight / 2;				
				switch(getCurrentImageOrientation())
				{						
					case 1:
						coordinates.x = canvasCentreX + (canvasCentreY - y);
						coordinates.y = canvasCentreY - (canvasCentreX - x);
						break;
					case 2:
						coordinates.x = canvasCentreX + (canvasCentreX - x);
						coordinates.y = canvasCentreY + (canvasCentreY - y);
						break;
					case 3:
						coordinates.x = canvasCentreX - (canvasCentreY - y);
						coordinates.y = canvasCentreY + (canvasCentreX - x);
						break;
				}
			}
			
			return coordinates;
		}
		
		/*
		 * There is a difference between this method and getRotationQualifiedCoordinates.
		 * This methods calculates the coordinated by changing the x, y coordinate frame as per the rotation
		 * while the getRotationQualifiedCoordinates method just determines where the given (x, y) coordinate
		 * will lie after rotation. (Confusing!)
		 */
		function getRotationQualifiedImageCoordinates(x, y)
		{
			var coordinates = {};
			coordinates.x = x;
			coordinates.y = y;
			coordinates.rotFactor = 0;
			if (getCurrentImageOrientation() != 0)
			{
				var canvasCentreX = canvasWidth / 2;
				var canvasCentreY = canvasHeight / 2;				
				switch(getCurrentImageOrientation())
				{						
					case 1:
						coordinates.y = canvasCentreX + (canvasCentreY - y);
						coordinates.x = canvasCentreY - (canvasCentreX - x);
						break;
					case 2:
						coordinates.x = canvasCentreX + (canvasCentreX - x);
						coordinates.y = canvasCentreY + (canvasCentreY - y);
						break;
					case 3:
						coordinates.y = canvasCentreX - (canvasCentreY - y);
						coordinates.x = canvasCentreY + (canvasCentreX - x);
						break;
				}
			}
			
			return coordinates;
		}
		
		function getCurrentEditablesWidth()
		{
			if (currentEditableAnnotation)
			{
				return currentEditableAnnotation.baseElement.attr('width');
			}
			else
			{
				return 50;
			}
		}
		
		function getCurrentEditablesHeight()
		{
			if (currentEditableAnnotation)
			{
				return currentEditableAnnotation.baseElement.attr('height');
			}
			else
			{
				return 50;
			}
		}
		
		function fitTo(fitToWhat)
		{
			var rotQualifiedDimensions = getRotationQualifiedImageDimensions();
			if ('width' == fitToWhat)
			{
				currentZoomLevel = canvasWidth / rotQualifiedDimensions.imageWidth;
			}
			else if ('height' == fitToWhat)
			{
				currentZoomLevel = canvasHeight / rotQualifiedDimensions.imageHeight;
			}
			else if ('window' == fitToWhat)
			{
				var fitToWidthZoom = canvasWidth / rotQualifiedDimensions.imageWidth;
				var fitToHeightZoom = canvasHeight / rotQualifiedDimensions.imageHeight;
				
				if (fitToWidthZoom <= fitToHeightZoom)
				{
					currentZoomLevel = fitToWidthZoom;
				}
				else
				{
					currentZoomLevel = fitToHeightZoom;
				}
			}
			effectZoom();
		}
		
		function getRotationQualifiedImageDimensions()
		{
			
			switch(getCurrentImageOrientation())
			{				
				case 0:
				case 2:
					return {'imageWidth' : imageWidth, 'imageHeight' : imageHeight};
					break;
				case 1:
				case 3:
					return {'imageWidth' : imageHeight, 'imageHeight' : imageWidth};
					break;
			}
		}
		
		function removeFromAllAnnotationsList(element)
		{			
			jQuery.each(allAnnotationsList, function(id, value)
			{
				if (value != null && value.customProps.id == element.customProps.id)
				{
					allAnnotationsList.splice(id, 1);
				}
			});
		}
		
		function doGrayScale(desaturateStatus)
		{
			if (desaturateStatus == 0)
			{
				currentImage.desaturate(1);
			}
			else
			{
				currentImage.desaturate(0);
			}
		}
			
		function doInvert(invertStatus)
		{	
			if (invertStatus == 0)
			{
				currentImage.invert(1);
			}
			else
			{
				currentImage.invert(0);
			}
		}
		
		function applyInvertAndGrayScale()
		{
			var desaturateStatus = m_pharmacyToolbarController.getDsatStatus();
			var invertStatus = m_pharmacyToolbarController.getInvertStatus();
			if (desaturateStatus == 0)
			{
				doGrayScale(desaturateStatus);
			}
			else if (invertStatus == 0)
			{
				doInvert(invertStatus);
			}
		}
		
		/* The following code handles panning of a variable sized image (size will change with zoom) on 
		 * a fixed sized canvas.
		 * Image panning is restricted at the boundaries of the canvas.*/
		function enablePanning(image) {
			var imageCoordinates;
			var boundary;
			var start = function() {
				// storing original coordinates
				this.x = this.attr("x");
				this.y = this.attr("y");
				this.customProps.startX = this.attr("x");
				this.customProps.startY = this.attr("y");
				for ( var i = 0; i < allAnnotationsList.length; i++) {
					allAnnotationsList[i].fn.moveStart();
				}
				imageCoordinates = getRotationQualifiedImageCoordinates(image.attr("x"), image.attr("y"));
				boundary = getRotationQualifiedCanvasBoundary();
			}, move = function(origDx, origDy) {
				var delta = getRotationQualifiedDeltas(origDx, origDy);
				var dx = delta.x;
				var dy = delta.y;
				switch(getCurrentImageOrientation())
				{
					case 0:
						if (!((dy < 0 && (imageCoordinates.y + dy) < boundary.minY) 
								|| ((dy > 0) && (imageCoordinates.y + dy) > boundary.maxY)))
						{
							this.attr('y', (this.y + dy));
							moveAllAnnotationsBy(0, origDy);
						}
						if (!((dx < 0 && (imageCoordinates.x + dx) < boundary.minX) 
								|| ((dx > 0) && (imageCoordinates.x + dx) > boundary.maxX)))
						{
							this.attr('x', (this.x + dx));
							moveAllAnnotationsBy(origDx, 0);
						}
						break;
					case 1:
						if (!((dy < 0 && (imageCoordinates.y - dy) > boundary.minY) 
								|| ((dy > 0) && (imageCoordinates.y - dy) < boundary.maxY)))
						{
							this.attr('y', (this.y + dy));
							moveAllAnnotationsBy(origDx, 0);
						}
						if (!((dx < 0 && (imageCoordinates.x + dx) < boundary.minX) 
								|| ((dx > 0) && (imageCoordinates.x + dx) > boundary.maxX)))
						{
							this.attr('x', (this.x + dx));
							moveAllAnnotationsBy(0, origDy);
						}
						break;
					case 2:
						if (!((dy < 0 && (imageCoordinates.y - dy) > boundary.minY) 
								|| ((dy > 0) && (imageCoordinates.y - dy) < boundary.maxY)))
						{
							this.attr('y', (this.y + dy));
							moveAllAnnotationsBy(0, origDy);
						}
						if (!((dx < 0 && (imageCoordinates.x - dx) > boundary.minX) 
								|| ((dx > 0) && (imageCoordinates.x - dx) < boundary.maxX)))
						{
							this.attr('x', (this.x + dx));
							moveAllAnnotationsBy(origDx, 0);
						}
						break;
					case 3:
						if (!((dy < 0 && (imageCoordinates.y + dy) < boundary.minY) 
								|| ((dy > 0) && (imageCoordinates.y + dy) > boundary.maxY)))
						{
							this.attr('y', (this.y + dy));
							moveAllAnnotationsBy(origDx, 0);
						}
						if (!((dx < 0 && (imageCoordinates.x - dx) > boundary.minX) 
								|| ((dx > 0) && (imageCoordinates.x - dx) < boundary.maxX)))
						{
							this.attr('x', (this.x + dx));
							moveAllAnnotationsBy(0, origDy);
						}
						break;
				}
			}, up = function() {
				/* X and Y delatas for current panning activity. */
				var pannedByDX = (this.attr("x") - this.customProps.startX) / currentZoomLevel;
				var pannedByDY = (this.attr("y") - this.customProps.startY) / currentZoomLevel;
				
				/* Update the aggregate panning dx, dy. */
				pannedByDXAggregated += pannedByDX;
				pannedByDYAggregated += pannedByDY;
				for ( var i = 0; i < allAnnotationsList.length; i++) {
					allAnnotationsList[i].fn.panStop(pannedByDX, pannedByDY);
				}
			};
			image.drag(move, start, up);
		}
		
		function moveAllAnnotationsBy(dx, dy)
		{
			for ( var i = 0; i < allAnnotationsList.length; i++) {
				allAnnotationsList[i].fn.moveAnnotation(dx, dy);
			}
		}
		
		function getRotationQualifiedCanvasBoundary()
		{
			var boundary = {};
			
			switch(getCurrentImageOrientation())
			{						
				case 1:
					if (getCurrentImageWidth() > canvasHeight)
					{
						boundary.minX = canvasHeight - getCurrentImageWidth() - 5;
						boundary.maxX = 5;
					}
					else
					{
						boundary.minX = 5;
						boundary.maxX = canvasHeight - getCurrentImageWidth() - 5;
					}
					if (getCurrentImageHeight() > canvasWidth)
					{
						boundary.minY = getCurrentImageHeight() + 5;
						boundary.maxY = canvasWidth - 5;
					}
					else
					{
						boundary.minY = canvasWidth - 5;
						boundary.maxY = getCurrentImageHeight() + 5;
					}
					break;
				case 2:
					if (getCurrentImageWidth() > canvasWidth)
					{
						boundary.minX = getCurrentImageWidth() + 5;
						boundary.maxX = canvasWidth - 5;
					}
					else
					{
						boundary.minX = canvasWidth - 5;
						boundary.maxX = getCurrentImageWidth() + 5;
					}
					if (getCurrentImageHeight() > canvasHeight)
					{
						boundary.minY = getCurrentImageHeight() + 5;
						boundary.maxY = canvasHeight - 5;
					}
					else
					{
						boundary.minY = canvasHeight - 5;
						boundary.maxY = getCurrentImageHeight() + 5;
					}
					break;
				case 3:
					if (getCurrentImageWidth() > canvasHeight)
					{
						boundary.minX = getCurrentImageWidth() + 5;
						boundary.maxX = canvasHeight - 5;
					}
					else
					{
						boundary.minX = canvasHeight - 5;
						boundary.maxX = getCurrentImageWidth() + 5;
					}
					if (getCurrentImageHeight() > canvasWidth)
					{
						boundary.minY = canvasWidth - getCurrentImageHeight() - 5;
						boundary.maxY = 5;
					}
					else
					{
						boundary.minY = 5;
						boundary.maxY = canvasWidth - getCurrentImageHeight() - 5;
					}
					break;
				default:
					if (getCurrentImageWidth() > canvasWidth)
					{
						boundary.minX = canvasWidth - getCurrentImageWidth();
						boundary.maxX = 0;;
					}
					else
					{
						boundary.minX = 0;
						boundary.maxX = canvasWidth - getCurrentImageWidth();
					}
					if (getCurrentImageHeight() > canvasHeight)
					{
						boundary.minY = canvasHeight - getCurrentImageHeight();
						boundary.maxY = 0;
					}
					else
					{
						boundary.minY = 0;
						boundary.maxY = canvasHeight - getCurrentImageHeight();
					}
			}
			
			return boundary;
		}
		 
		function makeAnnotationMovableStretchable(annotation) {
			var start = function() {
				annotation.fn.moveStart();
			}, up = function() {
				annotation.fn.moveStop();
			}, move = function(dx, dy) {
				// move will be called with dx and dy.
				annotation.fn.moveAnnotation(dx, dy);
			}, sStart = function() {
			    // storing original coordinates
			    annotation.fn.stretchStart();
			}, sUp = function() {
				annotation.fn.stretchStop();
			}, sMove = function(dx, dy) {
				annotation.fn.stretchMove(dx, dy);
			}
			
			annotation.baseElement.drag(move, start, up);
			annotation.stretchHandle.drag(sMove, sStart, sUp);
		}
		
		function effectZoom() {
			//Reset current rotation.
			rotateImageToDegree(0);
			
			//Reset any existing applied translations			
			translateAllGivenObjects(negate(zoomTranslationFactorX), negate(zoomTranslationFactorY));
			
			m_canvasManager.setZoomLevel(currentZoomLevel);
			zoomTranslationFactorX = (canvasWidth - getCurrentImageWidth()) / 2;
			zoomTranslationFactorY = (canvasHeight - getCurrentImageHeight()) / 2;

			//Apply new translations.
			translateAllGivenObjects(zoomTranslationFactorX, zoomTranslationFactorY);
			jQuery("#slider").slider('value', Math.round(currentZoomLevel * 100));
			jQuery("#slider").attr('title', Math.round(currentZoomLevel * 100) + '%');
			jQuery('#zoomLevel').html(Math.round(currentZoomLevel * 100) + '%');
			
			//Re-apply rotation
			rotateImageToDegree(currentRotationFactor);
			
			//Highlight any selected annotation
			highlightAnnotation(currentlySelectedAnnotation);
			showAnnotContextMenu(currentlySelectedAnnotation);
			sharedViewerState.zoomLevel = currentZoomLevel;
			updateSharedViewerState();
		}
		
		function effectZoomOut(args)
		{
			currentZoomLevel -= 0.1;
			if (parseFloat(currentZoomLevel.toPrecision(12)) < 0.1)
			{
				currentZoomLevel = 0.1;
			}
			m_pharmacyToolbarController.resetFitToSize();
			effectZoom();
		}
		
		function effectZoomIn(args)
		{
			if (parseFloat(currentZoomLevel.toPrecision(12)) < 3.0)
			{
				currentZoomLevel += 0.1;
				m_pharmacyToolbarController.resetFitToSize();
				effectZoom();
			}
		}
		
		function rotateImageToDegree(degreeOfRotation)
		{
			currentImage.rotate(parseInt(degreeOfRotation), getCurrentCanvasWidth()/2, getCurrentCanvasHeight()/2);
		}
		
		function rotateAnnotation(annotation, degreeOfRotation)
		{
			rotateAnnotationAroundPivot(annotation, degreeOfRotation, annotation.baseElement.attrs.x, annotation.baseElement.attrs.y);
		}
		
		function rotateAnnotationAroundPivot(annotation, degreeOfRotation, x, y)
		{
			rotateElement(annotation.baseElement, degreeOfRotation, x, y);
			rotateElement(annotation.stretchHandle, degreeOfRotation, x, y);
			positionTextVertically(annotation);
			if (annotation.text)
			{
				rotateElement(annotation.text, degreeOfRotation, x, y);
			}
		}
		
		function getAnnotationOrientation(annotation)
		{
			var annotRotOffset = getAnnotationSelfOrientationOffset(annotation);
			switch (getCurrentImageOrientation())
			{
				case 0:
					return annotRotOffset;
				case 1:
					return 90 + annotRotOffset
				case 2:
					return 180 + annotRotOffset;
				case 3:
					return 270 + annotRotOffset					
			}
		}
		
		function rotateElement(element, degreeOfRotation, x, y)
		{
			element.rotate(degreeOfRotation, x, y);
		}
		
		function getAnnotationSelfOrientationOffset(annotation)
		{
			switch(annotation.customProps.orientation)
			{
				case m_constants.ORIENTATION_NORTH:
					return 0;
					break;
				case m_constants.ORIENTATION_EAST:
					return -90;
					break;
				case m_constants.ORIENTATION_SOUTH:
					return 180;
					break;
				case m_constants.ORIENTATION_WEST:
					return 90;
					break;
			}
		}
		
		function rotateObjectByDegree(obj, degreeOfRotation)
		{
			obj.rotate(parseInt(degreeOfRotation), getCurrentCanvasWidth()/2, getCurrentCanvasHeight()/2);
		}
		
		function translateAllGivenObjects(xTransFactor, yTransFactor)
		{
			/* TODO - get rid of the isNaN check
			 * In FF, after a document is saved it refreshes and after the canvas.setZoom call the BG image's
			 * x, y some how become NaN and hence the bg image is not placed correctly.
			 * This behaviour was observed only in the given scenarion and hence this workaround of
			 * setting the values to 0 in case they are NaN*/
			if (isNaN(currentImage.attrs.x))
			{
				currentImage.attr('x', 0);
				currentImage.attr('y', 0);
			}
			currentImage.translate(xTransFactor, yTransFactor);
			jQuery.each(allAnnotationsList, function(id, value) {
				var coordnates = getRotationQualifiedCoordinates((value.customProps.commandObj.props.dimensions.x  * currentZoomLevel) + xTransFactor + (pannedByDXAggregated * currentZoomLevel), (value.customProps.commandObj.props.dimensions.y * currentZoomLevel) + yTransFactor + (pannedByDYAggregated * currentZoomLevel));
				rotateAnnotation(value, 0);
				moveAnnotationTo(value, coordnates.x, coordnates.y);
				rotateAnnotation(value, getAnnotationOrientation(value));
			});
		}
		
		function effectRotation(degrees) {
			changeCurrentRotationFactor(degrees);
			rotateImageToDegree(currentRotationFactor);

			jQuery.each(allAnnotationsList, function(id, value) {
				var coordnates = getRotationQualifiedCoordinates((value.customProps.commandObj.props.dimensions.x  * currentZoomLevel) + zoomTranslationFactorX + (pannedByDXAggregated * currentZoomLevel), (value.customProps.commandObj.props.dimensions.y * currentZoomLevel) + zoomTranslationFactorY + (pannedByDYAggregated * currentZoomLevel));	
				moveAnnotationTo(value, coordnates.x, coordnates.y);
				rotateAnnotation(value, getAnnotationOrientation(value));
			});

			highlightAnnotation(currentlySelectedAnnotation);
			hideExistingAnnotContextMenu();
		}
		
		function changeCurrentRotationFactor(degrees) {			
			currentRotationFactor = currentRotationFactor + parseFloat(degrees);
		}
		
		function getCurrentCanvasWidth() {
			return canvasWidth;
		}
		
		function getCurrentCanvasHeight() {
			return canvasHeight;
		}
		
		function getCurrentImageWidth() {
			return imageWidth * currentZoomLevel;
		}
		
		function getCurrentImageHeight() {
			return imageHeight * currentZoomLevel;
		}
		
		function positionTextVertically(annotation)
		{
			if (annotation.text)
			{
				annotation.text.attr('y', annotation.baseElement.attrs.y + (10 * currentZoomLevel) + (annotation.text.getBBox().height / 2));
			}
		}
		
		/* Breaks the text into number of rows and number of characters per row.
		 * Existing linefeeds are preserved. */
		function formatText(value, rows, charsPerRow)
		{			
			var strs = value.split(/\r\n|\r|\n/);
			var newValue = '';
			for (var i = 0; i < strs.length; i++)
			{
				newValue += strs[i] + '\r\n';
			}
			return breakIntoRows(newValue, rows, charsPerRow);
		}
		
		/* Breaks the text into number of rows and number of characters per row.
		 * Existing linefeeds may not be preserved.
		 * Use formatText for preserving existing linefeeds. */
		function breakIntoRows(value, rows, charsPerRow) {
			rows = parseInt(rows);
			charsPerRow = parseInt(charsPerRow);
			var newVal = "";
			for (var i = 0 ; i < rows ; i++) {
				var newLineIndex = value.substring(0, charsPerRow).indexOf('\r\n');
				if (newLineIndex == 0)				
				{
					newVal += value.substring(0, newLineIndex + 1);
					value = value.substring(newLineIndex + 1);
				}
				else if (newLineIndex != -1)
				{
					newVal += value.substring(0, newLineIndex + 1);
					value = value.substring(newLineIndex + 1);
				}
				else if (value.length > charsPerRow) {
					var spIndex = value.substring(0, charsPerRow).lastIndexOf(' ');
					if (spIndex != -1)
					{	
						newVal += value.substring(0, spIndex + 1) + '\r\n';
						value = value.substring(spIndex + 1);
					}
					else
					{
						newVal += value.substring(0, charsPerRow) + '\r\n';
						value = value.substring(charsPerRow);
					}
					
				} else {
					newVal += value;
					break;
				}
			}
			
			return newVal;
		}
		
		function makeAnnotationEditable(annotation) {
			jQuery(annotation.text.node).dblclick(function(e) {
				makeEditableCallback(annotation);
			});
			jQuery(annotation.baseElement.node).dblclick(function(e) {
				makeEditableCallback(annotation);
			});
		}
		
		function makeEditableCallback(annot, xOffset, yOffset)
		{
			currentEditableAnnotation = annot;
			currentlySelectedAnnotation = null;
			deHighlightAnnotation();
			hideExistingAnnotContextMenu();
			jQuery("#editable").moveDiv(getEditableBoxCoordinates(currentEditableAnnotation));
			jQuery("#editable").css('visibility', 'visible');
			jQuery("#editable").html(currentEditableAnnotation.text.customProps.completetext).show().trigger('dblclick');
			jQuery("#editable").escKeydown(function () {
				currentEditableAnnotation.fn.showAnnotation();
				jQuery("#editable").css('visibility', 'hidden');
			});
			currentEditableAnnotation.fn.hideAnnotation();
		}
		
		function getEditableBoxCoordinates(annotation)
		{
			var editableBoxYOffset = 35;
			var canvasCentreX = canvasWidth / 2;
			var canvasCentreY = canvasHeight / 2;
			
			var annotOrientation = convertDegreesToRotationFactor(getAnnotationOrientation(annotation));

			switch(annotOrientation)
			{
				case 1:
					return {x : annotation.baseElement.attrs.x - annotation.baseElement.attrs.width,
							y : annotation.baseElement.attrs.y + editableBoxYOffset};
					break;
				case 2:
					return {x : annotation.baseElement.attrs.x - annotation.baseElement.attrs.width,
							y : annotation.baseElement.attrs.y - annotation.baseElement.attrs.height + editableBoxYOffset};
					break;
				case 3:
					return {x : annotation.baseElement.attrs.x,
							y : annotation.baseElement.attrs.y - annotation.baseElement.attrs.height + editableBoxYOffset};
					break;
				default:
					return {x : annotation.baseElement.attrs.x,
						y : annotation.baseElement.attrs.y + editableBoxYOffset};
			}
		}
		
		function getCurrentImageOrientation()
		{
			var imageOr = ((currentRotationFactor / 90) % 4);
			if (imageOr < 0)
			{
				imageOr = 4 + imageOr;
			}
			
			return imageOr; 
		}
		
		/*
		 * Calculates the orientation of an annotations about to be created in relation to the image.
		 * Returns any of the following values.
		 * 'N' - North indicates that the annotation will be upright when the image is upright.
		 * 'E' - East indicates that the annotation will be 90 degrees cw wrt the image.
		 * 'S' - East indicates that the annotation will be 180 degrees cw wrt the image.
		 * 'W' - East indicates that the annotation will be 270 degrees cw wrt the image.
		 */
		function getImageRelativeAnotationOrientation()
		{
			switch(getCurrentImageOrientation())
			{
				case 0:
					return m_constants.ORIENTATION_NORTH;
					break;
				case 1:
					return m_constants.ORIENTATION_EAST;
					break;
				case 2:
					return m_constants.ORIENTATION_SOUTH;
					break;
				case 3:
					return m_constants.ORIENTATION_WEST;
					break;
				default:
					return m_constants.ORIENTATION_NORTH;
			}
		}

		function setUiqueId(object) {
			m_communicationController.syncGetData({url : getURL() + "/uniqueId"}, new function() {
				return {
					success : function(data) {
						object.customProps = {id : data};
					},
			
					failure : function(data) {}
				}
			});
		}
		
		function getSharedViewerState() {
			m_communicationController.syncGetData({url : getURL() + "/viewerState"}, new function() {
				return {
					success : function(data) {
						sharedViewerState = data;
					},
			
					failure : function(data) {}
				}
			});			
		}		
		
		function updateSharedViewerState()
		{
			m_communicationController.postData({url : getURL() + "/viewerState"}, JSON.stringify(sharedViewerState), new function() {
				return {
					success : function() {},				
					failure : function() {}
				}
			});
		}
		
		function initializePage()
		{
			getSharedViewerState();
			if (undefined != sharedViewerState.pageNo)
			{
				setInitialPage(sharedViewerState.pageNo);					
			}
			else
			{
				setInitialPage(require('m_urlUtils').getQueryParam('pageNo'));
			}	
		}
		
		function initializeZoom()
		{
			getSharedViewerState();
			if (undefined != sharedViewerState.zoomLevel)
			{
				currentZoomLevel = parseFloat(sharedViewerState.zoomLevel);
				effectZoom();
			}
			else
			{
				fitTo(m_pharmacyToolbarController.getFitToSize());
			}
		}
		
		function applyAnnotations() {
			m_communicationController.getData(getURL(), function(data) {
				if (data != null) {
					var rotationMetaData;
					for (var i = 0; i < data.length; i++) {
						if ('rotate' == data[i].type) {
							rotationMetaData = data[i]; 
						}
					}
					if (rotationMetaData != null) {
						effectRotation(parseInt(rotationMetaData.props.attributes.rotationfactor));
					}
					for (var i = 0; i < data.length; i++) {
						if (data[i] != null) {
							if ('StickyNote' == data[i].type) {
								drawStickyNote({x : data[i].props.dimensions.x, y : data[i].props.dimensions.y,
									width : data[i].props.dimensions.width, height : data[i].props.dimensions.height,
									text : data[i].props.text, completeText : data[i].props.completetext, action : 'Recreate',
									id : data[i].id, colour : data[i].props.attributes.colour, opacity : data[i].props.attributes.opacity,
									fontsize : data[i].props.attributes.fontsize, fontweight : data[i].props.attributes.fontweight,
									fontstyle : data[i].props.attributes.fontstyle, textdecoration : data[i].props.attributes.decoration,
									user : data[i].user, lastUserAction : data[i].lastuseraction, lastActionTimeStamp : data[i].lastactiontimestamp,
									orientation : data[i].props.orientation
									});
							} else if ('highlighter' == data[i].type) {
								drawHighlighter( {x : data[i].props.dimensions.x, y : data[i].props.dimensions.y, width : data[i].props.dimensions.width,
									height : data[i].props.dimensions.height, action : 'Recreate', id : data[i].id, colour : data[i].props.attributes.colour,
									opacity : data[i].props.attributes.opacity, user : data[i].user, lastUserAction : data[i].lastuseraction,
									lastActionTimeStamp : data[i].lastactiontimestamp, orientation : data[i].props.orientation
									});
							} 
							else if ('stamp' == data[i].type) {
								drawStamp({stampDocId : data[i].props.documentid, x : data[i].props.dimensions.x, y : data[i].props.dimensions.y,
									width : data[i].props.dimensions.width, height : data[i].props.dimensions.height, action : 'Recreate',
									id : data[i].id, user : data[i].user, lastUserAction : data[i].lastuseraction, lastActionTimeStamp : data[i].lastactiontimestamp,
									orientation : data[i].props.orientation
									});
							}					
						}
					}
				}
				
				jQuery(document).trigger('ANNOTATIONS_APPLIED_EVENT');
			});
		}
		
		function getPageDimensions(docId, pgNo)
		{
			var pageDimURL = require('m_urlUtils').getContextName() + "/services/rest/views-common/documentRepoService/retrievePageDimensions/" + docId + "/" + pgNo + "/" + m_pageController.getURLPostFix();
			var dimensions;
			m_communicationController.syncGetData({url : pageDimURL}, new function() {
				return {
					success : function(data) {
						dimensions = data;
					},
			
					failure : function(data) {}
				}
			});
			
			return dimensions;
		}
		
		function getURL() {
			return require('m_urlUtils').getContextName() + m_constants.ANNOTATIONS_RESTLET_PATH + documentId + m_constants.ANNOTATIONS_RESTLET_PATH_PAGE + m_pageController.getOriginalPageIndex() + "/" + m_pageController.getURLPostFix();
		}
		
		function getUserServicesURL()
		{
			return require('m_urlUtils').getContextName() + "/services/rest/views-common/documentRepoService"; 
		}
		
		/* Creates a command object. This is used for persisting the data in case of creation, deletion, change 
		 * to an annotation*/
		/* NOTE:
		 * The current x, y coordinates of the annotation are not stored as is because they are relative to the
		 * canvas and not relative to the image.
		 * Here we calculate the (x, y) of an annotation in relation to the top left corner of the image when as 100%
		 * zoom. This enables us to lay the annotations at correct location whenever it's accessed again 
		 * or in the event of image at zoom etc.
		 * 
		 * Calculation::
		 * Subtract the current image coordinates from those of the annotation to get dx, dy.
		 * Divide dx, dy by current zoom to get the actual dx, dy at 100% zoom.
		 * */
		function createCommandObject(annotation, type, action, txt) {
			var commandObject = {};
			commandObject.id = annotation.customProps.id;
			commandObject.type = type;
			commandObject.action = action;
			commandObject.user = annotation.customProps.user;
			commandObject.lastuseraction = annotation.customProps.lastUserAction;
			commandObject.lastactiontimestamp = annotation.customProps.lastActionTimeStamp;
			
			var zoomLevelToApply = ((action == 'Create') ? currentZoomLevel : 1);
			var coordinates = getRotationUnQualifiedCoordinates(annotation.baseElement.attr("x"), annotation.baseElement.attr("y"))
			commandObject.props = {
					dimensions : {
						x : (coordinates.x - currentImage.attr('x')) / currentZoomLevel,
						y : (coordinates.y - currentImage.attr('y')) / currentZoomLevel,
						width : annotation.baseElement.attr("width") / zoomLevelToApply,
						height : annotation.baseElement.attr("height") / zoomLevelToApply
					},
					attributes : {
						'colour' : annotation.customProps.colour,
						'opacity' : annotation.customProps.opacity
					},
					orientation : annotation.customProps.orientation
			};
			if (txt) {
				commandObject.props.text = txt.attr('text');
				commandObject.props.completetext = txt.customProps.completetext;
				commandObject.props.attributes.fontsize = annotation.text.zoom_memory["font-size"];
				commandObject.props.attributes.fontweight = annotation.customProps.fontweight;
				commandObject.props.attributes.fontstyle = annotation.customProps.fontstyle;
				commandObject.props.attributes.textdecoration = annotation.customProps.textdecoration;

			}
			if (annotation.customProps.docId != undefined)			
			{
				commandObject.props.documentid = annotation.customProps.docId;
			}
			commandObject.execute = function () {
				if (commandObject.action == 'Create') {
					jQuery(document).trigger("ANNOTATION_ADDED", {url : getURL(), jsonData : JSON.stringify(commandObject)});
				} else if (commandObject.action == 'Modify') {
					jQuery(document).trigger("ANNOTATION_MOVED", {url : getURL() + '/updateAnnotation', jsonData : JSON.stringify(commandObject)});
				} else if (commandObject.action == 'Delete') {
					jQuery(document).trigger("ANNOTATION_MOVED", {url : getURL() + '/deleteAnnotation', jsonData : JSON.stringify(commandObject)});
				}
			}
			
			annotation.customProps.commandObj = commandObject;			
			
			return commandObject;
		}
		
		function createRotateCommandObject(rotationFactor) {
			var commandObject = {};
			commandObject.id = 'rotationId';
			commandObject.type = 'rotate';
			commandObject.action = 'set';
			commandObject.props = {
					attributes : {
						'rotationfactor' : rotationFactor
					}
			};
			
			commandObject.execute = function () {
				jQuery(document).trigger("ROTATION_APPLIED", {url : getURL() + '/updateRotationFactor', jsonData : JSON.stringify(commandObject)});
			}
			
			return commandObject;
		}
		
		/* Draws highlighter annotation.
		 * Accepts following attributes: x, y, width, height, action, id */
		function drawHighlighter(args) {
			args.type = 'Highlighter';
			var annotation = newAnnotation(args);
			
			if (args.action == 'Recreate')
			{
				var coordnates = getRotationQualifiedCoordinates((args.x  * currentZoomLevel) + zoomTranslationFactorX + (pannedByDXAggregated * currentZoomLevel), (args.y * currentZoomLevel) + zoomTranslationFactorY + (pannedByDXAggregated * currentZoomLevel));
				args.x = coordnates.x;
				args.y = coordnates.y;
				args.width = args.width * currentZoomLevel;
				args.height = args.height * currentZoomLevel;
			}
			
			addRect(annotation, true, args);
			setStretchHandle(annotation, args);
			
			if (args.action == 'Recreate')
			{
				rotateAnnotation(annotation, getAnnotationOrientation(annotation));
			}
			
			//Set On-click functionality
			if (isDocumentEditable()) {
				setOnclickSelectHandling(annotation);
			}
			
			//Make annotation draggable - stretchable
			if (isDocumentEditable()) {
				makeAnnotationMovableStretchable(annotation);
			}
			
			//Creates a command object and associates it with the rectangle object
			var createHighlighterCmd = createCommandObject(annotation, 'highlighter', args.action);
			m_commandsController.handleCommand(createHighlighterCmd);
			
			addShowHideFunctionality(annotation);
			
			addHelperFunctions(annotation);
			
			allAnnotationsList.push(annotation);
			
			return annotation;
		}
		
		/*
		 * Draws stamp.
		 * Accepts following arguments - imageURL, x, y, width, height, action, id
		 * */
		function drawStamp(args)
		{
			args.type = 'Stamp';
			var annotation = newAnnotation(args);
			if (undefined == args.imageURL)
			{
				args.imageURL = m_pharmacyToolbarController.getDocDownloadURL(args.stampDocId);
			}
			
			if (args.action == 'Recreate')
			{
				var coordnates = getRotationQualifiedCoordinates((args.x  * currentZoomLevel) + zoomTranslationFactorX + (pannedByDXAggregated * currentZoomLevel), (args.y * currentZoomLevel) + zoomTranslationFactorY + (pannedByDYAggregated * currentZoomLevel))
				args.x = coordnates.x;
				args.y = coordnates.y;
				args.width = args.width * currentZoomLevel;
				args.height = args.height * currentZoomLevel;
			}
			
			setBGImage(annotation, true, args);
			setStretchHandle(annotation, args);
			
			if (args.action == 'Recreate')
			{
				rotateAnnotation(annotation, getAnnotationOrientation(annotation));
			}
			
			annotation.customProps.docId = args.stampDocId;

			//Set On-click functinality
			if (isDocumentEditable()) {
				setOnclickSelectHandling(annotation);
			}
			
			//Make annotation draggable - stretchable
			if (isDocumentEditable()) {
				makeAnnotationMovableStretchable(annotation);
			}
			
			//Creates a command object and associates it with the rectangle object
			var createStampsCmd = createCommandObject(annotation, 'stamp', args.action);
			m_commandsController.handleCommand(createStampsCmd);
			
			addShowHideFunctionality(annotation);
			
			addHelperFunctions(annotation);
			
			allAnnotationsList.push(annotation);
			
			return annotation;
		}
		
		/*
		 * Draws Stickynote.
		 * Accepts following arguments - x, y, width, height, text, completeText, action, id
		 * */
		function drawStickyNote(args) {
			args.type = 'StickyNote';
			var annotation = newAnnotation(args);

			if (args.action == 'Recreate')
			{
				var coordnates = getRotationQualifiedCoordinates((args.x  * currentZoomLevel) + zoomTranslationFactorX + (pannedByDXAggregated * currentZoomLevel), (args.y * currentZoomLevel) + zoomTranslationFactorY + (pannedByDXAggregated * currentZoomLevel));
				args.x = coordnates.x;
				args.y = coordnates.y;
				args.width = args.width * currentZoomLevel;
				args.height = args.height * currentZoomLevel;
				
			}
			
			setBGImage(annotation, true, args);
			if (args.action == 'Recreate')
			{
				addTextNode(annotation, args, (parseInt(annotation.customProps.fontsize) * currentZoomLevel));
			}
			else
			{
				addTextNode(annotation, args, imageViewerConfigOptions.noteFontSize);
			}
			setStretchHandle(annotation, args);
			
			if (args.action == 'Recreate')
			{
				rotateAnnotation(annotation, getAnnotationOrientation(annotation));
			}
			
			//Set On-click functionality
			if (isDocumentEditable()) {
				setOnclickSelectHandling(annotation);
			}
			
			//Make annotation draggable - stretchable
			if (isDocumentEditable()) {
				makeAnnotationMovableStretchable(annotation);
			}
			
			if (isDocumentEditable()) {
				setOnclickHandlingForText(annotation.text, annotation.bgImage);
			}
			
			if (isDocumentEditable()) {
				makeAnnotationEditable(annotation);
			}			
			
			var createStickyNoteCmd = createCommandObject(annotation, 'StickyNote', args.action, annotation.text);
			m_commandsController.handleCommand(createStickyNoteCmd);
			
			addShowHideFunctionality(annotation);
			
			addHelperFunctions(annotation);
			
			allAnnotationsList.push(annotation);
			return annotation;
		}
		 
		function newAnnotation(args)
		{
			var annotation = {type : args.type, fn : {}, customProps : {}};
			
			if (args.id == undefined) {
				setUiqueId(annotation);
				addUserInformation(annotation, 'Created');
			} else {
				annotation.customProps = {'id' : args.id, 'user' : args.user, 'lastUserAction' : args.lastUserAction, 'lastActionTimeStamp' : args.lastActionTimeStamp};				
			}
			annotation.customProps.action = args.action;
			
			return annotation;
		}
		
		function addUserInformation(annotation, action)
		{
			var userInfo = getUserInformation();
			annotation.customProps.user = userInfo.user;
			annotation.customProps.lastActionTimeStamp = userInfo.lastActionTimeStamp;
			annotation.customProps.lastUserAction = action;
		}
		
		function getUserInformation()
		{
			var userURL = getUserServicesURL() + "/getUser";
			var user = {};
			m_communicationController.syncGetData({url : userURL}, new function() {
				return {
					success : function(data) {
						if (data.user)
						{
							user.user = data.user;
							user.lastActionTimeStamp = data.timeStamp;
						}
						else
						{
							user = undefined;
							user.lastActionTimeStamp = undefined;
						}
						
					},
			
					failure : function(data) {}
				}
			});
			
			return user;
		}
		
		function addRect(annotation, isBaseElemement, args)
		{
			var rectAttrs;
			if (args.action == 'Create')
			{
				annotation.customProps.colour = imageViewerConfigOptions.highlighterColour;
				annotation.customProps.opacity = m_constants.HIGHLIGHTER_DEFAULT_OPACITY;
				annotation.customProps.orientation = getImageRelativeAnotationOrientation();
				rectAttrs = {'fill' : annotation.customProps.colour, 'fill-opacity' : m_constants.HIGHLIGHTER_DEFAULT_OPACITY}
			}
			else
			{
				annotation.customProps.colour = args.colour;
				annotation.customProps.opacity = m_constants.HIGHLIGHTER_DEFAULT_OPACITY;
				annotation.customProps.orientation = args.orientation;
				rectAttrs = {'fill' : annotation.customProps.colour, 'fill-opacity' : annotation.customProps.opacity};
			}
			annotation.rect = m_canvasManager.drawRectangle(args.x, args.y, args.width, args.height, rectAttrs).initZoom();
			if (isBaseElemement)
			{
				setBaseElement(annotation, annotation.rect);
			}
		}
		
		function setBGImage(annotation, isBaseElemement, args)
		{
			if (args.action == 'Create')
			{
				annotation.customProps.colour = imageViewerConfigOptions.noteColour;
				annotation.customProps.opacity = m_constants.STICKY_NOTE_DEFAULT_OPACITY;
				annotation.customProps.fontweight = (imageViewerConfigOptions.isBold == "true") ? 'bold' : 'normal';
				annotation.customProps.fontstyle = (imageViewerConfigOptions.isItalic == "true") ? 'italic' : 'normal';
				annotation.customProps.fontsize = parseInt(imageViewerConfigOptions.noteFontSize);
				annotation.customProps.textdecoration = (imageViewerConfigOptions.isUnderlined == "true") ? 'underline' : 'normal';
				annotation.customProps.orientation = getImageRelativeAnotationOrientation();
			}
			else
			{
				annotation.customProps.colour = args.colour;
				annotation.customProps.opacity = args.opacity;
				annotation.customProps.fontweight = args.fontweight;
				annotation.customProps.fontstyle = args.fontstyle;
				annotation.customProps.fontsize = args.fontsize;
				annotation.customProps.textdecoration = args.textdecoration;
				annotation.customProps.orientation = args.orientation;
			}
			var imageURL
			if (args.imageURL == undefined)
			{
				imageURL = getBGImageForColour(annotation.customProps.colour);
			}
			else
			{
				imageURL = args.imageURL;
			}
			annotation.bgImage = m_canvasManager.drawImageAt(imageURL, args.x, args.y, args.width, args.height).initZoom();
			if (isBaseElemement)
			{
				setBaseElement(annotation, annotation.bgImage)
			}
		}
		
		function getBGImageForColour(colour)
		{
			var colourMap = {
					green : "../../images/annotations/postItBG-Grn_200_150.png",
					pink : "../../images/annotations/postItBG-Pnk_200_150.png",
					blue : "../../images/annotations/postItBG-Blu_200_150.png",
					yellow : "../../images/annotations/postItBG-Yel_200_150.png"
			}
			
			var url = colourMap[colour];
			if (url == undefined)
			{
				url = "../../images/annotations/postItBG-Yel_200_150.png";
			}
			
			return url;
		}
		
		function setBaseElement(annotation, baseElement)
		{
			annotation.baseElement = baseElement;
		}
		
		function addTextNode(annotation, args, fontSize)
		{
			//Place the text node at the start of the rectangle.
			var txtX = parseInt(args.x) + (parseInt(args.width) / 2);
			var txtY = parseInt(args.y) + (parseInt(args.height) / 2);
			annotation.text = m_canvasManager.drawTextNode(txtX, txtY, args.text, fontSize).attr({'font-weight' : annotation.customProps.fontweight,
																						'font-style' : annotation.customProps.fontstyle,
																						'text-decoration' : annotation.customProps.textdecoration});
			annotation.text.attr('text', formatText(args.text, getNumberOfRows(annotation), getNumberOfCharactersPerRow(annotation)));
			positionTextVertically(annotation);
			
			/* Setting the font-size zoom memory to of RaphaelJS
			 * text object to a font size of how it should look at 100% zoom. 
			 * Thsi needs to be done as the raphael.zoom plugin uses this value to determine 
			 * font size of text at various zoom levels. */
			annotation.text.zoom_memory["font-size"] = parseInt(fontSize / currentZoomLevel);	
			annotation.text.customProps = {'completetext' : args.completeText};
		}
		
		function addShowHideFunctionality(annotation)
		{
			annotation.fn.hideAnnotation = function() {
				currentlySelectedAnnotation = null;
				deHighlightAnnotation();
				hideAnnotContextMenu(annotation);
				if (annotation.stretchHandle) {
					annotation.stretchHandle.hide();
				}
				if (annotation.rect) {
					annotation.rect.hide();
				}
				if (annotation.bgImage) {
					annotation.bgImage.hide();
				}
				if (annotation.text) {
					annotation.text.hide();
				}
			}
			
			annotation.fn.showAnnotation = function() {
				if (annotation.stretchHandle) {
					annotation.stretchHandle.show();
				}
				if (annotation.rect) {
					annotation.rect.show();
				}
				if (annotation.bgImage) {
					annotation.bgImage.show();
				}
				if (annotation.text) {
					annotation.text.show();
				}
			}
		}
		
		function setStretchHandle(annotation, args)
		{
			var size = Math.max(20, parseInt(annotation.baseElement.attrs.width * 0.1), parseInt(annotation.baseElement.attrs.height * 0.1));
			var x = annotation.baseElement.attrs.x + annotation.baseElement.attrs.width - size;
			var y = annotation.baseElement.attrs.y + annotation.baseElement.attrs.height - size;
			var stretchHandle = m_canvasManager.drawImageAt("../../images/icons/stretchHandle.png", x, y, size, size);
			stretchHandle.initZoom();
			makeElementEventOpaque(stretchHandle);
			stretchHandle.stretchable = annotation;
			annotation.stretchHandle = stretchHandle;
		}
		
		function addHelperFunctions(annotation)
		{
			//Removes all the elements of the annotation if they exist.
			annotation.fn.remove = function() {
				deHighlightAnnotation();
				hideAnnotContextMenu(annotation);
				removeFromAllAnnotationsList(annotation);

				if (annotation.stretchHandle)
				{
					annotation.stretchHandle.remove();				
				}
				if (annotation.text)
				{
					annotation.text.remove();
				}
				if (annotation.rect)
				{
					annotation.rect.remove();
				}
				if (annotation.bgImage)
				{
					annotation.bgImage.remove();
				}
			}
			
			//Move, stretch, pan related helper functions.
			addMoveHelpers(annotation);
		}
		
		function addMoveHelpers(annotation)
		{
			annotation.fn.moveStart = function() {
				deHighlightAnnotation();
				hideExistingAnnotContextMenu();
				if (annotation.rect)
				{
					annotation.rect.startX = annotation.rect.attr('x');
					annotation.rect.startY = annotation.rect.attr('y');
				}
				if (annotation.bgImage)
				{
					annotation.bgImage.startX = annotation.bgImage.attr('x');
					annotation.bgImage.startY = annotation.bgImage.attr('y');
				}
				if (annotation.stretchHandle)
				{
					annotation.stretchHandle.startX = annotation.stretchHandle.attr('x');
					annotation.stretchHandle.startY = annotation.stretchHandle.attr('y');				
				}
				if (annotation.text)
				{
					annotation.text.startX = annotation.text.attr('x');
					annotation.text.startY = annotation.text.attr('y');
				}
			}
			
			annotation.fn.moveAnnotation = function(dx, dy) {
				_annotationMovedFlag = true;
				
				/* Hide and rotate annotation to 0 degrees and move. Show the annotation after it's 
				 * rotated back.
				 * This is done to avoid cryptic calculations to related to moving of rotated elements
				 * and storing their correct image-relative coordinates. */
				if (m_pharmacyToolbarController.getAnnotationsShowHideStatus())
				{
					annotation.fn.hideAnnotation();
					//As hide annotations sets the currentlySelectedAnnotation to null.
					//setting it back to this annotation. 
					currentlySelectedAnnotation = annotation;					
				}
				rotateAnnotation(annotation, 0);
				
				/*
				 * Separate functions to move in x & y directions are needed as there may be situations where
				 * we may want to restrict the movement of annotations in any particular direction. In such scenarios
				 * either of the deltas sent to fn.moveAnnotations function can be 0. We don't want to use this delta
				 * as it would reset the position of an annotation to its original state.
				 */
				if (dx != 0)
				{
					annotation.fn.moveAnnotationX(dx);
				}
				if (dy != 0)
				{
					annotation.fn.moveAnnotationY(dy);
				}
				
				/*Rotate annotation back and make it visible. */
				rotateAnnotation(annotation, getAnnotationOrientation(annotation));
				
				if (m_pharmacyToolbarController.getAnnotationsShowHideStatus())
				{
					annotation.fn.showAnnotation();
				}
			}
			
			annotation.fn.moveAnnotationX = function(dx) {
				var boundary = getRotationQualifiedImageBoundaryForAnnotation(annotation);
				if (boundary.minX < annotation.baseElement.startX + dx && annotation.baseElement.startX + dx < boundary.maxX)
				{
					if (annotation.rect)
					{
						annotation.rect.attr({
							x : annotation.rect.startX + dx
						});
					}
					if (annotation.bgImage)
					{
						annotation.bgImage.attr({
							x : annotation.bgImage.startX + dx
						});
					}
					if (annotation.stretchHandle)
					{
						annotation.stretchHandle.attr({
							x : annotation.stretchHandle.startX + dx
						});
					}
					if (annotation.text)
					{
						annotation.text.attr({
							x : annotation.text.startX + dx
						});
					}
				}
			}
			
			annotation.fn.moveAnnotationY = function(dy) {
				var boundary = getRotationQualifiedImageBoundaryForAnnotation(annotation);
				if (boundary.minY < annotation.baseElement.startY + dy && annotation.baseElement.startY + dy < boundary.maxY)
				{
					if (annotation.rect)
					{
						annotation.rect.attr({
							y : annotation.rect.startY + dy
						});
					}
					if (annotation.bgImage)
					{
						annotation.bgImage.attr({
							y : annotation.bgImage.startY + dy
						});
					}
					if (annotation.stretchHandle)
					{
						annotation.stretchHandle.attr({
							y : annotation.stretchHandle.startY + dy
						});
					}
					if (annotation.text)
					{
						annotation.text.attr({
							y : annotation.text.startY + dy
						});
					}
				}
			}
			
			annotation.fn.moveStop = function() {
				executeMoveStop(annotation);
			}
			
			/* 
			 * Make adjustments to the memory attributes (x, y) of text as mentioned above.
			 * This is done to adjust the dx, dy caused by panning.
			 */
			annotation.fn.panStop = function(pannedByDX, pannedByDY) {
			}
			 
			annotation.fn.stretchMove = function(dx, dy) {
				var delta = getRotationQualifiedDeltasFor(dx, dy, getAnnotationOrientation(annotation));
				dx = delta.x;
				dy = delta.y;
				
				if (dx != 0 && dx < annotation.deltaMax.x)
				{
					annotation.fn.stretchMoveX(dx);
				}
				if (dy != 0 && dy < annotation.deltaMax.y)
				{
					annotation.fn.stretchMoveY(dy);
				}
			}
			
			annotation.fn.stretchMoveX = function(dx)
			{
				var oldWidth = annotation.baseElement.attrs.width;
				var oldX = annotation.stretchHandle.attrs.x;
				annotation.stretchHandle.attr({x: annotation.stretchHandle.x + dx});
				annotation.baseElement.attr({
				    	width : annotation.stretchHandle.attr("x") + annotation.stretchHandle.attrs.width - annotation.baseElement.attr("x")
				    });
				
				if(annotation.baseElement.attrs.width < m_constants.ANNOTATION_MIN_SIZE && annotation.baseElement.attrs.width < oldWidth)				
				{
					annotation.baseElement.attr('width', oldWidth);
					annotation.stretchHandle.attr('x', oldX);
				}
				
				if (annotation.text)
				{
					annotation.text.attr({
						x : annotation.baseElement.attr('x') + annotation.baseElement.attr('width') / 2,
						text : formatText(annotation.text.customProps.completetext, getNumberOfRows(annotation), getNumberOfCharactersPerRow(annotation))
					});
				}
			}
			
			annotation.fn.stretchMoveY = function(dy)
			{
				var oldHeight = annotation.baseElement.attrs.height;
				var oldY = annotation.stretchHandle.attrs.y;
				annotation.stretchHandle.attr({y: annotation.stretchHandle.y + dy});
				annotation.baseElement.attr({
				    height : annotation.stretchHandle.attr("y") + annotation.stretchHandle.attrs.height - annotation.baseElement.attr("y")
			    });
				
				if(annotation.baseElement.attrs.height < m_constants.ANNOTATION_MIN_SIZE && annotation.baseElement.attrs.height < oldHeight)				
				{
					annotation.baseElement.attr('height', oldHeight);
					annotation.stretchHandle.attr('y', oldY);
				}
				
				if (annotation.text)
				{
					annotation.text.attr({
						text : formatText(annotation.text.customProps.completetext, getNumberOfRows(annotation), getNumberOfCharactersPerRow(annotation))
					});
					positionTextVertically(annotation);
				}
			}
			
			annotation.fn.stretchStart = function()
			{
			    annotation.stretchHandle.x = annotation.stretchHandle.attr("x");
			    annotation.stretchHandle.y = annotation.stretchHandle.attr("y");
			    deHighlightAnnotation();
			    hideExistingAnnotContextMenu();
			    annotation.deltaMax = getRotationQualifiedMaxStretchLimits(annotation);
			}
			
			annotation.fn.stretchStop = function(dx, dy) {
				 executeStretchStop(annotation);
			}
		}
		
		function getRotationQualifiedDeltas(x, y)
		{
			return getRotationQualifiedDeltasFor(x, y, currentRotationFactor);
		}
		
		function getRotationQualifiedDeltasFor(x, y, rotationFactor)
		{
			var rotFactor = (rotationFactor / 90) % 4;
			if (rotFactor < 0)
			{
				rotFactor = 4 + rotFactor;
			}
			var tx;
			switch(rotFactor)
			{
				case 3:
					tx = x;
					x = -y;
					y = tx;
					break;
				case 2:
					x = -x;
					y = -y;
					break;
				case 1:
					tx = x;
					x = y;
					y = -tx;
			}
			
			return {'x' : x, 'y' : y};
		}
		
		function executeStretchStop(annotation)
		{
			var cmdObj = annotation.customProps.commandObj;
			cmdObj.action = 'Modify';
			cmdObj.props.dimensions.width = annotation.baseElement.attr("width")  / currentZoomLevel;
			cmdObj.props.dimensions.height = annotation.baseElement.attr("height")  / currentZoomLevel;
			if (cmdObj.props.completetext) {
				cmdObj.props.text = formatText(cmdObj.props.completetext, getNumberOfRows(annotation), getNumberOfCharactersPerRow(annotation));
			}
			
			require('m_commandsController').handleCommand(cmdObj);
			highlightAnnotation(annotation);
			annotation.deltaMax = undefined;
		}
		
		/* Called after an annotation is dragged around.
		 * NOTE:
		 * The current x, y coordinates of the annotation are not stored as is because they are relative to the
		 * canvas and not relative to the image. For details please see the comment at createCommandObject() function
		 * where a similar thing is done. */
		function executeMoveStop(annotation)
		{
			var cmdObj = annotation.customProps.commandObj;
			cmdObj.action = 'Modify';
			var coordinates = getRotationUnQualifiedCoordinates(annotation.baseElement.attr("x"), annotation.baseElement.attr("y"));
			cmdObj.props.dimensions.x = (coordinates.x - currentImage.attr('x')) / currentZoomLevel;
			cmdObj.props.dimensions.y = (coordinates.y - currentImage.attr('y')) / currentZoomLevel;
			updateAnnotationModifiedInfo(annotation);
			require('m_commandsController').handleCommand(cmdObj);			
		}
		
		function highlightAnnotation(annotation)
		{
			if (annotation)
			{
				deHighlightAnnotation();
				var x = annotation.baseElement.attr("x") - 2;
				var y = annotation.baseElement.attr("y") - 2;
				annotationHighlighter = m_canvasManager.drawRectangle(x, y, annotation.baseElement.attr("width") + 4, annotation.baseElement.attr("height") + 4, {'stroke-width' : 2, 'stroke' : 'blue'});
				
				/* Rotate the highlighting rectangle to the required degreed so 
				 * that it sits correctly around the annotation. */ 
				rotateElement(annotationHighlighter, getAnnotationOrientation(annotation), annotation.baseElement.attr("x"), annotation.baseElement.attr("y"));
			}
		}
		
		/*
		 * Display the context menu which you get on click of an annotation.
		 */
		function showAnnotContextMenu(annotation)
		{
			//First hide any other annotation context menu dialog that might be open.
			hideExistingAnnotContextMenu();
			if (annotation != undefined)
			{
				var menuOptions = getAnnotContextMenuOptions(annotation);
				var annotContextMenu = [];
				var blackBack = m_canvasManager.drawImageAt("../../images/annotations/black.jpeg", menuOptions.x, menuOptions.y, menuOptions.backWidth, menuOptions.backHeight);
				annotContextMenu.push(blackBack);
				
				//Add delete menu
				var delRect = m_canvasManager.drawImageAt("../../images/icons/toolbarBttn-Delete.png", menuOptions.x + 1, menuOptions.y + 1, 25, 25);
				annotContextMenu.push(delRect);
				addTooltip(delRect, InfinityBPMI18N.graphicscommon.getProperty('tiffViewer.annotation.settingsMenu.deleteAnnotation'));
				jQuery(delRect.node).click(function(event) {
					event.stopPropagation();
					deleteAnnotation(annotation);				
				});
				darkenOnHover(delRect);
				
				//Add settings menu
				if (menuOptions.showSettingsTab)
				{
					var settingsRect = m_canvasManager.drawImageAt("../../images/icons/toolbarBttn-Settings.png", menuOptions.x + 1, menuOptions.y + menuOptions.settingsYOffset, 25, 25);
					annotContextMenu.push(settingsRect);
					addTooltip(settingsRect, InfinityBPMI18N.graphicscommon.getProperty('tiffViewer.annotation.settingsMenu.options'));
					jQuery(settingsRect.node).click(function(event) {
						event.stopPropagation();
						hideAnnotationMenuDivs();
						if (annotation.type == 'StickyNote')
						{
							enableCloseButton(annotation, "#closeMenuDialogStickyNote");
							jQuery("#stickyNoteColourPalette #selectStickyFontsize option").each(function(event) {
								if (jQuery(this).text() == annotation.text.zoom_memory["font-size"])
								{
									jQuery(this).attr('selected', true);
								}
								else
								{
									jQuery(this).attr('selected', false);
								}
							});
							showStickyNoteSettingMenu(annotation);
						}
						else if (annotation.type == 'Highlighter')
						{
							enableCloseButton(annotation, "#closeMenuDialogHighlighter");
							jQuery("#highlighterColourPalette").moveDiv(getAnnotContextMenuCoordinates(annotation, 150, 50));
							jQuery("#highlighterColourPalette").css('visibility', 'visible');
							setHoverEffect("#highlighterColourPalette div");
							jQuery("#highlighterColourPalette div").click(function() {
								annotation.baseElement.attr('fill', jQuery(this).css('background-color'));
								annotation.customProps.commandObj.props.attributes.colour = jQuery(this).css('background-color');
								annotation.customProps.commandObj.action = 'Modify';
								// Update the 'annotation modified' information
								updateAnnotationModifiedInfo(annotation);
								m_commandsController.handleCommand(annotation.customProps.commandObj);
							});
						}
					});
					darkenOnHover(settingsRect);
				}
				
				//Add info menu
				var infoRect = m_canvasManager.drawImageAt("../../images/icons/toolbarBttn-Info.png", menuOptions.x + 1, menuOptions.y + menuOptions.infoYOffset, 25, 25);
				annotContextMenu.push(infoRect);
				addTooltip(infoRect, InfinityBPMI18N.graphicscommon.getProperty('tiffViewer.annotation.settingsMenu.information'));
				jQuery(infoRect.node).click(function(event) {
					event.stopPropagation();
					hideAnnotationMenuDivs();
					enableCloseButton(annotation, "#closeMenuDialogInfo");
					jQuery("#annotationInfoPanel").moveDiv(getAnnotContextMenuCoordinates(annotation, 170, 70));
					jQuery("#annotationInfoPanel").css('visibility', 'visible');
					jQuery("#annotationInfoPanel td#action").text(getI18NIsedUserAction(annotation.customProps.lastUserAction) + ":");
					jQuery("#annotationInfoPanel td#user").text(annotation.customProps.user);
					jQuery("#annotationInfoPanel td#timeStamp").text(annotation.customProps.lastActionTimeStamp);
				});
				darkenOnHover(infoRect);
				
				annotation.annotContextMenu = annotContextMenu;
				annotationWithContextMenu = annotation;
			}			
		}
		
		function getI18NIsedUserAction(action)
		{
			if ('Created' == action)
			{
				return InfinityBPMI18N.graphicscommon.getProperty('tiffViewer.annotation.info.userAction.created');
			}
			else if ('Modified' == action)
			{
				return InfinityBPMI18N.graphicscommon.getProperty('tiffViewer.annotation.info.userAction.modified');
			}			
		}
		
		function getAnnotContextMenuOptions(annotation)
		{
			var menuOptions;
			if (annotation.type == 'Stamp')
			{
				menuOptions = {
					backWidth : 27,
					backHeight : 51,
					showSettingsTab : false,
					infoYOffset : 26
				};
			}
			else
			{
				menuOptions = {
					backWidth : 27,
					backHeight : 76,
					showSettingsTab : true,
					settingsYOffset : 26,
					infoYOffset : 51
				};
			}
			
			//Set the context menu's x, y coordinates.
			var annotOrientation = convertDegreesToRotationFactor(getAnnotationOrientation(annotation));
			switch(annotOrientation)
			{
				case 1:
					menuOptions.x = annotation.baseElement.attrs.x + 1;
					menuOptions.y = annotation.baseElement.attrs.y - 1;
					break;
				case 2:
					menuOptions.x = annotation.baseElement.attrs.x + 1;
					menuOptions.y = annotation.baseElement.attrs.y - annotation.baseElement.attrs.height - 1;
					break;
				case 3:
					menuOptions.x = annotation.baseElement.attrs.x + annotation.baseElement.attrs.height + 1;
					menuOptions.y = annotation.baseElement.attrs.y - annotation.baseElement.attrs.width - 1;
					break;
				default:
					menuOptions.x = annotation.baseElement.attrs.x + annotation.baseElement.attrs.width + 1;
					menuOptions.y = annotation.baseElement.attrs.y - 1;
			}
			
			return menuOptions;
		}
		
		function showStickyNoteSettingMenu(annotation)
		{
			jQuery("#stickyNoteColourPalette").moveDiv(getAnnotContextMenuCoordinates(annotation, 170, 70));
			jQuery("#stickyNoteColourPalette").css('visibility', 'visible');
			setHoverEffect("#stickyNoteColourPalette div.stickyColourOption");
			setHoverEffect("#stickyNoteColourPalette div img");
			jQuery("#stickyNoteColourPalette div.stickyColourOption").click(function() {
				annotation.baseElement.attr('src', getBGImageForColour(jQuery(this).attr('selectedColour')));
				annotation.customProps.commandObj.props.attributes.colour = jQuery(this).attr('selectedColour');
				annotation.customProps.commandObj.action = 'Modify';
				// Update the 'annotation modified' information
				updateAnnotationModifiedInfo(annotation);
				m_commandsController.handleCommand(annotation.customProps.commandObj);
			});
			jQuery("#stickyNoteColourPalette div img.stickyTextBold").click(function() {
				if (annotation.text.attr('font-weight') != 'bold')
				{
					annotation.text.attr('font-weight', 'bold');
				}
				else
				{
					annotation.text.attr('font-weight', 'normal');
				}
				annotation.customProps.commandObj.props.attributes.fontweight = annotation.text.attr('font-weight');
				annotation.customProps.commandObj.action = 'Modify';
				annotation.text.attr({
					text : formatText(annotation.text.customProps.completetext, getNumberOfRows(annotation), getNumberOfCharactersPerRow(annotation))
				});
				positionTextVertically(annotation);
				annotation.customProps.commandObj.props.text = formatText(annotation.text.customProps.completetext, getNumberOfRows(annotation), getNumberOfCharactersPerRow(annotation))

				// Update the 'annotation modified' information
				updateAnnotationModifiedInfo(annotation);
				m_commandsController.handleCommand(annotation.customProps.commandObj);
			});
			jQuery("#stickyNoteColourPalette div img.stickyTextItalic").click(function() {
				if (annotation.text.attr('font-style') != 'italic')
				{
					annotation.text.attr('font-style', 'italic');
				}
				else
				{
					annotation.text.attr('font-style', 'normal');
				}
				annotation.customProps.commandObj.props.attributes.fontstyle = annotation.text.attr('font-style');
				annotation.customProps.commandObj.action = 'Modify';
				annotation.text.attr({
					text : formatText(annotation.text.customProps.completetext, getNumberOfRows(annotation), getNumberOfCharactersPerRow(annotation))
				});
				positionTextVertically(annotation);
				annotation.customProps.commandObj.props.text = formatText(annotation.text.customProps.completetext, getNumberOfRows(annotation), getNumberOfCharactersPerRow(annotation))

				// Update the 'annotation modified' information
				updateAnnotationModifiedInfo(annotation);
				m_commandsController.handleCommand(annotation.customProps.commandObj);
			});
			jQuery("#stickyNoteColourPalette div img.stickyTextUnderline").click(function() {
				if (annotation.text.attr('text-decoration') != 'underline')
				{
					annotation.text.attr('text-decoration', 'underline');
				}
				else
				{
					annotation.text.attr('text-decoration', 'normal');
				}
				annotation.customProps.commandObj.props.attributes.textdecoration = annotation.text.attr('text-decoration');
				annotation.customProps.commandObj.action = 'Modify';
				// Update the 'annotation modified' information
				updateAnnotationModifiedInfo(annotation);
				m_commandsController.handleCommand(annotation.customProps.commandObj);
			});
			jQuery("#stickyNoteColourPalette #selectStickyFontsize").change(function(event) {
				annotation.text.zoom_memory["font-size"] = parseFloat(jQuery("select option:selected").text());				
				effectZoom();
				annotation.customProps.commandObj.props.attributes.fontsize = annotation.text.zoom_memory["font-size"];				
				annotation.customProps.commandObj.action = 'Modify';
				annotation.text.attr({
					text : formatText(annotation.text.customProps.completetext, getNumberOfRows(annotation), getNumberOfCharactersPerRow(annotation))
				});
				positionTextVertically(annotation);
				annotation.customProps.commandObj.props.text = formatText(annotation.text.customProps.completetext, getNumberOfRows(annotation), getNumberOfCharactersPerRow(annotation))
				// Update the 'annotation modified' information
				updateAnnotationModifiedInfo(annotation);
				m_commandsController.handleCommand(annotation.customProps.commandObj);
				event.stopPropagation();
			});
			jQuery("#stickyNoteColourPalette #selectStickyFontsize").click(function(event) {
				event.stopPropagation();
			});
			jQuery("#stickyNoteColourPalette #selectStickyFontsize option").click(function(event) {
				event.stopPropagation();
			});
		}
		
		function enableCloseButton(annotation, selector)
		{
			//Set hover effect for menu close button
			setHoverEffect(selector);
			//On click handling of the menu close button
			jQuery(selector).click(function() {
				hideAnnotContextMenu(annotation);
			});
		}
		
		function addTooltip(element, tooltip)
		{
			jQuery(element.node).attr('title', tooltip);
		}
		
		function setHoverEffect(selector)
		{
			jQuery(selector).hover(function() {
				jQuery(this).css('border', '2px solid #333333');
			}, function() {
				jQuery(this).css('border', '1px solid #333333');
			});
		}
		
		function unbindAnnotationAnnotContextMenuEvents()
		{
			jQuery("#stickyNoteColourPalette div.stickyColourOption").unbind('click');
			jQuery("#stickyNoteColourPalette div img.stickyTextBold").unbind('click');
			jQuery("#stickyNoteColourPalette div img.stickyTextItalic").unbind('click');
			jQuery("#stickyNoteColourPalette div img.stickyTextUnderline").unbind('click');
			jQuery("#stickyNoteColourPalette #selectStickyFontsize").unbind('change');
			jQuery("#stickyNoteColourPalette div.selectStickyFontsize").unbind('click');
			jQuery("#highlighterColourPalette div").unbind('click');
			cleanAnnotationInfoPanel();			
		}
		
		function cleanAnnotationInfoPanel()
		{
			jQuery("#annotationInfoPanel td#action").text("");
			jQuery("#annotationInfoPanel td#user").text("");
			jQuery("#annotationInfoPanel td#timeStamp").text("");
		}
		
		function getAnnotContextMenuCoordinates(annotation, widthOffset, heightOffset)
		{
			var divYOffset = 30;
			var divXOffset = 5;
			var canvasCentreX = canvasWidth / 2;
			var canvasCentreY = canvasHeight / 2;
			
			var annotOrientation = convertDegreesToRotationFactor(getAnnotationOrientation(annotation));

			switch(annotOrientation)
			{
				case 1:
					return {x : annotation.baseElement.attrs.x - widthOffset + divXOffset,
							y : annotation.baseElement.attrs.y + divYOffset};
					break;
				case 2:
					return {x : annotation.baseElement.attrs.x - widthOffset + divXOffset,
							y : annotation.baseElement.attrs.y - annotation.baseElement.attrs.height + divYOffset};
					break;
				case 3:
					return {x : annotation.baseElement.attrs.x + annotation.baseElement.attrs.height - widthOffset + divXOffset,
							y : annotation.baseElement.attrs.y - annotation.baseElement.attrs.width + divYOffset};
					break;
				default:
					return {x : annotation.baseElement.attrs.x + annotation.baseElement.attrs.width - widthOffset + divXOffset,
							y : annotation.baseElement.attrs.y + divYOffset};
			}
		}
		
		function updateAnnotationModifiedInfo(annotation)
		{
			if(annotation.customProps.action != 'Create')
			{
				var user = getUserInformation();
				annotation.customProps.commandObj.lastuseraction = 'Modified';
				annotation.customProps.commandObj.user = user.user;
				annotation.customProps.commandObj.lastactiontimestamp = user.lastActionTimeStamp;
			}
		}
		
		function darkenOnHover(img)
		{
			jQuery(img.node).hover(function(event) {
				img.attr('opacity', '0.5');
			}, function() {
				img.attr('opacity', '1.0');
			});
		}

		function hideAnnotContextMenu(annotation)
		{
			//Unbinds any existing event bindings that may be present 
			unbindAnnotationAnnotContextMenuEvents();
			
			if (annotation.annotContextMenu)
			{
				jQuery.each(annotation.annotContextMenu, function(index, value) {
					value.remove();
				});
				annotation.annotContextMenu = undefined;
			}
			hideAnnotationMenuDivs();
		}
		
		function hideAnnotationMenuDivs()
		{
			jQuery("#stickyNoteColourPalette").css('visibility', 'hidden');
			jQuery("#highlighterColourPalette").css('visibility', 'hidden');
			jQuery("#annotationInfoPanel").css('visibility', 'hidden');			
		}
		
		function hideExistingAnnotContextMenu()
		{
			if (annotationWithContextMenu)
			{
				hideAnnotContextMenu(annotationWithContextMenu);
			}
		}
		
		function deHighlightAnnotation()
		{
			if (annotationHighlighter != undefined)
			{
				annotationHighlighter.remove();
				annotationHighlighter = undefined;
			}
		}

		function setOnclickSelectHandling(annotation) {
			jQuery(annotation.baseElement.node).click(
				function(event) {
					saveAndExitEditMode();
					deHighlightAnnotation();
					currentlySelectedAnnotation = annotation;
					highlightAnnotation(annotation);
					showAnnotContextMenuIfNotMoved(annotation)
					event.stopPropagation();
				});
		}
		
		function showAnnotContextMenuIfNotMoved(annotation)
		{
			if (_annotationMovedFlag == true)
			{
				_annotationMovedFlag = false;
			}
			else
			{
				showAnnotContextMenu(annotation);
			}
		}
		
		function setOnclickHandlingForText(textElement, rectElement) {
			jQuery(textElement.node).click(
				function(event) {
					event.stopPropagation();
					jQuery(rectElement.node).trigger('click');					
				});
		}
		
		function setInitialPage(pageNumber)
		{
			jQuery(document).trigger(require('m_constants').PAGE_NAVIGATION_EVENT, {type : 'textInput', value : pageNumber});		
		}
		
		function negate(value)
		{
			return -1 * value;
		}
		
		function makeElementEventOpaque(element)
		{
			jQuery(element.node).click(function(event) {
				event.stopPropagation();
			});
		}
		
		function getNumberOfCharactersPerRow(annotation)
		{
			var offset = 35;
			if('italic' == annotation.text.attr('font-weight'))
			{
				offset = 40;
			}
			if ('bold' == annotation.text.attr('font-weight'))
			{
				offset = 55;
			}
			return parseInt((annotation.baseElement.attr('width') - 35) / 4 * (10 / parseInt(annotation.text.attr("font-size"))));
		}
		
		function getNumberOfRows(annotation)
		{
			return parseInt((annotation.baseElement.attr('height') - 15) / 12 * (10 / parseInt(annotation.text.attr("font-size"))));
		}
		
		function initImageViewerConfigOptions()
		{
			var userURL = getUserServicesURL() + "/imageViewerConfig";
			m_communicationController.syncGetData({url : userURL}, new function() {
				return {
					success : function(data) {
						imageViewerConfigOptions = data;
					},
			
					failure : function(data) {}
				}
			});
		}
		
		function convertDegreesToRotationFactor(degrees)
		{
			var rotFactor = (degrees / 90) % 4;
			if (rotFactor < 0)
			{
				rotFactor = 4 + rotFactor;
			}
			
			return rotFactor;
		}
		
		function internationalizeAllElements()
		{
			jQuery("img[i18nPropKey]").each(function(index, elem) {				
				jQuery(elem).attr('alt', InfinityBPMI18N.graphicscommon.getProperty(jQuery(elem).attr('i18nPropKey')));
				jQuery(elem).attr('title', InfinityBPMI18N.graphicscommon.getProperty(jQuery(elem).attr('i18nPropKey')));
			});
			jQuery("span[i18nPropKey]").each(function(index, elem) {
				jQuery(elem).html(InfinityBPMI18N.graphicscommon.getProperty(jQuery(elem).attr('i18nPropKey')));
			});
			jQuery("div[i18nPropKey]").each(function(index, elem) {
				jQuery(elem).attr('title', InfinityBPMI18N.graphicscommon.getProperty(jQuery(elem).attr('i18nPropKey')));
			});
		}
		
		function isDocumentEditable()
		{
			return ('true' == isDocEditable);
		}
});