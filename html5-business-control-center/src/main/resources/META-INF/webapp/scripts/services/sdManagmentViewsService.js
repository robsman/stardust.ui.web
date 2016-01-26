(function(){
  
  var data=[
    
    {"name" : "Processes",
     "label" : "Processes",
     "i18nKey" : "views.categoryTree.tree.processes",
     "id" : "A",
     "type" : "Folder", 
     "children":[
      
        {"name" : "ProcessManager" , 
         "label" : "Process Manager", 
         "i18nKey" : "views.processOverviewView.label",
         "viewId" : "processOverviewView" , 
         "id" : "A1", 
         "type" : "view", 
         "children" :[]}, 
         
        {"name" : "ProcessSearch" , 
         "label" :"Process Search", 
         "i18nKey" : "views.processSearchView.labelTitle",
         "viewId" : "processSearchView" , 
         "id" : "A2", 
         "type" : "view", 
         "children" :[]},
         
        {"name" : "TrafficLightView" , 
         "label" :"Traffic Light View", 
         "i18nKey" : "views.trafficLightView.label",
         "viewId" : "trafficLightView" , 
         "id" : "A3", 
         "type" : "view", 
         "children" :[]}
        
      ]},
     
     {"name" : "Activities",
     "label" : "Activities",
     "i18nKey" : "views.categoryTree.tree.activities",
     "id" : "B",
     "type" : "Folder", 
     "children":[
       
        {"name" : "CriticalityManager" , 
         "label" :"Criticality Manager", 
         "i18nKey" : "views.activityCriticalityManagerView.label",
         "viewId" : "activityCriticalityManagerView" , 
         "id" : "B1", 
         "type" : "view", 
         "children" :[]},
         
        {"name" : "PendingActivities" , 
         "label" :"Pending Activities", 
         "i18nKey" : "views.pendingActivities.labelTitle",
         "viewId" : "pendingActivities" , 
         "id" : "B2", 
         "type" : "view", 
         "children" :[]},
         
        {"name" : "CompletedActivities" , 
         "label" :"Completed Activities", 
         "i18nKey" : "views.completedActivities.labelTitle",
         "viewId" : "completedActivities" , 
         "id" : "B3", 
         "type" : "view", 
         "children" :[]},
         
        {"name" : "PostponedActivities" , 
         "label" :"Postponed Activities", 
         "i18nKey" : "views.postponedActivities.labelTitle",
         "viewId" : "postponedActivities" , 
         "id" : "B4", 
         "type" : "view", 
         "children" :[]},
         
        {"name" : "StrandedActivities" , 
         "label" :"Stranded Activities", 
         "i18nKey" : "views.strandedActivities.label",
         "viewId" : "strandedActivities" , 
         "id" : "B5", 
         "type" : "view", 
         "children" :[]}
        
      ]},
     
     {"name" : "Resources",
     "label" : "Resources",
     "i18nKey" : "views.categoryTree.tree.resources",
     "id" : "C",
     "type" : "Folder", 
     "children":[
      
        {"name" : "ResourceAvailability" , 
         "label" : "Resource Availability", 
         "i18nKey" : "views.resourceAvailabilityView.label",
         "viewId" : "resourceAvailabilityView" , 
         "id" : "C1", 
         "type" : "view", 
         "children" :[]},
         
        {"name" : "RoleAssignments" , 
         "label" :"Role Assignments", 
         "i18nKey" : "views.roleAssignmentView.labelTitle",
         "viewId" : "roleAssignmentView" , 
         "id" : "C2", 
         "type" : "view", 
         "children" :[]},
         
        {"name" : "DeputyManagement" , 
         "label" :"Deputy Management", 
         "i18nKey" : "views.deputyTeamMemberView.label",
         "viewId" : "deputyTeamMemberView" , 
         "id" : "C3", 
         "type" : "view", 
         "children" :[]},
         
        {"name" : "ResourceLogin" , 
         "label" :"Resource Login", 
         "i18nKey" : "views.resourceLoginView.label",
         "viewId" : "resourceLoginView" , 
         "id" : "C4", 
         "type" : "view", 
         "children" :[]},
         
        {"name" : "ResourcePerformance" , 
         "label" :"Resource Performance", 
         "i18nKey" : "views.resourcePerformance.label",
         "viewId" : "resourcePerformance" , 
         "id" : "C5", 
         "type" : "view", 
         "children" :[]},
         
        {"name" : "PerformanceTeamLeader" , 
         "label" :"Performance Team Leader", 
         "i18nKey" : "views.performanceTeamleader.labelTitle",
         "viewId" : "performanceTeamleader" , 
         "id" : "C6", "type" : 
         "view", "children" :[]}
       
      ]},
     
     {"name" : "CostsControlling",
     "label" : "Costs and Controlling",
     "i18nKey" : "views.categoryTree.tree.costsAndControlling",
     "id" : "D",
     "type" : "Folder", 
     "children":[
      
        {"name" : "CostsControlling" , 
         "label" : "Costs and Controlling", 
         "i18nKey" : "views.categoryTree.tree.costsAndControlling",
         "viewId" : "costs" , 
         "id" : "D1", 
         "type" : "view", 
         "children" :[]}
       
      ]},
     
     {"name" : "Reports",
     "label" : "Reports",
     "i18nKey" : "views.categoryTree.tree.reportDesigns",
     "id" : "E",
     "type" : "Folder", 
     "children":[
      
        {"name" : "ReportManagement" , 
         "label" :"Report Management", 
         "i18nKey" : "views.myReportsView.reportManagement",
         "viewId" : "myReportsView" , 
         "id" : "E1", 
         "type" : "view", 
         "children" :[]}
       
      ]}
    
  ];
  
  
  service.$inject=["$q"];
  
  //constructor
  function service($q){
    this.$q = $q;
  }
  
  //Follow the promise pattern in the event this becomes a server call...
  service.prototype.getViews = function(){
    var deferred = this.$q.defer();
    deferred.resolve(data);
    return deferred.promise;
  };
  
  angular.module("bcc-ui.services").service("sdManagementViewsService",service);
  
})();