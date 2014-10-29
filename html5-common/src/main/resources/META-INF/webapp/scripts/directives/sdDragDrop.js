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
          e.dataTransfer.dropEffect = 'move';
          return false;
      });
      
      elem.bind("drag",function(){
        return false;
      });
      
      elem.bind("drop",function(e){
        if (e.stopPropagation) {e.stopPropagation();}
        var data = e.dataTransfer.getData("text/plain");
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
        e.dataTransfer.setData("text/plain",data);
        e.dataTransfer.effectAllowed="move";
        console.log("drag Start:" + data);
      });
      
    }
  };
});