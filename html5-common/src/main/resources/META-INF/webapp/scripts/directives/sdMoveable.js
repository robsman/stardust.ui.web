/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

angular.module('bpm-common.directives')
.directive('sdMoveable', ['$document' , function($document) {
    return {
      
      restrict: 'A',
      
      link: function(scope, elem, attrs) {
	   	    var x0, //Inital x
	        y0, //Inital y
	        x1, //tracked x pos
	        y1; //tracked y pos
			
			var dragArea = elem.find('.drag-area')
			
			if (dragArea.length === 0) {
				// if no drag area found use entire element
				dragArea = elem;
			}
			
			dragArea.css({cursor: 'all-scroll'});
			elem.css({position: 'absolute'});
			bindDraggable();
			
			function bindDraggable() {
				// Make sure to bind mouse event only to the drag Area
				dragArea.bind('mousedown', function($event) {
					// Now make the dialog draggable by noting down the position of the mouse cursor
			          x1 = elem.prop('offsetLeft');
			          y1 = elem.prop('offsetTop');
			          x0 = $event.clientX;
			          y0 = $event.clientY;
			          $document.bind('mousemove', mouseMove);
			          $document.bind('mouseup', mouseUp);
				      return false;
			    });
			}
			 
			function mouseMove($event) {
			      var dx = $event.clientX - x0,
			          dy = $event.clientY - y0;
			      var w = jQuery(window);
			      var top = y1 + dy;
			      var left = x1 + dx;
			      
			      if (top < 0) {
			    	  top = 0;
			      }
			      if (left < 0) {
			    	  left = 0;
			      }
			      if (top > w.height()) {
			    	  top = w.height();
			      }
			      if (left > w.width()) {
			    	  left = w.width();
			      }
			      
			      elem.css({
			        top:  top + 'px',
			        left: left + 'px'
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