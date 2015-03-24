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
          ['$scope', 'sdRequireJSService', 'sdUtilService',
              ActivityImplementationSubProcessCtrl]);

  /*
   * 
   */
  function ActivityImplementationSubProcessCtrl($scope, sdRequireJSService,
          sdUtilService) {
    var self = this;
    self.initialized = false;

    // load requireJs modules, in future these would be services
    var promise = sdRequireJSService.getPromise();
    promise.then(function() {
      self.m_utils = sdRequireJSService
              .getModule('plugins/bpm-modeler/js/m_utils');
      self.m_i18nUtils = sdRequireJSService
              .getModule('plugins/bpm-modeler/js/m_i18nUtils');
      self.m_constants = sdRequireJSService
              .getModule('plugins/bpm-modeler/js/m_constants');
      self.m_modelElementUtils = sdRequireJSService
              .getModule('plugins/bpm-modeler/js/m_modelElementUtils');
      self.m_model = sdRequireJSService
              .getModule('plugins/bpm-modeler/js/m_model');

    }, function() {
      console.error("exception occurred while loading requirejs modules")
    });

    $scope
            .$on(
                    'PAGE_ELEMENT_CHANGED',
                    function(event, page) {
                      if (!self.initialized) {
                        // generic
                        self.page = page;
                        self.propertiesPanel = self.page.propertiesPanel;

                        // Activity Implementation Page specific
                        self.initSubProcessModeList();
                      }

                      // Activity Implementation Page specific
                      if (self.propertiesPanel.element.modelElement) {
                        if (self.propertiesPanel.element.modelElement.activityType == self.m_constants.SUBPROCESS_ACTIVITY_TYPE) {
                          self.show = true;
                        } else {
                          self.show = false;
                          return;
                        }
                      }

                      self.reset();
                      self.initialized = true;
                    });

    ActivityImplementationSubProcessCtrl.prototype.safeApply = function() {
      sdUtilService.safeApply($scope);
    }
  }

  /**
   * 
   */
  ActivityImplementationSubProcessCtrl.prototype.i18n = function(key, params) {
    var value = this.m_i18nUtils.getProperty(key);
    return value;
  }

  /**
   * 
   */
  ActivityImplementationSubProcessCtrl.prototype.reset = function() {
    this.element = this.propertiesPanel.element;
    if (!this.element) { return; }

    this.modelElement = this.element.modelElement;

    this.dataItems = this.propertiesPanel.propertiesPage.getModel().dataItems;

    this.populateSubProcessList();

    this.subProcess = !this.modelElement.subprocessFullId
            ? this.m_constants.TO_BE_DEFINED
            : this.modelElement.subprocessFullId;

    this.subProcessMode = this.modelElement.subprocessMode;
    if (!this.subProcessMode) {
      this.subProcessMode = "synchShared";
    }

    this.copyAllData = this.modelElement.attributes["carnot:engine:subprocess:copyAllData"];

    this.runtimeBindingData = this.modelElement.attributes["carnot:engine:data"];
    if (!this.runtimeBindingData) {
      this.runtimeBindingData = this.m_constants.TO_BE_DEFINED;
    }
    this.runtimeBindingDataPath = this.modelElement.attributes["carnot:engine:dataPath"];
  }

  /**
   * 
   */
  ActivityImplementationSubProcessCtrl.prototype.populateSubProcessList = function() {
    this.subProcessList = [];

    this.subProcessList.push({
      fullId: this.m_constants.TO_BE_DEFINED,
      label: this.i18n("modeler.general.toBeDefined")
    })

    var processesSorted = this.m_utils.convertToSortedArray(
            this.getModel().processes, "name", true);

    if (processesSorted) {
      var modelName = this.i18n("modeler.general.thisModel");

      for ( var i in processesSorted) {
        this.subProcessList.push({
          fullId: processesSorted[i].getFullId(),
          label: processesSorted[i].name,
          group: modelName
        })
      }
    }

    modelName = this.i18n("modeler.general.otherModels");

    var modelsSorted = this.m_utils.convertToSortedArray(this.m_model
            .getModels(), "name", true);

    for ( var n in modelsSorted) {
      if (modelsSorted[n] == this.getModel()) {
        continue;
      }

      processesSorted = this.m_utils.convertToSortedArray(
              modelsSorted[n].processes, "name", true);
      for ( var m in processesSorted) {
        if (!(processesSorted[m].processInterfaceType === this.m_constants.NO_PROCESS_INTERFACE_KEY)) {
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
              label: this
                      .i18n("modeler.activity.propertyPages.controlling.executionMode.options.synchShared")
            });

    this.subProcessModeList
            .push({
              id: 'synchSeparate',
              label: this
                      .i18n("modeler.activity.propertyPages.controlling.executionMode.options.synchSeparate")
            });
    this.subProcessModeList
            .push({
              id: 'asynchSeparate',
              label: this
                      .i18n("modeler.activity.propertyPages.controlling.executionMode.options.asynchSeparate")
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
  ActivityImplementationSubProcessCtrl.prototype.getData = function(dataFullId) {
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
      if (this.dataItems[d].id == this.m_constants.LAST_ACTIVITY_PERFORMER) { return this.dataItems[d]
              .getFullId(); }
    }
  }

  // Server Interaction
  /**
   * 
   */
  ActivityImplementationSubProcessCtrl.prototype.submitImplementionChanges = function() {
    var attributes = {};

    attributes["carnot:engine:dataId"] = this.runtimeBindingData == this.m_constants.TO_BE_DEFINED
            ? null : this.runtimeBindingData;
    attributes["carnot:engine:dataPath"] = !this.runtimeBindingDataPath ? null
            : this.runtimeBindingDataPath;
    attributes["carnot:engine:dataId"] = this.runtimeBindingData == this.m_constants.TO_BE_DEFINED
            ? null : this.runtimeBindingData;
    
    attributes["carnot:engine:subprocess:copyAllData"] = this.copyAllData;

    var submitObj = {
      modelElement: {
        activityType: this.m_constants.SUBPROCESS_ACTIVITY_TYPE,
        subprocessFullId: this.subProcess == this.m_constants.TO_BE_DEFINED
                ? null : this.subProcess,
        subprocessMode: this.subProcessMode,
        attributes: attributes
      }
    };

    this.page.submitChanges(submitObj);
  }
})();