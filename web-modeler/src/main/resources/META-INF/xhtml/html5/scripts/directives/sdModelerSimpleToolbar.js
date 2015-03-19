/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

/**
 * @author Yogesh.Manware
 */

(function() {
  'use strict';

  angular.module('modeler-ui').directive('sdModelerSimpleToolbar',
          [ModelerSimpleToolbar]);

  function ModelerSimpleToolbar() {

    return {
      restrict: 'E',
      replace: true,
      scope: {
        addItem: '&sdaAddItem',
        deleteItem: '&sdaDeleteItem',
        // optional
        moveDown: '&sdaMoveDown',
        moveUp: '&sdaMoveUp',
      },
      controller: ['$scope', 'sdRequireJSService', '$attrs',
          ModelerSimpleToolbarCtrl],
      template: '<div class="toolBar" ng-if="toolbarCtrl.initialized">\
        <table>\
        <tr>\
          <td><input type="image" aid="add" ng-src="plugins/bpm-modeler/images/icons/add.png"\
            title="{{toolbarCtrl.addLabel}}" alt="{{toolbarCtrl.addLabel}}" class="toolbarButton" ng-click="addItem()" /></td>\
          <td><img ng-src="plugins/bpm-modeler/images/icons/toolbar-separator.png"></img></td>\
          <td><input type="image" aid="delete" ng-src="plugins/bpm-modeler/images/icons/delete.png"\
            title="{{toolbarCtrl.deleteLabel}}" alt="{{toolbarCtrl.deleteLabel}}" class="toolbarButton"\
            ng-click="deleteItem()" /></td>\
          <td ng-if="toolbarCtrl.showOrderCtrl"><img ng-src="plugins/bpm-modeler/images/icons/toolbar-separator.png"></img></td>\
          <td ng-if="toolbarCtrl.showOrderCtrl"><input type="image" aid="moveUp"\
            src="plugins/bpm-modeler/images/icons/arrow_up.png" title="{{toolbarCtrl.moveUpLabel}}" alt="{{toolbarCtrl.moveUpLabel}}" class="toolbarButton"\
            ng-click="moveUp()" /></td>\
          <td ng-if="toolbarCtrl.showOrderCtrl"><img ng-src="plugins/bpm-modeler/images/icons/toolbar-separator.png"></img></td>\
          <td ng-if="toolbarCtrl.showOrderCtrl"><input type="image" aid="moveDown"\
            src="plugins/bpm-modeler/images/icons/arrow_down.png" title="{{toolbarCtrl.moveDownLabel}}" alt="{{toolbarCtrl.moveDownLabel}}"\
            class="toolbarButton" ng-click="moveDown()" /></td>\
        </tr>\
      </table>\
    </div>'
    }
  }

  /**
   * 
   */
  var ModelerSimpleToolbarCtrl = function($scope, sdRequireJSService, $attrs) {
    var self = this;

    var defPromise = sdRequireJSService.getPromise();
    defPromise.then(function() {
      self.m_i18nUtils = sdRequireJSService
              .getModule('plugins/bpm-modeler/js/m_i18nUtils');
      self.initialize($scope, $attrs);
      $scope.toolbarCtrl = self;
      self.initialized = true;
    }, function() {
      console.error("failed to load m_I18nUtils");
    });
  }

  /**
   * 
   */
  ModelerSimpleToolbarCtrl.prototype.initialize = function($scope, $attrs) {
    this.showOrderCtrl = false;
    if (angular.isDefined($attrs.sdaMoveUp)) {
      this.showOrderCtrl = true;
    }
    this.addLabel = this.m_i18nUtils
            .getProperty('modeler.element.properties.commonProperties.add');
    this.deleteLabel = this.m_i18nUtils
            .getProperty('modeler.element.properties.commonProperties.delete');
    this.moveDownLabel = this.m_i18nUtils
            .getProperty('modeler.element.properties.commonProperties.moveDown');
    this.moveUpLabel = this.m_i18nUtils
            .getProperty('modeler.element.properties.commonProperties.moveUp');
  }

})();