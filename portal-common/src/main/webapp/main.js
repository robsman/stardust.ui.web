/**
 * This is starting place, this bootstraps HTML5 Framework
 *
 * @author Subodh.Godbole
 */

(function(requirejs, sungard) {
	'use strict';

	var endpoints = sungard.prefixContext({'config' : '/services/rest/common/html5/api/config'});
	// sungard.prefixContext has bug to evaluate correct Context. Hence do this
	endpoints.config = endpoints.config.replace('/main.html/', '/');

	sungard.initParams({
		appStage: 'P', /* sungard.utils.getRequestParam('appStage') || 'P' */
		configEndpoint: endpoints.config,
		baseElement: document,
		modules: ['bpm-ui']
	});

	var pathsWithContext = sungard.prefixContext(sungard.paths());
	// sungard.prefixContext has bug to evaluate correct Context. Hence do this
	for (var prop in pathsWithContext) {
		pathsWithContext[prop] = pathsWithContext[prop].replace('/main.html/', '/');
	}
	var packages = sungard.packages(['bpm-ui', 'shell', 'sg-components']);

	var r = requirejs.config({
		paths: pathsWithContext,
		shim: sungard.shim(),
		packages: packages,
		urlArgs: sungard.cacheQueryParameter()
	});

	// invoke initialization
	r(['sg-components', 'shell', 'bpm-ui'
		], function() {
			// everything is loaded, start application
			sungard.start();
		}
	);
})(requirejs, sungard);