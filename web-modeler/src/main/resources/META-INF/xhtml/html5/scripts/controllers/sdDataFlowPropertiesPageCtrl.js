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
              'sdI18nService', DataFlowPropertiesPageCtrl]);

  /*
   * 
   */
  function DataFlowPropertiesPageCtrl($scope, constants, sdUtilService, sdLoggerService, sdI18nService) {
    var self = this;
    self.initialized = false;
    var trace = sdLoggerService.getLogger('modeler-ui.sdDataFlowPropertiesPageCtrl');
    $scope.sdI18nModeler = sdI18nService.getInstance('bpm-modeler-messages').translate;
    var i18n = $scope.sdI18nModeler;
    
    $scope.$watch('page.propertiesPanel.refreshElement', function() {
      if (!self.initialized) {
        self.page = $scope.page;
        self.propertiesPanel = self.page.propertiesPanel;
        self.dataMappingIndex = 0;
      } 
      self.refresh();
      self.initialized = true;
    });
   
    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.refresh = function() {
      this.element = this.propertiesPanel.element;
      if(!this.element){
        return;
      }
      
      this.modelElement = this.element.modelElement;
      this.unifiedDataMappings = transformDMs(this.modelElement.dataMappings);
      this.unifiedDataMappings = sdUtilService.convertToSortedArray(
              this.unifiedDataMappings, "id", true);

      if (this.selectedDataMapping
              && this.selectedDataMapping.uuid != this.element.uuid) {
        this.setSelected(0);
      } else {
        this.setSelected(this.dataMappingIndex);
      }

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
        var count = 0;

        trace.debug("i = " + i);
        trace.debug(context);

        for (var m = 0; m < context.accessPoints.length; ++m) {
          var accessPoint = context.accessPoints[m];

          trace.debug("m = " + m);
          trace.debug(accessPoint);

          if (accessPoint.direction == constants.IN_ACCESS_POINT
                  || accessPoint.direction == constants.IN_OUT_ACCESS_POINT) {
            count++;
          }
        }

        if (count == 0) {
          continue;
        }

        var group = i18n("modeler.dataFlow.propertiesPanel.outputAccessPointSelectInput.group."
                        + i)

        for (var m = 0; m < context.accessPoints.length; ++m) {
          var accessPoint = context.accessPoints[m];

          if (accessPoint.direction == constants.OUT_ACCESS_POINT) {
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
        var count = 0;

        for (var m = 0; m < context.accessPoints.length; ++m) {
          var accessPoint = context.accessPoints[m];

          if (accessPoint.direction == constants.OUT_ACCESS_POINT
                  || accessPoint.direction == constants.IN_OUT_ACCESS_POINT) {
            count++;
          }
        }

        if (count == 0) {
          continue;
        }

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
      if (this.selectedDataMapping.outMapping) {
        var index = searchDataMapping(this.selectedDataMapping.outMapping,
                this.modelElement.dataMappings)
        if (index > -1) {
          this.modelElement.dataMappings[index].name = this.selectedDataMapping.name;
        }
      }
      if (this.selectedDataMapping.inMapping) {
        var index = searchDataMapping(this.selectedDataMapping.inMapping,
                this.modelElement.dataMappings)
        if (index > -1) {
          this.modelElement.dataMappings[index].name = this.selectedDataMapping.name;
        }
      }
      this.submitChanges();
    }

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.addMapping = function() {
      var index = this.getNextIdIndex();
      var id = "" + this.modelElement.id + index;
      var name = "" + this.modelElement.name + index;

      this.modelElement.dataMappings.push({
        id: id,
        name: name,
        direction: "IN"
      });

      this.dataMappingIndex = this.unifiedDataMappings.length;

      this.submitChanges();
    }

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.deleteMapping = function() {
      if (this.unifiedDataMappings.length == 1) { return; }

      if (this.selectedDataMapping.outMapping) {
        var index = searchDataMapping(this.selectedDataMapping.outMapping,
                this.modelElement.dataMappings)
        if (index > -1) {
          this.modelElement.dataMappings.splice(index, 1);
          this.selectedDataMapping.outMapping = null;
        }
      }
      if (this.selectedDataMapping.inMapping) {
        var index = searchDataMapping(this.selectedDataMapping.inMapping,
                this.modelElement.dataMappings)
        if (index > -1) {
          this.modelElement.dataMappings.splice(index, 1);
          this.selectedDataMapping.inMapping = null;
        }
      }

      if (this.dataMappingIndex > 0) {
        this.dataMappingIndex--;
      } else {
        this.dataMappingIndex = 0;
      }

      this.submitChanges();
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
    DataFlowPropertiesPageCtrl.prototype.setSelected = function(index) {
      this.dataMappingIndex = index;
      var dataMapping = this.unifiedDataMappings[index];
      var self = this;
      self.selectedDataMapping = {};
      self.selectedDataMapping.uuid = this.element.uuid;
      self.modelElement.dataMappings.forEach(function(dm) {
        if (dm.id == dataMapping.id && dm.direction == "IN") {
          self.selectedDataMapping.id = dm.id;
          self.selectedDataMapping.name = dm.name;
          self.selectedDataMapping.inMapping = dm;
          self.selectedDataMapping.inMappingExist = true;
        }
        if (dm.id == dataMapping.id && dm.direction == "OUT") {
          self.selectedDataMapping.id = dm.id;
          self.selectedDataMapping.name = dm.name;
          self.selectedDataMapping.outMapping = dm;
          self.selectedDataMapping.outMappingExist = true;
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
          var app = this.page.findApplication(this.element.modelElement.activity.applicationFullId);
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
          this.modelElement.dataMappings.splice(index, 1);
          this.selectedDataMapping.inMapping = undefined;
        }
      } else {
        // checked, it is assumed that at the moment outmapping exist
        this.modelElement.dataMappings.push({
          id: this.selectedDataMapping.outMapping.id,
          name: this.selectedDataMapping.outMapping.name,
          direction: "IN"
        });
      }

      this.submitChanges();

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
          this.modelElement.dataMappings.splice(index, 1);
          this.selectedDataMapping.outMapping = undefined;
        }
      } else {
        // checked, it is assumed that at the moment inmapping exist
        this.modelElement.dataMappings.push({
          id: this.selectedDataMapping.inMapping.id,
          name: this.selectedDataMapping.inMapping.name,
          direction: "OUT"
        });
      }
      this.submitChanges();

      this.dataPathDisabled = this.disableDataPath();
    }

    /**
     * TODO: engine updates [from/to]ModelElementOid and
     * [from/to]ModelElementType, legacy code, needs to be reviewed later
     */
    DataFlowPropertiesPageCtrl.prototype.updateConnection = function() {
      // TODO: find consolidated inMapping and outMapping
      if (this.selectedDataMapping.inMappingExist
              && this.selectedDataMapping.outMappingExist
              && this.element.fromAnchorPoint.symbol.type !== constants.DATA_SYMBOL) {
        // convert to Data to Activity connection
        var tempFromAnchorPoint = this.element.fromAnchorPoint; // activity
        this.element.fromAnchorPoint = this.element.toAnchorPoint; // data
        this.element.toAnchorPoint = tempFromAnchorPoint;
        this.element.fromModelElementOid = this.element.fromAnchorPoint.symbol.oid;
        this.element.toModelElementOid = this.element.toAnchorPoint.symbol.oid;
        var tempFromOrientation = this.element.fromAnchorPointOrientation;
        this.element.fromAnchorPointOrientation = this.element.toAnchorPointOrientation;
        this.element.toAnchorPointOrientation = tempFromOrientation;

      } else if (!this.selectedDataMapping.inMappingExist
              && this.selectedDataMapping.outMappingExist
              && this.element.fromAnchorPoint.symbol.type === constants.DATA_SYMBOL) {
        // convert to Activity to Data connection
        var tempFromAnchorPoint = this.element.fromAnchorPoint;
        this.element.fromAnchorPoint = this.element.toAnchorPoint;
        this.element.toAnchorPoint = tempFromAnchorPoint;
        this.element.fromModelElementOid = this.element.fromAnchorPoint.symbol.oid;
        this.element.toModelElementOid = this.element.toAnchorPoint.symbol.oid;
        var tempFromOrientation = this.element.fromAnchorPointOrientation;
        this.element.fromAnchorPointOrientation = this.element.toAnchorPointOrientation;
        this.element.toAnchorPointOrientation = tempFromOrientation;
      }

      var connectionChanges = {
        fromAnchorPointOrientation: this.element.fromAnchorPointOrientation,
        toAnchorPointOrientation: this.element.toAnchorPointOrientation,
        toModelElementOid: this.element.toModelElementOid,
        fromModelElementOid: this.element.fromModelElementOid,
      }

      return connectionChanges;
    }

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.inputAccessPointChanged = function() {
      if (this.inputAccessPointVal.id == "DEFAULT") {
        this.selectedDataMapping.inMapping.accessPointId = null;
        this.selectedDataMapping.inMapping.accessPointContext = null;
      } else {
        this.selectedDataMapping.inMapping.accessPointId = this.inputAccessPointVal.id;
        this.selectedDataMapping.inMapping.accessPointContext = this.inputAccessPointVal.context;
      }
      this.submitChanges();
    }

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.outputAccessPointChanged = function() {
      if (this.outputAccessPointVal.id == "DEFAULT") {
        this.selectedDataMapping.outMapping.accessPointId = null;
        this.selectedDataMapping.outMapping.accessPointContext = null;
      } else {
        this.selectedDataMapping.outMapping.accessPointId = this.outputAccessPointVal.id;
        this.selectedDataMapping.outMapping.accessPointContext = this.outputAccessPointVal.context;
      }

      this.submitChanges();
    }

    /**
     * 
     */
    DataFlowPropertiesPageCtrl.prototype.submitChanges = function() {
      var self = this;
      this.page.submitChanges({
        modelElement: {
          dataMappings: self.modelElement.dataMappings
        }
      });
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
      } else {
        nDataMappingsSet[dm.id] = {};
        nDataMappingsSet[dm.id].id = dm.id;
        nDataMappingsSet[dm.id].name = dm.name;
        nDataMappingsSet[dm.id].direction = dm.direction;
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