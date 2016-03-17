(function(){
	
	'use strict';
	
	//inject our dependencies for the controller
	sdMyReportsViewCtrl.$inject = ["sdReportsService", "sdI18nService", "$q", "eventBus"];
	
	function sdMyReportsViewCtrl(sdReportsService, sdI18nService, $q, eventBus){
		var that = this,
			i18n;

		this.paths = null;
		this.savedReportsPaths = null;
		this.grants = [];
		this.personalReports = [];
		this.eventBus = eventBus;

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

	sdMyReportsViewCtrl.prototype.eventHook = function(data,e){
		if(data.treeEvent==="node-delete" && data.valueItem.nodeType==="document"){
			this.eventBus.emitMsg("myReportsView.file.delete",data.valueItem);
		};
	};

	/**
	 * Hook into the sdDocumentRepository directive to allow us
	 * to override the default menu options available.
	 * @param  {[Array]} menuItems [description]
	 * @param  {[type]} treeNode  [description]
	 * @return {[type]}           [description]
	 */
	sdMyReportsViewCtrl.prototype.menuHook = function(menuItems,treeNode){

		//Root folders may not be renamed or deleted so remove those items.
		var isRoot = treeNode.path.match(/\//g).length === 1;

		if(isRoot===true || treeNode.nodeType==="folder"){
			menuItems = menuItems.filter(function(v){
				return  (v.indexOf("(rename,")===-1) &&  
						(v.indexOf("(delete,")===-1) &&
						(v.indexOf("(createFile,")===-1);
			});
		}

		return menuItems;
	};
	
	//inject controller into viewscommon-ui
	angular.module("viewscommon-ui")
	.controller("sdMyReportsViewCtrl",sdMyReportsViewCtrl);
	
})();