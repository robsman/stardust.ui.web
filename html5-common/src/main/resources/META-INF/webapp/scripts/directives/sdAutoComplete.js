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

.controller('autoCompleteController', ['$scope','$timeout', '$attrs',
  function($scope,$timeout,$attrs){
        
    var tmrPromise;
    
    $scope.ui={"selectedIndex" : 0};
    $scope.ui.styles={};
    
    $scope.$watch("matchStr",function(){
      if($scope.matchStr && $scope.matchStr.length==0){
           $scope.ui.selectedIndex=0;
      }
    });
    
    $scope.test=function(){
      return "'select-item-hot'"
    }
    
    $scope.classGeneratorIPC=function(v,i){
      var fx=$scope.itemPreClassFactory,
          cssClass="";
      if(angular.isFunction(fx)){
        cssClass=fx({item : v,index:i});
      }
      else{
        cssClass=$scope.itemPreClassFactory;
      }
      return cssClass;
    }
    
    $scope.classGeneratorTPC=function(v,i){
      var fx=$scope.tagPreClassFactory,
          cssClass="";
      if(angular.isFunction(fx)){
        cssClass=fx({item : v,index:i});
      }
      else{
        cssClass=$scope.tagPreClassFactory;
      }
      return cssClass;
    }
    
    $scope.newItemFactory=function(v){
    var fx=$scope.userEntryFactory,
        newItem={};
    if(angular.isFunction(fx)){
      newItem=fx({val : v});
    }
    else{
      newItem[$attrs.sdaTextProperty]=v;
    }
    return newItem;
  }
            
    /*wraps data attributes which correspond to classes or functions 
      which returns classes.*/
    $scope.classWrapper=function(attr,v,index){
        var fx,
            result=false;

        if($attrs[attr]){
          fx=$scope.$parent[$attrs[attr]];
          if(angular.isFunction(fx)){
              result=fx(v,index);
          }
          else{
            result=$attrs[attr];
          }
        }
        return result;
    }
    
    /**/           
    $scope.changeWrapper=function(v){
      var testChar=v.substring(v.length-1),
          newItem={},
          fx;
          
      
      if(tmrPromise){
        $timeout.cancel(tmrPromise);
      }

      if(testChar==$scope.userEntryDelimiter){
        newItem=$scope.newItemFactory(v.substring(0,v.length-1));
        $scope.matchStr="";
        $scope.pushData(newItem);
      }
      else{
        tmrPromise=$timeout(function(){
          $scope.changeHandler({value:v});
        },$scope.keyDelay*1,true);
      }
    }

    $scope.cancelTimer=function(){
      if(tmrPromise){
        $timeout.cancel(tmrPromise);
      }
    }
    
    $scope.startTimer = function(){
      if(tmrPromise){
        $timeout.cancel(tmrPromise);
      }
      
      tmrPromise=$timeout(function(){
      	$scope.$apply(function(){
          $scope.dataList=[];
          $scope.ui.selectedIndex=0;
        });
      },500); //need a delay here or we can lose click events on our dropdown
      
    };
              
    $scope.keyMonitor = function(e){
      
      if(e.keyCode==38 && $scope.ui.selectedIndex >0){
        $scope.ui.selectedIndex=$scope.ui.selectedIndex-1;
      }
      else if(e.keyCode==40 && $scope.ui.selectedIndex < $scope.dataList.length-1)
      {
        $scope.ui.selectedIndex=$scope.ui.selectedIndex+1;
      }
      else if (e.keyCode==13 && $scope.dataList.length >0 ){
        $scope.pushData($scope.dataList[$scope.ui.selectedIndex]);
      }
      else{
        $scope.dataList=[];/*clean up previous results*/
      }
    };
              
    $scope.pushData=function(item){
        var idx=-1;

        if($scope.allowMultiple===false){
          $scope.dataSelected=[];
        }
        
        if($scope.allowDuplicates===false){
          idx = $scope.dataSelected.indexOf(item);
          if(idx>-1){return;}
        }
        
        if($scope.removeOnSelect===true){
          idx=$scope.dataList.indexOf(item);
          $scope.dataList.splice(idx,1);
        }
        
        //clear user input on select
        if($scope.clearOnSelect===true){
        	$scope.matchStr ="";
        }

        $scope.dataSelected.push(item);

        if (angular.isDefined($attrs.sdaOnSelectionChange)) {
          $scope.onSelectionChange({selectedData: $scope.dataSelected});
        }
    };
              
    $scope.popData=function(item){
    	if($scope.readOnly) {
    		return;
    	}
    	
      var idx = $scope.dataSelected.indexOf(item);
      $scope.dataSelected.splice(idx,1);
      if (angular.isDefined($attrs.sdaOnSelectionChange)) {
        $scope.onSelectionChange({selectedData: $scope.dataSelected});
      }
      
      if($scope.removeOnSelect===true){
          $scope.dataList.splice(idx,0,item);
      }
    };
    
  }
])

.directive("sdAutoComplete",function(){
  
  var tpl = '<div name="sd-ac-Container"\
               ng-class="containerClass"\
               ng-keyDown="keyMonitor($event)" aid="{{autoIdPrefix}}-sd-ac-Container">\
              <div ng-attr-title="{{item[tooltipProperty]|| item[textProperty]}}" name="sd-ac-tag"\
                   ng-click="popData(item)"\
                   ng-repeat="item in dataSelected track by $index"\
                   ng-class="classWrapper(\'tagClass\',item)" aid="{{autoIdPrefix}}-sd-ac-tag">\
                   <i ng-class="classGeneratorTPC(item,$index)"></i>\
                     {{item[textProperty] || item}}\
              </div>\
              <input ng-hide="readOnly" ng-model="matchStr"\
                     ng-keyUp="changeWrapper(matchStr)"\
                     style="outline-width:0px;border:none; margin-left:4px"\
                     type="text" aid="{{autoIdPrefix}}-MatchStr" />\
              <div  ng-show="dataList.length >0 && matchStr.length>0"\
                    name="sd-ac-selectList"\
                    style="z-index:9999"\
                    ng-class="classWrapper(\'selectBoxClass\',{},-1)">\
                  <div ng-click="pushData(item)"\
                       ng-mouseover="ui.selectedIndex=$index"\
                       ng-class="{ \'{{itemHotClass}}\' : ui.selectedIndex==$index}"\
                       ng-repeat="item in dataList | orderBy:orderPredicate" aid="{{autoIdPrefix}}-PushData" >\
                       <i ng-class="classGeneratorIPC(item, $index)"></i>\
                       {{item[textProperty]||item}}\
                  </div>\
              </div>\
            </div>'
          
  var link = function(scope,elem,attrs){
  
    if(!attrs.sdaCloseDelay){
      attrs.sdaCloseDelay=500;
    }
    
    if(!attrs.sdaSelectedMatches){
      scope.dataSelected=[];
    }
    
    var input = $("input",elem);
    
     $("[name='sd-ac-selectList']",elem).on("click",function(){
       scope.cancelTimer();
       input.focus();
     });
    
    $("[name='sd-ac-Container']",elem).on("click",function(){
       scope.cancelTimer();
       input.focus();
     });
    
    input.on("blur",function(){
      scope.startTimer();
    });
    
    // Added watch to reset the auto-complete 
    scope.$watch('dataSelected', function() {
			scope.matchStr = '';
	});

  };
          
  return {
    "link" : link,
    "template"  : tpl,
    "controller"   : "autoCompleteController",
    "scope"        : { 
        dataList           : "=sdaMatches",
        clearOnSelect	   : "=sdaClearOnSelect",
        allowMultiple      : "=sdaAllowMultiple",
        textProperty       : "@sdaTextProperty",
        allowUserEntry     : "=sdAllowUserEntry",
        userEntryDelimiter : "@sdaUserEntryDelimiter",
        userEntryFactory   : "&sdaUserEntry",
        itemPreClassFactory: "&sdaItemPreClass",
        containerClass     : "@sdaContainerClass",
        itemHotClass       : "@sdaItemHotClass",
        tagPreClassFactory : "&sdaTagPreClass",
        dataSelected       : "=sdaSelectedMatches",
        matchStr           : "=sdaMatchStr",
        changeHandler      : "&sdaChange",
        allowDuplicates    : "=sdaAllowDuplicates",
        removeOnSelect     : "=sdaRemoveOnSelect",
        orderPredicate     : "@sdaOrderPredicate",
        closeDelay         : "@sdaCloseDelay",
        keyDelay           : "@sdaKeyDelay",
        onSelectionChange  : "&sdaOnSelectionChange",
        autoIdPrefix       : "@sdaAidPrefix",
        tooltipProperty    : "@sdaTooltipProperty",
        readOnly           : "=sdaReadOnly"
      }
  }
  
});