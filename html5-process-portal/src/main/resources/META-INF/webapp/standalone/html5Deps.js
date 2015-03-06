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
		var styleSheets = [
				"html5-common/styles/3rd-party/jquery-plugins/jquery-ui/1.10.2/jquery-ui-custom.css",
				"html5-common/styles/3rd-party/glyphicons/glyphicons.css",
				"html5-common/styles/starclipse/starclipse.css",
				"html5-common/styles/3rd-party/datatables/1.9.4/jquery.dataTables.css",
				"html5-common/styles/3rd-party/bootstrap/bootstrap-theme.css",
				"html5-common/styles/3rd-party/bootstrap/bootstrap-modal.css",
				"html5-common/styles/sd-data-table.css",
				"html5-common/styles/sd-autoComplete.css",
				"html5-common/styles/html5-common.css",
				"html5-common/styles/glyphicons-ext.css",
				"html5-common/styles/starclipse/starclipse.css",
				"html5-common/styles/3rd-party/jquery-plugins/jquery-ui/1.10.2/jquery-ui-custom.css",
				"html5-common/styles/3rd-party/glyphicons/glyphicons.css",
				"html5-common/styles/3rd-party/datatables/1.9.4/jquery.dataTables.css",
				"html5-common/styles/3rd-party/bootstrap/bootstrap-theme.css",
				"html5-common/styles/3rd-party/bootstrap/bootstrap-modal.css",
				"html5-common/styles/sd-data-table.css",
				"html5-common/styles/sd-autoComplete.css",
				"html5-common/styles/html5-common.css",
				"html5-common/styles/glyphicons-ext.css",
				"html5-process-portal/styles/html5-process-portal.css" ];

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
				'jquery.dataTables' : [ "html5-common/libs/datatables/1.9.4/jquery.dataTables" ],
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
			deps : [ "jquery.dataTables", "portalApplication",
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
		  
		  // TODO - temp fix applied, resolve using angularContext
		  module.provider('sgI18nService', function () {
		      this.$get = ['$rootScope', function ($rootScope) {
		          var service = {};
		          service.translate = function(key, value) {
		              if(value){
		            	  return value;
		              }else{
		            	  return key;
		              }
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
