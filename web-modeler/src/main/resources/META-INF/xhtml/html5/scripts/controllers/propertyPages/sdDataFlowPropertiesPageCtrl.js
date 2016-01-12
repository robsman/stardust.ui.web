/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
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
          'sdDataFlowPropertiesPageCtrl',
          ['$scope', 'sdModelerConstants', 'sdUtilService', 'sdLoggerService',
              'sdI18nService', 'sdMessageService', DataFlowPropertiesPageCtrl]);

  /*
   * 
   */
  function DataFlowPropertiesPageCtrl($scope, constants, sdUtilService,
          sdLoggerService, sdI18nService, sdMessageService) {
    var self = this;
    
    this.showMessage = false;
    
    self.initialized = false;
    var trace = sdLoggerService
            .getLogger('modeler-ui.sdDataFlowPropertiesPageCtrl');
    $scope.sdI18nModeler = sdI18nService.getInstance('bpm-modeler-messages').translate;
    var i18n = $scope.sdI18nModeler;

    $scope
            .$on(
                    'REFRESH_PROPERTIES_PANEL',
                    function(event, propertiesPanel, initialize) {
                      if (!self.initialized) {
                        self.dataMappingIndex = 0;
                      } else {
                        if (!initialize
                                && (self.propertiesPanel.diagram.process.uuid != propertiesPanel.diagram.process.uuid)) { return; }
                      }
                      self.propertiesPanel = propertiesPanel;
                      self.refresh();
                      self.initialized = true;
                    });

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.refresh = function() {
      this.element = this.propertiesPanel.element;
      if (!this.element) { return; }

      this.modelElement = this.element.modelElement;
      this.unifiedDataMappings = transformDMs(this.modelElement.dataMappings);
      this.unifiedDataMappings = sdUtilService.convertToSortedArray(
              this.unifiedDataMappings, "id", true);

      if (this.unifiedDataMappings.length == 0) { return; }

      // determine previously selected index of data mapping in case id is
      // modified
      if (this.dataMappingIndex == undefined) {
        this.dataMappingIndex = this.determineIndex();
      }

      this.setSelected(this.dataMappingIndex);

      if (this.propertiesPanel.getCurrentRole() != constants.INTEGRATOR_ROLE) {
        this.integrator = false;
      } else {
        this.integrator = true;
      }
    }

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.populateInputAccessPointSelectInput = function() {

      var inMapping = this.selectedDataMapping.inMapping;

      this.inputAccessPoints = [];

      if (this.modelElement.activity.hasInputAccessPoints()) {
        this.inputAccessPointSelectInputPanel = true;
      } else {
        this.inputAccessPointSelectInputPanel = false;
      }

      var defaultVal;

      if (this.modelElement.activity.taskType != constants.TASK_ACTIVITY_TYPE) {
        defaultVal = {
          id: "DEFAULT",
          label: i18n("modeler.general.defaultLiteral"),
          context: ""
        }
      } else {
        defaultVal = {
          id: "DEFAULT",
          label: i18n("modeler.general.toBeDefined"),
          context: ""
        }
      }
      this.inputAccessPoints.push(defaultVal);
      for ( var i in this.modelElement.activity.getContexts()) {
        var context = this.modelElement.activity.getContexts()[i];

        trace.debug("i = " + i);
        trace.debug(context);

        var group = i18n("modeler.dataFlow.propertiesPanel.outputAccessPointSelectInput.group."
                + i)

        for (var m = 0; m < context.accessPoints.length; ++m) {
          var accessPoint = context.accessPoints[m];

          if (accessPoint.direction == constants.OUT_ACCESS_POINT) {
            continue;
          }
          
          // Filter out accesspoints where for decorator application where default value is set
          if (accessPoint.attributes && accessPoint.attributes['carnot:engine:defaultValue']) {
        	  continue;
          }
          
          var label;
          if (accessPoint.isUsedAsList) {
            label = accessPoint.name
                    + " ("
                    + i18n("modeler.general.multiInstanceActivity.accesspoint.name.listSuffix")
                    + ")";
          } else {
            label = accessPoint.name;
          }

          this.inputAccessPoints.push({
            id: accessPoint.id,
            context: i,
            label: label,
            group: group
          })

        }
      }
      this.populateEngineAccessPoints(this.inputAccessPoints);
      this.populateRulesInAccesspoints(this.inputAccessPoints);

      if (inMapping) {
        if (inMapping.accessPointId == null) {
          this.inputAccessPointVal = defaultVal;
          this.inputAPPathDisabled = true;
        } else {
          this.inputAccessPointVal = findObject("id", "context",
                  inMapping.accessPointId, inMapping.accessPointContext,
                  this.inputAccessPoints);

          this.inputAPPathDisabled = false;
        }
      }
    };

    /**
     *
     */
    DataFlowPropertiesPageCtrl.prototype.populateOutputAccessPointSelectInput = function() {

      var outMapping = this.selectedDataMapping.outMapping;

      this.outputAccessPoints = [];

      if (this.modelElement.activity.hasOutputAccessPoints()) {
        this.outputAccessPointSelectInputPanel = true;
      } else {
        this.outputAccessPointSelectInputPanel = false;
      }

      var defaultVal;

      if (this.modelElement.activity.taskType != constants.TASK_ACTIVITY_TYPE) {
        defaultVal = {
          id: 'DEFAULT',
          label: i18n("modeler.general.defaultLiteral")
        };
      } else {
        defaultVal = {
          id: 'DEFAULT',
          label: +i18n("modeler.general.toBeDefined")
        };
      }

      this.outputAccessPoints.push(defaultVal);

      for ( var i in this.modelElement.activity.getContexts()) {
        var context = this.modelElement.activity.getContexts()[i];

        var group = i18n("modeler.dataFlow.propertiesPanel.outputAccessPointSelectInput.group."
                + i);

        for (var m = 0; m < context.accessPoints.length; ++m) {
          var accessPoint = context.accessPoints[m];

          if (accessPoint.direction == constants.IN_ACCESS_POINT) {
            continue;
          }

          var label;
          if (accessPoint.isUsedAsList) {
            label = accessPoint.name
                    + " ("
                    + i18n("modeler.general.multiInstanceActivity.accesspoint.name.listSuffix")
                    + ")";
          } else {
            label = accessPoint.name;
          }

          this.outputAccessPoints.push({
            id: accessPoint.id,
            context: i,
            label: label,
            group: group
          });
        }
      }
      this.populateEngineAccessPoints(this.outputAccessPoints);
      this.populateRulesOutAccesspoints(this.outputAccessPoints);

      // set selected value
      if (outMapping) {
        if (outMapping.accessPointId == null) {
          this.outputAccessPointVal = defaultVal;
          this.outputAPPathDisabled = true;
        } else {
          this.outputAccessPointVal = findObject("id", "context",
                  outMapping.accessPointId, outMapping.accessPointContext,
                  this.outputAccessPoints);

          this.outputAPPathDisabled = false;
        }
      }
    };

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.populateEngineAccessPoints = function(
            inputElement) {
      // Generate engine context access points for all data in the model,
      // for sub-process activities with where copyAllData is disabled.
      if (this.modelElement
              && this.modelElement.activity
              && this.modelElement.activity.activityType === constants.SUBPROCESS_ACTIVITY_TYPE
              && this.modelElement.activity.subprocessMode !== "synchShared"
              && (this.modelElement.activity.attributes && !this.modelElement.activity.attributes["carnot:engine:subprocess:copyAllData"])) {

        var group = i18n("modeler.dataFlow.propertiesPanel.outputAccessPointSelectInput.group.engine");

        for ( var i in this.propertiesPanel.getModel().dataItems) {
          var d = this.propertiesPanel.getModel().dataItems[i];
          inputElement.push({
            id: d.id,
            context: "engine",
            label: d.name,
            group: group
          });
        }
      }
    };

    /**
     *
     */
    DataFlowPropertiesPageCtrl.prototype.populateRulesInAccesspoints = function(
            inputElement) {
      // Generate engine context access points for all data in the model,
      // for sub-process activities with where copyAllData is disabled.
      if (this.modelElement
              && this.modelElement.activity
              && this.modelElement.activity.activityType === constants.TASK_ACTIVITY_TYPE
              && this.modelElement.activity.attributes["ruleSetId"]) {

        var ruleOptGroupName = i18n("modeler.dataFlow.propertiesPage.accessPoints.rules.optGroup.name");

        var ruleSets = this.propertiesPanel.getRuleSets();

        if (ruleSets) {
          var rule = null;
          for ( var i in ruleSets) {
            if (ruleSets[i].state.isDeleted != true) {
              if (ruleSets[i].id == this.modelElement.activity.attributes["ruleSetId"]) {
                rule = ruleSets[i];
              }
            }
          }

          if (rule) {
            for ( var i in rule.parameterDefinitions) {
              var param = rule.parameterDefinitions[i];
              if (param.direction === "IN" || param.direction === "INOUT") {
                inputElement.push({
                  id: param.id,
                  context: "application",
                  label: param.name,
                  group: ruleOptGroupName
                });
              }
            }
          }
        }
      }
    };

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.populateRulesOutAccesspoints = function(
            inputElement) {
      // Generate engine context access points for all data in the model,
      // for sub-process activities with where copyAllData is disabled.
      if (this.modelElement
              && this.modelElement.activity
              && this.modelElement.activity.activityType === constants.TASK_ACTIVITY_TYPE
              && this.modelElement.activity.attributes["ruleSetId"]) {
        var ruleOptGroupName = i18n("modeler.dataFlow.propertiesPage.accessPoints.rules.optGroup.name");

        var ruleSets = this.propertiesPanel.getRuleSets();

        if (ruleSets) {
          var rule = null;
          for ( var i in ruleSets) {
            if (ruleSets[i].state.isDeleted != true) {
              if (ruleSets[i].id == this.modelElement.activity.attributes["ruleSetId"]) {
                rule = ruleSets[i];
              }
            }
          }
          if (rule) {
            for ( var i in rule.parameterDefinitions) {
              var param = rule.parameterDefinitions[i];
              if (param.direction === "OUT" || param.direction === "INOUT") {
                inputElement.push({
                  id: param.id,
                  context: "application",
                  label: param.name,
                  group: ruleOptGroupName
                });
              }
            }
          }
        }
      }
    };

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.getClassFor = function(modelElement) {
      if (modelElement.direction == "IN") {
        return "inDataPathListItem";
      } else if (modelElement.direction == "OUT") {
        return "outDataPathListItem";
      } else {
        return "inoutDataPathListItem";
      }
    }

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.nameModified = function() {
      this.resetMessages();
      this.dataMappingIndex = undefined;
      
      if (this.selectedDataMapping.name == "" || !this.selectedDataMapping.name) {
        var index = this.determineIndex();
        var dataMapping = this.unifiedDataMappings[index];
        this.selectedDataMapping.name = dataMapping.name;
        this.showMessage_(i18n('modeler.dataFlow.propertiesPanel.name.required'), 'error');
        return;
      }

      this.updateDataMapping(this.getUuid(), {
        name: this.selectedDataMapping.name
      });
    }

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.getUuid = function() {
      var uuid = undefined;
      if (this.selectedDataMapping.inMapping) {
        uuid = this.selectedDataMapping.inMapping.uuid;
      }
      if (!uuid) {
        uuid = this.selectedDataMapping.outMapping.uuid;
      }
      return uuid;
    }

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.addMapping = function() {
      var index = this.getNextIdIndex();
      var id = this.modelElement.id + index;
      var name = this.modelElement.name + index;

      var changes = {
        id: id,
        name: name,
        direction: "IN"
      };

      // Done in order to make sure correct index is highlighted when adding the
      // mappings randomly
      this.dataMappingIndex = undefined;
      this.selectedDataMapping = {
        id: id
      };

      this.createDataMapping(changes);
    }

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.deleteMapping = function() {
      if (this.unifiedDataMappings.length == 1) { return; }

      var mappingsTobeDeleted = [];

      if (this.selectedDataMapping.outMapping) {
        var index = searchDataMapping(this.selectedDataMapping.outMapping,
                this.modelElement.dataMappings)
        if (index > -1) {
          mappingsTobeDeleted.push(this.modelElement.dataMappings[index].uuid);
          this.selectedDataMapping.outMapping = null;
        }
      }

      if (this.selectedDataMapping.inMapping) {
        var index = searchDataMapping(this.selectedDataMapping.inMapping,
                this.modelElement.dataMappings)
        if (index > -1) {
          mappingsTobeDeleted.push(this.modelElement.dataMappings[index].uuid);
          this.selectedDataMapping.inMapping = null;
        }
      }

      if (this.dataMappingIndex > 0) {
        this.dataMappingIndex--;
      } else {
        this.dataMappingIndex = 0;
      }

      this.deleteDataMapping(mappingsTobeDeleted);
    }

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.getNextIdIndex = function() {
      var n = 0;
      var idOrNameExists = true;
      while (idOrNameExists) {
        n++;
        var newId = "" + this.modelElement.id + n;
        var newName = "" + this.modelElement.name + n;
        idOrNameExists = false;
        for (var i = 0; i < this.unifiedDataMappings.length; i++) {
          if (this.unifiedDataMappings[i].id === newId
                  || this.unifiedDataMappings[i].name === newName) {
            idOrNameExists = true;
            break;
          }
        }
      }
      return n;
    }

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.determineIndex = function() {
      if (!this.selectedDataMapping) { return 0; }

      for (var i = 0; i < this.unifiedDataMappings.length; i++) {
        var dm = this.unifiedDataMappings[i];
        if (dm.id == this.selectedDataMapping.id
                || (this.selectedDataMapping.inMapping && (dm.uuid == this.selectedDataMapping.inMapping.uuid))
                || (this.selectedDataMapping.outMapping && (dm.uuid == this.selectedDataMapping.outMapping.uuid))) { return i; }
      }
      return 0;
    }

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.setSelected = function(index) {
      if (this.unifiedDataMappings.length <= index) {
        index = this.unifiedDataMappings.length - 1;
      }

      this.dataMappingIndex = index;
      var dataMapping = this.unifiedDataMappings[index];
      var self = this;
      self.selectedDataMapping = {};

      self.modelElement.dataMappings.forEach(function(dm) {
        if (dm.id == dataMapping.id && dm.direction == "IN") {
          self.selectedDataMapping.id = dm.id;
          self.selectedDataMapping.name = dm.name;
          self.selectedDataMapping.inMapping = dm;
          self.selectedDataMapping.inMappingExist = true;
          self.selectedDataMapping.inMapping.uuid = dm.uuid;
        }
        if (dm.id == dataMapping.id && dm.direction == "OUT") {
          self.selectedDataMapping.id = dm.id;
          self.selectedDataMapping.name = dm.name;
          self.selectedDataMapping.outMapping = dm;
          self.selectedDataMapping.outMappingExist = true;
          self.selectedDataMapping.outMapping.uuid = dm.uuid;
        }
      });

      self.populateInputAccessPointSelectInput();
      self.populateOutputAccessPointSelectInput();

      self.dataPathDisabled = self.disableDataPath();
    }

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.disableDataPath = function() {
      var disableDataPath = false;
      if (this.element && this.element.modelElement) {
        if (this.element.modelElement.activity
                && this.element.modelElement.activity.activityType === "Task"
                && this.element.modelElement.activity.applicationFullId) {
          var app = this.propertiesPanel
                  .findApplication(this.element.modelElement.activity.applicationFullId);
          if (app
                  && (app.applicationType === constants.JAVA_APPLICATION_TYPE
                          || app.applicationType === constants.SPRING_BEAN_APPLICATION_TYPE || app.applicationType === constants.SESSION_BEAN_APPLICATION_TYPE)) {
            disableDataPath = true;
          }
        }

        if (this.element.modelElement.data
                && (this.element.modelElement.data.dataType === constants.ENTITY_DATA_TYPE || this.element.modelElement.data.dataType === constants.HIBERNATE_DATA_TYPE)) {
          disableDataPath = true;
        }
      }

      return disableDataPath;
    };

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.inputCheckboxChanged = function() {
      if (!this.selectedDataMapping.inMappingExist
              && !this.selectedDataMapping.outMappingExist) {
        this.selectedDataMapping.inMappingExist = true;
        return;
      } else if (!this.selectedDataMapping.inMappingExist) {
        // find and delete
        var index = searchDataMapping(this.selectedDataMapping.inMapping,
                this.modelElement.dataMappings);
        if (index > -1) {
          this.deleteDataMapping([this.modelElement.dataMappings[index].uuid]);
          this.selectedDataMapping.inMapping = undefined;
        }
      } else {
        // checked, it is assumed that at the moment out-mapping exist
        var changes = {
          id: this.selectedDataMapping.outMapping.id,
          name: this.selectedDataMapping.outMapping.name,
          direction: "IN"
        };
        this.createDataMapping(changes);
      }

      this.dataPathDisabled = this.disableDataPath();
    }

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.outputCheckboxChanged = function() {
      if (!this.selectedDataMapping.inMappingExist
              && !this.selectedDataMapping.outMappingExist) {
        this.selectedDataMapping.outMappingExist = true;
        return;
      } else if (!this.selectedDataMapping.outMappingExist) {
        // find and delete
        var index = searchDataMapping(this.selectedDataMapping.outMapping,
                this.modelElement.dataMappings);
        if (index > -1) {
          this.deleteDataMapping([this.modelElement.dataMappings[index].uuid]);
          this.selectedDataMapping.outMapping = undefined;
        }
      } else {
        // checked, it is assumed that at the moment inmapping exist
        var changes = {
          id: this.selectedDataMapping.inMapping.id,
          name: this.selectedDataMapping.inMapping.name,
          direction: "OUT"
        };
        this.createDataMapping(changes);
      }

      this.dataPathDisabled = this.disableDataPath();
    }

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.inputAccessPointChanged = function() {
      var partialChanges = {};
      if (this.inputAccessPointVal.id == "DEFAULT") {
        partialChanges.accessPointId = null;
        partialChanges.accessPointContext = null;
      } else {
        partialChanges.accessPointId = this.inputAccessPointVal.id;
        partialChanges.accessPointContext = this.inputAccessPointVal.context;
      }
      this.updateDataMapping(this.selectedDataMapping.inMapping.uuid,
              partialChanges);
    }

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.outputAccessPointChanged = function() {
      var partialChanges = {};
      if (this.outputAccessPointVal.id == "DEFAULT") {
        partialChanges.accessPointId = null;
        partialChanges.accessPointContext = null;
      } else {
        partialChanges.accessPointId = this.outputAccessPointVal.id;
        partialChanges.accessPointContext = this.outputAccessPointVal.context;
      }

      this.updateDataMapping(this.selectedDataMapping.outMapping.uuid,
              partialChanges);
    }

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.inputDataPathChanged = function() {
      this.updateDataMapping(this.selectedDataMapping.inMapping.uuid, {
        dataPath: this.selectedDataMapping.inMapping.dataPath
      });
    }

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.outputDataPathChanged = function() {
      this.updateDataMapping(this.selectedDataMapping.outMapping.uuid, {
        dataPath: this.selectedDataMapping.outMapping.dataPath
      });
    }

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.inputAccessPointPathChanged = function() {
      this.updateDataMapping(this.selectedDataMapping.inMapping.uuid, {
        accessPointPath: this.selectedDataMapping.inMapping.accessPointPath
      });
    }

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.outputAccessPointPathChanged = function() {
      this.updateDataMapping(this.selectedDataMapping.outMapping.uuid, {
        accessPointPath: this.selectedDataMapping.outMapping.accessPointPath
      });
    }

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.updateDataMapping = function(uuid,
            partialChanges) {
      this.propertiesPanel.submitChangesWithUUIDForCommandType(
              "modelElement.update", uuid, {
                modelElement: partialChanges
              });
    }

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.createDataMapping = function(changes) {
      this.propertiesPanel.submitChangesWithUUIDForCommandType(
              "datamapping.create", this.element.uuid, changes);
    }

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.deleteDataMapping = function(uuid) {
      this.propertiesPanel.submitChangesWithUUIDForCommandType(
              "datamapping.delete", this.element.uuid, {
                'uuid': uuid
              });
    }
    
    //reset message
    DataFlowPropertiesPageCtrl.prototype.resetMessages = function() {
      this.showMessage = false;
      sdMessageService.showMessage({
        type: "error"
      });
    }
    
    //show error/confirmation message
    DataFlowPropertiesPageCtrl.prototype.showMessage_ = function(msg, type) {
      this.showMessage = true;
      if (!type) {
        sdMessageService.showMessage(msg);
      } else {
        sdMessageService.showMessage({
          message: msg,
          type: type
        });
      }
    }
  }

  /**
   * 
   */
  function transformDMs(dataMappings) {
    var nDataMappings = [];
    var nDataMappingsSet = {};
    if (dataMappings == null) { return nDataMappings; }

    dataMappings.forEach(function(dm) {
      if (nDataMappingsSet[dm.id]) {
        nDataMappingsSet[dm.id].direction = "INOUT";
        nDataMappingsSet[dm.id].uuid = dm.uuid;
      } else {
        nDataMappingsSet[dm.id] = {};
        nDataMappingsSet[dm.id].id = dm.id;
        nDataMappingsSet[dm.id].name = dm.name;
        nDataMappingsSet[dm.id].direction = dm.direction;
        nDataMappingsSet[dm.id].uuid = dm.uuid;
        nDataMappings.push(nDataMappingsSet[dm.id])
      }
    });
    return nDataMappings;
  }

  /**
   * 
   */
  function searchDataMapping(mapping, dataMappings) {
    if (dataMappings == null) { return nDataMappings; }
    for (var i = 0; i < dataMappings.length; i++) {
      var dm = dataMappings[i];
      if (dm.id == mapping.id && dm.direction == mapping.direction) { return i; }
    }
    return undefined;
  }

  /**
   * 
   */
  function findObject(property1, property2, val1, val2, objArray) {
    for (var i = 0; i < objArray.length; i++) {
      if (val1 == objArray[i][property1] && val2 == objArray[i][property2]) { return objArray[i]; }
    }
  }

})();