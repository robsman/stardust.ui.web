(function(){
	
	'use strict';
	
	//inject our dependencies for the controller
	sdMyDocumentsTreeViewCtrl.$inject = [];
	
	//controller constructor
	function sdMyDocumentsTreeViewCtrl(){
		this.path="/documents,/realms";
	}
	
	//inject controller into viewscommon-ui
	angular.module("viewscommon-ui")
	.controller("sdMyDocumentsTreeViewCtrl",sdMyDocumentsTreeViewCtrl);
	
})();