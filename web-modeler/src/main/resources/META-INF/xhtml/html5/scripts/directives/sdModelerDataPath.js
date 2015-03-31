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

  angular.module('modeler-ui').directive('sdModelerDataPath',
          ['$compile', ModelerDataPath]);

  function ModelerDataPath($compile) {
      return {
      restrict: 'E',
      replace: true,
      template: '<div ng-if="dataPathCtrl.initialized">\
        <table>\
      <tr>\
        <td><label>{{dataPathCtrl.sdI18nModeler("modeler.activity.propertyPages.onAssignment.data")}}</label></td>\
        <td><select ng-model="dataSelected" aid="dataSelect"\
          ng-change="dataPathCtrl.onDataChange(dataSelected)"\
          ng-options="dataItem.fullId as dataItem.label group by dataItem.group for dataItem in dataPathCtrl.dataItems">\
        </select></td>\
      </tr>\
      <tr>\
        <td><label>{{dataPathCtrl.sdI18nModeler("modeler.activity.propertyPages.onAssignment.path")}}</label></td>\
        <td ng-if="dataPathCtrl.supportsDataPath"><div class="dataPathInput" aid="dataPathInput" sd-auto-complete\
            sda-matches="dataPathCtrl.filteredDataPaths"\
            sda-match-str="dataPathCtrl.matchVal"\
            sda-change="dataPathCtrl.getMatches(dataPathCtrl.matchVal)"\
            sda-text-property="name" sda-container-class="sd-ac-container"\
            sda-item-hot-class="sd-ac-item-isActive"\
            sda-allow-duplicates="false"\
            sda-allow-multiple="false"\
            sda-selected-matches="dataPathCtrl.dataPathSelected"\
            sda-on-selection-change="dataPathCtrl.onDataPathChange(selectedData)"></div></td>\
        <td ng-if="!dataPathCtrl.supportsDataPath"><input type="text" ng-disabled="true" style="width:200px">  </td>\
      </tr>\
    </table>\
  </div>',
      scope: {
        dataItems: '=sdaDataItems',
        dataSelected: '=sdaDataSelected',
        dataPathSelectedStr: '=sdaDataPathSelected',
        onDataChangeClbk: '&sdaOnDataChange',
        m_model: '=sdaServiceModel'
      },
      controller: ['$scope', '$attrs', 'sdUtilService',
          'sdModelerParsingUtilService', 'sdI18nService', 'sdModelerConstants',
          DataPathController]
    }
  }

  var DataPathController = function($scope, $attrs, sdUtilService,
          sdModelerParsingUtilService, sdI18nService, sdModelerConstants) {
    var self = this;
    self.sdI18nModeler = sdI18nService.getInstance('bpm-modeler-messages').translate;
    var i18n = self.sdI18nModeler;

    DataPathController.prototype.safeApply = function() {
      sdUtilService.safeApply($scope);
    };

    /**
     * 
     */
    DataPathController.prototype.initialize = function($scope) {
      var self = this;

      self.dataList = $scope.dataItems;
      self.populateDataItemsList();
      if ($scope.dataSelected) {
        self.dataSelected = $scope.dataSelected;
      } else {
        self.dataSelected = sdModelerConstants.TO_BE_DEFINED;
      }

      self.resetDataPaths();

      if ($scope.dataPathSelectedStr) {
        self.dataPathSelected = [$scope.dataPathSelectedStr];
      } else {
        self.dataPathSelected = [];
      }

      this.refreshSupportDataPathFlag();

      self.onDataChangeClbk = $scope.onDataChangeClbk;
    }

    /**
     * @param selectedDataPath
     */
    DataPathController.prototype.onDataPathChange = function(selectedDataPath) {
      this.dataPathSelected = selectedDataPath;

      var dataPath = "";
      if (this.dataPathSelected && this.dataPathSelected[0]) {
        dataPath = this.dataPathSelected[0];
      }
      this.onDataChangeClbk({
        data: this.dataSelected,
        dataPath: dataPath.id
      });
    }

    /**
     * @param data
     */
    DataPathController.prototype.onDataChange = function(data) {
      this.dataSelected = data;
      this.resetDataPaths();
      this.dataPathSelected = [];

      this.refreshSupportDataPathFlag();

      this.onDataChangeClbk({
        data: data,
        dataPath: ""
      });
    }

    /**
     * 
     */
    DataPathController.prototype.refreshSupportDataPathFlag = function() {
      var selectedDataItem = this.getSelectedDataItem(this.dataSelected);
      if (selectedDataItem.supportsDataPath) {
        this.supportsDataPath = true;
      } else {
        this.supportsDataPath = false;
      }

    }

    /**
     * 
     */
    DataPathController.prototype.getMatches = function(match) {
      var matches = this.relevantDataPaths, filtered = [], delim = ".";
      var temp;
      for (var j = 0; j < matches.length; j++) {
        temp = matches[j];
        if (temp.indexOf(match) == 0) {
          if (temp.indexOf(delim, match.length) == -1) {
            if (temp.lastIndexOf(delim) > 0) {
              temp = temp.slice(temp.lastIndexOf(delim) + 1);
            }
            filtered.push({
              id: temp,
              name: matches[j]
            });
          }
        }
      }
      this.filteredDataPaths = filtered;
    }

    /**
     * 
     */
    DataPathController.prototype.resetDataPaths = function() {
      var delim = ".";
      var dataPaths = []
      if (sdModelerConstants.TO_BE_DEFINED != this.dataSelected) {
        dataPaths = sdModelerParsingUtilService.parseParamDefToStringFrags({
          dataFullId: this.dataSelected,
          id: "id"
        }, $scope.m_model) || [];
      }

      for (var i = 0; i < dataPaths.length; i++) {
        var tempStr = dataPaths[i].replace(/\./g, delim);
        dataPaths[i] = tempStr.slice(tempStr.indexOf(delim) + 1);
      }

      this.relevantDataPaths = dataPaths;
    }

    /**
     * @param dataFullId
     * @returns
     */
    DataPathController.prototype.getSelectedDataItem = function(dataFullId) {
      for (var i = 0; i < this.dataItems.length; i++) {
        if (this.dataItems[i].fullId == dataFullId) { return this.dataItems[i]; }
      }
    }

    /**
     *
     */
    DataPathController.prototype.populateDataItemsList = function() {
      var self = this;
      this.dataItems = [];

      this.dataItems.push({
        fullId: sdModelerConstants.TO_BE_DEFINED,
        label: i18n("modeler.general.toBeDefined")
      })

      if (this.dataList) {
        var modelName = i18n("modeler.element.properties.commonProperties.thisModel");

        for ( var i in this.dataList) {
          var data = this.dataList[i];
          // Show only data items from this model and not
          // external references.
          if (!data.externalReference) {
            var supportsDataPath = true;
            if (data.dataType == sdModelerConstants.PRIMITIVE_DATA_TYPE) {
              supportsDataPath = false;
            }

            this.dataItems.push({
              fullId: data.getFullId(),
              label: data.name,
              group: modelName,
              supportsDataPath: supportsDataPath
            })
          }
        }
      }
    }

    self.initialize($scope);
    $scope.dataPathCtrl = this;
    $scope.dataPathCtrl.initialized = true;
  }
})();