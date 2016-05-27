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

   angular.module('workflow-ui').directive('sdCriticalityIconSelector', [ CriticalityIconSelector ]);

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
          '<button class="button-link" aid="CritIconSelector">' +
           '<i class="pi pi-flag pi-lg" ng-show="editMode == true" sd-popover sda-template="\'criticalitySelector.html\'" '+
              'sda-trigger="outsideClick" sda-is-open="criticalityIconSelectorCtrl.popovers.visible" '+
              'sda-placement="right auto" aid="CritIconSelector-SelectedFlg"' +
              'ng-class="\'criticality-flag-\'+ (bindModel != undefined? bindModel : \'NO-COLOR\')"><\/i>  ' +
            '<i class="pi pi-flag pi-lg"  ng-show="editMode != true"' +
              'ng-class="\'criticality-flag-\'+ (bindModel != undefined? bindModel : \'NO-COLOR\')"><\/i>  ' +
          '</button>' +
          '<script id="criticalitySelector.html" type="text/ng-template">' +
              '<div style="float: left;width: 105px;">' +
                '<button ng-click="criticalityIconSelectorCtrl.setIcon(icon)" '+
                  'class="button-link tbl-tool-link" ng-repeat="icon in criticalityIconSelectorCtrl.getFlagIcons()" '+
                  ' aid="CritIconSelector-IconOptions">' +
                    '<i class="pi pi-flag pi-lg" ' +
                      'ng-class="\'criticality-flag-\'+icon"><\/i>  ' +
                   '<\/button>' +
               '</div>' +
          '</script>',

		 scope : {
			 bindModel:'=ngModel',
			 editMode:'=sdaEditMode'
		 },
         controller : [ '$scope', CriticalityIconSelectorController ]
      };

   }

   function CriticalityIconSelectorController( $scope) {

      this.i18n = $scope.i18n;
      this.popovers = {
        visible : false
      }

      this.openPopover = function(event) {
    	  this.popovers.visible = true;
      };

      this.closePopover = function() {
    	  this.popovers.visible = false;
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
