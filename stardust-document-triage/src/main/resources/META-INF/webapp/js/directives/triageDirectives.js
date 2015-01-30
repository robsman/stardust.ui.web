define([],function(){
	return {
		init: function(angular,jQuery,window){
			
			var mod = angular.module("triageDirectives",[]);
			
			mod.directive("ngOnLoad",function(){
				
				/**
				 * Directive to allow mapping of a callback handler to a load event.
				 * function map is of the form fn(eventData). 
				 */
				return {
			          restrict: 'A',
			          scope: { ngOnLoad: '&' },
			          link: function(scope, element, attrs) {
			            element.bind('load', function(e) {
			              scope.ngOnLoad({eventData: e});
			            });
			          }
		        };
			});
			
			/**
			 * Directive to monitor mousemove events over an element
			 * and scroll that element towards the top or bottom dependant on
			 * 
			 * 	1. mouse button is depressed (drag events)
			 *  2. location of the event coincides with a user defined top and bottom
			 *     hotzone/margin
			 * 
			 * This directive was designed to handle the specific case where a user 
			 * initiates a drag outside of a scrollable container and then attempts 
			 * to drop into the container on an element occluded by the scroll view.
			 */
			mod.directive("ngDragScroll",function(){
		        
		        var that = this;
		        that.mousedown=false;
		        
		        /*Add document monitors to track mouse[down|up]*/
		        angular.element(document)
		        .bind("mousedown",function(e){
		           that.mousedown=true;
		        })
		        .bind("mouseup",function(e){
		           that.mousedown=false;
		        });
		        
		        return {
		          restrict: 'A',
		          link: function(scope, element, attrs) {
		            
		            var offsetTop = element.offset().top,
		                elemHeight=element[0].getBoundingClientRect().height,
		                relPos,
		                margin,
		                step;
		                
		            margin = (attrs.margin || 50)*1;
		            step   = (attrs.step || 10)*1;
		            
		            element.bind('mousemove', function(e) {
		            	
		              if(that.mousedown){
		            	elemHeight=element[0].getBoundingClientRect().height;
		            	offsetTop = element.offset().top;
		                relPos=e.pageY-offsetTop;
		                if(relPos < margin){
		                  element[0].scrollTop =element[0].scrollTop -step;
		                }
		                else if(relPos > elemHeight-margin){
		                  element[0].scrollTop =element[0].scrollTop +step;
		                }
		              } 
		            });
		          }
		        };
		      });
			
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
		          
		          /*very important we ignore fractional parts so as to avoid initiating needless digest 
		           loops and possibly hitting a digest limit*/
		          height = height >> 0; 

		          elem.css("height",height + "px");
		          elem.css("max-height",height + "px");
		          elem.css("min-height",height + "px");
		 
		        };
		        
		        return {
		          link: function ($scope, element, attrs) {

		        	  	 var fudge = (attrs.fudge || 0),
		        	  	 	 i=0;
		        	  	 
		        	  	 fudge=fudge *1;
		        	  	 
		        	  	 element.css("box-sizing","border-box");
		        	  	 
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