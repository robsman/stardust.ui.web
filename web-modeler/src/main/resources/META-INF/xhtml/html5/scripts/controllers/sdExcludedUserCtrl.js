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
          ['$scope', '$timeout', 'sdRequireJSService', 'sdUtilService',
              ExcludedUserCtrl]);

  /*
   * 
   */
  function ExcludedUserCtrl($scope, $timeout, sdRequireJSService, sdUtilService) {
    var self = this;
    self.initialized = false;
    self.display = false;

    // load requireJs modules
    var promise = sdRequireJSService.getPromise();
    promise.then(function() {
      self.m_utils = sdRequireJSService
              .getModule('plugins/bpm-modeler/js/m_utils');
      self.m_i18nUtils = sdRequireJSService
              .getModule('plugins/bpm-modeler/js/m_i18nUtils');
      self.m_constants = sdRequireJSService
              .getModule('plugins/bpm-modeler/js/m_constants');
      self.m_commandsController = sdRequireJSService
              .getModule('plugins/bpm-modeler/js/m_commandsController');
      self.m_command = sdRequireJSService
              .getModule('plugins/bpm-modeler/js/m_command');
    }, function() {
      console.error("exception occurred while loading requirejs modules")
    });

    $scope.$on('PAGE_ELEMENT_CHANGED', function(event, page) {
      if (!self.initialized) {
        // generic
        self.page = page;
        self.propertiesPanel = self.page.propertiesPanel;

        // Excluded User Page specific
        self.exclusionIndex = 0;

      }
      // Excluded User Page specific
      self.reset();
      self.initialized = true;
    });

    ExcludedUserCtrl.prototype.safeApply = function() {
      sdUtilService.safeApply($scope);
    }
  }

  /**
   * 
   */
  ExcludedUserCtrl.prototype.getProperty = function(key, param1, param2) {
    var value = this.m_i18nUtils.getProperty(key);
    if (param1) {
      value = value.replace('{0}', param1);
    }
    if (param2) {
      value = value.replace('{1}', param2);
    }
    return value;
  }

  /**
   * 
   */
  ExcludedUserCtrl.prototype.reset = function() {
    this.element = this.propertiesPanel.element;
    if (!this.element) { return; }

    this.modelElement = this.element.modelElement;
    this.dataItems = this.propertiesPanel.propertiesPage.getModel().dataItems;
    this.onAssignmentHandler = this.modelElement.onAssignmentHandler;

    // TODO : remove following code post server side implementation - start
    if (!this.onAssignmentHandler) {
      this.onAssignmentHandler = {
        "uuid": "00000000-0000-0000-0000-000000000087",
        "logHandler": false,
        "userExclusions": [{
          "uuid": "00000000-0000-0000-0000-000000000088",
          "oid": 41,
          "name": "User A excluded",
          "type": "eventAction",
          "data": this.getDefaultData(),
          "dataPath": null
        }, {
          "uuid": "00000000-0000-0000-0000-000000000089",
          "oid": 42,
          "name": "User B excluded",
          "type": "eventAction",
          "data": this.getDefaultData(),
          "dataPath": null
        }]
      }
    }
    // TODO : remove following code post server side implementation - end

    this.exclusions = this.onAssignmentHandler.userExclusions
    this.setSelected(this.exclusionIndex);
  }

  /**
   * 
   */
  ExcludedUserCtrl.prototype.addExclusion = function() {

    // TODO: remove this code later
    this.exclusions
            .push({
              'uuid': "00000000-0000-0000-0000-userExclusion"
                      + this.exclusionIndex,
              'name': this
                      .getProperty('modeler.propertiesPage.activity.excludedUsers.exclude'),
              'data': this.getDefaultData(),
              'dataPath': null
            })

    // TODO remove -1
    this.exclusionIndex = this.exclusions.length - 1;
    // TODO remove following line
    this.setSelected(this.exclusionIndex);

    // submit changes
    this.submitCreateExclusion({
              "name": this
                      .getProperty('modeler.propertiesPage.activity.excludedUsers.exclude'),
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

    this.submitDeleteExclusion({
      "uuid": this.selectedExclusion.uuid
    })

    // TODO: remove all following code
    if (this.exclusionIndex > -1) {
      this.exclusions.splice(this.exclusionIndex, 1);
    }
    if (this.exclusionIndex > 0) {
      this.exclusionIndex--;
    } else {
      this.exclusionIndex = 0;
    }
    this.setSelected(this.exclusionIndex)
  }

  /**
   * @param name
   */
  ExcludedUserCtrl.prototype.onNameChange = function(name) {
    this.submitUpdateExclusion(this.selectedExclusion.uuid, {
      'name': name
    });
  }

  /**
   * @param data
   * @param dataPath
   */
  ExcludedUserCtrl.prototype.updateExclusion = function(data, dataPath) {
    this.submitUpdateExclusion(this.selectedExclusion.uuid, {
      'data': data,
      'dataPath': dataPath
    });
  }

  /**
   * @param log
   */
  ExcludedUserCtrl.prototype.onLogToAuditTrailChange = function(log) {
    this.submitUpdateExclusion(this.onAssignmentHandler.uuid, {
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

  /**
   * @returns
   */
  ExcludedUserCtrl.prototype.getDefaultData = function() {
    for ( var d in this.dataItems) {
      if (this.dataItems[d].id == this.m_constants.LAST_ACTIVITY_PERFORMER) { return this.dataItems[d]
              .getFullId(); }
    }
  }

  // server interaction
  /**
   * exclusion.uuid or onAssignmentHandler.uuid
   */
  ExcludedUserCtrl.prototype.submitUpdateExclusion = function(uuid, changes) {
    this.m_commandsController.submitCommand(this.m_command
            .createUpdateModelElementWithUUIDCommand(this.getModel().id, uuid,
                    {
                      'modelElement': changes
                    }));
  }

  /**
   * @param changes
   */
  ExcludedUserCtrl.prototype.submitCreateExclusion = function(changes) {
    this.m_commandsController.submitCommand(this.m_command
            .createAddExclusionCommand(this.getModel().id,
                    this.modelElement.uuid, changes));
  }

  /**
   * @param changes
   */
  ExcludedUserCtrl.prototype.submitDeleteExclusion = function(changes) {
    this.m_commandsController.submitCommand(this.m_command
            .createDeleteExclusionCommand(this.getModel().id,
                    this.modelElement.uuid, changes));
  }

})();