require
		.config({
			baseUrl : "../",
			paths : {
				'jquery' : [ 'document-triage/js/libs/jquery/jquery-1.11.0.min' ],
				'jquery-ui' : [ 'document-triage/js/libs/jquery/plugins/jquery-ui-1.10.2.custom.min' ],
				'angularjs' : [ 'document-triage/js/libs/angular/angular.1.2.11.min' ],
			},
			shim : {
				'jquery-ui' : [ 'jquery' ],
				'angularjs' : {
					require : "jquery",
					exports : "angular"
				}
			}
		});

require([ "require", "jquery", "jquery-ui", "angularjs",
		"document-triage/js/Utils", "document-triage/js/AngularDirectives",
		"document-triage/js/DocumentAssignmentPanelController" ], function(
		require, jquery, jqueryUi, angularjs, Utils, AngularDirectives,
		DocumentAssignmentPanelController) {
	jQuery(document).ready(
			function() {
				var module = angularjs.module(
						"documentAssignmentPanelApplication", []);

				module
						.controller('documentAssignmentPanelController',
								function($scope) {
									console.log($scope);

									Utils.inheritMethods($scope,
											DocumentAssignmentPanelController
													.create());

									$scope.initialize();

									console.log($scope);
								});
				AngularDirectives.initialize(module);
				angular.bootstrap(document,
						[ "documentAssignmentPanelApplication" ]);
			});
});
