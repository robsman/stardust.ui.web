define(["angularjs","jquery"],function(angular){

	var directives={
			/*
		 "jqSmartZoom" :function(){
			    
		    var link=function($scope, element, attributes){
		            attributes.$observe("src",function(val){
		              if(val && $(element).smartZoom){
		                if($(element).smartZoom('isPluginActive')===false){
		                   $(element).smartZoom({"containerBackground" : "#FF0000"});
		                }
		              }
		              else{
		            	  console.log("smartZoom failed to load");
		            	  console.log(element);
		              }
		            });
		    };
		    
		    return{
		      "restrict" : "A",
		      "link": {"post" : link}
		    };
		    
		  },*/

		 "imageViewer" : function(){
			 
			var ngInjector = angular.injector(["ng"]),
			     $sce = ngInjector.get("$sce");
			
		    return{
		    	"restrict" : 'EA',
		    	"scope" : {
		    		'url' : '@repositoryUrl'
		    	},
		        "template" : "<img ng-src='{{url}}'/>",
		        "link" : {post : function($scope,element,attr){
			            /*Future goodness goes here*/
		        	}
		          }/*Link function ends*/
	    		};
		    }
	};
	
	return directives;

});