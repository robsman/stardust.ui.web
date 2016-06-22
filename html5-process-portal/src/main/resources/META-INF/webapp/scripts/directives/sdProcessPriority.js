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
(function() {
   'use strict';

   angular.module('workflow-ui').directive( 'sdProcessPriority', [ '$parse', ProcessPriority ]);

   /*
    *
    */
   function ProcessPriority() {
	   return {
		   restrict : 'A',
		   scope : {
			   sdaPriority : '=',
			   sdaEditable : '=',
			   sdaAvailablePriorities : '=',
			   sdaOnChange : '&',
			   sdaValueChanged : '='
		   },
		   template :
			   '<div ng-if="!sdaEditable" sd-popover sda-template="\'priorityTemplate.html\'" sda-trigger="mouseenter" sda-placement="top auto" > '+
			   		'<i  class="pi pi-flag pi-lg" ng-class="\'priority-flag-\'+sdaPriority.name"> </i>\n'
			   +'</div>' +
			   '<div ng-if="sdaEditable" class="change-higlight-container" ng-class="{\'change-highlight\':sdaValueChanged}">\n' +
			   	   		'<select ng-if="sdaEditable" class="activity-table-priority-combo" ng-model="sdaPriority.value" ng-change="sdaOnChange();" ' +
			   	        ' ng-options="item.value as item.label for item in sdaAvailablePriorities"></select>\n' +
			   '</div>\n'+
			   '<script id="priorityTemplate.html" type="text/ng-template">'+
	  		   	   '<div>'+
  	  		   			'<span class="worklist-tooltip-label" ng-bind="processPriorityCtrl.i18n(\'views-common-messages.views-activityTable-priorityFilter-table-priorityColumn-name\')"></span>: '+
  	  		   			'<span ng-bind="sdaPriority.label"></span>' +
	  		   	   '</div>'+
	  		   '</script>',
		   controller : [ '$scope', '$attrs', '$parse' , 'sdPriorityService', ProcessPriorityController ]
	   };
   };
   /**
    *
    */
   function ProcessPriorityController($scope, $attrs, $parse) {
	   this.toolTip = {
         show : false
      };

      this.i18n = $scope.$parent.i18n;
      $scope.processPriorityCtrl = this;
   }
})();
