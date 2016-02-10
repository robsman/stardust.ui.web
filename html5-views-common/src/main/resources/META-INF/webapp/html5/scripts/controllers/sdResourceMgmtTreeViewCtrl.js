(function(){
	
	'use strict';
	
	//inject our dependencies for the controller
	sdResourceMgmtTreeViewCtrl.$inject = [];
	
	function sdResourceMgmtTreeViewCtrl(){
		this.path="/documents,/realms";
	}

	sdResourceMgmtTreeViewCtrl.prototype.menuHook = function(menuItems,treeNode){

		//Root folders may not be renamed or deleted so remove those items.
		var isRoot = treeNode.path.match(/\//g).length === 1;

		if(isRoot===true){
			menuItems = menuItems.filter(function(v){
				return  (v.indexOf("(rename,")===-1) &&  
						(v.indexOf("(delete,")===-1) &&
						(v.indexOf("(createSubFolder,")===-1) &&
						(v.indexOf("(refreshFolder,")===-1) &&
						(v.indexOf("(securityFolder,")===-1);
			});
		}

		return menuItems;
	};
	
	//inject controller into viewscommon-ui
	angular.module("viewscommon-ui")
	.controller("sdResourceMgmtTreeViewCtrl",sdResourceMgmtTreeViewCtrl);
	
})();