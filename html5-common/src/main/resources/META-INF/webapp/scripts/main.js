// Define Custom Modules
// Top level module needs to have dependency on framework top level module called 'bpm-ui'
angular.module('bpm-common.services', []);
angular.module('bpm-common.directives', []);
angular.module('bpm-common', ['bpm-common.services','bpm-common.directives']);

// Register top level module to Framework
portalApplication.addModule('bpm-common');