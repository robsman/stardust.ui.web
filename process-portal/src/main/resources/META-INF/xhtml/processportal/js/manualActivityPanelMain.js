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
 * @author Subodh.Godbole
 */

require.config({
	baseUrl: "../../plugins/",
	paths: {
		'jquery' : ['views-common/js/libs/jquery/jquery-1.7.2.min', '//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min'],
		'jquery-ui': ['views-common/js/libs/jquery/plugins/jquery-ui-1.10.2.min', '//ajax.googleapis.com/ajax/libs/jqueryui/1.10.2/jquery-ui.min'],
		'angularjs' : ['views-common/js/libs/angular/angular-1.0.2', '//ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min'],
		'json' : ['views-common/js/libs/json/json2', '//cdnjs.cloudflare.com/ajax/libs/json2/20110223/json2']
	},
	shim: {
		'angularjs': {
			require: "jquery",
			exports: "angular"
		},
		'jquery-ui': ['jquery']
	}
});


require(["require", "jquery", "jquery-ui", "angularjs", "processportal/js/m_manualActivityPanel"], function(){
	require("processportal/js/m_manualActivityPanel").initialize();
});