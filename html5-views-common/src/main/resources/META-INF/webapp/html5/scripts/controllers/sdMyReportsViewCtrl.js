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
		this.particpantPaths = {};
		i18n = sdI18nService.getInstance('views-common-messages');
		this.header = i18n.translate("views.myReportsView.header");
		this.user = {};

		//Important that we invoke the service for participant paths first as that contains server side code for initialization of
		//of the root report folder and its subfolders in the case where the root report folder does not exist.
		// 1: ROOT/reports 2: ROOT/reports/designs 3: ROOT/reports/saved-reports
		sdReportsService.getParticipantReports() //#3 Role/Org paths, .designs = ReportDefinitions, .saved-reports=Saved-Reports
		.then(function(paths){
			that.participantPaths = paths;
			return sdReportsService.getCurrentUser();//retrieve current user and then build up all paths 
		})
		//Next we need to verify and build out (if neccessary) the structure for our Saved-Reports Node.
		//This includes the Report root of the users document path as well as the Report roots two children [saved-reports,designs]
		.then(function(user){
			that.user = user;
			return sdReportsService.verifySavedReportsStructure(user.myDocumentsFolderPath);
		})
		//Now that we are confident that our underlying folder structure exists, build out our path hash maps
		.then(function(){

			var promises =[
				sdReportsService.getRoleOrgReportDefinitionGrants(that.user.id), //retrieve grants
				sdReportsService.getReportDefinitionPaths(that.user.myDocumentsFolderPath), //#1,#2 Public/Private Report Defintions
				sdReportsService.getSavedReportsPaths(that.user.myDocumentsFolderPath)//, //#4,#5 Saved-Reports node, public and private folders
			];
			
			return $q.all(promises);
		})
		.then(function(vals){

			var grants = vals[0]; 
			var grantMap = {};
			var reportDefinitionPaths = vals[1];
			var savedReportPaths = vals[2];

			//build grant map from which we will acquire folder names for participant reports
			grants.forEach(function(grant){
				grantMap[grant.qualifiedId] = grant;
			});

			//Loop through all designs and add then to the reportDefinition hash map
			that.participantPaths["designs"].forEach(function(item){

				var grant;
				var qualifiedId;
				var name;

				qualifiedId = item.path.split("/")[2];
				grant = grantMap[qualifiedId];
				name = (!grant)?qualifiedId:grant.name;

				reportDefinitionPaths[item.path] =name;

			});
			that.reportDefinitionPaths = reportDefinitionPaths;

			//Loop through all designs and add then to the saved-reports hash map
			that.participantPaths["saved-reports"].forEach(function(item){

				var grant;
				var qualifiedId;
				var name;
				
				qualifiedId = item.path.split("/")[2];
				grant = grantMap[qualifiedId];
				name = (!grant)?qualifiedId:grant.name;

				savedReportPaths[item.path] = name;

			});
			that.savedReportPaths = savedReportPaths;
			

		})
		["catch"](function(err){
			//TODO:err handling
		});

	}
	
	sdMyReportsViewCtrl.prototype.customSort = function(a,b){

		var name1 = (a.name)?a.name.toUpperCase():a.path,
        name2 = (b.name)?b.name.toUpperCase():b.path;

        if(a.name==="PUBLIC REPORT DEFINITIONS"){
        	return -1;
        }

        if(a.name==="PRIVATE REPORT DEFINITIONS"){
        	return -1;
        }

	    if(a.nodeType !== b.nodeType){
	      if(a.nodeType==="folder"){return -1;}
	      else{return 1;}
	    }
	    else if(name1===name2){return 0;}
	    else if(name1 < name2){return -1;}
	    else{return 1;}
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