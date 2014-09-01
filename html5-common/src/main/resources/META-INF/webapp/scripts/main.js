// Define Custom Modules
// Top level module needs to have dependency on framework top level module called 'bpm-ui'
angular.module('bpm-common.services', []);
angular.module('bpm-common', ['bpm-common.services']);

// Register top level module to Framework
portalApplication.addModule('bpm-common');