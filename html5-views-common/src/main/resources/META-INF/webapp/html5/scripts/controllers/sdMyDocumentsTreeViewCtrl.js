(function(){
	
	'use strict';
	
	//inject our dependencies for the controller
	sdMyDocumentsTreeViewCtrl.$inject = ["sdReportsService", "sdI18nService"];
	
	/**
	 * [sdMyDocumentsTreeViewCtrl description]
	 * @param  {[type]} sdReportsService [description]
	 * @param  {[type]} sdI18nService    [description]
	 * @return {[type]}                  [description]
	 */
	function sdMyDocumentsTreeViewCtrl(sdReportsService, sdI18nService){

		var that = this;
		var i18n = sdI18nService.getInstance('views-common-messages');

		sdReportsService.getCurrentUser()
		.then(function(user){
			var paths = {};
			paths["/documents"] = i18n.translate("views.myDocumentsTreeView.documentTree.commonDocumentsFolderLabel");
			paths[user.myDocumentsFolderPath] = i18n.translate("views.myDocumentsTreeView.documentTree.myDocuments");
			that.path = paths;
		})
		["catch"](function(err){
			//TODO:err handling
			alert("error");
		});

		//this.path="/documents,/realms";
	}

	/**
	 * Hook function to intercept the menu creation callback of the document repository
	 * directive.
	 * @param  {[type]} menuItems [description]
	 * @param  {[type]} treeNode  [description]
	 * @return {[type]}           [description]
	 */
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