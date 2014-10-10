angular.module('bpm-common.directives')
.directive('sdMoveable', ['$document' , function($document) {
    return {
      
      restrict: 'A',
      
      link: function(scope, elem, attrs) {
        
        var x0, //Inital x
            y0, //Inital y
            x1, //tracked x pos
            y1; //tracked y pos
            
        //elem.css({position: 'absolute'});
 
        elem.bind('mousedown', function($event) {
          x1 = elem.prop('offsetLeft');
          y1 = elem.prop('offsetTop');
          x0 = $event.clientX;
          y0 = $event.clientY;
          $document.bind('mousemove', mouseMove);
          $document.bind('mouseup', mouseUp);
          return false;
        });
 
        function mouseMove($event) {
          
          var dx = $event.clientX - x0,
              dy = $event.clientY - y0;
              
          elem.css({
            top:  y1 + dy + 'px',
            left: x1 + dx + 'px'
          });
          return false;
        }
 
        function mouseUp() {
          $document.unbind('mousemove', mouseMove);
          $document.unbind('mouseup', mouseUp);
        }
        
      }
    };
  }]);