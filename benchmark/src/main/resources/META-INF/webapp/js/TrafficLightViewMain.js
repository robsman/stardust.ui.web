require
		.config({
			baseUrl : "../..",
			paths : {
				'jquery' : [ 'benchmark/js/libs/jquery/jquery-1.11.0.min' ],
				'jquery-ui' : [ 'benchmark/js/libs/jquery/plugins/jquery-ui-1.10.2.custom.min' ],
				'jquery.dataTables' : [ "benchmark/js/libs/jquery/plugins/jquery.dataTables-1.9.4.min" ],
				'angularjs' : [ 'benchmark/js/libs/angular/angular-1.0.2' ],
				moments : [ 'business-calendar/html5/libs/moments/moments-with-langs.min' ],
				fullcalendar : [ 'business-calendar/html5/libs/fullcalendar/fullcalendar' ],
 				'i18n' : 'common/InfinityBPMI18N'
			},
			shim : {
				'jquery-ui' : [ 'jquery' ],
				'angularjs' : {
					require : "jquery",
					exports : "angular"
				},
				'i18n' : {
					exports : "InfinityBPMI18N"
				},
				'jquery.dataTables' : [ 'jquery' ]
			}
		});

require([ "require", "jquery", "jquery-ui", "jquery.dataTables", "angularjs", "moments", "fullcalendar",
		"benchmark/js/Utils",
		"business-object-management/js/AngularDirectives",
		"benchmark/js/TrafficLightViewController" ], function(require, jquery,
		jqueryUi, jqueryDataTables, angularjs, moments, fullcalendar, Utils, AngularDirectives,
		TrafficLightViewController) {
	jQuery(document).ready(function() {
		var module = angularjs.module("trafficLightViewApplication", []);

		module.controller('trafficLightViewController', function($scope) {

			Utils.inheritMethods($scope, TrafficLightViewController.create());

			$scope.initialize();
		});

		AngularDirectives.initialize(module);

		angular.bootstrap(document, [ "trafficLightViewApplication" ]);
	});
});
