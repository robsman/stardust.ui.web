/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * @author Zachary.McCain
 * 
 * @description
 * Implements  a simple drop-down menu directive which takes attributes as indicated below.
 * The menu has multiple hooks for customizing structure and behavior. If desired the menu 
 * can be used with promises to delay the menus closing state until some asynchronous behavior
 * corresponding to a user click has completed. During this state a deferred class is added the 
 * LI element which the user clicked to allow for css  customization.
 * 
 * Attributes
 * ------------------------------------------------------------------------------------------------
 * 	@attr - sdaMenuItems : Array of objects where each object must at least contain a value property.
 * 						   The 'value' property maps to the displayed name. The 'class' property is simply
 * 						   a string containing classes we will add to our LI element.  You are free to add 
 * 				           whatever other properties you find useful.
 * 						   Example: [{'value' : 'Delete', 'class' : 'fa fa-times'}]
 * 
 *  @attr - sdaMenuCallback : User supplied callback which will be invoked whenever the visible state of the menu
 *                           changes and whenever a user clicks a menu item. The structure of the object passed back
 *                           is variable based on the event. 
 */
(function(){
	
	'use strict';
	
	  function dropDownMenu($q){
		    
	      //Linking function for our menu
	      function dropDownLinkingFx(scope,elem,attrs){
	    	  
	    	//controls menu visibility
		    scope.showMenu = false;

	        //callback function from our template which wraps the users callback
	        scope.invoke = function(item,e){
	          var deferred = $q.defer(),
	              menuItem = {},
	              parentLi,
	              elem;
	          
	          //handle FireFox-IE-Chrome
	          elem = e.target || e.srcElement;
	          
	          //Clicks can occur on the LI or the nested span
	          if(elem.nodeName==='SPAN'){
	            parentLi = angular.element(elem.parentElement);
	          }
	          else{
	            parentLi = angular.element(elem.srcElement);
	          }
	          
	          //user can implement this css class as desired (or not)
	          parentLi.addClass("deferred");
	          
	          //build up our object we will pass back 
	          menuItem.item = item;
	          menuItem.deferred = deferred;
	          menuItem.menuEvent ="menuItem.clicked";
	          menuItem.scopeRef = scope;
	          
	          //invoke
	          this.callback({"menuItem" : menuItem,"e" : e});
	          
	          //wait for deferred to resolve.
	          deferred.promise.then(function(fx){
	        	  
	        	//on resolve hide menu
	            scope.showMenu=false;
	            
	            //then remove the deferred class
	            parentLi.removeClass("deferred");
	            
	            //let user muck about with the scope however they want by
	            //invoking their function passed through as resolved data
	            if(angular.isFunction(fx)){
	              fx(scope);
	            }
	            
	          });
	        };//scope.invoke ends.
	      
	    }//Linking function ends.
	    
	    
	    return {
	      template: "<div style='position:relative;' class='sd-drop-down-menu'>" +
	                  "<div class='dd-header' ng-click='showMenu = !showMenu' ng-transclude></div>" +
	                  "<ul style='position:absolute;' ng-show='showMenu'>" +
	                    "<span ng-click='showMenu=false'>x</span>" +
	                    "<li ng-click='invoke(item,$event)'" +
	                        "ng-class='item.class'" +
	                        "ng-repeat='item in menuItems'>" +
	                      "<span>{{item.value}}</span>" +
	                    "</li>" +
	                  "</ul>" +
	                "</div>",
	      restrict: "EA",
	      transclude:true,
	      scope: {
	        "menuItems" : "=sdaMenuItems",
	        "callback"  : "&sdaMenuCallback"
	      },
	      link : dropDownLinkingFx
	    };
	    
	  }
	  
	  
	  dropDownMenu.inject = ["$q"];
	  
	  angular.module("bpm-common.directives")
	  .directive("sdDropDownMenu",dropDownMenu);
	
})();