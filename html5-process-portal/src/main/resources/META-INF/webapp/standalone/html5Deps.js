/*
 * 
 * Utility for RequireJS way of loading things
 * Useful with arbitrary HTML page requiring utilities from HTML5 projects, like sd-data-table
 * 
 */

var html5Deps = {
	prepareRequireJsConfig : function(config) {
		var reqMod = {
			paths : {
				'jquery.dataTables' : [ "html5-common/libs/datatables/1.10.2/main/jquery.dataTables" ],
				'ngDialog' : [ 'html5-common/libs/ngDialog/ngDialog' ],
				'angular-datatables' : [ 'business-object-management/js/libs/jquery/plugins/angular-datatables.min' ],
				'portalApplication' : [ 'common/html5/portalApplication' ],
				'main' : [ 'html5-common/scripts/main' ],
				'sdEventBusService' : [ 'html5-common/scripts/services/sdEventBusService' ],
				'httpInterceptorProvider' : [ 'html5-common/scripts/services/sdHttpInterceptorProvider' ],
				'sdLoggerService' : [ 'html5-common/scripts/services/sdLoggerService' ],
				'sdData' : [ 'html5-common/scripts/directives/sdData' ],
				'sdDataTable' : [ 'html5-common/scripts/directives/sdDataTable' ],
				'sdUtilService' : [ 'html5-common/scripts/services/sdUtilService' ],
				'sdViewUtilService' : [ 'html5-common/scripts/services/sdViewUtilService' ]
			},
			shim : {
				'jquery.dataTables' : [ 'jquery' ],
				'ngDialog' : [ 'angularjs' ],
				'main' : [ 'angularjs' ],
				'sdEventBusService' : [ 'main' ],
				'httpInterceptorProvider' : [ 'main' ],
				'sdLoggerService' : [ 'main' ],
				'sdData' : [ 'main' ],
				'sdDataTable' : [ 'main', 'sdLoggerService' ],
				'sdUtilService' : [ 'main' ],
				'sdViewUtilService' : [ 'main' ]
			},
			arrayObj : [ "jquery.dataTables", "ngDialog", "portalApplication",
					"main", "sdEventBusService", "httpInterceptorProvider",
					"sdLoggerService", "sdData", "sdDataTable",
					'sdUtilService', 'sdViewUtilService' ]

		};

		this.concatenate(config.paths, reqMod.paths);
		this.concatenate(config.shim, reqMod.shim);
		config.arrayObj = config.arrayObj.concat(reqMod.arrayObj);
		return config;
	},

	concatenate : function(a, b) {
		for (property in b)
			a[property] = b[property];

		return a;
	},

	loadStyleSheets : function(pluginBaseUrl) {
		var head = document.getElementsByTagName('head')[0];
		var styleSheets = [
			"html5-common/styles/html5-common.css",
			"html5-common/styles/sd-data-table.css",
			"html5-common/styles/datatables/jquery.dataTables.css",
			"html5-common/styles/font-awesome-4.2.0/css/font-awesome.css"
		];

		for (var i in styleSheets) {
			this.injectCSS(head, pluginBaseUrl + styleSheets[i]);
		}
	},

	injectCSS : function(head, src) {
		var link = document.createElement('link');
		link.href = src;
		link.rel = 'stylesheet';
		link.type = 'text/css';
		head.appendChild(link);
	}
};