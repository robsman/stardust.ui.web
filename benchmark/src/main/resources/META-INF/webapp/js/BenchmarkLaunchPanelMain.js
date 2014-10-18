require
		.config({
			baseUrl : "../../",
			paths : {
				'jquery' : [ 'simple-modeler/js/libs/jquery/jquery-1.11.1.min' ],
				'jquery-ui' : [ 'simple-modeler/js/libs/jquery/plugins/jquery-ui-1.10.2.custom.min' ],
				'angularjs' : [ 'simple-modeler/js/libs/angular/angular-1.0.2' ],
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
				}
			}
		});

require(
		[ "require", "jquery", "jquery-ui", "angularjs",
				"benchmark/js/Utils",
				"benchmark/js/BenchmarkLaunchPanelController" ],
		function(require, jquery, jqueryUi, angularjs, Utils,
				benchmarkLaunchPanelController) {
			jQuery(document)
					.ready(
							function() {
								var module = angularjs
										.module(
												"benchmarkLaunchPanelApplication",
												[]);
								var controller = null;

								module
										.controller(
												'benchmarkLaunchPanelController',
												function($scope) {
													Utils.inheritMethods(
															$scope,
															benchmarkLaunchPanelController
																	.create());

													$scope.initialize();

													controller = $scope;
												});

								angular
										.bootstrap(
												document,
												[ "benchmarkLaunchPanelApplication" ]);
							});
		});
