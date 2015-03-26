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

  angular.module('modeler-ui').controller('sdModelViewCtrl',
          ['$scope', '$q', ModelViewCtrl]);

  /*
   * 
   */
  function ModelViewCtrl($scope, $q) {
    var self = this;
    self.initialized = false;

    doWhenModelElementViewIsAvailable($scope, function() {
      self.initialize($scope.elementView, $scope.require);
      // TODO is this better OR implement processCommand()?
      $scope.$watch('elementView.modelElement.id', function() {
        self.refresh();
      }, false);

      $scope.$watch('commandError.command', function() {
        if ($scope.commandError) {
          self.processCommandError($scope.commandError.command,
                  $scope.commandError.response);
        }
      }, false);

    });

    /**
     * @returns
     */
    ModelViewCtrl.prototype.onModelIdChangeConfirm = function() {
      if (this.modelId == this.modelElement.id) { return; }

      this.serverError = false;
      this.showOnSubmitErrors = true;

      this.deferIdChange = $q.defer();

      this.elementView.submitChanges({
        id: self.modelId
      });

      return this.deferIdChange.promise;
    }
  }

  /**
   * 
   */
  ModelViewCtrl.prototype.i18n = function(key) {
    return this.m_i18nUtils.getProperty(key);
  }

  /**
   *
   */
  ModelViewCtrl.prototype.initialize = function(elementView, require) {
    var self = this;
    self.elementView = elementView;
    self.modelElement = self.elementView.modelElement;

    require(['bpm-modeler/js/m_i18nUtils'], function(m_i18nUtils) {
      self.m_i18nUtils = m_i18nUtils;
      self.refresh();
      self.initialized = true;
    })
  }

  /**
   * @param modelElement
   */
  ModelViewCtrl.prototype.refresh = function() {
    this.modelId = this.modelElement.id;
    if (this.deferIdChange) {
      this.deferIdChange.resolve();
      window.parent.EventHub.events.publish("SAVE_AND_RELOAD_MODELS");
      this.deferIdChange = null;
    }
  }

  /**
   * 
   */
  ModelViewCtrl.prototype.onModelIdChange = function() {
    this.showOnSubmitErrors = false;
    this.serverError = false;
    this.serverErrorMsg = null;
  }

  /**
   * 
   */
  ModelViewCtrl.prototype.onModelIdChangeDialogOpen = function() {
    this.modelId = this.modelElement.id;
    this.showOnSubmitErrors = false;
    this.serverError = false;
    this.serverErrorMsg = null;
  }

  /**
   * 
   */
  ModelViewCtrl.prototype.processCommandError = function(command, response) {
    // TODO: verify if command pertains to this ctrl change but it is very
    // complex make such match
    // so, checking the specific error code
    var self = this;
    if (self.deferIdChange) {
      self.deferIdChange.reject();
      self.deferIdChange = null;
    }
    if (command.commandId == "modelElement.update") {
      self.serverError = true;
      if (response.responseText
              && ((response.responseText.indexOf("ModelerError.01002") > -1) || (response.responseText
                      .indexOf("ModelerError.01003") > -1))) {
        // with parameters
        self.serverErrorMsg = self.i18n(response.responseText,
                response.responseText).replace('{0}', self.modelId);
        self.modelId = self.modelElement.id;
      }
    }
  }

  /**
   * 
   */
  function doWhenModelElementViewIsAvailable($scope, callback) {
    console.log("Element View =");
    console.log($scope.elementView);

    if ($scope.elementView && $scope.elementView.modelElement) {
      callback();
    } else {
      // If not available Watch for it
      $scope.watchForIt = function() {
        return $scope.elementView && $scope.elementView.modelElement ? "GotIt"
                : "";
      };

      console.log("Registering Watch for Element View");
      var unregister = $scope.$watch("watchForIt()", function(newVal, oldVal) {
        if (newVal !== oldVal) {
          console.log("ModelElement view got initialized =");
          console.log($scope.elementView.modelElement);
          unregister();

          callback();
        }
      });
    }
  }
})();