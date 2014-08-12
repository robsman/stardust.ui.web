/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Omkar.Patil
 * @author Robert Sauer
 */
bpm.portal.modelerRequire.config({baseUrl: "../../"});

(function() {
	  if (!window.console) {
	    window.console = {};
	  }
	  // union of Chrome, FF, IE, and Safari console methods
	  var m = [
	    "log", "info", "warn", "error", "debug", "trace", "dir", "group",
	    "groupCollapsed", "groupEnd", "time", "timeEnd", "profile", "profileEnd",
	    "dirxml", "assert", "count", "markTimeline", "timeStamp", "clear"
	  ];
	  // define undefined methods as noops to prevent errors
	  for (var i = 0; i < m.length; i++) {
	    if (!window.console[m[i]]) {
	      window.console[m[i]] = function() {};
	    }    
	  } 
	})();

define([
         "jquery",
		 "jquery-ui",
		 "jquery.download",
		 "jquery.form",
		 "jquery.simplemodal",
		 "jquery.url",
		 "jquery.jstree",
		 "outline-plugins",
		 "i18n",
		 "bpm-modeler/js/m_outline"
], function() {
	var outline = require('bpm-modeler/js/m_outline');
	outline.init();
});

