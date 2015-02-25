/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
/**
 * @author Johnson.Quadras
 */
(function() {
	'use strict';

	angular.module('bpm-common').directive('sdTrivialManualActivityData',['sdUtilService', Directive]);

	/*
	 *
	 */
	function Directive(sdUtilService) {

		return {
			restrict : 'A',
			scope:{
			   'dataMappings' :'=sdaDataMappings',
			   'onChange':'&sdaOnChange',
			   'outData':'=sdaOutData'
			},
			templateUrl : 'plugins/html5-process-portal/scripts/directives/partials/trivialManualActivityData.html',
			controller : [ '$scope', '$parse', '$attrs','sdUtilService', DataController ]
		};
	}
	/**
	 *
	 */
	function DataController($scope, $parse, $attrs,sdUtilService) {
	   this.i18n = $scope.$parent.i18n;
	 
	   /**
	    * 
	    */
	   this.onChange = function(){
	      if($attrs.sdaOnChange){
	         $scope.onChange();
	      }
	   }
	   /**
	    * 
	    */
      this.stopEvent = function(event) {
         sdUtilService.stopEvent(event);
      }

	   
	   $scope.dataCtrl = this;
	}
	
	
})();
