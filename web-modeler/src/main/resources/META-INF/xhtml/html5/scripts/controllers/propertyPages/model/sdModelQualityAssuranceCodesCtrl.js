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
          'sdModelQualityAssuranceCodesCtrl',
          ['$scope', 'sdUtilService', 'sdI18nService', 'sdModelerConstants',
              ModelQualityAssuranceCodesCtrl]);

  /**
   * 
   */
  function ModelQualityAssuranceCodesCtrl($scope, sdUtilService, sdI18nService,
          sdModelerConstants) {
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
    ModelQualityAssuranceCodesCtrl.prototype.safeApply = function() {
      sdUtilService.safeApply($scope);
    }

    /**
     * 
     */
    ModelQualityAssuranceCodesCtrl.prototype.refresh = function() {
      this.qaCodes = this.getModel().qualityAssuranceCodes;

      if (!this.qaCodes) {
        this.qaCodes = [];
      }
    }
    /**
     * 
     */
    ModelQualityAssuranceCodesCtrl.prototype.addQACode = function() {
      this.tableRowIndex = this.qaCodes.length;

      var qaCodeName = this.generateQACodeName();

      // TODO remove hard coding
      var qaCode = {
        id: qaCodeName.replace(/\s+/g, ""),
        name: qaCodeName,
        description: ""
      }

      this.propertiesPanel.submitAddQACode(qaCode);

      // TODO: remove
      this.qaCodes.push(qaCode);
      this.setSelected(this.tableRowIndex);

      // TODO: remove
      qaCode.uuid = Math.random();
      this.getModel().qualityAssuranceCodes = this.qaCodes;
    }

    /**
     * 
     */
    ModelQualityAssuranceCodesCtrl.prototype.deleteQACode = function() {
      var qaCodeTobeDeltd = this.qaCodes[this.tableRowIndex];

      this.qaCodes.splice(this.tableRowIndex, 1);
      this.tableRowIndex > 0 ? this.tableRowIndex-- : this.tableRowIndex = 0;

      this.setSelected(this.tableRowIndex);

      // TODO submit the request to engine
      this.propertiesPanel.submitDeleteQACode({
        "uuid": qaCodeTobeDeltd.uuid
      });
    }

    /**
     * 
     */
    ModelQualityAssuranceCodesCtrl.prototype.moveUpQACode = function() {
      if (this.tableRowIndex < 1) { return; }
      var tmp = this.qaCodes[this.tableRowIndex];
      this.qaCodes[this.tableRowIndex] = this.qaCodes[this.tableRowIndex - 1];
      this.qaCodes[this.tableRowIndex - 1] = tmp;
      this.tableRowIndex--;

      this.propertiesPanel.submitChangeswithUUID({
        "qualityAssuranceCodes": this.qaCodes
      });
    }

    /**
     * 
     */
    ModelQualityAssuranceCodesCtrl.prototype.moveDownQACode = function() {
      if (!(this.tableRowIndex < this.qaCodes.length)) { return; }
      var tmp = this.qaCodes[this.tableRowIndex];
      this.qaCodes[this.tableRowIndex] = this.qaCodes[this.tableRowIndex + 1];
      this.qaCodes[this.tableRowIndex + 1] = tmp;
      this.tableRowIndex++;

      this.propertiesPanel.submitChangeswithUUID({
        "qualityAssuranceCodes": this.qaCodes
      });
    }

    /**
     * @param name
     */
    ModelQualityAssuranceCodesCtrl.prototype.qaCodeChange = function(name) {
      if (name) {
        this.qaCodes[this.tableRowIndex].id = name.replace(/\s+/g, "");
      }

      // TODO: fire server request
      this.propertiesPanel.submitChangeswithUUID({
        "modelElement": this.qaCodes[this.tableRowIndex]
      }, this.qaCodes[this.tableRowIndex].uuid);
    }

    /**
     * @param index
     */
    ModelQualityAssuranceCodesCtrl.prototype.setSelected = function(index) {
      this.tableRowIndex = index;
      this.selectedQaCode = this.qaCodes[index];
    }

    /**
     * @returns
     */
    ModelQualityAssuranceCodesCtrl.prototype.getModel = function() {
      return this.propertiesPanel.model;
    }

    /**
     * 
     */
    ModelQualityAssuranceCodesCtrl.prototype.generateQACodeName = function() {
      var name = this.getModel().name + " " + i18n('modeler.propertyView.modelView.qualityAssuranceCodes.qaCodeSuffix');
      var id = name.replace(/\s+/g, "");

      var index = 1;
      var generateNew = true;
      while (generateNew) {
        generateNew = false;
        for (var i = 0; i < this.qaCodes.length; i++) {
          if (this.qaCodes[i].id.indexOf(id + index) > -1) {
            index++;
            generateNew = true;
            break;
          }
        }
      }
      return name + index;
    }
  }

})();