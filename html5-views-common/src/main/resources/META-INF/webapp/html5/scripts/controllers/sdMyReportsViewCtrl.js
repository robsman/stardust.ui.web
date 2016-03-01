(function(){
	
	'use strict';
	
	//inject our dependencies for the controller
	sdMyReportsViewCtrl.$inject = ["sdReportsService", "sdI18nService", "$q"];
	
	function sdMyReportsViewCtrl(sdReportsService, sdI18nService, $q){
		var that = this,
			i18n;

		this.paths = null;
		this.savedReportsPaths = null;
		this.grants = [];
		this.personalReports = [];

		i18n = sdI18nService.getInstance('views-common-messages');
		this.header = i18n.translate("views.myReportsView.header");

		sdReportsService.getCurrentUser()
		.then(function(user){
			var promises =[
				sdReportsService.getRoleOrgReportDefinitionGrants(user.id),
				sdReportsService.getPersonalReports(),
				sdReportsService.getReportPaths(user.myDocumentsFolderPath)
			];
			return $q.all(promises);
		})
		.then(function(vals){

			var paths = angular.extend({},vals[2]);;
			var grants = vals[0];
			var grantMap = {};
			var personalReports ={};
			var savedReports ={};

			//Make a hashmap of our grants to speed up access as we will need
			//to resolve every report parent folder.
			grants.forEach(function(grant){
				grantMap[grant.qualifiedId] = grant;
			});
			
			//Caluclate each hashMap path key and name for personal reports.
			vals[1].designs.forEach(function(item){

				var grantName;
				var parsedQID;

				parsedQID = item.path.split("/")[2];
				grantName = grantMap[parsedQID].name;
				personalReports[item.path]=grantName;

			});

			//Caluclate each hashMap path key and name for saved reports.
			vals[1]["saved-reports"].forEach(function(item){

				var grantName;
				var parsedQID;

				parsedQID = item.path.split("/")[2];
				grantName = grantMap[parsedQID].name;
				savedReports[item.path]=grantName;

			});

			//hash map of all paths
			paths = angular.extend({},paths,personalReports,savedReports);
			that.paths = paths;

			//TODO:Saved Reports
			that.savedReportsPaths = {};
			

		})
		["catch"](function(err){
			//TODO:err handling
		});

	}

	//The personal reports request returns our folder paths but not with the 
	//info we need for our friendly display name.
	sdMyReportsViewCtrl.prototype.buildPersonalReportsPaths = function(grants,reports){
		var persRptPaths = {};

		//TODO: for each item in reports need to look up the proper name from the grants array
		//and assing that key:value as an entry in persRptPaths
		
		//Wrong imlementation
		/*
		grants.forEach(function(grant){
			var key = "/reports/designs/" + grant.qualifiedId;
			grantPaths.push({ key : grant.name});
			persRptPaths[key] = 
		});
		*/
		return persRptPaths;
	};

	sdMyReportsViewCtrl.prototype.menuHook = function(menuItems,treeNode){

		//Root folders may not be renamed or deleted so remove those items.
		var isRoot = treeNode.path.match(/\//g).length === 1;

		if(isRoot===true || treeNode.nodeType==="folder"){
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