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
		        "html5-common/styles/starclipse/starclipse.css",          
				"html5-common/styles/3rd-party/jquery-plugins/jquery-ui/1.10.2/jquery-ui-custom.css",
				"html5-common/styles/3rd-party/glyphicons/3.3.2/glyphicons.css",
				"html5-common/styles/portal-icons.css",
				"html5-common/styles/3rd-party/datatables/1.9.4/jquery.dataTables.css",
				"html5-common/styles/3rd-party/bootstrap/3.3.2/css/bootstrap-theme.min.css",
				"html5-common/styles/3rd-party/bootstrap/3.3.2/css/bootstrap-modal.css",
				"html5-common/styles/sd-data-table.css",
				"html5-common/styles/sd-autoComplete.css",
				"html5-common/styles/html5-common.css",
				"html5-common/styles/sdTree.css",
				"html5-process-portal/styles/html5-process-portal.css",
				"html5-common/styles/3rd-party/font-awesome/4.4.0/css/font-awesome.min.css"
				];
		

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
				'jquery' : ['../portal-shell/js/libs/jquery/1.9.1/jquery'],
				'jquery.dataTables' : [ "html5-common/libs/datatables/1.9.4/jquery.dataTables" ],
				'angularjs' : ['../portal-shell/js/libs/angular/1.2.11/angular'],
				'angularResource' : ['../portal-shell/js/libs/angular/1.2.11/angular-resource'],
				'bootstrap' : [ 'html5-common/libs/bootstrap/bootstrap' ],
				'ckeditor':['html5-common/libs/ckeditor/ckeditor'],
				'portalApplication' : [ 'common/html5/portalApplication' ],
				'html5CommonMain' : [ 'html5-common/scripts/main' ],
				'sdEventBusService' : [ 'html5-common/scripts/services/sdEventBusService' ],
				'httpInterceptorProvider' : [ 'html5-common/scripts/services/sdHttpInterceptorProvider' ],
				'sdLoggerService' : [ 'html5-common/scripts/services/sdLoggerService' ],
				'sdData' : [ 'html5-common/scripts/directives/sdData' ],
				'sdDataTable' : [ 'html5-common/scripts/directives/sdDataTable' ],
				'sdUtilService' : [ 'html5-common/scripts/services/sdUtilService' ],
				'sdViewUtilService' : [ 'html5-common/scripts/services/sdViewUtilService' ],
				'sdPreferenceService' : [ 'html5-common/scripts/services/sdPreferenceService' ],
				'sdDialog' : [ 'html5-common/scripts/directives/dialogs/sdDialog' ],
				'sdDialogService' : [ 'html5-common/scripts/services/sdDialogService' ],
				'sdPortalConfigurationService' : [ 'html5-common/scripts/services/sdPortalConfigurationService' ],
			    'sdLocalizationService' : [ 'html5-common/scripts/services/sdLocalizationService' ],
			    'sdDatePicker': ['html5-common/scripts/directives/sdDatePicker'],
			    'sdPopover': ['html5-common/scripts/directives/sdPopover'],
			    'sdAutoComplete': ['html5-common/scripts/directives/sdAutoComplete'],
			    'sdRichTextEditor':['html5-common/scripts/directives/sdRichTextEditor'],
			    'sdTree' : ['html5-common/scripts/directives/sdTree'],
			    'sdFolderTree' : ['html5-common/scripts/directives/sdFolderTree/sdFolderTree'],
			    'sdProcessDocumentTree': ['html5-common/scripts/directives/sdProcessDocumentsTree/sdProcessDocumentsTree']
			    
			},
			shim : {
				'jquery.dataTables' : [ 'jquery' ],
				'angularjs' : {
					deps : ["jquery"],
					exports : "angular"
				},
				'angularResource' : ['angularjs'],
				'bootstrap' : ['jquery'],
				'html5CommonMain' : [ 'angularjs', 'portalApplication'],
				'sdEventBusService' : [ 'html5CommonMain' ],
				'httpInterceptorProvider' : [ 'html5CommonMain' ],
				'sdLoggerService' : [ 'html5CommonMain' ],
				'sdData' : [ 'html5CommonMain' ],
				'sdDataTable' : [ 'html5CommonMain', 'sdLoggerService', 'sdDialogService' ,'sdPopover'],
				'sdUtilService' : [ 'html5CommonMain' ],
				'sdViewUtilService' : [ 'html5CommonMain' ],
				'sdPreferenceService' : [ 'html5CommonMain','angularResource'],
				'sdDialog' : [ 'html5CommonMain', 'sdLoggerService', 'bootstrap' ],
				'sdDialogService' : [ 'sdDialog' ],
				'sdPortalConfigurationService' : [ 'html5CommonMain' ],
			    'sdLocalizationService' : [ 'html5CommonMain' ],
			    'sdDatePicker' : [ 'html5CommonMain', 'sdLocalizationService' ],
			    'sdPopover' : [ 'html5CommonMain', 'sdLoggerService', 'bootstrap' ],
			    'sdAutoComplete' :['html5CommonMain'],
			    'sdRichTextEditor':['html5CommonMain','ckeditor'],
			    'sdTree' :['html5CommonMain','ckeditor'] ,
			    'sdFolderTree' : ['html5CommonMain','sdUtilService','sdTree'],
			    'sdProcessDocumentTree':['html5CommonMain','sdUtilService','sdTree']
			},
			deps : [ "jquery.dataTables", "angularjs", "angularResource","bootstrap","ckeditor","portalApplication",
					"html5CommonMain", "sdEventBusService", "httpInterceptorProvider",
					"sdLoggerService", "sdData", "sdDataTable",
					'sdUtilService', 'sdViewUtilService', 'sdPreferenceService', 'sdDialog', 'sdDialogService', 'sdPortalConfigurationService' , 'sdPopover', 
					'sdAutoComplete','sdRichTextEditor','sdTree','sdFolderTree','sdProcessDocumentTree']

		};

		copyValues(config.paths, reqMod.paths);
		copyValues(config.shim, reqMod.shim);
		config.deps = config.deps.concat(reqMod.deps);
		return config;
	}

	function bootstrapAngular(applicationModules) {
		var module = angular.module("dummyBootstrapModule", ['ngResource']);
		
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
		  

		/**
		 * requireJs injection service is not available as bpm-ui module
		 * is skipped for HTML5 datatable
		 */  
		module.provider('sgRequireJSService', function() {
			this.$get = [
					'$rootScope',
					function($rootScope) {
						var service = {};
						/*
						 * 
						 */
						service.getModule = function(modules) {
							var deferred = jQuery.Deferred();

							if ("string" == typeof (modules)) {
								modules = [ modules ];
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
								waitSeconds : 0,
								baseUrl : baseUrl+ "/plugins", //for web-modeler dependency using /plugins
								paths : paths
							});

							r(deps, function() {
								var args = arguments ? Array.prototype.slice
										.call(arguments, 0) : [];
								deferred.resolve.apply(null, args);
							});

							return deferred.promise();
						}
						return service;
					} ];
		});
		  
		  // TODO - temp fix applied, resolve using angularContext
		  module.provider('sgI18nService', function () {
		      this.$get = ['$rootScope', function ($rootScope) {
		          var service = {};
		          var parentScope = parent.window.angular.element(parent.document.body).scope();
		          // use parent i18n implementation
		          if (parentScope && parentScope.sdI18n) {
		        	  service.translate = parentScope.sdI18n;
		        	  $rootScope.i18n = parentScope.sdI18n;
		          } else {
		        	  // provide dummy implementation if i18n not
			          // available from parent
			          service.translate = function(key, value) {
			              if(value){
			            	  return value;
			              }else{
			            	  return key;
			              }
			          };
			          $rootScope.i18n = service.translate;  
		          }
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
