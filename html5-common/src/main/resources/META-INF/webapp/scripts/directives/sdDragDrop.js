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