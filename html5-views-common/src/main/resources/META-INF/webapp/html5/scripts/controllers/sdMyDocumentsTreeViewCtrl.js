(function(){
	
	'use strict';
	
	//inject our dependencies for the controller
	sdMyDocumentsTreeViewCtrl.$inject = ["sdReportsService", "sdI18nService", "sdMyDocumentsTreeService"];
	
	/**
	 * [sdMyDocumentsTreeViewCtrl description]
	 * @param  {[type]} sdReportsService [description]
	 * @param  {[type]} sdI18nService    [description]
	 * @return {[type]}                  [description]
	 */
	function sdMyDocumentsTreeViewCtrl(sdReportsService, sdI18nService, sdMyDocumentsTreeService){

		var that = this;
		
		var i18n = sdI18nService.getInstance('views-common-messages');
		this.header = i18n.translate("views.myDocumentsTreeView.header");
		this.currentUser = {};

		this.sdMyDocumentsTreeService = sdMyDocumentsTreeService;

		sdReportsService.getCurrentUser()
		.then(function(user){
			that.currentUser = user;
			return that.buildDefaultStructure(user);
		})
		.then(function(){
			var paths = {};
			paths["/documents"] = i18n.translate("views.myDocumentsTreeView.documentTree.commonDocumentsFolderLabel");
			paths[that.currentUser.myDocumentsFolderPath] = i18n.translate("views.myDocumentsTreeView.documentTree.myDocuments");
			that.path = paths;
		})
		["catch"](function(err){
			//TODO:err handling
			alert("error");
		});

		//this.path="/documents,/realms";
	}

	sdMyDocumentsTreeViewCtrl.prototype.buildDefaultStructure = function(user){
		return this.sdMyDocumentsTreeService.buildDefaultStructure(user);
	};

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