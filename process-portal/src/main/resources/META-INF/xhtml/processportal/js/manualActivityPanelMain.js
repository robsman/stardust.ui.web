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
		'jquery-base64': ['common/js/thirdparty/jquery.base64'],
		'jquery-mobile' : ["mobile-workflow/public/js/libs/jquery/plugins/jquery.mobile-1.4.0", "//code.jquery.com/mobile/1.4.0/jquery.mobile-1.4.0"], 
		'angularjs' : ['views-common/js/libs/angular/angular-1.0.2', '//ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min'],
		'json' : ['views-common/js/libs/json/json2', '//cdnjs.cloudflare.com/ajax/libs/json2/20110223/json2'],
		'bpm.portal.GenericAngularApp' : [ 'processportal/js/GenericAngularApp'],
		'bpm.portal.GenericController' : [ 'processportal/js/GenericController'],
		'i18n' : 'common/InfinityBPMI18N',
		'portalSupport' : 'common/js/portalSupport'
	},
	shim: {
		'angularjs': {
			require: "jquery",
			exports: "angular"
		},
		'jquery-ui': ['jquery'],
		'jquery-base64' : ['jquery'],
		'jquery-mobile': ['jquery'],
		'portalSupport' : ['jquery'],
		'bpm.portal.GenericAngularApp' : ['jquery'],
		'bpm.portal.GenericController' : ['jquery']
	}
});


require(["require", "jquery", "jquery-ui", "angularjs", "jquery-base64", "portalSupport", "processportal/js/m_manualActivityPanel",
         "bpm.portal.GenericAngularApp", "bpm.portal.GenericController"], function(){
	require("processportal/js/m_manualActivityPanel").initialize();
});