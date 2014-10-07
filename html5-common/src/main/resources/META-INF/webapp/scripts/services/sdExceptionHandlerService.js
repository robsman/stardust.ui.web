/*override exception handler*/
angular.module('bpm-common.services')
.factory('$exceptionHandler', function ($injector) {
    return function errorCatcherHandler(exception, cause) {
    	
      /*Late resolution to avoid circular dependency*/
      var eventBus = $injector.get('eventBus');
      rootScope = $injector.get('$rootScope');
      eventBus.emitMsg("js.error",exception.message,{emitXApp:true});
      console.error(exception);
      
    };
});