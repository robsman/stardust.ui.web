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
		'text/xhtml' : 'sc sc-fw sc-document-html',
		'text/html' : 'sc sc-fw sc-document-html',
		'image/jpeg' : 'sc sc-fw  sc-picture',
		'image/pjpeg' : 'sc sc-fw  sc-picture',
		'image/gif' : 'sc sc-fw  sc-picture',
		'image/tiff' : 'sc sc-fw sc-picture',
		'application/pdf' : 'sc sc-fw sc-document-adobe',
		'text/rtf' : 'glyphicon glyphicon-book',
		'application/msword' : 'sc sc-fw sc-document-word',
		'video/quicktime' : 'sc sc-fw  sc-document-video',
		'video/x-ms-wmv' : 'sc sc-fw  sc-document-video',
		'video/x-msvideo' : 'sc sc-fw  sc-document-video',
		'application/x-shockwave-flash' : 'sc sc-fw sc-document-flash',
		'audio/x-ms-wma' : 'sc sc-fw  sc-document-audio',
		'audio/mpeg' : 'sc sc-fw  sc-document-audio',
		'application/zip' : 'sc sc-fw sc-document-zip',
		'text/plain' : 'sc sc-fw  sc-document-text-o',
		'application/vnd.ms-powerpoint' : 'sc sc-fw sc-document-powerpoint',
		'application/vnd.ms-excel' : 'sc sc-fw sc-document-excel-alt',
		'image/png' : 'sc sc-fw sc-picture',
		'application/vnd.openxmlformats-officedocument.wordprocessingml.document' : 'sc sc-fw  sc-document',
		'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' : 'sc sc-fw sc-document-excel',
		'application/vnd.openxmlformats-officedocument.presentationml.presentation' : 'sc sc-fw  sc-document-powerpoint',
		'application/bpmrptdesign' : 'glyphicon glyphicon-book',
		'application/bpmrpt' : 'glyphicon glyphicon-book',
		'audio/x-mpeg' : 'sc sc-fw  sc-document-audio',
		'text/csv' : 'sc sc-fw  sc-document-csv'
	// 'text/css' : 'glyphicon-plus',
	// 'application/octet-stream' : 'glyphicon-plus',
	// 'text/xml' : 'glyphicon-plus',
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
