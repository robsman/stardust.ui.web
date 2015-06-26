/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

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
                parentLi;
            
            //Clicks can occur on the LI or the nested span
            if(e.srcElement.nodeName==='SPAN'){
              parentLi = angular.element(e.srcElement.parentElement);
            }
            else{
              parentLi = angular.element(e.srcElement);
            }
            
            //user can implement this css class as desired (or not)
            parentLi.addClass("deferred");
            
            //build up our object we will pass back 
            menuItem.item = item;
            menuItem.deferred = deferred;
            menuItem.menuEvent ="menuItem.clicked";
            
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
                    "<div class='dd-header' ng-right-click='showMenu = !showMenu' ng-transclude></div>" +
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
    .directive("sdRightClickMenu",dropDownMenu);
    
})();