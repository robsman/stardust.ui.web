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
define(function() {
	
	return {
		//Servlet related constants.
		TIFF_RENDERER_SERVLET_PATH : "/IppTiffRenderer",
		ANNOTATIONS_RESTLET_PATH : "/services/rest/documents/",
		ANNOTATIONS_RESTLET_PATH_PAGE : "/pages/",
		DMS_RESTLET_PATH : "/services/rest/documentRepoService",
		STAMPS_GET_REQUEST_URL : "/retrieveStamps",
		DOCUMENT_DOWNLOAD_TOKEN_URL : "/documentDownloadURL",
			
		//Events
		CANVAS_CLICKED_EVENT : "CANVAS_CLICKED",
		TOOL_CLICK_EVENT_POSTFIX : "_TOOL_CLICKED",
		STICKEY_NOTE_ADD_EVENT : "STICKEY_NOTE_ADDED",
		HIGHLIGHTER_ADD_EVENT : "HIGHLIGHTER_ADDED",
		
		//Annotations
		HIGHLIGHTER_DEFAULT_WIDTH : 220,
		HIGHLIGHTER_DEFAULT_HEIGHT : 100,
		HIGHLIGHTER_DEFAULT_COLOUR : '00FFFF',
		HIGHLIGHTER_DEFAULT_OPACITY : 0.5,
		
		STICKY_NOTE_DEFAULT_WIDTH : 280,
		STICKY_NOTE_DEFAULT_HEIGHT : 180,
		STICKY_NOTE_DEFAULT_COLOUR : 'yellow',
		STICKY_NOTE_DEFAULT_OPACITY : 1,
		
		STAMP_DEFAULT_WIDTH : 150,
		STAMP_DEFAULT_HEIGHT : 70,
		STAMP_PREVIEW_WIDTH : 70,
		STAMP_PREVIEW_HEIGHT : 18,
		
		ANNOTATION_MIN_SIZE : 30,
		
		//Page navigation
		PAGE_CHANGE_EVENT : "PAGE_CHANGED",
		PAGE_NAVIGATION_EVENT : "PAGE_NAVIGATION_TRIGGERED",
		ZOOM_IN_EVENT : "ZOOM_IN",
		ZOOM_OUT_EVENT : "ZOOM_OUT",
		ZOOM_LEVEL_CHANGE_EVENT : "ZOOM_LEVEL_CHANGED",
		ROTATE_EVENT : "ROTATE",
			
		ORIENTATION_NORTH : 'N',
		ORIENTATION_EAST : 'E',
		ORIENTATION_WEST : 'W',
		ORIENTATION_SOUTH : 'S'		
	}
});