(function(){

	sdScrollDirective.$inject = ["sdScrollService", "$timeout"];

	/**
	 * Allows user to invoke scrolling behavoir on a scrollable element.
	 * User can force a scroll to the elements top, bottom, or to any
	 * DOM element therein using a css selector.
	 * ATTRIBUTES:
	 * ----------------------------------------------------------------
	 * sdScrollTo : reference attribute that should either be a string or 
	 * 				a reference to a JQLite DOM element. Valid string values
	 * 				are as follows...
	 * 				1. TOP = Scroll to top of container
	 * 				2. BOTTOM = Scroll to bottom of container
	 * 				3. [CSS selector] = scroll to first element matching selector.
	 * 				   Note: selector must be recognizable by element.querySelector
	 *
	 * 				If passing a DOM reference the reference should be a JQlite compatible
	 * 				object. Compatible in the sense that the code expects to use array syntax
	 * 				to obtain the native DOM element (e.g. elem[0]).
	 * 				  the element.
	 * @param  {[type]} sdScrollService [description]
	 * @param  {[type]} $timeout        [description]
	 * @return {[type]}                 [description]
	 */
	function sdScrollDirective(sdScrollService,$timeout){

		/**
		 * Linking function where we establish our watch on
		 * the scrollTo scoped property tied to the sdScrollTo
		 * reference attribute.
		 */
		var linkFx = function(scope,elem,attrs){
		  
		  	//Valid values are TOP|BOTTOM|[css selector]|DOM element
			scope.$watch("scrollTo",function(v){

			  	//if v is a string we look for an explicit value of
			  	//TOP or BOTTOM, otherwise we assume user referenced a 
			  	//css selector string.
			    if(angular.isString(v)){
			      if(v.toUpperCase()==="TOP"){
			        sdScrollService.scrollToTop(elem[0]);
			      }
			      else if(v.toUpperCase() === "BOTTOM"){
			        sdScrollService.scrollToBottom(elem[0]);
			      }
			      else{
			        sdScrollService.scrollToSelector(elem[0],v);
			      }
			    }
			    //If not a string we assume a JQLite compatible DOM object
			    else if(angular.isObject(v)){
			      sdScrollService.scrollToElement(elem[0]);
			    }
			    
			    //Reset the value, this also resets the value of the reference attribute
			    //declared on the directive.
			    $timeout(function(){
			      scope.scrollTo = "DONE";
			    },0);
			    
			});
		  
		};

		return{
		  restrict: "A",
		  link:linkFx,
		  scope:{
		    "scrollTo" : "=sdScrollTo" //TOP|BOTTOM|[css selector]|DOM element
		  }
		};

	}

	angular.module("bpm-common.directives").directive('sdScrollTo',sdScrollDirective);

})();