/*****************************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ****************************************************************************************/

/*
 * @author Johnson.Quadras
 */

(function() {
	'use strict';

	angular.module('workflow-ui.services').provider('sdMimeTypeService',
			function() {
				this.$get = [function($q, $http) {
					var service = new MimeTypeService();
					return service;
				} ];
			});

	var iconsForMIME = {
		'text/xhtml' : 'sc sc-fw sc-document-html-o',
		'text/html' : 'sc sc-fw sc-document-html-o',
		'image/jpeg' : 'sc sc-fw  sc-picture',
		'image/pjpeg' : 'sc sc-fw  sc-picture',
		'image/gif' : 'sc sc-fw  sc-picture',
		'image/tiff' : 'sc sc-fw sc-picture',
		'application/pdf' : 'sc sc-fw sc-document-adobe-o',
		'text/rtf' : 'glyphicon glyphicon-book',
		'application/msword' : 'sc sc-fw sc-document-word-o',
		'video/quicktime' : 'sc sc-fw  sc-document-video-o',
		'video/x-ms-wmv' : 'sc sc-fw  sc-document-video-o',
		'video/x-msvideo' : 'sc sc-fw  sc-document-video-o',
		'application/x-shockwave-flash' : 'sc sc-fw sc-document-flash-o',
		'audio/x-ms-wma' : 'sc sc-fw  sc-document-audio-o',
		'audio/mpeg' : 'sc sc-fw  sc-document-audio-o',
		'application/zip' : 'sc sc-fw sc-document-zip-o',
		'text/plain' : 'sc sc-fw  sc-document-text-o',
		'application/vnd.ms-powerpoint' : 'sc sc-fw sc-document-powerpoint-o',
		'application/vnd.ms-excel' : 'sc sc-fw sc-document-excel-alt',
		'image/png' : 'sc sc-fw sc-picture',
		'application/vnd.openxmlformats-officedocument.wordprocessingml.document' : 'sc sc-fw  sc-document-o',
		'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' : 'sc sc-fw sc-document-excel-o',
		'application/vnd.openxmlformats-officedocument.presentationml.presentation' : 'sc sc-fw  sc-document-powerpoint-o',
		'application/bpmrptdesign' : 'glyphicon glyphicon-book',
		'application/bpmrpt' : 'glyphicon glyphicon-book',
		'audio/x-mpeg' : 'sc sc-fw  sc-document-audio-o',
		'text/csv' : 'sc sc-fw  sc-document-csv-o',
		'text/css' : 'sc sc-fw sc-document-text-o',
		'application/octet-stream' : 'sc sc-fw sc-document-o',
		'text/xml' : 'sc sc-fw sc-document-html-o'
	};

	/**
	 * 
	 */
	function MimeTypeService() {
		/**
		 * 
		 */
		MimeTypeService.prototype.getIcon = function(mimeType) {

			var icon = iconsForMIME[mimeType];

			if (angular.isUndefined(icon)) {
				//Default Icon
				icon = "sc sc-fw  sc-document";
			}
			return icon;
		};
	}
	;

})();
