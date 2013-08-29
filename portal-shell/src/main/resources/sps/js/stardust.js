/*
 * 
 */
(function () {
	'use strict';
	if (window.console == undefined) {
		window.console = {};
	}
	return window.console;
}());

/*
 * 
 */
(function (stardust) {
	'use strict';

	var min = '.min';
	var params = null;
	var version = '${buildNumber}';
	var cacheQueryParameter = '';
	var injector = null;

	/*
	 * 
	 */
    function extend(o1, o2) {
        for (var itm in o2) {
            if (itm) {
                o1[itm] = o2[itm];
            }
        }
        return o1;
    }

    var paths = {
		'jQuery' : '/libs/jquery/1.9.1/jquery',
		'angular' : '/libs/angular/1.1.5/angular',
		'angular-resources' : '/libs/angular/1.1.5/angular-resource',
		'underscore' : '/libs/underscore/1.4.2/underscore'
    };

    var shim = {
        'jQuery': {
            exports: 'jQuery'
        },
        'angular': {
            deps: ['jQuery'],
            exports: 'angular'
        },
        'underscore': {
            exports: 'underscore'
        },
        'angular-resources': {
            deps: ['angular']
        }
    };

	/*
	 * 
	 */
	stardust.paths = function () {
        var pathsToReturn = extend({}, paths);
        return pathsToReturn;
    };

    /*
     * 
     */
    stardust.shim = function () {
        return (extend({}, shim));
    };

	/*
	 * 
	 */
	stardust.packages = function (arr) {
        return arr;
	};

	/*
	 * 
	 */
	stardust.prefixContext = function (pathObj) {
        var modifiedPaths = extend({}, pathObj);
        var context = stardust.getLocationPath();
        if (context !== '/') {
            //check if it ends in resource extension like test.html, test.htm etc. Drop it if that's the case
            var arrContext = context.split('/');
            var arrContextLength = arrContext.length;
            if (arrContext[arrContextLength - 1].indexOf('.') !== -1) {//resource extension present
                arrContext.splice(arrContextLength - 1, 1);//drop the last part
            }
            context = arrContext.join('/');

            //remove the trailing / if it ends with one
            if (context.match(/\/$/)) {
                context = context.substr(0, context.length - 1);
            }
            for (var prop in modifiedPaths) {
                if (modifiedPaths[prop].indexOf('/') === 0) {
                    modifiedPaths[prop] = context + modifiedPaths[prop];
                }
            }
        }
        return modifiedPaths;
	};

	/*
	 * 
	 */
	stardust.getLocationPath = function () {
        return window.location.pathname;
    };

	/*
	 * 
	 */
	stardust.cacheQueryParameter = function () {
		return cacheQueryParameter;
	};

	/*
	 * 
	 */
	stardust.initParams = function (opts) {
        if (opts || params === null) {
            var stylesheets = ['sps/themes/default/style.css'];
            var scripts = [];
            var rel = 'stylesheet';

            if (opts) {
                //cacheQueryParameter = 'version=' + version + '&appStage=' + opts.appStage + '';
            }

            params = extend(params || {
                appStage: 'P',
                configEndpoint: 'api/config',
                baseElement: document,
                modules: ['sps'],
                defaultTheme: {
                    id : 'default',
                    name : 'default',
                    stylesheets : stylesheets,
                    rel : rel,
                    scripts : scripts
                }
            }, opts || {});
        }
        return params;
	};

	/*
	 * 
	 */
	stardust.start = function () {
        console.log('Bootstrapping Stardust Portal Shell (' + stardust.version + ')...');
        injector = angular.bootstrap(document, stardust.initParams().modules);
        jQuery('body').removeClass('loading-indicator');
    };
    
    stardust.version = version;

    /*
     * 
     */
    stardust.utils = {};

    /*
     * 
     */
    stardust.utils.getRequestParam = function (name) {
        var re = new RegExp('[?&]' + encodeURIComponent(name) + '=([^&]*)', 'g');
        var res = [];
        var search = typeof(this) === 'string' ? this : window.location.search;
        var match = re.exec(search);

        while (match) {
            res.push(decodeURIComponent(match[1]));
            match = re.exec(search);
        }
        if (res.length === 0) {
            return null;
        } else if (res.length === 1) {
            return res[0];
        }
        return res;
    };

    /*
     * 
     */
    stardust.utils.getInjector = function () {
        return injector;
    };

}(window.stardust = window.sd = (window.stardust || window.sd || {})));