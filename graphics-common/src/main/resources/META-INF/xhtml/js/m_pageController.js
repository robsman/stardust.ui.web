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
define(["m_constants"], function(m_constants) {
	var minPageIndex = 1;
	var maxPageIndex;
	var currentPageIndex;
	var currentOriginalPageIndex;
	var pageIndexMap = [];
	var _toolActionsMap;
	var documentId;
	var thisObj = {
		init : function(docId, firstPage) {
			documentId = docId;
			initializeToolActionsMap();
			setPageSequence();
			currentPageIndex = firstPage;
			setupEventHandling();
			window.parent.EventHub.events.subscribe("page_click_event", pageChangeEventSubscriber);
			window.parent.EventHub.events.subscribe("page_sequence_change_event", setPageSequence);
		},
	
		getCurrentPageIndex : function() {			
			return currentPageIndex;
		},
		
		hasNextPage : function() {
			if (currentPageIndex < maxPageIndex)
			{
				return true;
			}
			else
			{
				return false;
			}
		},
		
		hasPreviousPage : function() {
			if (currentPageIndex > minPageIndex)
			{
				return true;
			}
			else
			{
				return false;
			}
			
		},
		
		isFirstPage : function() {
			if (currentPageIndex == minPageIndex) {
				return true;
			}
			
			return false;
		},
		
		isLastPage : function() {
			if (currentPageIndex == maxPageIndex) {
				return true;
			}
			
			return false;
		},
		
		getURLPostFix : function() {
			return new Date().getTime();
		},
		
		getOriginalPageIndex : function() {
			return pageIndexMap[currentPageIndex];
		}
	}
	
	return thisObj;
	
	function initializeToolActionsMap()
	{
		_toolActionsMap = {
			firstPageToolAction : function(args)
			{
				if (!thisObj.isFirstPage()) {
					moveToFirstPage();
					firePageChangedEvent();
				}
			},
			
			lastPageToolAction : function(args)
			{
				if (!thisObj.isLastPage()) {
					moveToLastPage();
					firePageChangedEvent();
				}
			},
			previousPageToolAction : function(args)
			{
				if (thisObj.hasPreviousPage()) {
					moveToPreviousPage();
					firePageChangedEvent();
				}
			},
			
			nextPageToolAction : function(args)
			{
				if (thisObj.hasNextPage()) {
					moveToNextPage();
					firePageChangedEvent();
				}
			}
		};
	}
	
	function moveToNextPage() {
		if (currentPageIndex < maxPageIndex) {
			currentPageIndex++;
		}
		return currentPageIndex;
	}
	
	function moveToPreviousPage() {
		if (currentPageIndex > minPageIndex) {
			currentPageIndex--;				
		}
		return currentPageIndex;
	}
	
	function moveToLastPage() {
		currentPageIndex = maxPageIndex;
		return currentPageIndex;
	}
	
	function moveToFirstPage() {
		currentPageIndex = minPageIndex;
		return currentPageIndex;
	}
	
	function moveToPage(pgNo) {
		pageNo = parseInt(pgNo);
		if (!isNaN(pageNo)) {
			if (pageNo > maxPageIndex) {
				currentPageIndex = maxPageIndex;
			} else if (pageNo < minPageIndex) {
				currentPageIndex = minPageIndex;
			} else {
				currentPageIndex = pageNo;
			}
		}
	}
	
	function setupEventHandling() {
		jQuery(document).bind(m_constants.PAGE_NAVIGATION_EVENT, function(event, data) {
			moveToPage(data.value);
			firePageChangedEvent();
		});
		jQuery(document).bind("TOOL_CLICKED_EVENT", function(event, data) {
			try
			{
				_toolActionsMap[data.toolId + "ToolAction"](data);
			}
			catch(e)
			{
				//Ignore
			}
		});
	}
	
	function firePageChangedEvent() {
		jQuery(document).trigger(m_constants.PAGE_CHANGE_EVENT);
	}
	
	function pageChangeEventSubscriber(arg1, pageNo, origPageNo) {
		currentOriginalPageIndex = origPageNo;
		jQuery(document).trigger(m_constants.PAGE_NAVIGATION_EVENT, {type : 'textInput', value : pageNo});
	};
	
	function setPageSequence()
	{
		var pgSeqURL = require('m_urlUtils').getContextName() + "/services/rest/views-common/documentRepoService" + "/documentPageSequence/" + documentId + "/" + new Date().getTime();
		require("m_communicationController").syncGetData({url : pgSeqURL}, new function() {
			return {
				success : function(pageSequence) {
					maxPageIndex = pageSequence.length;
					for (var i = 1; i <= maxPageIndex; i++) {
						pageIndexMap[i] = pageSequence[i - 1];
					}
				},
		
				failure : function(data) {}
			}
		});
	}
});