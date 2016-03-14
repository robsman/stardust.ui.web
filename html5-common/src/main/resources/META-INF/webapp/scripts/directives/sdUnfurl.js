/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *******************************************************************************/

/**
 * Unfurls an element to the bottom of the viewable window and monitors the elements top position to update it's
 * height so that it dynamically adjusts as needed.
 *
 * @Usage: sda-unfurl
 * 
 * @Attributes:
 * 
 * 	sda-fudge: {numeric value e.g. 75} Usually some fudge value is needed to adjust for margins etc. The value is interpreted
 * 		as a pixel adjustment and allows the user to pad the internal height calculation as required.
 *
 * 	sda-min-height: sets the minimum height which the element is allowed to size to. I advise setting this to avoid scenarios where
 * 					you get a zero calculated height.
 * 
 * WARNING:
 * This can sometimes have the intended or unintended effect
 * of an infinity scroll depending on the containing elements styling and the fudged calculation of the directive.
 * I need to better document how to achieve this as I have only done this inadvertantly to date.
 * @return {[type]} [description]
 */
(function(){
	
	var sdUnfurl = function($timeout){
		
		  /*calculate height needed to reach bottom of page*/
		  var adjustHeight=function(elem,fudge,minHeight){
		    
		    var  marginBottom = window.getComputedStyle(document.body).getPropertyValue('margin-bottom');
		         height = window.innerHeight - elem.getBoundingClientRect().top|0;
		         
		    fudge = 1*fudge;
		    minHeight = 1*minHeight;
		    
		    marginBottom=parseInt(marginBottom);
		    height = height - marginBottom + fudge ;
		    height = (minHeight > height)?minHeight:height;
		    if(height==0)height=minHeight;
		    elem.style.height = height + "px";
		    elem.style.minHeight = height + "px";
		  }
		  
		  //Return object
		  return {
		    link: function ($scope, element, attrs) {
		          
		          var elem = element[0],
		              interval;
		          
		          elem.lastPosition = elem.getBoundingClientRect().top|0;
		          
		          //Avoid $interval as we don't wish to incur digests
		          interval =setInterval( function(){
		            var currentPosition = elem.getBoundingClientRect().top|0;
		            if(currentPosition !== elem.lastPosition){
		              elem.lastPosition = currentPosition;
		              adjustHeight(elem,attrs.sdaFudge || 0, attrs.sdaMinHeight || 0);
		            }
		          }, 50);//poll every 50 milliseconds
		          
		          //initial sizing when compilation is finished, we do want to incur a digest here
		          $timeout(function(){
		            adjustHeight(elem,attrs.fudge || 0);
		          },0)
		          
		          //cancel interval when scope is destroyed
		          $scope.$on("$destroy",function(){
		            clearInterval(interval);
		          });

		      }
		  	}
		}
		
		sdUnfurl.$inject = ["$timeout"];
		
		angular.module('bpm-common.directives').directive("sdUnfurl", sdUnfurl);
	
	
})();