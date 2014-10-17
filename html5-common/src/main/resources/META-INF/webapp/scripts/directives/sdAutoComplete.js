angular.module('bpm-common.directives')

.controller('autoCompleteController', ['$scope','$timeout', '$attrs',
                                       
  function($scope,$timeout,$attrs){
        
    var tmrPromise;
    
    $scope.ui={"selectedIndex" : 0};
    $scope.ui.styles={};
    
    $scope.$watch("matchStr",function(){
      if($scope.matchStr.length==0){
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
      newItem[$attrs.sdTextProperty]=v;
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
        console.log(result);
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
          $scope.changeHandler(v);
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
      },$scope.closeDelay,true);
      
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
        
        $scope.dataSelected.push(item);
    };
              
    $scope.popData=function(item){
      var idx = $scope.dataSelected.indexOf(item);
      $scope.dataSelected.splice(idx,1);
      
      if($scope.removeOnSelect===true){
          $scope.dataList.splice(idx,0,item);
      }
    };
    
  }
])

.directive("sdAutoComplete",function(){
          
  var link = function(scope,elem,attrs){
    
    if(!attrs.sdCloseDelay){
      attrs.sdCloseDelay=500;
    }
    
    if(!attrs.sdSelectedMatches){
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

  };
          
  return {
    "link" : link,
    "templateUrl"  : "autoComplete/template/autoComplete.html",
    "controller"   : "autoCompleteController",
    "scope"        : { 
        dataList           : "=sdMatches",
        allowMultiple      : "=sdAllowMultiple",
        textProperty       : "@sdTextProperty",
        allowUserEntry     : "=sdAllowUserEntry",
        userEntryDelimiter : "@sdUserEntryDelimiter",
        userEntryFactory   : "&sdUserEntry",
        itemPreClassFactory: "&sdItemPreClass",
        containerClass     : "@sdContainerClass",
        itemHotClass       : "@sdItemHotClass",
        tagPreClassFactory : "&sdTagPreClass",
        dataSelected       : "=sdSelectedMatches",
        matchStr           : "=sdMatchStr",
        changeHandler      : "&sdChange",
        allowDuplicates    : "=sdAllowDuplicates",
        removeOnSelect     : "=sdRemoveOnSelect",
        orderPredicate     : "@sdOrderPredicate",
        closeDelay         : "@sdCloseDelay",
        keyDelay           : "@sdKeyDelay"
      }
  }
  
});