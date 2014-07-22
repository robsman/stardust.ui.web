require
		.config({
			baseUrl : "../..",
			paths : {
				'jquery' : [ 'business-object-management/js/libs/jquery/jquery-1.11.0.min' ],
				'jquery-ui' : [ 'business-object-management/js/libs/jquery/plugins/jquery-ui-1.10.2.custom.min' ],
				'angularjs' : [ 'business-object-management/js/libs/angular/angular-1.2.0' ],
				'jquery.dataTables' : [ "business-object-management/js/libs/jquery/plugins/jquery.dataTables-1.9.4.min" ],
			},
			shim : {
				'jquery-ui' : [ 'jquery' ],
				'angularjs' : {
					require : "jquery",
					exports : "angular"
				},
				'jquery.dataTables' : [ 'jquery' ],
			}
		});

require([ "require", "jquery", "jquery-ui", "angularjs", "jquery.dataTables",
		"business-object-management/js/Utils", "business-object-management/js/AngularDirectives",
		"business-object-management/js/BusinessObjectManagementViewController" ], function(
		require, jquery, jqueryUi, angularjs, jqueryDataTables, Utils,
		AngularDirectives, BusinessObjectManagementViewController) {
	jQuery(document).ready(
			function() {
				var module = angularjs.module(
						"businessObjectManagementViewApplication", []);

				module
						.controller('businessObjectManagementViewController',
								function($scope) {
									console.log($scope);

									Utils.inheritMethods($scope,
											BusinessObjectManagementViewController
													.create());

									$scope.initialize();
								});
				AngularDirectives.initialize(module);
				angular.bootstrap(document,
						[ "businessObjectManagementViewApplication" ]);
			});
});
