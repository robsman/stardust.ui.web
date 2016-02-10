(function(){
	
	'use strict';
	
	//inject our dependencies for the controller
	sdMyDocumentsTreeViewCtrl.$inject = [];
	
	function sdMyDocumentsTreeViewCtrl(){
		this.path="/documents,/realms";
	}

	sdMyDocumentsTreeViewCtrl.prototype.menuHook = function(menuItems,treeNode){

		//Root folders may not be renamed or deleted so remove those items.
		var isRoot = treeNode.path.match(/\//g).length === 1;

		if(isRoot===true){
			menuItems = menuItems.filter(function(v){
				return (v.indexOf("(rename,")===-1 &&  v.indexOf("(delete,")===-1);
			});
		}

		return menuItems;
	};
	
	//inject controller into viewscommon-ui
	angular.module("viewscommon-ui")
	.controller("sdMyDocumentsTreeViewCtrl",sdMyDocumentsTreeViewCtrl);
	
})();