console.log("Before require");

require
		.config({
			baseUrl : "plugins/",
			paths : {
				'jquery' : [ 'business-object-management/js/libs/jquery/jquery-1.11.0.min' ],
				'jquery-ui' : [ 'business-object-management/js/libs/jquery/plugins/jquery-ui-1.10.2.custom.min' ],
				'jquery.dataTables' : [ "business-object-management/js/libs/jquery/plugins/jquery.dataTables-1.9.4.min" ],
				'angularjs' : [ 'business-object-management/js/libs/angular/angular-1.2.26.min' ],
				'angular-datatables' : [ 'business-object-management/js/libs/jquery/plugins/angular-datatables.min' ],
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

require(
		[ "require", "jquery", "jquery-ui", "jquery.dataTables", "angularjs",
				"plugins/business-object-management/js/Utils",
				"plugins/business-object-management/js/AngularDirectives",
				"plugins/business-object-management/js/BusinessObjectManagementViewController" ],
		function(require, jquery, jqueryUi, jqueryDataTables, angularjs, Utils,
				AngularDirectives, BusinessObjectManagementViewController) {
			jQuery(document).ready(
					function() {
						console.log("Document ready");

						var module = angularjs.module(
								"businessObjectManagementViewApplication",
								[]);

						module.controller(
								'businessObjectManagementViewController',
								function($scope) {

									Utils.inheritMethods($scope,
											BusinessObjectManagementViewController
													.create());

									$scope.initialize();
								});

						AngularDirectives.initialize(module);

						angular.bootstrap(document,
								[ "businessObjectManagementViewApplication" ]);

						console.log("Angular boostrapped =============>");
					});
		});
