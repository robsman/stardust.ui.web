(function(){
	
	/**
	 * Simple service to enable scrolling to a position inside of a scrollable container.
	 * Absolute vertical scrolling (top|bottom) is supported explicitly otherwise scrolling methods require
	 * either a selector (#id, etc) or a DOM element for the invocation. 
	 * @return {[type]} [description]
	 */
	function sdScrollService(){}
	
	/**
	 * Given a DOM element, leverage the native scrollIntoView method to
	 * scroll the element into view (X and Y).
	 * @param  {[type]} elem       :DOM element to scroll to
	 * @param  {[type]} alignToTop :defaults true in browsers
	 * @return {[type]}            [description]
	 */
	sdScrollService.prototype.scrollToElement = function(elem,alignToTop){
		elem.scrollIntoView(alignToTop);
	};
	  
	/**
	 * Given a selector string leverage the native scrollToElement function to target the
	 * element and position it in view.
	 * @param  {[type]} elem     :element containing the child of interest.
	 * @param  {[type]} selector :selector string indicating our target element
	 * @return {[type]}          [description]
	 */
	sdScrollService.prototype.scrollToSelector = function(elem,selector){
	  var obj = elem.querySelector(selector);
	  if(angular.isObject(obj)){
	    this.scrollToElement(obj,true);
	  }
	};
	
	/**
	 * Given a scrollable element, scroll to its absolute top.
	 * @param  {[type]} elem : scrollable container
	 * @return {[type]}      [description]
	 */
	sdScrollService.prototype.scrollToTop = function(elem){
		elem.scrollTop = 0;
	};
	  
	/**
	 * Given a scrollable element, scroll to its absolute bottom.
	 * @param  {[type]} elem : scrollable container
	 * @return {[type]}      [description]
	 */
	sdScrollService.prototype.scrollToBottom = function(elem){
		elem.scrollTop = elem.scrollHeight;
	};
	  
	angular.module('bpm-common.services')
	.service("sdScrollService",sdScrollService);
	
	
})();