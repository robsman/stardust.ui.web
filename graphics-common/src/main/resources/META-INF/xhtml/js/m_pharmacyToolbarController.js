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
define(["m_toolbarManager", "m_constants", "m_pageController"], function(m_toolbarManager, m_constants, m_pageController) {
	var currentSelection;
	var annotationsShowHideFlag = true;
	var invertStatus = 1;
	var dsatStatus = 1;
	var stamps;
	var selectedStamp;
	var selectedStampDocId;
	var _toolClickActionsMap;
	var fitToSize = 'window'; //Can take following values ['window', 'width', 'height', 'none']
	var isDocEditable;
	
	return {
		init: function(toolbarDiv, noOfPages, isEditable, imageViewerConfigOptions)
		{
			isDocEditable = isEditable;
			applyImageViewerConfigOptions(imageViewerConfigOptions);
			setUpToolClickActionMap();
			setupToolStyling();
			m_toolbarManager.init(toolbarDiv, _toolClickActionsMap);
			setupEventHandling();
			jQuery('#numOfPagesLabel').html(" / " + noOfPages);
			jQuery('#currentPageNumTextBox').val(m_pageController.getCurrentPageIndex());
		},
		
		getCurrentSelection: function()
		{
			return currentSelection;
		},
		
		resetCurrentSelection: function()
		{
			deHighlightTools("#" + currentSelection);
			currentSelection = undefined;			
		},
		
		getInvertStatus : function()
		{
			return invertStatus;
		},
		
		getDsatStatus : function()
		{
			return dsatStatus;
		},
		
		getSelectedStamp : function()
		{
			return selectedStamp;
		},
		
		getSelectedStampDocId : function()
		{
			return selectedStampDocId;
		},
		
		getDocDownloadURL : function(docId)
		{
			return getDocDownloadURL(docId);
		},
		
		getAnnotationsShowHideStatus : function ()
		{
			return annotationsShowHideFlag;
		},
		
		getFitToSize : function()
		{
			return fitToSize;
		},
		
		resetFitToSize : function()
		{
			fitToSize = 'none';
			deHighlightFitToSizeTools();
		}
	}
	
	function setUpToolClickActionMap()
	{
		_toolClickActionsMap = {
			firstPageToolSelected : function(args) {
				fireToolClickedEvent(args);
			},
			lastPageToolSelected : function(args) {
				fireToolClickedEvent(args);
			},
			previousPageToolSelected : function(args) {
				fireToolClickedEvent(args);
			},
			nextPageToolSelected : function(args) {
				fireToolClickedEvent(args);
			},
			zoomOutToolSelected : function(args) {
				fireToolClickedEvent(args);
			},
			zoomInToolSelected : function(args) {
				fireToolClickedEvent(args);
			},
			fitToWindowToolSelected : function(args) {
				fitToSize = 'window';
				fireToolClickedEvent(args);
				deHighlightFitToSizeTools();
				highlightTool(args);
			},
			fitToHeightToolSelected : function(args) {
				fitToSize = 'height';
				fireToolClickedEvent(args);
				deHighlightFitToSizeTools();
				highlightTool(args);
			},
			fitToWidthToolSelected : function(args) {
				fitToSize = 'width';
				fireToolClickedEvent(args);
				deHighlightFitToSizeTools();
				highlightTool(args);
			},
			rotateRightToolSelected : function(args) {
				fireToolClickedEvent(args);
			},
			rotateLeftToolSelected : function(args) {
				fireToolClickedEvent(args);
			},
			rulersToolSelected : function(args) {
				//TODO
			},
			grayScaleToolSelected : function(args) {
				if (dsatStatus == 0)
				{
					dsatStatus = 1;
					deHighlightTools('#' + args.toolId);
				}
				else
				{
					dsatStatus = 0;
					invertStatus = 1;
					deHighlightTools("#invert");
					highlightTool({'toolId' : args.toolId});
				}
				args.dsatStatus = dsatStatus;
				fireToolClickedEvent(args);				
			},
			invertToolSelected : function(args) {
				if (invertStatus == 0)
				{
					invertStatus = 1;
					deHighlightTools('#' + args.toolId);
				}
				else
				{
					invertStatus = 0;
					dsatStatus = 1;
					deHighlightTools("#grayScale");
					highlightTool({'toolId' : args.toolId});
				}
				args.invertStatus = invertStatus;
				fireToolClickedEvent(args);
			},
			stickyNoteToolSelected : function(args) {
				if(isDocumentEditable()) {
					toggleCurrentSelection(args.toolId);
				}
			},
			highlighterToolSelected : function(args) {
				if(isDocumentEditable()) {
					toggleCurrentSelection(args.toolId);
				}
			},
			stampToolSelected : function(args) {
				if(isDocumentEditable()) {
					if (undefined != selectedStamp)
					{
						toggleCurrentSelection(args.toolId);
					}
					else
					{
						showStamps(args);
					}
				}
			},
			selectedStampToolSelected : function(args) {
				showStamps(args);
			},			
			stampsDialogToolSelected : function(args) {
				if(isDocumentEditable()) {
					showStamps(args);
				}
			},
			showHideAnnotationsToolSelected : function(args) {
				annotationsShowHideFlag = !annotationsShowHideFlag;
				args.show = annotationsShowHideFlag;
				fireToolClickedEvent(args);
				if (annotationsShowHideFlag == true)
				{
					highlightTool(args);
				}
				else
				{
					deHighlightTools("#" + args.toolId);
				}
			}
		};
	}
	
	function fireToolClickedEvent(data)
	{
		jQuery(document).trigger("TOOL_CLICKED_EVENT", data);
	}
	
	function setupEventHandling() {
		jQuery(document).bind(m_constants.PAGE_CHANGE_EVENT, function() {
			handlePageChangeEvent();
		});
		jQuery('#currentPageNumTextBox').change(function() {
			firePageNavigationEvent({type : 'textInput', value : jQuery('#currentPageNumTextBox').val()});
		});
		jQuery('#zoomLevel').change(function() {
			fireZoomLevelChangeEvent({value : jQuery('#zoomLevel').val()});
		});
	};
	
	function showStamps(args)
	{
		retrieveStamps();
		jQuery('#stampGallery').css("padding", "0px").html("<div style='max-height: 480px; overflow:scroll;'>" + generateStampsHTML(stamps) + "</div>");
		jQuery('#stampGallery img').css("cursor", "pointer");
		//Stamps selection handling
		jQuery('#stampGallery img').click(function() {
			selectedStamp = jQuery(this).attr('src');
			jQuery("#selectedStamp").attr('src', selectedStamp);
			jQuery("#selectedStamp").attr('width', m_constants.STAMP_PREVIEW_WIDTH);
			jQuery("#selectedStamp").attr('height', m_constants.STAMP_PREVIEW_HEIGHT);
			selectedStampDocId = jQuery(this).attr('stampDocId');
			makeSelection("stamp");
			jQuery('#stampGallery').dialog('close');
		});
		
		//Add handling for collapsible panel header - plain js
		/*jQuery('#stampGallery table a').click(function() {
			var spaceLessKey = removeSpaces(jQuery(this).attr('id'));
			var elems = document.getElementsByTagName("tr");
			for (var i = 0; i < elems.length; i++)
			{
				if (("SUB_STAMPS_" + spaceLessKey) === elems[i].getAttribute('name'))
				{
					if (elems[i].style.display != 'none')
					{
						elems[i].style.display = 'none';
					}
					else
					{
						elems[i].style.display = '';
					}
				}
			}
		});*/
		
		//Add handling for collapsible panel header using jQuery
		jQuery('#stampGallery table a').click(function() {
			var spaceLessKey = removeSpaces(jQuery(this).attr('id'));
			if (jQuery('#stampGallery table tr[name="SUB_STAMPS_' + spaceLessKey + '"]').css('display') != 'none')
			{
				jQuery('#stampGallery table tr[name="SUB_STAMPS_' + spaceLessKey + '"]').css('display', 'none');
			}
			else
			{
				jQuery('#stampGallery table tr[name="SUB_STAMPS_' + spaceLessKey + '"]').css('display', '');
			}
		});
		
		jQuery('#stampGallery').dialog({ autoOpen : false, width : 280, height : 'auto' });
		if (jQuery('#stampGallery').dialog('isOpen'))
		{
			jQuery('#stampGallery').dialog('close');
		}
		else
		{
			jQuery('#stampGallery').dialog('open').dialog({position : [jQuery('#stampsDialog').offset().left - 250, jQuery('#stampsDialog').offset().top + 20]});
		}
	}
	
	function retrieveStamps()
	{
		if (undefined == stamps)
		{
			require("m_communicationController").syncGetData({url : require("m_urlUtils").getStampsQueryURL()}, new function() {
				return {
					success : function(data) {
						stamps = data;
					},		
					failure : function(data) {}
				}			
			});
		}
	}
	
	function formatFolderHeading(key)
	{
		if ('STANDARD_STAMPS' == key)
		{
			return InfinityBPMI18N.graphicscommon.getProperty('tiffViewer.stamps.standardStamps.heading');
		}
		else if ('MY_STAMPS' == key)
		{
			return InfinityBPMI18N.graphicscommon.getProperty('tiffViewer.stamps.myStamps.heading');
		}
		
		return key;
	}
	
	function generateStampsHTML(stamps)
	{
		var stampsHTML = "<table border='1px' width='100%'>";
		jQuery.each(stamps, function(key, stampTypes) {
			stampsHTML += "<tr><td><table width='100%'><tr style='background-color: #CCCCCC'><td width='100%' colspan='2'><a id='" + removeSpaces(key) + "' href='#' style='font-family : Arial,Helvetica,sans-serif; font-size : 12px; color : #708090; text-decoration : none; font-weight : bold;'>" + formatFolderHeading(key) + "</a></td></tr>";
			jQuery.each(stampTypes, function(stampType, stampsArray) {
				stampsHTML += generateSubFolderStampsHTML(stampsArray, stampType, removeSpaces(key));
			})
			stampsHTML += "</table></td></tr>";
		});
		stampsHTML += "</table>";
		return stampsHTML;
	}

	
	function generateSubFolderStampsHTML(valArray, folderName, collapsibleKey)
	{
		var stampsHTML = "";
		if (folderName != 'Uncategorized')
		{
			stampsHTML += "<tr width='100%' style='background-color: #E5E5E5' name = 'SUB_STAMPS_" + collapsibleKey + "'><td width='100%' colspan='2'><span style='font-family : Arial,Helvetica,sans-serif; font-size : 12px; color : #708090;'>" + folderName + "</span></td></tr>";
		}

		for (var i = 0; i < valArray.length; i++)
		{
			stampsHTML += "<tr name = 'SUB_STAMPS_" + collapsibleKey + "'><td><img src='";
			stampsHTML += valArray[i].stampURL;
			stampsHTML += "' stampDocId='";
			stampsHTML += valArray[i].stampDocId;
			stampsHTML += "' style='width: 120px; height: 70px;' 'title='Stamps' alt='Stamps' /></td>";
			i++;
			if (i < valArray.length)
			{
				stampsHTML += "<td><img src='";
				stampsHTML += valArray[i].stampURL;
				stampsHTML += "' stampDocId='";
				stampsHTML += valArray[i].stampDocId;
				stampsHTML += "' style='width: 120px; height: 70px' title='Stamps' alt='Stamps' /></td></tr>";
			}
			else
			{
				stampsHTML += "<td></td></tr>";
			}
		}
		
		return stampsHTML;
	}
	
	function removeSpaces(str)
	{
		var i = str.indexOf(" "); 
		while (i > 0)
		{
			
			str = str.slice(0, i) + str.slice(i+1); // remove the space
			i = str.indexOf(" "); 
		}
		return str;

	}
	
	function handlePageChangeEvent()
	{
		jQuery('#currentPageNumTextBox').val(m_pageController.getCurrentPageIndex());
		if (m_pageController.isFirstPage())
		{
			jQuery('#firstPage').css('opacity', 0.3).parent().removeClass("buttonHolder").css('cursor', '');
			jQuery('#previousPage').css('opacity', 0.3).parent().removeClass("buttonHolder").css('cursor', '');
		}
		else
		{
			jQuery('#firstPage').css('opacity', 1).parent().css('cursor', 'pointer').addClass("buttonHolder");
			jQuery('#previousPage').css('opacity', 1).parent().css('cursor', 'pointer').addClass("buttonHolder");
		}
		if (m_pageController.isLastPage())
		{
			jQuery('#nextPage').css('opacity', 0.3).parent().css('cursor', '').removeClass("buttonHolder");
			jQuery('#lastPage').css('opacity', 0.3).parent().css('cursor', '').removeClass("buttonHolder");
		}
		else
		{
			jQuery('#nextPage').css('opacity', 1).parent().css('cursor', 'pointer').addClass("buttonHolder");
			jQuery('#lastPage').css('opacity', 1).parent().css('cursor', 'pointer').addClass("buttonHolder");
		}
		
		if (!isDocumentEditable())
		{
			jQuery('#stickyNote').css('opacity', 0.3).parent().css('cursor', '').removeClass("buttonHolder");
			jQuery('#highlighter').css('opacity', 0.3).parent().css('cursor', '').removeClass("buttonHolder");
			jQuery('#stamp').css('opacity', 0.3).parent().css('cursor', '').removeClass("buttonHolder");
			jQuery('.stampSelector').css('opacity', 0.3).removeClass("stampSelector").css('cursor', '');
			jQuery('#selectedStamp').css('opacity', 0.3).parent().removeClass("stampButtonHolder").css('cursor', '');
			jQuery('#stampsDialog').css('opacity', 0.3).parent().removeClass("stampButtonHolder").css('cursor', '');
		}
	}
	
	function firePageNavigationEvent(data)
	{
		jQuery(document).trigger(m_constants.PAGE_NAVIGATION_EVENT, data);
	}

	function makeSelection(elementId)
	{
		if(currentSelection != undefined)
		{
			deHighlightTools("#" + currentSelection);
		}
		currentSelection = elementId;
		highlightTool({'toolId' : currentSelection});
	}
	
	function toggleCurrentSelection(elementId)
	{
		if(elementId == currentSelection)
		{
			deHighlightTools("#" + currentSelection);
			currentSelection = undefined;
		}
		else
		{
			makeSelection(elementId);
		}
	}
	
	function fireZoomLevelChangeEvent(data)
	{
		jQuery(document).trigger(m_constants.ZOOM_LEVEL_CHANGE_EVENT, data);
	}
	
	function applyImageViewerConfigOptions(imageViewerConfigOptions)
	{
		setInitialZoomLevel(imageViewerConfigOptions);
		setInitialInvertStatus(imageViewerConfigOptions);
		intializeShowHideFlag(imageViewerConfigOptions);
		initializeDefaultStamp(imageViewerConfigOptions)
	}
	
	function setInitialInvertStatus(imageViewerConfigOptions)
	{
		if (imageViewerConfigOptions.isInverted == "true")
		{
			invertStatus = 0;
			highlightTool({toolId : 'invert'});
		}
		else
		{
			invertStatus = 1;
		}
	}
	
	function intializeShowHideFlag(imageViewerConfigOptions)
	{
		if (imageViewerConfigOptions.showAnnotations == "true")
		{
			annotationsShowHideFlag = true;
			highlightTool({toolId : 'showHideAnnotations'});
		}
		else
		{
			annotationsShowHideFlag = false;
		}
	}
	
	function initializeDefaultStamp(imageViewerConfigOptions)
	{
		if ("" != imageViewerConfigOptions.selectedStamp)
		{
			selectedStampDocId = imageViewerConfigOptions.selectedStamp;
			selectedStamp = getDocDownloadURL(imageViewerConfigOptions.selectedStamp);
			jQuery("#selectedStamp").attr("src", selectedStamp);
		}
		else
		{
			jQuery("#selectedStamp").attr("width", 0);
		}
	}
	
	function setInitialZoomLevel(imageViewerConfigOptions)
	{
		var iZoomLevel = imageViewerConfigOptions.zoomLevel;
		if (iZoomLevel == '0')	//100% zoom
		{
			currentZoomLevel = 1;
			fitToSize = 'none';
		}
		else if (iZoomLevel == '1')	//fit to window
		{
			fitToSize = 'window';
			highlightTool({toolId : 'fitToWindow'});
		}
		else if (iZoomLevel == '2')	//fit to height
		{
			fitToSize = 'height';
			highlightTool({toolId : 'fitToHeight'});
		}
		else if (iZoomLevel == '3')	//fit to width
		{
			fitToSize = 'width';
			highlightTool({toolId : 'fitToWidth'});
		}
		else //fit to window
		{
			fitToSize = 'window';
			highlightTool({toolId : 'fitToWindow'});
		}
	}

	function setupToolStyling()
	{
		jQuery(".toolBarCtrl").hover(function() {
			jQuery(this).parent().addClass('toolOnHoverStyle');
		}, function() {
			jQuery(this).parent().removeClass('toolOnHoverStyle');
		});
	}
	
	function highlightTool(args)
	{
		jQuery('#' + args.toolId).parent().addClass('toolSelectedStyle');
	}
	
	function deHighlightFitToSizeTools()
	{
		deHighlightTools("img[id^='fitTo']");
	}
	
	function deHighlightTools(selector)
	{
		jQuery(selector).parent().removeClass('toolSelectedStyle');		
	}
	
	function getDocDownloadURL(docId)
	{
		var stampURL;
		require("m_communicationController").syncGetData({url : require("m_urlUtils").getDocDownloadTokenURL() + "/" + docId}, new function() {
			return {
				success : function(data) {
					stampURL = data;
				},		
				failure : function(data) {}
			}			
		});
		return stampURL;
	}
	
	function isDocumentEditable()
	{
		return ('true' == isDocEditable);
	}
});