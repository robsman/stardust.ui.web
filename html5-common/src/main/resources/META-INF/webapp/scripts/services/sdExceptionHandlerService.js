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
/*override exception handler*/
angular.module('bpm-common.services');

/* Temporarily commenting out the override it's not compatible with Angular upgrade
.factory('$exceptionHandler', function ($injector) {
    return function errorCatcherHandler(exception, cause) {
    	
      // Late resolution to avoid circular dependency
      var eventBus = $injector.get('eventBus');
      rootScope = $injector.get('$rootScope');
      eventBus.emitMsg("js.error",exception.message,{emitXApp:true});
      console.error(exception.stack);
      
    };
});
*/