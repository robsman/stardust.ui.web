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
          'sdExcludedUserCtrl',
          ['$scope', 'sdUtilService', 'sdI18nService', 'sdModelerConstants',
              ExcludedUserCtrl]);

  /*
   * 
   */
  function ExcludedUserCtrl($scope, sdUtilService, sdI18nService,
          sdModelerConstants) {
    var self = this;
    self.initialized = false;
    self.display = false;

    $scope.sdI18nModeler = sdI18nService.getInstance('bpm-modeler-messages').translate;
    var i18n = $scope.sdI18nModeler;

    // TODO: find some other way to know the model element is initialized or
    // changed
    $scope.$on('PAGE_ELEMENT_CHANGED', function(event, page) {
      if (!self.initialized) {
        self.page = page;
        self.propertiesPanel = self.page.propertiesPanel;
        self.exclusionIndex = 0;
      }

      self.refresh();
      self.initialized = true;
    });

    ExcludedUserCtrl.prototype.safeApply = function() {
      sdUtilService.safeApply($scope);
    }

    /**
     * @returns
     */
    ExcludedUserCtrl.prototype.getDefaultData = function() {
      for ( var d in this.dataItems) {
        if (this.dataItems[d].id == sdModelerConstants.LAST_ACTIVITY_PERFORMER) { return this.dataItems[d]
                .getFullId(); }
      }
    }

    /**
     * 
     */
    ExcludedUserCtrl.prototype.refresh = function() {
      this.element = this.propertiesPanel.element;
      if (!this.element) { return; }

      this.modelElement = this.element.modelElement;
      this.dataItems = this.propertiesPanel.propertiesPage.getModel().dataItems;
      this.onAssignmentHandler = this.modelElement.onAssignmentHandler;

      this.exclusions = [];
      if (this.onAssignmentHandler && this.onAssignmentHandler.userExclusions) {
        this.exclusions = this.onAssignmentHandler.userExclusions
      }

      this.setSelected(this.exclusionIndex);

      this.logToAuditTrail = false;

      if (this.onAssignmentHandler && this.onAssignmentHandler.logHandler) {
        this.logToAuditTrail = this.onAssignmentHandler.logHandler
      }
    }
    /**
     * 
     */
    ExcludedUserCtrl.prototype.addExclusion = function() {

      this.exclusionIndex = this.exclusions.length;

      // submit changes
      this.propertiesPanel.submitCreateExclusion({
        "name": i18n('modeler.propertiesPage.activity.excludedUsers.exclude'),
        'data': this.getDefaultData(),
        'dataPath': null
      });
    }

    /**
     * 
     */
    ExcludedUserCtrl.prototype.deleteExclusion = function() {
      if (this.exclusionIndex > 0) {
        this.exclusionIndex--;
      } else {
        this.exclusionIndex = 0;
      }

      this.propertiesPanel.submitDeleteExclusion({
        "uuid": this.selectedExclusion.uuid
      })
    }

    /**
     * @param name
     */
    ExcludedUserCtrl.prototype.onNameChange = function(name) {
      this.propertiesPanel.submitUpdateExclusion(this.selectedExclusion.uuid, {
        'name': name
      });
    }

    /**
     * @param data
     * @param dataPath
     */
    ExcludedUserCtrl.prototype.updateExclusion = function(data, dataPath) {
      this.propertiesPanel.submitUpdateExclusion(this.selectedExclusion.uuid, {
        'data': data,
        'dataPath': dataPath
      });

    }

    /**
     * @param log
     */
    ExcludedUserCtrl.prototype.onLogToAuditTrailChange = function(log) {
      this.logToAuditTrail = log;
      this.propertiesPanel.submitUpdateExclusion(this.onAssignmentHandler.uuid,
              {
                'logHandler': log
              });
    }

    /**
     * @param index
     */
    ExcludedUserCtrl.prototype.setSelected = function(index) {
      this.exclusionIndex = index;

      this.selectedExclusion = this.exclusions[index];
      this.display = false;
      this.safeApply();
      var self = this;

      if (this.selectedExclusion) {
        window.setTimeout(function() {
          self.display = true;
          self.safeApply();
        })
      }
    }

    /**
     * @param exclusion
     * @returns {String}
     */
    ExcludedUserCtrl.prototype.getDatapathStr = function(exclusion) {
      var data = this.getData(exclusion.data);

      if (!data) {
        console.error("data not found....");
        data = "";
      }
      if (exclusion.dataPath) { return (data.name + "/" + exclusion.dataPath); }
      return data.name;
    }

    /**
     * @returns
     */
    ExcludedUserCtrl.prototype.getModel = function() {
      return this.propertiesPanel.propertiesPage.getModel();
    }

    /**
     * @param datafullId
     * @returns
     */
    ExcludedUserCtrl.prototype.getData = function(dataFullId) {
      for ( var d in this.dataItems) {
        if (this.dataItems[d].getFullId() == dataFullId) { return this.dataItems[d]; }
      }
      return null;
    }
  }
})();