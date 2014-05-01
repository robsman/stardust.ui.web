define(["jquery"],function(){

	var directives={
			
	   "testTemplate" : function(){
		    return{
		        restrict: 'EA',
		        templateUrl: "templates/test.html"
		    };
		},

		"jqmLoader" : function(){
		    
		    var link=function(scope,element,attrs){
		      console.log(element);
		      
		      var options = {
		        "html"        : attrs.jqmHtml || "",
		        "textVisible" : attrs.jqmTextVisible ==='true',
		        "textonly"    : attrs.jqmTextOnly === 'true',
		        "text"        : attrs.jqmText || "",
		        "theme"       : attrs.jqmTheme || "a"
		      };
		      
		      $(element).loader(options);
		      
		      attrs.$observe("jqmShowLoader",function(val){
		          if(val==true || val=='true'){
		            $(element).loader('show');
		          }
		          else{
		            $(element).loader('hide');
		          }
		      });
		        
		      attrs.$observe("jqmTextVisible",function(val){
                val = (val === 'true' || val === true);
                $( element ).loader( "option", "textVisible", val );
              });
	              
              attrs.$observe("jqmTextOnly",function(val){
                val = (val === 'true' || val === true);
                $( element ).loader( "option", "textonly", val );
              });
              
              attrs.$observe("jqmText",function(val){
                $( element ).loader( "option", "text", val );
              });
              
              attrs.$observe("jqmTheme",function(val){
                $( element ).loader( "option", "theme", val );
              });
		        
		    };
		
		    return {
		      link:link
		    };
		    
		},
            
	    
	  "jqmPopup" : function(){
		  
		  /*Linking function, 
		   * 1. Assign our observers to our data attributes,
		   * 2. Convert our element into a Jquery Mobile popup*/
		  var link=function(scope, element, attrs){
              console.log("JQM attrs");
              console.log(attrs);
	          var options={
	 					  "corners" : attrs.jqmCorners || true,
	 					  "disabled" : attrs.jqmDisabled || false,
	 					  "dismissible" : attrs.jqmDismissible || false,
	 					  "history" : attrs.jqmHistory || false,
	 					  "overlayTheme" : attrs.jqmOverlayTheme || "b",
	 					  "positionTo" : attrs.jqmPositionTo || "origin",
	 					  "shadow" : attrs.jqmShadow || false,
	 					  "theme" : attrs.jqmTheme || "a",
	 					  "tolerance" : attrs.jqmTolerance || "0,0",
	 					  "transition" : attrs.jqmTransition || "pop"
	 					  
	 			  },
	 			  $popup=$(element).popup(options),
			      observerFunc=function(prop,val){
	        	  		console.log("jqmpopup observer...");
	        	  		console.log(prop + "  :  " + val);
	        	  		$popup.popup( "option", prop,val );
			  	  };
 
             /*Observe our options*/
             attrs.$observe("jqmCorners",function(val){
               observerFunc("corners",val);
             });
             
             attrs.$observe("jqmDisabled",function(val){
               observerFunc("disabled",val);
             });
             
             attrs.$observe("jqmDismissible",function(val){
               observerFunc("dismissible",val);
             });
             
             attrs.$observe("jqmHistory",function(val){
               observerFunc("history",val);
             });
             
             attrs.$observe("jqmOverlayTheme",function(val){
               observerFunc("overlayTheme",val);
             });
             
             attrs.$observe("jqmPositionTo",function(val){
               observerFunc("positionTo",val);
             });
             
             attrs.$observe("jqmShadow",function(val){
               observerFunc("shadow",val);
             });
             
             attrs.$observe("jqmTheme",function(val){
               observerFunc("theme",val);
             });
             
             attrs.$observe("jqmTolerance",function(val){
               observerFunc("tolerance",val);
             });
             
             attrs.$observe("jqmTransition",function(val){
               observerFunc("transition",val);
             });
             
             /*Observe our data attribute which maps to our open/close method call*/
		     attrs.$observe("jqmOpen",function(val){
		       if(val=="true"){
		         $popup.popup("open");
		       }else{
		         $popup.popup("close");
		       }
		     });
		  };/*Link Function Ends*/
		  
		  return {
              restrict: 'EA',
              scope:true, /*Complete isolate scope*/
              link: {post:link}
          };
		  
	  },
	  
	  "jqmTemplate" : function () {
		    var link=function(scope, element, attrs){
		    	console.log("linking function...");
		        console.log(element);
		        /*Acquire array of all jqm widget dependencies in our template, must
		          be declared on our data-jqm-widgets attribute as a 
		          comma delimited list*/
		        var widgetTypes=attrs.jqmWidgets.split(","),
		            widget,$widget,$widgets=[];
		        
		        /*build collection of all JQM widgets declared on our directive,
		         *corresponding template must declare them using data-role attributes*/    
		        for(var i=0;i<widgetTypes.length;i++){
		            widget=widgetTypes[i];
		            $widget=$("[data-role='" + widget + "']",element);
		            if($widget.length>0 && $widget[widget]){
		              $widget[widget]();
		              $widgets.push($widget);
		            };
		        }
		        
		      $(element).enhanceWithin();
		      /*TODO: Investigate $watch vs $watchCollection vs $watch(deep=true)*/
		      /*TODO: detect when data-bind-to is not present and adjust accordingly*/
		      scope.$watchCollection(attrs.bindTo,function(){
		    	  console.log("change in ... " +  attrs.bindTo);
		    	  $(element).enhanceWithin();
		    	  console.log("enhanceWithin...");
		         $($widgets).each(function(){
		           try{
		        	   this[this.attr("data-role")]("refresh");
		        	   console.log("refreshed " + this.attr("data-role"));
		           }catch(ex){
		        	   console.log("no refresh method for " + this.attr("data-role"));
		           }
		           $(element).enhanceWithin();
		         });
		      },true); 

		    };
		    
		    return {
		        restrict: 'EA',
		        scope:false, /*inherit parent scope with no restrictions*/
		        templateUrl : function(element, attr) { return attr.templateUrl;},
		        link: {post:link}
		    };
		  }
	
	};
	
	return directives;

});