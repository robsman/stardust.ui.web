// Define Modules
angular.module('bpm-common.services', []);
angular.module('bpm-common', ['bpm-common.services']);

// Register top level module to Framework
angular.module('bpm-ui').addPluginModule('bpm-common');