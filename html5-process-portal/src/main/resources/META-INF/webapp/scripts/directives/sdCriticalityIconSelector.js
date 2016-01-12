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
 * @author Nikhil.Gahlot
 */
(function() {
   'use strict';

   angular.module('bpm-common').directive('sdCriticalityIconSelector', [ CriticalityIconSelector ]);

   /*
	 * Directive class
	 * 
	 * 		Attributes			Type	Required	Default
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 * 		nd-model			=		Yes
	 * 		sda-edit-mode 		@ 		No			false
	 */
   function CriticalityIconSelector() {

      return {
         restrict : 'A',
         template :
        	'<button class="button-link" ng-disabled="editMode != true" aid="CritIconSelector">'
        	+ '<i ng-click="criticalityIconSelectorCtrl.openPopover($event)" class="pi pi-flag pi-lg" aid="CritIconSelector-SelectedFlg"' 
	 			+ 'ng-class="\'criticality-flag-\'+ (bindModel != undefined? bindModel : \'NO-COLOR\')"><\/i>  '
	 		+'</button>'
			+'<span sd-popover="criticalityIconSelectorCtrl.popoverDirective" ng-disabled="editMode != true">'
				+ '<div class="popover-body" style="width: 105px;">'
					+ '<button type="button" class="close" ng-click="criticalityIconSelectorCtrl.closePopover()" aid="CritIconSelector-Close" aria-label="Close" title="{{i18n == undefined ? \'Close\' : i18n(\'views-common-messages.common-close\')}}">'
		        		+ '<span aria-hidden="true">&times;</span>'
		            + '</button>'
					+ '<div style="float: left;">'
						+ '<button ng-click="criticalityIconSelectorCtrl.setIcon(icon)" class="button-link tbl-tool-link" ng-repeat="icon in criticalityIconSelectorCtrl.getFlagIcons()" aid="CritIconSelector-IconOptions">'
							+ '<i class="pi pi-flag pi-lg" '
							+ 'ng-class="\'criticality-flag-\'+icon"><\/i>  '
						+ '<\/button>'
					+ '</div>'
				+ '<\/div>' 
			+'</span>',
		 scope : {
			 bindModel:'=ngModel',
			 editMode:'=sdaEditMode'
		 },
         controller : [ '$scope', CriticalityIconSelectorController ]
      };

   }

   function CriticalityIconSelectorController( $scope) {
      
      this.i18n = $scope.i18n;

      this.openPopover = function(event) {
    	  this.popoverDirective.show(event);
      };
      
      this.closePopover = function() {
    	  this.popoverDirective.hide();
      };
      
      this.setIcon = function(icon) {
    	  $scope.bindModel = icon;
    	  this.closePopover();
      };
      
      $scope.criticalityIconSelectorCtrl = this;
   };
   
   /*
    * 
    */
	CriticalityIconSelectorController.prototype.getFlagIcons = function() {
		return [ 'RED', 'GREEN', 'BLUE', 'YELLOW', 'PURPLE', 'PINK', 'ORANGE' ];
	};
   
})();
