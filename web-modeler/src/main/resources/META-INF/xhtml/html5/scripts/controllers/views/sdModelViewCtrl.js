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
          ['$scope', '$q', 'sdUtilService', 'sdI18nService', ModelViewCtrl]);

  /*
   * 
   */
  function ModelViewCtrl($scope, $q, sdUtilService, sdI18nService) {
    var self = this;
    self.initialized = false;

    $scope.sdI18nModeler = sdI18nService.getInstance('bpm-modeler-messages').translate;
    var i18n = $scope.sdI18nModeler;

    $scope.$watch('elementView.refreshElement', function() {
      if (!self.initialized) {
        self.elementView = $scope.elementView;
        if (!self.elementView) { return; }
        self.initialized = true;
      }
      self.modelElement = self.elementView.modelElement;
      self.refresh();
    }, false);

    $scope.$watch('commandError.command', function() {
      if ($scope.commandError) {
        self.processCommandError($scope.commandError.command,
                $scope.commandError.response);
      }
    }, false);

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
          self.serverErrorMsg = i18n(response.responseText,
                  response.responseText, [self.modelId])
          self.modelId = self.modelElement.id;
        }
      }
    }
  }
})();