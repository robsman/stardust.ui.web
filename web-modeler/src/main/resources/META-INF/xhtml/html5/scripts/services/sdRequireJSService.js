/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

/**
 * This services makes available the requireJS modules in Angular context
 * 
 * @author Yogesh.Manware
 */

(function() {

  'use strict';

  angular.module('modeler-ui').service(
          'sdRequireJSService',
          [
              '$q',
              function($q) {

                var modules = ['plugins/bpm-modeler/js/m_utils',
                    'plugins/bpm-modeler/js/m_i18nUtils',
                    'plugins/bpm-modeler/js/m_constants',
                    'plugins/bpm-modeler/js/m_parsingUtils',
                    'plugins/bpm-modeler/js/m_user',
                    'plugins/bpm-modeler/js/m_commandsController',
                    'plugins/bpm-modeler/js/m_command'];

                var loadedModules = {};
                var initialized = false;

                var initialize = function() {
                  var deferred = $q.defer();

                  if ("string" == typeof (modules)) {
                    modules = [modules];
                  }

                  var paths = {};
                  var deps = [];
                  for ( var i in modules) {
                    paths["module" + i] = modules[i];
                    deps.push("module" + i);
                  }

                  var baseUrl = location.pathname.substring(0,
                          location.pathname.indexOf('/', 1));

                  var r = requirejs.config({
                    waitSeconds: 0,
                    baseUrl: baseUrl,
                    paths: paths
                  });

                  r(deps, function() {
                    var args = arguments ? Array.prototype.slice.call(
                            arguments, 0) : [];

                    for (var i = 0; i < modules.length; i++) {
                      loadedModules[modules[i]] = args[i];
                    }
                    initialized = true;
                    deferred.resolve();
                  });

                  return deferred.promise;
                }

                var defPromise = initialize();

                return {
                  getPromise: function() {
                    return defPromise;
                  },
                  getModule: function(module) {
                    return loadedModules[module];
                  }
                }
              }]);
})();
