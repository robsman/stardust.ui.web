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
	angular.module('bpm-common.services').provider('sdMimeTypeService',
			function() {
				this.$get = [function($q, $http) {
					var service = new MimeTypeService();
					return service;
				} ];
			});

	/**
	 * Mimetypes are keys in a hashmap from which that mimetype is associated
	 * with an icon and an rx property. The icon property specifies the css portal-icon
	 * classes which should be associated with the mime-type. The rx property is a regular
	 * expression which should match a file name with that mime type based on file extensions.
	 * @type {Object}
	 */
	var mimeMap = {

		'text/xhtml' : {'icon' : 'pi pi-html', 
						'rx' : /\.(xhtml)$/i},

		'text/html' : {'icon' : 'pi pi-html', 
					   'rx' : /\.(html|htm)$/i},

		'image/jpeg' : {'icon' : 'pi pi-image',
						'rx' : /\.(jpeg|jpg|jpe|jif|jfif|jfi)$/i},

		'image/pjpeg' : {'icon' : 'pi pi-image', 
						 'rx' : /\.(pjpeg)$/i},

		'image/gif' : {'icon' : 'pi pi-image', 
					   'rx' : /\.(gif)$/i},

		'image/bmp' : {'icon' : 'pi pi-image', 
					   'rx' : /\.(bmp|dib)$/i},

		'image/tiff' : {'icon' : 'pi pi-image', 
					    'rx' : /\.(tiff|tif)$/i},

		'application/json' : {'icon' : 'pi pi-html', 
							  'rx' : /\.(json)$/i},

		'application/javascript' : {'icon' : 'pi pi-html', 
									'rx' : /\.(js)$/i},

		'application/pdf' : {'icon' : 'pi pi-pdf', 
							 'rx' : /\.(pdf)$/i},

		'text/rtf' : {'icon' : 'pi pi-word', 
					  'rx' : /\.(rtf)$/i},

		'application/msword' : {'icon' : 'pi pi-word', 
							    'rx' : /\.(doc|dot|docx|docm|dotx|dotm|docb)$/i},

		'video/quicktime' : {'icon' : 'pi pi-video', 
						     'rx' : /\.(mov|qt)$/i},

		'video/x-ms-wmv' : {'icon' : 'pi pi-video', 
							'rx' : /\.(wmv|asf|wma)$/i},

		'video/x-msvideo' : {'icon' : 'pi pi-video', 
							 'rx' : /\.(avi)$/i},

		'application/x-shockwave-flash' : {'icon' : 'pi pi-video', 
										   'rx' : /\.(swf)$/i},

		'audio/x-ms-wma' : {'icon' : 'pi pi-audio', 
							'rx' : /\.(wma)$/i},

		'audio/mpeg' : {'icon' : 'pi pi-audio', 
						'rx' : /\.(mpeg|mpg|mpe|mpa|mp2|m1v|m3u|mp4|m4a|m4p|m4b|m4r)$/i},

		'application/zip' : {'icon' : 'pi pi-zip', 
							 'rx' : /\.(zip|zipx)$/i},

		'text/plain' : {'icon' : 'pi pi-text', 
						'rx' : /\.(txt|text)$/i},

		'application/vnd.ms-powerpoint' : {'icon' : 'pi pi-ppt', 
										   'rx' : /\.(ppt|pot|pps|pptx|pptm|potx|potm|ppam|ppsx|ppsm|sldx|sldm)$/i},

		'application/vnd.ms-excel' : {'icon' : 'pi pi-csv-excel', 
									  'rx' : /\.(xls|xlt|xlm|xlsx|xlsm|xltx|xltm|xlsb|xla|xlam|xll|xlw)$/i},

		'image/png' : {'icon' : 'pi pi-image', 
					   'rx' : /\.(png)$/i},

		'application/vnd.openxmlformats-officedocument.wordprocessingml.document' : {'icon' : 'pi pi-word', 
																					 'rx' : /\.(docx)$/i},

		'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' : {'icon' : 'pi pi-csv-excel', 
																               'rx' : /\.(xlsx)$/i},

		'application/vnd.openxmlformats-officedocument.presentationml.presentation' : {'icon' : 'pi pi-ppt', 
																	                   'rx' : /\.(pptx)$/i},

		'application/bpmrptdesign' : {'icon' : 'pi pi-report', 
									  'rx' : /\.(bpmrptdesign)$/i},

		'application/bpmrpt' : {'icon' : 'pi pi-bar-chart', 
								'rx' : /\.(bpmrpt)$/i},

		'audio/x-mpeg' : {'icon' : 'pi pi-audio', 
						  'rx' : /\.(mp2|mp3)$/i},

		'text/csv' : {'icon' : 'pi pi-csv-excel', 
				      'rx' : /\.(csv)$/i},

		'text/css' : {'icon' : 'pi pi-css', 
					  'rx' : /\.(css)$/i},

		'application/octet-stream' : {'icon' : 'pi pi-other', 
					 'rx' : /\.(exe|bin|dll)$/i},

		'text/xml' : {'icon' : 'pi pi-xml-json', 
					  'rx' : /\.(xml)$/i}
	};
	/**
	 * 
	 */
	function MimeTypeService() {

		MimeTypeService.prototype.getMimeTypeFromFileName = function(fileName){
			var key,
				result;

			for(key in mimeMap){
				result = mimeMap[key].rx.test(fileName);
				if(result){
					return key
				}
			}

			//default if we don't have a match
			return 'application/octet-stream';

		};
		/**
		 * 
		 */
		MimeTypeService.prototype.getIcon = function(mimeType) {
		  	var icon = undefined;

			if (mimeType &&  mimeMap[mimeType]) {
				var icon = mimeMap[mimeType].icon;
			}

			if (angular.isUndefined(icon)) {
			// Default Icon
				icon = "pi pi-other";
			}
			return icon;
		};
	};
})();
