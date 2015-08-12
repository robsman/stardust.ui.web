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

(function(){
	'use strict';

	angular.module('bpm-common').directive('sdRichTextEditor', 
			[ 'sdLoggerService', Directive]);
	
	
	function Directive (){

	    return {
	        require: '?ngModel',
	        link: function ($scope, elm, attr, ngModel) {
	        	
	            var ck = CKEDITOR.replace(elm[0]);
	            if(attr.sdaReadOnly) {
	            	ck.config.readOnly = true;
	        	}
	          
	            ck.on('pasteState', function () {
	                $scope.$apply(function () {
	                    ngModel.$setViewValue(ck.getData());
	                });
	            });

	            ngModel.$render = function (value) {
	                ck.setData(ngModel.$modelValue);
	            };
	        }
	    };
	}

})();