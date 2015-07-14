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
	    	console.log("Drag-enter");
	        e.preventDefault();
	        return true;
	      });
	      
	      elem.bind("dragover",function(e){
	          if (e.preventDefault) {e.preventDefault();}
	          if (e.stopPropagation) {e.stopPropagation();}
	          e = (e.dataTransfer)?e: e.originalEvent;
	          e.dataTransfer.dropEffect = 'move';
	          console.log("Drag-over");
	          return false;
	      });
	      
	      elem.bind("drag",function(){
	    	console.log("Drag");
	        return false;
	      });
	      
	      elem.bind("drop",function(e){
	    	if (e.preventDefault) {e.preventDefault();}
	        if (e.stopPropagation) {e.stopPropagation();}
	        e = (e.dataTransfer)?e: e.originalEvent;
	        var data = e.dataTransfer.getData(dataType);
	        var fn = $parse(attrs.sdaDrop);
	        fn(scope, {$data: data, $event: e});
	        console.log("Drop");
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
	      
	      var data=ngModelCtrl.$viewValue;
	      
	      elem.attr("draggable","true");

	      elem.bind("dragend",function(e){
	        var fn = $parse(attrs.sdaDragend);
	        
	        srcObject=ngModelCtrl.$modelValue;
	        fn(scope, {$data: srcObject, $event: e});
	        console.log("Drag-end");
	        
	      });

	      elem.bind("dragstart",function(e){
	    	  
	        var data =ngModelCtrl.$viewValue,
	        	fn = $parse(attrs.sdaDragstart);
	        
	        //DragStart callback
	        data = fn(scope, {$data: ngModelCtrl.$viewValue, $event: e}) || data;
	        data = JSON.stringify(data);
	        
	        e = (e.dataTransfer)?e: e.originalEvent;
	        e.dataTransfer.setData(dataType,data);
	        e.dataTransfer.dropEffect = 'move';
	        if(e.dataTransfer.setDragImage){
	        	e.dataTransfer.setDragImage(elem[0],0,0);
	        }
	        e.dataTransfer.effectAllowed="move";

	      });
	      
	    }
	  };
	});//sd-data-drag end
	
})();

//TODO: REMOVE OLD IMPLEMENTATION
/*
angular.module('bpm-common.directives')
.directive("sdDataDrop",function($parse){
   return{
    scope: true,
    link: function(scope, elem, attrs){
      
      elem.attr("droppable","true");

      elem.bind("dragenter",function(e){
        e.preventDefault();
        return true;
      });
      
      elem.bind("dragover",function(e){
          if (e.preventDefault) {e.preventDefault();}
          if (e.stopPropagation) {e.stopPropagation();}
          var dataTransfer = e.dataTransfer || e.originalEvent.dataTransfer;
          dataTransfer.dropEffect = 'move';
          return false;
      });
      
      elem.bind("drag",function(){
        return false;
      });
      
      elem.bind("drop",function(e){
        if (e.stopPropagation) {e.stopPropagation();}
        var dataTransfer = e.dataTransfer || e.originalEvent.dataTransfer;
        var data = dataTransfer.getData("text/plain");
        var fn = $parse(attrs.onDrop);
        fn(scope, {$data: data, $event: e});
        console.log("Drop:" + data);
        console.log(e);
      });
    }
  };
});

angular.module('bpm-common.directives')
.directive("sdDataDrag",function($parse){
  return{
    require: "ngModel",
    link: function(scope, elem, attrs,ngModelCtrl){
      
      var data=ngModelCtrl.$viewValue;
      
      elem.attr("draggable","true");

      elem.bind("dragend",function(e){
        var fn = $parse(attrs.onDragEnd);
            srcScope=angular.element(e.srcElement).scope(),
            srcModel=angular.element(e.srcElement).attr("ng-model"),
            srcObject=srcScope[srcModel];
            
        fn(scope, {$data: srcObject, $event: e});
        
      });

      elem.bind("dragstart",function(e){
        var data =JSON.stringify(ngModelCtrl.$viewValue);
        var dataTransfer = e.dataTransfer || e.originalEvent.dataTransfer;
        dataTransfer.setData("text/plain",data);
        dataTransfer.effectAllowed="move";
        console.log("drag Start:" + data);
      });
      
    }
  };
});
*/