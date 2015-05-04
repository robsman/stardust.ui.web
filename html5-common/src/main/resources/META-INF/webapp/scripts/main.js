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
// Define Custom Modules
angular.module('bpm-common.services', []);

angular.module('bpm-common.directives', []);

angular.module('bpm-common.init', []).run(['$rootScope', 'sdUtilService', function($rootScope, sdUtilService) {
	$rootScope.bpmCommon = {
		stopEvent : sdUtilService.stopEvent
	};
}]);

angular.module('bpm-common', ['bpm-common.services', 'bpm-common.directives', 'bpm-common.init'])
.config(['$httpProvider', function ($httpProvider) {
	$httpProvider.interceptors.push('httpInterceptor');
}]);

// Register top level module to Framework
portalApplication.registerModule('bpm-common');