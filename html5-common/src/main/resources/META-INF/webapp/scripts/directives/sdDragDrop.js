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
(function(){
	
	//Sniff browser so we can set the proper dataType on the dataTransfer API
	var dataType = "text/plain";
	
	if(navigator.appName==="Microsoft Internet Explorer"){
		dataType = "text";
	}
	
	/**
	 * sdDataDrop: makes the associated element a drop target with
	 * a user defined callback for drop events - attrs.onDrop
	 */
	angular.module('bpm-common.directives')
	.directive("sdDataDrop",function($parse){
		
	   return{
	    scope: true,
	    link: function(scope, elem, attrs){
	      
	      elem.attr("droppable","true");

	      elem.bind("dragenter",function(e){
	        e.preventDefault();
	        elem.addClass("drop-over");
	        return true;
	      });
	      
	      elem.bind("dragleave",function(e){
	    	  elem.removeClass("drop-over");  
	      });
	      
	      elem.bind("dragover",function(e){
	          if (e.preventDefault) {e.preventDefault();}
	          if (e.stopPropagation) {e.stopPropagation();}
	          e = (e.dataTransfer)?e: e.originalEvent;
	          e.dataTransfer.dropEffect = 'move';
	          return false;
	      });
	      
	      elem.bind("drag",function(){
	        return false;
	      });
	      
	      elem.bind("drop",function(e){
	    	if (e.preventDefault) {e.preventDefault();}
	        if (e.stopPropagation) {e.stopPropagation();}
	        e = (e.dataTransfer)?e: e.originalEvent;
	        var data = e.dataTransfer.getData(dataType);
	        var fn = $parse(attrs.sdaDrop);
	        fn(scope, {$data: data, $event: e});
	        elem.removeClass("drop-over");
	      });
	    }
	  };
	});//sd-data-drop end
	
	
	/**
	 * sdDataDrag: makes the associated element draggable with
	 * a user defined callback for drop events - attrs.onDrop
	 */
	angular.module('bpm-common.directives')
	.directive("sdDataDrag",function($parse){
		
		
	  return{
	    require: "ngModel",
	    link: function(scope, elem, attrs,ngModelCtrl){
	      
	      var data=ngModelCtrl.$viewValue,
    	      dragElem,
    	      cloneElem;
	      
	      elem.attr("draggable","true");

	      elem.bind("dragend",function(e){
	        var fn = $parse(attrs.sdaDragend);
	        
	        srcObject=ngModelCtrl.$modelValue;
	        fn(scope, {$data: srcObject, $event: e});
	        
	        if(dragElem && dragElem.remove){
	          dragElem.remove();
	        }
	        
	        if(cloneElem && cloneElem.remove){
	          cloneElem.remove();
	        }
	        
	      });

	      elem.bind("dragstart",function(e){
	    	  
	        var data =ngModelCtrl.$viewValue,
	        	  fn = $parse(attrs.sdaDragstart),
	            fn2 = $parse(attrs.sdaDragElemFx),
	            dragObj;

	          
	        //DragStart callback
	        data = fn(scope, {$data: ngModelCtrl.$viewValue, $event: e}) || data;
	        
	        //Drag element factory callback
	        dragString =  fn2(scope, {item: ngModelCtrl.$viewValue});
	        
            
	        if(dragString){
  	        //now create drag element and append to top of body
  	        dragElem = $(dragString);
  	        dragElem.css({"position":"absolute","top":0,"left":0,"z-index" : -2});
  	        
  	        
  	        //now createClone
  	        cloneElem = dragElem.clone();
  	        
  	        //cloneElem.css(cloneCss);
  	        //cloneElem.children().css(cloneCss);
  	        cloneElem.addClass("sd-clone-elem");
  	        $("body").append(dragElem);
  	        $("body").append(cloneElem);
	        }
	        //ensure we at least pass an empty string
	        if(angular.isUndefined(data)){data="";}
	        
	        data = JSON.stringify(data);
	        
	        e = (e.dataTransfer)?e: e.originalEvent;
	        e.dataTransfer.setData(dataType,data);
	        e.dataTransfer.dropEffect = 'move';
	        if(e.dataTransfer.setDragImage){
	        	e.dataTransfer.setDragImage(dragElem[0],0,0);
	        }
	        e.dataTransfer.effectAllowed="move";

	      });
	      
	    }
	  };
	});//sd-data-drag end
	
})();
