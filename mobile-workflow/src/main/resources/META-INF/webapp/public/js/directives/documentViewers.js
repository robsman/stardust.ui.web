/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
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