require
		.config({
			baseUrl : "../",
			paths : {
				'jquery' : [ 'document-triage/js/libs/jquery/jquery-1.11.0.min' ],
				'jquery-ui' : [ 'document-triage/js/libs/jquery/plugins/jquery-ui-1.10.2.custom.min' ],
				'angularjs' : [ 'document-triage/js/libs/angular/angular-1.2.0' ],
				'ngDialog'  : ['document-triage/js/directives/ngDialog'],
 				'jquery.dataTables' : [ "document-triage/js/libs/jquery/plugins/jquery.dataTables-1.9.4.min" ]
			},
			shim : {
				'jquery-ui' : [ 'jquery' ],
				'angularjs' : {
					require : "jquery",
					exports : "angular"
				},
				'ngDialog' : {require: "angular"},
				'jquery.dataTables' : [ 'jquery' ]
			}
		});

require([ "require", "jquery", "jquery-ui", "angularjs", "jquery.dataTables",
		"document-triage/js/Utils", "document-triage/js/AngularDirectives",
		"document-triage/js/DocumentAssignmentPanelController","ngDialog"], function(
		require, jquery, jqueryUi, angularjs, jqueryDataTables, Utils,
		AngularDirectives, DocumentAssignmentPanelController,ngDialog) {
	jQuery(document).ready(
			function() {
				
				var module;
				ngDialog.init(angularjs,window);
				module = angularjs.module("documentAssignmentPanelApplication", ["ngDialog"]);

				module.controller('documentAssignmentPanelController',function($scope,ngDialog) {
					$scope.ngDialog = ngDialog;	
					Utils.inheritMethods($scope,DocumentAssignmentPanelController.create());
					$scope.initialize();
				});
				
				AngularDirectives.initialize(module);
				angular.bootstrap(document,[ "documentAssignmentPanelApplication" ]);
				
			});
});
