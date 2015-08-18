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

  angular.module('shell.services').provider('sdI18nService', function() {
    this.$get = ['sgI18nService', function(sgI18nService) {
      var service = new I18nService(sgI18nService);
      return service;
    }];

  });

  function I18nService(sgI18nService) {

    /**
     * 
     */
    I18nService.prototype.translate = function(key, defVal, param) {
      // convert the key from keyPart1.keyPart2.keyPart3 to
      // pluginName.keyPart1-keyPart2-keyPart3

      if (key.indexOf(".") != -1) {
        var keyPrefix = key.substring(0, key.indexOf("."));
        var propKey = key.substring(key.indexOf(".") + 1);

        if (propKey.indexOf('.') != -1) {
          propKey = propKey.split('.').join('-');
        }

        key = keyPrefix + "." + propKey;
      }

      var i18nedMsg = defVal;

      i18nedMsg = sgI18nService.translate(key, defVal);

      if (i18nedMsg && param) {
        for (var i = 0; i < param.length; i++) {
          i18nedMsg = i18nedMsg.replace('{' + i + '}', param[i])
        }
      }

      if (!i18nedMsg) {
        i18nedMsg = defVal;
      }

      if (!i18nedMsg) {
        i18nedMsg = key;
      }
      return i18nedMsg;

    }

    /**
     * returns the bundle/perspective specific translator
     */
    I18nService.prototype.getInstance = function(prefix) {
      var self = this;
      return {
        translate: function(key, defVal, params) {
          return self.translate(prefix + "." + key, defVal, params);
        }
      }
    }
  }

})();
