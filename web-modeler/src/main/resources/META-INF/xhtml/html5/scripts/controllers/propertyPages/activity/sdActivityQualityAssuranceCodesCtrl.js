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

  angular.module('modeler-ui').controller(
          'sdActivityQualityAssuranceCodesCtrl',
          ['$scope', 'sdUtilService', 'sdI18nService', 'sdModelerConstants',
              ActivityQualityAssuranceCodesCtrl]);

  /**
   * 
   */
  function ActivityQualityAssuranceCodesCtrl($scope, sdUtilService,
          sdI18nService, sdModelerConstants) {
    this.initialized = false;

    var self = this;
    $scope.sdI18nModeler = sdI18nService.getInstance('bpm-modeler-messages').translate;
    var i18n = $scope.sdI18nModeler;

    // TODO: find some other way to know the model element is initialized or
    // changed
    $scope.$on('REFRESH_PROPERTIES_PANEL', function(event, propertiesPanel) {
      if (!self.initialized) {
        self.tableRowIndex = 0;
      }
      self.propertiesPanel = propertiesPanel;

      self.refresh();
      self.initialized = true;
    });

    /**
     * 
     */
    ActivityQualityAssuranceCodesCtrl.prototype.safeApply = function() {
      sdUtilService.safeApply($scope);
    }

    /**
     * 
     */
    ActivityQualityAssuranceCodesCtrl.prototype.refresh = function() {
      this.element = this.propertiesPanel.element;
      if (!this.element) { return; }

      this.modelElement = this.element.modelElement;

      this.qaCodes = angular.copy(this.getModel().qualityAssuranceCodes);

      if (!this.qaCodes) {
        this.qaCodes = [];
      }

      // set selection from activity model element
      this.selectedQaCodes = this.element.modelElement.selectedQaCodes;
      if (!this.selectedQaCodes) {
        this.selectedQaCodes = [];
      }
      for (var i = 0; i < this.qaCodes.length; i++) {
        for (var k = 0; k < this.selectedQaCodes.length; k++) {
          if (this.selectedQaCodes[k] == this.qaCodes[i].uuid) {
            this.qaCodes[i].selected = true;
          }
        }
      }
    }

    /**
     * @param name
     */
    ActivityQualityAssuranceCodesCtrl.prototype.qaCodeselected = function(index) {
      if (this.qaCodes[index].selected) {
        this.selectedQaCodes.push(this.qaCodes[index].uuid);
      } else {
        // delete it from the array
        var ind = -1;
        for (var i = 0; i < this.selectedQaCodes.length; i++) {
          if (this.selectedQaCodes[i] == this.qaCodes[index].uuid) {
            ind = i;
            break;
          }
        }
        if (ind > -1) {
          this.selectedQaCodes.splice(ind, 1);
        }
      }

      this.propertiesPanel.submitChangesWithUUID({
        "qualityAssuranceCodes": this.selectedQaCodes
      });
    }

    /**
     * @param index
     */
    ActivityQualityAssuranceCodesCtrl.prototype.setSelected = function(index) {
      this.tableRowIndex = index;
      this.selectedQaCode = this.qaCodes[index];
    }

    /**
     * @returns
     */
    ActivityQualityAssuranceCodesCtrl.prototype.getModel = function() {
      return this.propertiesPanel.propertiesPage.getModel();
    }
  }
})();