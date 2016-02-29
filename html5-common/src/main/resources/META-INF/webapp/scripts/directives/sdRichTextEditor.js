/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * @author Johnson.Quadras
 */


/**
 * options :
 * @sdaReadOnly : true / false  Default : false
 * @sdaPosition : bottom / top  Default : bottom
 * &sdaCustomToolbar: Could be a key to one of the "predefinedToolbars" defined in the directive
 * 						or an array of custom tools.
 * 						If not set, uses the default toolbar.
 */
(function(){
	'use strict';

	angular.module('bpm-common').directive('sdRichTextEditor', 
			[ 'sdLoggerService', Directive]);
	
	
	function Directive () {
		
		var predefinedToolbars = {
				minimal: [
		                  {name: 'basicstyles', items: [ 'Bold', 'Italic', 'Strike', '-', 'RemoveFormat' ]},
		                  {name: 'styles', items: ['Styles', 'Formats']},
		                  {name: 'paragraph', items: ['BulletedList', 'NumberedList']},
		                  {name: 'clipboard', items: [ 'Undo', 'Redo' ]}
		                ]
		};

	    return {
	        require: '?ngModel',
	        scope : {
	        	position : '@sdaPosition',
	        	readOnly : '@sdaReadOnly',
	        	height: '@sdaHeight',
	        	customToolbar: '&sdaCustomToolbar',
	        	onBlur : '&sdaOnBlur',
	        	onMode: '&sdaOnModeChange'
	        },
	        link: function ($scope, elm, attr, ngModel) {
	        	
	            var ck = CKEDITOR.replace(elm[0]);
	            ck.config.readOnly = $scope.readOnly || false;
	            ck.config.toolbarLocation =$scope.position || 'bottom';
	            if ($scope.height) {
	            	ck.config.height = $scope.height;
	            }
	            if ($scope.customToolbar) {
		            ck.config.toolbar = predefinedToolbars[$scope.customToolbar()] || $scope.customToolbar();
	            }	            
	            
	            ck.on('blur', function() {
	              $scope.$apply(function () {
                  ngModel.$setViewValue(ck.getData());
	              });
	            	$scope.onBlur();
	             });
	           
	            ck.on('pasteState', function () {
	                $scope.$apply(function () {
	                    ngModel.$setViewValue(ck.getData());
	                });
	            });

	            ck.on('mode', function() {
	              $scope.$apply(function () {
                  ngModel.$setViewValue(ck.getData());
                });
                $scope.onMode();
               });
             
	            ngModel.$render = function (value) {
	                ck.setData(ngModel.$modelValue);
	            };
	        }
	    };
	}

})();
