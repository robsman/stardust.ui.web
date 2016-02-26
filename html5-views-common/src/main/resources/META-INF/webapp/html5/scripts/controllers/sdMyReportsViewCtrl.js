(function(){
	
	'use strict';
	
	//inject our dependencies for the controller
	sdMyReportsViewCtrl.$inject = ["sdReportsService", "sdI18nService", "$q"];
	
	function sdMyReportsViewCtrl(sdReportsService, sdI18nService, $q){
		var that = this,
			i18n;

		this.paths= null;
		this.grants = [];
		this.personalReports = [];

		i18n = sdI18nService.getInstance('views-common-messages');
		this.header = i18n.translate("views.myReportsView.header");

		sdReportsService.getCurrentUser()
		.then(function(user){
			var promises =[];
			promises.push(sdReportsService.getRoleOrgReportDefinitionGrants(user.id));
			promises.push(sdReportsService.getPersonalReports());
			promises.push(sdReportsService.getReportPaths(user.myDocumentsFolderPath));
			return $q.all(promises);
		})
		.then(function(vals){
			that.grants = vals[0];
			that.personalReports = vals[1];
			that.paths = vals[2];
		})
		["catch"](function(err){
			//TODO:err handling
		});

	}

	sdMyReportsViewCtrl.prototype.menuHook = function(menuItems,treeNode){

		//Root folders may not be renamed or deleted so remove those items.
		var isRoot = treeNode.path.match(/\//g).length === 1;

		if(isRoot===true){
			menuItems = menuItems.filter(function(v){
				return  (v.indexOf("(rename,")===-1) &&  
						(v.indexOf("(delete,")===-1)
			});
		}

		return menuItems;
	};
	
	//inject controller into viewscommon-ui
	angular.module("viewscommon-ui")
	.controller("sdMyReportsViewCtrl",sdMyReportsViewCtrl);
	
})();