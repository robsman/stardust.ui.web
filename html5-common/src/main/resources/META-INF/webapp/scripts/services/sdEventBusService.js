angular.module('bpm-common.services')
.factory("eventBus",function($window,$injector){
  
  var $rs = $injector.get("$rootScope");
  
  /*Check for our global*/
  if(!$window.$starDust){$window.$starDust={};}
  
  /*Check for event Bus*/
  if(!$window.$starDust.eventBus){
    $window.$starDust.eventBus =
    {
      rootScopes:[],
      test : "hello from global"
    };
  }
  
  /*Push our rootScope onto global, we will iterate
    over that collection when we need to emit.
    This allows us to communciate between different
    angular applciations.*/
  $window.$starDust.eventBus.rootScopes.push($rs);
  
  return {
    
    emitMsg: function(msg, data, options) {
      
      var rs;
      
      options = options || {
        emitXApp:false
      };
      
      if(options.emitXApp===true){
        rs=$window.$starDust.eventBus.rootScopes;
      }
      else{
        rs=[$rs];
      }
      
      for(var i = 0; i < rs.length;i++){
        data = data || {}
        rs[i].$emit(msg, data);
      }
      
    },
    
    onMsg: function(msg, func, scope){
      var unbind = $rs.$on(msg, func);
      if (scope) {
          scope.$on('$destroy', unbind);
      }
    }
    
  };
  
});