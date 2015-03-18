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

   angular.module('bpm-common').directive( 'sdProcessPriority', [ '$parse', ProcessPriority ]);

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
		   template : '<i ng-if="!sdaEditable" class="glyphicon glyphicon-flag priority-flag" '+
		   						'ng-class="\'priority-flag-\'+sdaPriority.name" '+
		   						'ng-mouseenter=\'processPriorityCtrl.toolTip.show = true\' '+
		   						'ng-mouseleave=\'processPriorityCtrl.toolTip.show = false\'> '+
		   			  '<\/i>'+
		   			  '<div style="width:90px;" ng-class="{\'activity-table-priority-changed-highlight\':sdaValueChanged}"><select ng-if="sdaEditable" class="activity-table-priority-combo" ng-model="sdaPriority.value" ng-change="sdaOnChange();" '+
		   	         ' ng-options="item.value as item.label for item in sdaAvailablePriorities"></select></div>'+
		   			  '<div class="popup-dlg worklist-tooltip" style="color: black" ng-show="processPriorityCtrl.toolTip.show">'+
		   			  		'<span class="worklist-tooltip-label" ng-bind="processPriorityCtrl.i18n(\'views-common-messages.views-activityTable-priorityFilter-table-priorityColumn-name\')"><\/span> '+
		   			  		': <span ng-bind="sdaPriority.label"><\/span>' +
		   			  '<\/div>',
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
