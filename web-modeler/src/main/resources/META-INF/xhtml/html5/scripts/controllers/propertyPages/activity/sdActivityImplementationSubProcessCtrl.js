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
          'sdActivityImplementationSubProcessCtrl',
          ['$scope', 'sdUtilService', 'sdI18nService', 'sdModelerConstants',
              ActivityImplementationSubProcessCtrl]);

  /*
   * 
   */
  function ActivityImplementationSubProcessCtrl($scope, sdUtilService,
          sdI18nService, sdModelerConstants) {
    var self = this;
    self.initialized = false;

    $scope.sdI18nModeler = sdI18nService.getInstance('bpm-modeler-messages').translate;
    var i18n = $scope.sdI18nModeler;

    $scope
    .$on(
            'REFRESH_PROPERTIES_PANEL',
            function(event, propertiesPanel) {
                      if (!self.initialized) {
                        self.propertiesPanel = propertiesPanel;
                        self.initSubProcessModeList();
                        self.initialized = true;
                      }

                      if (self.propertiesPanel.element.modelElement) {
                        if (self.propertiesPanel.element.modelElement.activityType == sdModelerConstants.SUBPROCESS_ACTIVITY_TYPE) {
                          self.show = true;
                        } else {
                          self.show = false;
                          return;
                        }
                      }
                      self.refresh();
                    });
    /**
     * 
     */
    ActivityImplementationSubProcessCtrl.prototype.refresh = function() {
      this.element = this.propertiesPanel.element;
      if (!this.element) { return; }

      this.modelElement = this.element.modelElement;

      this.dataItems = this.propertiesPanel.propertiesPage.getModel().dataItems;

      this.populateSubProcessList();

      this.subProcess = !this.modelElement.subprocessFullId
              ? sdModelerConstants.TO_BE_DEFINED
              : this.modelElement.subprocessFullId;

      this.subProcessMode = this.modelElement.subprocessMode;
      if (!this.subProcessMode) {
        this.subProcessMode = "synchShared";
      }

      this.copyAllData = this.modelElement.attributes["carnot:engine:subprocess:copyAllData"];

      this.runtimeBindingData = this.modelElement.attributes["carnot:engine:dataId"];
      if (!this.runtimeBindingData) {
        this.runtimeBindingData = sdModelerConstants.TO_BE_DEFINED;
      }
      this.runtimeBindingDataPath = this.modelElement.attributes["carnot:engine:dataPath"];
    }

    /**
     * 
     */
    ActivityImplementationSubProcessCtrl.prototype.populateSubProcessList = function() {
      this.subProcessList = [];

      this.subProcessList.push({
        fullId: sdModelerConstants.TO_BE_DEFINED,
        label: i18n("modeler.general.toBeDefined")
      })

      var processesSorted = sdUtilService.convertToSortedArray(
              this.getModel().processes, "name", true);

      if (processesSorted) {
        var modelName = i18n("modeler.general.thisModel");

        for ( var i in processesSorted) {
          this.subProcessList.push({
            fullId: processesSorted[i].getFullId(),
            label: processesSorted[i].name,
            group: modelName
          })
        }
      }

      modelName = i18n("modeler.general.otherModels");

      var modelsSorted = sdUtilService.convertToSortedArray(this.propertiesPanel
              .getModels(), "name", true);

      for ( var n in modelsSorted) {
        if (modelsSorted[n] == this.getModel()) {
          continue;
        }

        processesSorted = sdUtilService.convertToSortedArray(
                modelsSorted[n].processes, "name", true);
        for ( var m in processesSorted) {
          if (!(processesSorted[m].processInterfaceType === sdModelerConstants.NO_PROCESS_INTERFACE_KEY)) {
            this.subProcessList.push({
              fullId: processesSorted[m].getFullId(),
              label: modelsSorted[n].name + "/" + processesSorted[m].name,
              group: modelName
            })
          }
        }
      }
    }
    /**
     * 
     */
    ActivityImplementationSubProcessCtrl.prototype.initSubProcessModeList = function() {
      this.subProcessModeList = [];

      this.subProcessModeList
              .push({
                id: 'synchShared',
                label: i18n("modeler.activity.propertyPages.controlling.executionMode.options.synchShared")
              });

      this.subProcessModeList
              .push({
                id: 'synchSeparate',
                label: i18n("modeler.activity.propertyPages.controlling.executionMode.options.synchSeparate")
              });
      this.subProcessModeList
              .push({
                id: 'asynchSeparate',
                label: i18n("modeler.activity.propertyPages.controlling.executionMode.options.asynchSeparate")
              });
    };

    /**
     * @returns {Boolean}
     */
    ActivityImplementationSubProcessCtrl.prototype.isCopyDataDisabled = function() {
      if (this.subProcessMode == "synchShared") {
        this.copyAllData = false;
        return true;
      }
      return false;
    }

    /**
     * 
     */
    ActivityImplementationSubProcessCtrl.prototype.onImplementationChange = function() {
      this.submitImplementionChanges();
    }

    /**
     * @param data
     * @param dataPath
     */
    ActivityImplementationSubProcessCtrl.prototype.updateRuntimeBinding = function(
            data, dataPath) {

      this.runtimeBindingData = data;
      this.runtimeBindingDataPath = dataPath;

      this.submitImplementionChanges();
    }

    /**
     * @returns
     */
    ActivityImplementationSubProcessCtrl.prototype.getModel = function() {
      return this.propertiesPanel.propertiesPage.getModel();
    }
    /**
     * @param datafullId
     * @returns
     */
    ActivityImplementationSubProcessCtrl.prototype.getData = function(
            dataFullId) {
      for ( var d in this.dataItems) {
        if (this.dataItems[d].getFullId() == dataFullId) { return this.dataItems[d]; }
      }
      return null;
    }

    /**
     * @returns
     */
    ActivityImplementationSubProcessCtrl.prototype.getDefaultData = function() {
      for ( var d in this.dataItems) {
        if (this.dataItems[d].id == sdModelerConstants.LAST_ACTIVITY_PERFORMER) { return this.dataItems[d]
                .getFullId(); }
      }
    }

    // Server Interaction
    /**
     * 
     */
    ActivityImplementationSubProcessCtrl.prototype.submitImplementionChanges = function() {
      var attributes = {};

      attributes["carnot:engine:dataId"] = this.runtimeBindingData == sdModelerConstants.TO_BE_DEFINED
              ? null : this.runtimeBindingData;
      attributes["carnot:engine:dataPath"] = !this.runtimeBindingDataPath
              ? null : this.runtimeBindingDataPath;
      attributes["carnot:engine:dataId"] = this.runtimeBindingData == sdModelerConstants.TO_BE_DEFINED
              ? null : this.runtimeBindingData;

      attributes["carnot:engine:subprocess:copyAllData"] = this.copyAllData;

      var submitObj = {
        modelElement: {
          activityType: sdModelerConstants.SUBPROCESS_ACTIVITY_TYPE,
          subprocessFullId: this.subProcess == sdModelerConstants.TO_BE_DEFINED
                  ? null : this.subProcess,
          subprocessMode: this.subProcessMode,
          attributes: attributes
        }
      };

      this.propertiesPanel.submitChanges(submitObj);
    }
  }
})();