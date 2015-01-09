var html5Deps = function() {

	return {
		loadStyleSheets : loadStyleSheets,
		prepareRequireJsConfig : prepareRequireJsConfig,
		bootstrapAngular : bootstrapAngular
	};
	
	/**
	 * Load datatable specific styles
	 */
	function loadStyleSheets(pluginBaseUrl) {
		var head = document.getElementsByTagName('head')[0];
		var styleSheets = [ "html5-common/styles/html5-common.css",
				"html5-common/styles/sd-data-table.css",
				"html5-common/styles/datatables/jquery.dataTables.css",
				"html5-common/styles/font-awesome-4.2.0/css/font-awesome.css" ];

		for ( var i in styleSheets) {
			injectCSS(head, pluginBaseUrl + styleSheets[i]);
		}
	}
	
	/**
	 * Update config with HTML5 dataTable paths,shims
	 */	
	function prepareRequireJsConfig(config) {
		var reqMod = {
			paths : {
				'jquery.dataTables' : [ "html5-common/libs/datatables/1.10.2/main/jquery.dataTables" ],
				'ngDialog' : [ 'html5-common/libs/ngDialog/ngDialog' ],
				'portalApplication' : [ 'common/html5/portalApplication' ],
				'html5CommonMain' : [ 'html5-common/scripts/main' ],
				'sdEventBusService' : [ 'html5-common/scripts/services/sdEventBusService' ],
				'httpInterceptorProvider' : [ 'html5-common/scripts/services/sdHttpInterceptorProvider' ],
				'sdLoggerService' : [ 'html5-common/scripts/services/sdLoggerService' ],
				'sdData' : [ 'html5-common/scripts/directives/sdData' ],
				'sdDataTable' : [ 'html5-common/scripts/directives/sdDataTable' ],
				'sdUtilService' : [ 'html5-common/scripts/services/sdUtilService' ],
				'sdViewUtilService' : [ 'html5-common/scripts/services/sdViewUtilService' ],
				'sdPreferenceService' : [ 'html5-common/scripts/services/sdPreferenceService' ]
			},
			shim : {
				'jquery.dataTables' : [ 'jquery' ],
				'ngDialog' : [ 'angularjs' ],
				'html5CommonMain' : [ 'angularjs' ],
				'sdEventBusService' : [ 'html5CommonMain' ],
				'httpInterceptorProvider' : [ 'html5CommonMain' ],
				'sdLoggerService' : [ 'html5CommonMain' ],
				'sdData' : [ 'html5CommonMain' ],
				'sdDataTable' : [ 'html5CommonMain', 'sdLoggerService' ],
				'sdUtilService' : [ 'html5CommonMain' ],
				'sdViewUtilService' : [ 'html5CommonMain' ],
				'sdPreferenceService' : [ 'html5CommonMain' ]
			},
			deps : [ "jquery.dataTables", "ngDialog", "portalApplication",
					"html5CommonMain", "sdEventBusService", "httpInterceptorProvider",
					"sdLoggerService", "sdData", "sdDataTable",
					'sdUtilService', 'sdViewUtilService', 'sdPreferenceService' ]

		};

		copyValues(config.paths, reqMod.paths);
		copyValues(config.shim, reqMod.shim);
		config.deps = config.deps.concat(reqMod.deps);
		return config;
	}

	function bootstrapAngular(applicationModules) {
		var module = angular.module("dummyBootstrapModule", []);
		
		module.provider('sgViewPanelService', function () {
		      this.$get = ['$rootScope', function ($rootScope) {
		          var service = {};
		          return service;
		      }];
		  });
		
		  module.provider('sgPubSubService', function () {
		      this.$get = ['$rootScope', function ($rootScope) {
		          var service = {};
		          service.subscribe = function() {
		              
		          };
		          return service;
		      }];
		  });
		  
		var modules = portalApplication.getModules();
		if (modules != null && modules.length > 0) {
			modules = modules.concat(module.name);
			if (modules[0] == 'bpm-ui') {
				modules = modules.splice(1);
			}
		} else {
			modules = [];
		}

		// Append application modules
		modules = modules.concat(applicationModules);

		angular.bootstrap(document, modules);  
	}
	/**
	 * 
	 */
	function copyValues(dest, src) {
		for (property in src) {
			dest[property] = src[property];
		}
		return dest;
	}
	
	/**
	 * 
	 */
	function injectCSS(head, src) {
		var link = document.createElement('link');
		link.href = src;
		link.rel = 'stylesheet';
		link.type = 'text/css';
		head.appendChild(link);
	}
}();
