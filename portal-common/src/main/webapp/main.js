/**
 * This is starting place, this bootstraps HTML5 Framework
 *
 * @author Subodh.Godbole
 */

(function(requirejs, sg) {
	'use strict';

	var endpoints = sg.prefixContext({'config' : '/services/rest/common/html5/api/config?random=' + Math.floor(Math.random()*10000)+1});
	// sg.prefixContext has bug to evaluate correct Context. Hence do this
	endpoints.config = endpoints.config.replace('/main.html/', '/');

	sg.initParams({
		appStage: sg.utils.getRequestParam('appStage') || 'P',
		configEndpoint: endpoints.config,
		baseElement: document,
		modules: ['bpm-ui']
	});

	var pathsWithContext = sg.prefixContext(sg.paths());
	// sg.prefixContext has bug to evaluate correct Context. Hence do this
	for (var prop in pathsWithContext) {
		pathsWithContext[prop] = pathsWithContext[prop].replace('/main.html/', '/');
	}
	var packages = sg.packages(['bpm-ui', 'shell', 'sg-components']);

	var r = requirejs.config({
		paths: pathsWithContext,
		shim: sg.shim(),
		packages: packages,
		waitSeconds: 0,
		urlArgs: sg.cacheQueryParameter()
	});

	// invoke initialization
	r(['sg-components', 'shell', 'bpm-ui'
		], function() {
			// everything is loaded, start application
			sg.start();
		}
	);
})(requirejs, sg);