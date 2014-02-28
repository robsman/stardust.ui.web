define(["angularjs"],function(angular){

	var directives={
		
		 "imageViewer" : function(){
			 
			var ngInjector = angular.injector(["ng"]),
			     $sce = ngInjector.get("$sce");
			
		    return{
		    	"restrict" : 'EA',
		    	"scope" : {
		    		'url' : '@repositoryUrl'
		    	},
		        "template" : "<img ng-src='{{url}}'/>",
		        "link" : function(scope,element,attr){
		            /*Future goodness goes here*/
		          }/*Link function ends*/
		    
	    		};/*Return object ends*/
	    		
		    }/*fileUpload function ends*/
	};
	
	return directives;

});