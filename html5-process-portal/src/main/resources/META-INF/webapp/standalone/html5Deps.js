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
        'jquery' : ['../portal-shell/js/libs/jquery/2.1.3/jquery'],
        'jquery.dataTables' : [ "html5-common/libs/datatables/1.9.4/jquery.dataTables" ],
        'angularjs' : ['../portal-shell/js/libs/angular/1.4.4/angular'],
        'angularResource' : ['../portal-shell/js/libs/angular/1.4.4/angular-resource'],
        'bootstrap' : [ 'html5-common/libs/bootstrap/bootstrap' ],
        'ckeditor':['html5-common/libs/ckeditor/ckeditor'],
        'portalApplication' : [ 'common/html5/portalApplication' ],
        'html5CommonMain' : [ 'html5-common/scripts/main' ],
        'html5ViewsCommonMain' : [ 'html5-views-common/html5/scripts/main' ],
        'sdEventBusService' : [ 'html5-common/scripts/services/sdEventBusService' ],
        'httpInterceptorProvider' : [ 'html5-common/scripts/services/sdHttpInterceptorProvider' ],
        'sdLoggerService' : [ 'html5-common/scripts/services/sdLoggerService' ],
        'sdData' : [ 'html5-common/scripts/directives/sdData' ],
        'sdDataTable' : [ 'html5-common/scripts/directives/sdDataTable' ],
        'sdEnvConfigService' : [ 'html5-common/scripts/services/sdEnvConfigService' ],
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
        'sdProcessDocumentTree': ['html5-common/scripts/directives/sdProcessDocumentsTree/sdProcessDocumentsTree'],
        'uiBootstrap':['html5-common/libs/ui-bootstrap/ui-bootstrap-tpls-1.2.1.min'],   
        'sdDateTimeFilter' : [ 'html5-common/scripts/filters/sdDateTimeFilter'],
        'sdNotesPanel': ['html5-process-portal/scripts/directives/sdNotesPanel/sdNotesPanel'],
        'sdMimeTypeService': ['html5-process-portal/scripts/services/sdMimeTypeService'],
        'sdActivityPanelPropertiesPage': ['html5-process-portal/scripts/directives/sdActivityPanelPropertiesPage/sdActivityPanelPropertiesPage'],
        'sdProcessDocumentsPanel': ['html5-process-portal/scripts/directives/sdProcessDocumentsPanel/sdProcessDocumentsPanel'],
        'sdFileDropbox' : [ 'html5-common/scripts/directives/sdFileDropbox'],
        'sdUtilDirectives' : [ 'html5-common/scripts/directives/sdUtilDirectives'],
        'sdRepositoryUploadDialog': [ 'html5-common/scripts/directives/dialogs/sdRepositoryUploadDialog'],
        'sdVersionHistoryDialog': [ 'html5-views-common/html5/scripts/directives/sdVersionHistoryDialog'],
        'documentRepositoryService': ['html5-common/scripts/directives/sdDocumentRepository/documentRepositoryService'],
        'sdLoggedInUserService' : ['html5-common/scripts/services/sdLoggedInUserService'],
        'sdInitializerService' :  [ 'html5-common/scripts/services/sdInitializerService'],
        'sdInitializer' :  [ 'html5-common/scripts/directives/sdInitializer'],
        'sdSsoService' :  [ 'html5-common/scripts/services/sdSsoService'],
        'sdSessionService' : ['html5-common/scripts/services/sdSessionService'],
        'sdOpenDocumentLink' : ['html5-process-portal/scripts/directives/sdOpenDocumentLink'],
        'sdCommonViewUtilService' : ['html5-views-common/html5/scripts/services/sdCommonViewUtilService']
      },
      shim : {
        'jquery.dataTables' : [ 'jquery' ],
        'angularjs' : {
          deps : ["jquery"],
          exports : "angular"
        },
        'angularResource' : ['angularjs'],
        'bootstrap' : ['jquery'],
        'uiBootstrap' : ['bootstrap','jquery','angularjs'],
		'html5CommonMain' : [ 'angularjs', 'portalApplication','uiBootstrap'],
		'html5ViewsCommonMain' : [ 'angularjs', 'portalApplication'],
        'sdEventBusService' : [ 'html5CommonMain' ],
        'httpInterceptorProvider' : [ 'html5CommonMain' ],
        'sdLoggerService' : [ 'html5CommonMain' ],
        'sdData' : [ 'html5CommonMain' ],
        'sdDataTable' : [ 'html5CommonMain', 'sdLoggerService', 'sdDialogService' ,'sdPopover'],
        'sdEnvConfigService' : [ 'html5CommonMain' ],
        'sdUtilService' : [ 'html5CommonMain' ],
        'sdViewUtilService' : [ 'html5CommonMain' ],
        'sdPreferenceService' : [ 'html5CommonMain','angularResource'],
        'sdDialog' : [ 'html5CommonMain', 'sdLoggerService', 'bootstrap', 'sdEnvConfigService', 'sdUtilService', 'sdLoggerService'],
        'sdDialogService' : [ 'sdDialog' ],
        'sdPortalConfigurationService' : [ 'html5CommonMain' ],
        'sdLocalizationService' : [ 'html5CommonMain' ],
        'sdDatePicker' : [ 'html5CommonMain', 'sdLocalizationService' ],
        'sdPopover' : [ 'html5CommonMain', 'sdLoggerService', 'bootstrap'],
        'sdAutoComplete' :['html5CommonMain'],
        'sdRichTextEditor':['html5CommonMain','ckeditor'],
        'sdTree' :['html5CommonMain','ckeditor'] ,
        'sdFolderTree' : ['html5CommonMain','sdUtilService','sdTree'],
        'sdProcessDocumentTree':['html5CommonMain','sdUtilService','sdTree'],
        'sdDateTimeFilter' : ['sdLocalizationService'],
        'sdNotesPanel':['html5CommonMain','sdUtilService', 'sdDateTimeFilter'],
        'sdProcessDocumentsPanel':['html5CommonMain','sdUtilService', 'sdMimeTypeService', 'sdPopover', 'sdRepositoryUploadDialog'],
        'sdMimeTypeService': ['html5CommonMain'],
        'sdActivityPanelPropertiesPage': ['sdNotesPanel', 'sdProcessDocumentsPanel'],
        'sdFileDropbox':  ['html5CommonMain'],
        'sdRepositoryUploadDialog' : ['html5CommonMain'],
        'sdUtilDirectives': ['html5CommonMain'],
        'sdVersionHistoryDialog': ['documentRepositoryService'],
        'documentRepositoryService': ['sdMimeTypeService', 'sdUtilService'],
        'sdLoggedInUserService' : ['html5CommonMain','sdUtilService'],
        'sdSessionService' : ['sdUtilService','sdLoggerService'],
        'sdSsoService': ['sdEnvConfigService', 'sdSessionService','html5CommonMain'],
        'sdInitializerService' : ['html5CommonMain','sdSsoService','sdLoggedInUserService'],
        'sdInitializer' : ['sdInitializerService'],
        'sdCommonViewUtilService' : ['html5ViewsCommonMain','sdViewUtilService', 'sdLoggerService'],
        'sdOpenDocumentLink' : ['html5CommonMain','sdUtilService','sdCommonViewUtilService','sdMimeTypeService']
      },
      deps : [ "jquery.dataTables", "angularjs", "angularResource","bootstrap","ckeditor","portalApplication",
          "html5CommonMain","html5ViewsCommonMain","sdEventBusService", "httpInterceptorProvider",
          "sdLoggerService", "sdData", "sdDataTable", "sdEnvConfigService",
          'sdUtilService', 'sdViewUtilService', 'sdPreferenceService', 'sdDialog', 'sdDialogService', 'sdPortalConfigurationService' , 'sdPopover', 
          'sdAutoComplete','sdRichTextEditor','sdTree','sdFolderTree','sdProcessDocumentTree', 'sdDateTimeFilter', 'sdNotesPanel', 
          'sdActivityPanelPropertiesPage', 'sdProcessDocumentsPanel', 'uiBootstrap', 'sdFileDropbox', 'sdUtilDirectives',
          'sdVersionHistoryDialog', 'documentRepositoryService', 'sdInitializer', 'sdOpenDocumentLink']
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
    	this.$get = [ function () {
    		var listeners = {};
    		var service = {};
    		/*
    		 * 
    		 */
    		function getCount(topicListeners) {
    			var count = 0;
    			for(var id in topicListeners) {
    				count++;
    			}
    			return count;
    		}

    		
    		service.subscribe = function(topic, callback) {

    			if (!topic || !callback) {
    				return;
    			}

    			listeners[topic] = listeners[topic] || {};
    			var id = getCount(listeners[topic]);
    			listeners[topic][id] = callback;

    			// Unsubscribe function
    			var ret = function() {
    				if (listeners[topic] && listeners[topic][id]) {
    					delete listeners[topic][id];
    				}
    			};


    		};
    		service.publish = function(topic, payload) {
    			var ret = [];

    			var allListeners = listeners[topic] || {};
    			for(var id in allListeners) {
    				ret.push(allListeners[id](payload));
    			}

    			for(var i in ret) {
    				if (ret[i] !== 'object' && ret[i] == false) {
    					return false;
    				}
    			}

    			return true;
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
                $rootScope.i18n = parentScope.sdI18n; //TODO: should have sgi18n??
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

      module.provider('sdI18nService', function () {
        this.$get = ['$rootScope', function ($rootScope) {
            var service = {};
            var parentScope = parent.window.angular.element(parent.document.body).scope();
            // use parent i18n implementation
            if (parentScope && parentScope.sdI18n) {
              service.translate = parentScope.sdI18n;
              $rootScope.sdI18n = parentScope.sdI18n;
              
              service.getInstance = function(prefix) {
                var self = this;
                return {
                  translate: function(key, defVal, params) {
                    return self.translate(prefix + "." + key, defVal, params);
                  }
                }
              }
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
