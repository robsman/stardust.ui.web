require
		.config({
			baseUrl : "../../",
			paths : {
				'jquery' : [ 'business-object-management/js/libs/jquery/jquery-1.11.0.min' ],
				'jquery-ui' : [ 'business-object-management/js/libs/jquery/plugins/jquery-ui-1.10.2.custom.min' ],
				'angularjs' : [ 'business-object-management/js/libs/angular/angular-1.2.0' ],
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
				"business-object-management/js/Utils",
				"business-object-management/js/BusinessObjectManagementLaunchPanelController" ],
		function(require, jquery, jqueryUi, angularjs, Utils,
				businessObjectManagementLaunchPanelController) {
			jQuery(document).ready(
					function() {
						var module = angularjs.module(
								"businessObjectManagementLaunchPanelApplication", []);
						var controller = null;

						module.controller(
								'businessObjectManagementLaunchPanelController',
								function($scope) {
									Utils.inheritMethods($scope,
											businessObjectManagementLaunchPanelController
													.create());

									$scope.initialize();

									controller = $scope;
								});

						angular.bootstrap(document,
								[ "businessObjectManagementLaunchPanelApplication" ]);
					});
		});
