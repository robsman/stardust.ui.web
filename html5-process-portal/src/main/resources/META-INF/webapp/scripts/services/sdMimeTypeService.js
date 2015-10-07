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
		'text/xhtml' : 'pi pi-html',
		'text/html' : 'pi pi-html',
		'image/jpeg' : 'pi pi-image',
		'image/pjpeg' : 'pi pi-image',
		'image/gif' : 'pi pi-image',
		'image/tiff' : 'pi pi-image',
		'application/pdf' : 'pi pi-pdf',
		'text/rtf' : 'pi pi-word',
		'application/msword' : 'pi pi-word',
		'video/quicktime' : 'pi pi-video',
		'video/x-ms-wmv' : 'pi pi-video',
		'video/x-msvideo' : 'pi pi-video',
		'application/x-shockwave-flash' : 'pi pi-video',
		'audio/x-ms-wma' : 'pi pi-audio',
		'audio/mpeg' : 'pi pi-audio',
		'application/zip' : 'pi pi-zip',
		'text/plain' : 'pi pi-text',
		'application/vnd.ms-powerpoint' : 'pi pi-ppt',
		'application/vnd.ms-excel' : 'pi pi-csv-excel',
		'image/png' : 'pi pi-image',
		'application/vnd.openxmlformats-officedocument.wordprocessingml.document' : 'pi pi-word',
		'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' : 'pi pi-csv-excel',
		'application/vnd.openxmlformats-officedocument.presentationml.presentation' : 'pi pi-ppt',
		'application/bpmrptdesign' : 'pi pi-report',
		'application/bpmrpt' : 'pi pi-bar-chart',
		'audio/x-mpeg' : 'pi pi-audio',
		'text/csv' : 'pi pi-csv-excel',
		'text/css' : 'pi pi-css',
		'application/octet-stream' : 'pi pi-other',
		'text/xml' : 'pi pi-text'
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
				icon = "pi pi-other";
			}
			return icon;
		};
	};
})();
