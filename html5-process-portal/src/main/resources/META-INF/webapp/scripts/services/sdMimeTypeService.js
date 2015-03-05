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
		//'text/xhtml' : 'glyphicon-plus',
		//'text/html' : 'glyphicon-plus',
		'image/jpeg' : 'sc sc-fw  sc-picture',
		'image/pjpeg' : 'sc sc-fw  sc-picture',
		'image/gif' : 'sc sc-fw  sc-picture',
		'image/tiff' : 'sc sc-fw sc-picture',
		//	'application/pdf' : 'glyphicon-plus',
		//	'text/rtf' : 'glyphicon-plus',
		//	'application/msword' : 'glyphicon-plus',
		//	'video/quicktime' : 'glyphicon-plus',
		//	'video/x-ms-wmv' : 'glyphicon-plus',
		//	'video/x-msvideo' : 'glyphicon-plus',
		//	'application/x-shockwave-flash' : 'glyphicon-plus',
		//	'audio/x-ms-wma' : 'glyphicon-plus',
		//	'audio/mpeg' : 'glyphicon-plus',
		//	'application/zip' : 'glyphicon-plus',
		//	'text/plain' : 'glyphicon-plus',
		//	'text/xml' : 'glyphicon-plus',
		//	'application/vnd.ms-powerpoint' : 'glyphicon-plus',
		//	'application/vnd.ms-excel' : 'glyphicon-plus',
		'image/png' : 'sc sc-fw sc-picture',
		//	'application/octet-stream' : 'glyphicon-plus',
		'application/vnd.openxmlformats-officedocument.wordprocessingml.document' : 'sc sc-fw  sc-document',
		'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' : 'sc sc-fw sc-document-excel',
		//	'application/vnd.openxmlformats-officedocument.presentationml.presentation' : 'glyphicon-plus',
		//	'application/bpmrptdesign' : 'glyphicon-plus',
		//	'application/bpmrpt' : 'glyphicon-plus',
		//	'text/css' : 'glyphicon-plus',
		//	'audio/x-mpeg' : 'glyphicon-plus',
		'text/csv' : 'sc sc-fw  sc-document-csv'
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
