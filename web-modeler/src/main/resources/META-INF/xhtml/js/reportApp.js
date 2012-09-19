'use strict';

/**
 * The main Report Application module.
 *
 * @type {angular.Module}
 */
define(
  'reportApp'
  ,[
    'angularjs',
    'jquery.url'
  ]
  ,function ModelReportApp(angular) {
    var angularModule = angular.module('modelReport', []),
    	app = {};

    app.init = function init() {

    	// register modelId, processId for injection by angular
        angularModule.factory('modelId', function ($window) {
            var modelId = jQuery.url.setUrl(window.location.search).param("modelId");
            return modelId;
        });
        angularModule.factory('processId', function ($window) {
            var modelId = jQuery.url.setUrl(window.location.search).param("processId");
            return modelId;
        });

        // start angular and compile/bind HTML
    	angular.bootstrap(document, ['modelReport']);
    };

    // make the "modelReport" (Angular) module available on the "app" (AMD) module
    app.__defineGetter__('modelReport', function() {
    	return angularModule;
	});

    return app;
  }
);