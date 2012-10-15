/**
 * @author Omkar.Patil
 */
define(['jquery'], function($) {
	/* moveDiv function for inline edit on activities*/
  $.fn.moveDiv = function(params) {
    var obj = this.eq(0); //only operate on the first selected object;
    obj.css({'position':'absolute' , 'left': params.x , 'top': params.y, 'clear' : 'both'});
    return obj;
  };
  
  /* Capture escape key down event. Passes the event as an argument to the callback function - just in case needed. */
  $.fn.escKeydown = function(handlerCallback)
  {
	  $(this).keydown(function(event) {
		  if (event.which == '27')
		  {
			  handlerCallback();
		  }
	  });
  };
  
  /* Capture delete key event. Passes the event as an argument to the callback function - just in case needed. */
  $.fn.delKeydown = function(handlerCallback)
  {
	  $(this).keydown(function(event) {
		  if (event.which == '46')
		  {
			  handlerCallback(event);
		  }
	  });
  };
  
  /* Capture right arrow key event. Passes the event as an argument to the callback function - just in case needed. */
  $.fn.rightArrowKeydown = function(handlerCallback)
  {
	  $(this).keydown(function(event) {
		  if (event.which == '39')
		  {
			  handlerCallback(event);
		  }
	  });
  };
  
  /* Capture left arrow key event. Passes the event as an argument to the callback function - just in case needed. */
  $.fn.leftArrowKeydown = function(handlerCallback)
  {
	  $(this).keydown(function(event) {
		  if (event.which == '37')
		  {
			  handlerCallback(event);
		  }
	  });
  };
  
  /* Capture right arrow key event. Passes the event as an argument to the callback function - just in case needed. */
  $.fn.upArrowKeydown = function(handlerCallback)
  {
	  $(this).keydown(function(event) {
		  if (event.which == '38')
		  {
			  handlerCallback(event);
		  }
	  });
  };
  
  /* Capture left arrow key event. Passes the event as an argument to the callback function - just in case needed. */
  $.fn.downArrowKeydown = function(handlerCallback)
  {
	  $(this).keydown(function(event) {
		  if (event.which == '40')
		  {
			  handlerCallback(event);
		  }
	  });
  };
});
