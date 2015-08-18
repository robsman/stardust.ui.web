/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

/*
 * @author Yogesh.Manware
 */

(function() {
  'use strict';

  angular.module('modeler-ui').provider('sdModelerUtilService', function() {
    this.$get = [function() {
      var service = new UtilService();
      return service;
    }];
  });

  /*
   * 
   */
  function UtilService() {

    /**
     * 
     */
    UtilService.prototype.hasPublicVisibility = function(modelElement) {
      if (!modelElement.attributes['carnot:engine:visibility']
              || modelElement.attributes['carnot:engine:visibility'] === "Public") { return true; }

      return false;
    };
  }

})();
