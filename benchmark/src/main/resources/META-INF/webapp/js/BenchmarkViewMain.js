require
		.config({
			baseUrl : "../..",
			paths : {
				'jquery' : [ 'benchmark/js/libs/jquery/jquery-1.11.0.min' ],
				'jquery-ui' : [ 'benchmark/js/libs/jquery/plugins/jquery-ui-1.10.2.custom.min' ],
				'jquery.dataTables' : [ "benchmark/js/libs/jquery/plugins/jquery.dataTables-1.9.4.min" ],
				'angularjs' : [ 'benchmark/js/libs/angular/angular-1.2.26.min' ]
			},
			shim : {
				'jquery-ui' : [ 'jquery' ],
				'angularjs' : {
					require : "jquery",
					exports : "angular"
				},
				'jquery.dataTables' : [ 'jquery' ]
			}
		});

require([ "require", "jquery", "jquery-ui", "jquery.dataTables", "angularjs",
		"benchmark/js/Utils", "benchmark/js/AngularDirectives",
		"benchmark/js/BenchmarkViewController" ], function(require, jquery,
		jqueryUi, jqueryDataTables, angularjs, Utils, AngularDirectives,
		BenchmarkViewController) {
	jQuery(document).ready(
			function() {
				var module = angularjs.module("benchmarkViewApplication",
						[]);

				module.controller('benchmarkViewController', function($scope) {

					Utils.inheritMethods($scope, BenchmarkViewController
							.create());

					$scope.initialize();
				});

				AngularDirectives.initialize(module);

				angular.bootstrap(document, [ "benchmarkViewApplication" ]);
			});
});
