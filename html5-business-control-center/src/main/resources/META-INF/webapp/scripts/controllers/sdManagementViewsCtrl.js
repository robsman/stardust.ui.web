(function(){
  
  //main module we will retrieve from angular
  var app;
  
  //TODO:need our view service and i18n service
  cntrl.$inject=["sdManagementViewsService", "sdViewUtilService", "sgI18nService"];
  
  //constructor
  function cntrl(sdManagementViewsService, sdViewUtilService, sgI18nService){
    var that = this;
    
    this.treeApi = {};
    this.sdViewUtilService = sdViewUtilService;
    this.i18n = sgI18nService.translate;
    
    sdManagementViewsService.getViews()
    .then(function(data){
      data = that.internationalizeText(data);
      that.views = data;
    });
    
  }
  
  cntrl.prototype.internationalizeText = function(data){
	  var that = this;
	  
	  if(!data.forEach){data=[data];}
	  
	  data.forEach(function(item){
		  item.label = that.i18n("business-control-center-messages." + item.i18nKey.replace(/\./g,"-"));
		  if(item.children && item.children.length > 0){
			  that.internationalizeText(item.children)
		  }
		  
	  });
    return data;
  };
  
  cntrl.prototype.onTreeInit = function(api){
    this.treeApi = api;
  };
  
  cntrl.prototype.treeEventCallback = function(data,e){
    
    var that = this;
    
    if(data.valueItem.type==="Folder"){
      data.deferred.resolve();
    }
    else if(data.treeEvent==="node-click"){
      that.openView(data.valueItem.viewId);
    }
    else{
      data.deferred.resolve();  
    }
    console.log(data,e);
  };
  
  cntrl.prototype.openView = function(viewId){
    //alert("Open: " + viewId);
    this.sdViewUtilService.openView(viewId,viewId);
  };
  
  cntrl.prototype.iconCallback = function(data,e){
    var css = ["pi","pi-lg"];
    
    if(data.type==="Folder"){
      css.push("pi-folder-open");
      return css.join(" ");
    }
    
    switch(data.name){
      case "ProcessManager":
        css.push("pi-process-manager");
        break;
      case "ProcessSearch":
        css.push("pi-search");
        break;
      case "TrafficLightView":
        css.push("pi-traffic-light");
        break;
      case "CriticalityManager":
        css.push("pi-criticality_manager");
        break;
      case "PendingActivities":
        css.push("pi-activity-pending");
        break;
      case "CompletedActivities":
        css.push("pi-activity-complete");
        break;
      case "PostponedActivities":
        css.push("pi-activity-postponed");
        break;
      case "StrandedActivities":
        css.push("pi-activity-stranded");
        break;
      case "ResourceAvailability":
        css.push("pi-resource-availability");
        break;
      case "RoleAssignments":
        css.push("pi-role-assignment");
        break;
      case "DeputyManagement":
        css.push("pi-deputy-management");
        break;
      case "ResourceLogin":
        css.push("pi-resource-login");
        break;
      case "ResourcePerformance":
        css.push("pi-resource-performance");
        break;
      case "PerformanceTeamLeader":
        css.push("pi-performance-team-lead");
        break;
      case "CostsControlling":
        css.push("pi-costs");
        break;
      case "ReportManagement":
        css.push("pi-report-management");
        break;
      default:
        css.push("pi-cog");
    }
    
    return css.join(" ");
    
  };
  
  app=angular.module("bcc-ui");
  app.controller("sdManagementViewsCtrl",cntrl);
  
})();