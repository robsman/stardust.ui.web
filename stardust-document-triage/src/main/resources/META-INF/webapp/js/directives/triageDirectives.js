define([],function(){
	return {
		init: function(angular,jQuery,window){
			
			var mod = angular.module("triageDirectives",[]);
			
			/**
			 * When applied to a DOM element this will apply box-model
			 * to the element and apply a calculated height so as to stretch 
			 * the element to the bottom of the viewable screen;
			 */
			mod.directive("ngStretchV",function($timeout){
		        
		        /*calculate height needed to reach bottom of page*/
				var adjustHeight=function(elem,fudge){
		          
		          var  margin = angular.element(document.body).css("marginBottom"),
		          	   offsetTop = jQuery(elem).offset().top;
		               height = (window.innerHeight <1)?window.outerHeight:window.innerHeight;
		                  	
		          height = height-offsetTop;

		          margin=parseInt(margin);
		          height = height - margin + fudge;
		          elem.css("height",height + "px");
		          elem.css("box-sizing","border-box");
		          elem.css("max-height",height + "px");
		          elem.css("min-height",height + "px");
		 
		        };
		        
		        return {
		          link: function ($scope, element, attrs) {

		        	  	 var fudge = (attrs.fudge || 0),
		        	  	 	 i=0;
		        	  	 
		        	  	 fudge=fudge *1;
		        	  	 
		        	  	 if(attrs.watch){
		                    watchElem = jQuery(attrs.watch);
		                    if(watchElem.length >0){
		                      for(;i<watchElem.length;i++){
		                        $scope["watchElem" + i] = watchElem[i];
		                        $scope.$watch("watchElem" + i + ".offsetTop",function(){
		                          adjustHeight(element,fudge);
		                        });
		                      }
		                    }
		                  }
		        	  	 
		        	  	 
		        	  	 $(window).on("resize",function(){
		                    adjustHeight(element,fudge);
		                 });
		                 
		                 $scope.element = element[0];
		                 
		                 $scope.$watch("element.offsetTop",function(){
		                    adjustHeight(element,fudge);
		                 });
		                 
		                 $scope.$watch("window.innerHeight",function(v){
		                	adjustHeight(element,fudge);
		                 });
		                 
		                 $scope.$watch("window.height",function(v){
		                	adjustHeight(element,fudge);
			             });
		                 
		                 angular.element(document).ready(function(){
		                	 $timeout(function(){adjustHeight(element,fudge);},0);
		                 });
		                 
		              }
		            };
		          
		       });/** ngStretchV end **/
			
			
			
			
			
		}
	};
});