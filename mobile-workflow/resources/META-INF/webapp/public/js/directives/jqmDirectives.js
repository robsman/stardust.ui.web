define([],function(){

	var directives={
			
	  "testTemplate" : function(){
		    return{
		        restrict: 'EA',
		        templateUrl: "templates/test.html"
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